package moe.icyr.tfc.anvil.calc.formatter;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

/**
 * 范围整数格式器（字符串类型）
 *
 * @deprecated 还没实现好，有bug
 *
 * @author Icy
 * @since 2023/9/23
 */
@Slf4j
@Deprecated
public class RangeStringFormat extends Format {

    private final String min, max;

    public RangeStringFormat(@NonNull String min, @NonNull String max) {
        if ((min.startsWith("-") ? min.replaceAll("\\D", "").length() + 1 : min.replaceAll("\\D", "").length()) != min.length()) {
            throw new IllegalArgumentException("Min is not an Integer");
        }
        if ((max.startsWith("-") ? max.replaceAll("\\D", "").length() + 1 : max.replaceAll("\\D", "").length()) != max.length()) {
            throw new IllegalArgumentException("Max is not an Integer");
        }
        while (true) {
            if (min.startsWith("00")) {
                min = min.substring(1);
            } else if (min.startsWith("-00")) {
                min = "-" + min.substring(2);
            } else {
                break;
            }
        }
        while (true) {
            if (max.startsWith("00")) {
                max = max.substring(1);
            } else if (max.startsWith("-00")) {
                max = "-" + max.substring(2);
            } else {
                break;
            }
        }
        this.min = min.startsWith("-") && !"-0".equals(min) ? "-" + min.replaceAll("\\D", "") : min.replaceAll("\\D", "");
        this.max = max.startsWith("-") && !"-0".equals(max) ? "-" + max.replaceAll("\\D", "") : max.replaceAll("\\D", "");
    }

    public static RangeStringFormat getInstance(String min, String max) {
        return new RangeStringFormat(min, max);
    }

    @Override
    public StringBuffer format(Object obj, @NonNull StringBuffer toAppendTo, @NonNull FieldPosition pos) {
        if (obj == null || String.valueOf(obj).trim().length() == 0) {
            return null;
        }
        String objStr = String.valueOf(obj);
        boolean negative = objStr.startsWith("-");
        objStr = objStr.replaceAll("\\D", "");
        StringBuffer dest = new StringBuffer();
        if (negative) {
            if (!"0".equals(objStr)) {
                objStr = "-" + objStr;
            }
            // dest is negative
            boolean needCheckMax = true;
            if (!min.startsWith("-") || min.length() < objStr.length()) {
                dest.append(min);
                needCheckMax = false;
            } else if (min.length() == objStr.length()) {
                char[] srcC = min.toCharArray();
                char[] destC = objStr.toCharArray();
                for (int i = 1; i < srcC.length; i++) {
                     if (Integer.parseInt(String.valueOf(destC[i])) < Integer.parseInt(String.valueOf(srcC[i]))) {
                        dest.append(objStr);
                        needCheckMax = false;
                        break;
                    }
                }
            }
            if (needCheckMax) {
                if (!max.startsWith("-") || max.length() < objStr.length()) {
                    dest.append(objStr);
                } else if (max.length() > objStr.length()) {
                    dest.append(max);
                } else {
                    boolean useDestValue = true;
                    char[] srcC = max.toCharArray();
                    char[] destC = objStr.toCharArray();
                    for (int i = 1; i < srcC.length; i++) {
                        if (Integer.parseInt(String.valueOf(srcC[i])) < Integer.parseInt(String.valueOf(destC[i]))) {
                            dest.append(max);
                            useDestValue = false;
                            break;
                        }
                    }
                    if (useDestValue) {
                        dest.append(objStr);
                    }
                }
            }
        } else {
            // dest is positive
            if (max.startsWith("-") || max.length() < objStr.length()) {
                dest.append(max);
            } else {
                boolean needCheckMin = true;
                if (max.length() == objStr.length()) {
                    char[] srcC = max.toCharArray();
                    char[] destC = objStr.toCharArray();
                    for (int i = 0; i < srcC.length; i++) {
                        if (Integer.parseInt(String.valueOf(destC[i])) < Integer.parseInt(String.valueOf(srcC[i]))) {
                            dest.append(objStr);
                            needCheckMin = false;
                            break;
                        }
                    }
                }
                if (needCheckMin) {
                    if (min.startsWith("-") || min.length() < objStr.length()) {
                        dest.append(objStr);
                    } else if (min.length() > objStr.length()) {
                        dest.append(min);
                    } else {
                        boolean useDestValue = true;
                        char[] srcC = min.toCharArray();
                        char[] destC = objStr.toCharArray();
                        for (int i = 0; i < srcC.length; i++) {
                            if (Integer.parseInt(String.valueOf(srcC[i])) > Integer.parseInt(String.valueOf(destC[i]))) {
                                dest.append(min);
                                useDestValue = false;
                                break;
                            }
                        }
                        if (useDestValue) {
                            dest.append(objStr);
                        }
                    }
                }
            }
        }
        return dest;
    }

    @Override
    public Object parseObject(String source, @NonNull ParsePosition pos) {
        log.info("{} {}", source, pos);
        return source;
    }

}
