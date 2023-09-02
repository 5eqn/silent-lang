// 新变量名生成器

var counter = 0

def fresh =
  counter += 1
  s"x$counter"

// 语境

case class Ctx(types: Map[String, IRType], values: Map[String, IRVal]):
  def bind(name: String, ty: IRType, value: IRVal) =
    Ctx(types + (name -> ty), values + (name -> value))
  def typeOf(name: String) = types(name)
  def valueOf(name: String) = values(name)

object Ctx:
  def empty =
    Ctx(Map("input" -> IRType.I32), Map("input" -> IRVal.Var("input")))
