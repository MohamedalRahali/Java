package utils;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import models.Reclamation;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class PDFGenerator {
    private static final String PDF_DIRECTORY = "reclamations_pdf/";

    public static String generateReclamationPDF(Reclamation reclamation, String filePath) throws IOException {
        // Créer le document PDF
        PdfWriter writer = new PdfWriter(filePath);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Ajouter le titre
        Paragraph title = new Paragraph("RÉCLAMATION N°" + reclamation.getId())
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(20)
                .setBold();
        document.add(title);

        // Ajouter un espace
        document.add(new Paragraph("\n"));

        // Créer un tableau pour les informations
        Table table = new Table(2);
        table.setWidth(UnitValue.createPercentValue(100));

        // Ajouter les informations de la réclamation
        table.addCell("Titre");
        table.addCell(reclamation.getTitle());

        table.addCell("Description");
        table.addCell(reclamation.getDescription());

        table.addCell("Type");
        table.addCell(reclamation.getTypeName());

        if (reclamation.getCreatedAt() != null) {
            table.addCell("Date");
            table.addCell(reclamation.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }

        document.add(table);

        // Fermer le document
        document.close();

        return filePath;
    }
} 