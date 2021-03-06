package com.healthlogger.libs.file.s3

import com.healthlogger.libs.file.FileUploader
import org.slf4j.LoggerFactory
import software.amazon.awssdk.auth.credentials.ContainerCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.ssm.SsmClient
import software.amazon.awssdk.services.ssm.model.GetParameterRequest
import java.io.File

class S3Uploader: FileUploader {
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

    override fun upload(pathName: String, fileName: String, file: File) {
        val bucketName = ssmClient.getParameter(GetParameterRequest.builder().name("HealthLoggerDataBucketName").build())
        logger.info(bucketName.parameter().value())

        s3Client.putObject(
            PutObjectRequest
                .builder()
                .bucket(bucketName.parameter().value())
                .key(arrayOf(pathName, fileName).joinToString("/"))
                .build(),
            file.toPath()
        )
    }
}
