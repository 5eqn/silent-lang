// 处理结果
enum Result[+A]:
  case Success(res: A, rem: Input)
  case Fail(at: Input)

// 分析模块
case class Parser[A](run: Input => Result[A]):
  def map[B](cont: A => B) = Parser(str =>
    run(str) match
      case Result.Success(res, rem) => Result.Success(cont(res), rem)
      case Result.Fail(at)          => Result.Fail(at)
  )
  def flatMap[B](cont: A => Parser[B]) = Parser(str =>
    run(str) match
      case Result.Success(res, rem) => cont(res).run(rem)
      case Result.Fail(at)          => Result.Fail(at)
  )
  def |(another: Parser[A]) = Parser(str =>
    run(str) match
      case Result.Success(res, rem) => Result.Success(res, rem)
      case Result.Fail(at) =>
        another.run(str) match
          case Result.Success(res, rem) => Result.Success(res, rem)
          case Result.Fail(another)     => Result.Fail(at.max(another))
  )

// 可以获得范围的 Parser
def ranged[A <: Ranged](p: Parser[A]) = Parser(str =>
  p.run(str) match
    case Result.Success(res, rem) =>
      res.range = Range(str, rem)
      Result.Success(res, rem)
    case Result.Fail(at) => Result.Fail(at)
)

// 杂项 Parser
def until(p: String) = Parser(str => Result.Success((), str.until(p)))
def line = Parser(str => Result.Success((), str.trim(_ != '\n')))
def space = Parser(str => Result.Success((), str.trim(_.isWhitespace)))
def success[A](res: A) = Parser(str => Result.Success(res, str))
def optional[A](p: Parser[A]) = p.map(Some(_)) | success(None)

// 直接读入给定字符
def kwd(exp: Char) = lexeme(exact(exp))
def exact(exp: Char) = Parser(str =>
  str.headOption match
    case Some(hd) if hd == exp => Result.Success(hd, str.tail)
    case _                     => Result.Fail(str)
)

// 直接读入给定字符串
def kwd(exp: String) = lexeme(exact(exp))
def exact(exp: String) = Parser(str =>
  str.stripPrefix(exp) match
    case Some(rem) => Result.Success(exp, rem)
    case _         => Result.Fail(str)
)

// 读入一个数字
def number = Parser(str =>
  val (res, rem) = collect(isNumeric, str, "")
  if res.length() > 0
  then Result.Success(res.toInt, rem)
  else Result.Fail(str)
)

// 读入一个变量名
def ident = Parser(str =>
  val (res, rem) = collect(isNotSpecial, str, "")
  if res.length() > 0 && !keywords.contains(res)
  then Result.Success(res, rem)
  else Result.Fail(str)
)

// 注释和空格
def lexeme[A](p: Parser[A]) = ws.flatMap(_ => p.flatMap(_ => ws))
def comment = (lineComment | blockComment | blockEmoji).flatMap(_ => ws)
def ws: Parser[Unit] = space.flatMap(_ => optional(comment).map(_ => ()))

def lineComment: Parser[Unit] = for {
  _ <- exact("😅") | exact("//")
  _ <- line
} yield ()

def blockEmoji: Parser[Unit] = for {
  _ <- exact("👉")
  _ <- until("👈")
  _ <- exact("👈")
} yield ()

def blockComment: Parser[Unit] = for {
  _ <- exact("/*")
  _ <- until("*/")
  _ <- exact("*/")
} yield ()

// 读入以逗号分隔的一系列东西
def some[A](p: Parser[A]) = for {
  lhs <- p
  res <- someRest(List(lhs), p)
} yield res

def someRest[A](lhs: List[A], p: Parser[A]): Parser[List[A]] = (for {
  _ <- kwd(',')
  rhs <- p
  res <- someRest(lhs :+ rhs, p)
} yield res) | success(lhs)

// 操作符列表
def oprts = List(
  kwd('&').map(_ => Oprt.And)
    | kwd('|').map(_ => Oprt.Or)
    | kwd('^').map(_ => Oprt.Xor),
  kwd(">>").map(_ => Oprt.Shr)
    | kwd("<<").map(_ => Oprt.Shl),
  kwd('*').map(_ => Oprt.Mul)
    | kwd('/').map(_ => Oprt.Div)
    | kwd('%').map(_ => Oprt.Mod),
  kwd('+').map(_ => Oprt.Add)
    | kwd('-').map(_ => Oprt.Sub),
  kwd(">=").map(_ => Oprt.Ge)
    | kwd("<=").map(_ => Oprt.Le)
    | kwd('>').map(_ => Oprt.Gt)
    | kwd('<').map(_ => Oprt.Lt)
    | kwd("==").map(_ => Oprt.Eq)
    | kwd("!=").map(_ => Oprt.Ne),
  kwd("&&").map(_ => Oprt.All)
    | kwd("||").map(_ => Oprt.Any)
)

// 原子操作符
def atm = ranged(inp | brk | pos | neg | boo | vrb | par | prt)

def inp = kwd("input").map(_ => Raw.Inp)

def brk = kwd("nope").map(_ => Raw.Brk)

def pos = number.map(Raw.Num(_))

def neg = for {
  _ <- kwd('-')
  value <- number
} yield Raw.Num(-value)

def boo = kwd("true").map(_ => Raw.Boo(true)) |
  kwd("false").map(_ => Raw.Boo(false))

def vrb = ident.map(Raw.Var(_))

def par = for {
  _ <- kwd('(')
  tm <- term
  _ <- kwd(')')
} yield tm

def prt = for {
  _ <- kwd("print")
  _ <- kwd('(')
  arg <- term
  _ <- kwd(')')
} yield Raw.Prt(arg)

// 函数应用
def app = ranged(
  for {
    lhs <- atm
    res <- appRest(lhs)
  } yield res
)

def appRest(lhs: Raw): Parser[Raw] = (for {
  _ <- kwd('(')
  rhs <- term
  _ <- kwd(')')
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
  op <- oprts(level - 1)
  rhs <- mid(level - 1)
  res <- midRest(level, Raw.Mid(op, lhs, rhs))
} yield res) | success(lhs)

// 元组
def tup = some(mid(oprts.length)).map(ls =>
  if ls.length == 1 then ls(0) else Raw.Tup(ls)
)

// 类型
def tyInt = kwd("int").map(_ => Type.I32)

def tyPar = for {
  _ <- kwd('(')
  res <- tyAny
  _ <- kwd(')')
} yield res

def tyAtom: Parser[Type] = tyInt | tyPar

def tyFun = for {
  lhs <- tyAtom
  res <- tyFunRest(lhs)
} yield res

def tyFunRest(lhs: Type): Parser[Type] = (for {
  _ <- kwd("->")
  rhs <- tyFun
} yield Type.Fun(lhs, rhs)) | success(lhs)

def tyAny =
  some(tyFun).map(ls => if ls.length == 1 then ls(0) else Type.Tup(ls))

// 赋值
def rec = for {
  _ <- kwd("rec")
  value <- term
} yield value

def let = for {
  _ <- kwd("let")
  name <- some(ident)
  _ <- kwd('=')
  value <- term
  recVal <- optional(rec)
  _ <- ws
  next <- term
} yield Raw.Let(name, value, recVal, next)

// 匿名函数
def lam = for {
  _ <- kwd('(')
  param <- ident
  _ <- kwd(':')
  ty <- tyAny
  _ <- kwd(')')
  _ <- kwd("=>")
  body <- term
} yield Raw.Lam(param, ty, body)

// 选择
def alt = for {
  _ <- kwd("if")
  cond <- term
  _ <- kwd("then")
  x <- term
  _ <- kwd("else")
  y <- term
} yield Raw.Alt(cond, x, y)

// 整个表达式
def term: Parser[Raw] = ranged(let | lam | alt | tup)
