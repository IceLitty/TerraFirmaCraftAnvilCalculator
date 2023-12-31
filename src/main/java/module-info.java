module moe.icyr.tfc.anvil.calc {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires org.tomlj;
    requires org.slf4j;
    requires static lombok;
    requires ch.qos.logback.core;

    opens moe.icyr.tfc.anvil.calc to javafx.fxml;
    exports moe.icyr.tfc.anvil.calc;
    exports moe.icyr.tfc.anvil.calc.entity;
    exports moe.icyr.tfc.anvil.calc.resource;
    exports moe.icyr.tfc.anvil.calc.ui;
    exports moe.icyr.tfc.anvil.calc.util;
    exports moe.icyr.tfc.anvil.calc.formatter;
}