enum Error extends Exception:
  case TypeMismatch(tm: Raw, exp: Type, inf: Type)
  case CountMismatch(tm: Raw)

  override def toString(): String = this match
    case TypeMismatch(tm, exp, inf) =>
      s"${tm.range}\n这玩意的类型应该是 $exp，但实际上是 $inf"
    case CountMismatch(tm) =>
      s"${tm.range}\n赋值数量不匹配"

case class UnifyError() extends Exception
