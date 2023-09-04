// 位置信息
case class Position(line: Int, col: Int, content: String)

object Position:
  def empty = Position(0, 0, "")

// 有位置的东西
trait Positional:
  var pos = Position.empty

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
  "rec",
  "app",
  "if",
  "then",
  "else",
  "int",
  "ptr",
  "input",
  "print",
  "nope"
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

def some[A](p: Parser[A]) = for {
  lhs <- p
  res <- someRest(List(lhs), p)
} yield res

def someRest[A](lhs: List[A], p: Parser[A]): Parser[List[A]] = (for {
  _ <- exact(',')
  rhs <- p
  res <- someRest(lhs :+ rhs, p)
} yield res) | success(lhs)

def optional[A](p: Parser[A]) = p.map(Some(_)) | success(None)

def oprts = List(
  exact('&').map(_ => Oprt.And)
    | exact('|').map(_ => Oprt.Or)
    | exact('^').map(_ => Oprt.Xor),
  exact('*').map(_ => Oprt.Mul)
    | exact('/').map(_ => Oprt.Div)
    | exact('%').map(_ => Oprt.Mod),
  exact('+').map(_ => Oprt.Add)
    | exact('-').map(_ => Oprt.Sub),
  exact(">=").map(_ => Oprt.Ge)
    | exact("<=").map(_ => Oprt.Le)
    | exact('>').map(_ => Oprt.Gt)
    | exact('<').map(_ => Oprt.Lt)
    | exact("==").map(_ => Oprt.Eq)
    | exact("!=").map(_ => Oprt.Ne),
  exact("&&").map(_ => Oprt.All)
    | exact("||").map(_ => Oprt.Any)
)

// 原子操作符
def inp = exact("input").map(_ => Raw.Inp)

def brk = exact("nope").map(_ => Raw.Brk)

def pos = number.map(Raw.Num(_))

def neg = for {
  _ <- exact('-')
  value <- number
} yield Raw.Num(-value)

def boo = exact("true").map(_ => Raw.Boo(true)) |
  exact("false").map(_ => Raw.Boo(false))

def vrb = ident.map(Raw.Var(_))

def par = for {
  _ <- exact('(')
  tm <- term
  _ <- exact(')')
} yield tm

def prt = for {
  _ <- exact("print")
  _ <- exact('(')
  arg <- term
  _ <- exact(')')
} yield Raw.Prt(arg)

def atm = inp | brk | pos | neg | boo | vrb | par | prt

// 函数应用
def app = for {
  lhs <- atm
  res <- appRest(lhs)
} yield res

def appRest(lhs: Raw): Parser[Raw] = (for {
  _ <- exact('(')
  rhs <- term
  _ <- exact(')')
  res <- appRest(Raw.App(lhs, rhs))
} yield res) | success(lhs)

// 中缀运算符
def mid(level: Int): Parser[Raw] = level match
  case 0 => app
  case _ =>
    for {
      lhs <- mid(level - 1)
      res <- midRest(level, lhs)
    } yield res

def midRest(level: Int, lhs: Raw): Parser[Raw] = (for {
  op <- oprts(level - 1)
  rhs <- mid(level - 1)
  res <- midRest(level, Raw.Mid(op, lhs, rhs))
} yield res) | success(lhs)

// 元组
def tup = some(mid(oprts.length)).map(ls =>
  if ls.length == 1 then ls(0) else Raw.Tup(ls)
)

// 类型
def tyInt = exact("int").map(_ => Type.I32)

def tyPar = for {
  _ <- exact('(')
  res <- tyAny
  _ <- exact(')')
} yield res

def tyAtom: Parser[Type] = tyInt | tyPar

def tyFun = for {
  lhs <- tyAtom
  res <- tyFunRest(lhs)
} yield res

def tyFunRest(lhs: Type): Parser[Type] = (for {
  _ <- exact("->")
  rhs <- tyFun
} yield Type.Fun(lhs, rhs)) | success(lhs)

def tyAny =
  some(tyFun).map(ls => if ls.length == 1 then ls(0) else Type.Tup(ls))

// 匿名函数
def lam = for {
  _ <- exact('(')
  param <- ident
  _ <- exact(':')
  ty <- tyAny
  _ <- exact(')')
  _ <- exact("=>")
  body <- term
} yield Raw.Lam(param, ty, body)

// 赋值
def rec = for {
  _ <- exact("rec")
  value <- term
} yield value

def let = for {
  _ <- exact("let")
  name <- some(ident)
  _ <- exact('=')
  value <- term
  recVal <- optional(rec)
  _ <- exact("in")
  next <- term
} yield Raw.Let(name, value, recVal, next)

// 选择
def alt = for {
  _ <- exact("if")
  cond <- term
  _ <- exact("then")
  x <- term
  _ <- exact("else")
  y <- term
} yield Raw.Alt(cond, x, y)

// 整个表达式
def term: Parser[Raw] = tup | lam | let | rec | alt
