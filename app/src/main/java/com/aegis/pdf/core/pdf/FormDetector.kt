package com.aegis.pdf.core.pdf

import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDAcroForm
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDField
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FormDetector @Inject constructor() {

    data class FormField(
        val name: String,
        val type: String,
        val value: String = "",
        val isRequired: Boolean = false,
        val options: List<String> = emptyList()
    )

    fun detectFormFields(file: File): List<FormField> {
        return try {
            PDDocument.load(file).use { document ->
                val acroForm = document.documentCatalog.acroForm
                if (acroForm == null) return emptyList()

                val fields = mutableListOf<FormField>()
                acroForm.fields.forEach { field ->
                    fields.add(
                        FormField(
                            name = field.fullyQualifiedName ?: "Unknown",
                            type = field.fieldType ?: "Text",
                            value = field.valueAsString ?: "",
                            isRequired = field.isRequired,
                            options = getFieldOptions(field)
                        )
                    )
                }
                fields
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun fillFormFields(
        inputFile: File,
        outputFile: File,
        fieldValues: Map<String, String>
    ): Boolean {
        return try {
            PDDocument.load(inputFile).use { document ->
                val acroForm = document.documentCatalog.acroForm ?: return false
                fieldValues.forEach { (name, value) ->
                    val field = acroForm.getField(name)
                    field?.let { it.valueAsString = value }
                }
                document.save(outputFile)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun getFieldOptions(field: PDField): List<String> {
        return try {
            if (field is org.apache.pdfbox.pdmodel.interactive.form.PDChoice) {
                field.options
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}