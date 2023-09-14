package moe.icyr.tfc.anvil.calc.ui;

import lombok.extern.slf4j.Slf4j;
import moe.icyr.tfc.anvil.calc.AssetsLoader;
import moe.icyr.tfc.anvil.calc.util.JarUtil;
import moe.icyr.tfc.anvil.calc.util.MessageUtil;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Icy
 * @since 2023/9/13
 */
@Slf4j
public class MainFrame extends JFrame {

    public MainFrame() throws HeadlessException {
        // 加载MOD资源包
        new AssetsLoader().load();
        // 初始化UI
        this.setTitle(MessageUtil.getMessage("ui.title"));
//        this.setSize();
//        this.setLocation();
//        this.setVisible(true);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

}
