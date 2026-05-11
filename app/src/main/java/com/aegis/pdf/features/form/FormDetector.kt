package com.aegis.pdf.features.form

import com.aegis.pdf.core.NativeBridge
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FormDetector @Inject constructor(
    private val nativeBridge: NativeBridge
) {

    fun detectForms(docPtr: Long, pageNum: Int): List<FormField> {
        val fields = mutableListOf<FormField>()
        
        try {
            val pageText = nativeBridge.extractText(docPtr, pageNum)
            
            // Detect text fields (look for empty spaces with labels)
            val textFieldPattern = Regex("""
                (Name|Email|Phone|Address|Date|Signature|City|State|Zip|Country)
                \s*[:.]?\s*
                [_]{3,}
            """.trimIndent(), RegexOption.IGNORE_CASE)
            
            textFieldPattern.findAll(pageText).forEach { match ->
                fields.add(
                    FormField(
                        id = "field_${System.currentTimeMillis()}_${fields.size}",
                        name = match.groupValues[1],
                        type = when {
                            match.groupValues[1].contains("signature", true) -> FormFieldType.SIGNATURE
                            match.groupValues[1].contains("date", true) -> FormFieldType.DATE
                            match.groupValues[1].contains("email", true) -> FormFieldType.TEXT
                            else -> FormFieldType.TEXT
                        },
                        pageNumber = pageNum
                    )
                )
            }

            // Detect checkboxes [ ] or ☐
            val checkboxPattern = Regex("\\[\\s*\\]|☐|□")
            checkboxPattern.findAll(pageText).forEach { match ->
                fields.add(
                    FormField(
                        id = "checkbox_${System.currentTimeMillis()}_${fields.size}",
                        name = "Checkbox ${fields.size + 1}",
                        type = FormFieldType.CHECKBOX,
                        pageNumber = pageNum
                    )
                )
            }

            // Detect radio buttons ( ) or ○
            val radioPattern = Regex("\\(\\s*\\)|○|◯")
            radioPattern.findAll(pageText).forEach { match ->
                fields.add(
                    FormField(
                        id = "radio_${System.currentTimeMillis()}_${fields.size}",
                        name = "Radio ${fields.size + 1}",
                        type = FormFieldType.RADIO,
                        pageNumber = pageNum
                    )
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return fields
    }

    fun detectAllForms(docPtr: Long, pageCount: Int): Map<Int, List<FormField>> {
        val allFields = mutableMapOf<Int, List<FormField>>()
        for (page in 1..pageCount) {
            val fields = detectForms(docPtr, page)
            if (fields.isNotEmpty()) {
                allFields[page] = fields
            }
        }
        return allFields
    }

    fun hasFormFields(docPtr: Long, pageNum: Int): Boolean {
        return detectForms(docPtr, pageNum).isNotEmpty()
    }
}