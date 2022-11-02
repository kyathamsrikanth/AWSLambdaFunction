import AkkaHttpService.system.dispatcher
import org.slf4j.{Logger, LoggerFactory}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse}
import akka.util.ByteString
import com.typesafe.config.Config
import akka.actor.ActorSystem

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.{DurationInt, FiniteDuration}
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
  val timeout: FiniteDuration = config.getInt("CONFIG.futureAwait").seconds

  /**
   * sendAPIGateWayRequest : Will make HTTP POST Request using AKKA HTTP services  to invoke Lambda function .
   */
  def sendAPIGateWayRequest(): Future[String] = {
    // Building HTTP Request

    logger.info("Building HTTP Request")
    val request = HttpRequest(method = HttpMethods.POST, uri = s"$AWSAPIGateWayURL", entity = HttpEntity(ContentTypes.`application/json`, formatHttpContent))
    logger.info("POST HTTP Request")
    val eventualHttpResponse: Future[HttpResponse] = Http().singleRequest(request)
    // Parse output Json to get Output
    logger.info("Computing Future HttpResponse")
    val entityFuture: Future[HttpEntity.Strict] = eventualHttpResponse.flatMap(response => response.entity.toStrict(timeout))
    entityFuture.map(entity => entity.data.utf8String)
  }

  /**
   * formatHttpContent : Build HTTP Body Message .
   */
  private def formatHttpContent = {
    "{\n  \"body\": \"{\\\"timeStamp\\\" : \\\"%s\\\",\\\"date\\\" : \\\"%s\\\",\\\"deltaTime\\\" : \\\"%s\\\",\\\"pattern\\\" : \\\"%s\\\"}\"\n}".format(timeStamp, date, deltaTime, designatedPattern)
  }

  def main(args: Array[String]): Unit = {
    // Run sendAPIGateWayRequest 
    System.out.println(Await.result(sendAPIGateWayRequest(), timeout))
    System.exit(0)
  }
}
