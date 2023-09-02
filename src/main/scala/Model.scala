// Silent-Lang 表层语法树

enum Raw:
  case Inp
  case Prt(arg: Raw)
  case Num(value: Int)
  case Var(name: String)
  case Lam(param: String, ty: Type, body: Raw)
  case App(func: Raw, arg: Raw)
  case Add(lhs: Raw, rhs: Raw)
  case Let(name: List[String], value: Raw, next: Raw)
  case Alt(lhs: Raw, rhs: Raw, x: Raw, y: Raw)
  case Tup(ls: List[Raw])

// Silent-Lang 底层语法树

enum Term:
  case Inp
  case Prt(arg: Term, ty: Type)
  case Num(value: Int)
  case Var(name: String)
  case Lam(param: String, ty: Type, body: Term)
  case App(func: Term, arg: Term)
  case Add(lhs: Term, rhs: Term, ty: Type)
  case Let(name: List[String], value: Term, next: Term)
  case Alt(lhs: Term, rhs: Term, x: Term, y: Term, ty: Type)
  case Tup(ls: List[Term])

// 带类型的 Term

case class TmPack(tm: Term, ty: Type)

// LLVM-IR 类型

enum Type:
  case I32
  case Fun(from: Type, to: Type)
  case Tup(ls: List[Type])

  override def toString() = this match
    case I32           => "i32"
    case Fun(from, to) => "ptr"
    case Tup(ls)       => throw new Exception("can't serialize tuple type")

// LLVM-IR 值（不含操作序列）

enum IRVal:
  case Num(value: Int)
  case Var(name: String)
  case Tup(ls: List[IRVal])
  case Lam(fn: IRVal => IRPack)

  override def toString() = this match
    case Num(value) => s"$value"
    case Var(name)  => s"%$name"
    case Tup(ls)    => throw new Exception("can't serialize tuple")
    case Lam(fn)    => throw new Exception("can't serialize function")

// LLVM-IR 操作

enum IROp:
  case Inp(res: String)
  case Prt(arg: IRVal, ty: Type)
  case Add(res: String, ty: Type, lhs: IRVal, rhs: IRVal)
  case Let(res: String, ty: Type, value: IRVal)
  case Alt(res: String, ty: Type, lhs: IRVal, rhs: IRVal, x: IRPack, y: IRPack)

  override def toString() = this match
    case Inp(res)               => s"  %$res = call i32 @input()"
    case Prt(arg, ty)           => s"  call void @print($ty noundef $arg)"
    case Add(res, ty, lhs, rhs) => s"  %$res = add nsw $ty $lhs, $rhs"
    case Let(res, ty, value)    => s"  %$res = $value"
    case Alt(res, ty, lhs, rhs, x, y) =>
      val (flag, ptr, l1, l2, l3) = (fresh, fresh, fresh, fresh, fresh)
      s"""  %$flag = icmp eq i32 $lhs, $rhs
  %$ptr = alloca i32, align 4
  br i1 %$flag, label %$l1, label %$l2

$l1:
${x.store(ptr, ty)}
  br label %$l3

$l2:
${y.store(ptr, ty)}
  br label %$l3

$l3:
  %$res = load i32, ptr %$ptr, align 4"""

// LLVM-IR 操作序列

case class IROps(ops: List[IROp]):
  def add(irop: IROp) = IROps(irop :: ops)
  def add(irops: IROps) =
    val IROps(newOps) = irops
    IROps(newOps ++ ops)
  override def toString() =
    ops.reverse.map(op => s"$op\n").mkString("")

object IROps:
  def empty = IROps(List())
  def from(irop: IROp) = IROps(List(irop))

// LLVM-IR 值

case class IRPack(value: IRVal, ops: IROps):
  def store(res: String, ty: Type) =
    s"""$ops  store $ty $value, ptr %$res, align 4"""
  def prepend(oldOps: IROps) = IRPack(value, oldOps.add(ops))
  override def toString() = s"$ops"
