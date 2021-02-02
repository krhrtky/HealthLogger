package com.healthlogger.libs.file.s3

import com.healthlogger.libs.file.FileDownloader
import org.slf4j.LoggerFactory
import software.amazon.awssdk.auth.credentials.ContainerCredentialsProvider
import software.amazon.awssdk.core.sync.ResponseTransformer
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.ssm.SsmClient
import software.amazon.awssdk.services.ssm.model.GetParameterRequest
import java.io.File
import java.io.FileOutputStream


class S3Downloader: FileDownloader {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val s3Client = S3Client
        .builder()
        .region(Region.US_EAST_1)
        .credentialsProvider(ContainerCredentialsProvider.builder().build())
        .build()

    private val ssmClient = SsmClient
            .builder()
            .region(Region.US_EAST_1)
            .credentialsProvider(ContainerCredentialsProvider.builder().build())
            .build()

    override fun download(key: String): File {
        val bucketName = ssmClient
                .getParameter(
                        GetParameterRequest
                                .builder()
                                .name("HealthLoggerDataBucketName")
                                .build()
                )

        logger.info("bucket: ${bucketName.parameter().value()}. key: $key")

        val file = File.createTempFile("master-date-", ".zip")
        file.deleteOnExit()
        FileOutputStream(file).use { it ->
            s3Client.getObject(
                    GetObjectRequest
                            .builder()
                            .bucket(bucketName.parameter().value())
                            .key(key)
                            .build(),
                    ResponseTransformer.toOutputStream(it)
            )
        }
        return file
    }
}
