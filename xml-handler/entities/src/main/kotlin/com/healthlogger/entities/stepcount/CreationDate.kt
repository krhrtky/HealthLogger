package com.healthlogger.entities.stepcount

import org.joda.time.DateTime
import java.util.Date

data class CreationDate(val value: DateTime) {

    constructor(value: Date): this(DateTime(value))
}
