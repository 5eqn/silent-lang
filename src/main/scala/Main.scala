import scala.io.Source

// 从这里开始运行
@main def run(from: String, to: String) =
  val src = Source.fromFile(from)
  val str = src.mkString
  src.close()
  term.run(Input(str, 0)) match
    case Result.Fail => println("Parse failed")
    case Result.Success(res, rem) =>
      val TmPack(tm, ty) = infer(Ctx.empty, res)
      val pk = pEval(Env.empty, tm)
      output(s"$pk", to)
      println("Compilation succeeds!")
