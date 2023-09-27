package moe.icyr.tfc.anvil.calc.ui;

import lombok.Getter;
import moe.icyr.tfc.anvil.calc.util.TooltipColorUtil;

import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;
import java.awt.*;
import java.text.Format;
import java.util.List;

/**
 * @author Icy
 * @since 2023/9/27
 */
public class TooltipJFormattedTextField extends JFormattedTextField {

    @Getter
    private List<TooltipColorUtil.TooltipColor> colorTooltips;

    public TooltipJFormattedTextField() {
    }

    public TooltipJFormattedTextField(Object value) {
        this();
        setValue(value);
    }

    public TooltipJFormattedTextField(Format format) {
        super(format);
    }

    public TooltipJFormattedTextField(AbstractFormatter formatter) {
        this(new DefaultFormatterFactory(formatter));
    }

    public TooltipJFormattedTextField(AbstractFormatterFactory factory) {
        this();
        setFormatterFactory(factory);
    }

    public TooltipJFormattedTextField(AbstractFormatterFactory factory, Object currentValue) {
        this(currentValue);
        setFormatterFactory(factory);
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
