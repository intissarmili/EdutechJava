package controller;

import javafx.fxml.FXML;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.IOException;

public class PDFViewerController {

    public static void createCertificationPDF(String certifName, String certifDescription, String imagePath) {
        // Crée un document PDF
        Document document = new Document();
        try {
            // Spécifie le fichier de sortie du PDF
            PdfWriter.getInstance(document, new FileOutputStream("Certification_" + certifName + ".pdf"));
            document.open(); // Ouvre le document pour l'écriture

            // Titre du document
            document.add(new Paragraph("Certification: " + certifName));

            // Description
            document.add(new Paragraph("Description: " + certifDescription));

            // Ajouter l'image
            Image image = Image.getInstance(imagePath); // Chargement de l'image
            image.scaleToFit(250, 250); // Redimensionner l'image si nécessaire
            document.add(image);

            // Fermer le document
            document.close();

            System.out.println("PDF généré avec succès!");

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        }
    }
}
