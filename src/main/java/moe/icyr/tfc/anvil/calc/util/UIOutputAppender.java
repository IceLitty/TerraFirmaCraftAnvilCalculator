package moe.icyr.tfc.anvil.calc.util;

import ch.qos.logback.core.OutputStreamAppender;
import moe.icyr.tfc.anvil.calc.Application;

import java.io.OutputStream;

/**
 * @deprecated 研究下来TextPane还是最好用于组件拼接，或者直接setText后再insertImage，不能用作日志流
 *
 * @author Icy
 * @since 2023/9/23
 */
@Deprecated
public class UIOutputAppender<E> extends OutputStreamAppender<E> {

    @Override
    public void start() {
        Application.uiOutputAppender = this;
    }

    public void initOutputStream(OutputStream outputStream) {
        setOutputStream(outputStream);
        super.start();
    }

}
