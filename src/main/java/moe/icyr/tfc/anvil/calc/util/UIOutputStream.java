package moe.icyr.tfc.anvil.calc.util;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Icy
 * @since 2023/9/23
 */
@Deprecated
public class UIOutputStream extends OutputStream {

    private final ByteArrayOutputStream baos;
    private final List<BufferedImage> images;
    private final JTextPane textPane;

    public UIOutputStream(JTextPane textPane) {
        this.baos = new ByteArrayOutputStream();
        this.images = new ArrayList<>();
        this.textPane = textPane;
    }

    public void insertImage(BufferedImage img) throws IOException {
        this.images.add(img);
        this.baos.write("[IMG_PLACEHOLDER]".getBytes());
    }

    @Override
    public void write(int b) throws IOException {
        baos.write(b);
        int i = textPane.getSelectionEnd();
        double position = 1;
        if (i > 0) {
            position = (double) i / textPane.getText().length();
        }
        String fullLogString = baos.toString(StandardCharsets.UTF_8);
        String[] fullLogStr = fullLogString.split("\\[IMG_PLACEHOLDER]");
        for (int id = 0; id < fullLogStr.length; id++) {
            textPane.insertComponent(new JLabel(fullLogStr[id]));
            if (images.size() > id) {
                textPane.insertIcon(new ImageIcon(images.get(id)));
            }
        }
        int ii = (int) (position * textPane.getText().length());
        textPane.setSelectionEnd(ii);
    }

}
