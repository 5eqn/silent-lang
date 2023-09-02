import scala.io.Source
import java.nio.file.{Files, Paths}

// 一些辅助函数

def isNumeric(ch: Char) = ch >= '0' && ch <= '9'
def isAlphabetic(ch: Char) = ch >= 'A' && ch <= 'Z' || ch >= 'a' && ch <= 'z'

def collect(pred: Char => Boolean, str: String, acc: String): (String, String) =
  str.headOption match
    case Some(hd) if pred(hd) => collect(pred, str.tail, acc + hd);
    case _                    => (acc, str)

def keywords = List(
  "lam",
  "let",
  "in",
  "app",
  "if",
  "then",
  "else",
  "int",
  "ptr",
  "input",
  "print"
)

// 处理结果

enum Result[+A]:
  case Success(res: A, rem: String)
  case Fail

// 分析模块

case class Parser[A](run: String => Result[A]):
  def map[B](cont: A => B) = Parser(str =>
    run(str) match
      case Result.Success(res, rem) => Result.Success(cont(res), rem)
      case Result.Fail              => Result.Fail
  )
  def flatMap[B](cont: A => Parser[B]) = Parser(str =>
    run(str) match
      case Result.Success(res, rem) => cont(res).run(rem.trim())
      case Result.Fail              => Result.Fail
  )
  def |(another: Parser[A]) = Parser(str =>
    run(str) match
      case Result.Success(res, rem) => Result.Success(res, rem)
      case Result.Fail              => another.run(str)
  )

// 一些分析函数

def success[A](res: A) = Parser(str => Result.Success(res, str))

def exact(exp: Char) = Parser(str =>
  str.headOption match
    case Some(hd) if hd == exp => Result.Success(hd, str.tail)
    case _                     => Result.Fail
)

def exact(exp: String) = Parser(str =>
  str.stripPrefix(exp) match
    case rem if rem.length() < str.length() => Result.Success(exp, rem)
    case _                                  => Result.Fail
)

def number = Parser(str =>
  val (res, rem) = collect(isNumeric, str, "")
  if res.length() > 0
  then Result.Success(res.toInt, rem)
  else Result.Fail
)

def ident = Parser(str =>
  val (res, rem) = collect(isAlphabetic, str, "")
  if res.length() > 0 && !keywords.contains(res)
  then Result.Success(res, rem)
  else Result.Fail
)

// 编程语言的数据结构

enum Term:
  case Inp
  case Prt(arg: Term)
  case Num(value: Int)
  case Var(name: String)
  case Lam(param: String, ty: IRType, body: Term)
  case App(func: Term, arg: Term)
  case Add(lhs: Term, rhs: Term)
  case Let(name: String, value: Term, next: Term)
  case Alt(lhs: Term, rhs: Term, x: Term, y: Term)

// 让机器理解编程语言

def inp = exact("input").map(_ => Term.Inp)

def pos = number.map(value => Term.Num(value))

def neg = for {
  _ <- exact('-')
  value <- number
} yield Term.Num(-value)

def vrb = ident.map(name => Term.Var(name))

def brk = for {
  _ <- exact('(')
  tm <- term
  _ <- exact(')')
} yield tm

def atm = inp | pos | neg | vrb | brk | prt

def irInt = exact("int").map(_ => IRType.I32)
def irPtr = exact("ptr").map(_ => IRType.Ptr)
def irType = irInt | irPtr

def lam = for {
  _ <- exact('(')
  param <- ident
  _ <- exact(':')
  ty <- irType
  _ <- exact(')')
  _ <- exact("=>")
  body <- term
} yield Term.Lam(param, ty, body)

def app = for {
  lhs <- atm
  res <- appRest(lhs)
} yield res

def appRest(lhs: Term): Parser[Term] = (for {
  _ <- exact('(')
  rhs <- term
  _ <- exact(')')
  res <- appRest(Term.App(lhs, rhs))
} yield res) | success(lhs)

def add = for {
  lhs <- app
  res <- addRest(lhs)
} yield res

def addRest(lhs: Term): Parser[Term] = (for {
  _ <- exact('+')
  rhs <- app
  res <- addRest(Term.Add(lhs, rhs))
} yield res) | success(lhs)

def prt = for {
  _ <- exact("print")
  _ <- exact('(')
  arg <- term
  _ <- exact(')')
} yield Term.Prt(arg)

def let = for {
  _ <- exact("let")
  name <- ident
  _ <- exact('=')
  value <- term
  _ <- exact("in")
  next <- term
} yield Term.Let(name, value, next)

def alt = for {
  _ <- exact("if")
  lhs <- term
  _ <- exact("==")
  rhs <- term
  _ <- exact("then")
  x <- term
  _ <- exact("else")
  y <- term
} yield Term.Alt(lhs, rhs, x, y)

def term: Parser[Term] = lam | add | let | alt

// LLVM-IR 语法树

enum IRType:
  case I32
  case Ptr

  override def toString() = this match
    case I32 => "i32"
    case Ptr => "ptr"

enum IRVal:
  case Num(value: Int)
  case Var(name: String)
  case Lam(fn: (IRVal, IROps) => IRPack)

  override def toString() = this match
    case Num(value) => s"$value"
    case Var(name)  => s"%$name"
    case Lam(fn)    => throw new Exception("can't serialize function")

enum IROp:
  case Inp(res: String)
  case Prt(arg: IRVal, ty: IRType)
  case Add(res: String, lhs: IRVal, rhs: IRVal)
  case Let(res: String, value: IRVal)
  case Alt(res: String, lhs: IRVal, rhs: IRVal, x: IRPack, y: IRPack)

  override def toString() = this match
    case Inp(res)           => s"  %$res = call i32 @input()"
    case Prt(arg, ty)       => s"  call void @print($ty noundef $arg)"
    case Add(res, lhs, rhs) => s"  %$res = add nsw i32 $lhs, $rhs"
    case Let(res, value)    => s"  %$res = $value"
    case Alt(res, lhs, rhs, x, y) =>
      val flag = fresh
      val ptr = fresh
      val label1 = fresh
      val label2 = fresh
      val label3 = fresh
      s"""  %$flag = icmp eq i32 $lhs, $rhs
  %$ptr = alloca i32, align 4
  br i1 %$flag, label %$label1, label %$label2

$label1:
${x.store(ptr)}
  br label %$label3

$label2:
${y.store(ptr)}
  br label %$label3

$label3:
  %$res = load i32, ptr %$ptr, align 4"""

case class IROps(ops: List[IROp]):
  def add(irop: IROp) = IROps(irop :: ops)
  override def toString() =
    ops.reverse.map(op => s"$op\n").mkString("")

object IROps:
  def empty = IROps(List())

case class IRPack(value: IRVal, ty: IRType, ops: IROps):
  def store(res: String) = s"""$ops  store $ty $value, ptr %$res, align 4"""
  override def toString() = s"$ops"

// 新变量名生成器

var counter = 0

def fresh =
  counter += 1
  s"x$counter"

// 语境

case class Ctx(types: Map[String, IRType], values: Map[String, IRVal]):
  def bind(name: String, ty: IRType, value: IRVal) =
    Ctx(types + (name -> ty), values + (name -> value))
  def typeOf(name: String) = types(name)
  def valueOf(name: String) = values(name)

object Ctx:
  def empty =
    Ctx(Map("input" -> IRType.I32), Map("input" -> IRVal.Var("input")))

// 部分求值

def pEval(ctx: Ctx, term: Term, ops: IROps): IRPack = term match

  // 对输入操作单独使用一条指令
  case Term.Inp =>
    val name = fresh
    IRPack(IRVal.Var(name), IRType.I32, ops.add(IROp.Inp(name)))

  // 对输出操作单独使用一条指令
  case Term.Prt(arg) =>
    val IRPack(av, at, aop) = pEval(ctx, arg, ops)
    IRPack(av, at, aop.add(IROp.Prt(av, at)))

  // 数字直接返回
  case Term.Num(value) => IRPack(IRVal.Num(value), IRType.I32, ops)

  // 变量要查表得到值
  case Term.Var(name) => IRPack(ctx.valueOf(name), ctx.typeOf(name), ops)

  // 函数收到值之后再 pEval，且继承 IROps，因为 inline
  case Term.Lam(param, ty, body) =>
    val f = (arg: IRVal, ops: IROps) =>
      pEval(ctx.bind(param, ty, arg), body, ops)
    IRPack(IRVal.Lam(f), IRType.Ptr, ops)

  // 先求出函数和参数，然后直接传参
  case Term.App(func, arg) =>
    val IRPack(fv, ft, fop) = pEval(ctx, func, ops)
    val IRPack(av, at, aop) = pEval(ctx, arg, fop)
    fv match
      case IRVal.Lam(fn) => fn(av, aop)
      case _             => throw new Exception("app lhs is not a function")

  // 加法，先求加法两边的值
  case Term.Add(lhs, rhs) =>
    val IRPack(lv, lt, lop) = pEval(ctx, lhs, ops)
    val IRPack(rv, rt, rop) = pEval(ctx, rhs, lop)

    // 希望这两个都是整数
    if lt != IRType.I32 || rt != IRType.I32 then
      throw new Exception("add non-numbers")
    (lv, rv) match

      // 如果两个都是数，就可以直接化简
      case (IRVal.Num(a), IRVal.Num(b)) =>
        IRPack(IRVal.Num(a + b), IRType.I32, rop)

      // 否则新建一个变量存储这个加法成果
      case _ =>
        val name = fresh
        val newOp = IROp.Add(name, lv, rv)
        IRPack(IRVal.Var(name), IRType.I32, rop.add(newOp))

  // 定义变量直接转移值
  case Term.Let(name, value, next) =>
    val IRPack(vv, vt, ops2) = pEval(ctx, value, ops)
    pEval(ctx.bind(name, vt, vv), next, ops2)

  // 选择分支，先求等式两边的值
  case Term.Alt(lhs, rhs, x, y) =>
    val IRPack(lv, lt, lop) = pEval(ctx, lhs, ops)
    val IRPack(rv, rt, rop) = pEval(ctx, rhs, lop)

    // 希望这两个值是整数
    if lt != IRType.I32 || rt != IRType.I32 then
      throw new Exception("compare non-numbers")
    (lv, rv) match

      // 两个值可以直接判断，就直接化简
      case (IRVal.Num(a), IRVal.Num(b)) =>
        if a == b
        then pEval(ctx, x, rop)
        else pEval(ctx, y, rop)

      // 否则，考虑到两边在分支里面，要对 IROps 另起炉灶
      case _ =>
        val IRPack(xv, xt, xop) = pEval(ctx, x, IROps.empty)
        val IRPack(yv, yt, yop) = pEval(ctx, y, IROps.empty)

        // 希望这两个值类型相同
        if xt != yt then throw new Exception("if cases type mismatch")

        // 构造出 Alt 操作
        val name = fresh
        val xpk = IRPack(xv, xt, xop)
        val ypk = IRPack(yv, yt, yop)
        val newOp = IROp.Alt(name, lv, rv, xpk, ypk)
        IRPack(IRVal.Var(name), xt, rop.add(newOp))

// 编译结果输出

def fileStart =
  """target datalayout = "e-m:e-p270:32:32-p271:32:32-p272:64:64-i64:64-f80:128-n8:16:32:64-S128"
target triple = "x86_64-pc-linux-gnu"

@.str = private unnamed_addr constant [3 x i8] c"%d\00", align 1
@.str.1 = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1

; Function Attrs: noinline nounwind optnone sspstrong uwtable
define dso_local i32 @input() #0 {
  %1 = alloca i32, align 4
  %2 = call i32 (ptr, ...) @__isoc99_scanf(ptr noundef @.str, ptr noundef %1)
  %3 = load i32, ptr %1, align 4
  ret i32 %3
}

declare i32 @__isoc99_scanf(ptr noundef, ...) #1

; Function Attrs: noinline nounwind optnone sspstrong uwtable
define dso_local void @print(i32 noundef %0) #0 {
  %2 = alloca i32, align 4
  store i32 %0, ptr %2, align 4
  %3 = load i32, ptr %2, align 4
  %4 = call i32 (ptr, ...) @printf(ptr noundef @.str.1, i32 noundef %3)
  ret void
}

declare i32 @printf(ptr noundef, ...) #1

; Function Attrs: noinline nounwind optnone sspstrong uwtable
define dso_local i32 @main() #0 {
"""

def fileEnd = """  ret i32 0
}

attributes #0 = { noinline nounwind optnone sspstrong uwtable "frame-pointer"="all" "min-legal-vector-width"="0" "no-trapping-math"="true" "stack-protector-buffer-size"="8" "target-cpu"="x86-64" "target-features"="+cx8,+fxsr,+mmx,+sse,+sse2,+x87" "tune-cpu"="generic" }
attributes #1 = { "frame-pointer"="all" "no-trapping-math"="true" "stack-protector-buffer-size"="8" "target-cpu"="x86-64" "target-features"="+cx8,+fxsr,+mmx,+sse,+sse2,+x87" "tune-cpu"="generic" }

!llvm.module.flags = !{!0, !1, !2, !3, !4}
!llvm.ident = !{!5}

!0 = !{i32 1, !"wchar_size", i32 4}
!1 = !{i32 8, !"PIC Level", i32 2}
!2 = !{i32 7, !"PIE Level", i32 2}
!3 = !{i32 7, !"uwtable", i32 2}
!4 = !{i32 7, !"frame-pointer", i32 2}
!5 = !{!"clang version 16.0.6"}
"""

def output(code: String) =
  val content = fileStart + code + fileEnd
  val filePath = Paths.get(s"sample/$fileName.ll")
  Files.write(filePath, content.getBytes)

// 从这里开始运行

val fileName = "fn"

@main def run() =
  val src = Source.fromFile(s"sample/$fileName.silent")
  val str = src.mkString
  src.close()
  term.run(str) match
    case Result.Fail => println("Parse failed")
    case Result.Success(res, rem) =>
      val pk = pEval(Ctx.empty, res, IROps.empty)
      output(s"$pk")
