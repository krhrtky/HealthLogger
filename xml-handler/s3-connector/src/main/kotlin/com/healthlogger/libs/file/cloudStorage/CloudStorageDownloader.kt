package com.healthlogger.libs.file.cloudStorage

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import com.healthlogger.libs.file.FileDownloader
import java.io.File

class CloudStorageDownloader: FileDownloader {
    companion object {
        private val storage: Storage =  StorageOptions.getDefaultInstance().service
        private val bucketName = System.getenv("HEALTH_LOGGER_DATA_BUCKET_NAME")
    }
    override fun download(key: String): File {
        val blob = storage.get(BlobId.of(bucketName, key))
        val file = File.createTempFile("tmp", ".tmp")

        blob.downloadTo(file.toPath())
        return file
    }
}
