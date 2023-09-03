import scala.io.Source

val fileName = "fib"

// 从这里开始运行
@main def run() =
  val src = Source.fromFile(s"sample/$fileName.silent")
  val str = src.mkString
  src.close()
  term.run(str) match
    case Result.Fail => println("Parse failed")
    case Result.Success(res, rem) =>
      val TmPack(tm, ty) = infer(Ctx.empty, res)
      println(tm)
      val pk = pEval(Env.empty, tm)
      output(s"$pk", fileName)
      println("Compilation succeeds!")
