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
  "rec",
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

// 高结合性语法

def inp = exact("input").map(_ => Term.Inp)

def pos = number.map(Term.Num(_))

def neg = for {
  _ <- exact('-')
  value <- number
} yield Term.Num(-value)

def vrb = ident.map(Term.Var(_))

def brk = for {
  _ <- exact('(')
  tm <- term
  _ <- exact(')')
} yield tm

def prt = for {
  _ <- exact("print")
  _ <- exact('(')
  arg <- term
  _ <- exact(')')
} yield Term.Prt(arg)

def atm = inp | pos | neg | vrb | brk | prt

// 左递归语法

def some[A](p: Parser[A]) = for {
  lhs <- p
  res <- someRest(List(lhs), p)
} yield res

def someRest[A](lhs: List[A], p: Parser[A]): Parser[List[A]] = (for {
  _ <- exact(',')
  rhs <- p
  res <- someRest(rhs :: lhs, p)
} yield res) | success(lhs)

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

def tup = some(add).map(ls => if ls.length == 1 then ls(0) else Term.Tup(ls))

// 外层语法

def irInt = exact("int").map(_ => IRType.I32)
def irPtr = exact("ptr").map(_ => IRType.Ptr)
def irOne = irInt | irPtr
def irType =
  some(irOne).map(ls => if ls.length == 1 then ls(0) else IRType.Tup(ls))

def lam = for {
  _ <- exact('(')
  param <- ident
  _ <- exact(':')
  ty <- irType
  _ <- exact(')')
  _ <- exact("=>")
  body <- term
} yield Term.Lam(param, ty, body)

def let = for {
  _ <- exact("let")
  name <- some(ident)
  _ <- exact('=')
  value <- term
  _ <- exact("in")
  next <- term
} yield Term.Let(name, value, next)

def rec = for {
  _ <- exact("let")
  name <- some(ident)
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

def term: Parser[Term] = tup | lam | let | alt
