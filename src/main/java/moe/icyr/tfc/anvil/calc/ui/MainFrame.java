package moe.icyr.tfc.anvil.calc.ui;

import lombok.extern.slf4j.Slf4j;
import moe.icyr.tfc.anvil.calc.Application;
import moe.icyr.tfc.anvil.calc.AssetsLoader;
import moe.icyr.tfc.anvil.calc.formatter.RangeIntegerFormat;
import moe.icyr.tfc.anvil.calc.formatter.SeedTextFormat;
import moe.icyr.tfc.anvil.calc.resource.ResourceLocation;
import moe.icyr.tfc.anvil.calc.resource.ResourceManager;
import moe.icyr.tfc.anvil.calc.resource.Texture;
import moe.icyr.tfc.anvil.calc.util.ConfigUtil;
import moe.icyr.tfc.anvil.calc.util.MessageUtil;
import moe.icyr.tfc.anvil.calc.util.UIOutputStream;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

/**
 * @author Icy
 * @since 2023/9/13
 */
@Slf4j
public class MainFrame extends JFrame {

    public MainFrame() throws HeadlessException {
        // 加载配置文件
        log.debug("Config " + ConfigUtil.INSTANCE + " loaded.");
        AssetsLoader assetsLoader = new AssetsLoader();
        // 加载MOD资源包材质包
        assetsLoader.loadMods();
        // [x]还是加载合成配方UI时一并加载进按钮对象中 _TODO 还是得先加载铁砧配方再初始化UI
        // 动态处理素材-加载窗体UI
        this.loadAnvilUI();
        // TODO 根据TFC铁砧UI加载工具UI界面，或者修改UI图改为内置
        // TODO 加载type=tfc:anvil的recipe到铁砧配方对象（方便UI调用及后续按钮交互）
        //  加载全部的input配方到UI的输入格点开的面板中，实现一个输入的配方及一个输入后筛选第二输入的物品列表
        //  并且加载对应的物品材质创建配方按钮等UI元素
        //  实现输入选择后自动带出输出或多个输出配方的情况下筛选输出选择面板
        //  并且支持不选择输入的情况下显示全部的配方输出，反过来自动加载输入（也需要选择，不进行选择就获取不到配方ID，就没法自动算出实例seed及target值）
        //  支持跳过配方选择，直接输入target值和rules进行计算
        //  支持右键移除选中的配方物品、rules
        //  面板添加地图seed输入框，默认0，必须数字，添加target，默认0，必须数字
        //  定义一个新的STDOUT，配置logback将日志通过该流输出至界面上
        // 初始化UI
        this.setTitle(MessageUtil.getMessage("ui.title"));
        this.setSize(ConfigUtil.INSTANCE.getAnvilAssetUIWidth() * ConfigUtil.INSTANCE.getScaleUI() + ConfigUtil.INSTANCE.getMainFrameWidthOffset(),
                ConfigUtil.INSTANCE.getAnvilAssetUIHeight() * ConfigUtil.INSTANCE.getScaleUI() + ConfigUtil.INSTANCE.getMainFrameHeightOffset());
        this.setResizable(false);
        this.setLocationRelativeTo(null); // 屏幕居中
        this.setVisible(true);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    /**
     * 加载TFC铁砧UI
     */
    public void loadAnvilUI() {
        List<ResourceLocation> anvilRs = ResourceManager.getResources("tfc", r -> r instanceof Texture rr && "gui".equals(rr.getTextureType()) && "anvil".equals(r.getPath()));
        if (anvilRs.isEmpty()) {
            log.error("Not load tfc:anvil texture resource!");
            return;
        }
        BufferedImage asset = ((Texture) anvilRs.get(0)).getImg();
        BufferedImage anvil = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIX(), ConfigUtil.INSTANCE.getAnvilAssetUIY(),
                ConfigUtil.INSTANCE.getAnvilAssetUIWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIHeight());
        Graphics g = anvil.getGraphics();
        // 将背包涂为文本区
        BufferedImage anvilBackpackImg = new BufferedImage(ConfigUtil.INSTANCE.getAnvilAssetUIBackpackWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIBackpackHeight(), BufferedImage.TYPE_INT_ARGB);
        drawSlotUI(anvilBackpackImg);
        g.drawImage(anvilBackpackImg, ConfigUtil.INSTANCE.getAnvilAssetUIBackpackX(), ConfigUtil.INSTANCE.getAnvilAssetUIBackpackY(), null);
        // 将技术图标绘制在技术操作按钮上
        AffineTransform iconTechTransform = new AffineTransform();
        iconTechTransform.setToScale(0.5, 0.5);
        AffineTransformOp iconTechTransformOp = new AffineTransformOp(iconTechTransform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        BufferedImage _iconPunch = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIPunchX(), ConfigUtil.INSTANCE.getAnvilAssetUIPunchY(),
                ConfigUtil.INSTANCE.getAnvilAssetUIPunchWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIPunchHeight());
        BufferedImage iconPunch = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        iconPunch = iconTechTransformOp.filter(_iconPunch, iconPunch);
        g.drawImage(iconPunch, ConfigUtil.INSTANCE.getAnvilAssetUITechPunchX(), ConfigUtil.INSTANCE.getAnvilAssetUITechPunchY(), iconPunch.getWidth(), iconPunch.getHeight(), null);
        BufferedImage _iconBend = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIBendX(), ConfigUtil.INSTANCE.getAnvilAssetUIBendY(),
                ConfigUtil.INSTANCE.getAnvilAssetUIBendWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIBendHeight());
        BufferedImage iconBend = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        iconBend = iconTechTransformOp.filter(_iconBend, iconBend);
        g.drawImage(iconBend, ConfigUtil.INSTANCE.getAnvilAssetUITechBendX(), ConfigUtil.INSTANCE.getAnvilAssetUITechBendY(), iconBend.getWidth(), iconBend.getHeight(), null);
        BufferedImage _iconUpset = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIUpsetX(), ConfigUtil.INSTANCE.getAnvilAssetUIUpsetY(),
                ConfigUtil.INSTANCE.getAnvilAssetUIUpsetWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIUpsetHeight());
        BufferedImage iconUpset = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        iconUpset = iconTechTransformOp.filter(_iconUpset, iconUpset);
        g.drawImage(iconUpset, ConfigUtil.INSTANCE.getAnvilAssetUITechUpsetX(), ConfigUtil.INSTANCE.getAnvilAssetUITechUpsetY(), iconUpset.getWidth(), iconUpset.getHeight(), null);
        BufferedImage _iconShrink = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIShrinkX(), ConfigUtil.INSTANCE.getAnvilAssetUIShrinkY(),
                ConfigUtil.INSTANCE.getAnvilAssetUIShrinkWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIShrinkHeight());
        BufferedImage iconShrink = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        iconShrink = iconTechTransformOp.filter(_iconShrink, iconShrink);
        g.drawImage(iconShrink, ConfigUtil.INSTANCE.getAnvilAssetUITechShrinkX(), ConfigUtil.INSTANCE.getAnvilAssetUITechShrinkY(), iconShrink.getWidth(), iconShrink.getHeight(), null);
        BufferedImage _iconHitLight = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIHitLightX(), ConfigUtil.INSTANCE.getAnvilAssetUIHitLightY(),
                ConfigUtil.INSTANCE.getAnvilAssetUIHitLightWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIHitLightHeight());
        BufferedImage iconHitLight = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        iconHitLight = iconTechTransformOp.filter(_iconHitLight, iconHitLight);
        g.drawImage(iconHitLight, ConfigUtil.INSTANCE.getAnvilAssetUITechHitLightX(), ConfigUtil.INSTANCE.getAnvilAssetUITechHitLightY(), iconHitLight.getWidth(), iconHitLight.getHeight(), null);
        BufferedImage _iconHitMedium = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIHitMediumX(), ConfigUtil.INSTANCE.getAnvilAssetUIHitMediumY(),
                ConfigUtil.INSTANCE.getAnvilAssetUIHitMediumWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIHitMediumHeight());
        BufferedImage iconHitMedium = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        iconHitMedium = iconTechTransformOp.filter(_iconHitMedium, iconHitMedium);
        g.drawImage(iconHitMedium, ConfigUtil.INSTANCE.getAnvilAssetUITechHitMediumX(), ConfigUtil.INSTANCE.getAnvilAssetUITechHitMediumY(), iconHitMedium.getWidth(), iconHitMedium.getHeight(), null);
        BufferedImage _iconHitHeavy = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIHitHeavyX(), ConfigUtil.INSTANCE.getAnvilAssetUIHitHeavyY(),
                ConfigUtil.INSTANCE.getAnvilAssetUIHitHeavyWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIHitHeavyHeight());
        BufferedImage iconHitHeavy = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        iconHitHeavy = iconTechTransformOp.filter(_iconHitHeavy, iconHitHeavy);
        g.drawImage(iconHitHeavy, ConfigUtil.INSTANCE.getAnvilAssetUITechHitHeavyX(), ConfigUtil.INSTANCE.getAnvilAssetUITechHitHeavyY(), iconHitHeavy.getWidth(), iconHitHeavy.getHeight(), null);
        BufferedImage _iconDraw = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIDrawX(), ConfigUtil.INSTANCE.getAnvilAssetUIDrawY(),
                ConfigUtil.INSTANCE.getAnvilAssetUIDrawWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIDrawHeight());
        BufferedImage iconDraw = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        iconDraw = iconTechTransformOp.filter(_iconDraw, iconDraw);
        g.drawImage(iconDraw, ConfigUtil.INSTANCE.getAnvilAssetUITechDrawX(), ConfigUtil.INSTANCE.getAnvilAssetUITechDrawY(), iconDraw.getWidth(), iconDraw.getHeight(), null);
        // 放大并加载到背景UI
        g.dispose();
        anvil = scaleGlobally(anvil);
        this.setContentPane(new ImageJPanel(anvil));
        // 选择合成配方按钮
        BufferedImage _buttonScroll = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIButtonX(), ConfigUtil.INSTANCE.getAnvilAssetUIButtonY(),
                ConfigUtil.INSTANCE.getAnvilAssetUIButtonWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIButtonHeight());
        g = _buttonScroll.getGraphics();
        BufferedImage iconScroll = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIScrollX(), ConfigUtil.INSTANCE.getAnvilAssetUIScrollY(),
                ConfigUtil.INSTANCE.getAnvilAssetUIScrollWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIScrollHeight());
        g.drawImage(iconScroll, 1, 1, iconScroll.getWidth(), iconScroll.getHeight(), null);
        g.dispose();
        _buttonScroll = scaleGlobally(_buttonScroll);
        ImageJButton buttonScroll = new ImageJButton(new ImageIcon(_buttonScroll));
        buttonScroll.setLocation(ConfigUtil.INSTANCE.getAnvilAssetUIOpenRecipeButtonX() * ConfigUtil.INSTANCE.getScaleUI(),
                ConfigUtil.INSTANCE.getAnvilAssetUIOpenRecipeButtonY() * ConfigUtil.INSTANCE.getScaleUI());
        buttonScroll.setSize(new Dimension(_buttonScroll.getWidth(), _buttonScroll.getHeight()));
        buttonScroll.addActionListener(e -> {
            // TODO 打开新Frame，显示合成成品列表
            log.debug("clicked! " + e.getID() + " " + ((ImageJButton) e.getSource()));
        });
        this.add(buttonScroll);
        // 主合成物按钮
        ImageJButton buttonMainMaterial = new ImageJButton();
        buttonMainMaterial.setIcon(null);
        buttonMainMaterial.setLocation(ConfigUtil.INSTANCE.getAnvilAssetUIOpenMaterial1ButtonX() * ConfigUtil.INSTANCE.getScaleUI(),
                ConfigUtil.INSTANCE.getAnvilAssetUIOpenMaterial1ButtonY() * ConfigUtil.INSTANCE.getScaleUI());
        buttonMainMaterial.setSize(new Dimension(ConfigUtil.INSTANCE.getAnvilAssetUIOpenMaterial1ButtonWidth() * ConfigUtil.INSTANCE.getScaleUI(),
                ConfigUtil.INSTANCE.getAnvilAssetUIOpenMaterial1ButtonHeight() * ConfigUtil.INSTANCE.getScaleUI()));
        buttonMainMaterial.addActionListener(e -> {
            // TODO 打开新Frame，显示可合成素材（若另一个按钮没选择素材，则显示全部可合成素材，若选择素材，则筛选显示剩余可拼凑成配方的素材）
            log.debug("clicked! " + e.getID() + " " + ((ImageJButton) e.getSource()));
        });
        this.add(buttonMainMaterial);
        // 副合成物按钮
        ImageJButton buttonOffMaterial = new ImageJButton();
        buttonOffMaterial.setIcon(null);
        buttonOffMaterial.setLocation(ConfigUtil.INSTANCE.getAnvilAssetUIOpenMaterial2ButtonX() * ConfigUtil.INSTANCE.getScaleUI(),
                ConfigUtil.INSTANCE.getAnvilAssetUIOpenMaterial2ButtonY() * ConfigUtil.INSTANCE.getScaleUI());
        buttonOffMaterial.setSize(new Dimension(ConfigUtil.INSTANCE.getAnvilAssetUIOpenMaterial2ButtonWidth() * ConfigUtil.INSTANCE.getScaleUI(),
                ConfigUtil.INSTANCE.getAnvilAssetUIOpenMaterial2ButtonHeight() * ConfigUtil.INSTANCE.getScaleUI()));
        buttonOffMaterial.addActionListener(e -> {
            // TODO 打开新Frame，显示可合成素材（若另一个按钮没选择素材，则显示全部可合成素材，若选择素材，则筛选显示剩余可拼凑成配方的素材）
            log.debug("clicked! " + e.getID() + " " + ((ImageJButton) e.getSource()));
        });
        this.add(buttonOffMaterial);
        // 合成配方目标值输入框
        JFormattedTextField targetInput = new JFormattedTextField(RangeIntegerFormat.getInstance(0, 145, 0));
        targetInput.setOpaque(false);
        targetInput.setBorder(BorderFactory.createEmptyBorder());
        targetInput.setText("0");
        targetInput.setCaretColor(Color.WHITE);
        targetInput.setForeground(Color.WHITE);
        targetInput.setLocation(ConfigUtil.INSTANCE.getScaleUI(), ConfigUtil.INSTANCE.getScaleUI());
        targetInput.setSize(new Dimension((ConfigUtil.INSTANCE.getAnvilAssetUIInputTargetWidth() - 2) * ConfigUtil.INSTANCE.getScaleUI(),
                (ConfigUtil.INSTANCE.getAnvilAssetUIInputTargetHeight() - 2) * ConfigUtil.INSTANCE.getScaleUI()));
        targetInput.getDocument().addDocumentListener(new DocumentListener() {
            // TODO 自动执行计算
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!targetInput.getText().isEmpty()) {
                    log.debug("changed! " + targetInput.getText());
                }
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!targetInput.getText().isEmpty()) {
                    log.debug("changed! " + targetInput.getText());
                }
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
        BufferedImage targetInputImg = new BufferedImage(ConfigUtil.INSTANCE.getAnvilAssetUIInputTargetNowWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIInputTargetNowHeight(), BufferedImage.TYPE_INT_ARGB);
        drawSlotUI(targetInputImg);
        targetInputImg = scaleGlobally(targetInputImg);
        JLabel targetInputLabel = new JLabel(new ImageIcon(targetInputImg));
        targetInputLabel.setLayout(null);
        targetInputLabel.add(targetInput);
        targetInputLabel.setLocation(ConfigUtil.INSTANCE.getAnvilAssetUIInputTargetX() * ConfigUtil.INSTANCE.getScaleUI(),
                ConfigUtil.INSTANCE.getAnvilAssetUIInputTargetY() * ConfigUtil.INSTANCE.getScaleUI());
        targetInputLabel.setSize(new Dimension(ConfigUtil.INSTANCE.getAnvilAssetUIInputTargetWidth() * ConfigUtil.INSTANCE.getScaleUI(),
                ConfigUtil.INSTANCE.getAnvilAssetUIInputTargetHeight() * ConfigUtil.INSTANCE.getScaleUI()));
        this.add(targetInputLabel);
        // 合成配方当前值输入框
        JFormattedTextField targetNowInput = new JFormattedTextField(RangeIntegerFormat.getInstance(0, 145, 0));
        targetNowInput.setOpaque(false);
        targetNowInput.setBorder(BorderFactory.createEmptyBorder());
        targetNowInput.setText("0");
        targetNowInput.setCaretColor(Color.WHITE);
        targetNowInput.setForeground(Color.WHITE);
        targetNowInput.setLocation(ConfigUtil.INSTANCE.getScaleUI(), ConfigUtil.INSTANCE.getScaleUI());
        targetNowInput.setSize(new Dimension((ConfigUtil.INSTANCE.getAnvilAssetUIInputTargetNowWidth() - 2) * ConfigUtil.INSTANCE.getScaleUI(),
                (ConfigUtil.INSTANCE.getAnvilAssetUIInputTargetNowHeight() - 2) * ConfigUtil.INSTANCE.getScaleUI()));
        targetNowInput.getDocument().addDocumentListener(new DocumentListener() {
            // TODO 自动执行计算
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!targetNowInput.getText().isEmpty()) {
                    log.debug("changed! " + targetNowInput.getText());
                }
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!targetNowInput.getText().isEmpty()) {
                    log.debug("changed! " + targetNowInput.getText());
                }
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
        BufferedImage targetNowInputImg = new BufferedImage(ConfigUtil.INSTANCE.getAnvilAssetUIInputTargetWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIInputTargetHeight(), BufferedImage.TYPE_INT_ARGB);
        drawSlotUI(targetNowInputImg);
        targetNowInputImg = scaleGlobally(targetNowInputImg);
        JLabel targetNowInputLabel = new JLabel(new ImageIcon(targetNowInputImg));
        targetNowInputLabel.setLayout(null);
        targetNowInputLabel.add(targetNowInput);
        targetNowInputLabel.setLocation(ConfigUtil.INSTANCE.getAnvilAssetUIInputTargetNowX() * ConfigUtil.INSTANCE.getScaleUI(),
                ConfigUtil.INSTANCE.getAnvilAssetUIInputTargetNowY() * ConfigUtil.INSTANCE.getScaleUI());
        targetNowInputLabel.setSize(new Dimension(ConfigUtil.INSTANCE.getAnvilAssetUIInputTargetNowWidth() * ConfigUtil.INSTANCE.getScaleUI(),
                ConfigUtil.INSTANCE.getAnvilAssetUIInputTargetNowHeight() * ConfigUtil.INSTANCE.getScaleUI()));
        this.add(targetNowInputLabel);
        // 地图种子输入框
        JFormattedTextField seedInput = new JFormattedTextField();
        seedInput.setOpaque(false);
        seedInput.setBorder(BorderFactory.createEmptyBorder());
        seedInput.setText(ConfigUtil.INSTANCE.getMapSeed());
        seedInput.setCaretColor(Color.WHITE);
        seedInput.setForeground(Color.WHITE);
        seedInput.setLocation(ConfigUtil.INSTANCE.getScaleUI(), ConfigUtil.INSTANCE.getScaleUI());
        seedInput.setSize(new Dimension((ConfigUtil.INSTANCE.getAnvilAssetUIInputSeedWidth() - 2) * ConfigUtil.INSTANCE.getScaleUI(),
                (ConfigUtil.INSTANCE.getAnvilAssetUIInputSeedHeight() - 2) * ConfigUtil.INSTANCE.getScaleUI()));
        seedInput.getDocument().addDocumentListener(new DocumentListener() {
            // TODO 重新计算target值，自动执行计算
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!seedInput.getText().isEmpty()) {
                    log.debug("changed! " + seedInput.getText());
                }
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!seedInput.getText().isEmpty()) {
                    log.debug("changed! " + seedInput.getText());
                }
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
        BufferedImage seedInputImg = new BufferedImage(ConfigUtil.INSTANCE.getAnvilAssetUIInputSeedWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIInputSeedHeight(), BufferedImage.TYPE_INT_ARGB);
        drawSlotUI(seedInputImg);
        seedInputImg = scaleGlobally(seedInputImg);
        JLabel seedInputLabel = new JLabel(new ImageIcon(seedInputImg));
        seedInputLabel.setLayout(null);
        seedInputLabel.add(seedInput);
        seedInputLabel.setLocation(ConfigUtil.INSTANCE.getAnvilAssetUIInputSeedX() * ConfigUtil.INSTANCE.getScaleUI(),
                ConfigUtil.INSTANCE.getAnvilAssetUIInputSeedY() * ConfigUtil.INSTANCE.getScaleUI());
        seedInputLabel.setSize(new Dimension(ConfigUtil.INSTANCE.getAnvilAssetUIInputSeedWidth() * ConfigUtil.INSTANCE.getScaleUI(),
                ConfigUtil.INSTANCE.getAnvilAssetUIInputSeedHeight() * ConfigUtil.INSTANCE.getScaleUI()));
        this.add(seedInputLabel);
        // 输出文本框
        JTextPane outputArea = new JTextPane();
//        Application.uiOutputAppender.initOutputStream(new UIOutputStream(outputArea));
        outputArea.setOpaque(false);
        outputArea.setBorder(BorderFactory.createEmptyBorder());
        outputArea.setCaretColor(Color.WHITE);
        outputArea.setForeground(Color.WHITE);
        outputArea.setEditable(false);
//        outputArea.setSelectedTextColor(Color.DARK_GRAY);
        outputArea.setLocation(ConfigUtil.INSTANCE.getScaleUI(), ConfigUtil.INSTANCE.getScaleUI());
        outputArea.setSize(new Dimension((ConfigUtil.INSTANCE.getAnvilAssetUIBackpackWidth() - 2) * ConfigUtil.INSTANCE.getScaleUI(),
                (ConfigUtil.INSTANCE.getAnvilAssetUIBackpackHeight() - 2) * ConfigUtil.INSTANCE.getScaleUI()));
        JScrollPane outputScrollPane = new JScrollPane(outputArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        outputScrollPane.setOpaque(false);
        outputScrollPane.getViewport().setOpaque(false);
        outputScrollPane.setLocation(ConfigUtil.INSTANCE.getAnvilAssetUIBackpackX() * ConfigUtil.INSTANCE.getScaleUI(),
                ConfigUtil.INSTANCE.getAnvilAssetUIBackpackY() * ConfigUtil.INSTANCE.getScaleUI());
        outputScrollPane.setSize(new Dimension(ConfigUtil.INSTANCE.getAnvilAssetUIBackpackWidth() * ConfigUtil.INSTANCE.getScaleUI(),
                ConfigUtil.INSTANCE.getAnvilAssetUIBackpackHeight() * ConfigUtil.INSTANCE.getScaleUI()));
        this.add(outputScrollPane);
    }

    /**
     * 通过配置文件缩放比例通用放大方法
     *
     * @param src 原始图像对象
     * @return 输出图像对象
     */
    private static BufferedImage scaleGlobally(BufferedImage src) {
        AffineTransform anvilTransform = new AffineTransform();
        anvilTransform.setToScale(ConfigUtil.INSTANCE.getScaleUI(), ConfigUtil.INSTANCE.getScaleUI());
        AffineTransformOp anvilTransformOp = new AffineTransformOp(anvilTransform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        BufferedImage dest = new BufferedImage(src.getWidth() * ConfigUtil.INSTANCE.getScaleUI(), src.getHeight() * ConfigUtil.INSTANCE.getScaleUI(), BufferedImage.TYPE_INT_ARGB);
        dest = anvilTransformOp.filter(src, dest);
        return dest;
    }

    /**
     * 对于原始大小的图像绘制背包格子UI
     *
     * @param src 原始图像对象
     */
    private static void drawSlotUI(BufferedImage src) {
        Graphics g = src.getGraphics();
        g.setColor(new Color(ConfigUtil.INSTANCE.getAnvilAssetUISlotDarkColorR(), ConfigUtil.INSTANCE.getAnvilAssetUISlotDarkColorG(),
                ConfigUtil.INSTANCE.getAnvilAssetUISlotDarkColorB(), ConfigUtil.INSTANCE.getAnvilAssetUISlotDarkColorA()));
        g.fillRect(0, 0, src.getWidth() - 1, 1);
        g.fillRect(0, 0, 1, src.getHeight() - 1);
        g.setColor(new Color(ConfigUtil.INSTANCE.getAnvilAssetUISlotLightColorR(), ConfigUtil.INSTANCE.getAnvilAssetUISlotLightColorG(),
                ConfigUtil.INSTANCE.getAnvilAssetUISlotLightColorB(), ConfigUtil.INSTANCE.getAnvilAssetUISlotLightColorA()));
        g.fillRect(1, src.getHeight() - 1, src.getWidth() - 1, 1);
        g.fillRect(src.getWidth() - 1, 1, 1, src.getHeight() - 1);
        g.setColor(new Color(ConfigUtil.INSTANCE.getAnvilAssetUISlotColorR(), ConfigUtil.INSTANCE.getAnvilAssetUISlotColorG(),
                ConfigUtil.INSTANCE.getAnvilAssetUISlotColorB(), ConfigUtil.INSTANCE.getAnvilAssetUISlotColorA()));
        g.fillRect(src.getWidth() - 1, 0, 1, 1);
        g.fillRect(0, src.getHeight() - 1, 1, 1);
        g.fillRect(1, 1, src.getWidth() - 2, src.getHeight() - 2);
        g.dispose();
    }

}
