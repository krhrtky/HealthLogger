package com.healthlogger.normalizer.routes

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Body(val message: Message) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Message(var data: String)
}
