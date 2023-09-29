package moe.icyr.tfc.anvil.calc.ui;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import moe.icyr.tfc.anvil.calc.AssetsLoader;
import moe.icyr.tfc.anvil.calc.entity.AnvilFuncStep;
import moe.icyr.tfc.anvil.calc.formatter.RangeIntegerFormat;
import moe.icyr.tfc.anvil.calc.resource.*;
import moe.icyr.tfc.anvil.calc.util.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.LookupOp;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
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
    private ImageJButton targetIcon;
    private ImageJButton targetNowIcon;
    private ImageJButton funcPunchButton;
    private ImageJButton funcBendButton;
    private ImageJButton funcUpsetButton;
    private ImageJButton funcShrinkButton;
    private ImageJButton funcHitLightButton;
    private ImageJButton funcHitMediumButton;
    private ImageJButton funcHitHeavyButton;
    private ImageJButton funcDrawButton;

    /**
     * 程序主面板
     * 若不考虑rules，则计算逻辑具有边际问题，如0-145的范围中，会产生执行操作数后小于0或大于145的情况，但由于有rules限制最后几位操作数，故而导致不可能在极大的目标值时指定减小操作，或极小目标值指定增大操作，一定程度上避免了边际问题
     */
    public MainFrame() throws HeadlessException {
        this.mainFrame = this;
        // 加载配置文件
        log.debug(MessageUtil.getMessage("log.config.loaded", ConfigUtil.INSTANCE));
        // 初始化UI
        this.setSize(ConfigUtil.INSTANCE.getAnvilAssetUIWidth() * ConfigUtil.INSTANCE.getScaleUI() + ConfigUtil.INSTANCE.getMainFrameWidthOffset(),
                ConfigUtil.INSTANCE.getAnvilAssetUIHeight() * ConfigUtil.INSTANCE.getScaleUI() + ConfigUtil.INSTANCE.getMainFrameHeightOffset());
        this.setResizable(false);
        this.setLocationRelativeTo(null); // 屏幕居中
        this.setVisible(true);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        AssetsLoader assetsLoader = new AssetsLoader();
        // 加载MOD资源包材质包
        assetsLoader.loadMods(this::setTitle);
        // 动态处理素材-加载窗体UI
        this.setTitle(MessageUtil.getMessage("ui.title.loading.ui", 2, 2));
        this.loadAnvilUI(this::setTitle);
        // 设置全局Tooltip响应时间
        ToolTipManager.sharedInstance().setInitialDelay(0);
        ToolTipManager.sharedInstance().setDismissDelay(10000);
        this.setTitle(MessageUtil.getMessage("ui.title"));
    }

    /**
     * 加载TFC铁砧UI
     */
    public void loadAnvilUI(Consumer<String> progressFeedback) {
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
            this.setTitle(MessageUtil.getMessage("ui.label.no.tfc.jar"));
            return;
        }
        BufferedImage anvil = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIX(), ConfigUtil.INSTANCE.getAnvilAssetUIY(),
                ConfigUtil.INSTANCE.getAnvilAssetUIWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIHeight());
        Graphics g = anvil.getGraphics();
        // 将背包涂为文本区
        BufferedImage anvilBackpackImg = new BufferedImage(ConfigUtil.INSTANCE.getAnvilAssetUIBackpackWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIBackpackHeight(), BufferedImage.TYPE_INT_ARGB);
        drawSlotUI(anvilBackpackImg);
        g.drawImage(anvilBackpackImg, ConfigUtil.INSTANCE.getAnvilAssetUIBackpackX(), ConfigUtil.INSTANCE.getAnvilAssetUIBackpackY(), null);
        // 放大并加载到背景UI
        g.dispose();
        anvil = scaleGlobally(anvil);
        this.setContentPane(new ImageJPanel(anvil));
        // 绘制技术图标按钮 32x32 to 16x16 * UIScale
        AffineTransform iconTechTransform = new AffineTransform();
        double funcScaleWidth = ((double) ConfigUtil.INSTANCE.getAnvilAssetUITechWidth()) / ConfigUtil.INSTANCE.getAnvilAssetUIDrawWidth() * ConfigUtil.INSTANCE.getScaleUI();
        double funcScaleHeight = ((double) ConfigUtil.INSTANCE.getAnvilAssetUITechHeight()) / ConfigUtil.INSTANCE.getAnvilAssetUIDrawHeight() * ConfigUtil.INSTANCE.getScaleUI();
        int funcWidth = (int) funcScaleWidth * ConfigUtil.INSTANCE.getAnvilAssetUIDrawWidth();
        int funcHeight = (int) funcScaleHeight * ConfigUtil.INSTANCE.getAnvilAssetUIDrawHeight();
        iconTechTransform.setToScale(funcScaleWidth, funcScaleHeight);
        AffineTransformOp iconTechTransformOp = new AffineTransformOp(iconTechTransform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        BufferedImage _iconPunch = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIPunchX(), ConfigUtil.INSTANCE.getAnvilAssetUIPunchY(),
                ConfigUtil.INSTANCE.getAnvilAssetUIPunchWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIPunchHeight());
        BufferedImage iconPunch = new BufferedImage(funcWidth, funcHeight, BufferedImage.TYPE_INT_ARGB);
        iconPunch = iconTechTransformOp.filter(_iconPunch, iconPunch);
        this.funcPunchButton = new ImageJButton(new ImageIcon(iconPunch));
        funcPunchButton.setLocation(ConfigUtil.INSTANCE.getAnvilAssetUITechPunchX() * ConfigUtil.INSTANCE.getScaleUI(),
                ConfigUtil.INSTANCE.getAnvilAssetUITechPunchY() * ConfigUtil.INSTANCE.getScaleUI());
        funcPunchButton.setSize(new Dimension(iconPunch.getWidth(), iconPunch.getHeight()));
        funcPunchButton.setColorTooltips(getRuleTooltip(AnvilFuncStep.PUNCH.getId()));
        funcPunchButton.addActionListener(new FuncAction(AnvilFuncStep.PUNCH, progressFeedback));
        this.add(funcPunchButton);
        BufferedImage _iconBend = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIBendX(), ConfigUtil.INSTANCE.getAnvilAssetUIBendY(),
                ConfigUtil.INSTANCE.getAnvilAssetUIBendWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIBendHeight());
        BufferedImage iconBend = new BufferedImage(funcWidth, funcHeight, BufferedImage.TYPE_INT_ARGB);
        iconBend = iconTechTransformOp.filter(_iconBend, iconBend);
        this.funcBendButton = new ImageJButton(new ImageIcon(iconBend));
        funcBendButton.setLocation(ConfigUtil.INSTANCE.getAnvilAssetUITechBendX() * ConfigUtil.INSTANCE.getScaleUI(),
                ConfigUtil.INSTANCE.getAnvilAssetUITechBendY() * ConfigUtil.INSTANCE.getScaleUI());
        funcBendButton.setSize(new Dimension(iconBend.getWidth(), iconBend.getHeight()));
        funcBendButton.setColorTooltips(getRuleTooltip(AnvilFuncStep.BEND.getId()));
        funcBendButton.addActionListener(new FuncAction(AnvilFuncStep.BEND, progressFeedback));
        this.add(funcBendButton);
        BufferedImage _iconUpset = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIUpsetX(), ConfigUtil.INSTANCE.getAnvilAssetUIUpsetY(),
                ConfigUtil.INSTANCE.getAnvilAssetUIUpsetWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIUpsetHeight());
        BufferedImage iconUpset = new BufferedImage(funcWidth, funcHeight, BufferedImage.TYPE_INT_ARGB);
        iconUpset = iconTechTransformOp.filter(_iconUpset, iconUpset);
        this.funcUpsetButton = new ImageJButton(new ImageIcon(iconUpset));
        funcUpsetButton.setLocation(ConfigUtil.INSTANCE.getAnvilAssetUITechUpsetX() * ConfigUtil.INSTANCE.getScaleUI(),
                ConfigUtil.INSTANCE.getAnvilAssetUITechUpsetY() * ConfigUtil.INSTANCE.getScaleUI());
        funcUpsetButton.setSize(new Dimension(iconUpset.getWidth(), iconUpset.getHeight()));
        funcUpsetButton.setColorTooltips(getRuleTooltip(AnvilFuncStep.UPSET.getId()));
        funcUpsetButton.addActionListener(new FuncAction(AnvilFuncStep.UPSET, progressFeedback));
        this.add(funcUpsetButton);
        BufferedImage _iconShrink = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIShrinkX(), ConfigUtil.INSTANCE.getAnvilAssetUIShrinkY(),
                ConfigUtil.INSTANCE.getAnvilAssetUIShrinkWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIShrinkHeight());
        BufferedImage iconShrink = new BufferedImage(funcWidth, funcHeight, BufferedImage.TYPE_INT_ARGB);
        iconShrink = iconTechTransformOp.filter(_iconShrink, iconShrink);
        this.funcShrinkButton = new ImageJButton(new ImageIcon(iconShrink));
        funcShrinkButton.setLocation(ConfigUtil.INSTANCE.getAnvilAssetUITechShrinkX() * ConfigUtil.INSTANCE.getScaleUI(),
                ConfigUtil.INSTANCE.getAnvilAssetUITechShrinkY() * ConfigUtil.INSTANCE.getScaleUI());
        funcShrinkButton.setSize(new Dimension(iconShrink.getWidth(), iconShrink.getHeight()));
        funcShrinkButton.setColorTooltips(getRuleTooltip(AnvilFuncStep.SHRINK.getId()));
        funcShrinkButton.addActionListener(new FuncAction(AnvilFuncStep.SHRINK, progressFeedback));
        this.add(funcShrinkButton);
        BufferedImage _iconHitLight = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIHitLightX(), ConfigUtil.INSTANCE.getAnvilAssetUIHitLightY(),
                ConfigUtil.INSTANCE.getAnvilAssetUIHitLightWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIHitLightHeight());
        BufferedImage iconHitLight = new BufferedImage(funcWidth, funcHeight, BufferedImage.TYPE_INT_ARGB);
        iconHitLight = iconTechTransformOp.filter(_iconHitLight, iconHitLight);
        this.funcHitLightButton = new ImageJButton(new ImageIcon(iconHitLight));
        funcHitLightButton.setLocation(ConfigUtil.INSTANCE.getAnvilAssetUITechHitLightX() * ConfigUtil.INSTANCE.getScaleUI(),
                ConfigUtil.INSTANCE.getAnvilAssetUITechHitLightY() * ConfigUtil.INSTANCE.getScaleUI());
        funcHitLightButton.setSize(new Dimension(iconHitLight.getWidth(), iconHitLight.getHeight()));
        funcHitLightButton.setColorTooltips(getRuleTooltip(AnvilFuncStep.HIT_LIGHT.getId()));
        funcHitLightButton.addActionListener(new FuncAction(AnvilFuncStep.HIT_LIGHT, progressFeedback));
        this.add(funcHitLightButton);
        BufferedImage _iconHitMedium = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIHitMediumX(), ConfigUtil.INSTANCE.getAnvilAssetUIHitMediumY(),
                ConfigUtil.INSTANCE.getAnvilAssetUIHitMediumWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIHitMediumHeight());
        BufferedImage iconHitMedium = new BufferedImage(funcWidth, funcHeight, BufferedImage.TYPE_INT_ARGB);
        iconHitMedium = iconTechTransformOp.filter(_iconHitMedium, iconHitMedium);
        this.funcHitMediumButton = new ImageJButton(new ImageIcon(iconHitMedium));
        funcHitMediumButton.setLocation(ConfigUtil.INSTANCE.getAnvilAssetUITechHitMediumX() * ConfigUtil.INSTANCE.getScaleUI(),
                ConfigUtil.INSTANCE.getAnvilAssetUITechHitMediumY() * ConfigUtil.INSTANCE.getScaleUI());
        funcHitMediumButton.setSize(new Dimension(iconHitMedium.getWidth(), iconHitMedium.getHeight()));
        funcHitMediumButton.setColorTooltips(getRuleTooltip(AnvilFuncStep.HIT_MEDIUM.getId()));
        funcHitMediumButton.addActionListener(new FuncAction(AnvilFuncStep.HIT_MEDIUM, progressFeedback));
        this.add(funcHitMediumButton);
        BufferedImage _iconHitHeavy = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIHitHeavyX(), ConfigUtil.INSTANCE.getAnvilAssetUIHitHeavyY(),
                ConfigUtil.INSTANCE.getAnvilAssetUIHitHeavyWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIHitHeavyHeight());
        BufferedImage iconHitHeavy = new BufferedImage(funcWidth, funcHeight, BufferedImage.TYPE_INT_ARGB);
        iconHitHeavy = iconTechTransformOp.filter(_iconHitHeavy, iconHitHeavy);
        this.funcHitHeavyButton = new ImageJButton(new ImageIcon(iconHitHeavy));
        funcHitHeavyButton.setLocation(ConfigUtil.INSTANCE.getAnvilAssetUITechHitHeavyX() * ConfigUtil.INSTANCE.getScaleUI(),
                ConfigUtil.INSTANCE.getAnvilAssetUITechHitHeavyY() * ConfigUtil.INSTANCE.getScaleUI());
        funcHitHeavyButton.setSize(new Dimension(iconHitHeavy.getWidth(), iconHitHeavy.getHeight()));
        funcHitHeavyButton.setColorTooltips(getRuleTooltip(AnvilFuncStep.HIT_HARD.getId()));
        funcHitHeavyButton.addActionListener(new FuncAction(AnvilFuncStep.HIT_HARD, progressFeedback));
        this.add(funcHitHeavyButton);
        BufferedImage _iconDraw = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIDrawX(), ConfigUtil.INSTANCE.getAnvilAssetUIDrawY(),
                ConfigUtil.INSTANCE.getAnvilAssetUIDrawWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIDrawHeight());
        BufferedImage iconDraw = new BufferedImage(funcWidth, funcHeight, BufferedImage.TYPE_INT_ARGB);
        iconDraw = iconTechTransformOp.filter(_iconDraw, iconDraw);
        this.funcDrawButton = new ImageJButton(new ImageIcon(iconDraw));
        funcDrawButton.setLocation(ConfigUtil.INSTANCE.getAnvilAssetUITechDrawX() * ConfigUtil.INSTANCE.getScaleUI(),
                ConfigUtil.INSTANCE.getAnvilAssetUITechDrawY() * ConfigUtil.INSTANCE.getScaleUI());
        funcDrawButton.setSize(new Dimension(iconDraw.getWidth(), iconDraw.getHeight()));
        funcDrawButton.setColorTooltips(getRuleTooltip(AnvilFuncStep.DRAW.getId()));
        funcDrawButton.addActionListener(new FuncAction(AnvilFuncStep.DRAW, progressFeedback));
        this.add(funcDrawButton);
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
        buttonScroll.setColorTooltips(getButtonScrollEmptyTooltip(buttonScroll));
        buttonScroll.addMouseListener(new RecipeJButtonMouseAdapter(progressFeedback));
        this.add(buttonScroll);
        // 主合成物按钮
        buttonMainMaterial.setIcon(null);
        buttonMainMaterial.setLocation(ConfigUtil.INSTANCE.getAnvilAssetUIOpenMaterial1ButtonX() * ConfigUtil.INSTANCE.getScaleUI(),
                ConfigUtil.INSTANCE.getAnvilAssetUIOpenMaterial1ButtonY() * ConfigUtil.INSTANCE.getScaleUI());
        buttonMainMaterial.setSize(new Dimension(ConfigUtil.INSTANCE.getAnvilAssetUIOpenMaterial1ButtonWidth() * ConfigUtil.INSTANCE.getScaleUI(),
                ConfigUtil.INSTANCE.getAnvilAssetUIOpenMaterial1ButtonHeight() * ConfigUtil.INSTANCE.getScaleUI()));
        buttonMainMaterial.setColorTooltips(getButtonScrollEmptyTooltip(buttonMainMaterial));
        buttonMainMaterial.addMouseListener(new RecipeJButtonMouseAdapter(progressFeedback));
        this.add(buttonMainMaterial);
        // 副合成物按钮
        buttonOffMaterial.setIcon(null);
        buttonOffMaterial.setLocation(ConfigUtil.INSTANCE.getAnvilAssetUIOpenMaterial2ButtonX() * ConfigUtil.INSTANCE.getScaleUI(),
                ConfigUtil.INSTANCE.getAnvilAssetUIOpenMaterial2ButtonY() * ConfigUtil.INSTANCE.getScaleUI());
        buttonOffMaterial.setSize(new Dimension(ConfigUtil.INSTANCE.getAnvilAssetUIOpenMaterial2ButtonWidth() * ConfigUtil.INSTANCE.getScaleUI(),
                ConfigUtil.INSTANCE.getAnvilAssetUIOpenMaterial2ButtonHeight() * ConfigUtil.INSTANCE.getScaleUI()));
        buttonOffMaterial.setColorTooltips(getButtonScrollEmptyTooltip(buttonOffMaterial));
        buttonOffMaterial.addMouseListener(new RecipeJButtonMouseAdapter(progressFeedback));
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
                .withText(MessageUtil.getMessage("ui.tooltip.target"), ColorPresent.getTooltipItemName())
                .build());
        targetInput.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                // 调整红色箭头
                String target = targetInput.getText();
                if (target != null && !target.isEmpty() && target.replaceAll("\\D", "").length() == target.length()) {
                    int t = Math.max(Math.min(145, Integer.parseInt(target)), 0);
                    targetIcon.setLocation((ConfigUtil.INSTANCE.getAnvilAssetUITargetStartX() + t) * ConfigUtil.INSTANCE.getScaleUI(),
                            ConfigUtil.INSTANCE.getAnvilAssetUITargetStartY() * ConfigUtil.INSTANCE.getScaleUI());
                }
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
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
                    if (seedInput.getText() != null && !seedInput.getText().isBlank() && !buttonScroll.getNowChooseRecipes().isEmpty()) {
                        // TODO 提前统计全部配方的MD5，然后根据固定逆向顺序将target还原成map seed
                    }
                    calcResults(progressFeedback);
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
                .withText(MessageUtil.getMessage("ui.tooltip.target.now"), ColorPresent.getTooltipItemName())
                .build());
        targetNowInput.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                // 调整绿色箭头
                String targetNow = targetNowInput.getText();
                if (targetNow != null && !targetNow.isEmpty() && targetNow.replaceAll("\\D", "").length() == targetNow.length()) {
                    int t = Math.max(Math.min(145, Integer.parseInt(targetNow)), 0);
                    targetNowIcon.setLocation((ConfigUtil.INSTANCE.getAnvilAssetUITargetNowStartX() + t) * ConfigUtil.INSTANCE.getScaleUI(),
                            ConfigUtil.INSTANCE.getAnvilAssetUITargetNowStartY() * ConfigUtil.INSTANCE.getScaleUI());
                }
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
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
                    calcResults(progressFeedback);
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
                .withText(MessageUtil.getMessage("ui.tooltip.map.seed"), ColorPresent.getTooltipItemName())
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
                    int testLong = seed.replaceAll("\\D", "").length();
                    if (seed.startsWith("-") ? testLong != seed.length() - 1 : testLong != seed.length()) {
                        seedInput.setText(seed.startsWith("-") ? "-" + seed.replaceAll("\\D", "") : seed.replaceAll("\\D", ""));
                    }
                    List<RecipeAnvil> savedRecipe = buttonScroll.getNowChooseRecipes();
                    if (!savedRecipe.isEmpty()) {
                        try {
                            // 计算目标值
                            long _seed = Long.parseLong(seed);
                            int target = new XoroshiroRandomUtil().calcTarget(_seed, savedRecipe.get(0).toResourceLocationStr());
                            targetInput.setText(String.valueOf(target));
                            calcResults(progressFeedback);
                        } catch (NumberFormatException ignored) {
                            seedInput.setText("0");
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
        outputArea.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));
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
        // 红绿色箭头指示器
        BufferedImage _targetIcon = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUITargetX(), ConfigUtil.INSTANCE.getAnvilAssetUITargetY(),
                ConfigUtil.INSTANCE.getAnvilAssetUITargetWidth(), ConfigUtil.INSTANCE.getAnvilAssetUITargetHeight());
        _targetIcon = scaleGlobally(_targetIcon);
        this.targetIcon = new ImageJButton(new ImageIcon(_targetIcon));
        targetIcon.setLocation(ConfigUtil.INSTANCE.getAnvilAssetUITargetStartX() * ConfigUtil.INSTANCE.getScaleUI(),
                ConfigUtil.INSTANCE.getAnvilAssetUITargetStartY() * ConfigUtil.INSTANCE.getScaleUI());
        targetIcon.setSize(new Dimension(_targetIcon.getWidth(), _targetIcon.getHeight()));
        this.add(targetIcon);
        BufferedImage _targetNowIcon = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUITargetNowX(), ConfigUtil.INSTANCE.getAnvilAssetUITargetNowY(),
                ConfigUtil.INSTANCE.getAnvilAssetUITargetNowWidth(), ConfigUtil.INSTANCE.getAnvilAssetUITargetNowHeight());
        _targetNowIcon = scaleGlobally(_targetNowIcon);
        this.targetNowIcon = new ImageJButton(new ImageIcon(_targetNowIcon));
        targetNowIcon.setLocation(ConfigUtil.INSTANCE.getAnvilAssetUITargetNowStartX() * ConfigUtil.INSTANCE.getScaleUI(),
                ConfigUtil.INSTANCE.getAnvilAssetUITargetNowStartY() * ConfigUtil.INSTANCE.getScaleUI());
        targetNowIcon.setSize(new Dimension(_targetNowIcon.getWidth(), _targetNowIcon.getHeight()));
        this.add(targetNowIcon);
        // Done
        this.revalidate();
        this.repaint();
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
     * @param sourceButton     事件触发按钮，会根据此按钮读取其他几个按钮的已选配方数据
     * @param progressFeedback 进度反馈
     */
    private void openRecipeResultScreen(ImageJButton sourceButton, Consumer<String> progressFeedback) {
        if (progressFeedback != null)
            progressFeedback.accept(MessageUtil.getMessage("ui.title.loading.resource", 1, 1, ""));
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
        if (progressFeedback != null)
            progressFeedback.accept(MessageUtil.getMessage("ui.title.loading.resource", 0, recipes.size(), ""));
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
                this.targetIcon.setVisible(true);
                this.targetNowIcon.setVisible(true);
                this.funcPunchButton.setVisible(true);
                this.funcDrawButton.setVisible(true);
                this.funcHitHeavyButton.setVisible(true);
                this.funcHitMediumButton.setVisible(true);
                this.funcHitLightButton.setVisible(true);
                this.funcShrinkButton.setVisible(true);
                this.funcUpsetButton.setVisible(true);
                this.funcBendButton.setVisible(true);
                this.mainFrame.revalidate();
                this.mainFrame.repaint();
            });
            recipeResultScrollPanePanel.add(backButton);
        }
        for (int i = 0, recipesSize = recipes.size(); i < recipesSize; i++) {
            if (progressFeedback != null)
                progressFeedback.accept(MessageUtil.getMessage("ui.title.loading.resource", i + 1, recipesSize, ""));
            RecipeAnvil recipe = (RecipeAnvil) recipes.get(i);
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
            final StringBuilder fullRecipeDescStringBuilder = new StringBuilder();
            List<TooltipColorUtil.TooltipColor> fullRecipeDesc;
            TooltipColorUtil.Builder fullRecipeDescBuilder = new TooltipColorUtil.Builder();
            String resultItemName = getItemDisplayName(recipe.getResult().gotItemId(), null, recipe.toResourceLocationStr());
            String inputMainItemName = getItemDisplayName(recipe.getInput().getItem(), recipe.getInput().getTag(), recipe.toResourceLocationStr());
            String inputOffItemName = "";
            boolean canShowRecipeInTooltip = !inputMainItemName.isBlank() || !inputOffItemName.isBlank() || !resultItemName.isBlank();
            if (canShowRecipeInTooltip) {
                fullRecipeDescBuilder.withNewLine();
            }
            if (!inputMainItemName.isBlank()) {
                fullRecipeDescStringBuilder.append(inputMainItemName);
                fullRecipeDescBuilder.withText(inputMainItemName, sourceButton == this.buttonScroll ? ColorPresent.getTooltipItemName() : ColorPresent.getTooltipItemDesc());
            }
            if (!inputOffItemName.isBlank()) {
                fullRecipeDescStringBuilder.append(" + ").append(inputOffItemName);
                fullRecipeDescBuilder.withText(" + ", ColorPresent.getTooltipItemDesc())
                        .withText(inputOffItemName, sourceButton == this.buttonScroll ? ColorPresent.getTooltipItemName() : ColorPresent.getTooltipItemDesc());
            }
            if (!resultItemName.isBlank()) {
                fullRecipeDescStringBuilder.append(" -> ").append(resultItemName);
                fullRecipeDescBuilder.withText(" -> ", ColorPresent.getTooltipItemDesc())
                        .withText(resultItemName, sourceButton == this.buttonScroll ? ColorPresent.getTooltipItemDesc() : ColorPresent.getTooltipItemName());
            }
            if (canShowRecipeInTooltip) {
                fullRecipeDesc = fullRecipeDescBuilder.build();
            } else {
                fullRecipeDesc = null;
            }
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
                    .withText(fullRecipeDesc)
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
                        .withText(fullRecipeDescStringBuilder.isEmpty() ? null : "\n" + fullRecipeDescStringBuilder.toString(), ColorPresent.getTooltipItemDesc())
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
                        this.seedInput.setText("0");
                        log.error(MessageUtil.getMessage("log.func.calc.wrong.seed"));
                    }
                }
                this.targetNowInput.setText("0");
                String target = this.targetInput.getText();
                if (target != null && !"0".equals(target)) {
                    calcResults(progressFeedback);
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
                this.targetIcon.setVisible(true);
                this.targetNowIcon.setVisible(true);
                this.funcPunchButton.setVisible(true);
                this.funcDrawButton.setVisible(true);
                this.funcHitHeavyButton.setVisible(true);
                this.funcHitMediumButton.setVisible(true);
                this.funcHitLightButton.setVisible(true);
                this.funcShrinkButton.setVisible(true);
                this.funcUpsetButton.setVisible(true);
                this.funcBendButton.setVisible(true);
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
        this.targetIcon.setVisible(false);
        this.targetNowIcon.setVisible(false);
        this.funcPunchButton.setVisible(false);
        this.funcDrawButton.setVisible(false);
        this.funcHitHeavyButton.setVisible(false);
        this.funcHitMediumButton.setVisible(false);
        this.funcHitLightButton.setVisible(false);
        this.funcShrinkButton.setVisible(false);
        this.funcUpsetButton.setVisible(false);
        this.funcBendButton.setVisible(false);
        this.repaint();
    }

    /**
     * 计算并打印结果集
     */
    private void calcResults(Consumer<String> progressFeedback) {
        if (progressFeedback != null)
            progressFeedback.accept(MessageUtil.getMessage("ui.title.calculating"));
        String target = this.targetInput.getText();
        String targetNow = this.targetNowInput.getText();
        List<RecipeAnvil> nowChooseRecipes = this.buttonScroll.getNowChooseRecipes();
        if (target != null && !target.isEmpty() && !nowChooseRecipes.isEmpty()) {
            this.outputArea.removeAll();
            RecipeAnvil recipe = nowChooseRecipes.get(0);
            List<String> rules = recipe.getRules();
            // 根据order排序
            rules.sort((r1, r2) -> {
                String order1 = AnvilFuncStep.takeOrderFromKey(r1);
                String order2 = AnvilFuncStep.takeOrderFromKey(r2);
                int order1n = switch (order1 == null ? r1 : order1) {
                    case "last" -> -10;
                    case "not_last" -> 1;
                    case "second_last" -> 5;
                    case "third_last" -> 10;
                    default -> 0;
                };
                int order2n = switch (order2 == null ? r2 : order2) {
                    case "last" -> -10;
                    case "not_last" -> 1;
                    case "second_last" -> 5;
                    case "third_last" -> 10;
                    default -> 0;
                };
                return order2n - order1n;
            });
            List<List<Integer>> resultL = new ArrayList<>();
            List<int[]> _rules = new ArrayList<>();
            CalculatorUtil.convert(rules, new int[rules.size()], _rules, 0);
            for (int[] rule : _rules) {
                int _target = Integer.parseInt(target);
                int _targetNow = 0;
                if (targetNow != null && !targetNow.isEmpty()) {
                    _targetNow = Integer.parseInt(targetNow);
                }
                Integer[] result = null;
                try {
                    result = CalculatorUtil.calc(_targetNow, _target, rule);
                } catch (StackOverflowError e) {
                    log.warn(MessageUtil.getMessage("ui.output.error", targetNow, target, Arrays.toString(rule)));
                }
                if (result != null) {
                    List<Integer> result2 = new ArrayList<>(result.length + rule.length);
                    result2.addAll(Arrays.asList(result));
                    for (int r : rule) {
                        result2.add(r);
                    }
                    resultL.add(result2);
                }
            }
            if (resultL.isEmpty()) {
                JLabel tips = new JLabel();
                log.warn(MessageUtil.getMessage("ui.output.error", targetNow, target, rules));
                this.outputArea.add(tips);
            } else {
                List<Integer> result = null;
                for (List<Integer> r : resultL) {
                    if (result == null) {
                        result = r;
                    } else if (r.size() < result.size()) {
                        result = r;
                    }
                }
                JLabel tips = new JLabel();
                tips.setText(MessageUtil.getMessage("ui.output.message"));
                this.outputArea.add(tips);
                BufferedImage asset = getTfcAnvilAsset();
                if (asset == null)
                    return;
                for (Integer r : result) {
                    AnvilFuncStep anvilFuncStep = AnvilFuncStep.findByVal(r);
                    if (anvilFuncStep == null) {
                        log.error(MessageUtil.getMessage("log.func.rule.wrong.step.val", r));
                        continue;
                    }
                    String step = anvilFuncStep.getId();
                    BufferedImage _icon = switch (anvilFuncStep) {
                        case DRAW ->
                                asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIDrawX(), ConfigUtil.INSTANCE.getAnvilAssetUIDrawY(),
                                        ConfigUtil.INSTANCE.getAnvilAssetUIDrawWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIDrawHeight());
                        case HIT_HARD ->
                                asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIHitHeavyX(), ConfigUtil.INSTANCE.getAnvilAssetUIHitHeavyY(),
                                        ConfigUtil.INSTANCE.getAnvilAssetUIHitHeavyWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIHitHeavyHeight());
                        case HIT, HIT_MEDIUM ->
                                asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIHitMediumX(), ConfigUtil.INSTANCE.getAnvilAssetUIHitMediumY(),
                                        ConfigUtil.INSTANCE.getAnvilAssetUIHitMediumWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIHitMediumHeight());
                        case HIT_LIGHT ->
                                asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIHitLightX(), ConfigUtil.INSTANCE.getAnvilAssetUIHitLightY(),
                                        ConfigUtil.INSTANCE.getAnvilAssetUIHitLightWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIHitLightHeight());
                        case PUNCH ->
                                asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIPunchX(), ConfigUtil.INSTANCE.getAnvilAssetUIPunchY(),
                                        ConfigUtil.INSTANCE.getAnvilAssetUIPunchWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIPunchHeight());
                        case BEND ->
                                asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIBendX(), ConfigUtil.INSTANCE.getAnvilAssetUIBendY(),
                                        ConfigUtil.INSTANCE.getAnvilAssetUIBendWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIBendHeight());
                        case UPSET ->
                                asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIUpsetX(), ConfigUtil.INSTANCE.getAnvilAssetUIUpsetY(),
                                        ConfigUtil.INSTANCE.getAnvilAssetUIUpsetWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIUpsetHeight());
                        case SHRINK ->
                                asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIShrinkX(), ConfigUtil.INSTANCE.getAnvilAssetUIShrinkY(),
                                        ConfigUtil.INSTANCE.getAnvilAssetUIShrinkWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIShrinkHeight());
                    };
                    ImageJButton icon = new ImageJButton(new ImageIcon(_icon));
                    icon.setSize(_icon.getWidth(), _icon.getHeight());
                    icon.setColorTooltips(getRuleTooltip(step));
                    this.outputArea.add(icon);
                }
            }
            this.outputArea.revalidate();
            this.outputArea.repaint();
        }
        if (progressFeedback != null)
            progressFeedback.accept(MessageUtil.getMessage("ui.title"));
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
     * 通过模型ID寻找对应的首个材质ID
     *
     * @param id 模型ID
     * @return 首个材质ID
     */
    private static String getFinallyIdFromModels(@NonNull String id) {
        Map<String, List<ResourceLocation>> models = ResourceManager.getResources((n, r) ->
                r instanceof Model && id.equals(r.toResourceLocationStr()));
        if (models.isEmpty())
            return null;
        for (Map.Entry<String, List<ResourceLocation>> entry : models.entrySet()) {
            for (ResourceLocation rl : entry.getValue()) {
                Model model = (Model) rl;
                if ((model.getParent() == null || !model.getParent().contains(":")) && (model.getTextures() == null || model.getTextures().isEmpty()))
                    continue;
                if (model.getTextures() == null || model.getTextures().isEmpty()) {
                    return getFinallyIdFromModels(model.getParent());
                } else {
                    Map<String, String> textures = model.getTextures();
                    for (Map.Entry<String, String> entry1 : textures.entrySet()) {
                        String textureWithTypeId = entry1.getValue();
                        if (!textureWithTypeId.contains(":")) {
                            return textureWithTypeId;
                        }
                        String namespaceWithSuffix = textureWithTypeId.substring(0, textureWithTypeId.indexOf(":") + 1);
                        String textureWithType = textureWithTypeId.substring(textureWithTypeId.indexOf(":") + 1);
                        if (!textureWithType.contains("/") || !(textureWithType.startsWith("block/") || textureWithType.startsWith("item/"))) {
                            return textureWithTypeId;
                        }
                        return namespaceWithSuffix + textureWithType.substring(textureWithType.indexOf("/") + 1);
                    }
                }
            }
        }
        return null;
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
                    r instanceof Texture rt && ("item".equals(rt.getTextureType()) || "items".equals(rt.getTextureType()) || "block".equals(rt.getTextureType()) || "blocks".equals(rt.getTextureType())) && finalItemId.equals(r.toResourceLocationStr()) && rt.getImg() != null);
            if (textures.isEmpty()) {
                // 可能位于模型描述文件中，需要二次查找
                String textureIdFromModel = getFinallyIdFromModels(itemId);
                if (textureIdFromModel == null) {
                    log.warn(MessageUtil.getMessage("log.func.texture.item.id.not.found.and.not.in.model", itemId, sourceId));
                } else {
                    textures = ResourceManager.getResources((n, r) ->
                            r instanceof Texture rt && ("item".equals(rt.getTextureType()) || "items".equals(rt.getTextureType()) || "block".equals(rt.getTextureType()) || "blocks".equals(rt.getTextureType())) && textureIdFromModel.equals(r.toResourceLocationStr()) && rt.getImg() != null);
                    if (textures.isEmpty()) {
                        log.warn(MessageUtil.getMessage("log.func.texture.item.id.not.found.and.not.in.model", itemId, sourceId));
                    }
                }
            }
            if (textures.isEmpty()) {
                log.warn(MessageUtil.getMessage("log.func.texture.item.id.not.found", itemId, sourceId));
            } else {
                if (textures.size() > 1) {
                    List<ResourceLocation> texturesTypeItem = new ArrayList<>();
                    List<ResourceLocation> texturesTypeBlock = new ArrayList<>();
                    textures.values().forEach(rll -> rll.forEach(rlr -> {
                        Texture _rlr = (Texture) rlr;
                        if ("item".equals(_rlr.getTextureType()) || "items".equals(_rlr.getTextureType())) {
                            texturesTypeItem.add(_rlr);
                        } else if ("block".equals(_rlr.getTextureType()) || "blocks".equals(_rlr.getTextureType())) {
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
                            if ("item".equals(rllr1.getTextureType()) || "items".equals(rllr1.getTextureType())) {
                                texturesTypeItem.add(rllr1);
                            } else if ("block".equals(rllr1.getTextureType()) || "blocks".equals(rllr1.getTextureType())) {
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
     * @param ruleKeyOrStepId 规则名称
     * @return 显示文本
     */
    private static List<TooltipColorUtil.TooltipColor> getRuleTooltip(String ruleKeyOrStepId) {
        AnvilFuncStep anvilFuncStep = AnvilFuncStep.findById(ruleKeyOrStepId);
        TooltipColorUtil.Builder builder = TooltipColorUtil.builder();
        if (anvilFuncStep == null) {
            // 必须包含step和order
            String[] s = ruleKeyOrStepId.split("_");
            if (s.length < 2) {
                log.warn(MessageUtil.getMessage("log.func.rule.wrong.string.format", ruleKeyOrStepId));
            }
            anvilFuncStep = AnvilFuncStep.findByKey(ruleKeyOrStepId);
            if (anvilFuncStep == null) {
                log.warn(MessageUtil.getMessage("log.func.rule.wrong.step", ruleKeyOrStepId));
            } else {
                switch (anvilFuncStep) {
                    case HIT -> builder.withText(getLocaleText("tfc.enum.forgestep.hit"), ColorPresent.getTooltipItemName());
                    case HIT_LIGHT -> builder.withText(getLocaleText("tfc.enum.forgestep.hit_light"), ColorPresent.getTooltipItemName());
                    case HIT_MEDIUM -> builder.withText(getLocaleText("tfc.enum.forgestep.hit_medium"), ColorPresent.getTooltipItemName());
                    case HIT_HARD -> builder.withText(getLocaleText("tfc.enum.forgestep.hit_hard"), ColorPresent.getTooltipItemName());
                    case DRAW -> builder.withText(getLocaleText("tfc.enum.forgestep.draw"), ColorPresent.getTooltipItemName());
                    case PUNCH -> builder.withText(getLocaleText("tfc.enum.forgestep.punch"), ColorPresent.getTooltipItemName());
                    case BEND -> builder.withText(getLocaleText("tfc.enum.forgestep.bend"), ColorPresent.getTooltipItemName());
                    case UPSET -> builder.withText(getLocaleText("tfc.enum.forgestep.upset"), ColorPresent.getTooltipItemName());
                    case SHRINK -> builder.withText(getLocaleText("tfc.enum.forgestep.shrink"), ColorPresent.getTooltipItemName());
                }
            }
            String order = AnvilFuncStep.takeOrderFromKey(ruleKeyOrStepId);
            if (order == null) {
                log.warn(MessageUtil.getMessage("log.func.rule.wrong.order", ruleKeyOrStepId));
            } else {
                switch (order) {
                    case "any" -> builder.withText(" " + getLocaleText("tfc.enum.order.any"), ColorPresent.getTooltipItemName());
                    case "last" -> builder.withText(" " + getLocaleText("tfc.enum.order.last"), ColorPresent.getTooltipItemName());
                    case "not_last" -> builder.withText(" " + getLocaleText("tfc.enum.order.not_last"), ColorPresent.getTooltipItemName());
                    case "second_last" -> builder.withText(" " + getLocaleText("tfc.enum.order.second_last"), ColorPresent.getTooltipItemName());
                    case "third_last" -> builder.withText(" " + getLocaleText("tfc.enum.order.third_last"), ColorPresent.getTooltipItemName());
                    default -> log.warn(MessageUtil.getMessage("log.func.rule.wrong.order", order));
                }
            }
            if (anvilFuncStep != null) {
                if (anvilFuncStep == AnvilFuncStep.HIT) {
                    builder.withNewLine().withText(AnvilFuncStep.HIT_LIGHT.getVal() + "/" + AnvilFuncStep.HIT_MEDIUM.getVal() + "/" + AnvilFuncStep.HIT_HARD.getVal(), ColorPresent.getTooltipItemDesc());
                } else {
                    builder.withNewLine().withText(String.valueOf(anvilFuncStep.getVal()), ColorPresent.getTooltipItemDesc());
                }
            }
            return builder.build();
        } else {
            // 只有step
            switch (anvilFuncStep) {
                case HIT -> builder.withText(getLocaleText("tfc.enum.forgestep.hit"), ColorPresent.getTooltipItemName())
                        .withNewLine().withText(AnvilFuncStep.HIT_LIGHT.getVal() + "/" + AnvilFuncStep.HIT_MEDIUM.getVal() + "/" + AnvilFuncStep.HIT_HARD.getVal(), ColorPresent.getTooltipItemDesc());
                case HIT_LIGHT -> builder.withText(getLocaleText("tfc.enum.forgestep.hit_light"), ColorPresent.getTooltipItemName())
                        .withNewLine().withText(String.valueOf(anvilFuncStep.getVal()), ColorPresent.getTooltipItemDesc());
                case HIT_MEDIUM -> builder.withText(getLocaleText("tfc.enum.forgestep.hit_medium"), ColorPresent.getTooltipItemName())
                        .withNewLine().withText(String.valueOf(anvilFuncStep.getVal()), ColorPresent.getTooltipItemDesc());
                case HIT_HARD -> builder.withText(getLocaleText("tfc.enum.forgestep.hit_hard"), ColorPresent.getTooltipItemName())
                        .withNewLine().withText(String.valueOf(anvilFuncStep.getVal()), ColorPresent.getTooltipItemDesc());
                case DRAW -> builder.withText(getLocaleText("tfc.enum.forgestep.draw"), ColorPresent.getTooltipItemName())
                        .withNewLine().withText(String.valueOf(anvilFuncStep.getVal()), ColorPresent.getTooltipItemDesc());
                case PUNCH -> builder.withText(getLocaleText("tfc.enum.forgestep.punch"), ColorPresent.getTooltipItemName())
                        .withNewLine().withText(String.valueOf(anvilFuncStep.getVal()), ColorPresent.getTooltipItemDesc());
                case BEND -> builder.withText(getLocaleText("tfc.enum.forgestep.bend"), ColorPresent.getTooltipItemName())
                        .withNewLine().withText(String.valueOf(anvilFuncStep.getVal()), ColorPresent.getTooltipItemDesc());
                case UPSET -> builder.withText(getLocaleText("tfc.enum.forgestep.upset"), ColorPresent.getTooltipItemName())
                        .withNewLine().withText(String.valueOf(anvilFuncStep.getVal()), ColorPresent.getTooltipItemDesc());
                case SHRINK -> builder.withText(getLocaleText("tfc.enum.forgestep.shrink"), ColorPresent.getTooltipItemName())
                        .withNewLine().withText(String.valueOf(anvilFuncStep.getVal()), ColorPresent.getTooltipItemDesc());
            }
            return builder.build();
        }
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
        String order = AnvilFuncStep.takeOrderFromKey(ruleName);
        if (order == null) {
            log.warn(MessageUtil.getMessage("log.func.rule.wrong.order", ruleName));
            return new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        }
        BufferedImage frame = null;
        switch (order) {
            case "any" -> frame = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameAnyX(), ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameAnyY(),
                    ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameAnyWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameAnyHeight());
            case "last" -> frame = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameLastX(), ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameLastY(),
                    ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameLastWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameLastHeight());
            case "not_last" -> frame = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameNotLastX(), ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameNotLastY(),
                    ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameNotLastWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameNotLastHeight());
            case "second_last" -> frame = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameNextToLastX(), ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameNextToLastY(),
                    ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameNextToLastWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameNextToLastHeight());
            case "third_last" -> frame = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameThirdFromLastX(), ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameThirdFromLastY(),
                    ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameThirdFromLastWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIRecipeFrameThirdFromLastHeight());
            default -> log.warn(MessageUtil.getMessage("log.func.rule.wrong.order", order));
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
            AnvilFuncStep anvilFuncStep = AnvilFuncStep.findByKey(ruleName);
            if (anvilFuncStep == null) {
                log.warn(MessageUtil.getMessage("log.func.rule.wrong.step", ruleName));
            } else {
                switch (anvilFuncStep) {
                    case HIT, HIT_MEDIUM -> icon = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIHitMediumX(), ConfigUtil.INSTANCE.getAnvilAssetUIHitMediumY(),
                            ConfigUtil.INSTANCE.getAnvilAssetUIHitMediumWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIHitMediumHeight());
                    case HIT_LIGHT -> icon = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIHitLightX(), ConfigUtil.INSTANCE.getAnvilAssetUIHitLightY(),
                            ConfigUtil.INSTANCE.getAnvilAssetUIHitLightWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIHitLightHeight());
                    case HIT_HARD -> icon = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIHitHeavyX(), ConfigUtil.INSTANCE.getAnvilAssetUIHitHeavyY(),
                            ConfigUtil.INSTANCE.getAnvilAssetUIHitHeavyWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIHitHeavyHeight());
                    case DRAW -> icon = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIDrawX(), ConfigUtil.INSTANCE.getAnvilAssetUIDrawY(),
                            ConfigUtil.INSTANCE.getAnvilAssetUIDrawWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIDrawHeight());
                    case PUNCH -> icon = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIPunchX(), ConfigUtil.INSTANCE.getAnvilAssetUIPunchY(),
                            ConfigUtil.INSTANCE.getAnvilAssetUIPunchWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIPunchHeight());
                    case BEND -> icon = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIBendX(), ConfigUtil.INSTANCE.getAnvilAssetUIBendY(),
                            ConfigUtil.INSTANCE.getAnvilAssetUIBendWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIBendHeight());
                    case UPSET -> icon = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIUpsetX(), ConfigUtil.INSTANCE.getAnvilAssetUIUpsetY(),
                            ConfigUtil.INSTANCE.getAnvilAssetUIUpsetWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIUpsetHeight());
                    case SHRINK -> icon = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIShrinkX(), ConfigUtil.INSTANCE.getAnvilAssetUIShrinkY(),
                            ConfigUtil.INSTANCE.getAnvilAssetUIShrinkWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIShrinkHeight());
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
        private Consumer<String> progressFeedback;
        public RecipeJButtonMouseAdapter(Consumer<String> progressFeedback) {
            this.progressFeedback = progressFeedback;
        }
        @Override
        public void mouseClicked(MouseEvent e) {
            ImageJButton source = (ImageJButton) e.getSource();
            if (source.isEnabled()) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    // 打开合成窗口，显示合成结果列表
                    source.setEnabled(false);
                    openRecipeResultScreen(source, this.progressFeedback);
                    if (progressFeedback != null)
                        progressFeedback.accept(MessageUtil.getMessage("ui.title"));
                    source.setEnabled(true);
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    // 清除合成结果选择
                    source.setEnabled(false);
                    source.setColorTooltips(getButtonScrollEmptyTooltip(source));
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

    /**
     * 获取配方按钮和素材按钮的空置提示框
     *
     * @param source 事件来源
     * @return Tooltip
     */
    private List<TooltipColorUtil.TooltipColor> getButtonScrollEmptyTooltip(ImageJButton source) {
        String key;
        if (source == this.buttonMainMaterial) {
            key = "ui.button.choose.main.material.tooltip";
        } else if (source == this.buttonOffMaterial) {
            key = "ui.button.choose.off.material.tooltip";
        } else {
            key = "ui.button.choose.result.tooltip";
        }
        return TooltipColorUtil.builder()
                .withText(MessageUtil.getMessage(key), ColorPresent.getTooltipItemName())
                .build();
    }

    /**
     * 功能区操作按钮，操作targetNow
     */
    private class FuncAction implements ActionListener {
        private final AnvilFuncStep func;
        private final Consumer<String> progressFeedback;
        public FuncAction(AnvilFuncStep func, Consumer<String> progressFeedback) {
            if (func == AnvilFuncStep.HIT) {
                throw new IllegalArgumentException(MessageUtil.getMessage("log.load.function.register.hit"));
            }
            this.func = func;
            this.progressFeedback = progressFeedback;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            String nowText = targetNowInput.getText();
            int now = 0;
            try {
                now = Math.max(Math.min(Integer.parseInt(nowText), 145), 0);
            } catch (NumberFormatException ignored) {}
            if (now + func.getVal() > 145 || now + func.getVal() < 0) {
                return;
            }
            now += func.getVal();
            targetNowInput.setText(String.valueOf(now));
            calcResults(progressFeedback);
        }
    }

}
