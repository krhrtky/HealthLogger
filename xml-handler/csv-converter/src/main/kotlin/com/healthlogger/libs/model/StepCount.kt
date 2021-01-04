package com.healthlogger.libs.model

import com.opencsv.bean.CsvBindByName
import com.opencsv.bean.CsvDate
import java.util.Date

data class StepCount(
    @CsvBindByName(column = "value", required = true)
    val value: Int,
    @CsvBindByName(column = "unit", required = true)
    val unit: String,
    @CsvDate("yyyy-MM-dd HH:mm:ss ZZ")
    @CsvBindByName(column = "creationDate", required = true)
    val creationDate: Date,
    @CsvDate("yyyy-MM-dd HH:mm:ss ZZ")
    @CsvBindByName(column = "startDate", required = true)
    val startDate: Date,
    @CsvDate("yyyy-MM-dd HH:mm:ss ZZ")
    @CsvBindByName(column = "endDate", required = true)
    val endDate: Date,
)
