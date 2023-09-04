// 带语义的输入
case class Input(source: String, offset: Int):
  def headOption =
    if source.length() == offset then None else Some(source(offset))
  def tail = Input(source, offset + 1)
  def stripPrefix(p: String) =
    if (source.startsWith(p, offset)) then
      Some(Input(source, offset + p.length))
    else None
  def trim() =
    var from = offset
    val to = source.length()
    while (from < to && source(from).isWhitespace)
      from += 1
    Input(source, from)

// 范围
case class Range(from: Input, to: Input)

trait Ranged:
  var range = Range(Input("", 0), Input("", 0))

def ranged[A <: Ranged](p: Parser[A]) = Parser(str =>
  p.run(str) match
    case Result.Success(res, rem) =>
      res.range = Range(str, rem)
      Result.Success(res, rem)
    case Result.Fail => Result.Fail
)

// 一些辅助函数
def isNumeric(ch: Char) = ch >= '0' && ch <= '9'
def isAlphabetic(ch: Char) = ch >= 'A' && ch <= 'Z' || ch >= 'a' && ch <= 'z'

def collect(pred: Char => Boolean, str: Input, acc: String): (String, Input) =
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
  case Success(res: A, rem: Input)
  case Fail

// 分析模块
case class Parser[A](run: Input => Result[A]):
  def map[B](cont: A => B) = Parser(str =>
    run(str) match
      case Result.Success(res, rem) => Result.Success(cont(res), rem)
      case Result.Fail              => Result.Fail
  )
  def flatMap[B](cont: A => Parser[B]) = Parser(str =>
    run(str) match
      case Result.Success(res, rem) => cont(res).run(rem)
      case Result.Fail              => Result.Fail
  )
  def |(another: Parser[A]) = Parser(str =>
    run(str) match
      case Result.Success(res, rem) => Result.Success(res, rem)
      case Result.Fail              => another.run(str)
  )

// 一些分析函数
def success[A](res: A) = Parser(str => Result.Success(res, str))

def ws = Parser(str => Result.Success((), str.trim()))

def exact(exp: Char) = Parser(str =>
  str.headOption match
    case Some(hd) if hd == exp => Result.Success(hd, str.tail)
    case _                     => Result.Fail
)

def exact(exp: String) = Parser(str =>
  str.stripPrefix(exp) match
    case Some(rem) => Result.Success(exp, rem)
    case _         => Result.Fail
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
  _ <- ws
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

def atm = ranged(inp | brk | pos | neg | boo | vrb | par | prt)

// 函数应用
def app = ranged(
  for {
    lhs <- atm
    res <- appRest(lhs)
  } yield res
)

def appRest(lhs: Raw): Parser[Raw] = (for {
  _ <- exact('(')
  rhs <- term
  _ <- exact(')')
  res <- appRest(Raw.App(lhs, rhs))
} yield res) | success(lhs)

// 中缀运算符
def mid(level: Int): Parser[Raw] = ranged(
  level match
    case 0 => app
    case _ =>
      for {
        lhs <- mid(level - 1)
        res <- midRest(level, lhs)
      } yield res
)

def midRest(level: Int, lhs: Raw): Parser[Raw] = (for {
  _ <- ws
  op <- oprts(level - 1)
  _ <- ws
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
  _ <- ws
  _ <- exact("->")
  _ <- ws
  rhs <- tyFun
} yield Type.Fun(lhs, rhs)) | success(lhs)

def tyAny =
  some(tyFun).map(ls => if ls.length == 1 then ls(0) else Type.Tup(ls))

// 匿名函数
def lam = for {
  _ <- exact('(')
  param <- ident
  _ <- ws
  _ <- exact(':')
  _ <- ws
  ty <- tyAny
  _ <- exact(')')
  _ <- ws
  _ <- exact("=>")
  _ <- ws
  body <- term
} yield Raw.Lam(param, ty, body)

// 赋值
def rec = for {
  _ <- exact("rec")
  _ <- ws
  value <- term
} yield value

def let = for {
  _ <- exact("let")
  _ <- ws
  name <- some(ident)
  _ <- ws
  _ <- exact('=')
  _ <- ws
  value <- term
  _ <- ws
  recVal <- optional(rec)
  _ <- ws
  _ <- exact("in")
  _ <- ws
  next <- term
} yield Raw.Let(name, value, recVal, next)

// 选择
def alt = for {
  _ <- exact("if")
  _ <- ws
  cond <- term
  _ <- ws
  _ <- exact("then")
  _ <- ws
  x <- term
  _ <- ws
  _ <- exact("else")
  _ <- ws
  y <- term
} yield Raw.Alt(cond, x, y)

// 整个表达式
def term: Parser[Raw] = ranged(tup | lam | let | rec | alt)
