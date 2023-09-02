def pEval(env: Env, term: Term): IRPack = term match

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
  case Term.Add(lhs, rhs, ty) =>
    val IRPack(lv, lop) = pEval(env, lhs)
    val IRPack(rv, rop) = pEval(env, rhs)
    val ops = lop.add(rop)
    (lv, rv) match

      // 如果两个都是数，就可以直接化简
      case (IRVal.Num(a), IRVal.Num(b)) =>
        IRPack(IRVal.Num(a + b), ops)

      // 否则新建一个变量存储这个加法成果
      case _ =>
        val name = fresh
        val newOp = IROp.Add(name, ty, lv, rv)
        IRPack(IRVal.Var(name), ops.add(newOp))

  // 定义变量直接转移值
  case Term.Let(name, value, next) =>
    val IRPack(vv, vop) = pEval(env, value)
    val e = (name.length, vv) match

      // 在 let a, b = c, d 中，希望左右一样多
      case (len, IRVal.Tup(ls)) if len == ls.length =>
        name
          .zip(ls)
          .foldRight(env)((pair, e) =>
            val (n, v) = pair
            e.bind(n, v)
          )

      // 在 let pair = 1, 2 中，直接把 (1, 2) 绑定到 pair 上
      case (1, _) =>
        env.bind(name(0), vv)

      // 其他情况全部寄掉
      case _ => throw new Exception("let spine length mismatch")

    // 继续求值
    pEval(e, next).prepend(vop)

  // 选择分支，先求等式两边的值
  case Term.Alt(lhs, rhs, x, y, ty) =>
    val IRPack(lv, lop) = pEval(env, lhs)
    val IRPack(rv, rop) = pEval(env, rhs)
    val ops = lop.add(rop)
    (lv, rv) match

      // 两个值可以直接判断，就直接化简
      case (IRVal.Num(a), IRVal.Num(b)) =>
        if a == b
        then pEval(env, x).prepend(ops)
        else pEval(env, y).prepend(ops)

      // 否则两个分支都要计算
      case _ =>
        val IRPack(xv, xop) = pEval(env, x)
        val IRPack(yv, yop) = pEval(env, y)

        // 构造出 Alt 操作
        val name = fresh
        val xpk = IRPack(xv, xop)
        val ypk = IRPack(yv, yop)
        val newOp = IROp.Alt(name, ty, lv, rv, xpk, ypk)
        IRPack(IRVal.Var(name), ops.add(newOp))

  // 元组直接暴力求
  case Term.Tup(ls) =>
    val (v, op) =
      ls.foldRight((List[IRVal](), IROps.empty))((tm, pk) =>
        val (pv, pop) = pk
        val IRPack(tv, top) = pEval(env, tm)
        (tv :: pv, pop.add(top))
      )
    IRPack(IRVal.Tup(v), op)
