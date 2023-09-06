// 不能作为变量名的特殊字符列表
def specialChar = "&|^><*/%+-=!,:~()[] \t\n"

// 不能作为变量名的关键字列表
def keywords = List(
  "lam",
  "let",
  "rec",
  "app",
  "if",
  "then",
  "else",
  "int",
  "input",
  "print",
  "nope"
)

// 判断一个字符是否是数字
def isNumeric(ch: Char) = ch >= '0' && ch <= '9'

// 判断一个字符是否可以作为变量名
def isNotSpecial(ch: Char) = !specialChar.contains(ch)

// 按照给定的谓词分割给定的输入
def collect(pred: Char => Boolean, str: Input, acc: String): (String, Input) =
  str.headOption match
    case Some(hd) if pred(hd) => collect(pred, str.tail, acc + hd);
    case _                    => (acc, str)

// 获得字符串在命令行中显示的宽度
def displayWidth(input: String): Int =
  var width = 0
  for c <- input do
    if isWideCharacter(c) then width += 2
    else width += 1
  width

// 判断一个字符串是否是宽的
def isWideCharacter(c: Char): Boolean =
  Character.UnicodeScript.of(c) match
    case Character.UnicodeScript.HAN => true // CJK character
    case _ if Character.getType(c) == Character.OTHER_SYMBOL => true // Emoji
    case _                                                   => false

// 位置
case class Position(line: Int, col: Int, content: String)

object Position:
  def empty = Position(1, 1, "Offset is out of bounds")

// 带位置的输入
case class Input(source: String, offset: Int):

  // 获取下一个位置的输入
  def next: Input = Input(source, offset + 1)

  // 获取两个输入中比较后的一个
  def max(rhs: Input) =
    val roff = rhs.offset
    Input(source, if roff > offset then roff else offset)

  // 将 offset 转化为带行和列的位置
  def pos: Position =
    val lines = source.split('\n')
    var currentOffset = 0
    val lineAndOffset = lines.zipWithIndex.flatMap((line, lineNum) =>
      val lineEnd = currentOffset + line.length + 1
      if (offset >= currentOffset && offset < lineEnd)
        val colNum = offset - currentOffset + 1
        Some(Position(lineNum + 1, colNum, line))
      else
        currentOffset = lineEnd
        None
    )
    lineAndOffset.headOption.getOrElse(Position.empty)

  // 递归模块封装
  def headOption =
    if source.length() == offset then None else Some(source(offset))
  def tail = Input(source, offset + 1)

  // 处理模块封装
  def stripPrefix(p: String) =
    if (source.startsWith(p, offset)) then
      Some(Input(source, offset + p.length))
    else None
  def trim(pred: Char => Boolean) =
    var from = offset
    val to = source.length()
    while (from < to && pred(source(from)))
      from += 1
    Input(source, from)
  def until(p: String) =
    var from = offset
    val to = source.length()
    while (from < to && !source.startsWith(p, from))
      from += 1
    Input(source, from)

// 范围
case class Range(from: Input, to: Input):
  override def toString(): String =
    val fromPos = from.pos
    val toPos = to.pos
    val fromCol = fromPos.col - 1
    val toCol =
      if fromPos.line == toPos.line then toPos.col - 1
      else fromPos.content.length()
    val content = fromPos.content
    val padWidth = displayWidth(content.take(fromCol))
    val selWidth = displayWidth(content.slice(fromCol, toCol))
    val selection = " " * padWidth + "^" * selWidth
    s"第 ${fromPos.line} 行 ${fromPos.col} 列有错误：\n | ${fromPos.content}\n | $selection"

trait Ranged:
  var range = Range(Input("", 0), Input("", 0))
