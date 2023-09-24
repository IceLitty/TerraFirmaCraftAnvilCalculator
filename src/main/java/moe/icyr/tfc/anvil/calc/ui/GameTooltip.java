package moe.icyr.tfc.anvil.calc.ui;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import moe.icyr.tfc.anvil.calc.util.ConfigUtil;
import moe.icyr.tfc.anvil.calc.util.TooltipColorUtil;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Icy
 * @since 2023/9/24
 */
@Slf4j
public class GameTooltip extends JToolTip {

    @Getter
    private List<TooltipColorUtil.TooltipColor> colorTooltips;

    public GameTooltip() {
        this.setBorder(BorderFactory.createEmptyBorder());
    }

    public GameTooltip(List<TooltipColorUtil.TooltipColor> colorTooltips) {
        this();
        this.colorTooltips = colorTooltips;
    }

    public Dimension calcTooltipSize(String tooltipText, @NonNull Graphics2D g2d) {
        if (tooltipText != null && !tooltipText.isBlank()) {
            UIDefaults uiDefs = UIManager.getLookAndFeelDefaults();
            Font font = uiDefs.getFont("ToolTip.font");
            Font derivedFont = font.deriveFont((float) font.getSize() * ConfigUtil.INSTANCE.getTooltipFontScale());
            FontMetrics metrics = g2d.getFontMetrics(derivedFont);
            int maxTextWidth = 0;
            double totalTextHeightWithMargin = 0;
            String[] split = tooltipText.split("\n");
            for (String s : split) {
                maxTextWidth = Math.max(maxTextWidth, metrics.stringWidth(s));
                double fontHeight = derivedFont.createGlyphVector(metrics.getFontRenderContext(), s).getVisualBounds().getHeight();
                totalTextHeightWithMargin += fontHeight + ConfigUtil.INSTANCE.getTooltipMargin() * ConfigUtil.INSTANCE.getTooltipScale();
            }
            int width = maxTextWidth + ConfigUtil.INSTANCE.getTooltipMargin() * ConfigUtil.INSTANCE.getTooltipScale() * 2;
            int height = (int) (ConfigUtil.INSTANCE.getTooltipMargin() * ConfigUtil.INSTANCE.getTooltipScale() + metrics.getAscent() + totalTextHeightWithMargin);
            return new Dimension(width, height);
        }
        return new Dimension();
    }

    @Override
    protected void paintComponent(Graphics g) {
        String tipText = (colorTooltips != null && !colorTooltips.isEmpty()) ? TooltipColorUtil.getPlainText(colorTooltips) : getTipText();
        if (tipText != null && !tipText.isBlank()) {
            Graphics2D g2d = (Graphics2D) g;
            UIDefaults uiDefs = UIManager.getLookAndFeelDefaults();
            Font font = uiDefs.getFont("ToolTip.font");
            Font derivedFont = font.deriveFont((float) font.getSize() * ConfigUtil.INSTANCE.getTooltipFontScale());
            FontMetrics metrics = g2d.getFontMetrics(derivedFont);
            g2d.setFont(derivedFont);
            int maxTextWidth = 0;
            double totalTextHeightWithMargin = 0;
            String[] split = tipText.split("\n");
            for (String s : split) {
                maxTextWidth = Math.max(maxTextWidth, metrics.stringWidth(s));
                double fontHeight = derivedFont.createGlyphVector(metrics.getFontRenderContext(), s).getVisualBounds().getHeight();
                totalTextHeightWithMargin += fontHeight + ConfigUtil.INSTANCE.getTooltipMargin() * ConfigUtil.INSTANCE.getTooltipScale();
            }
            int width = maxTextWidth + ConfigUtil.INSTANCE.getTooltipMargin() * ConfigUtil.INSTANCE.getTooltipScale() * 2;
            int height = (int) (ConfigUtil.INSTANCE.getTooltipMargin() * ConfigUtil.INSTANCE.getTooltipScale() + metrics.getAscent() + totalTextHeightWithMargin);
            g2d.setPaint(new Color(ConfigUtil.INSTANCE.getTooltipBackgroundColorR(), ConfigUtil.INSTANCE.getTooltipBackgroundColorG(),
                    ConfigUtil.INSTANCE.getTooltipBackgroundColorB(), ConfigUtil.INSTANCE.getTooltipBackgroundColorA()));
            g2d.fillRect(ConfigUtil.INSTANCE.getTooltipScale(), 0, width - 2 * ConfigUtil.INSTANCE.getTooltipScale(), ConfigUtil.INSTANCE.getTooltipScale());
            g2d.fillRect(width - ConfigUtil.INSTANCE.getTooltipScale(), ConfigUtil.INSTANCE.getTooltipScale(), ConfigUtil.INSTANCE.getTooltipScale(), height - 2 * ConfigUtil.INSTANCE.getTooltipScale());
            g2d.fillRect(ConfigUtil.INSTANCE.getTooltipScale(), height - ConfigUtil.INSTANCE.getTooltipScale(), width - 2 * ConfigUtil.INSTANCE.getTooltipScale(), ConfigUtil.INSTANCE.getTooltipScale());
            g2d.fillRect(0, ConfigUtil.INSTANCE.getTooltipScale(), ConfigUtil.INSTANCE.getTooltipScale(), height - 2 * ConfigUtil.INSTANCE.getTooltipScale());
            g2d.setPaint(new Color(ConfigUtil.INSTANCE.getTooltipBorderColorR(), ConfigUtil.INSTANCE.getTooltipBorderColorG(),
                    ConfigUtil.INSTANCE.getTooltipBorderColorB(), ConfigUtil.INSTANCE.getTooltipBorderColorA()));
            g2d.fillRect(ConfigUtil.INSTANCE.getTooltipScale(), ConfigUtil.INSTANCE.getTooltipScale(),
                    width - 2 * ConfigUtil.INSTANCE.getTooltipScale(), height - 2 * ConfigUtil.INSTANCE.getTooltipScale());
            g2d.setPaint(new Color(ConfigUtil.INSTANCE.getTooltipBackgroundColorR(), ConfigUtil.INSTANCE.getTooltipBackgroundColorG(),
                    ConfigUtil.INSTANCE.getTooltipBackgroundColorB(), ConfigUtil.INSTANCE.getTooltipBackgroundColorA()));
            g2d.fillRect(2 * ConfigUtil.INSTANCE.getTooltipScale(), 2 * ConfigUtil.INSTANCE.getTooltipScale(),
                    width - 4 * ConfigUtil.INSTANCE.getTooltipScale(), height - 4 * ConfigUtil.INSTANCE.getTooltipScale());
            List<TooltipColorUtil.TooltipColor> colors;
            if (colorTooltips == null || colorTooltips.isEmpty()) {
                colors = new ArrayList<>();
                for (String s : split) {
                    colors.add(new TooltipColorUtil.TooltipColor(s));
                }
            } else {
                colors = colorTooltips;
            }
            float afterX = ConfigUtil.INSTANCE.getTooltipMargin() * ConfigUtil.INSTANCE.getTooltipScale();
            float afterY = ConfigUtil.INSTANCE.getTooltipMargin() * ConfigUtil.INSTANCE.getTooltipScale() + metrics.getAscent();
            for (int i = 0; i < colors.size(); i++) {
                TooltipColorUtil.TooltipColor c = colors.get(i);
                if (c.getColor() == null) {
                    if (i == 0) {
                        g2d.setPaint(new Color(ConfigUtil.INSTANCE.getTooltipNamedTextColorR(), ConfigUtil.INSTANCE.getTooltipNamedTextColorG(),
                                ConfigUtil.INSTANCE.getTooltipNamedTextColorB(), ConfigUtil.INSTANCE.getTooltipNamedTextColorA()));
                    } else {
                        g2d.setPaint(new Color(ConfigUtil.INSTANCE.getTooltipDescTextColorR(), ConfigUtil.INSTANCE.getTooltipDescTextColorG(),
                                ConfigUtil.INSTANCE.getTooltipDescTextColorB(), ConfigUtil.INSTANCE.getTooltipDescTextColorA()));
                    }
                } else {
                    g2d.setPaint(new Color(c.getColor().getRed(), c.getColor().getGreen(),
                            c.getColor().getBlue(), c.getColor().getAlpha()));
                }
                g2d.drawString(c.getText(), afterX,  afterY);
                if (i < colors.size() - 1 && "\n".equals(colors.get(i + 1).getText())) {
                    double thisLineHeight = derivedFont.createGlyphVector(metrics.getFontRenderContext(), c.getText()).getVisualBounds().getHeight();
                    afterX = ConfigUtil.INSTANCE.getTooltipMargin() * ConfigUtil.INSTANCE.getTooltipScale();
                    afterY += thisLineHeight + ConfigUtil.INSTANCE.getTooltipMargin() * ConfigUtil.INSTANCE.getTooltipScale();
                    i++;
                } else {
                    int thisLineWidth = metrics.stringWidth(c.getText());
                    afterX += thisLineWidth;
                }
            }
        }
    }

}
