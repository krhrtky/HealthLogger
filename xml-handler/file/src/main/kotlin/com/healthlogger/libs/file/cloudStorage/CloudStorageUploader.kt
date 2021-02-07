package com.healthlogger.libs.file.cloudStorage

import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import com.healthlogger.libs.file.FileUploader
import java.io.File

class CloudStorageUploader: FileUploader {
    companion object {
        private val storage: Storage =  StorageOptions.getDefaultInstance().service
        private val bucketName = System.getenv("HEALTH_LOGGER_DATA_BUCKET_NAME")
    }
    override fun upload(pathName: String, fileName: String, file: File) {
        storage.createFrom(
            BlobInfo
                .newBuilder(bucketName, "$pathName/$fileName")
                .build(),
            file.inputStream()
        )
    }
}
