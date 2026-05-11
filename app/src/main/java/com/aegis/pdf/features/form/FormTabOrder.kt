package com.aegis.pdf.features.form

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FormTabOrder @Inject constructor() {

    private val fieldOrder = mutableMapOf<Int, MutableList<FormField>>()

    fun setTabOrder(pageNumber: Int, fields: List<FormField>) {
        val ordered = fields.sortedBy { it.rect[1] }.sortedBy { it.rect[0] }
        ordered.forEachIndexed { index, field ->
            fieldOrder.getOrPut(pageNumber) { mutableListOf() }.add(
                field.copy(tabOrder = index + 1)
            )
        }
    }

    fun getNextField(pageNumber: Int, currentFieldId: String): FormField? {
        val fields = fieldOrder[pageNumber] ?: return null
        val currentIndex = fields.indexOfFirst { it.id == currentFieldId }
        if (currentIndex < 0 || currentIndex >= fields.size - 1) return null
        return fields[currentIndex + 1]
    }

    fun getPreviousField(pageNumber: Int, currentFieldId: String): FormField? {
        val fields = fieldOrder[pageNumber] ?: return null
        val currentIndex = fields.indexOfFirst { it.id == currentFieldId }
        if (currentIndex <= 0) return null
        return fields[currentIndex - 1]
    }

    fun getFirstField(pageNumber: Int): FormField? {
        return fieldOrder[pageNumber]?.firstOrNull()
    }

    fun getAllFields(pageNumber: Int): List<FormField> {
        return fieldOrder[pageNumber]?.toList() ?: emptyList()
    }
}