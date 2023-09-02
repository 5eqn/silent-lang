// Silent-Lang 语法树

enum Term:
  case Inp
  case Prt(arg: Term)
  case Num(value: Int)
  case Var(name: String)
  case Lam(param: String, ty: IRType, body: Term)
  case App(func: Term, arg: Term)
  case Add(lhs: Term, rhs: Term)
  case Let(name: List[String], value: Term, next: Term)
  case Alt(lhs: Term, rhs: Term, x: Term, y: Term)
  case Tup(ls: List[Term])

// LLVM-IR 类型

enum IRType:
  case I32
  case Ptr
  case Tup(ls: List[IRType])

  override def toString() = this match
    case I32     => "i32"
    case Ptr     => "ptr"
    case Tup(ls) => throw new Exception("can't serialize tuple type")

// LLVM-IR 值（不含操作序列）

enum IRVal:
  case Num(value: Int)
  case Var(name: String)
  case Tup(ls: List[IRVal])
  case Lam(fn: (IRVal, IROps) => IRPack)

  override def toString() = this match
    case Num(value) => s"$value"
    case Var(name)  => s"%$name"
    case Tup(ls)    => throw new Exception("can't serialize tuple")
    case Lam(fn)    => throw new Exception("can't serialize function")

// LLVM-IR 操作

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
      val (flag, ptr, l1, l2, l3) = (fresh, fresh, fresh, fresh, fresh)
      s"""  %$flag = icmp eq i32 $lhs, $rhs
  %$ptr = alloca i32, align 4
  br i1 %$flag, label %$l1, label %$l2

$l1:
${x.store(ptr)}
  br label %$l3

$l2:
${y.store(ptr)}
  br label %$l3

$l3:
  %$res = load i32, ptr %$ptr, align 4"""

// LLVM-IR 操作序列

case class IROps(ops: List[IROp]):
  def add(irop: IROp) = IROps(irop :: ops)
  override def toString() =
    ops.reverse.map(op => s"$op\n").mkString("")

object IROps:
  def empty = IROps(List())

// LLVM-IR 值

case class IRPack(value: IRVal, ty: IRType, ops: IROps):
  def store(res: String) = s"""$ops  store $ty $value, ptr %$res, align 4"""
  override def toString() = s"$ops"
