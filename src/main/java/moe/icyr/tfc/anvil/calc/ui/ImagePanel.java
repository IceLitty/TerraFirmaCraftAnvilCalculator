package moe.icyr.tfc.anvil.calc.ui;

import javax.swing.*;
import java.awt.*;

/**
 * @author Icy
 * @since 2023/9/23
 */
public class ImagePanel extends JPanel {

    private Image image;

    public ImagePanel(Image image) {
        this.image = image;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, this);
    }

}
