package com.example.vmoov;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PdfGenerator {

    public static File createPdf(Context context, String fileName, String[] lines) {
        Document document = new Document(PageSize.A4);
        File pdfFile = null;

        try {
            // 📂 Crear carpeta de reportes en Descargas
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File reportsDir = new File(downloadsDir, "VMOOV Reports");
            if (!reportsDir.exists()) {
                reportsDir.mkdirs();
            }

            // 📄 Guardar el archivo en la carpeta de reports
            pdfFile = new File(reportsDir, fileName);
            PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();

            // 🎨 Cargar la fuente Verdana desde assets
            BaseFont verdanaFont = BaseFont.createFont("assets/fonts/verd.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font titleFont = new Font(verdanaFont, 22, Font.BOLD, new BaseColor(225, 171, 252)); // Color violeta
            Font subtitleFont = new Font(verdanaFont, 16, Font.BOLD, new BaseColor(225, 171, 252));
            Font regularFont = new Font(verdanaFont, 14, Font.NORMAL, BaseColor.BLACK);

            // 📌 Agregar Logo desde `res/drawable`
            try {
                Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.logovmoov_v);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                Image logo = Image.getInstance(stream.toByteArray());
                logo.scaleToFit(120, 120);
                logo.setAlignment(Element.ALIGN_CENTER);
                document.add(new Paragraph(" ", new Font(verdanaFont, 12, Font.NORMAL))); // Espaciado
                document.add(logo);
                document.add(new Paragraph(" ", new Font(verdanaFont, 12, Font.NORMAL))); // Espaciado extra
            } catch (Exception e) {
                System.err.println("⚠️ No se pudo cargar el logo desde drawable: " + e.getMessage());
            }

            // 🔹 Separador con espacio extra
            LineSeparator separator = new LineSeparator();
            separator.setLineColor(new BaseColor(225, 171, 252));
            document.add(new Chunk(separator));
            document.add(new Paragraph(" ", new Font(verdanaFont, 10, Font.NORMAL))); // Espaciado extra

            // 📌 Agregar Título
            Paragraph title = new Paragraph("Reporte de Sesión", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // 📅 Fecha de la última sesión
            Paragraph fecha = new Paragraph(lines[1], regularFont);
            fecha.setAlignment(Element.ALIGN_CENTER);
            fecha.setSpacingAfter(15);
            document.add(fecha);

            // 🔹 Otro separador
            document.add(new Chunk(separator));
            document.add(new Paragraph(" ", new Font(verdanaFont, 10, Font.NORMAL))); // Espaciado extra

            // 📝 Crear una tabla de **dos columnas**
            PdfPTable table = new PdfPTable(2); // 2 columnas
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);
            table.setWidths(new float[]{3, 3}); // Ajustar proporción de columnas

            // 🔢 Agregar datos en dos columnas
            for (int i = 2; i < lines.length; i++) {
                String[] splitLine = lines[i].split(":");
                if (splitLine.length == 2) {
                    // Celda del parámetro
                    PdfPCell paramCell = new PdfPCell(new Phrase(splitLine[0] + ":", subtitleFont));
                    paramCell.setBorder(Rectangle.NO_BORDER);
                    paramCell.setPaddingBottom(15);
                    paramCell.setPaddingTop(15);
                    table.addCell(paramCell);

                    // Celda del valor
                    PdfPCell valueCell = new PdfPCell(new Phrase(splitLine[1], regularFont));
                    valueCell.setBorder(Rectangle.NO_BORDER);
                    valueCell.setPaddingBottom(15);
                    valueCell.setPaddingTop(15);
                    table.addCell(valueCell);
                }
            }

            document.add(table);

            // 🔹 Separador con espacio extra
            LineSeparator separator2 = new LineSeparator();
            separator.setLineColor(new BaseColor(225, 171, 252));
            document.add(new Chunk(separator));
            document.add(new Paragraph(" ", new Font(verdanaFont, 10, Font.NORMAL))); // Espaciado extra


            // ✅ Cierre del reporte
            Paragraph cierre = new Paragraph("\nVMOOV\nBienestar, Salud y Diversión.", subtitleFont);
            cierre.setAlignment(Element.ALIGN_CENTER);
            cierre.setSpacingBefore(20);
            document.add(cierre);

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
        return pdfFile;
    }
}
