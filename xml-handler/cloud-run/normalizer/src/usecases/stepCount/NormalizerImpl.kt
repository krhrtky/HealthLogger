package com.healthlogger.normalizer.usecases.stepCount

import com.healthlogger.libs.FileConverter
import com.healthlogger.libs.file.FileDownloader
import com.healthlogger.libs.file.FileUploader

class NormalizerImpl(
    private val fileDownloader: FileDownloader,
    private val fileUploader: FileUploader,
    private val converter: FileConverter,
    ): Normalizer {
    override fun normalize(filePath: String) {
        val downloadFile = fileDownloader.download(filePath)
        val convertedFile = converter.convert(downloadFile)
        fileUploader.upload(
            "convert/stepCount",
            System.currentTimeMillis().toString(),
            convertedFile
        )
    }
}
