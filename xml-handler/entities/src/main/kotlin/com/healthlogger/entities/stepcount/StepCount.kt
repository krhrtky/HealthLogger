package com.healthlogger.entities.stepcount

class StepCount(
    val value: Value,
    val unit: Unit,
    val creationDate: CreationDate,
    val startDate: StartDate,
    val endDate: EndDate,
) {
    override fun equals(other: Any?): Boolean {
        if (other is StepCount) {
            return value.value == other.value.value
                    && unit.value == other.unit.value
                    && creationDate.value.isEqual(other.creationDate.value)
                    && startDate.value.isEqual(other.startDate.value)
                    && endDate.value.isEqual(other.endDate.value)
        }
        return false
    }

    override fun hashCode(): Int {
        var result = value.hashCode()
        result = 31 * result + unit.hashCode()
        result = 31 * result + creationDate.hashCode()
        result = 31 * result + startDate.hashCode()
        result = 31 * result + endDate.hashCode()
        return result
    }
}
