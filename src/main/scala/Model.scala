// Silent-Lang 表层语法树
enum Raw:
  case Inp
  case Brk
  case Prt(arg: Raw)
  case Num(value: Int)
  case Var(name: String)
  case Lam(param: String, ty: Type, body: Raw)
  case App(func: Raw, arg: Raw)
  case Add(lhs: Raw, rhs: Raw)
  case Let(name: Names, value: Raw, rec: Option[Raw], next: Raw)
  case Alt(lhs: Raw, rhs: Raw, x: Raw, y: Raw)
  case Tup(ls: List[Raw])

// Silent-Lang 底层语法树
enum Term:
  case Inp
  case Brk
  case Prt(arg: Term, ty: Type)
  case Num(value: Int)
  case Var(name: String)
  case Lam(param: String, ty: Type, body: Term)
  case App(func: Term, arg: Term)
  case Add(lhs: Term, rhs: Term, ty: Type)
  case Let(name: Names, value: Term, rec: Option[Term], next: Term, ty: Type)
  case Alt(lhs: Term, rhs: Term, x: Term, y: Term, ty: Type)
  case Tup(ls: List[Term])

  // 调试输出
  override def toString(): String = this match
    case Inp                  => "input"
    case Brk                  => "nope"
    case Prt(arg, ty)         => s"print($arg)"
    case Num(value)           => s"$value"
    case Var(name)            => name
    case Lam(param, ty, body) => s"($param: $ty) => $body"
    case App(func, arg)       => s"$func($arg)"
    case Add(lhs, rhs, ty)    => s"$lhs + $rhs"
    case Let(name, value, rec, next, ty) =>
      val ns = name.mkString(", ")
      rec match
        case None     => s"let $ns: $ty = $value in\n$next"
        case Some(rv) => s"let $ns: $ty = $value\nrec $rv in\n$next"
    case Alt(lhs, rhs, x, y, ty) => s"if $lhs == $rhs then $x else $y"
    case Tup(ls)                 => ls.map(_.toString()).mkString(", ")

// 带类型的 Term
case class TmPack(tm: Term, ty: Type)

// LLVM-IR 类型
enum Type:
  case I32
  case Fun(from: Type, to: Type)
  case Tup(ls: List[Type])
  case Any

  override def toString() = this match
    case I32           => "i32"
    case Fun(from, to) => "ptr"
    case Tup(ls)       => ls.map(_.toString()).mkString(", ")
    case Any           => "any"

// LLVM-IR 值（不含操作序列）
enum IRVal:
  case Brk
  case Num(value: Int)
  case Var(name: String)
  case Tup(ls: List[IRVal])
  case Lam(fn: IRVal => IRPack)

  override def toString() = this match
    case Num(value) => s"$value"
    case Var(name)  => s"%$name"
    case Tup(ls)    => s"$ls"
    case Lam(fn)    => throw new Exception("can't serialize function")
    case Brk        => throw new Exception("can't serialize nope")

// 存储当前如果 Nope 了要跳到哪里
var exit = ""

// LLVM-IR 操作
enum IROp:
  case Brk
  case Inp(res: String)
  case Prt(arg: IRVal, ty: Type)
  case Add(res: String, ty: Type, lhs: IRVal, rhs: IRVal)
  case Alt(res: Names, ty: Types, l: IRVal, r: IRVal, x: IRPack, y: IRPack)
  case Rec(res: Names, ty: Types, init: IRPack, rec: IRPack)

  // 把操作转化成 LLVM-IR 代码字符串
  override def toString() = this match

    // 遇到 Nope，检查 exit 变量，如果不为空就跳转
    case Brk =>
      if exit == "" then throw new Exception("no loop to nope")
      else s"  br label %$exit"

    // 输入、打印、相加指令都能直接翻译
    case Inp(res)               => s"  %$res = call i32 @input()"
    case Prt(arg, ty)           => s"  call void @print($ty noundef $arg)"
    case Add(res, ty, lhs, rhs) => s"  %$res = add nsw $ty $lhs, $rhs"

    // 遇到选择块，先搞出两个分支的值列表
    case Alt(res, ty, lhs, rhs, x, y) =>
      val IRPack(xv, xop) = x
      val IRPack(yv, yop) = y
      val xvs = xv match
        case IRVal.Tup(ls) => ls
        case _             => List(xv)
      val yvs = yv match
        case IRVal.Tup(ls) => ls
        case _             => List(yv)

      // 初始化指针列表
      val ptrs = ty.map(IRPtr.next(_))
      val alloc = ptrs.map(_.alloca).mkString("\n")

      // 把两个分支的值存到指针里
      val storeX = ptrs.zip(xvs).map((p, v) => p.store(v)).mkString("\n")
      val storeY = ptrs.zip(yvs).map((p, v) => p.store(v)).mkString("\n")

      // 读取最终结果
      val load = ptrs.zip(res).map((p, v) => p.load(v)).mkString("\n")

      // 构造出分支字符串
      val (flag, l1, l2, l3) = (fresh, fresh, fresh, fresh)
      s"""  %$flag = icmp eq i32 $lhs, $rhs
$alloc
  br i1 %$flag, label %$l1, label %$l2

$l1:
$xop
$storeX
  br label %$l3

$l2:
$yop
$storeY
  br label %$l3

$l3:
$load"""

    // 遇到递归块，先搞出初始值和递归值的列表
    case Rec(res, ty, IRPack(iv, iop), IRPack(rv, rop)) =>
      val ivs = iv match
        case IRVal.Tup(ls) => ls
        case _             => List(iv)
      val rvs = rv match
        case IRVal.Tup(ls) => ls
        case _             => List(rv)

      // 初始化指针及其值
      val ptrs = ty.map(IRPtr.next(_))
      val alloc = ptrs.map(_.alloca).mkString("\n")
      val init = ptrs.zip(ivs).map((p, v) => p.store(v)).mkString("\n")

      // 循环第一步：从指针读取值
      val load = ptrs.zip(res).map((p, v) => p.load(v)).mkString("\n")

      // 循环第二步：把值放到指针里
      val store = ptrs.zip(rvs).map((p, v) => p.store(v)).mkString("\n")

      // 构建循环字符串
      val (l1, l2) = (fresh, fresh)
      exit = l2
      val ret = s"""$alloc
$iop
$init
  br label %$l1

$l1:
$load
$rop
$store
  br label %$l1

$l2:"""
      exit = ""
      ret

// LLVM-IR 指针
case class IRPtr(name: String, ty: Type):
  def store(v: IRVal) = v match
    case IRVal.Brk => ""
    case _         => s"""  store $ty $v, ptr %$name, align 4"""
  def alloca = s"  %$name = alloca $ty, align 4"
  def load(to: String) = s"  %$to = load $ty, ptr %$name, align 4"

object IRPtr:
  def next(ty: Type) = IRPtr(fresh, ty)

// LLVM-IR 操作序列
case class IROps(ops: List[IROp]):
  def add(irop: IROp) = IROps(ops :+ irop)
  def add(irops: IROps) =
    val IROps(newOps) = irops
    IROps(ops ++ newOps)
  override def toString() =
    ops.map(op => s"$op").mkString("\n")

object IROps:
  def empty = IROps(List())
  def from(irop: IROp) = IROps(List(irop))

// LLVM-IR 值
case class IRPack(value: IRVal, ops: IROps):
  def prepend(oldOps: IROps) = IRPack(value, oldOps.add(ops))
  override def toString() = s"$ops"
