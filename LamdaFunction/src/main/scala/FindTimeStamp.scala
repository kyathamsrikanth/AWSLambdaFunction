
import com.amazonaws.services.lambda.runtime.{Context, LambdaLogger, RequestHandler}
import com.amazonaws.services.lambda.runtime.events.{APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent}
import com.typesafe.config.Config
import org.apache.http.HttpStatus
import org.json4s.jackson.JsonMethods.parse
import play.api.libs.json.Json
import software.amazon.awssdk.services.s3.model.S3Object

import scala.collection.JavaConverters._
import java.io.{BufferedReader, InputStream, InputStreamReader}
import scala.collection.immutable.Map
import scala.util.matching.Regex

class FindTimeStamp extends RequestHandler[APIGatewayProxyRequestEvent,APIGatewayProxyResponseEvent]{

  val config: Config = GetConfigClass("CONFIG") match {
    case Some(value) => value
    case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
  }
  val AWSS3Bucket: String = config.getString("CONFIG.AWSS3Buket")
  override def handleRequest(input: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent = {
    val logger: LambdaLogger = context.getLogger
    // Get Input Params form From Input JSon
    val parsedJson: Map[_root_.java.lang.String, Any] = parseJson(input)
    val timeStamp = parsedJson("timeStamp").toString
    logger.log("Requested timeStamp = " + timeStamp + "")
    val deltaTime = parsedJson("deltaTime").toString
    logger.log("Requested deltaTime = " + deltaTime + "")
    val pattern = parsedJson("pattern").toString
    logger.log("Requested pattern = " + pattern + "")
    val date = parsedJson("date").toString
    logger.log("Requested date = " + date + "")
    findTimeStamp(timeStamp, deltaTime, pattern,date, logger)
  }

  private def parseJson(input: APIGatewayProxyRequestEvent) = {
    val parsedJson: Map[String, Any] = parse(input.getBody).values.asInstanceOf[Map[String, Any]]
    parsedJson
  }

  def findTimeStamp(timeStamp: String, deltaTime: String, pattern: String, date:String ,logger: LambdaLogger): APIGatewayProxyResponseEvent = {

    //Fetch the file name under the input folder in the s3 bucket
    val logFile: Option[S3Object] = CommonUtils.fetchLogFile(AWSS3Bucket,date, logger)
    val s3Stream: InputStream = CommonUtils.getS3InputStream(AWSS3Bucket, logFile.get.key())
    val bufferReader = new BufferedReader(new InputStreamReader(s3Stream))
    val logFileSize = logFile.get.size()
    val logsFound = CommonUtils.findLogs(timeStamp, deltaTime, bufferReader, logFileSize, logger)

    if(logsFound)
    {
      // get Logs with Designated Pattern inside given time Interval
      val hashList = CommonUtils.getLogswithDesignatedPattern(timeStamp, deltaTime, bufferReader, logFileSize, logger)
      logger.log("Returning 200 as Logs are  Found")
      getHttpResponse(hashList, "Logs Found", HttpStatus.SC_OK)
    }
    else{
      // Return 404 Error as Logs are not Found
      logger.log("Returning 404 Error as Logs are not Found")
      getHttpResponse(List(), "No Logs Found", HttpStatus.SC_NOT_FOUND)
    }
  }
  def getHttpResponse(hashList:List[String],outputMessage:String,httpStatus:Int): APIGatewayProxyResponseEvent ={
    new APIGatewayProxyResponseEvent().withStatusCode(httpStatus).withIsBase64Encoded(true).withHeaders(Map("Content-Type" -> "application/json").asJava)
      .withBody(Json.obj("status" -> httpStatus, "message" -> outputMessage ,"MD5Hexoutput" ->hashList.toString() ).toString)
  }


}
