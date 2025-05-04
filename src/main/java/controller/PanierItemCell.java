package controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import services.Certification;

public class PanierItemCell extends ListCell<Certification> {
    @Override
    protected void updateItem(Certification certif, boolean empty) {
        super.updateItem(certif, empty);
        if (empty || certif == null) {
            setGraphic(null);
        } else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/pidev1/views/PanierItem.fxml"));
                HBox hbox = loader.load();
                PanierItemController controller = loader.getController();
                controller.setData(certif);
                setGraphic(hbox);
            } catch (Exception e) {
                e.printStackTrace();
                setText("Erreur de chargement !");
            }
        }
    }
}
