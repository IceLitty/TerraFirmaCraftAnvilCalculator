package moe.icyr.tfc.anvil.calc.ui;

import lombok.extern.slf4j.Slf4j;
import moe.icyr.tfc.anvil.calc.AssetsLoader;
import moe.icyr.tfc.anvil.calc.util.ConfigUtil;
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
        // 加载配置文件
        log.info("Config " + ConfigUtil.INSTANCE + " loaded.");
        // 加载MOD资源包材质包
        new AssetsLoader().load();
        // TODO 根据TFC铁砧UI加载工具UI界面，或者修改UI图改为内置
        // TODO 加载type=tfc:anvil的recipe到铁砧配方对象（方便UI调用及后续按钮交互）
        //  加载全部的input配方到UI的输入格点开的面板中，实现一个输入的配方及一个输入后筛选第二输入的物品列表
        //  并且加载对应的物品材质创建配方按钮等UI元素
        //  实现输入选择后自动带出输出或多个输出配方的情况下筛选输出选择面板
        //  并且支持不选择输入的情况下显示全部的配方输出，反过来自动加载输入（也需要选择，不进行选择就获取不到配方ID，就没法自动算出实例seed及target值）
        //  支持跳过配方选择，直接输入target值和rules进行计算
        //  支持右键移除选中的配方物品、rules
        //  面板添加地图seed输入框，默认0，必须数字，添加target，默认0，必须数字
        // 初始化UI
        this.setTitle(MessageUtil.getMessage("ui.title"));
        this.setSize(100, 100); // TODO 等UI背景搞好后设成固定值
//        this.setResizable(false); // TODO 需要不可调整大小吗？对于高分屏是否不太友好？
//        this.setLocation(); // TODO 搞个屏幕居中？
        this.setVisible(true);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

}
