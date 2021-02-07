package com.healthlogger.libs

import com.healthlogger.libs.model.BodyTemperature
import com.healthlogger.libs.model.StepCount
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.slf4j.LoggerFactory
import org.w3c.dom.Node
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class XmlToCsvConverter: FileConverter {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val dateTimeFormatPattern = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss ZZ")
    private val parser = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    private val csvConverter = RecordsToCsvConverter()

    /**
     * XML から CSV に変換する.
     *
     * @param inputFile XML file.
     * @return CSV file.
     */
    override fun convert(inputFile: File): File {
        logger.info("Start convert.")

        val dom = parser.parse(inputFile)

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

        return csvConverter.convertStepCount(stepCounts)
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
