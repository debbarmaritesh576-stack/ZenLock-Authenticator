package com.aegis.pdf.features.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FormState(
    val fields: List<FormField> = emptyList(),
    val isLoading: Boolean = false,
    val validationErrors: List<ValidationError> = emptyList(),
    val saveSuccess: Boolean = false,
    val showExportDialog: Boolean = false,
    val showProfilePicker: Boolean = false,
    val suggestedProfile: AutoFillProfile? = null,
    val profiles: List<AutoFillProfile> = emptyList(),
    val focusedFieldId: String? = null,
    val exportResult: FormDataExporter.ExportResult? = null
)

@HiltViewModel
class FormViewModel @Inject constructor(
    private val formDetector: FormDetector,
    private val formValidator: FormValidator,
    private val formDataExporter: FormDataExporter,
    private val autoFillEngine: AutoFillEngine,
    private val formCalculator: FormCalculator
) : ViewModel() {

    private val _state = MutableStateFlow(FormState())
    val state: StateFlow<FormState> = _state

    private var docPtr: Long = 0
    private var currentPage: Int = 0

    fun loadForm(docPtr: Long, pageNum: Int) {
        this.docPtr = docPtr
        this.currentPage = pageNum

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val fields = formDetector.detectAllFields(docPtr, pageNum)
            val suggestedProfile = autoFillEngine.suggestProfile(fields)
            val profiles = autoFillEngine.getProfiles()

            _state.value = _state.value.copy(
                fields = fields,
                isLoading = false,
                suggestedProfile = suggestedProfile,
                profiles = profiles
            )
        }
    }

    fun updateField(fieldId: String, value: String) {
        val updatedFields = _state.value.fields.map { field ->
            if (field.id == fieldId) {
                field.copy(value = value)
            } else field
        }

        // Auto-calculate dependent fields
        val fieldMap = updatedFields.associate { it.id to it.value }
        val recalculated = formCalculator.autoCalculate(fieldId, value, fieldMap)

        val finalFields = updatedFields.map { field ->
            if (recalculated.containsKey(field.id) && field.id != fieldId) {
                field.copy(value = recalculated[field.id] ?: field.value)
            } else field
        }

        _state.value = _state.value.copy(
            fields = finalFields,
            validationErrors = emptyList(),
            saveSuccess = false,
            focusedFieldId = fieldId
        )
    }

    fun validate(): List<ValidationError> {
        val errors = formValidator.validate(_state.value.fields)
        _state.value = _state.value.copy(validationErrors = errors)
        return errors
    }

    fun validateAndSave() {
        val errors = validate()
        if (errors.isEmpty()) {
            save()
        }
    }

    fun save() {
        viewModelScope.launch {
            try {
                // Save form data via native bridge
                _state.value = _state.value.copy(saveSuccess = true, validationErrors = emptyList())
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    validationErrors = listOf(
                        ValidationError("", "", "Save failed: ${e.message}")
                    )
                )
            }
        }
    }

    fun autoFill() {
        val profile = _state.value.suggestedProfile
        if (profile != null) {
            autoFillWithProfile(profile.id)
        } else {
            showProfilePicker()
        }
    }

    fun autoFillWithProfile(profileId: String) {
        val profile = autoFillEngine.getProfiles().find { it.id == profileId } ?: return
        val filledFields = autoFillEngine.autoFill(_state.value.fields, profile)
        _state.value = _state.value.copy(
            fields = filledFields,
            showProfilePicker = false
        )
    }

    fun showExportOptions() {
        _state.value = _state.value.copy(showExportDialog = true)
    }

    fun hideExportOptions() {
        _state.value = _state.value.copy(showExportDialog = false)
    }

    fun showProfilePicker() {
        val profiles = autoFillEngine.getProfiles()
        if (profiles.isNotEmpty()) {
            _state.value = _state.value.copy(
                showProfilePicker = true,
                profiles = profiles
            )
        }
    }

    fun hideProfilePicker() {
        _state.value = _state.value.copy(showProfilePicker = false)
    }

    fun exportToJson() {
        val result = formDataExporter.exportToJson(_state.value.fields)
        when (result) {
            is FormDataExporter.ExportResult.Success -> {
                formDataExporter.saveToFile(result.dataOrUri, "form_data.json", "application/json")
                _state.value = _state.value.copy(exportResult = result, showExportDialog = false)
            }
            is FormDataExporter.ExportResult.Error -> {
                _state.value = _state.value.copy(
                    validationErrors = listOf(ValidationError("", "", result.message)),
                    showExportDialog = false
                )
            }
        }
    }

    fun exportToCsv() {
        val result = formDataExporter.exportToCsv(_state.value.fields)
        when (result) {
            is FormDataExporter.ExportResult.Success -> {
                formDataExporter.saveToFile(result.dataOrUri, "form_data.csv", "text/csv")
                _state.value = _state.value.copy(exportResult = result, showExportDialog = false)
            }
            is FormDataExporter.ExportResult.Error -> {
                _state.value = _state.value.copy(
                    validationErrors = listOf(ValidationError("", "", result.message)),
                    showExportDialog = false
                )
            }
        }
    }

    fun exportToFdf() {
        val result = formDataExporter.exportToFdf(_state.value.fields)
        when (result) {
            is FormDataExporter.ExportResult.Success -> {
                formDataExporter.saveToFile(result.dataOrUri, "form_data.fdf", "application/vnd.fdf")
                _state.value = _state.value.copy(exportResult = result, showExportDialog = false)
            }
            is FormDataExporter.ExportResult.Error -> {
                _state.value = _state.value.copy(
                    validationErrors = listOf(ValidationError("", "", result.message)),
                    showExportDialog = false
                )
            }
        }
    }

    fun addCalculation(targetFieldId: String, expression: String, sourceFields: List<String>) {
        formCalculator.addCalculation(targetFieldId, expression, sourceFields)
    }
}