package com.healthlogger.libs.file

import java.io.File

interface FileDownloader {
    fun download(key: String): File
}
