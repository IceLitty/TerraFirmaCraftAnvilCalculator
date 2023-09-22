package moe.icyr.tfc.anvil.calc.ui;

import lombok.extern.slf4j.Slf4j;
import moe.icyr.tfc.anvil.calc.AssetsLoader;
import moe.icyr.tfc.anvil.calc.resource.ResourceLocation;
import moe.icyr.tfc.anvil.calc.resource.ResourceManager;
import moe.icyr.tfc.anvil.calc.resource.Texture;
import moe.icyr.tfc.anvil.calc.util.ConfigUtil;
import moe.icyr.tfc.anvil.calc.util.MessageUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
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
        // TODO 还是得先加载铁砧配方再初始化UI
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
        BufferedImage _anvil = asset.getSubimage(ConfigUtil.INSTANCE.getAnvilAssetUIX(), ConfigUtil.INSTANCE.getAnvilAssetUIY(),
                ConfigUtil.INSTANCE.getAnvilAssetUIWidth(), ConfigUtil.INSTANCE.getAnvilAssetUIHeight());
        Graphics g = _anvil.getGraphics();
        // 将背包涂为文本区
        g.setColor(new Color(ConfigUtil.INSTANCE.getAnvilAssetUISlotDarkColorR(), ConfigUtil.INSTANCE.getAnvilAssetUISlotDarkColorG(),
                ConfigUtil.INSTANCE.getAnvilAssetUISlotDarkColorB(), ConfigUtil.INSTANCE.getAnvilAssetUISlotDarkColorA()));
        g.fillRect(ConfigUtil.INSTANCE.getAnvilAssetUIBackpackX(), ConfigUtil.INSTANCE.getAnvilAssetUIBackpackY(),
                ConfigUtil.INSTANCE.getAnvilAssetUIBackpackWidth() - 1, 1);
        g.fillRect(ConfigUtil.INSTANCE.getAnvilAssetUIBackpackX(), ConfigUtil.INSTANCE.getAnvilAssetUIBackpackY(),
                1, ConfigUtil.INSTANCE.getAnvilAssetUIBackpackHeight() - 1);
        g.setColor(new Color(ConfigUtil.INSTANCE.getAnvilAssetUISlotLightColorR(), ConfigUtil.INSTANCE.getAnvilAssetUISlotLightColorG(),
                ConfigUtil.INSTANCE.getAnvilAssetUISlotLightColorB(), ConfigUtil.INSTANCE.getAnvilAssetUISlotLightColorA()));
        g.fillRect(ConfigUtil.INSTANCE.getAnvilAssetUIBackpackX() + 1, ConfigUtil.INSTANCE.getAnvilAssetUIBackpackY() + ConfigUtil.INSTANCE.getAnvilAssetUIBackpackHeight() - 1,
                ConfigUtil.INSTANCE.getAnvilAssetUIBackpackWidth() - 1, 1);
        g.fillRect(ConfigUtil.INSTANCE.getAnvilAssetUIBackpackX() + ConfigUtil.INSTANCE.getAnvilAssetUIBackpackWidth() - 1, ConfigUtil.INSTANCE.getAnvilAssetUIBackpackY() + 1,
                1, ConfigUtil.INSTANCE.getAnvilAssetUIBackpackHeight() - 1);
        g.setColor(new Color(ConfigUtil.INSTANCE.getAnvilAssetUISlotColorR(), ConfigUtil.INSTANCE.getAnvilAssetUISlotColorG(),
                ConfigUtil.INSTANCE.getAnvilAssetUISlotColorB(), ConfigUtil.INSTANCE.getAnvilAssetUISlotColorA()));
        g.fillRect(ConfigUtil.INSTANCE.getAnvilAssetUIBackpackX() + ConfigUtil.INSTANCE.getAnvilAssetUIBackpackWidth() - 1,
                ConfigUtil.INSTANCE.getAnvilAssetUIBackpackY(), 1, 1);
        g.fillRect(ConfigUtil.INSTANCE.getAnvilAssetUIBackpackX(),
                ConfigUtil.INSTANCE.getAnvilAssetUIBackpackY() + ConfigUtil.INSTANCE.getAnvilAssetUIBackpackHeight() - 1, 1, 1);
        g.fillRect(ConfigUtil.INSTANCE.getAnvilAssetUIBackpackX() + 1, ConfigUtil.INSTANCE.getAnvilAssetUIBackpackY() + 1,
                ConfigUtil.INSTANCE.getAnvilAssetUIBackpackWidth() - 2, ConfigUtil.INSTANCE.getAnvilAssetUIBackpackHeight() - 2);
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
        AffineTransform anvilTransform = new AffineTransform();
        anvilTransform.setToScale(ConfigUtil.INSTANCE.getScaleUI(), ConfigUtil.INSTANCE.getScaleUI());
        AffineTransformOp anvilTransformOp = new AffineTransformOp(anvilTransform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        BufferedImage anvil = new BufferedImage(_anvil.getWidth() * ConfigUtil.INSTANCE.getScaleUI(), _anvil.getHeight() * ConfigUtil.INSTANCE.getScaleUI(), BufferedImage.TYPE_INT_ARGB);
        anvil = anvilTransformOp.filter(_anvil, anvil);
        this.setContentPane(new ImagePanel(anvil));
//        JLabel testat = new JLabel("testat");
//        testat.setForeground(Color.RED);
//        this.add(testat);
        // TODO 添加合成物按钮、合成源按钮、target输入框、now输入框、seed输入框、输出文本框控件
    }

}
