module moe.icyr.tfc.anvil.calc {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires org.tomlj;
    requires org.slf4j;
    requires static lombok;

    opens moe.icyr.tfc.anvil.calc to javafx.fxml;
    exports moe.icyr.tfc.anvil.calc;
}