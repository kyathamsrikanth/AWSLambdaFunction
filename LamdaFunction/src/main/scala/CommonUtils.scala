
import CommonUtils.logger
import com.amazonaws.services.lambda.runtime.LambdaLogger
import com.typesafe.config.Config
import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.LoggerFactory
import software.amazon.awssdk.http.apache.ApacheHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.model.{GetObjectRequest, ListObjectsV2Request, S3Object}
import software.amazon.awssdk.services.s3.S3Client

import java.io.{BufferedReader, InputStream}
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.time.Duration
import java.util.Date
import scala.util.matching.Regex
object CommonUtils {

  val config: Config = GetConfigClass("CONFIG") match {
    case Some(value) => value
    case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
  }
  private val logger = LoggerFactory.getLogger(getClass)
  val s3: S3Client = S3Client.builder().region(Region.of(config.getString("CONFIG.REGION"))).httpClient(ApacheHttpClient.builder().build()).build()

  def fetchLogFile(bucket: String, date :String ,logger: LambdaLogger): Option[S3Object] = {
    val logFilePath: String = getLogFilePAth(date)
    CommonUtils.logger.info(" Fecthing Log file : " + logFilePath)
    val objectsList = ListObjectsV2Request.builder.bucket(bucket).prefix(logFilePath).build
    Option.apply(s3.listObjectsV2(objectsList).contents().get(0))
  }

  private def getLogFilePAth(date: String) = {
    val logFileName = config.getString("CONFIG.LogFileNameFormat").split("_")(0) + "." + date + "." + config.getString("CONFIG.LogFileNameFormat").split("_")(1)
    val logFilePath = config.getString("CONFIG.LogFilePath")  + logFileName
    logFilePath
  }

  def getS3InputStream(bucket: String, key: String): InputStream = {
    logger.info("getting Input Stream")
    val fileObject: GetObjectRequest = GetObjectRequest.builder().bucket(bucket).key(key).build()
    s3.getObjectAsBytes(fileObject).asInputStream()
  }

  def findLogs(startTime: String, timeDelta: String, bufferReader: BufferedReader, fileSize: Long, logger: LambdaLogger): Boolean = {
    // get time interval from start time and time delta
    CommonUtils.logger.info("get time interval from start time and time delta")
    val givenTime = new SimpleDateFormat("HH:mm").parse(startTime)
    val intervalStartTime = Date.from(givenTime.toInstant.minus(Duration.ofMinutes((timeDelta.toInt))))
    val intervalEndTime = Date.from(givenTime.toInstant.plus(Duration.ofMinutes((timeDelta.toInt))))
    bufferReader.mark(fileSize.asInstanceOf[Int])
    recBinSearchBytes(0, fileSize, bufferReader, intervalStartTime, intervalEndTime, fileSize, logger)
  }

  def recBinSearchBytes(startIndex: Long, endIndex: Long, br: BufferedReader, startTime: Date, endtime: Date, fileSize: Long, logger: LambdaLogger): Boolean = {
    CommonUtils.logger.info("Binary Search")
    val mid = (startIndex + endIndex) / 2
    if ((startIndex > endIndex) || (mid < 0) || (mid > fileSize)) {
      return false
    }
    // Find Log in the Mid of Range
    val midLog = fetchMidLog(br, mid)
    if (midLog.isDefined) {
      val logTimeFormatRegex = getlogTimeFormatRegex.r
      val midlogTimeStamp = new SimpleDateFormat("HH:mm").parse(logTimeFormatRegex.findFirstIn(midLog.get).get)
      if (midlogTimeStamp.compareTo(startTime) >= 0 && midlogTimeStamp.compareTo(endtime) <= 0) {
        return true
      } else if (midlogTimeStamp.compareTo(startTime) < 0) {
        // Go Right if midLogTimeStamp is less than start Time
        return recBinSearchBytes(mid + 1, endIndex, br, startTime, endtime, fileSize, logger)
      } else if (midlogTimeStamp.compareTo(endtime) > 0) {
        // Go Left  if midLogTimeStamp is more  than start Time
        return recBinSearchBytes(startIndex, mid - 1, br, startTime, endtime, fileSize, logger)
      }
    }
    false
  }

  def getlogTimeFormatRegex: String = {
    // get logTimeFormatRegex from Config
    val intervalTimeFrame = config.getString(s"CONFIG.logTimeFormatRegex")
    intervalTimeFrame
  }

  def fetchMidLog(bufferedReader: BufferedReader, midBytes: Long): Option[String] = {
    // reset the Buffer reader
    bufferedReader.reset()
    //skip to mid
    bufferedReader.skip(midBytes)
    val line = bufferedReader.readLine()
    if (findLogTimeStamp(line).isEmpty) {
      // Check if next Line has TimeStamp
      return Option.apply(bufferedReader.readLine())
    }
    Option.apply(line)
  }

  def findLogTimeStamp(message: String): Option[String] = {
    // Find Time Regex Pattern in The Log
    val pattern = new Regex("[0-9]{2}:[0-9]{2}:[0-9]{2}\\.[0-9]{3}")
    pattern.findFirstIn(message)
  }

  def getLogswithDesignatedPattern(startTime: String, timeDelta: String, br: BufferedReader, fileSize: Long, logger: LambdaLogger): List[String] = {
    // get the interval from time delta and start time
    CommonUtils.logger.info("get time interval from start time and time delta")
    val givenTime = new SimpleDateFormat("HH:mm").parse(startTime)
    val intervalStartTime = Date.from(givenTime.toInstant.minus(Duration.ofMinutes((timeDelta.toInt))))
    val intervalEndTime = Date.from(givenTime.toInstant.plus(Duration.ofMinutes((timeDelta.toInt))))
    br.reset()
    CommonUtils.logger.info("do Binary Search And Get StartI ndex")
    val start = doBinarySearchAndGetStartIndex(0, fileSize, br, intervalStartTime, intervalEndTime, fileSize, logger)
    if (start < 0) {
      br.reset()
      val line = br.readLine()
      CommonUtils.logger.info("processLogFiles")
      processLogFiles(br, intervalStartTime, intervalEndTime, line) match {
        case Some(toReturn) => return toReturn
        case None =>
      }
    }
    val arrayList = List()
    val resultList = getDesignatedLogsInTimeFrame(arrayList, br, intervalStartTime, intervalEndTime)
    resultList.reverse

  }

    def doBinarySearchAndGetStartIndex (min: Long, max: Long, br: BufferedReader, start: Date, end: Date, fileSize: Long, logger: LambdaLogger): Long = {
      // Do Binary Search using mid and max  and find the start time in the log file
      CommonUtils.logger.info("Finding Mid ")
      val mid = (min + max) / 2
      if ((min > max) || (mid < 0) || (mid > fileSize)) {
        return -1
      }
      // Fetch the Mid line of the given Log
      val log = fetchMidLog(br, mid)
      if (!log.isEmpty) {
        val logTimeFormatRegex = getlogTimeFormatRegex.r
        val midlogTimeStamp = new SimpleDateFormat("HH:mm").parse(logTimeFormatRegex.findFirstIn(log.get).get)
        if (midlogTimeStamp.compareTo(start) == 0) {
          return mid
        } else if (midlogTimeStamp.compareTo(start) < 0) {
          // Case if  midlogTimeStamp is smaller than start
          return doBinarySearchAndGetStartIndex(mid + 1, max, br, start, end, fileSize, logger)
        } else if (midlogTimeStamp.compareTo(start) > 0) {
          // Case if  midlogTimeStamp is  greater  than start
          return doBinarySearchAndGetStartIndex(min, mid - 1, br, start, end, fileSize, logger)
        }
      }
      -1
    }

  private def processLogFiles(br: BufferedReader, intervalStartTime: Date, intervalEndTime: Date, line: String): Option[List[String]] = {
    if (getLogTimeStamp(line).isEmpty) {
      // Read Next Line if Current line doesnt have
      val log = Option.apply(br.readLine()).get
      val logTimeFormatRegex = getlogTimeFormatRegex.r
      val midlogTimeStamp = new SimpleDateFormat("HH:mm").parse(logTimeFormatRegex.findFirstIn(log).get)
      //CHeck if current Time Stamp is in given Range
      if ((midlogTimeStamp.compareTo(intervalStartTime) >= 0 && midlogTimeStamp.compareTo(intervalEndTime) <= 0)) {
        val arrayList = List()
        val resultList = getDesignatedLogsInTimeFrame(arrayList, br, intervalStartTime, intervalEndTime)
        //  return md5Hex Hash Message
        return Some(DigestUtils.md5Hex(log.getBytes(StandardCharsets.UTF_8)) :: resultList.reverse)
      }
    } else {
      val logTimeFormatRegex = getlogTimeFormatRegex.r
      val midlogTimeStamp = new SimpleDateFormat("HH:mm").parse(logTimeFormatRegex.findFirstIn(line).get)
      //CHeck if current Time Stamp is in given Range
      if ((midlogTimeStamp.compareTo(intervalStartTime) >= 0 && midlogTimeStamp.compareTo(intervalEndTime) <= 0)) {
        val arrayList = List()
        val resultList = getDesignatedLogsInTimeFrame(arrayList, br, intervalStartTime, intervalEndTime)
        // return md5Hex Hash Message
        return Some(DigestUtils.md5Hex(line.getBytes(StandardCharsets.UTF_8)) :: resultList.reverse)
      }
    }
    None
  }

  def getDesignatedLogsInTimeFrame(stringList: List[String], br: BufferedReader, startTime: Date, endTime: Date): List[String] = {
    val line = br.readLine()
    if (getLogTimeStamp(line).isEmpty) {
      //if the line read does not have a timestamp read the next line and return
      val log = Option.apply(br.readLine()).get
      extractLogs(stringList, br, startTime, endTime, log)
    } else {
      // Extract logs from the current line
      extractLogs(stringList, br, startTime, endTime, line)
    }
  }

  private def extractLogs(stringList: List[String], br: BufferedReader, startTime: Date, endTime: Date, line: String) = {
    val logTimeFormatRegex = getlogTimeFormatRegex.r
    val midlogTimeStamp = new SimpleDateFormat("HH:mm").parse(logTimeFormatRegex.findFirstIn(line).get)
    // Check if  midlogTimeStamp is in Given interval
    if (!(midlogTimeStamp.compareTo(startTime) >= 0 && midlogTimeStamp.compareTo(endTime) <= 0)) {
      stringList
    }
    else {
      // Recursively iterate next line
      getDesignatedLogsInTimeFrame(DigestUtils.md5Hex(line.getBytes(StandardCharsets.UTF_8)) :: stringList, br, startTime, endTime)
    }
  }

  def getLogTimeStamp(message: String): Option[String] = {
    // Regex pattern to match the time stamp in a log string
    val pattern = new Regex("[0-9]{2}:[0-9]{2}:[0-9]{2}\\.[0-9]{3}")
    pattern.findFirstIn(message)
  }
}
