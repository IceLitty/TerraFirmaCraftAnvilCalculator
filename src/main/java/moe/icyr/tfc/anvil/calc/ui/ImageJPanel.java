package moe.icyr.tfc.anvil.calc.ui;

import javax.swing.*;
import java.awt.*;

/**
 * 窗体面板
 *
 * @author Icy
 * @since 2023/9/23
 */
public class ImageJPanel extends JPanel {

    private Image image;

    public ImageJPanel(Image image) {
        this.image = image;
        this.setLayout(null);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, this);
    }

}
