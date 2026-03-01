package com.kundaliai.app.pdf

import android.content.Context
import android.graphics.Color
import android.os.Environment
import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.*
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.kundaliai.app.data.models.KundaliResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class PdfGenerator(private val context: Context) {

    // Colors
    private val deepBlue = DeviceRgb(13, 27, 42)
    private val gold = DeviceRgb(255, 215, 0)
    private val purple = DeviceRgb(58, 12, 163)
    private val white = DeviceRgb(255, 255, 255)
    private val lightGrey = DeviceRgb(240, 240, 240)
    private val darkText = DeviceRgb(30, 30, 50)

    suspend fun generatePdf(result: KundaliResult): File? {
        return withContext(Dispatchers.IO) {
            try {
                val fileName = "Kundali_${result.userName.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
                val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                    ?: context.filesDir
                val file = File(outputDir, fileName)

                val writer = PdfWriter(file)
                val pdfDoc = PdfDocument(writer)
                val document = Document(pdfDoc, PageSize.A4)
                document.setMargins(40f, 40f, 40f, 40f)

                val boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)
                val regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA)
                val italicFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE)

                // ── Cover Header ─────────────────────────────────────────────
                val headerTable = Table(UnitValue.createPercentArray(floatArrayOf(100f)))
                    .useAllAvailableWidth()

                val headerCell = Cell()
                    .setBackgroundColor(deepBlue)
                    .setPadding(20f)
                    .add(
                        Paragraph("✨ KUNDALI AI ✨")
                            .setFont(boldFont)
                            .setFontSize(24f)
                            .setFontColor(gold)
                            .setTextAlignment(TextAlignment.CENTER)
                    )
                    .add(
                        Paragraph("AI-Powered Vedic Horoscope Report")
                            .setFont(regularFont)
                            .setFontSize(13f)
                            .setFontColor(white)
                            .setTextAlignment(TextAlignment.CENTER)
                    )
                    .add(
                        Paragraph("Ancient Wisdom. Powered by AI.")
                            .setFont(italicFont)
                            .setFontSize(11f)
                            .setFontColor(gold)
                            .setTextAlignment(TextAlignment.CENTER)
                    )

                headerTable.addCell(headerCell)
                document.add(headerTable)
                document.add(Paragraph("\n"))

                // ── User Details ─────────────────────────────────────────────
                document.add(sectionTitle("👤 User Details", boldFont))

                val userTable = Table(UnitValue.createPercentArray(floatArrayOf(40f, 60f)))
                    .useAllAvailableWidth()
                    .setMarginBottom(10f)

                addTableRow(userTable, "Name", result.userName, boldFont, regularFont)
                addTableRow(userTable, "Date of Birth", result.dateOfBirth, boldFont, regularFont)
                addTableRow(userTable, "Time of Birth", result.timeOfBirth, boldFont, regularFont)
                addTableRow(userTable, "Place of Birth", result.placeOfBirth, boldFont, regularFont)
                addTableRow(userTable, "Report Generated", SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date()), boldFont, regularFont)
                document.add(userTable)
                document.add(Paragraph("\n"))

                // ── Jyotish Basics ───────────────────────────────────────────
                document.add(sectionTitle("🔮 Kundali Details", boldFont))

                val kundaliTable = Table(UnitValue.createPercentArray(floatArrayOf(40f, 60f)))
                    .useAllAvailableWidth()
                    .setMarginBottom(10f)

                addTableRow(kundaliTable, "Lagna (Ascendant)", result.lagna, boldFont, regularFont)
                addTableRow(kundaliTable, "Rashi (Moon Sign)", result.rashi, boldFont, regularFont)
                addTableRow(kundaliTable, "Nakshatra (Birth Star)", result.nakshatra, boldFont, regularFont)
                addTableRow(kundaliTable, "Lagna Lord", result.lagnaLord, boldFont, regularFont)
                addTableRow(kundaliTable, "Rashi Lord", result.rashiLord, boldFont, regularFont)
                document.add(kundaliTable)
                document.add(Paragraph("\n"))

                // ── Planet Positions ─────────────────────────────────────────
                document.add(sectionTitle("🪐 Planet Positions", boldFont))

                val planetTable = Table(UnitValue.createPercentArray(floatArrayOf(30f, 35f, 20f, 15f)))
                    .useAllAvailableWidth()
                    .setMarginBottom(10f)

                // Header row
                listOf("Planet", "Sign", "House", "Degree").forEach { header ->
                    planetTable.addHeaderCell(
                        Cell().setBackgroundColor(purple)
                            .add(
                                Paragraph(header).setFont(boldFont).setFontSize(10f)
                                    .setFontColor(white)
                            )
                    )
                }

                result.planetPositions.forEachIndexed { index, planet ->
                    val bg = if (index % 2 == 0) lightGrey else white
                    planetTable.addCell(Cell().setBackgroundColor(bg).add(Paragraph(planet.planet).setFont(boldFont).setFontSize(9f)))
                    planetTable.addCell(Cell().setBackgroundColor(bg).add(Paragraph(planet.sign).setFont(regularFont).setFontSize(9f)))
                    planetTable.addCell(Cell().setBackgroundColor(bg).add(Paragraph("House ${planet.house}").setFont(regularFont).setFontSize(9f)))
                    planetTable.addCell(Cell().setBackgroundColor(bg).add(Paragraph("${String.format("%.1f", planet.degree)}°").setFont(regularFont).setFontSize(9f)))
                }

                document.add(planetTable)
                document.add(Paragraph("\n"))

                // ── AI Predictions ───────────────────────────────────────────
                document.add(sectionTitle("🤖 AI-Powered Predictions", boldFont))

                if (result.careerPrediction.isNotBlank()) {
                    document.add(predictionSection("💼 Career", result.careerPrediction, boldFont, regularFont))
                    document.add(predictionSection("💍 Marriage & Relationships", result.marriagePrediction, boldFont, regularFont))
                    document.add(predictionSection("🏥 Health", result.healthPrediction, boldFont, regularFont))
                    document.add(predictionSection("💰 Finance", result.financePrediction, boldFont, regularFont))
                    document.add(predictionSection("⏰ Dasha Period", result.dashaInfo, boldFont, regularFont))
                } else {
                    document.add(predictionSection("Overview", result.aiPreview, boldFont, regularFont))
                }

                // ── Lucky Details ─────────────────────────────────────────────
                document.add(sectionTitle("🍀 Lucky Guidance", boldFont))
                val luckyTable = Table(UnitValue.createPercentArray(floatArrayOf(40f, 60f)))
                    .useAllAvailableWidth().setMarginBottom(10f)
                addTableRow(luckyTable, "Lucky Color", result.luckyColor, boldFont, regularFont)
                addTableRow(luckyTable, "Lucky Number", result.luckyNumber.toString(), boldFont, regularFont)
                addTableRow(luckyTable, "Gemstone", result.gemstone, boldFont, regularFont)
                document.add(luckyTable)
                document.add(Paragraph("\n"))

                // ── Remedies ─────────────────────────────────────────────────
                if (result.remedies.isNotBlank()) {
                    document.add(sectionTitle("🙏 Vedic Remedies", boldFont))
                    document.add(
                        Paragraph(result.remedies)
                            .setFont(regularFont).setFontSize(10f).setFontColor(darkText)
                            .setMarginBottom(10f)
                    )
                }

                // ── Disclaimer ───────────────────────────────────────────────
                document.add(Paragraph("\n"))
                val disclaimerBox = Table(UnitValue.createPercentArray(floatArrayOf(100f)))
                    .useAllAvailableWidth()
                disclaimerBox.addCell(
                    Cell().setBackgroundColor(lightGrey).setPadding(10f)
                        .add(
                            Paragraph("⚠️ Disclaimer")
                                .setFont(boldFont).setFontSize(10f)
                        )
                        .add(
                            Paragraph(result.disclaimer)
                                .setFont(italicFont).setFontSize(9f).setFontColor(darkText)
                        )
                )
                document.add(disclaimerBox)

                // ── Footer ───────────────────────────────────────────────────
                document.add(Paragraph("\n"))
                document.add(
                    Paragraph("Generated by Kundali AI • kundaliai.app • For guidance purposes only")
                        .setFont(italicFont).setFontSize(8f)
                        .setFontColor(DeviceRgb(150, 150, 150))
                        .setTextAlignment(TextAlignment.CENTER)
                )

                document.close()
                file
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun sectionTitle(title: String, boldFont: com.itextpdf.kernel.font.PdfFont): Paragraph {
        return Paragraph(title)
            .setFont(boldFont)
            .setFontSize(14f)
            .setFontColor(deepBlue)
            .setMarginTop(8f)
            .setMarginBottom(6f)
            .setBorderBottom(com.itextpdf.layout.borders.SolidBorder(gold, 1.5f))
    }

    private fun predictionSection(
        title: String,
        content: String,
        boldFont: com.itextpdf.kernel.font.PdfFont,
        regularFont: com.itextpdf.kernel.font.PdfFont
    ): Table {
        val table = Table(UnitValue.createPercentArray(floatArrayOf(100f)))
            .useAllAvailableWidth().setMarginBottom(8f)
        table.addCell(
            Cell().setBackgroundColor(lightGrey).setPadding(8f)
                .add(Paragraph(title).setFont(boldFont).setFontSize(11f).setFontColor(purple))
                .add(Paragraph(content).setFont(regularFont).setFontSize(10f).setFontColor(darkText))
        )
        return table
    }

    private fun addTableRow(
        table: Table, label: String, value: String,
        boldFont: com.itextpdf.kernel.font.PdfFont,
        regularFont: com.itextpdf.kernel.font.PdfFont
    ) {
        table.addCell(
            Cell().setPadding(6f)
                .add(Paragraph(label).setFont(boldFont).setFontSize(10f))
        )
        table.addCell(
            Cell().setPadding(6f)
                .add(Paragraph(value).setFont(regularFont).setFontSize(10f))
        )
    }
}
