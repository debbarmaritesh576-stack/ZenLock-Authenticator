package com.aegis.pdf.features.form

import org.json.JSONObject

enum class FormFieldType {
    TEXT, MULTILINE_TEXT, NUMBER, DATE, TIME,
    CHECKBOX, RADIO, DROPDOWN, LISTBOX,
    SIGNATURE, IMAGE, BARCODE
}

data class FormField(
    val id: String,
    val name: String,
    val type: FormFieldType,
    val value: String = "",
    val defaultValue: String = "",
    val options: List<String> = emptyList(),
    val isRequired: Boolean = false,
    val isReadOnly: Boolean = false,
    val isMultiline: Boolean = false,
    val maxLength: Int = 0,
    val tooltip: String = "",
    val pageNumber: Int = 0,
    val rect: FloatArray = floatArrayOf(0f, 0f, 100f, 30f),
    val tabOrder: Int = 0
)

class FormFieldParser {

    fun parseField(json: JSONObject): FormField {
        val type = parseFieldType(json.optString("FT", "Tx"))
        return FormField(
            id = json.optString("id", ""),
            name = json.optString("T", ""),
            type = type,
            value = json.optString("V", ""),
            defaultValue = json.optString("DV", ""),
            options = parseOptions(json.optJSONArray("Opt")),
            isRequired = json.optBoolean("Required", false),
            isReadOnly = json.optBoolean("ReadOnly", false),
            isMultiline = json.optBoolean("Multiline", false),
            maxLength = json.optInt("MaxLen", 0),
            tooltip = json.optString("TU", "")
        )
    }

    fun serializeField(field: FormField): JSONObject {
        return JSONObject().apply {
            put("id", field.id)
            put("T", field.name)
            put("FT", serializeFieldType(field.type))
            put("V", field.value)
            put("DV", field.defaultValue)
        }
    }

    private fun parseFieldType(ft: String): FormFieldType {
        return when (ft) {
            "Tx" -> FormFieldType.TEXT
            "Btn" -> FormFieldType.CHECKBOX
            "Ch" -> FormFieldType.DROPDOWN
            "Sig" -> FormFieldType.SIGNATURE
            else -> FormFieldType.TEXT
        }
    }

    private fun serializeFieldType(type: FormFieldType): String {
        return when (type) {
            FormFieldType.TEXT -> "Tx"
            FormFieldType.CHECKBOX -> "Btn"
            FormFieldType.DROPDOWN -> "Ch"
            FormFieldType.SIGNATURE -> "Sig"
            else -> "Tx"
        }
    }

    private fun parseOptions(optArray: org.json.JSONArray?): List<String> {
        if (optArray == null) return emptyList()
        val options = mutableListOf<String>()
        for (i in 0 until optArray.length()) {
            options.add(optArray.getString(i))
        }
        return options
    }
}