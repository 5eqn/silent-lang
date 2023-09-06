// LLVM-IR 值（不含操作序列）
enum IRVal:
  case Brk
  case Num(value: Int)
  case Boo(value: Boolean)
  case Var(name: String)
  case Tup(ls: List[IRVal])
  case Lam(fn: IRVal => IRPack)

  override def toString() = this match
    case Brk        => "nope"
    case Num(value) => s"$value"
    case Boo(value) => s"$value"
    case Var(name)  => s"%$name"
    case Tup(ls)    => s"$ls"
    case Lam(fn)    => "lam"

// LLVM-IR 操作
enum IROp:
  case Brk
  case Inp(res: String)
  case Prt(arg: IRVal, ty: Type)
  case Mid(oprt: Oprt, res: String, ty: Type, lhs: IRVal, rhs: IRVal)
  case Alt(res: Names, ty: Types, cond: IRVal, x: IRPack, y: IRPack)
  case Rec(res: Names, ty: Types, init: IRVal, rec: IRPack)

  // 把操作转化成 LLVM-IR 代码字符串
  def compile(exit: Option[String]): String = this match

    // 遇到 Nope，检查 exit 变量，如果不为空就跳转
    case Brk =>
      exit match
        case None        => throw new Exception("no loop to nope")
        case Some(label) => s"  br label %$label"

    // 输入、打印、相加指令都能直接翻译
    case Inp(res)     => s"  %$res = call i32 @input()"
    case Prt(arg, ty) => s"  call void @print($ty noundef $arg)"
    case Mid(oprt, res, ty, lhs, rhs) =>
      s"  %$res = ${oprt.compile(ty, lhs, rhs)}"

    // 遇到选择块，先搞出两个分支的值列表
    case Alt(res, ty, cond, x, y) =>
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
      s"""$alloc
  br i1 $cond, label %$l1, label %$l2

$l1:
${xop.compile(exit)}
$storeX
  br label %$l3

$l2:
${yop.compile(exit)}
$storeY
  br label %$l3

$l3:
$load"""

    // 遇到递归块，先搞出初始值和递归值的列表
    case Rec(res, ty, iv, IRPack(rv, rop)) =>
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
      val newExit = Some(l2)
      val ret = s"""$alloc
$init
  br label %$l1

$l1:
$load
${rop.compile(newExit)}
$store
  br label %$l1

$l2:"""
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
  def compile(exit: Option[String]) =
    ops.map(op => op.compile(exit)).mkString("\n")

object IROps:
  def empty = IROps(List())
  def from(irop: IROp) = IROps(List(irop))

// LLVM-IR 值，是 Partial Eval 的结果
case class IRPack(value: IRVal, ops: IROps):
  def prepend(oldOps: IROps) = IRPack(value, oldOps.add(ops))
  def compile(exit: Option[String]) = ops.compile(exit)
