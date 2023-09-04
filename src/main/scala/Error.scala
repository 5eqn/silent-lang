enum Error extends Exception:
  case TypeMismatch(tm: Raw, exp: Type, inf: Type)
  case CountMismatch(tm: Raw, exp: Int)
  case NotApplicable(tm: Raw)
