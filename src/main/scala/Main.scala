import scala.io.Source

// 从这里开始运行

val fileName = "fn"

@main def run() =
  val src = Source.fromFile(s"sample/$fileName.silent")
  val str = src.mkString
  src.close()
  term.run(str) match
    case Result.Fail => println("Parse failed")
    case Result.Success(res, rem) =>
      val TmPack(tm, ty) = infer(Ctx.empty, res)
      val pk = pEval(Env.empty, tm)
      output(s"$pk", fileName)
      println("Compilation succeeds!")
