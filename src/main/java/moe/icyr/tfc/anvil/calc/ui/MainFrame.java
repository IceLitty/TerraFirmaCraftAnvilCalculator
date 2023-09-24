package moe.icyr.tfc.anvil.calc.ui;

import lombok.extern.slf4j.Slf4j;
import moe.icyr.tfc.anvil.calc.AssetsLoader;
import moe.icyr.tfc.anvil.calc.formatter.RangeIntegerFormat;
import moe.icyr.tfc.anvil.calc.resource.*;
import moe.icyr.tfc.anvil.calc.util.ColorPresent;
import moe.icyr.tfc.anvil.calc.util.ConfigUtil;
import moe.icyr.tfc.anvil.calc.util.MessageUtil;
import moe.icyr.tfc.anvil.calc.util.TooltipColorUtil;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Icy
 * @since 2023/9/13
 */
@Slf4j
public class MainFrame extends JFrame {

    private final MainFrame mainFrame;
    private ImageJButton buttonScroll;
    private ImageJButton buttonMainMaterial;
    private ImageJButton buttonOffMaterial;
    private JFormattedTextField targetInput;
    private JLabel targetInputLabel;
    private JFormattedTextField targetNowInput;
    private JLabel targetNowInputLabel;
    private JFormattedTextField seedInput;
    private JLabel seedInputLabel;
    private JTextPane outputArea;
    private JScrollPane outputScrollPane;

    public MainFrame() throws HeadlessException {
        this.mainFrame = this;
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
        // 设置tooltip时间
        ToolTipManager.sharedInstance().setInitialDelay(0);
        ToolTipManager.sharedInstance().setDismissDelay(10000);
        // 初始化UI
        this.setTitle(MessageUtil.getMessage("ui.title"));
        this.setSize(ConfigUtil.INSTANCE.getAnvilAssetUIWidth() * ConfigUtil.INSTANCE.getScaleUI() + ConfigUtil.INSTANCE.getMainFrameWidthOffset(),
                ConfigUtil.INSTANCE.getAnvilAssetUIHeight() * ConfigUtil.INSTANCE.getScaleUI() + ConfigUtil.INSTANCE.getMainFrameHeightOffset());
//        this.setResizable(false); // TODO debug
        this.setLocationRelativeTo(null); // 屏幕居中
        this.setVisible(true);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    /**
     * 加载TFC铁砧UI
     */
    public void loadAnvilUI() {
        BufferedImage asset = getTfcAnvilAsset();
        if (asset == null)
            return;
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
        // 按钮引用
        this.buttonMainMaterial = new ImageJButton();
        this.buttonOffMaterial = new ImageJButton();
        // 选择合成配方按钮
        BufferedImage _buttonScroll = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIButtonX(), ConfigUtil.INSTANCE.getAnvilAssetUIButtonY(),
                ConfigUtil.INSTANCE.getAnvilAssetUIButtonWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIButtonHeight());
        g = _buttonScroll.getGraphics();
        BufferedImage iconScroll = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIScrollX(), ConfigUtil.INSTANCE.getAnvilAssetUIScrollY(),
                ConfigUtil.INSTANCE.getAnvilAssetUIScrollWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIScrollHeight());
        g.drawImage(iconScroll, 1, 1, iconScroll.getWidth(), iconScroll.getHeight(), null);
        g.dispose();
        _buttonScroll = scaleGlobally(_buttonScroll);
        this.buttonScroll = new ImageJButton(new ImageIcon(_buttonScroll));
        buttonScroll.setLocation(ConfigUtil.INSTANCE.getAnvilAssetUIOpenRecipeButtonX() * ConfigUtil.INSTANCE.getScaleUI(),
                ConfigUtil.INSTANCE.getAnvilAssetUIOpenRecipeButtonY() * ConfigUtil.INSTANCE.getScaleUI());
        buttonScroll.setSize(new Dimension(_buttonScroll.getWidth(), _buttonScroll.getHeight()));
        buttonScroll.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ImageJButton source = (ImageJButton) e.getSource();
                if (source.isEnabled()) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        // 打开合成窗口，显示合成结果列表
                        buttonScroll.setEnabled(false);
                        openRecipeResultScreen(buttonScroll, buttonMainMaterial, buttonOffMaterial, buttonScroll);
                        buttonScroll.setEnabled(true);
                    } else if (e.getButton() == MouseEvent.BUTTON3) {
                        // 清除合成结果选择
                        buttonScroll.setEnabled(false);
                        source.setColorTooltips(null);
                        source.getNowChooseRecipes().clear();
                        BufferedImage icon = getButtonScrollIcon(null, true);
                        if (icon != null) {
                            source.setIcon(new ImageIcon(icon));
                        }
                        buttonScroll.setEnabled(true);
                    }
                }
                log.debug("clicked! " + e.getButton() + " " + e.getID() + " " + source); // TODO debug
            }
        });
        this.add(buttonScroll);
        // 主合成物按钮
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
        this.targetInput = new JFormattedTextField(RangeIntegerFormat.getInstance(0, 145, 0));
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
                if (!targetInput.getText().isBlank()) {
                    log.debug("changed! " + targetInput.getText());
                }
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!targetInput.getText().isBlank()) {
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
        this.targetInputLabel = new JLabel(new ImageIcon(targetInputImg));
        targetInputLabel.setLayout(null);
        targetInputLabel.add(targetInput);
        targetInputLabel.setLocation(ConfigUtil.INSTANCE.getAnvilAssetUIInputTargetX() * ConfigUtil.INSTANCE.getScaleUI(),
                ConfigUtil.INSTANCE.getAnvilAssetUIInputTargetY() * ConfigUtil.INSTANCE.getScaleUI());
        targetInputLabel.setSize(new Dimension(ConfigUtil.INSTANCE.getAnvilAssetUIInputTargetWidth() * ConfigUtil.INSTANCE.getScaleUI(),
                ConfigUtil.INSTANCE.getAnvilAssetUIInputTargetHeight() * ConfigUtil.INSTANCE.getScaleUI()));
        this.add(targetInputLabel);
        // 合成配方当前值输入框
        this.targetNowInput = new JFormattedTextField(RangeIntegerFormat.getInstance(0, 145, 0));
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
                if (!targetNowInput.getText().isBlank()) {
                    log.debug("changed! " + targetNowInput.getText());
                }
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!targetNowInput.getText().isBlank()) {
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
        this.targetNowInputLabel = new JLabel(new ImageIcon(targetNowInputImg));
        targetNowInputLabel.setLayout(null);
        targetNowInputLabel.add(targetNowInput);
        targetNowInputLabel.setLocation(ConfigUtil.INSTANCE.getAnvilAssetUIInputTargetNowX() * ConfigUtil.INSTANCE.getScaleUI(),
                ConfigUtil.INSTANCE.getAnvilAssetUIInputTargetNowY() * ConfigUtil.INSTANCE.getScaleUI());
        targetNowInputLabel.setSize(new Dimension(ConfigUtil.INSTANCE.getAnvilAssetUIInputTargetNowWidth() * ConfigUtil.INSTANCE.getScaleUI(),
                ConfigUtil.INSTANCE.getAnvilAssetUIInputTargetNowHeight() * ConfigUtil.INSTANCE.getScaleUI()));
        this.add(targetNowInputLabel);
        // 地图种子输入框
        this.seedInput = new JFormattedTextField();
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
                if (!seedInput.getText().isBlank()) {
                    log.debug("changed! " + seedInput.getText());
                }
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!seedInput.getText().isBlank()) {
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
        this.seedInputLabel = new JLabel(new ImageIcon(seedInputImg));
        seedInputLabel.setLayout(null);
        seedInputLabel.add(seedInput);
        seedInputLabel.setLocation(ConfigUtil.INSTANCE.getAnvilAssetUIInputSeedX() * ConfigUtil.INSTANCE.getScaleUI(),
                ConfigUtil.INSTANCE.getAnvilAssetUIInputSeedY() * ConfigUtil.INSTANCE.getScaleUI());
        seedInputLabel.setSize(new Dimension(ConfigUtil.INSTANCE.getAnvilAssetUIInputSeedWidth() * ConfigUtil.INSTANCE.getScaleUI(),
                ConfigUtil.INSTANCE.getAnvilAssetUIInputSeedHeight() * ConfigUtil.INSTANCE.getScaleUI()));
        this.add(seedInputLabel);
        // 输出文本框
        this.outputArea = new JTextPane();
        outputArea.setOpaque(false);
        outputArea.setBorder(BorderFactory.createEmptyBorder());
        outputArea.setCaretColor(Color.WHITE);
        outputArea.setForeground(Color.WHITE);
        outputArea.setEditable(false);
        outputArea.setLocation(ConfigUtil.INSTANCE.getScaleUI(), ConfigUtil.INSTANCE.getScaleUI());
        outputArea.setSize(new Dimension((ConfigUtil.INSTANCE.getAnvilAssetUIBackpackWidth() - 2) * ConfigUtil.INSTANCE.getScaleUI(),
                (ConfigUtil.INSTANCE.getAnvilAssetUIBackpackHeight() - 2) * ConfigUtil.INSTANCE.getScaleUI()));
        this.outputScrollPane = new JScrollPane(outputArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        outputScrollPane.setOpaque(false);
        outputScrollPane.getViewport().setOpaque(false);
        outputScrollPane.getVerticalScrollBar().setUnitIncrement(30);
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

    /**
     * 打开合成结果选择页面
     *
     * @param resultButton       结果按钮
     * @param mainMaterialButton 主素材按钮
     * @param offMaterialButton  副素材按钮
     * @param sourceButton       事件触发按钮
     */
    private void openRecipeResultScreen(ImageJButton resultButton, ImageJButton mainMaterialButton, ImageJButton offMaterialButton, ImageJButton sourceButton) {
        List<ResourceLocation> recipes = new ArrayList<>();
        ResourceManager.getResources((namespace, resource) -> resource instanceof RecipeAnvil recipeR && "tfc:anvil".equals(recipeR.getType()))
                .values().forEach(recipes::addAll);
        if (sourceButton != resultButton && resultButton.getNowChooseRecipes() != null && !resultButton.getNowChooseRecipes().isEmpty()) {
            recipes = recipes.stream().filter(recipeFilterPredicate(resultButton.getNowChooseRecipes().get(0), 1)).collect(Collectors.toList());
        }
        if (sourceButton != mainMaterialButton && mainMaterialButton.getNowChooseRecipes() != null && !mainMaterialButton.getNowChooseRecipes().isEmpty()) {
            recipes = recipes.stream().filter(recipeFilterPredicate(mainMaterialButton.getNowChooseRecipes().get(0), 2)).collect(Collectors.toList());
        }
        if (sourceButton != offMaterialButton && offMaterialButton.getNowChooseRecipes() != null && !offMaterialButton.getNowChooseRecipes().isEmpty()) {
            recipes = recipes.stream().filter(recipeFilterPredicate(offMaterialButton.getNowChooseRecipes().get(0), 3)).collect(Collectors.toList());
        }
        // 加载背景图
        BufferedImage asset = getTfcAnvilAsset();
        if (asset == null)
            return;
        BufferedImage anvil = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIX(), ConfigUtil.INSTANCE.getAnvilAssetUIY(),
                ConfigUtil.INSTANCE.getAnvilAssetUIWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIHeight());
        Graphics g = anvil.getGraphics();
        g.setColor(new Color(ConfigUtil.INSTANCE.getAnvilAssetUIForegroundColorR(), ConfigUtil.INSTANCE.getAnvilAssetUIForegroundColorG(),
                ConfigUtil.INSTANCE.getAnvilAssetUIForegroundColorB(), ConfigUtil.INSTANCE.getAnvilAssetUIForegroundColorA()));
        g.fillRect(ConfigUtil.INSTANCE.getAnvilAssetUIBackgroundX(), ConfigUtil.INSTANCE.getAnvilAssetUIBackgroundY(),
                ConfigUtil.INSTANCE.getAnvilAssetUIBackgroundWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIBackgroundHeight());
        g.dispose();
        anvil = scaleGlobally(anvil);
        Integer scrollBarSize = (Integer) UIManager.get("ScrollBar.width");
        ImageJPanel recipeResultPanel = new ImageJPanel(anvil);
        recipeResultPanel.setLocation(0, 0);
        recipeResultPanel.setSize(anvil.getWidth(), anvil.getHeight());
        recipeResultPanel.setLayout(new GridLayout(1, 1));
        JPanel recipeResultScrollPanePanel = new JPanel();
        recipeResultScrollPanePanel.setOpaque(false);
        recipeResultScrollPanePanel.setLayout(null);
        recipeResultScrollPanePanel.setSize(anvil.getWidth() - scrollBarSize, anvil.getHeight());
        recipeResultScrollPanePanel.setPreferredSize(new Dimension(anvil.getWidth() - scrollBarSize, anvil.getHeight()));
        JScrollPane recipeResultScrollPane = new JScrollPane(recipeResultScrollPanePanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        recipeResultScrollPane.setOpaque(false);
        recipeResultScrollPane.getViewport().setOpaque(false);
        recipeResultScrollPane.setLocation(0, 0);
        recipeResultScrollPane.getVerticalScrollBar().setUnitIncrement(30);
        recipeResultPanel.add(recipeResultScrollPane);
        // 继续生成合成配方按钮
        int lastButtonX = ConfigUtil.INSTANCE.getAnvilAssetUIBackgroundX() * ConfigUtil.INSTANCE.getScaleUI() -
                // 距离左上角框架margin的距离
                ConfigUtil.INSTANCE.getAnvilAssetUIButtonWidth() * ConfigUtil.INSTANCE.getScaleUI() - ConfigUtil.INSTANCE.getScaleUI() +
                // 因右侧滚动条宽度，给左侧也留点空
                scrollBarSize -
                // 间距 1*缩放比例
                ConfigUtil.INSTANCE.getScaleUI();
        int lastButtonY = ConfigUtil.INSTANCE.getAnvilAssetUIBackgroundX() * ConfigUtil.INSTANCE.getScaleUI();
        // 插入一个空的配方按钮，用于取消操作直接返回上一级UI
        ImageJButton backButton = makeRecipeImageButton(null, null);
        if (backButton != null) {
            lastButtonX += backButton.getWidth() + ConfigUtil.INSTANCE.getScaleUI();
            backButton.setLocation(lastButtonX, lastButtonY);
            backButton.setColorTooltips(TooltipColorUtil.builder().withText("Return to anvil frame", ColorPresent.getTooltipItemName()).build());
            backButton.addActionListener(e -> {
                recipeResultPanel.removeAll();
                this.mainFrame.remove(recipeResultPanel);
                this.outputScrollPane.setVisible(true);
                this.seedInputLabel.setVisible(true);
                this.targetNowInputLabel.setVisible(true);
                this.targetInputLabel.setVisible(true);
                this.buttonOffMaterial.setVisible(true);
                this.buttonMainMaterial.setVisible(true);
                this.buttonScroll.setVisible(true);
                this.mainFrame.revalidate();
                this.mainFrame.repaint();
            });
            recipeResultScrollPanePanel.add(backButton);
        }
        for (ResourceLocation rl : recipes) {
            RecipeAnvil recipe = (RecipeAnvil) rl;
            // TODO 根据sourceButton再判断一下从配方中提取物品的来源，是合成结果还是主素材/副素材，然后再循环添加至panel
            if (recipe.getResult() == null) {
                log.error("Recipe " + recipe.toResourceLocationStr() + " hasn't result list, can't add in recipe menu.");
                continue;
            }
            String itemId = recipe.getResult().gotItemId();
            if (itemId == null || itemId.isBlank()) {
                log.error("Recipe " + recipe.toResourceLocationStr() + " has an empty result list, can't add in recipe menu.");
                continue;
            }
            String itemName = getItemDisplayName(itemId, null, recipe.toResourceLocationStr());
            // 写入缓存
            if (recipe.getResult().getItemTextureCache() == null) {
                Texture texture = getItemOrBlockTexture(itemId, null, recipe.toResourceLocationStr());
                recipe.getResult().setItemTextureCache(texture);
            }
            BufferedImage img;
            String textureCantFindReason = null;
            if (recipe.getResult().getItemTextureCache() != null) {
                Texture texture = recipe.getResult().getItemTextureCache();
                img = texture.getImg();
            } else {
                img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
                textureCantFindReason = "Texture not found by itemId!";
            }
            ImageJButton recipeButton = makeRecipeImageButton(img, recipe);
            if (recipeButton == null) {
                continue;
            }
            if (lastButtonX + recipeButton.getWidth() * 2 + ConfigUtil.INSTANCE.getScaleUI() > recipeResultScrollPanePanel.getWidth()) {
                // 换行
                lastButtonX = ConfigUtil.INSTANCE.getAnvilAssetUIBackgroundX() * ConfigUtil.INSTANCE.getScaleUI() +
                        scrollBarSize -
                        ConfigUtil.INSTANCE.getScaleUI();
                lastButtonY += recipeButton.getHeight() + ConfigUtil.INSTANCE.getScaleUI();
            } else {
                lastButtonX += recipeButton.getWidth() + ConfigUtil.INSTANCE.getScaleUI();
            }
            recipeButton.setLocation(lastButtonX, lastButtonY);
            recipeButton.setColorTooltips(TooltipColorUtil.builder().withText(itemName,
                            ColorPresent.getTooltipItemName())
                    .withNewLine().withText(itemId, ColorPresent.getTooltipItemDesc())
                    .withText(textureCantFindReason == null ? null : "\n" + textureCantFindReason, Color.RED).build());
            recipeButton.addActionListener(e -> {
                // 选中配方，带入主窗口
                ImageJButton source = (ImageJButton) e.getSource();
                RecipeAnvil nowChooseRecipe = source.getNowChooseRecipes().get(0);
                List<RecipeAnvil> savedRecipe = this.buttonScroll.getNowChooseRecipes();
                if (savedRecipe.size() == 0) {
                    savedRecipe.add(nowChooseRecipe);
                } else {
                    savedRecipe.set(0, nowChooseRecipe);
                }
                Texture resultTexture = nowChooseRecipe.getResult().getItemTextureCache();
                BufferedImage icon;
                if (resultTexture == null) {
                    icon = getButtonScrollIcon(null, false);
                } else {
                    icon = getButtonScrollIcon(resultTexture.getImg(), false);
                }
                if (icon != null) {
                    this.buttonScroll.setIcon(new ImageIcon(icon));
                }
                this.buttonScroll.setColorTooltips(source.getColorTooltips());
                // TODO 将配方的输入也带入UI、将配方的规则也带入面板、若有seed则计算target及自动计算
                RecipeAnvil.Ingredients input = nowChooseRecipe.getInput();
                Texture inputTexture = getItemOrBlockTexture(input.getItem(), input.getTag(), nowChooseRecipe.toResourceLocationStr());
                if (inputTexture == null) {
                    this.buttonMainMaterial.setIcon(null);
                } else {
                    this.buttonMainMaterial.setIcon(new ImageIcon(scaleGlobally(inputTexture.getImg())));
                }
                savedRecipe = this.buttonMainMaterial.getNowChooseRecipes();
                if (savedRecipe.size() == 0) {
                    savedRecipe.add(nowChooseRecipe);
                } else {
                    savedRecipe.set(0, nowChooseRecipe);
                }
                this.buttonMainMaterial.setColorTooltips(TooltipColorUtil.builder().withText(
                                getItemDisplayName(input.getItem(), input.getTag(), nowChooseRecipe.toResourceLocationStr()), ColorPresent.getTooltipItemName())
                        .withNewLine().withText(itemId, ColorPresent.getTooltipItemDesc()).build());
                this.buttonOffMaterial.setIcon(null);
                this.buttonOffMaterial.getNowChooseRecipes().clear();
                recipeResultPanel.removeAll();
                this.mainFrame.remove(recipeResultPanel);
                this.outputScrollPane.setVisible(true);
                this.seedInputLabel.setVisible(true);
                this.targetNowInputLabel.setVisible(true);
                this.targetInputLabel.setVisible(true);
                this.buttonOffMaterial.setVisible(true);
                this.buttonMainMaterial.setVisible(true);
                this.buttonScroll.setVisible(true);
                this.mainFrame.revalidate();
                this.mainFrame.repaint();
                log.debug("clicked! " + source.getNowChooseRecipes());
            });
            recipeResultScrollPanePanel.add(recipeButton);
        }
        this.add(recipeResultPanel);
        // 除了lastButtonY的高度少一行外，再多展示一行空行
        recipeResultScrollPanePanel.setPreferredSize(new Dimension(anvil.getWidth() - scrollBarSize,
                lastButtonY + (ConfigUtil.INSTANCE.getAnvilAssetUIButtonHeight() + 1) * ConfigUtil.INSTANCE.getScaleUI() + ConfigUtil.INSTANCE.getScaleUI()));
        this.outputScrollPane.setVisible(false);
        this.seedInputLabel.setVisible(false);
        this.targetNowInputLabel.setVisible(false);
        this.targetInputLabel.setVisible(false);
        this.buttonOffMaterial.setVisible(false);
        this.buttonMainMaterial.setVisible(false);
        this.buttonScroll.setVisible(false);
        this.repaint();
    }

    /**
     * 通过TagId获取物品Id
     *
     * @param tagId    TagId
     * @param sourceId 用于日志打印的请求来源ID
     * @return 物品ID
     */
    private static String getItemIdFromTagId(String tagId, String sourceId) {
        String itemId = null;
        if (tagId != null) {
            Map<String, List<ResourceLocation>> tags = ResourceManager.getResources((n, r) -> r instanceof Tag && tagId.equals(r.toResourceLocationStr()));
            if (tags.isEmpty()) {
                log.warn("Can't find item from tag " + tagId + " used by " + sourceId + ", will be display blank icon.");
            } else {
                List<ResourceLocation> rl = new ArrayList<>();
                tags.values().forEach(rl::addAll);
                if (rl.isEmpty()) {
                    log.warn("Can't find item from tag " + tagId + " used by " + sourceId + ", will be display blank icon.");
                } else if (rl.size() > 1) {
                    String join = tags.values().stream().map(rll -> rll.stream().map(ResourceLocation::getOriginalPath).collect(Collectors.joining(", "))).collect(Collectors.joining(", "));
                    log.warn("Tag " + tagId + " has more than 1 values used by " + sourceId + " ([" + join + "]), will be display blank icon.");
                } else {
                    List<String> tagVals = ((Tag) rl.get(0)).getValues();
                    if (tagVals == null || tagVals.size() != 1) {
                        log.warn("Tag " + tagId + " has empty or more than 1 values used by " + sourceId + ", will be display blank icon.");
                    } else {
                        itemId = tagVals.get(0);
                    }
                }
            }
        }
        return itemId;
    }

    /**
     * 根据物品ID或TagID获取物品材质
     *
     * @param itemId   物品ID
     * @param tagId    TagId
     * @param sourceId 用于日志打印的请求来源ID
     * @return 材质
     */
    private static Texture getItemOrBlockTexture(String itemId, String tagId, String sourceId) {
        if (itemId == null && tagId != null) {
            itemId = getItemIdFromTagId(tagId, sourceId);
        }
        if (itemId != null) {
            String finalItemId = itemId;
            Map<String, List<ResourceLocation>> textures = ResourceManager.getResources((n, r) ->
                    r instanceof Texture rt && ("item".equals(rt.getTextureType()) || "block".equals(rt.getTextureType())) && finalItemId.equals(r.toResourceLocationStr()) && rt.getImg() != null);
            if (textures.isEmpty()) {
                log.warn("Can't find " + itemId + " texture from " + sourceId + ", will be display blank icon.");
            } else {
                if (textures.size() > 1) {
                    List<ResourceLocation> texturesTypeItem = new ArrayList<>();
                    List<ResourceLocation> texturesTypeBlock = new ArrayList<>();
                    textures.values().forEach(rll -> rll.forEach(rllr -> {
                        Texture rllr1 = (Texture) rllr;
                        if ("item".equals(rllr1.getTextureType())) {
                            texturesTypeItem.add(rllr1);
                        } else if ("block".equals(rllr1.getTextureType())) {
                            texturesTypeBlock.add(rllr1);
                        }
                    }));
                    if (texturesTypeItem.size() == 1) {
                        ResourceLocation found = texturesTypeItem.get(0);
                        log.warn("Find " + itemId + " textures more than 1 from " + sourceId + ", but it contain only 1 item texture, UI will use it.");
                        return (Texture) found;
                    } else if (texturesTypeBlock.size() == 1) {
                        ResourceLocation found = texturesTypeBlock.get(0);
                        log.warn("Find " + itemId + " textures more than 1 from " + sourceId + ", but it contain only 1 block texture, UI will use it.");
                        return (Texture) found;
                    } else {
                        String join = textures.values().stream().map(rll -> rll.stream().map(ResourceLocation::getOriginalPath).collect(Collectors.joining(", "))).collect(Collectors.joining(", "));
                        log.warn("Find " + itemId + " textures more than 1 from " + sourceId + " ([" + join + "]), will be display blank icon.");
                    }
                } else {
                    Iterator<String> it = textures.keySet().iterator();
                    //noinspection unused
                    boolean b = it.hasNext();
                    List<ResourceLocation> rll = textures.get(it.next());
                    if (rll.isEmpty()) {
                        log.warn("Can't find " + itemId + " texture from " + sourceId + " result item, will be display blank icon.");
                    } else if (rll.size() > 1) {
                        List<ResourceLocation> texturesTypeItem = new ArrayList<>();
                        List<ResourceLocation> texturesTypeBlock = new ArrayList<>();
                        rll.forEach(rllr -> {
                            Texture rllr1 = (Texture) rllr;
                            if ("item".equals(rllr1.getTextureType())) {
                                texturesTypeItem.add(rllr1);
                            } else if ("block".equals(rllr1.getTextureType())) {
                                texturesTypeBlock.add(rllr1);
                            }
                        });
                        if (texturesTypeItem.size() == 1) {
                            ResourceLocation found = texturesTypeItem.get(0);
                            log.warn("Find " + itemId + " textures more than 1 from " + sourceId + ", but it contain only 1 item texture, UI will use it.");
                            return (Texture) found;
                        } else if (texturesTypeBlock.size() == 1) {
                            ResourceLocation found = texturesTypeBlock.get(0);
                            log.warn("Find " + itemId + " textures more than 1 from " + sourceId + ", but it contain only 1 block texture, UI will use it.");
                            return (Texture) found;
                        } else {
                            String join = rll.stream().map(ResourceLocation::getOriginalPath).collect(Collectors.joining(", "));
                            log.warn("Find " + itemId + " textures more than 1 from " + sourceId + " ([" + join + "]), will be display blank icon.");
                        }
                    } else {
                        for (ResourceLocation v : rll) {
                            return (Texture) v;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * 获取物品显示名称
     *
     * @param itemId   物品ID
     * @param tagId    TagId
     * @param sourceId 用于日志打印的请求来源ID
     * @return 显示名称
     */
    private static String getItemDisplayName(String itemId, String tagId, String sourceId) {
        if (itemId == null && tagId != null) {
            itemId = getItemIdFromTagId(tagId, sourceId);
        }
        if (itemId == null) {
            log.warn("Recipe " + sourceId + " call getItemDisplayName function but with null itemId, it will lost name display in tooltip.");
            return "";
        }
        String blockNameKey = "block." + itemId.replace(":", ".").replace("/", ".");
        String itemNameKey = "item." + itemId.replace(":", ".").replace("/", ".");
        Map<String, List<ResourceLocation>> langResources = ResourceManager.getResources((namespace, resource) -> resource instanceof Lang rlang && (blockNameKey.equals(rlang.getFullKey()) || itemNameKey.equals(rlang.getFullKey())));
        String itemName = "";
        if (!langResources.isEmpty()) {
            for (List<ResourceLocation> ll : langResources.values()) {
                for (ResourceLocation r : ll) {
                    Lang lang = (Lang) r;
                    itemName = lang.getDisplayName();
                    break;
                }
            }
        }
        if (itemName.isBlank()) {
            log.warn("Recipe " + sourceId + " hasn't lang resource (block. or item." + itemId.replace(":", ".").replace("/", ".") + "), it will lost name display in tooltip.");
        }
        return itemName;
    }

    /**
     * 拼装合成配方图标按钮
     *
     * @param img 图标，可为null则无图标
     * @return 按钮
     */
    private static ImageJButton makeRecipeImageButton(BufferedImage img, RecipeAnvil recipeAnvil) {
        BufferedImage asset = getTfcAnvilAsset();
        if (asset == null) {
            return null;
        }
        BufferedImage _buttonScroll = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIButtonX(), ConfigUtil.INSTANCE.getAnvilAssetUIButtonY(),
                ConfigUtil.INSTANCE.getAnvilAssetUIButtonWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIButtonHeight());
        // 先放大，防止先缩小图标后再放大导致失真
        _buttonScroll = scaleGlobally(_buttonScroll);
        if (img != null) {
            double scaleWidth = (double) ConfigUtil.INSTANCE.getAnvilAssetUIButtonWidth() / img.getWidth() * ConfigUtil.INSTANCE.getScaleUI();
            double scaleHeight = (double) ConfigUtil.INSTANCE.getAnvilAssetUIButtonHeight() / img.getHeight() * ConfigUtil.INSTANCE.getScaleUI();
            AffineTransform anvilTransform = new AffineTransform();
            anvilTransform.setToScale(scaleWidth, scaleHeight);
            AffineTransformOp anvilTransformOp = new AffineTransformOp(anvilTransform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            BufferedImage dest = new BufferedImage(img.getWidth() * ConfigUtil.INSTANCE.getScaleUI(), img.getHeight() * ConfigUtil.INSTANCE.getScaleUI(), BufferedImage.TYPE_INT_ARGB);
            img = anvilTransformOp.filter(img, dest);
            Graphics g = _buttonScroll.getGraphics();
            g.drawImage(img, 1, 1, img.getWidth(), img.getHeight(), null);
            g.dispose();
        }
        ImageJButton buttonScroll = new ImageJButton(new ImageIcon(_buttonScroll));
        buttonScroll.setSize(new Dimension(_buttonScroll.getWidth(), _buttonScroll.getHeight()));
        if (recipeAnvil != null) {
            buttonScroll.getNowChooseRecipes().add(recipeAnvil);
        }
        return buttonScroll;
    }

    /**
     * 获取拷贝的TFC铁砧UI资源
     */
    public static BufferedImage getTfcAnvilAsset() {
        List<ResourceLocation> anvilRs = ResourceManager.getResources("tfc", r -> r instanceof Texture rr && "gui".equals(rr.getTextureType()) && "anvil".equals(r.getPath()));
        if (anvilRs.isEmpty()) {
            log.error("Not load tfc:anvil texture resource!");
            return null;
        }
        BufferedImage _asset = ((Texture) anvilRs.get(0)).getImg();
        BufferedImage asset = new BufferedImage(_asset.getWidth(), _asset.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = asset.getGraphics();
        g.drawImage(_asset, 0, 0, null);
        g.dispose();
        return asset;
    }

    /**
     * 获取合成结果按钮图
     *
     * @param itemTexture   要附加在按钮图上的物品图标
     * @param useScrollIcon 使用默认的卷轴图标
     * @return 图标成品图
     */
    public static BufferedImage getButtonScrollIcon(BufferedImage itemTexture, boolean useScrollIcon) {
        BufferedImage asset = getTfcAnvilAsset();
        if (asset == null) {
            return null;
        }
        BufferedImage _buttonScroll = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIButtonX(), ConfigUtil.INSTANCE.getAnvilAssetUIButtonY(),
                ConfigUtil.INSTANCE.getAnvilAssetUIButtonWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIButtonHeight());
        _buttonScroll = scaleGlobally(_buttonScroll);
        BufferedImage iconScroll = null;
        if (useScrollIcon) {
            iconScroll = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIScrollX(), ConfigUtil.INSTANCE.getAnvilAssetUIScrollY(),
                    ConfigUtil.INSTANCE.getAnvilAssetUIScrollWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIScrollHeight());
            iconScroll = scaleGlobally(iconScroll);
        } else if (itemTexture != null) {
            double widthAf = (double) (_buttonScroll.getWidth() - 2) / itemTexture.getWidth();
            double heightAf = (double) (_buttonScroll.getHeight() - 2) / itemTexture.getHeight();
            AffineTransform iconTechTransform = new AffineTransform();
            iconTechTransform.setToScale(widthAf, heightAf);
            AffineTransformOp iconTechTransformOp = new AffineTransformOp(iconTechTransform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            iconScroll = new BufferedImage(_buttonScroll.getWidth() - 2, _buttonScroll.getHeight() - 2, BufferedImage.TYPE_INT_ARGB);
            iconScroll = iconTechTransformOp.filter(itemTexture, iconScroll);
        }
        if (iconScroll != null) {
            Graphics g = _buttonScroll.getGraphics();
            g.drawImage(iconScroll, 1, 1, iconScroll.getWidth(), iconScroll.getHeight(), null);
            g.dispose();
        }
        return _buttonScroll;
    }

    /**
     * 筛选符合已选合成素材的配方
     * TODO 目前带不出来，可能要用公用方法把tag转换itemid来覆盖掉此处逻辑以重写
     *
     * @param nowChoose
     * @param findFromWhere 查询源，用来区分从配方的哪处进行筛选。1=结果按钮，2=主素材按钮，3=副素材按钮
     * @return 筛选方法 {@link java.util.function.Predicate}<{@link Map.Entry}<{@link String}, {@link RecipeAnvil}>
     */
    private static Predicate<ResourceLocation> recipeFilterPredicate(RecipeAnvil nowChoose, int findFromWhere) {
        return recipe -> {
            RecipeAnvil.Ingredients input = ((RecipeAnvil) recipe).getInput();
            if (input.getItem() != null && !input.getItem().isBlank()) {
                if (nowChoose.toResourceLocationStr().equals(input.getItem())) {
                    return true;
                }
            } else if (input.getTag() != null && !input.getTag().isBlank()) {
                // 写入缓存
                if (input.getTagCache().isEmpty()) {
                    Map<String, List<ResourceLocation>> tags = ResourceManager.getResources((n, r) ->
                            r instanceof Tag && r.toResourceLocationStr().equals(input.getTag()));
                    if (tags.isEmpty()) {
                        log.warn("Can't find " + input.getTag() + " from " + recipe.toResourceLocationStr() + " input ingredients, will be lost in result menu.");
                    } else {
                        tags.values().forEach(t -> t.forEach(tl -> input.getTagCache().add((Tag) tl)));
                    }
                }
                if (!input.getTagCache().isEmpty()) {
                    for (Tag t : input.getTagCache()) {
                        for (String tv : t.getValues()) {
                            if (nowChoose.toResourceLocationStr().equals(tv)) {
                                return true;
                            }
                        }
                    }
                }
            } else if (input.getType() != null && !input.getType().isBlank()) {
                if (input.getIngredient() != null && input.getIngredient().getItem() != null) {
                    if (nowChoose.toResourceLocationStr().equals(input.getIngredient().getItem())) {
                        return true;
                    }
                } else {
                    log.warn("This type " + input.getType() + " doesn't specific an item id, so can't support. This recipe from " + recipe.toResourceLocationStr() + " and will be lost in result menu.");
                }
            }
            return false;
        };
    }

}
