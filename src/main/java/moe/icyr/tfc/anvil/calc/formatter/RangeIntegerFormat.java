package moe.icyr.tfc.anvil.calc.formatter;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.text.*;

/**
 * 范围整数格式器
 *
 * @author Icy
 * @since 2023/9/23
 */
@Slf4j
public class RangeIntegerFormat extends DecimalFormat {

    private final Long min, max, defaultVal;

    public RangeIntegerFormat(@NonNull Long min, @NonNull Long max, Long defaultVal) {
        if (min > max) {
            this.max = min;
            this.min = max;
        } else {
            this.min = min;
            this.max = max;
        }
        this.defaultVal = defaultVal;
        this.setParseIntegerOnly(true);
        this.setDecimalSeparatorAlwaysShown(false);
        this.setGroupingUsed(false);
    }

    public static RangeIntegerFormat getInstance(@NonNull Long min, @NonNull Long max, Long defaultVal) {
        return new RangeIntegerFormat(min, max, defaultVal);
    }

    public static RangeIntegerFormat getInstance(@NonNull Integer min, @NonNull Integer max, Integer defaultVal) {
        return new RangeIntegerFormat(Long.parseLong(String.valueOf(min)), Long.parseLong(String.valueOf(max)), defaultVal == null ? null : Long.parseLong(String.valueOf(defaultVal)));
    }

    @Override
    public Number parse(String text, ParsePosition pos) {
        Number number = super.parse(text, pos);
        if (number == null) {
            return defaultVal;
        }
        if (number.longValue() > max) {
            return max;
        } else if (number.longValue() < min) {
            return min;
        } else {
            return number;
        }
    }

}
