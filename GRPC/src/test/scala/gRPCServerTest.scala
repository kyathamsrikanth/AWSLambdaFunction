import org.scalatest.funsuite.AnyFunSuite

class gRPCServerTest extends AnyFunSuite {

  test("testPort") {
    val expectedOutput = "60607"
    val actualOutput = gRPCServer.config.getString("CONFIG.port")
    assert(expectedOutput == actualOutput)

  }

  test("testdesignatedPattern") {
    val expectedOutput = "([a-c][e-g][0-3]|[A-Z][5-9][f-w]){5,15}"
    val actualOutput = gRPCServer.config.getString("CONFIG.designatedPattern")
    assert(expectedOutput == actualOutput)
  }

}
