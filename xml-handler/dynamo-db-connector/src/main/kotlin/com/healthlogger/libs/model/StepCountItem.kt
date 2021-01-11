package com.healthlogger.libs.model

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey

@DynamoDbBean
data class StepCountItem(
    @get:DynamoDbAttribute(value = "value")
    var value: Int? = null,
    @get:DynamoDbAttribute(value = "unit")
    var unit: String? = null,
    @get:DynamoDbSortKey
    @get:DynamoDbAttribute(value = "creationDate")
    var creationDate: Long? = null,
    @get:DynamoDbPartitionKey
    @get:DynamoDbAttribute(value = "startDate")
    var startDate: Long? = null,
    @get:DynamoDbAttribute(value = "endDate")
    var endDate: Long? = null,
)
