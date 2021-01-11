import com.healthlogger.libs.CsvToRecordsConverter
import com.healthlogger.libs.StepCountRepository
import com.healthlogger.libs.file.s3.S3Downloader
import org.slf4j.LoggerFactory

fun main(args: Array<String>) {

    val logger = LoggerFactory.getLogger("App")
    logger.info("Start upload process.")

    val keyName = System.getenv("KEY_NAME")

    if (keyName == null) {
        logger.error("KYE_NAME is Null. Exit process.")
        return
    }

    val downloader = S3Downloader()
    val converter = CsvToRecordsConverter()
    val repository = StepCountRepository()

    val file = downloader.download(keyName)

    val stepCounts = converter.convertStepCount(file)

    val latestRecord = repository.findLatestItem()

    val target = if (latestRecord == null) stepCounts else stepCounts.filter { it.isAfter(latestRecord) }

    repository.insert(target)

    logger.info("Success upload process!")
}
