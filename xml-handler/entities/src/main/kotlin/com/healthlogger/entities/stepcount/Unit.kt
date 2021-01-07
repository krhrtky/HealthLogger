package com.healthlogger.entities.stepcount

import java.lang.IllegalArgumentException

data class Unit(val value: String) {
    init {
        if (value.isBlank()) {
            throw IllegalArgumentException("Value must not be empty or only white space. Argument is '$value'.")
        }
    }
}
