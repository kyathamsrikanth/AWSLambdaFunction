import MyProtoBuf.findTimeStampInLogsGrpc.findTimeStampInLogs
import MyProtoBuf.{findTimeStampInLogsGrpc, mesageRequest, messageResponse}
import io.grpc.{Server, ServerBuilder}

import java.util.logging.Logger
import scala.concurrent.{ExecutionContext, Future}
import com.typesafe.config.{Config, ConfigFactory}
import gRPCServer.config
import scalaj.http.{Http, HttpOptions}
import scalapb.options.ScalapbProto.message
import spray.json._
object gRPCServer {
  val config: Config  = ConfigFactory.load()
  private val logger = Logger.getLogger(classOf[gRPCServer].getName)

  def main(args: Array[String]): Unit = {
    val server: gRPCServer = new gRPCServer(ExecutionContext.global)
    startServer(server)
    blockServerUntilShutdown(server)
  }
  def startServer(server: gRPCServer): Unit = {
    // start() will start the server
    server.start()
  }
  def blockServerUntilShutdown(server: gRPCServer): Unit =  {
    server.blockUntilShutdown()
  }
  /// Get Port from the Config file
  private val port:Int = ConfigFactory.load().getInt("CONFIG.port")
}

class gRPCServer(executionContext: ExecutionContext) { self =>
  private[this] var grpcServer: Server = _
  private[this] val logger = Logger.getLogger(classOf[gRPCServer].getName)
  private def start(): Unit = {
    // Build Server on Designated Port
    grpcServer = ServerBuilder.forPort(gRPCServer.port).addService(findTimeStampInLogs.bindService(new findTimeStampInLogsImpl, executionContext)).asInstanceOf[ServerBuilder[_]].build.start
    gRPCServer.logger.info("Successfully Started Server " + gRPCServer.port)
    sys.addShutdownHook {
      logger.warning("Shutdown due to error")
      self.stop()
      System.err.println("Shutdown due to error")
    }
  }
  private def stop(): Unit = {
    // stop() Function will Shutdown the Server
    if (grpcServer != null) {grpcServer.shutdown()}
  }
  private def blockUntilShutdown(): Unit = {
    if (grpcServer != null) {grpcServer.awaitTermination()}
  }

  private class findTimeStampInLogsImpl extends findTimeStampInLogsGrpc.findTimeStampInLogs{
    // Fetching Params from  Config Folder
    val AWSAPIGateWayURL: String = config.getString("CONFIG.AWSAPIGateWayURL")

    override def fetchTimeStampInLogs(request: mesageRequest): Future[messageResponse] = {
      // Send HTTP Request with requestParamsList
      logger.warning("request made : " + request.request)
      val requestParamsList = request.request.split("_")
      val timeStamp = requestParamsList(0)
      val deltaTime = requestParamsList(1)
      val date = requestParamsList(2)
      val designatedPattern = requestParamsList(3)
      // Make HTTP Request
      val result = Http(AWSAPIGateWayURL).postData(formatHttpContent(timeStamp,deltaTime,date,designatedPattern)).header("Content-Type", "application/json").header("Charset", "UTF-8").option(HttpOptions.readTimeout(1000000)).asString
      val responseJson = result.body.toString
     // Send Response back to Client
      val response = messageResponse(message = responseJson)
      Future.successful(response)
    }

    private def formatHttpContent(timeStamp :String,deltaTime: String,date : String,designatedPattern : String) = {
      "{\n  \"body\": \"{\\\"timeStamp\\\" : \\\"%s\\\",\\\"date\\\" : \\\"%s\\\",\\\"deltaTime\\\" : \\\"%s\\\",\\\"pattern\\\" : \\\"%s\\\"}\"\n}".format(timeStamp, date, deltaTime, designatedPattern)
    }
  }


}