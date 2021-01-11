package com.healthlogger.libs.model

import com.opencsv.bean.CsvBindByName
import com.opencsv.bean.CsvDate
import java.util.Date

data class StepCount(
    @CsvBindByName(column = "value", required = true)
    var value: Int? = null,
    @CsvBindByName(column = "unit", required = true)
    var unit: String? = null,
    @CsvDate("yyyy-MM-dd HH:mm:ss ZZ")
    @CsvBindByName(column = "creationDate", required = true)
    var creationDate: Date? = null,
    @CsvDate("yyyy-MM-dd HH:mm:ss ZZ")
    @CsvBindByName(column = "startDate", required = true)
    var startDate: Date? = null,
    @CsvDate("yyyy-MM-dd HH:mm:ss ZZ")
    @CsvBindByName(column = "endDate", required = true)
    var endDate: Date? = null,
)
