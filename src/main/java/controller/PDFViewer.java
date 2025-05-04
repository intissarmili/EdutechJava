package controller;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

public class PDFViewer {

    public static void openPDF(String filePath) {
        try {
            File file = new File(filePath);
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (file.exists()) {
                    desktop.open(file); // Ouvre le PDF avec l'application par défaut
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
