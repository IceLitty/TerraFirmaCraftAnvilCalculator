package moe.icyr.tfc.anvil.calc.ui;

import lombok.Getter;
import moe.icyr.tfc.anvil.calc.resource.RecipeAnvil;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Icy
 * @since 2023/9/23
 */
public class ImageJButton extends JButton {

    @Getter
    private final List<RecipeAnvil> nowChooseRecipes;

    public ImageJButton() {
        this(null, null);
    }

    public ImageJButton(Icon icon) {
        this(null, icon);
    }

    public ImageJButton(String text) {
        this(text, null);
    }

    public ImageJButton(Action a) {
        this();
        this.setAction(a);
    }

    public ImageJButton(String text, Icon icon) {
        super(text, icon);
        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        this.setBorder(BorderFactory.createEmptyBorder());
        this.setBorderPainted(false);
        this.setOpaque(true);
        this.setContentAreaFilled(false);
        this.nowChooseRecipes = new ArrayList<>();
    }

    @Override
    public JToolTip createToolTip() {
        GameTooltip tip = new GameTooltip();
        tip.setComponent(this);
        return tip;
    }

}
