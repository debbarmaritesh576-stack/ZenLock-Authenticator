package com.aegis.pdf.features.editor.annotation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import java.util.UUID

data class UnderlineAnnotation(
    val id: Long,
    val pageNumber: Int,
    val points: List<Offset>,
    val color: Color = Color.Red,
    val thickness: Float = 3f
)

class UnderlineTool {

    private var isDragging = false

    private val points = mutableListOf<Offset>()

    fun start(start: Offset) {
        isDragging = true
        points.clear()
        points.add(start)
    }

    fun update(current: Offset) {
        if (!isDragging) return
        points.add(current)
    }

    fun end(pageNumber: Int): UnderlineAnnotation? {

        isDragging = false

        if (points.size < 2) {
            points.clear()
            return null
        }

        val annotation = UnderlineAnnotation(
            id = UUID.randomUUID().mostSignificantBits,
            pageNumber = pageNumber,
            points = points.toList()
        )

        points.clear()

        return annotation
    }

    fun DrawScope.draw(annotation: UnderlineAnnotation) {

        if (annotation.points.size < 2) return

        val path = Path()

        path.moveTo(
            annotation.points.first().x,
            annotation.points.first().y
        )

        for (i in 1 until annotation.points.size) {

            val point = annotation.points[i]

            path.lineTo(point.x, point.y)
        }

        drawPath(
            path = path,
            color = annotation.color,
            style = Stroke(
                width = annotation.thickness,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}