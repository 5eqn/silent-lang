import scala.io.Source
import java.nio.file.{Files, Paths}

// 一些辅助函数

def isNumeric(ch: Char) = ch >= '0' && ch <= '9'
def isAlphabetic(ch: Char) = ch >= 'A' && ch <= 'Z' || ch >= 'a' && ch <= 'z'

def collect(pred: Char => Boolean, str: String, acc: String): (String, String) =
  str.headOption match
    case Some(hd) if pred(hd) => collect(pred, str.tail, acc + hd);
    case _                    => (acc, str)

def keywords =
  List("lam", "let", "in", "app", "add", "if", "then", "else", "int", "ptr")

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

def irtype = Parser(str =>
  val (res, rem) = collect(isAlphabetic, str, "")
  if res == "int"
  then Result.Success(IRType.I32, rem)
  else if res == "ptr"
  then Result.Success(IRType.Ptr, rem)
  else Result.Fail
)

// 编程语言的数据结构

enum Term:
  case Inp
  case Num(value: Int)
  case Var(name: String)
  case Lam(param: String, ty: IRType, body: Term)
  case App(func: Term, arg: Term)
  case Add(lhs: Term, rhs: Term)
  case Let(name: String, value: Term, next: Term)
  case Alt(lhs: Term, rhs: Term, x: Term, y: Term)

// 让机器理解编程语言

def inp = exact('_').map(_ => Term.Inp)

def pos = number.map(value => Term.Num(value))

def neg = for {
  _ <- exact('-')
  value <- number
} yield Term.Num(-value)

def atom = ident.map(name => Term.Var(name))

def lam = for {
  _ <- exact('(')
  param <- ident
  _ <- exact(':')
  ty <- irtype
  _ <- exact(')')
  _ <- exact("=>")
  body <- term
} yield Term.Lam(param, ty, body)

def app = for {
  _ <- exact("app")
  _ <- exact('(')
  func <- term
  _ <- exact(',')
  arg <- term
  _ <- exact(')')
} yield Term.App(func, arg)

def add = for {
  _ <- exact("add")
  _ <- exact('(')
  lhs <- term
  _ <- exact(',')
  rhs <- term
  _ <- exact(')')
} yield Term.Add(lhs, rhs)

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

def term: Parser[Term] = inp | pos | neg | atom | lam | app | add | let | alt

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
  case Lam(fn: IRVal => (IRVal, IRType))

  override def toString() = this match
    case Num(value) => value.toString()
    case Var(name)  => s"%$name"
    case Lam(fn)    => throw new Exception("can't serialize function")

  def print(ty: IRType) = s"  call void @print($ty noundef $this)"

enum IROp:
  case Inp(res: String)
  case Add(res: String, ty: IRType, lhs: IRVal, rhs: IRVal)
  case Let(res: String, value: IRVal)

  override def toString() = this match
    case Inp(res)               => s"  %$res = call i32 @input()"
    case Add(res, ty, lhs, rhs) => s"  %$res = add nsw $ty $lhs $rhs"
    case Let(res, value)        => s"  %$res = $value"

// 操作列表

var irops = List[IROp]()

def push(irop: IROp) =
  irops = irop :: irops

def opsStr = irops.reverse.map(op => op.toString() + "\n").mkString("")

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

def pEval(ctx: Ctx, term: Term): (IRVal, IRType) = term match
  case Term.Inp =>
    val name = fresh
    push(IROp.Inp(name))
    (IRVal.Var(name), IRType.I32)
  case Term.Num(value) => (IRVal.Num(value), IRType.I32)
  case Term.Var(name) =>
    (ctx.valueOf(name), ctx.typeOf(name))
  case Term.Lam(param, ty, body) =>
    (IRVal.Lam(arg => pEval(ctx.bind(param, ty, arg), body)), IRType.Ptr)
  case Term.App(func, arg) =>
    val (fv, ft) = pEval(ctx, func)
    val (av, at) = pEval(ctx, arg)
    fv match
      case IRVal.Lam(fn) => fn(av)
      case _             => throw new Exception("app lhs is not a function")
  case Term.Add(lhs, rhs) =>
    val (lv, lt) = pEval(ctx, lhs)
    val (rv, rt) = pEval(ctx, rhs)
    if lt != IRType.I32 || rt != IRType.I32 then
      throw new Exception("add non-numbers")
    (lv, rv) match
      case (IRVal.Num(a), IRVal.Num(b)) => (IRVal.Num(a + b), IRType.I32)
      case _ =>
        val name = fresh
        push(IROp.Add(name, IRType.I32, lv, rv))
        (IRVal.Var(name), IRType.I32)
  case Term.Let(name, value, next) =>
    val (vv, vt) = pEval(ctx, value)
    pEval(ctx.bind(name, vt, vv), next)
  case Term.Alt(lhs, rhs, x, y) =>
    throw new Exception("control flow is not supported")

// 编译结果输出

def fileStart = """
; ModuleID = 'main.c'
source_filename = "main.c"
target datalayout = "e-m:e-p270:32:32-p271:32:32-p272:64:64-i64:64-f80:128-n8:16:32:64-S128"
target triple = "x86_64-pc-linux-gnu"

@.str = private unnamed_addr constant [3 x i8] c"%d\00", align 1

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
  %4 = call i32 (ptr, ...) @printf(ptr noundef @.str, i32 noundef %3)
  ret void
}

declare i32 @printf(ptr noundef, ...) #1

; Function Attrs: noinline nounwind optnone sspstrong uwtable
define dso_local i32 @main() #0 {
"""

def fileEnd = """
  ret i32 0
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
  val filePath = Paths.get("sample/fn.ll")
  Files.write(filePath, content.getBytes)

// 从这里开始运行

@main def run() =
  val src = Source.fromFile("sample/fn.silent")
  val str = src.mkString
  src.close()
  term.run(str) match
    case Result.Fail => println("Parse failed")
    case Result.Success(res, rem) =>
      val (rv, rt) = pEval(Ctx.empty, res)
      output(opsStr + rv.print(rt))
