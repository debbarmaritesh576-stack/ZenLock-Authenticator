package com.aegis.pdf.features.editor.text

import javax.inject.Inject
import javax.inject.Singleton

data class TextLayer(
    val id: Long,
    val name: String,
    val elements: List<TextElement>,
    val isVisible: Boolean = true,
    val isLocked: Boolean = false,
    val opacity: Float = 1.0f
)

@Singleton
class TextLayerManager @Inject constructor() {

    private val layers = mutableListOf<TextLayer>()
    private var activeLayerId: Long = 0L
    private var nextLayerId = 0L

    init {
        // Default layer
        layers.add(
            TextLayer(
                id = nextLayerId++,
                name = "Layer 1",
                elements = emptyList()
            )
        )
        activeLayerId = layers.first().id
    }

    fun getLayers(): List<TextLayer> = layers.toList()

    fun getActiveLayer(): TextLayer? = layers.find { it.id == activeLayerId }

    fun getActiveLayerElements(): List<TextElement> = getActiveLayer()?.elements ?: emptyList()

    fun setActiveLayer(layerId: Long) {
        if (layers.any { it.id == layerId }) {
            activeLayerId = layerId
        }
    }

    fun addLayer(name: String): TextLayer {
        val layer = TextLayer(id = nextLayerId++, name = name, elements = emptyList())
        layers.add(layer)
        return layer
    }

    fun removeLayer(layerId: Long): Boolean {
        if (layers.size <= 1) return false
        val removed = layers.removeAll { it.id == layerId }
        if (activeLayerId == layerId) {
            activeLayerId = layers.firstOrNull()?.id ?: 0L
        }
        return removed
    }

    fun updateLayerElements(layerId: Long, elements: List<TextElement>) {
        val index = layers.indexOfFirst { it.id == layerId }
        if (index >= 0) {
            layers[index] = layers[index].copy(elements = elements)
        }
    }

    fun toggleLayerVisibility(layerId: Long) {
        val index = layers.indexOfFirst { it.id == layerId }
        if (index >= 0) {
            layers[index] = layers[index].copy(isVisible = !layers[index].isVisible)
        }
    }

    fun toggleLayerLock(layerId: Long) {
        val index = layers.indexOfFirst { it.id == layerId }
        if (index >= 0) {
            layers[index] = layers[index].copy(isLocked = !layers[index].isLocked)
        }
    }

    fun renameLayer(layerId: Long, newName: String) {
        val index = layers.indexOfFirst { it.id == layerId }
        if (index >= 0) {
            layers[index] = layers[index].copy(name = newName)
        }
    }

    fun moveLayerUp(layerId: Long) {
        val index = layers.indexOfFirst { it.id == layerId }
        if (index > 0) {
            val layer = layers.removeAt(index)
            layers.add(index - 1, layer)
        }
    }

    fun moveLayerDown(layerId: Long) {
        val index = layers.indexOfFirst { it.id == layerId }
        if (index < layers.size - 1) {
            val layer = layers.removeAt(index)
            layers.add(index + 1, layer)
        }
    }

    fun getAllVisibleElements(): List<TextElement> {
        return layers
            .filter { it.isVisible }
            .flatMap { it.elements }
    }

    fun clearAll() {
        layers.clear()
        layers.add(TextLayer(id = nextLayerId++, name = "Layer 1", elements = emptyList()))
        activeLayerId = layers.first().id
    }
}