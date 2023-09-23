package moe.icyr.tfc.anvil.calc.formatter;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

/**
 * MC地图种子格式器
 *
 * @deprecated 似乎不需要，mc的/seed返回的是纯数字，含负数，包含符号位20长度
 *
 * @author Icy
 * @since 2023/9/23
 */
@Slf4j
@Deprecated
public class SeedTextFormat extends Format {

    public static SeedTextFormat getInstance() {
        return new SeedTextFormat();
    }

    @Override
    public StringBuffer format(Object obj, @NonNull StringBuffer toAppendTo, @NonNull FieldPosition pos) {
        if (obj == null || String.valueOf(obj).trim().length() == 0) {
            return null;
        }
        String objStr = String.valueOf(obj);
        objStr = objStr.trim().replaceAll("[^0-9a-fA-F]", "");
        if (objStr.length() > 32) {
            objStr = objStr.substring(0, 32);
        }
//        objStr = objStr.toUpperCase();
        return new StringBuffer(objStr);
    }

    @Override
    public Object parseObject(String source, @NonNull ParsePosition pos) {
        log.info("{} {}", source, pos);
        return source;
    }

}
