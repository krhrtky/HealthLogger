package com.healthlogger.libs.file

import java.io.File

interface FileUploader {
    fun upload(pathName: String, fileName: String, file: File): Unit
}
