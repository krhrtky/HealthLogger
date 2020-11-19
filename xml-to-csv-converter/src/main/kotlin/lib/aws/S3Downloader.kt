package lib.aws

import org.slf4j.LoggerFactory
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.ssm.SsmClient
import software.amazon.awssdk.services.ssm.model.GetParameterRequest
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream


class S3Downloader {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val s3Client = S3Client
        .builder()
        .region(Region.US_EAST_1)
        .credentialsProvider(ProfileCredentialsProvider.create())
        .build()

    private val ssmClient = SsmClient.builder().build()

    fun download(key: String): File {
        val bucketName = ssmClient
                .getParameter(
                        GetParameterRequest
                                .builder()
                                .name("HealthLoggerDataBucketName")
                                .build()
                )

        logger.info(bucketName.parameter().value())

        val s3Obj = s3Client.getObject(
                GetObjectRequest
                        .builder()
                        .bucket(bucketName.parameter().value())
                        .key(key)
                        .build(),
        )

        val file = File.createTempFile("master-date-", ".zip")
        file.deleteOnExit()
        ZipOutputStream(FileOutputStream(file)).use {
            ZipInputStream(s3Obj).use { zis ->
                var ze: ZipEntry
                while (zis.nextEntry.also { ze = it } != null) {
                    if (ze.isDirectory) {
                        logger.warn("File has directory. key: $key.")
                    } else {
                        it.putNextEntry(ze)
                    }
                }
                it.closeEntry()
            }
        }

        return file



    }
}
