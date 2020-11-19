import lib.aws.S3Downloader
import lib.aws.S3Uploader
import lib.record.RecordsToCsvConverter
import lib.record.XmlToRecordsParser
import org.slf4j.LoggerFactory

fun main(args: Array<String>) {

    val logger = LoggerFactory.getLogger("App")
    logger.info("Start App!!")

    val keyName = System.getenv("KEY_NAME")

    if (keyName == null) {
        logger.error("KYE_NAME is Null. Exit process.")
        return
    }

    val file = S3Downloader().download(keyName)

    val result = XmlToRecordsParser().parse(file)
    val converter = RecordsToCsvConverter()

    val stepCountFile = converter.convertStepCount(result.stepCounts)
    val bodyTemperatureFile = converter.convertBodyTemperature(result.bodyTemperatures)

    val timeStamp = System.currentTimeMillis()

    val uploader = S3Uploader()
    uploader.upload(
        "convert/stepCount",
        timeStamp.toString(),
        stepCountFile
    )
    uploader.upload(
        "convert/bodyTemperature",
        timeStamp.toString(),
        bodyTemperatureFile
    )

    logger.info("Success all process!")
}
