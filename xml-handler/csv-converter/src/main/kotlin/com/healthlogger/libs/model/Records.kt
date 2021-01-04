package com.healthlogger.libs.model

data class Records(
        val stepCounts: List<StepCount>,
        val bodyTemperatures: List<BodyTemperature>
)
