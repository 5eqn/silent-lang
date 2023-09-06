import scala.io.Source

val M = 1000000

// 从这里开始运行
@main def run(from: String, to: String) =
  val tStart = System.nanoTime()
  val src = Source.fromFile(from)
  val str = src.mkString
  src.close()
  val tRead = System.nanoTime()
  println(s"读入文件耗时：${(tRead - tStart) / M} ms")
  source.run(Input(str, 0)) match
    case Result.Fail(at) =>
      println(s"${Range(at, at.next)}\n文件的语法有错误！")
    case Result.Success(res, rem) =>
      try
        val tParse = System.nanoTime()
        println(s"语法分析耗时：${(tParse - tRead) / M} ms")
        val TmPack(tm, ty) = infer(Ctx.empty, res)
        val tInfer = System.nanoTime()
        println(s"类型检查耗时：${(tInfer - tParse) / M} ms")
        val pk = pEval(Env.empty, tm)
        val tEval = System.nanoTime()
        println(s"代码生成耗时：${(tEval - tInfer) / M} ms")
        output(s"$pk", to)
        println("编译成功！")
      catch case e => println(e)
