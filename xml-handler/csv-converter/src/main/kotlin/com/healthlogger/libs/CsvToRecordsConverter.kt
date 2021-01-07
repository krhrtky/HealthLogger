package com.healthlogger.libs

import com.healthlogger.entities.stepcount.CreationDate
import com.healthlogger.entities.stepcount.EndDate
import com.healthlogger.entities.stepcount.StartDate
import com.healthlogger.entities.stepcount.StepCounts
import com.healthlogger.entities.stepcount.StepCount
import com.healthlogger.entities.stepcount.Unit
import com.healthlogger.entities.stepcount.Value
import com.healthlogger.libs.model.StepCount as Bean
import com.opencsv.bean.CsvToBeanBuilder
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

class CsvToRecordsConverter {

    fun convertStepCount(stepCountsFile: File): StepCounts {

        BufferedReader(InputStreamReader(FileInputStream(stepCountsFile))).use { reader ->
            val list = CsvToBeanBuilder<Bean>(reader)
                .build()
                .parse()
                .map {
                    StepCount(
                        value = Value(it.value),
                        unit = Unit(it.unit),
                        creationDate = CreationDate(it.creationDate),
                        startDate = StartDate(it.startDate),
                        endDate = EndDate(it.endDate),
                    )
                }
            return StepCounts(list)
        }
    }
}
