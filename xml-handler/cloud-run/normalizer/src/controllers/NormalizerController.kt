package com.healthlogger.normalizer.controllers

import com.healthlogger.normalizer.usecases.stepCount.Normalizer

class NormalizerController(private val normalizer: Normalizer) {
    fun normalize(filePath: String) = normalizer.normalize(filePath)
}
