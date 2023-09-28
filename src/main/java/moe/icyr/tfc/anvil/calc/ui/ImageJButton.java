package moe.icyr.tfc.anvil.calc.ui;

import lombok.Getter;
import moe.icyr.tfc.anvil.calc.resource.RecipeAnvil;
import moe.icyr.tfc.anvil.calc.util.TooltipColorUtil;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

/**
 * 图标按钮组件
 *
 * @author Icy
 * @since 2023/9/23
 */
public class ImageJButton extends JButton {

    /**
     * 用于获取按钮绑定的合成配方，目前仅使用0号下标
     */
    @Getter
    private final List<RecipeAnvil> nowChooseRecipes;
    @Getter
    private List<TooltipColorUtil.TooltipColor> colorTooltips;

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

    public void setColorTooltips(List<TooltipColorUtil.TooltipColor> colorTooltips) {
        this.colorTooltips = colorTooltips;
        this.setToolTipText(TooltipColorUtil.getPlainText(colorTooltips));
    }

    @Override
    public JToolTip createToolTip() {
        String toolTipText = this.getToolTipText();
        GameTooltip tip = new GameTooltip(this.getColorTooltips());
        tip.setPreferredSize(tip.calcTooltipSize(toolTipText, (Graphics2D) this.getGraphics()));
        tip.setComponent(this);
        return tip;
    }

}
