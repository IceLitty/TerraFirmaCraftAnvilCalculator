package moe.icyr.tfc.anvil.calc.util;

import java.awt.*;

/**
 * @author Icy
 * @since 2023/9/24
 */
public class ColorPresent {

    private static Color tooltipItemName;
    private static Color tooltipItemDesc;
    private static Color tooltipModId;

    public static Color getTooltipItemName() {
        if (tooltipItemName == null) {
            tooltipItemName = new Color(ConfigUtil.INSTANCE.getTooltipNamedTextColorR(), ConfigUtil.INSTANCE.getTooltipNamedTextColorG(),
                    ConfigUtil.INSTANCE.getTooltipNamedTextColorB(), ConfigUtil.INSTANCE.getTooltipNamedTextColorA());
        }
        return tooltipItemName;
    }

    public static Color getTooltipItemDesc() {
        if (tooltipItemDesc == null) {
            tooltipItemDesc = new Color(ConfigUtil.INSTANCE.getTooltipDescTextColorR(), ConfigUtil.INSTANCE.getTooltipDescTextColorG(),
                    ConfigUtil.INSTANCE.getTooltipDescTextColorB(), ConfigUtil.INSTANCE.getTooltipDescTextColorA());
        }
        return tooltipItemDesc;
    }

    public static Color getTooltipModId() {
        if (tooltipModId == null) {
            tooltipModId = new Color(ConfigUtil.INSTANCE.getTooltipModIdColorR(), ConfigUtil.INSTANCE.getTooltipModIdColorG(),
                    ConfigUtil.INSTANCE.getTooltipModIdColorB(), ConfigUtil.INSTANCE.getTooltipModIdColorA());
        }
        return tooltipModId;
    }

}
