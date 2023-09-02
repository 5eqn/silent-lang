import scala.io.Source

// 从这里开始运行

val fileName = "tuple"

@main def run() =
  val src = Source.fromFile(s"sample/$fileName.silent")
  val str = src.mkString
  src.close()
  term.run(str) match
    case Result.Fail => println("Parse failed")
    case Result.Success(res, rem) =>
      val pk = pEval(Ctx.empty, res, IROps.empty)
      output(s"$pk", fileName)
