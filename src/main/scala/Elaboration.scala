// 推导一个 Raw 的类型
def infer(ctx: Ctx, term: Raw): TmPack = term match

  // Nope 的类型是任意的
  case Raw.Brk => TmPack(Term.Brk, Type.Any)

  // 输入的类型暂时只能是 i32
  case Raw.Inp => TmPack(Term.Inp, Type.I32)

  // 输出的参数类型暂时只能是 i32
  case Raw.Prt(arg) =>
    val TmPack(tm, ty) = infer(ctx, arg)
    if ty != Type.I32 then throw Error.TypeMismatch(arg, Type.I32, ty)
    TmPack(Term.Prt(tm, ty), ty)

  // 数字的类型一定是 i32
  case Raw.Num(value) => TmPack(Term.Num(value), Type.I32)

  // 布尔值的类型一定是 i1
  case Raw.Boo(value) => TmPack(Term.Boo(value), Type.Boo)

  // 变量的类型要查表
  case Raw.Var(name) =>
    try
      val ty = ctx(name)
      TmPack(Term.Var(name), ty)
    catch case _ => throw Error.NameNotFound(term)

  // 匿名函数的类型可以直接求出，因为没有依值类型
  case Raw.Lam(param, ty, body) =>
    val TmPack(btm, bty) = infer(ctx.bind(param, ty), body)
    TmPack(Term.Lam(param, ty, btm), Type.Fun(ty, bty))

  // 函数传参类型容易通过函数的类型求出，但要确保参数类型匹配
  case Raw.App(func, arg) =>
    val TmPack(ftm, fty) = infer(ctx, func)
    fty match
      case Type.Fun(from, to) =>
        val atm = check(ctx, arg, from)
        TmPack(Term.App(ftm, atm), to)
      case _ =>
        throw Error.TypeMismatch(func, Type.Fun(Type.Any, Type.Any), fty)

  // 中缀运算类型容易通过参数求出
  case Raw.Mid(oprt, lhs, rhs) =>
    val TmPack(ltm, lty) = infer(ctx, lhs)
    val TmPack(rtm, rty) = infer(ctx, rhs)

    // 检查中缀运算参数类型
    oprt.check(lhs, lty)
    oprt.check(rhs, rty)
    val resTy = oprt.retTy
    TmPack(Term.Mid(oprt, ltm, rtm, lty), resTy)

  // 前缀运算类型容易通过参数求出
  case Raw.Pre(oprt, value) =>
    val TmPack(vtm, vty) = infer(ctx, value)

    // 检查前缀运算参数类型
    oprt.check(value, vty)
    val resTy = oprt.retTy
    TmPack(Term.Pre(oprt, vtm, vty), resTy)

  // 赋值语句直接转换成语境
  case Raw.Let(name, value, recVal, next) =>
    val TmPack(tm, ty) = infer(ctx, value)
    val c = (name.length, ty) match

      // 在 let a, b = c, d 中，希望左右一样多
      case (len, Type.Tup(ls)) if ls.length == len =>
        name
          .zip(ls)
          .foldLeft(ctx)((c, pair) =>
            val (n, v) = pair
            c.bind(n, v)
          )

      // 在 let pair = 1, 2 中，直接把 (1, 2) 绑定到 pair 上
      case (1, _) =>
        ctx.bind(name(0), ty)

      // 其他情况全部寄掉
      case (len, _) =>
        throw Error.CountMismatch(value)

    // 检查递归体内语句是否正确
    val rtm = recVal match
      case Some(res) => Some(check(c, res, ty))
      case None      => None

    // 推导后续语句的类型
    val TmPack(ntm, nty) = infer(c, next)
    TmPack(Term.Let(name, tm, rtm, ntm, ty), nty)

  // 选择语句类型容易通过两边求出
  case Raw.Alt(cond, x, y) =>
    val TmPack(ctm, cty) = infer(ctx, cond)

    // 比较的依据必须是一个布尔值
    if cty != Type.Boo then throw Error.TypeMismatch(cond, Type.Boo, cty)
    val TmPack(xtm, xty) = infer(ctx, x)
    val TmPack(ytm, yty) = infer(ctx, y)

    // 两种分支的类型应该相同
    val uty = unify(xty, yty)
    TmPack(Term.Alt(ctm, xtm, ytm, uty), uty)

  // 元组直接暴力求
  case Raw.Tup(ls) =>
    val (tms, tys) =
      ls.foldLeft((List[Term](), List[Type]()))((pk, tm) =>
        val (ptm, pty) = pk
        val TmPack(ttm, tty) = infer(ctx, tm)
        (ptm :+ ttm, pty :+ tty)
      )
    TmPack(Term.Tup(tms), Type.Tup(tys))

// 检查某 Raw 是否是给定类型
def check(ctx: Ctx, term: Raw, tyExp: Type): Term =
  val TmPack(tm, tyInf) = infer(ctx, term)
  try unify(tyExp, tyInf)
  catch case UnifyError() => throw Error.TypeMismatch(term, tyExp, tyInf)
  tm

// 检查两个类型能不能被看成相同
def unify(lhs: Type, rhs: Type): Type =
  (lhs, rhs) match

    // Any 类型可以与任何东西相同
    case (Type.Any, _) => rhs
    case (_, Type.Any) => lhs

    // 否则两个类型必须要严格相同
    case _ => if lhs != rhs then throw UnifyError() else lhs
