package moe.icyr.tfc.anvil.calc;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

@Deprecated
public class HelloController {

    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

}