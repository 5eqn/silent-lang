// 新变量名生成器
type Names = List[String]
type Types = List[Type]

var counter = 0

def fresh =
  counter += 1
  s"x$counter"

// 语境，存储变量名到类型的对应
case class Ctx(types: Map[String, Type]):
  def bind(name: String, ty: Type) = Ctx(types + (name -> ty))
  def apply(name: String) = types(name)

object Ctx:
  def empty = Ctx(Map("input" -> Type.I32))

// 环境，存储变量名到值的对应
case class Env(values: Map[String, IRVal]):
  def bind(name: String, value: IRVal) = Env(values + (name -> value))
  def apply(name: String) = values(name)

object Env:
  def empty = Env(Map("input" -> IRVal.Var("input")))
