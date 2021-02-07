package com.healthlogger.libs

import java.io.File

interface FileConverter {
    fun convert(inputFile: File): File
}
