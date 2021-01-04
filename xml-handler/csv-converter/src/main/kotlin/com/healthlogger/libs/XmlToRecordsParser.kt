package com.healthlogger.libs

import com.healthlogger.libs.model.BodyTemperature
import com.healthlogger.libs.model.Records
import com.healthlogger.libs.model.StepCount
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.slf4j.LoggerFactory
import org.w3c.dom.Node
import org.xml.sax.InputSource
import java.io.File
import java.util.zip.ZipFile
import javax.xml.parsers.DocumentBuilderFactory

class XmlToRecordsParser {
    private val dateTimeFormatPattern = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss ZZ")
    private val logger = LoggerFactory.getLogger(javaClass)
    private val recordFileName = "export.xml"

    fun parse(file: File): Records = ZipFile(file).use {
        val entry = it
            .entries()
            .asSequence()
            .first { entry -> entry.name.contains(recordFileName) }

        val factory = DocumentBuilderFactory.newInstance()
        val parser = factory.newDocumentBuilder()
        val dom = parser.parse(InputSource(it.getInputStream(entry)))

        val tags = dom.getElementsByTagName("Record")

        val stepCounts = mutableListOf<StepCount>()
        val bodyTemperatures = mutableListOf<BodyTemperature>()

        for (index in 0 until tags.length) {
            when(tags.item(index).attributes.getNamedItem("type").nodeValue) {
                "HKQuantityTypeIdentifierStepCount" ->
                    stepCounts.add(tags.item(index).convertToStepCount())

                "HKQuantityTypeIdentifierBodyTemperature" ->
                    bodyTemperatures.add(tags.item(index).convertToBodyTemperature())

                else -> {
                    logger.debug("Type not match. attribute: ${tags.item(index).attributes.getNamedItem("type")}")
                }
            }
        }

        Records(stepCounts.toList(), bodyTemperatures.toList())
    }


    private fun Node.convertToStepCount() = StepCount(
        value = attributes.getNamedItem("value").nodeValue.toInt(),
        unit = attributes.getNamedItem("unit").nodeValue,
        creationDate = DateTime.parse(
            attributes.getNamedItem("creationDate").nodeValue,
            dateTimeFormatPattern
        ).toDate(),
        startDate = DateTime.parse(
            attributes.getNamedItem("startDate").nodeValue,
            dateTimeFormatPattern
        ).toDate(),
        endDate = DateTime.parse(
            attributes.getNamedItem("endDate").nodeValue,
            dateTimeFormatPattern
        ).toDate(),
    )

    private fun Node.convertToBodyTemperature() = BodyTemperature(
        value = attributes.getNamedItem("value").nodeValue.toDouble(),
        unit = attributes.getNamedItem("unit").nodeValue,
        creationDate = DateTime.parse(
            attributes.getNamedItem("creationDate").nodeValue,
            dateTimeFormatPattern
        ).toDate(),
        startDate = DateTime.parse(
            attributes.getNamedItem("startDate").nodeValue,
            dateTimeFormatPattern
        ).toDate(),
        endDate = DateTime.parse(
            attributes.getNamedItem("endDate").nodeValue,
            dateTimeFormatPattern
        ).toDate(),
    )
}
