package moe.icyr.tfc.anvil.calc.ui;

import lombok.extern.slf4j.Slf4j;
import moe.icyr.tfc.anvil.calc.AssetsLoader;
import moe.icyr.tfc.anvil.calc.formatter.RangeIntegerFormat;
import moe.icyr.tfc.anvil.calc.resource.*;
import moe.icyr.tfc.anvil.calc.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.LookupOp;
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
    private TooltipJFormattedTextField targetInput;
    private JLabel targetInputLabel;
    private TooltipJFormattedTextField targetNowInput;
    private JLabel targetNowInputLabel;
    private TooltipJFormattedTextField seedInput;
    private JLabel seedInputLabel;
    private JTextPane outputArea;
    private JScrollPane outputScrollPane;
    private ImageJButton ruleLeft;
    private ImageJButton ruleMiddle;
    private ImageJButton ruleRight;

    public MainFrame() throws HeadlessException {
        // TODO 改为先显示空面板，标题显示加载资源进度条
        this.mainFrame = this;
        // 加载配置文件
        log.debug(MessageUtil.getMessage("log.config.loaded", ConfigUtil.INSTANCE));
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
        if (asset == null) {
            JTextArea jTextArea = new JTextArea();
            jTextArea.setText(MessageUtil.getMessage("ui.label.no.tfc.jar"));
            jTextArea.setOpaque(false);
            jTextArea.setBackground(UIManager.getColor("Label.background"));
            jTextArea.setForeground(Color.RED);
            jTextArea.setWrapStyleWord(true);
            jTextArea.setLineWrap(true);
            jTextArea.setEditable(false);
            jTextArea.setFocusable(false);
            jTextArea.setFont(UIManager.getFont("Label.font"));
            this.add(jTextArea);
            return;
        }
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
        buttonScroll.addMouseListener(new RecipeJButtonMouseAdapter());
        this.add(buttonScroll);
        // 主合成物按钮
        buttonMainMaterial.setIcon(null);
        buttonMainMaterial.setLocation(ConfigUtil.INSTANCE.getAnvilAssetUIOpenMaterial1ButtonX() * ConfigUtil.INSTANCE.getScaleUI(),
                ConfigUtil.INSTANCE.getAnvilAssetUIOpenMaterial1ButtonY() * ConfigUtil.INSTANCE.getScaleUI());
        buttonMainMaterial.setSize(new Dimension(ConfigUtil.INSTANCE.getAnvilAssetUIOpenMaterial1ButtonWidth() * ConfigUtil.INSTANCE.getScaleUI(),
                ConfigUtil.INSTANCE.getAnvilAssetUIOpenMaterial1ButtonHeight() * ConfigUtil.INSTANCE.getScaleUI()));
        buttonMainMaterial.addMouseListener(new RecipeJButtonMouseAdapter());
        this.add(buttonMainMaterial);
        // 副合成物按钮
        buttonOffMaterial.setIcon(null);
        buttonOffMaterial.setLocation(ConfigUtil.INSTANCE.getAnvilAssetUIOpenMaterial2ButtonX() * ConfigUtil.INSTANCE.getScaleUI(),
                ConfigUtil.INSTANCE.getAnvilAssetUIOpenMaterial2ButtonY() * ConfigUtil.INSTANCE.getScaleUI());
        buttonOffMaterial.setSize(new Dimension(ConfigUtil.INSTANCE.getAnvilAssetUIOpenMaterial2ButtonWidth() * ConfigUtil.INSTANCE.getScaleUI(),
                ConfigUtil.INSTANCE.getAnvilAssetUIOpenMaterial2ButtonHeight() * ConfigUtil.INSTANCE.getScaleUI()));
        buttonOffMaterial.addMouseListener(new RecipeJButtonMouseAdapter());
        this.add(buttonOffMaterial);
        // 合成配方目标值输入框
        this.targetInput = new TooltipJFormattedTextField(RangeIntegerFormat.getInstance(0, 145, 0));
        targetInput.setOpaque(false);
        targetInput.setBorder(BorderFactory.createEmptyBorder());
        targetInput.setText("0");
        targetInput.setCaretColor(Color.WHITE);
        targetInput.setForeground(Color.WHITE);
        targetInput.setLocation(ConfigUtil.INSTANCE.getScaleUI(), ConfigUtil.INSTANCE.getScaleUI());
        targetInput.setSize(new Dimension((ConfigUtil.INSTANCE.getAnvilAssetUIInputTargetWidth() - 2) * ConfigUtil.INSTANCE.getScaleUI(),
                (ConfigUtil.INSTANCE.getAnvilAssetUIInputTargetHeight() - 2) * ConfigUtil.INSTANCE.getScaleUI()));
        targetInput.setColorTooltips(TooltipColorUtil.builder()
                .withText(MessageUtil.getMessage("ui.tooltip.target"), Color.WHITE)
                .build());
        targetInput.addFocusListener(new FocusListener() {
            private String textGet = null;
            @Override
            public void focusGained(FocusEvent e) {
                textGet = ((JTextField) e.getSource()).getText();
            }
            @Override
            public void focusLost(FocusEvent e) {
                String target = targetInput.getText();
                if (target != null && !target.isBlank() && !target.equals(textGet)) {
                    log.debug("changed! " + target);
                    // TODO 调整红色箭头 自动执行计算
                }
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
        this.targetNowInput = new TooltipJFormattedTextField(RangeIntegerFormat.getInstance(0, 145, 0));
        targetNowInput.setOpaque(false);
        targetNowInput.setBorder(BorderFactory.createEmptyBorder());
        targetNowInput.setText("0");
        targetNowInput.setCaretColor(Color.WHITE);
        targetNowInput.setForeground(Color.WHITE);
        targetNowInput.setLocation(ConfigUtil.INSTANCE.getScaleUI(), ConfigUtil.INSTANCE.getScaleUI());
        targetNowInput.setSize(new Dimension((ConfigUtil.INSTANCE.getAnvilAssetUIInputTargetNowWidth() - 2) * ConfigUtil.INSTANCE.getScaleUI(),
                (ConfigUtil.INSTANCE.getAnvilAssetUIInputTargetNowHeight() - 2) * ConfigUtil.INSTANCE.getScaleUI()));
        targetNowInput.setColorTooltips(TooltipColorUtil.builder()
                .withText(MessageUtil.getMessage("ui.tooltip.target.now"), Color.WHITE)
                .build());
        targetNowInput.addFocusListener(new FocusListener() {
            private String textGet = null;
            @Override
            public void focusGained(FocusEvent e) {
                textGet = ((JTextField) e.getSource()).getText();
            }
            @Override
            public void focusLost(FocusEvent e) {
                String targetNow = targetNowInput.getText();
                if (targetNow != null && !targetNow.isBlank() && !targetNow.equals(textGet)) {
                    log.debug("changed! " + targetNow);
                    // TODO 调整绿色箭头 自动执行计算
                }
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
        this.seedInput = new TooltipJFormattedTextField();
        seedInput.setOpaque(false);
        seedInput.setBorder(BorderFactory.createEmptyBorder());
        seedInput.setText(ConfigUtil.INSTANCE.getMapSeed());
        seedInput.setCaretColor(Color.WHITE);
        seedInput.setForeground(Color.WHITE);
        seedInput.setLocation(ConfigUtil.INSTANCE.getScaleUI(), ConfigUtil.INSTANCE.getScaleUI());
        seedInput.setSize(new Dimension((ConfigUtil.INSTANCE.getAnvilAssetUIInputSeedWidth() - 2) * ConfigUtil.INSTANCE.getScaleUI(),
                (ConfigUtil.INSTANCE.getAnvilAssetUIInputSeedHeight() - 2) * ConfigUtil.INSTANCE.getScaleUI()));
        seedInput.setColorTooltips(TooltipColorUtil.builder()
                .withText(MessageUtil.getMessage("ui.tooltip.map.seed"), Color.WHITE)
                .build());
        seedInput.addFocusListener(new FocusListener() {
            private String textGet = null;
            @Override
            public void focusGained(FocusEvent e) {
                textGet = ((JTextField) e.getSource()).getText();
            }
            @Override
            public void focusLost(FocusEvent e) {
                String seed = seedInput.getText();
                if (seed != null && !seed.isBlank() && !seed.equals(textGet)) {
                    List<RecipeAnvil> savedRecipe = buttonScroll.getNowChooseRecipes();
                    if (!savedRecipe.isEmpty()) {
                        try {
                            long _seed = Long.parseLong(seed);
                            int target = new XoroshiroRandomUtil().calcTarget(_seed, savedRecipe.get(0).toResourceLocationStr());
                            targetInput.setText(String.valueOf(target));
                            // TODO 自动执行计算
                        } catch (NumberFormatException ignored) {
                            targetInput.setText("0");
                            log.error(MessageUtil.getMessage("log.func.calc.wrong.seed"));
                        }
                    }
                }
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
        // 合成物规则左
        this.ruleLeft = new ImageJButton();
        ruleLeft.setIcon(null);
        ruleLeft.setLocation(ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFramePos1X() * ConfigUtil.INSTANCE.getScaleUI(),
                ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFramePos1Y() * ConfigUtil.INSTANCE.getScaleUI());
        ruleLeft.setSize(new Dimension(ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameNotAnyWidth() * ConfigUtil.INSTANCE.getScaleUI(),
                ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameNotAnyHeight() * ConfigUtil.INSTANCE.getScaleUI()));
        this.add(ruleLeft);
        // 合成物规则中
        this.ruleMiddle = new ImageJButton();
        ruleMiddle.setIcon(null);
        ruleMiddle.setLocation(ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFramePos2X() * ConfigUtil.INSTANCE.getScaleUI(),
                ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFramePos2Y() * ConfigUtil.INSTANCE.getScaleUI());
        ruleMiddle.setSize(new Dimension(ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameAnyWidth() * ConfigUtil.INSTANCE.getScaleUI(),
                ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameAnyHeight() * ConfigUtil.INSTANCE.getScaleUI()));
        this.add(ruleMiddle);
        // 合成物规则右
        this.ruleRight = new ImageJButton();
        ruleRight.setIcon(null);
        ruleRight.setLocation(ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFramePos3X() * ConfigUtil.INSTANCE.getScaleUI(),
                ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFramePos3Y() * ConfigUtil.INSTANCE.getScaleUI());
        ruleRight.setSize(new Dimension(ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameNotLastWidth() * ConfigUtil.INSTANCE.getScaleUI(),
                ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameNotLastHeight() * ConfigUtil.INSTANCE.getScaleUI()));
        this.add(ruleRight);
        // TODO 红绿色箭头指示器
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
     * @param sourceButton 事件触发按钮，会根据此按钮读取其他几个按钮的已选配方数据
     */
    private void openRecipeResultScreen(ImageJButton sourceButton) {
        List<ResourceLocation> recipes = new ArrayList<>();
        ResourceManager.getResources((namespace, resource) -> resource instanceof RecipeAnvil recipeR && "tfc:anvil".equals(recipeR.getType()))
                .values().forEach(recipes::addAll);
        recipes = recipes.stream().filter(recipeFilterPredicate(sourceButton)).sorted((o1, o2) -> {
            // 按显示文本排序
            RecipeAnvil o11 = (RecipeAnvil) o1;
            RecipeAnvil o22 = (RecipeAnvil) o2;
            String o1ItemId;
            String o2ItemId;
            if (sourceButton == buttonScroll) {
                o1ItemId = o11.getResult().gotItemId();
                o2ItemId = o22.getResult().gotItemId();
            } else {
                o1ItemId = getItemIdFromTagId(o11.getInput(), o1.toResourceLocationStr());
                o2ItemId = getItemIdFromTagId(o22.getInput(), o2.toResourceLocationStr());
            }
            String o1ItemName = getItemDisplayName(o1ItemId, null, o1.toResourceLocationStr());
            String o2ItemName = getItemDisplayName(o2ItemId, null, o2.toResourceLocationStr());
            if (o1ItemName.trim().isEmpty() || o2ItemName.trim().isEmpty()) {
                o1ItemName = o1ItemId;
                o2ItemName = o2ItemId;
            }
            return o1ItemName.compareTo(o2ItemName);
        }).collect(Collectors.toList());
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
            backButton.setColorTooltips(TooltipColorUtil.builder().withText(MessageUtil.getMessage("ui.choose.button.back.title"), ColorPresent.getTooltipItemName()).build());
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
                this.ruleLeft.setVisible(true);
                this.ruleMiddle.setVisible(true);
                this.ruleRight.setVisible(true);
                this.mainFrame.revalidate();
                this.mainFrame.repaint();
            });
            recipeResultScrollPanePanel.add(backButton);
        }
        for (ResourceLocation rl : recipes) {
            RecipeAnvil recipe = (RecipeAnvil) rl;
            String itemId;
            if (sourceButton == this.buttonScroll) {
                if (recipe.getResult() == null) {
                    log.error(MessageUtil.getMessage("log.load.wrong.recipe.no.result", recipe.toResourceLocationStr()));
                    continue;
                }
                itemId = recipe.getResult().gotItemId();
                if (itemId == null || itemId.isBlank()) {
                    log.error(MessageUtil.getMessage("log.load.wrong.recipe.no.result.item", recipe.toResourceLocationStr()));
                }
            } else if (sourceButton == this.buttonMainMaterial) {
                itemId = getItemIdFromTagId(recipe.getInput(), recipe.toResourceLocationStr());
                if (itemId == null || itemId.isBlank()) {
                    log.error(MessageUtil.getMessage("log.load.wrong.recipe.no.input.item", recipe.toResourceLocationStr()));
                }
            } else if (sourceButton == this.buttonOffMaterial) {
                continue;
            } else {
                continue;
            }
            if (itemId == null || itemId.isBlank()) {
                continue;
            }
            String itemNamespace = itemId.split(":")[0];
            String itemName = getItemDisplayName(itemId, null, recipe.toResourceLocationStr());
            String fullRecipeDesc = "";
            String resultItemName = getItemDisplayName(recipe.getResult().gotItemId(), null, recipe.toResourceLocationStr());
            String inputMainItemName = getItemDisplayName(recipe.getInput().getItem(), recipe.getInput().getTag(), recipe.toResourceLocationStr());
            String inputOffItemName = "";
            if (!inputMainItemName.isBlank()) {
                fullRecipeDesc += inputMainItemName;
            }
            if (!inputOffItemName.isBlank()) {
                fullRecipeDesc += " + " + inputOffItemName;
            }
            if (!resultItemName.isBlank()) {
                fullRecipeDesc += " -> " + resultItemName;
            }
            final String finalFullRecipeDesc = fullRecipeDesc;
            // 获取材质写入缓存
            RecipeAnvil.Textureable textureable;
            if (sourceButton == this.buttonScroll) {
                textureable = recipe.getResult();
            } else {
                textureable = recipe.getInput();
            }
            if (textureable.getItemTextureCache() == null) {
                Texture texture = getItemOrBlockTexture(itemId, null, recipe.toResourceLocationStr());
                textureable.setItemTextureCache(texture);
            }
            BufferedImage img;
            final String textureCantFindReason;
            if (textureable.getItemTextureCache() != null) {
                textureCantFindReason = null;
                Texture texture = textureable.getItemTextureCache();
                img = texture.getImg();
            } else {
                img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
                textureCantFindReason = MessageUtil.getMessage("ui.tooltip.texture.not.found");
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
            recipeButton.setColorTooltips(TooltipColorUtil.builder()
                    .withText(itemName, ColorPresent.getTooltipItemName())
                    .withNewLine().withText(itemId, ColorPresent.getTooltipItemDesc())
                    .withNewLine().withText(MessageUtil.getMessage("ui.tooltip.mod.is"), ColorPresent.getTooltipItemDesc()).withText(ResourceManager.getModDisplayNameByModId(itemNamespace), ColorPresent.getTooltipModId(), false, true)
                    .withText(fullRecipeDesc.isBlank() ? null : "\n" + fullRecipeDesc, ColorPresent.getTooltipItemDesc())
                    .withNewLine().withText(MessageUtil.getMessage("ui.tooltip.recipe.from") + recipe.toResourceLocationStr(), ColorPresent.getTooltipItemDesc())
                    .withText(textureCantFindReason == null ? null : "\n" + textureCantFindReason, Color.RED)
                    .build());
            recipeButton.addActionListener(e -> {
                // 选中配方，带入主窗口
                ImageJButton chooseRecipe = (ImageJButton) e.getSource();
                RecipeAnvil nowChooseRecipe = chooseRecipe.getNowChooseRecipes().get(0);
                String resultItemId = nowChooseRecipe.getResult().gotItemId();
                String mainMaterialItemId = getItemIdFromTagId(nowChooseRecipe.getInput(), nowChooseRecipe.toResourceLocationStr());
                // 设置结果按钮
                List<RecipeAnvil> savedRecipe = this.buttonScroll.getNowChooseRecipes();
                if (savedRecipe.isEmpty()) {
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
                this.buttonScroll.setColorTooltips(TooltipColorUtil.builder()
                        .withText(resultItemName, ColorPresent.getTooltipItemName())
                        .withNewLine().withText(resultItemId, ColorPresent.getTooltipItemDesc())
                        .withNewLine().withText(MessageUtil.getMessage("ui.tooltip.mod.is"), ColorPresent.getTooltipItemDesc()).withText(ResourceManager.getModDisplayNameByModId(resultItemId.split(":")[0]), ColorPresent.getTooltipModId(), false, true)
                        .withText(finalFullRecipeDesc.isBlank() ? null : "\n" + finalFullRecipeDesc, ColorPresent.getTooltipItemDesc())
                        .withNewLine().withText(MessageUtil.getMessage("ui.tooltip.recipe.from") + recipe.toResourceLocationStr(), ColorPresent.getTooltipItemDesc())
                        .build());
                // 设置主素材按钮
                savedRecipe = this.buttonMainMaterial.getNowChooseRecipes();
                if (savedRecipe.isEmpty()) {
                    savedRecipe.add(nowChooseRecipe);
                } else {
                    savedRecipe.set(0, nowChooseRecipe);
                }
                RecipeAnvil.Ingredients input = nowChooseRecipe.getInput();
                Texture inputTexture = getItemOrBlockTexture(input.getItem(), input.getTag(), nowChooseRecipe.toResourceLocationStr());
                if (inputTexture == null) {
                    this.buttonMainMaterial.setIcon(null);
                } else {
                    this.buttonMainMaterial.setIcon(new ImageIcon(scaleGlobally(inputTexture.getImg())));
                }
                this.buttonMainMaterial.setColorTooltips(TooltipColorUtil.builder()
                        .withText(inputMainItemName, ColorPresent.getTooltipItemName())
                        .withNewLine().withText(mainMaterialItemId, ColorPresent.getTooltipItemDesc())
                        .withNewLine().withText(MessageUtil.getMessage("ui.tooltip.mod.is"), ColorPresent.getTooltipItemDesc()).withText(ResourceManager.getModDisplayNameByModId(mainMaterialItemId.split(":")[0]), ColorPresent.getTooltipModId(), false, true)
                        .withNewLine().withText(MessageUtil.getMessage("ui.tooltip.recipe.from") + recipe.toResourceLocationStr(), ColorPresent.getTooltipItemDesc())
                        .build());
                // 设置副素材按钮
                this.buttonOffMaterial.setIcon(null);
                this.buttonOffMaterial.getNowChooseRecipes().clear();
                if (nowChooseRecipe.getRules().isEmpty()) {
                    this.ruleLeft.setIcon(null);
                    this.ruleMiddle.setIcon(null);
                    this.ruleRight.setIcon(null);
                    this.ruleLeft.setColorTooltips(null);
                    this.ruleMiddle.setColorTooltips(null);
                    this.ruleRight.setColorTooltips(null);
                } else {
                    String ruleLeft = nowChooseRecipe.getRules().get(0);
                    BufferedImage ruleLeftIcon = getRuleIcon(ruleLeft);
                    if (ruleLeftIcon != null) {
                        this.ruleLeft.setIcon(new ImageIcon(ruleLeftIcon));
                    }
                    this.ruleLeft.setColorTooltips(getRuleTooltip(ruleLeft));
                    if (nowChooseRecipe.getRules().size() > 1) {
                        String ruleMiddle = nowChooseRecipe.getRules().get(1);
                        BufferedImage ruleMiddleIcon = getRuleIcon(ruleMiddle);
                        if (ruleMiddleIcon != null) {
                            this.ruleMiddle.setIcon(new ImageIcon(ruleMiddleIcon));
                        }
                        this.ruleMiddle.setColorTooltips(getRuleTooltip(ruleMiddle));
                    } else {
                        this.ruleMiddle.setIcon(null);
                        this.ruleMiddle.setColorTooltips(null);
                    }
                    if (nowChooseRecipe.getRules().size() > 2) {
                        String ruleRight = nowChooseRecipe.getRules().get(2);
                        BufferedImage ruleRightIcon = getRuleIcon(ruleRight);
                        if (ruleRightIcon != null) {
                            this.ruleRight.setIcon(new ImageIcon(ruleRightIcon));
                        }
                        this.ruleRight.setColorTooltips(getRuleTooltip(ruleRight));
                    } else {
                        this.ruleRight.setIcon(null);
                        this.ruleRight.setColorTooltips(null);
                    }
                }
                // TODO 根据配方的tier显示不同等级的锤子
                String seed = this.seedInput.getText();
                if (seed != null && !seed.isBlank()) {
                    try {
                        long _seed = Long.parseLong(seed);
                        int target = new XoroshiroRandomUtil().calcTarget(_seed, savedRecipe.get(0).toResourceLocationStr());
                        this.targetInput.setText(String.valueOf(target));
                    } catch (NumberFormatException ignored) {
                        this.targetInput.setText("0");
                        log.error(MessageUtil.getMessage("log.func.calc.wrong.seed"));
                    }
                }
                this.targetNowInput.setText("0");
                String target = this.targetInput.getText();
                if (target != null && !"0".equals(target)) {
                    calcResults();
                }
                recipeResultPanel.removeAll();
                this.mainFrame.remove(recipeResultPanel);
                this.outputScrollPane.setVisible(true);
                this.seedInputLabel.setVisible(true);
                this.targetNowInputLabel.setVisible(true);
                this.targetInputLabel.setVisible(true);
                this.buttonOffMaterial.setVisible(true);
                this.buttonMainMaterial.setVisible(true);
                this.buttonScroll.setVisible(true);
                this.ruleLeft.setVisible(true);
                this.ruleMiddle.setVisible(true);
                this.ruleRight.setVisible(true);
                this.mainFrame.revalidate();
                this.mainFrame.repaint();
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
        this.ruleLeft.setVisible(false);
        this.ruleMiddle.setVisible(false);
        this.ruleRight.setVisible(false);
        this.repaint();
    }

    /**
     * TODO 读取UI中的target和rule，调用计算方法完成配方操作计算
     */
    private void calcResults() {
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
                log.warn(MessageUtil.getMessage("log.func.tag.id.not.found", tagId, sourceId));
            } else {
                List<ResourceLocation> rl = new ArrayList<>();
                tags.values().forEach(rl::addAll);
                if (rl.isEmpty()) {
                    log.warn(MessageUtil.getMessage("log.func.tag.id.not.found", tagId, sourceId));
                } else if (rl.size() > 1) {
                    String join = tags.values().stream().map(rll -> rll.stream().map(ResourceLocation::getOriginalPath).collect(Collectors.joining(", "))).collect(Collectors.joining(", "));
                    log.warn(MessageUtil.getMessage("log.func.tag.id.found.more.than.one", tagId, sourceId, join));
                } else {
                    List<String> tagVals = ((Tag) rl.get(0)).getValues();
                    if (tagVals == null) {
                        log.warn(MessageUtil.getMessage("log.func.tag.id.content.has.empty.or.more.than.one", tagId, sourceId));
                    } else {
                        if (tagVals.size() != 1) {
                            String findTfcItem = null;
                            String findMinecraftItem = null;
                            for (String t : tagVals) {
                                if (t.startsWith("tfc:")) {
                                    findTfcItem = t;
                                } else if (t.startsWith("minecraft:")) {
                                    findMinecraftItem = t;
                                }
                            }
                            if (findTfcItem == null && findMinecraftItem == null) {
                                log.warn(MessageUtil.getMessage("log.func.tag.id.content.has.empty.or.more.than.one", tagId, sourceId));
                            } else if (findTfcItem != null) {
                                itemId = findTfcItem;
                            } else {
                                itemId = findMinecraftItem;
                            }
                        } else {
                            itemId = tagVals.get(0);
                        }
                    }
                }
            }
        }
        return itemId;
    }

    /**
     * 通过TagId获取物品Id
     *
     * @param itemId   物品ID
     * @param tagId    TagId
     * @param sourceId 用于日志打印的请求来源ID
     * @return 物品ID
     */
    private static String getItemIdFromTagId(String itemId, String tagId, String sourceId) {
        if (itemId == null && tagId != null) {
            itemId = getItemIdFromTagId(tagId, sourceId);
        }
        return itemId;
    }

    /**
     * 通过TagId获取物品Id
     *
     * @param ingredients 合成配方输入配方
     * @param sourceId    用于日志打印的请求来源ID
     * @return 物品ID
     */
    private static String getItemIdFromTagId(RecipeAnvil.Ingredients ingredients, String sourceId) {
        String itemId = ingredients.getItem();
        if ((ingredients.getItem() == null || ingredients.getItem().isBlank()) && ingredients.getTag() != null) {
            itemId = getItemIdFromTagId(ingredients.getTag(), sourceId);
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
                log.warn(MessageUtil.getMessage("log.func.texture.item.id.not.found", itemId, sourceId));
            } else {
                if (textures.size() > 1) {
                    List<ResourceLocation> texturesTypeItem = new ArrayList<>();
                    List<ResourceLocation> texturesTypeBlock = new ArrayList<>();
                    textures.values().forEach(rll -> rll.forEach(rlr -> {
                        Texture _rlr = (Texture) rlr;
                        if ("item".equals(_rlr.getTextureType())) {
                            texturesTypeItem.add(_rlr);
                        } else if ("block".equals(_rlr.getTextureType())) {
                            texturesTypeBlock.add(_rlr);
                        }
                    }));
                    if (texturesTypeItem.size() == 1) {
                        ResourceLocation found = texturesTypeItem.get(0);
                        log.warn(MessageUtil.getMessage("log.func.texture.item.id.found.more.than.one.with.out.item.prefix", itemId, sourceId));
                        return (Texture) found;
                    } else if (texturesTypeBlock.size() == 1) {
                        ResourceLocation found = texturesTypeBlock.get(0);
                        log.warn(MessageUtil.getMessage("log.func.texture.item.id.found.more.than.one.with.out.block.prefix", itemId, sourceId));
                        return (Texture) found;
                    } else {
                        String join = textures.values().stream().map(rll -> rll.stream().map(ResourceLocation::getOriginalPath).collect(Collectors.joining(", "))).collect(Collectors.joining(", "));
                        log.warn(MessageUtil.getMessage("log.func.texture.item.id.found.more.than.one.2", itemId, sourceId, join));
                    }
                } else {
                    Iterator<String> it = textures.keySet().iterator();
                    //noinspection unused
                    boolean b = it.hasNext();
                    List<ResourceLocation> rll = textures.get(it.next());
                    if (rll.isEmpty()) {
                        log.warn(MessageUtil.getMessage("log.func.texture.item.id.found.more.than.one", itemId, sourceId));
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
                            log.warn(MessageUtil.getMessage("log.func.texture.item.id.found.more.than.one.with.out.item.prefix", itemId, sourceId));
                            return (Texture) found;
                        } else if (texturesTypeBlock.size() == 1) {
                            ResourceLocation found = texturesTypeBlock.get(0);
                            log.warn(MessageUtil.getMessage("log.func.texture.item.id.found.more.than.one.with.out.block.prefix", itemId, sourceId));
                            return (Texture) found;
                        } else {
                            String join = rll.stream().map(ResourceLocation::getOriginalPath).collect(Collectors.joining(", "));
                            log.warn(MessageUtil.getMessage("log.func.texture.item.id.found.more.than.one.2", itemId, sourceId, join));
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
            log.warn(MessageUtil.getMessage("log.func.i18n.use.empty.item.id", sourceId));
            return "";
        }
        String itemIdWithoutNamespace = itemId.replace(":", ".").replace("/", ".");
        String blockNameKey = "block." + itemIdWithoutNamespace;
        String itemNameKey = "item." + itemIdWithoutNamespace;
        Map<String, List<ResourceLocation>> langResources =
                ResourceManager.getResources((namespace, resource) -> resource instanceof Lang rlang &&
                        (blockNameKey.equals(rlang.getFullKey()) || itemNameKey.equals(rlang.getFullKey())));
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
            log.warn(MessageUtil.getMessage("log.func.i18n.item.id.not.found", sourceId, itemIdWithoutNamespace));
        }
        return itemName;
    }

    /**
     * 获取物品显示名称
     *
     * @param localeId 语言key
     * @return 显示名称
     */
    private static String getLocaleText(String localeId) {
        Map<String, List<ResourceLocation>> langResources =
                ResourceManager.getResources((namespace, resource) -> resource instanceof Lang rlang && localeId.equals(rlang.getFullKey()));
        String localeText = "";
        if (!langResources.isEmpty()) {
            for (List<ResourceLocation> ll : langResources.values()) {
                for (ResourceLocation r : ll) {
                    Lang lang = (Lang) r;
                    localeText = lang.getDisplayName();
                    break;
                }
            }
        }
        if (localeText.isBlank()) {
            log.warn(MessageUtil.getMessage("log.func.i18n.locale.id.not.found", localeId));
        }
        return localeText;
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
        List<ResourceLocation> anvilRs = ResourceManager.getResources("tfc", r ->
                r instanceof Texture rr && "gui".equals(rr.getTextureType()) && "anvil".equals(r.getPath()));
        if (anvilRs.isEmpty()) {
            log.error(MessageUtil.getMessage("log.load.no.tfc.anvil.texture"));
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
     * TODO 因为目前没有第二输入物的配方，不清楚input{@link RecipeAnvil.Ingredients}是怎么读取第二个输入物的，好像只有tfc:welding格式才有两个输入
     *
     * @param source 事件来源按钮
     * @return 是否通过筛选，加入面板
     */
    @SuppressWarnings({"ConstantValue", "RedundantIfStatement"})
    private Predicate<ResourceLocation> recipeFilterPredicate(ImageJButton source) {
        return resource -> {
            if (!(resource instanceof RecipeAnvil recipe)) return false;
            if (source == this.buttonScroll) {
                String firstInput = this.buttonMainMaterial.getNowChooseRecipes().isEmpty() ? null :
                        getItemIdFromTagId(this.buttonMainMaterial.getNowChooseRecipes().get(0).getInput(),
                                this.buttonMainMaterial.getNowChooseRecipes().get(0).toResourceLocationStr());
                String secondInput = null;
                if (firstInput == null && secondInput == null)
                    return true;
                String targetFirstInput = getItemIdFromTagId(recipe.getInput(), recipe.toResourceLocationStr());
                String targetSecondInput = null;
                if (firstInput != null && !firstInput.equals(targetFirstInput))
                    return false;
                if (secondInput != null && !secondInput.equals(targetSecondInput))
                    return false;
                return true;
            } else if (source == this.buttonMainMaterial) {
                String output = this.buttonScroll.getNowChooseRecipes().isEmpty() ? null :
                        this.buttonScroll.getNowChooseRecipes().get(0).getResult().gotItemId();
                String secondInput = null;
                if (output == null && secondInput == null)
                    return true;
                String targetOutput = recipe.getResult().gotItemId();
                String targetSecondInput = null;
                if (output != null && !output.equals(targetOutput))
                    return false;
                if (secondInput != null && !secondInput.equals(targetSecondInput))
                    return false;
                return true;
            } else if (source == this.buttonOffMaterial) {
                // 目前没有需要第二个素材的配方
                return false;
            } else {
                return false;
            }
        };
    }

    /**
     * 获取规则显示文本
     *
     * @param ruleName 规则名称
     * @return 显示文本
     */
    private static List<TooltipColorUtil.TooltipColor> getRuleTooltip(String ruleName) {
        String[] s = ruleName.split("_");
        if (s.length < 2) {
            log.warn(MessageUtil.getMessage("log.func.rule.wrong.string.format", ruleName));
        }
        TooltipColorUtil.Builder builder = TooltipColorUtil.builder();
        switch (s[0]) {
            case "hit" -> {
                builder.withText(getLocaleText("tfc.enum.forgestep.hit"), Color.WHITE);
            }
            case "hit_light" -> {
                builder.withText(getLocaleText("tfc.enum.forgestep.hit_light"), Color.WHITE);
            }
            case "hit_medium" -> {
                builder.withText(getLocaleText("tfc.enum.forgestep.hit_medium"), Color.WHITE);
            }
            case "hit_hard" -> {
                builder.withText(getLocaleText("tfc.enum.forgestep.hit_hard"), Color.WHITE);
            }
            case "draw" -> {
                builder.withText(getLocaleText("tfc.enum.forgestep.draw"), Color.WHITE);
            }
            case "punch" -> {
                builder.withText(getLocaleText("tfc.enum.forgestep.punch"), Color.WHITE);
            }
            case "bend" -> {
                builder.withText(getLocaleText("tfc.enum.forgestep.bend"), Color.WHITE);
            }
            case "upset" -> {
                builder.withText(getLocaleText("tfc.enum.forgestep.upset"), Color.WHITE);
            }
            case "shrink" -> {
                builder.withText(getLocaleText("tfc.enum.forgestep.shrink"), Color.WHITE);
            }
            default -> {
                log.warn(MessageUtil.getMessage("log.func.rule.wrong.step", s[0]));
            }
        }
        String order = ruleName.substring(ruleName.indexOf("_") + 1);
        switch (order) {
            case "any" -> {
                builder.withText(" " + getLocaleText("tfc.enum.order.any"), Color.WHITE);
            }
            case "last" -> {
                builder.withText(" " + getLocaleText("tfc.enum.order.last"), Color.WHITE);
            }
            case "not_last" -> {
                builder.withText(" " + getLocaleText("tfc.enum.order.not_last"), Color.WHITE);
            }
            case "second_last" -> {
                builder.withText(" " + getLocaleText("tfc.enum.order.second_last"), Color.WHITE);
            }
            case "third_last" -> {
                builder.withText(" " + getLocaleText("tfc.enum.order.third_last"), Color.WHITE);
            }
            default -> {
                log.warn(MessageUtil.getMessage("log.func.rule.wrong.order", order));
            }
        }
        return builder.build();
    }

    /**
     * 规则名称转换为图案
     *
     * @param ruleName 规则名称
     * @return 规则图案
     */
    private static BufferedImage getRuleIcon(String ruleName) {
        String[] s = ruleName.split("_");
        if (s.length < 2) {
            log.warn(MessageUtil.getMessage("log.func.rule.wrong.string.format", ruleName));
        }
        BufferedImage asset = getTfcAnvilAsset();
        if (asset == null) {
            return null;
        }
        String order = ruleName.substring(ruleName.indexOf("_") + 1);
        BufferedImage frame = null;
        switch (order) {
            case "any" -> {
                frame = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameAnyX(), ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameAnyY(),
                        ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameAnyWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameAnyHeight());
            }
            case "last" -> {
                frame = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameLastX(), ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameLastY(),
                        ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameLastWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameLastHeight());
            }
            case "not_last" -> {
                frame = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameNotLastX(), ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameNotLastY(),
                        ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameNotLastWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameNotLastHeight());
            }
            case "second_last" -> {
                frame = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameNextToLastX(), ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameNextToLastY(),
                        ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameNextToLastWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameNextToLastHeight());
            }
            case "third_last" -> {
                frame = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameThirdFromLastX(), ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameThirdFromLastY(),
                        ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameThirdFromLastWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameThirdFromLastHeight());
            }
            default -> {
                log.warn(MessageUtil.getMessage("log.func.rule.wrong.order", order));
            }
        }
        if (frame != null) {
            BufferedImageOp replaceFrame = new LookupOp(new ColorReplacer(new Color(
                    ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameColorRSrc(),
                    ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameColorGSrc(),
                    ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameColorBSrc(),
                    ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameColorASrc()
            ), new Color(
                    ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameColorR(),
                    ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameColorG(),
                    ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameColorB(),
                    ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameColorA()
            )), null);
            BufferedImageOp replaceFrameDarker = new LookupOp(new ColorReplacer(new Color(
                    ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameDarkerColorRSrc(),
                    ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameDarkerColorGSrc(),
                    ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameDarkerColorBSrc(),
                    ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameDarkerColorASrc()
            ), new Color(
                    ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameDarkerColorR(),
                    ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameDarkerColorG(),
                    ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameDarkerColorB(),
                    ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameDarkerColorA()
            )), null);
            BufferedImageOp replaceFrameLighter = new LookupOp(new ColorReplacer(new Color(
                    ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameLighterColorRSrc(),
                    ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameLighterColorGSrc(),
                    ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameLighterColorBSrc(),
                    ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameLighterColorASrc()
            ), new Color(
                    ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameLighterColorR(),
                    ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameLighterColorG(),
                    ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameLighterColorB(),
                    ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameLighterColorA()
            )), null);
            BufferedImageOp replaceFrameMiddle = new LookupOp(new ColorReplacer(new Color(
                    ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameMiddleColorRSrc(),
                    ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameMiddleColorGSrc(),
                    ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameMiddleColorBSrc(),
                    ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameMiddleColorASrc()
            ), new Color(
                    ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameMiddleColorR(),
                    ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameMiddleColorG(),
                    ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameMiddleColorB(),
                    ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameMiddleColorA()
            )), null);
            frame = replaceFrame.filter(frame, null);
            frame = replaceFrameDarker.filter(frame, null);
            frame = replaceFrameLighter.filter(frame, null);
            frame = replaceFrameMiddle.filter(frame, null);
            frame = scaleGlobally(frame);
            BufferedImage icon = null;
            switch (s[0]) {
                case "hit", "hit_medium" -> {
                    icon = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIHitMediumX(), ConfigUtil.INSTANCE.getAnvilAssetUIHitMediumY(),
                            ConfigUtil.INSTANCE.getAnvilAssetUIHitMediumWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIHitMediumHeight());
                }
                case "hit_light" -> {
                    icon = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIHitLightX(), ConfigUtil.INSTANCE.getAnvilAssetUIHitLightY(),
                            ConfigUtil.INSTANCE.getAnvilAssetUIHitLightWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIHitLightHeight());
                }
                case "hit_hard" -> {
                    icon = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIHitHeavyX(), ConfigUtil.INSTANCE.getAnvilAssetUIHitHeavyY(),
                            ConfigUtil.INSTANCE.getAnvilAssetUIHitHeavyWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIHitHeavyHeight());
                }
                case "draw" -> {
                    icon = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIDrawX(), ConfigUtil.INSTANCE.getAnvilAssetUIDrawY(),
                            ConfigUtil.INSTANCE.getAnvilAssetUIDrawWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIDrawHeight());
                }
                case "punch" -> {
                    icon = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIPunchX(), ConfigUtil.INSTANCE.getAnvilAssetUIPunchY(),
                            ConfigUtil.INSTANCE.getAnvilAssetUIPunchWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIPunchHeight());
                }
                case "bend" -> {
                    icon = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIBendX(), ConfigUtil.INSTANCE.getAnvilAssetUIBendY(),
                            ConfigUtil.INSTANCE.getAnvilAssetUIBendWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIBendHeight());
                }
                case "upset" -> {
                    icon = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIUpsetX(), ConfigUtil.INSTANCE.getAnvilAssetUIUpsetY(),
                            ConfigUtil.INSTANCE.getAnvilAssetUIUpsetWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIUpsetHeight());
                }
                case "shrink" -> {
                    icon = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIShrinkX(), ConfigUtil.INSTANCE.getAnvilAssetUIShrinkY(),
                            ConfigUtil.INSTANCE.getAnvilAssetUIShrinkWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIShrinkHeight());
                }
                default -> {
                    log.warn(MessageUtil.getMessage("log.func.rule.wrong.step", s[0]));
                }
            }
            if (icon != null) {
                double w = ((double) ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameWidth() * ConfigUtil.INSTANCE.getScaleUI()) / icon.getWidth();
                double h = ((double) ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameHeight() * ConfigUtil.INSTANCE.getScaleUI()) / icon.getHeight();
                AffineTransform transform = new AffineTransform();
                transform.setToScale(w, h);
                AffineTransformOp transformOp = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
                BufferedImage iconShrink = new BufferedImage(ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameWidth() * ConfigUtil.INSTANCE.getScaleUI(),
                        ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameHeight() * ConfigUtil.INSTANCE.getScaleUI(), BufferedImage.TYPE_INT_ARGB);
                icon = transformOp.filter(icon, iconShrink);
                Graphics g = frame.getGraphics();
                g.drawImage(icon, ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameX() * ConfigUtil.INSTANCE.getScaleUI(),
                        ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameY() * ConfigUtil.INSTANCE.getScaleUI(), null);
                g.dispose();
            }
        }
        return frame;
    }

    /**
     * 配方按钮点击后选择配方事件
     */
    private class RecipeJButtonMouseAdapter extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            ImageJButton source = (ImageJButton) e.getSource();
            if (source.isEnabled()) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    // 打开合成窗口，显示合成结果列表
                    source.setEnabled(false);
                    openRecipeResultScreen(source);
                    source.setEnabled(true);
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    // 清除合成结果选择
                    source.setEnabled(false);
                    source.setColorTooltips(null);
                    source.getNowChooseRecipes().clear();
                    if (source == buttonScroll) {
                        BufferedImage icon = getButtonScrollIcon(null, true);
                        if (icon != null) {
                            source.setIcon(new ImageIcon(icon));
                        }
                    } else {
                        source.setIcon(null);
                    }
                    source.setEnabled(true);
                }
            }
        }
    }

}
