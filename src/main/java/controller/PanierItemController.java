package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import services.Certification;

public class PanierItemController {

    @FXML
    private Label itemLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private Label prixLabel;
    @FXML
    private ImageView imgView;
    @FXML
    private HBox hbox;

    public void setData(Certification certif) {
        itemLabel.setText(certif.getNom());
        descriptionLabel.setText(certif.getDescription());
        prixLabel.setText(certif.getPrix() + " DT");
        imgView.setImage(new Image(getClass().getResourceAsStream(certif.getImg())));
    }
}
