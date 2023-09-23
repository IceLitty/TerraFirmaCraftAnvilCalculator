package moe.icyr.tfc.anvil.calc.ui;

import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;

/**
 * @author Icy
 * @since 2023/9/24
 */
@Slf4j
public class GameTooltip extends JToolTip {

    @Override
    protected void paintComponent(Graphics g) {
        String tipText = getTipText();
        if (tipText != null && !tipText.isBlank()) {
            Graphics2D g2d = (Graphics2D) g;
            UIDefaults uiDefs = UIManager.getLookAndFeelDefaults();
            Font font = uiDefs.getFont("ToolTip.font");
            log.warn("font size " + font.getSize());
            FontMetrics metrics = g2d.getFontMetrics(font);
            int stringWidth = 0;
            for (String s : tipText.split("\n")) {
                stringWidth = Math.max(stringWidth, metrics.stringWidth(s));
            }
            int stringHeight = metrics.getHeight() * tipText.split("\n").length;
            log.warn(tipText + " : " + stringWidth + " str hei " + stringHeight);
        }
        // TODO margin background color border etc...
        super.paintComponent(g);
    }

}
