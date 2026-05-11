package com.aegis.pdf.features.form

import org.json.JSONArray
import org.json.JSONObject

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
    val tabOrder: Int = 0,
    val fieldFlags: Int = 0
)

enum class FormFieldType {
    TEXT, MULTILINE_TEXT, NUMBER, DATE, TIME,
    CHECKBOX, RADIO, DROPDOWN, LISTBOX,
    SIGNATURE, IMAGE, BARCODE, PASSWORD, EMAIL, PHONE
}

// PDF Spec: Field flags for Button fields (Btn)
object PdfFieldFlags {
    const val PUSHBUTTON = 0x10000      // Bit 17
    const val RADIO = 0x8000            // Bit 16
    const val NO_TOGGLE_TO_OFF = 0x4000 // Bit 15 (Radio buttons only)
    
    fun isRadio(flags: Int): Boolean = (flags and RADIO) != 0
    fun isPushButton(flags: Int): Boolean = (flags and PUSHBUTTON) != 0
    fun isCheckbox(flags: Int): Boolean = !isRadio(flags) && !isPushButton(flags)
}

// PDF Spec: Field flags for Choice fields (Ch)
object PdfChoiceFlags {
    const val COMBO = 0x20000           // Bit 18: Dropdown if set, Listbox if not
    
    fun isDropdown(flags: Int): Boolean = (flags and COMBO) != 0
    fun isListbox(flags: Int): Boolean = (flags and COMBO) == 0
}

class FormFieldParser {

    fun parseAcroFormJson(json: String): List<FormField> {
        val fields = mutableListOf<FormField>()
        try {
            val root = JSONObject(json)
            val fieldsArray = root.optJSONArray("fields") ?: return fields
            
            for (i in 0 until fieldsArray.length()) {
                val fieldJson = fieldsArray.getJSONObject(i)
                fields.add(parseField(fieldJson))
                
                // Handle child fields (radio button groups)
                val kids = fieldJson.optJSONArray("Kids")
                if (kids != null) {
                    for (j in 0 until kids.length()) {
                        fields.add(parseField(kids.getJSONObject(j)))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return fields
    }

    fun parseField(json: JSONObject): FormField {
        val ft = json.optString("FT", "Tx")
        val flags = json.optInt("Ff", 0)
        val type = parseFieldTypeWithFlags(ft, flags)
        
        return FormField(
            id = json.optString("id", java.util.UUID.randomUUID().toString()),
            name = json.optString("T", json.optString("TU", "")),
            type = type,
            value = json.optString("V", ""),
            defaultValue = json.optString("DV", ""),
            options = parseOptions(json.optJSONArray("Opt")),
            isRequired = (flags and 0x02) != 0,       // Bit 2: Required
            isReadOnly = (flags and 0x01) != 0,        // Bit 1: ReadOnly
            isMultiline = (flags and 0x1000) != 0,     // Bit 13: Multiline
            maxLength = json.optInt("MaxLen", 0),
            tooltip = json.optString("TU", ""),
            fieldFlags = flags
        )
    }

    /**
     * PDF Spec compliant type detection with /Flags (Ff) checking
     */
    fun parseFieldTypeWithFlags(ft: String, flags: Int): FormFieldType {
        return when (ft) {
            "Tx" -> {
                // Text field
                when {
                    (flags and 0x200000) != 0 -> FormFieldType.PASSWORD  // Bit 22
                    (flags and 0x1000) != 0 -> FormFieldType.MULTILINE_TEXT  // Bit 13
                    else -> FormFieldType.TEXT
                }
            }
            "Btn" -> {
                // Button: Could be Pushbutton, Checkbox, or Radio
                when {
                    PdfFieldFlags.isPushButton(flags) -> FormFieldType.TEXT // Pushbutton is non-interactive
                    PdfFieldFlags.isRadio(flags) -> FormFieldType.RADIO
                    else -> FormFieldType.CHECKBOX
                }
            }
            "Ch" -> {
                // Choice: Could be Dropdown or Listbox
                if (PdfChoiceFlags.isDropdown(flags)) FormFieldType.DROPDOWN
                else FormFieldType.LISTBOX
            }
            "Sig" -> FormFieldType.SIGNATURE
            else -> FormFieldType.TEXT
        }
    }

    fun serializeField(field: FormField): JSONObject {
        return JSONObject().apply {
            put("id", field.id)
            put("T", field.name)
            put("FT", serializeFieldType(field.type))
            put("V", escapePdfString(field.value))
            put("DV", escapePdfString(field.defaultValue))
            if (field.options.isNotEmpty()) {
                put("Opt", JSONArray(field.options))
            }
            if (field.fieldFlags > 0) {
                put("Ff", field.fieldFlags)
            }
        }
    }

    private fun serializeFieldType(type: FormFieldType): String {
        return when (type) {
            FormFieldType.TEXT, FormFieldType.MULTILINE_TEXT, 
            FormFieldType.NUMBER, FormFieldType.DATE, FormFieldType.TIME,
            FormFieldType.PASSWORD, FormFieldType.EMAIL, FormFieldType.PHONE -> "Tx"
            FormFieldType.CHECKBOX, FormFieldType.RADIO -> "Btn"
            FormFieldType.DROPDOWN, FormFieldType.LISTBOX -> "Ch"
            FormFieldType.SIGNATURE -> "Sig"
            FormFieldType.IMAGE, FormFieldType.BARCODE -> "Tx"
        }
    }

    private fun parseOptions(optArray: JSONArray?): List<String> {
        if (optArray == null) return emptyList()
        val options = mutableListOf<String>()
        for (i in 0 until optArray.length()) {
            val item = optArray.opt(i)
            when (item) {
                is String -> options.add(item)
                is JSONArray -> {
                    // PDF spec: [ExportValue, DisplayValue] pairs
                    if (item.length() >= 2) {
                        options.add(item.getString(1)) // Display value
                    }
                }
            }
        }
        return options
    }

    /**
     * Escape special characters for PDF string format
     */
    fun escapePdfString(value: String): String {
        return value
            .replace("\\", "\\\\")
            .replace("(", "\\(")
            .replace(")", "\\)")
            .replace("\r", "\\r")
            .replace("\n", "\\n")
    }

    /**
     * Unescape PDF string
     */
    fun unescapePdfString(value: String): String {
        return value
            .replace("\\\\", "\\")
            .replace("\\(", "(")
            .replace("\\)", ")")
            .replace("\\r", "\r")
            .replace("\\n", "\n")
    }
}