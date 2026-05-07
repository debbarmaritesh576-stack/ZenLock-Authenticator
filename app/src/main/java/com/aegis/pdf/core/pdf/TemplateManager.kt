package com.aegis.pdf.core.pdf

import android.content.Context
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TemplateManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    enum class TemplateType {
        INVOICE, RESUME, LETTER, BLANK, LINED, GRID
    }

    fun generatePdf(
        type: TemplateType,
        outputFile: File,
        content: Map<String, String> = emptyMap()
    ): Boolean {
        return try {
            PDDocument().use { document ->
                val page = PDPage(PDRectangle.A4)
                document.addPage(page)

                PDPageContentStream(document, page).use { cs ->
                    when (type) {
                        TemplateType.INVOICE -> drawInvoiceTemplate(cs, content)
                        TemplateType.RESUME -> drawResumeTemplate(cs, content)
                        TemplateType.LETTER -> drawLetterTemplate(cs, content)
                        TemplateType.LINED -> drawLinedTemplate(cs)
                        TemplateType.GRID -> drawGridTemplate(cs)
                        TemplateType.BLANK -> {} // Empty page
                    }
                }
                document.save(outputFile)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun drawInvoiceTemplate(
        cs: PDPageContentStream,
        content: Map<String, String>
    ) {
        val width = PDRectangle.A4.width
        val height = PDRectangle.A4.height

        cs.setFont(PDType1Font.HELVETICA_BOLD, 20f)
        cs.beginText()
        cs.newLineAtOffset(50f, height - 50f)
        cs.showText(content["companyName"] ?: "INVOICE")
        cs.endText()

        cs.setFont(PDType1Font.HELVETICA, 12f)
        cs.beginText()
        cs.newLineAtOffset(50f, height - 80f)
        cs.showText("Invoice #: ${content["invoiceNumber"] ?: "001"}")
        cs.endText()

        cs.beginText()
        cs.newLineAtOffset(50f, height - 95f)
        cs.showText("Date: ${content["date"] ?: "___________"}")
        cs.endText()

        cs.beginText()
        cs.newLineAtOffset(50f, height - 110f)
        cs.showText("Bill To: ${content["clientName"] ?: "___________"}")
        cs.endText()

        // Line items table header
        var yPos = height - 160f
        cs.setFont(PDType1Font.HELVETICA_BOLD, 10f)
        cs.beginText()
        cs.newLineAtOffset(50f, yPos)
        cs.showText("Description")
        cs.endText()
        cs.beginText()
        cs.newLineAtOffset(350f, yPos)
        cs.showText("Qty")
        cs.endText()
        cs.beginText()
        cs.newLineAtOffset(420f, yPos)
        cs.showText("Rate")
        cs.endText()
        cs.beginText()
        cs.newLineAtOffset(500f, yPos)
        cs.showText("Amount")
        cs.endText()

        // Line
        cs.setLineWidth(0.5f)
        cs.moveTo(50f, yPos - 5f)
        cs.lineTo(width - 50f, yPos - 5f)
        cs.stroke()

        // Total
        yPos -= 100f
        cs.setFont(PDType1Font.HELVETICA_BOLD, 12f)
        cs.beginText()
        cs.newLineAtOffset(50f, yPos)
        cs.showText("Total: ${content["total"] ?: "$0.00"}")
        cs.endText()
    }

    private fun drawResumeTemplate(
        cs: PDPageContentStream,
        content: Map<String, String>
    ) {
        val width = PDRectangle.A4.width
        val height = PDRectangle.A4.height

        cs.setFont(PDType1Font.HELVETICA_BOLD, 24f)
        cs.beginText()
        cs.newLineAtOffset(50f, height - 50f)
        cs.showText(content["name"] ?: "Your Name")
        cs.endText()

        cs.setFont(PDType1Font.HELVETICA, 12f)
        cs.beginText()
        cs.newLineAtOffset(50f, height - 70f)
        cs.showText(content["title"] ?: "Professional Title")
        cs.endText()

        cs.setFont(PDType1Font.HELVETICA, 10f)
        cs.beginText()
        cs.newLineAtOffset(50f, height - 90f)
        cs.showText("${content["email"] ?: "email@example.com"} | ${content["phone"] ?: "Phone"}")
        cs.endText()

        cs.setLineWidth(1f)
        cs.moveTo(50f, height - 100f)
        cs.lineTo(width - 50f, height - 100f)
        cs.stroke()

        cs.setFont(PDType1Font.HELVETICA_BOLD, 14f)
        cs.beginText()
        cs.newLineAtOffset(50f, height - 130f)
        cs.showText("Experience")
        cs.endText()

        cs.setFont(PDType1Font.HELVETICA, 11f)
        cs.beginText()
        cs.newLineAtOffset(50f, height - 150f)
        cs.showText(content["experience"] ?: "Add your work experience here...")
        cs.endText()
    }

    private fun drawLetterTemplate(
        cs: PDPageContentStream,
        content: Map<String, String>
    ) {
        val height = PDRectangle.A4.height

        cs.setFont(PDType1Font.HELVETICA, 12f)
        cs.beginText()
        cs.newLineAtOffset(50f, height - 50f)
        cs.showText(content["senderAddress"] ?: "Your Address")
        cs.endText()

        cs.beginText()
        cs.newLineAtOffset(50f, height - 90f)
        cs.showText(content["date"] ?: "Date")
        cs.endText()

        cs.beginText()
        cs.newLineAtOffset(50f, height - 120f)
        cs.showText(content["recipientAddress"] ?: "Recipient Address")
        cs.endText()

        cs.beginText()
        cs.newLineAtOffset(50f, height - 160f)
        cs.showText("Dear ${content["recipientName"] ?: "Sir/Madam"},")
        cs.endText()

        cs.setFont(PDType1Font.HELVETICA, 11f)
        val body = content["body"] ?: "Write your letter content here..."
        val lines = body.chunked(80)
        var y = height - 190f
        lines.forEach { line ->
            cs.beginText()
            cs.newLineAtOffset(50f, y)
            cs.showText(line)
            cs.endText()
            y -= 15f
        }

        y -= 30f
        cs.setFont(PDType1Font.HELVETICA, 12f)
        cs.beginText()
        cs.newLineAtOffset(50f, y)
        cs.showText("Sincerely,")
        cs.endText()

        y -= 30f
        cs.beginText()
        cs.newLineAtOffset(50f, y)
        cs.showText(content["senderName"] ?: "Your Name")
        cs.endText()
    }

    private fun drawLinedTemplate(cs: PDPageContentStream) {
        val width = PDRectangle.A4.width
        val height = PDRectangle.A4.height

        cs.setLineWidth(0.3f)
        cs.setNonStrokingColor(200, 200, 200)

        var y = height - 50f
        while (y > 50f) {
            cs.moveTo(50f, y)
            cs.lineTo(width - 50f, y)
            cs.stroke()
            y -= 25f
        }
    }

    private fun drawGridTemplate(cs: PDPageContentStream) {
        val width = PDRectangle.A4.width
        val height = PDRectangle.A4.height
        val gridSize = 20f

        cs.setLineWidth(0.2f)
        cs.setNonStrokingColor(220, 220, 220)

        var x = 50f
        while (x < width - 50f) {
            cs.moveTo(x, 50f)
            cs.lineTo(x, height - 50f)
            cs.stroke()
            x += gridSize
        }

        var y = 50f
        while (y < height - 50f) {
            cs.moveTo(50f, y)
            cs.lineTo(width - 50f, y)
            cs.stroke()
            y += gridSize
        }
    }
}