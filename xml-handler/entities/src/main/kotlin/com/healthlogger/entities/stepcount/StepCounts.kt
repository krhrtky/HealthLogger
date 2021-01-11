package com.healthlogger.entities.stepcount

class StepCounts(val list: List<StepCount>): List<StepCount> by list {
    fun filter(predicate: (StepCount) -> Boolean) = StepCounts(list.filter(predicate))
}
