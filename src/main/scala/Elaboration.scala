// 推导一个 Raw 的类型

def infer(ctx: Ctx, term: Raw): TmPack = term match

  // 输入的类型暂时只能是 i32
  case Raw.Inp =>
    TmPack(Term.Inp, Type.I32)

  // 输出的参数类型暂时只能是 i32
  case Raw.Prt(arg) =>
    val TmPack(tm, ty) = infer(ctx, arg)
    if ty != Type.I32 then throw new Exception("can't print non-number")
    TmPack(Term.Prt(tm, ty), ty)

  // 数字的类型一定是 i32
  case Raw.Num(value) =>
    TmPack(Term.Num(value), Type.I32)

  // 变量的类型要查表
  case Raw.Var(name) =>
    TmPack(Term.Var(name), ctx(name))

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
      case _ => throw new Exception("can't call non-function")

  // 加法类型容易通过参数求出
  case Raw.Add(lhs, rhs) =>
    val TmPack(ltm, lty) = infer(ctx, lhs)
    val TmPack(rtm, rty) = infer(ctx, rhs)

    // 加号两边都得是 i32 才能相加
    if lty != Type.I32 || rty != Type.I32 then
      throw new Exception("can't add non-numbers")
    TmPack(Term.Add(ltm, rtm, Type.I32), Type.I32)

  // 赋值语句直接转换成语境
  case Raw.Let(name, value, next) =>
    val TmPack(tm, ty) = infer(ctx, value)
    val c = (name.length, ty) match

      // 在 let a, b = c, d 中，希望左右一样多
      case (len, Type.Tup(ls)) if ls.length == len =>
        name
          .zip(ls)
          .foldRight(ctx)((pair, c) =>
            val (n, v) = pair
            c.bind(n, v)
          )

      // 在 let pair = 1, 2 中，直接把 (1, 2) 绑定到 pair 上
      case (1, _) =>
        ctx.bind(name(0), ty)

      // 其他情况全部寄掉
      case _ => throw new Exception("let spine length mismatch")

    // 推导后续语句的类型
    val TmPack(ntm, nty) = infer(c, next)
    TmPack(Term.Let(name, tm, ntm), nty)

  // 选择语句类型容易通过两边求出
  case Raw.Alt(lhs, rhs, x, y) =>
    val TmPack(ltm, lty) = infer(ctx, lhs)
    val TmPack(rtm, rty) = infer(ctx, rhs)

    // 等式两边全是整数才能比较
    if lty != Type.I32 || rty != Type.I32 then
      throw new Exception("can't compare non-numbers")
    val TmPack(xtm, xty) = infer(ctx, x)
    val TmPack(ytm, yty) = infer(ctx, y)

    // 两种分支的类型应该相同
    if xty != ytm then throw new Exception("if cases type mismatch")
    TmPack(Term.Alt(ltm, rtm, xtm, ytm, xty), xty)

  // 元组直接暴力求
  case Raw.Tup(ls) =>
    val (tms, tys) =
      ls.foldRight((List[Term](), List[Type]()))((tm, pk) =>
        val (ptm, pty) = pk
        val TmPack(ttm, tty) = infer(ctx, tm)
        (ttm :: ptm, tty :: pty)
      )
    TmPack(Term.Tup(tms), Type.Tup(tys))

// 检查某 Raw 是否是给定类型

def check(ctx: Ctx, term: Raw, ty: Type): Term =
  val TmPack(tm, expected) = infer(ctx, term)
  if ty != expected then throw new Exception("type mismatch")
  tm
