import MyProtoBuf.findTimeStampInLogsGrpc.{findTimeStampInLogs, findTimeStampInLogsBlockingStub}
import MyProtoBuf.{findTimeStampInLogsGrpc, mesageRequest, messageResponse}
import com.typesafe.config.{Config, ConfigFactory}
import io.grpc.{ManagedChannel, ManagedChannelBuilder, StatusRuntimeException}

import java.util.concurrent.TimeUnit
import java.util.logging.Logger

object gRPCClient {
  val config: Config  = ConfigFactory.load()
  /* fetching TIME AND DELTA_TIME from Configs */
  val Time: String = config.getString("CONFIG.givenTimeStamp")
  val deltaTime: String = config.getString("CONFIG.deltaTimeInterval")
  val date: String = config.getString("CONFIG.date")
  val designatedPattern: String = config.getString("CONFIG.designatedPattern")
  def apply(host: String, port: Int): gRPCClient = {
    val channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().asInstanceOf[ManagedChannelBuilder[_]].build
    val blockingStub = findTimeStampInLogsGrpc.blockingStub(channel)
    new gRPCClient(channel, blockingStub)
  }

  def main(args: Array[String]): Unit = {
    // Get Port Number From Config
    val client = gRPCClient("localhost", ConfigFactory.load().getInt("CONFIG.port"))
    try {
      // add Params to the  Arguments
      val user = args.headOption.getOrElse(Time+"_"+deltaTime+"_"+date + "_" + designatedPattern)
      println(" Sending Request "+client.sendRequest(user))
    } finally {
      // Shut Down
      client.shutdown()
    }
  }
}

class gRPCClient private(private val grpcCh: ManagedChannel,private val grpcStub: findTimeStampInLogsBlockingStub) {
  private[this] val logger = Logger.getLogger(classOf[gRPCClient].getName)

  def shutdown(): Unit = {
    // Set Await time for the Channel
    grpcCh.shutdown.awaitTermination(9, TimeUnit.SECONDS)
  }
  def sendRequest(name: String): String = {
    logger.info("value of name is "+name)
    val request = mesageRequest(name)
    try {
      //  fetch TimeStamp
      val response = grpcStub.fetchTimeStampInLogs(request)
      logger.info("Result: " + response.message)
      response.message
    }
    catch {
      case exception: StatusRuntimeException => logger.info("Exception Occurred " + exception)
        "Exception Occurred"
    }
  }
}
