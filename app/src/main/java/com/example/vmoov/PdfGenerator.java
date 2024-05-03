package com.example.vmoov;

import android.os.Environment;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import java.util.List;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PdfGenerator {

    public static void createPdf(String[] lines) {
        Document document = new Document();

        try {
            // Obtener la ruta del directorio de descargas
            File pdfFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "EPICareReport.pdf");

            // Crear el archivo PDF
            PdfWriter.getInstance(document, new FileOutputStream(pdfFile));

            // Abrir el documento
            document.open();

            // Personalizar el estilo del documento
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 28, BaseColor.BLACK);
            Font regularFont = FontFactory.getFont(FontFactory.HELVETICA, 18, BaseColor.BLACK);

            // Agregar líneas de texto al PDF con estilos personalizados
            for (String line : lines) {
                // Utilizar estilos diferentes según la línea
                if (line.startsWith("EPICare")) {
                    Paragraph title = new Paragraph(line, titleFont);
                    title.setAlignment(Element.ALIGN_CENTER);
                    document.add(title);
                } else {
                    Paragraph regularLine = new Paragraph(line, regularFont);
                    document.add(regularLine);
                }
            }

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        } finally {
            // Cerrar el documento después de agregar el contenido
            if (document.isOpen()) {
                document.close();
            }
        }
    }

}
