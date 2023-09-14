package moe.icyr.tfc.anvil.calc.util;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * @author Icy
 * @since 2023/9/13
 */
public class MessageUtil {

    private static final ResourceBundle messageBundle = ResourceBundle.getBundle("moe/icyr/tfc/anvil/calc/message");

    /**
     * 获取提示文本
     *
     * @param bundleKey key
     * @return 原生提示文本
     */
    public static String getBundleString(String bundleKey) {
        return messageBundle.getString(bundleKey);
    }

    /**
     * 获取格式化后的提示文本
     *
     * @param bundleKey key
     * @param params    文本参数
     * @return 提示文本
     */
    public static String getMessage(String bundleKey, Object... params) {
        return MessageFormat.format(messageBundle.getString(bundleKey), params);
    }

}
