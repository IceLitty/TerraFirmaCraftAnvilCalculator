package moe.icyr.tfc.anvil.calc;

import javafx.stage.Stage;
import moe.icyr.tfc.anvil.calc.ui.MainFrame;

import java.io.IOException;

public class Application extends javafx.application.Application {

    MainFrame mainFrame;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        mainFrame = new MainFrame();
//        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
//        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
//        stage.setTitle("Hello!");
//        stage.setScene(scene);
//        stage.show();
    }

}