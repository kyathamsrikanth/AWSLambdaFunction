import org.scalatest.funsuite.AnyFunSuite

import java.io.{BufferedReader, File, FileReader, InputStreamReader}
import java.text.SimpleDateFormat
import java.time.Duration
import java.util.Date
import scala.io.Source
class CommonUtilsTest extends AnyFunSuite {

  test("testGetDesignatedLogsInTimeFrame") {
    val path  = Thread.currentThread.getContextClassLoader.getResource("input.txt").getPath
    val bufferReader = Source.fromURL(getClass.getResource("/input.txt")).bufferedReader()
    val inputFile = new File(path)
    val fileSize = inputFile.length
    bufferReader.mark(fileSize.asInstanceOf[Int])
    val actualOutput =   CommonUtils.getDesignatedLogsInTimeFrame(List(),bufferReader,new SimpleDateFormat("HH:mm").parse("02:01") , new SimpleDateFormat("HH:mm").parse("02:02"))
    assert(15 == actualOutput.length)
  }

  test("testDoBinarySearchAndGetStartIndex") {
    val path = Thread.currentThread.getContextClassLoader.getResource("input.txt").getPath
    val bufferReader = Source.fromURL(getClass.getResource("/input.txt")).bufferedReader()
    val inputFile = new File(path)
    val fileSize = inputFile.length
    bufferReader.mark(fileSize.asInstanceOf[Int])
    val actualOutput = CommonUtils.doBinarySearchAndGetStartIndex(0,fileSize,bufferReader,new SimpleDateFormat("HH:mm").parse("02:01") , new SimpleDateFormat("HH:mm").parse("02:02"),fileSize,null)
    assert(1110 == actualOutput)

  }

  test("testGetLogswithDesignatedPattern") {
    val path = Thread.currentThread.getContextClassLoader.getResource("input.txt").getPath
    val bufferReader = Source.fromURL(getClass.getResource("/input.txt")).bufferedReader()
    val inputFile = new File(path)
    val fileSize = inputFile.length
    bufferReader.mark(fileSize.asInstanceOf[Int])
    val actualOutput = CommonUtils.getLogswithDesignatedPattern( "02:03:30","2",bufferReader,fileSize,null)
    assert(6 == actualOutput.length)

  }


  test("testGetLogTimeStamp") {
    val line = "19:36:31.037 [scala-execution-context-global-14] INFO  HelperUtils.Parameters$ - 2Xzz1{p?[E5waf2bf1cg3be29UWFDZMIY"
    val Output = CommonUtils.getLogTimeStamp(line )
    assert("19:36:31.037" == Output.get)

  }


  test("testFetchMidLog") {

    val expectedOutput = "03:06:13.643 [scala-execution-context-global-14] INFO  HelperUtils.Parameters$ - M5Mc1\"9BU,EAiYO?A;eG_&<}o0DDX:^vjq~}rw~</4g{owLggfS^hjYGZQ#i"
    val path = Thread.currentThread.getContextClassLoader.getResource("input.txt").getPath
    val bufferReader = Source.fromURL(getClass.getResource("/input.txt")).bufferedReader()
    val inputFile = new File(path)
    val fileSize = inputFile.length
    bufferReader.mark(fileSize.asInstanceOf[Int])
    val actualOutput = CommonUtils.fetchMidLog(bufferReader,2222)
    assert(expectedOutput == actualOutput.get)

  }


}
