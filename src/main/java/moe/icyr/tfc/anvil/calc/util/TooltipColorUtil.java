package moe.icyr.tfc.anvil.calc.util;

import lombok.Data;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Icy
 * @since 2023/9/24
 */
public class TooltipColorUtil {

    @Data
    public static class TooltipColor {
        private String text;
        private Color color;
        private Boolean bold;
        private Boolean italic;
        public TooltipColor() {
            this(null);
        }
        public TooltipColor(String text) {
            this(text, null);
        }
        public TooltipColor(String text, Color color) {
            this(text, color, false, false);
        }
        public TooltipColor(String text, Color color, boolean bold, boolean italic) {
            this.text = text;
            this.color = color;
            this.bold = bold;
            this.italic = italic;
        }
    }

    public static TooltipColorUtil.Builder builder() {
        return new TooltipColorUtil.Builder();
    }

    public static String getPlainText(List<TooltipColor> colors) {
        if (colors == null) return null;
        StringBuilder builder = new StringBuilder();
        for (TooltipColor c : colors) {
            builder.append(c.getText());
        }
        return builder.toString();
    }

    public static class Builder {
        private final List<TooltipColor> color;
        public Builder() {
            this.color = new ArrayList<>();
        }
        public TooltipColorUtil.Builder withText(String text) {
            return withText(text, null);
//            if (text == null || text.length() == 0) {
//                return this;
//            }
//            if (text.contains("\n")) {
//                String[] split = text.split("\n");
//                for (int i = 0; i < split.length; i++) {
//                    String s = split[i];
//                    if (s.length() != 0) {
//                        this.color.add(new TooltipColor(s));
//                    }
//                    if (i != split.length - 1 || text.endsWith("\n")) {
//                        this.color.add(new TooltipColor("\n"));
//                    }
//                }
//            } else {
//                this.color.add(new TooltipColor(text));
//            }
//            return this;
        }
        public TooltipColorUtil.Builder withText(String text, Color color) {
            return withText(text, color, false, false);
//            if (text == null || text.length() == 0) {
//                return this;
//            }
//            if (text.contains("\n")) {
//                String[] split = text.split("\n");
//                for (int i = 0; i < split.length; i++) {
//                    String s = split[i];
//                    if (s.length() != 0) {
//                        this.color.add(new TooltipColor(s, color));
//                    }
//                    if (i != split.length - 1 || text.endsWith("\n")) {
//                        this.color.add(new TooltipColor("\n"));
//                    }
//                }
//            } else {
//                this.color.add(new TooltipColor(text, color));
//            }
//            return this;
        }
        public TooltipColorUtil.Builder withText(String text, Color color, boolean bold, boolean italic) {
            if (text == null || text.length() == 0) {
                return this;
            }
            if (text.contains("\n")) {
                String[] split = text.split("\n");
                for (int i = 0; i < split.length; i++) {
                    String s = split[i];
                    if (s.length() != 0) {
                        this.color.add(new TooltipColor(s, color, bold, italic));
                    }
                    if (i != split.length - 1 || text.endsWith("\n")) {
                        this.color.add(new TooltipColor("\n"));
                    }
                }
            } else {
                this.color.add(new TooltipColor(text, color, bold, italic));
            }
            return this;
        }
        public TooltipColorUtil.Builder withNewLine() {
            this.color.add(new TooltipColor("\n"));
            return this;
        }
        public List<TooltipColor> build() {
            return this.color;
        }
    }

}
