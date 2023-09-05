import scala.io.Source

// 从这里开始运行
@main def run(from: String, to: String) =
  val src = Source.fromFile(from)
  val str = src.mkString
  src.close()
  source.run(Input(str, 0)) match
    case Result.Fail(at) =>
      println(s"${Range(at, at.next)}\n文件的语法有错误！")
    case Result.Success(res, rem) =>
      try
        val TmPack(tm, ty) = infer(Ctx.empty, res)
        val pk = pEval(Env.empty, tm)
        output(s"$pk", to)
        println("编译成功！")
      catch case e => println(e)
