def pEval(ctx: Ctx, term: Term, ops: IROps): IRPack = term match

  // 对输入操作单独使用一条指令
  case Term.Inp =>
    val name = fresh
    IRPack(IRVal.Var(name), IRType.I32, ops.add(IROp.Inp(name)))

  // 对输出操作单独使用一条指令
  case Term.Prt(arg) =>
    val IRPack(av, at, aop) = pEval(ctx, arg, ops)
    IRPack(av, at, aop.add(IROp.Prt(av, at)))

  // 数字直接返回
  case Term.Num(value) => IRPack(IRVal.Num(value), IRType.I32, ops)

  // 变量要查表得到值
  case Term.Var(name) => IRPack(ctx.valueOf(name), ctx.typeOf(name), ops)

  // 函数收到值之后再 pEval，且继承 IROps，因为 inline
  case Term.Lam(param, ty, body) =>
    val f = (arg: IRVal, ops: IROps) =>
      pEval(ctx.bind(param, ty, arg), body, ops)
    IRPack(IRVal.Lam(f), IRType.Ptr, ops)

  // 先求出函数和参数，然后直接传参
  case Term.App(func, arg) =>
    val IRPack(fv, ft, fop) = pEval(ctx, func, ops)
    val IRPack(av, at, aop) = pEval(ctx, arg, fop)
    fv match
      case IRVal.Lam(fn) => fn(av, aop)
      case _             => throw new Exception("app lhs is not a function")

  // 加法，先求加法两边的值
  case Term.Add(lhs, rhs) =>
    val IRPack(lv, lt, lop) = pEval(ctx, lhs, ops)
    val IRPack(rv, rt, rop) = pEval(ctx, rhs, lop)

    // 希望这两个都是整数
    if lt != IRType.I32 || rt != IRType.I32 then
      throw new Exception("add non-numbers")
    (lv, rv) match

      // 如果两个都是数，就可以直接化简
      case (IRVal.Num(a), IRVal.Num(b)) =>
        IRPack(IRVal.Num(a + b), IRType.I32, rop)

      // 否则新建一个变量存储这个加法成果
      case _ =>
        val name = fresh
        val newOp = IROp.Add(name, lv, rv)
        IRPack(IRVal.Var(name), IRType.I32, rop.add(newOp))

  // 定义变量直接转移值
  case Term.Let(name, value, next) =>
    val IRPack(vv, vt, vop) = pEval(ctx, value, ops)
    (name.length, vv, vt) match

      // 在 let a, b = c, d 中，希望左右一样多
      case (len, IRVal.Tup(vls), IRType.Tup(tls)) if len == vls.length =>
        val ls = vls.zip(tls)

        // 从前往后绑定变量的值
        val c = name
          .zip(ls)
          .foldRight(ctx)((pair, c) =>
            val (n, (vv, vt)) = pair
            c.bind(n, vt, vv)
          )

        // 继续求值
        pEval(c, next, vop)

      // 在 let pair = 1, 2 中，直接把 (1, 2) 绑定到 pair 上
      case (1, _, _) =>
        val c = ctx.bind(name(0), vt, vv)

        // 继续求值
        pEval(c, next, vop)
      case _ => throw new Exception("let spine length mismatch")

  // 选择分支，先求等式两边的值
  case Term.Alt(lhs, rhs, x, y) =>
    val IRPack(lv, lt, lop) = pEval(ctx, lhs, ops)
    val IRPack(rv, rt, rop) = pEval(ctx, rhs, lop)

    // 希望这两个值是整数
    if lt != IRType.I32 || rt != IRType.I32 then
      throw new Exception("compare non-numbers")
    (lv, rv) match

      // 两个值可以直接判断，就直接化简
      case (IRVal.Num(a), IRVal.Num(b)) =>
        if a == b
        then pEval(ctx, x, rop)
        else pEval(ctx, y, rop)

      // 否则，考虑到两边在分支里面，要对 IROps 另起炉灶
      case _ =>
        val IRPack(xv, xt, xop) = pEval(ctx, x, IROps.empty)
        val IRPack(yv, yt, yop) = pEval(ctx, y, IROps.empty)

        // 希望这两个值类型相同
        if xt != yt then throw new Exception("if cases type mismatch")

        // 构造出 Alt 操作
        val name = fresh
        val xpk = IRPack(xv, xt, xop)
        val ypk = IRPack(yv, yt, yop)
        val newOp = IROp.Alt(name, lv, rv, xpk, ypk)
        IRPack(IRVal.Var(name), xt, rop.add(newOp))

  // 元组
  case Term.Tup(ls) =>
    val (v, t, op) =
      ls.foldRight((List[IRVal](), List[IRType](), ops))((tm, pk) =>
        val (pv, pt, pop) = pk
        val IRPack(tv, tt, top) = pEval(ctx, tm, pop)
        (tv :: pv, tt :: pt, top)
      )
    IRPack(IRVal.Tup(v), IRType.Tup(t), op)
