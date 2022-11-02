import AkkaHttpService.{config, sendAPIGateWayRequest, timeout}
import org.scalatest.funsuite.AnyFunSuite

import scala.concurrent.Await

class AkkaHttpServiceTest extends AnyFunSuite {

  test("testAWSAPIGateWayURL") {

    val expectedOutput = "https://dupc3jzjd4.execute-api.us-east-1.amazonaws.com/Test/mylambda"
    val actualOutput = AkkaHttpService.config.getString("CONFIG.AWSAPIGateWayURL")
    assert(expectedOutput == actualOutput)

  }

  test("testDeltaTime") {
    val expectedOutput = "2"
    val actualOutput = AkkaHttpService.config.getString("CONFIG.deltaTimeInterval")
    assert(expectedOutput == actualOutput)
  }


  test("testAWSAPIGateWayURL") {
    // Calling API Gateway Reuqest
    val futureResponse = Await.result(sendAPIGateWayRequest(), timeout)
    // Assert that response is success
    assert(futureResponse.contains("\"statusCode\":200"))
  }


}
