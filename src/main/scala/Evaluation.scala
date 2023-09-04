def pEval(env: Env, term: Term): IRPack = term match

  // Nope 操作会变成一条跳转指令，值是 Brk，这会阻止 store 操作
  case Term.Brk =>
    IRPack(IRVal.Brk, IROps.from(IROp.Brk))

  // 对输入操作单独使用一条指令
  case Term.Inp =>
    val name = fresh
    IRPack(IRVal.Var(name), IROps.from(IROp.Inp(name)))

  // 对输出操作单独使用一条指令
  case Term.Prt(arg, ty) =>
    val IRPack(av, aop) = pEval(env, arg)
    IRPack(av, aop.add(IROp.Prt(av, ty)))

  // 数字直接返回
  case Term.Num(value) =>
    IRPack(IRVal.Num(value), IROps.empty)

  // 布尔值直接返回
  case Term.Boo(value) =>
    IRPack(IRVal.Boo(value), IROps.empty)

  // 变量要查表得到值
  case Term.Var(name) =>
    IRPack(env(name), IROps.empty)

  // 函数收到值之后再 pEval
  case Term.Lam(param, ty, body) =>
    val f = (arg: IRVal) => pEval(env.bind(param, arg), body)
    IRPack(IRVal.Lam(f), IROps.empty)

  // 先求出函数和参数
  case Term.App(func, arg) =>
    val IRPack(fv, fop) = pEval(env, func)
    val IRPack(av, aop) = pEval(env, arg)
    fv match

      // 函数有实现，传参之后，把求函数和参数产生的操作加在前面
      case IRVal.Lam(fn) =>
        fn(av).prepend(fop.add(aop))

      // 函数没有实现（可能是外部函数），暂时处理不了
      case _ => throw new Exception("app lhs is not a function")

  // 加法，先求加法两边的值
  case Term.Mid(oprt, lhs, rhs, ty) =>
    val IRPack(lv, lop) = pEval(env, lhs)
    val IRPack(rv, rop) = pEval(env, rhs)
    val ops = lop.add(rop)

    // 尝试直接化简
    oprt.tryEval(lv, rv) match
      case Some(value) => IRPack(value, ops)

      // 否则新建一个变量存储这个加法成果
      case None =>
        val name = fresh
        val newOp = IROp.Mid(oprt, name, ty, lv, rv)
        IRPack(IRVal.Var(name), ops.add(newOp))

  // 定义或递归
  case Term.Let(name, value, recVal, next, ty) =>
    val IRPack(vv, vop) = pEval(env, value)
    val (e, op) = recVal match

      // 若不是递归块，直接转移值
      case None =>
        (name.length, vv) match

          // 在 let a, b = c, d 中，希望左右一样多
          case (len, IRVal.Tup(ls)) if len == ls.length =>
            (
              name
                .zip(ls)
                .foldLeft(env)((e, pair) =>
                  val (n, v) = pair
                  e.bind(n, v)
                ),
              IROps.empty
            )

          // 在 let pair = 1, 2 中，直接把 (1, 2) 绑定到 pair 上
          case (1, _) =>
            (env.bind(name(0), vv), IROps.empty)

          // 其他情况全部寄掉
          case _ => throw new Exception("let spine length mismatch")

      // 若是递归块，先计算递归操作
      case Some(rec) =>
        val ns = name.map(_ => fresh)
        val e = name
          .zip(ns)
          .foldLeft(env)((e, pair) =>
            val (from, to) = pair
            e.bind(from, IRVal.Var(to))
          )
        val rpk = pEval(e, rec)

        // 构建类型列表
        val tys = ty match
          case Type.Tup(ls) => ls
          case _            => List(ty)

        // 构造出递归操作
        val newOp = IROp.Rec(ns, tys, vv, rpk)

        // 构造出结果
        (e, IROps.from(newOp))

    // 继续求值
    pEval(e, next).prepend(vop.add(op))

  // 选择分支，先求等式两边的值
  case Term.Alt(cond, x, y, ty) =>
    val IRPack(cv, cop) = pEval(env, cond)
    cv match

      // 两个值可以直接判断，就直接化简
      case IRVal.Boo(flag) =>
        if flag
        then pEval(env, x).prepend(cop)
        else pEval(env, y).prepend(cop)

      // 否则两个分支都要计算
      case _ =>
        val IRPack(xv, xop) = pEval(env, x)
        val IRPack(yv, yop) = pEval(env, y)
        val xpk = IRPack(xv, xop)
        val ypk = IRPack(yv, yop)

        // 构造出类型列表
        val tys = ty match
          case Type.Tup(ls) => ls
          case _            => List(ty)

        // 构造出 Alt 操作
        val name = tys.map(_ => fresh)
        val newOp = IROp.Alt(name, tys, cv, xpk, ypk)

        // 构造出结果
        val res =
          if name.length == 1 then IRVal.Var(name(0))
          else IRVal.Tup(name.map(n => IRVal.Var(n)))
        IRPack(res, cop.add(newOp))

  // 元组直接暴力求
  case Term.Tup(ls) =>
    val (v, op) =
      ls.foldLeft((List[IRVal](), IROps.empty))((pk, tm) =>
        val (pv, pop) = pk
        val IRPack(tv, top) = pEval(env, tm)
        (pv :+ tv, pop.add(top))
      )
    IRPack(IRVal.Tup(v), op)
