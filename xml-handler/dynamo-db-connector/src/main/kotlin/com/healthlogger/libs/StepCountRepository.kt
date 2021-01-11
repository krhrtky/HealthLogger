package com.healthlogger.libs

import com.healthlogger.entities.stepcount.CreationDate
import com.healthlogger.entities.stepcount.EndDate
import com.healthlogger.entities.stepcount.StartDate
import com.healthlogger.entities.stepcount.StepCount
import com.healthlogger.entities.stepcount.StepCounts
import com.healthlogger.entities.stepcount.Unit
import com.healthlogger.entities.stepcount.Value
import com.healthlogger.libs.model.StepCountItem
import software.amazon.awssdk.auth.credentials.ContainerCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import java.util.Date


class StepCountRepository {
    private val client = DynamoDbEnhancedClient
        .builder()
        .dynamoDbClient(
            DynamoDbClient
                .builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(ContainerCredentialsProvider.builder().build())
                .build()
        )
        .build()

    private val table =
        client.table("StepCount", TableSchema.fromBean(StepCountItem::class.java))

    fun insert(stepCounts: StepCounts) = stepCounts.forEach {
        table.putItem(
            StepCountItem(
                value = it.value.value,
                unit = it.unit.value,
                creationDate = it.creationDate.value.millis,
                startDate = it.startDate.value.millis,
                endDate = it.endDate.value.millis,
            )
        )
    }

    fun findLatestItem(): StepCount? {

        val result = table.scan(
            ScanEnhancedRequest
                .builder()
                .limit(1)
                .build()
        )
            .items()
            .map {
                StepCount(
                    value = Value(it.value!!),
                    unit = Unit(it.unit!!),
                    creationDate = CreationDate(Date(it.creationDate!!)),
                    startDate = StartDate(Date(it.startDate!!)),
                    endDate = EndDate(Date(it.endDate!!)),
                )
            }

        return if (result.isEmpty()) null else result[0]

    }
}
