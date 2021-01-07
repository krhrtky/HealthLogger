package com.healthlogger.entities.stepcount

import java.lang.IllegalArgumentException

class Value(val value: Int) {

    init {
        if (value <= 0) {
            throw IllegalArgumentException("Value must be positive integer. Argument is '$value'.")
        }
    }
}
