// 中缀操作符枚举
enum Oprt:
  case Add
  case Sub
  case Mul
  case Div
  case Mod
  case And
  case Or
  case Xor
  case Gt
  case Lt
  case Ge
  case Le
  case Eq
  case Ne
  case All
  case Any

  // 检查操作符两端的类型是否合法，如果合法，返回操作结果的类型
  def infer(a: Type, b: Type) = (this, a, b) match
    case (Add, Type.I32, Type.I32) => Type.I32
    case (Sub, Type.I32, Type.I32) => Type.I32
    case (Mul, Type.I32, Type.I32) => Type.I32
    case (Div, Type.I32, Type.I32) => Type.I32
    case (Mod, Type.I32, Type.I32) => Type.I32
    case (And, Type.I32, Type.I32) => Type.I32
    case (Or, Type.I32, Type.I32)  => Type.I32
    case (Xor, Type.I32, Type.I32) => Type.I32
    case (Gt, Type.I32, Type.I32)  => Type.Boo
    case (Lt, Type.I32, Type.I32)  => Type.Boo
    case (Ge, Type.I32, Type.I32)  => Type.Boo
    case (Le, Type.I32, Type.I32)  => Type.Boo
    case (Eq, Type.I32, Type.I32)  => Type.Boo
    case (Ne, Type.I32, Type.I32)  => Type.Boo
    case (All, Type.Boo, Type.Boo) => Type.Boo
    case (Any, Type.Boo, Type.Boo) => Type.Boo
    case _ => throw new Exception("operator type mismatch")

  // 给定操作符两边的值，尝试化简
  def tryEval(a: IRVal, b: IRVal) = (this, a, b) match
    case (Add, IRVal.Num(a), IRVal.Num(b)) => Some(IRVal.Num(a + b))
    case (Sub, IRVal.Num(a), IRVal.Num(b)) => Some(IRVal.Num(a - b))
    case (Mul, IRVal.Num(a), IRVal.Num(b)) => Some(IRVal.Num(a * b))
    case (Div, IRVal.Num(a), IRVal.Num(b)) => Some(IRVal.Num(a / b))
    case (Mod, IRVal.Num(a), IRVal.Num(b)) => Some(IRVal.Num(a % b))
    case (And, IRVal.Num(a), IRVal.Num(b)) => Some(IRVal.Num(a & b))
    case (Or, IRVal.Num(a), IRVal.Num(b))  => Some(IRVal.Num(a | b))
    case (Xor, IRVal.Num(a), IRVal.Num(b)) => Some(IRVal.Num(a ^ b))
    case (Gt, IRVal.Num(a), IRVal.Num(b))  => Some(IRVal.Boo(a > b))
    case (Lt, IRVal.Num(a), IRVal.Num(b))  => Some(IRVal.Boo(a < b))
    case (Ge, IRVal.Num(a), IRVal.Num(b))  => Some(IRVal.Boo(a >= b))
    case (Le, IRVal.Num(a), IRVal.Num(b))  => Some(IRVal.Boo(a <= b))
    case (Eq, IRVal.Num(a), IRVal.Num(b))  => Some(IRVal.Boo(a == b))
    case (Ne, IRVal.Num(a), IRVal.Num(b))  => Some(IRVal.Boo(a != b))
    case (All, IRVal.Boo(a), IRVal.Boo(b)) => Some(IRVal.Boo(a && b))
    case (Any, IRVal.Boo(a), IRVal.Boo(b)) => Some(IRVal.Boo(a || b))
    case _                                 => None

  // 转化为 LLVM-IR 语句
  def compile(ty: Type, x: IRVal, y: IRVal) = this match
    case Add => s"add $ty $x, $y"
    case Sub => s"sub $ty $x, $y"
    case Mul => s"mul $ty $x, $y"
    case Div => s"sdiv $ty $x, $y"
    case Mod => s"srem $ty $x, $y"
    case And => s"and $ty $x, $y"
    case Or  => s"or $ty $x, $y"
    case Xor => s"xor $ty $x, $y"
    case Gt  => s"icmp sgt $ty $x, $y"
    case Lt  => s"icmp slt $ty $x, $y"
    case Ge  => s"icmp sge $ty $x, $y"
    case Le  => s"icmp sle $ty $x, $y"
    case Eq  => s"icmp eq $ty $x, $y"
    case Ne  => s"icmp ne $ty $x, $y"
    case All => s"select i1 $x, i1 $y, i1 false"
    case Any => s"select i1 $x, i1 true, i1 $y"

  // 用于调试输出，打出操作符本来的样子
  def symbol = this match
    case Add => "+"
    case Sub => "-"
    case Mul => "*"
    case Div => "/"
    case Mod => "%"
    case And => "&"
    case Or  => "|"
    case Xor => "^"
    case Gt  => ">"
    case Lt  => "<"
    case Ge  => ">="
    case Le  => "<="
    case Eq  => "=="
    case Ne  => "!="
    case All => "&&"
    case Any => "||"

// Silent-Lang 表层语法树
enum Raw:
  case Inp
  case Brk
  case Prt(arg: Raw)
  case Num(value: Int)
  case Boo(value: Boolean)
  case Var(name: String)
  case Lam(param: String, ty: Type, body: Raw)
  case App(func: Raw, arg: Raw)
  case Mid(oprt: Oprt, lhs: Raw, rhs: Raw)
  case Let(name: Names, value: Raw, rec: Option[Raw], next: Raw)
  case Alt(cond: Raw, x: Raw, y: Raw)
  case Tup(ls: List[Raw])

// Silent-Lang 底层语法树
enum Term:
  case Inp
  case Brk
  case Prt(arg: Term, ty: Type)
  case Num(value: Int)
  case Boo(value: Boolean)
  case Var(name: String)
  case Lam(param: String, ty: Type, body: Term)
  case App(func: Term, arg: Term)
  case Mid(oprt: Oprt, lhs: Term, rhs: Term, ty: Type)
  case Let(name: Names, value: Term, rec: Option[Term], next: Term, ty: Type)
  case Alt(cond: Term, x: Term, y: Term, ty: Type)
  case Tup(ls: List[Term])

  // 用于调试输出
  override def toString(): String = this match
    case Inp                     => "input"
    case Brk                     => "nope"
    case Prt(arg, ty)            => s"print($arg)"
    case Num(value)              => s"$value"
    case Boo(value)              => s"$value"
    case Var(name)               => name
    case Lam(param, ty, body)    => s"($param: $ty) => $body"
    case App(func, arg)          => s"$func($arg)"
    case Mid(oprt, lhs, rhs, ty) => s"$lhs ${oprt.symbol} $rhs"
    case Let(name, value, rec, next, ty) =>
      val ns = name.mkString(", ")
      rec match
        case None     => s"let $ns: $ty = $value in\n$next"
        case Some(rv) => s"let $ns: $ty = $value\nrec $rv in\n$next"
    case Alt(cond, x, y, ty) => s"if $cond then $x else $y"
    case Tup(ls)             => ls.map(_.toString()).mkString(", ")

// 类型枚举
enum Type:
  case I32
  case Boo
  case Fun(from: Type, to: Type)
  case Tup(ls: List[Type])
  case Any

  override def toString() = this match
    case I32           => "i32"
    case Boo           => "i1"
    case Fun(from, to) => "ptr"
    case Tup(ls)       => ls.map(_.toString()).mkString(", ")
    case Any           => "any"

// 带类型的 Term，是 Infer 的结果
case class TmPack(tm: Term, ty: Type)
