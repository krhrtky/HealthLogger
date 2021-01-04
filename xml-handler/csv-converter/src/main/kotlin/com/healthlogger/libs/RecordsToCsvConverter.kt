package com.healthlogger.libs

import com.healthlogger.libs.model.BodyTemperature
import com.healthlogger.libs.model.StepCount
import com.opencsv.CSVWriter
import com.opencsv.bean.StatefulBeanToCsvBuilder
import java.io.File

class RecordsToCsvConverter {

    fun convertStepCount(stepCounts: List<StepCount>): File {

        val file = File.createTempFile("step-count-", ".csv")
        file.deleteOnExit()
        file.writer().use {
            StatefulBeanToCsvBuilder<StepCount>(it)
                .withQuotechar(CSVWriter.DEFAULT_QUOTE_CHARACTER)
                .build()
                .write(stepCounts)
        }

        return file
    }

    fun convertBodyTemperature(bodyTemperatures: List<BodyTemperature>): File {
        val file = File.createTempFile("body-temperature-", ".csv")
        file.deleteOnExit()
        file.writer().use {
            StatefulBeanToCsvBuilder<BodyTemperature>(it)
                .withQuotechar(CSVWriter.DEFAULT_QUOTE_CHARACTER)
                .build()
                .write(bodyTemperatures)
        }

        return file
    }
}
