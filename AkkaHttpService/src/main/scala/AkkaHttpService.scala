import AkkaHttpService.system.dispatcher
import org.slf4j.{Logger, LoggerFactory}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse}
import akka.util.ByteString
import com.typesafe.config.Config
import akka.actor.ActorSystem
import scala.concurrent.Future
import scala.util.{Failure, Success}

object AkkaHttpService {
  implicit val system: ActorSystem = ActorSystem()
  private val logger = LoggerFactory.getLogger(getClass)
  val config: Config = GetConfigClass("CONFIG") match {
    case Some(value) => value
    case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
  }
   // Fetching Params from  Config Folder
  val AWSAPIGateWayURL: String = config.getString("CONFIG.AWSAPIGateWayURL")
  val timeStamp: String = config.getString("CONFIG.givenTimeStamp")
  val deltaTime: String = config.getString("CONFIG.deltaTimeInterval")
  val date: String = config.getString("CONFIG.date")
  val designatedPattern : String = config.getString("CONFIG.designatedPattern")

  def sendAPIGateWayRequest(): Unit = {
    // Building HTTP Request

    logger.info("Building HTTP Request")
    val request = HttpRequest(method = HttpMethods.POST, uri = s"$AWSAPIGateWayURL", entity = HttpEntity(ContentTypes.`application/json`, formatHttpContent))
    logger.info("POST HTTP Request")
    val eventualHttpResponse: Future[HttpResponse] = Http().singleRequest(request)
    // Parse output Json to get Output
    processHTTPResponse(eventualHttpResponse)
  }

  private def formatHttpContent = {
    "{\n  \"body\": \"{\\\"timeStamp\\\" : \\\"%s\\\",\\\"date\\\" : \\\"%s\\\",\\\"deltaTime\\\" : \\\"%s\\\",\\\"pattern\\\" : \\\"%s\\\"}\"\n}".format(timeStamp, date, deltaTime, designatedPattern)
  }

  private def processHTTPResponse(eventualHttpResponse: Future[HttpResponse]): Unit = {
    eventualHttpResponse.onComplete {
      case Success(res) =>
        val HttpResponse(statusCodes, headers, entity, _) = res
        logger.info(entity.toString)
        logger.info(statusCodes.toString)
        logger.info(headers.toString)
        entity.dataBytes.runFold(ByteString(""))(_ ++ _).foreach(body => {
          logger.info(body.utf8String)
        })
        system.terminate()
      case Failure(_) => sys.error("HTTP POST Request Failed")
    }
  }

  def main(args: Array[String]): Unit = {
    sendAPIGateWayRequest()
  }
}
