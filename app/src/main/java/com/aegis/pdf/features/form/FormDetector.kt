package com.aegis.pdf.features.form

import com.aegis.pdf.core.NativeBridge
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FormDetector @Inject constructor(
    private val nativeBridge: NativeBridge
) {

    /**
     * Primary method: Parse real AcroForm fields from PDF
     * Uses native bridge to extract /Fields array from PDF Catalog → AcroForm dictionary
     */
    fun detectAcroFormFields(docPtr: Long): List<FormField> {
        val fields = mutableListOf<FormField>()
        
        try {
            // Native call to get AcroForm field data as JSON
            val acroFormJson = nativeBridge.extractAcroFormFields(docPtr)
            if (acroFormJson.isNotBlank()) {
                val parser = FormFieldParser()
                fields.addAll(parser.parseAcroFormJson(acroFormJson))
            }
        } catch (e: Exception) {
            // Log to crash reporter, not printStackTrace
            e.printStackTrace()
        }
        
        return fields
    }

    /**
     * Fallback: Detect form-like patterns in text-only PDFs
     * Only runs if AcroForm detection returns empty
     */
    fun detectTextFormFields(docPtr: Long, pageNum: Int): List<FormField> {
        val fields = mutableListOf<FormField>()
        
        try {
            val pageText = nativeBridge.extractText(docPtr, pageNum)
            
            // Text field pattern: Label followed by underline/blank
            val textFieldPattern = Regex(
                """(Name|Full\s*Name|Email|E-?mail|Phone|Telephone|Mobile|Address|Street|City|State|Zip|Postal|Country|Date|Signature|Company|Title|Department)\s*[:.]?\s*[_]{3,}""",
                RegexOption.IGNORE_CASE
            )
            
            textFieldPattern.findAll(pageText).forEach { match ->
                val label = match.groupValues[1].trim()
                fields.add(
                    FormField(
                        id = "text_field_${UUID.randomUUID().toString().take(8)}",
                        name = label,
                        type = determineTypeFromLabel(label),
                        pageNumber = pageNum
                    )
                )
            }

            // Checkbox pattern
            val checkboxPattern = Regex("""\[[\s]*\]|☐|□|⬜""")
            checkboxPattern.findAll(pageText).forEachIndexed { index, _ ->
                fields.add(
                    FormField(
                        id = "checkbox_${UUID.randomUUID().toString().take(8)}",
                        name = "Checkbox ${index + 1}",
                        type = FormFieldType.CHECKBOX,
                        pageNumber = pageNum
                    )
                )
            }

            // Radio button pattern
            val radioPattern = Regex("""\([\s]*\)|○|◯|⚪""")
            radioPattern.findAll(pageText).forEachIndexed { index, _ ->
                fields.add(
                    FormField(
                        id = "radio_${UUID.randomUUID().toString().take(8)}",
                        name = "Option ${index + 1}",
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

    /**
     * Smart detection: Try AcroForm first, fallback to text regex
     */
    fun detectAllFields(docPtr: Long, pageNum: Int): List<FormField> {
        // First try real AcroForm parsing
        val acroFields = detectAcroFormFields(docPtr)
        if (acroFields.isNotEmpty()) {
            // Filter fields for current page only
            return acroFields.filter { it.pageNumber == 0 || it.pageNumber == pageNum }
        }
        
        // Fallback to text-based detection
        return detectTextFormFields(docPtr, pageNum)
    }

    fun hasFormFields(docPtr: Long, pageNum: Int): Boolean {
        return detectAllFields(docPtr, pageNum).isNotEmpty()
    }

    /**
     * Determine field type from label text
     */
    private fun determineTypeFromLabel(label: String): FormFieldType {
        return when {
            label.contains("signature", ignoreCase = true) -> FormFieldType.SIGNATURE
            label.contains("date", ignoreCase = true) -> FormFieldType.DATE
            label.contains("time", ignoreCase = true) -> FormFieldType.TIME
            label.contains("email", ignoreCase = true) || label.contains("e-mail", ignoreCase = true) -> FormFieldType.TEXT
            label.contains("phone", ignoreCase = true) || label.contains("mobile", ignoreCase = true) -> FormFieldType.NUMBER
            label.contains("address", ignoreCase = true) -> FormFieldType.MULTILINE_TEXT
            else -> FormFieldType.TEXT
        }
    }
}