package com.healthlogger.normalizer.externals.di

import com.healthlogger.libs.FileConverter
import com.healthlogger.libs.XmlToCsvConverter
import com.healthlogger.libs.file.FileDownloader
import com.healthlogger.libs.file.FileUploader
import com.healthlogger.libs.file.cloudStorage.CloudStorageDownloader
import com.healthlogger.libs.file.cloudStorage.CloudStorageUploader
import com.healthlogger.normalizer.controllers.NormalizerController
import com.healthlogger.normalizer.usecases.stepCount.Normalizer
import com.healthlogger.normalizer.usecases.stepCount.NormalizerImpl
import org.koin.dsl.module

class DIContainer {
    companion object {
        val normalizer = module {
            single<FileDownloader> { CloudStorageDownloader() }
            single<FileUploader> { CloudStorageUploader() }
            single<FileConverter> { XmlToCsvConverter() }

            single<Normalizer> { NormalizerImpl(get(), get(), get()) }
            factory { NormalizerController(get())}
        }
    }
}
