package com.healthlogger.libs.file.cloudStorage

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import com.healthlogger.libs.file.FileDownloader
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.zip.ZipFile

class CloudStorageDownloader: FileDownloader {
    private val storage: Storage =  StorageOptions.getDefaultInstance().service
    private val bucketName = System.getenv("HEALTH_LOGGER_DATA_BUCKET_NAME")
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun download(key: String): File {
        logger.info("Start download file. filePath: $bucketName/$key")

        val blob = storage.get(BlobId.of(bucketName, key))
        val file = File.createTempFile("tmp", ".tmp")

        blob.downloadTo(file.toPath())
        logger.info("Complete download. File path: ${file.path}")

        val xmlFile = File.createTempFile("tmp", ".xml")

        ZipFile(file).use {
             val entry =   it
                .entries()
                .asSequence()
                .first { entry -> entry.name.contains("export.xml") }

            val stream = it.getInputStream(entry)
            Files.copy(stream, xmlFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }

        logger.info("Extract xml. File path: ${xmlFile.path}")

        return xmlFile
    }
}
