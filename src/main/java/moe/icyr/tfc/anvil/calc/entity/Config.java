package moe.icyr.tfc.anvil.calc.entity;

import lombok.Data;

/**
 * @author Icy
 * @since 2023/9/16
 */
@Data
public class Config {

    private String mapSeed = "";
    private Boolean isResetScreenLocation = false;
    private Integer screenX = 0;
    private Integer screenY = 0;
    private Integer scaleUI = 2;
    private Integer mainFrameWidthOffset = 16;
    private Integer mainFrameHeightOffset = 38;
    /* Use TFC mc 1.18 ver anvil ui https://github.com/TerraFirmaCraft/TerraFirmaCraft/blob/bba6f10afdabafd49f1c261913d9bae77ca77d27/src/main/resources/assets/tfc/textures/gui/anvil.png */
    /* 素材UI面板位置 */
    private Integer anvilAssetUIX = 0;
    private Integer anvilAssetUIY = 0;
    private Integer anvilAssetUIWidth = 176;
    private Integer anvilAssetUIHeight = 207;
    /* 素材UI面板背景位置 */
    private Integer anvilAssetUIBackgroundX = 3;
    private Integer anvilAssetUIBackgroundY = 3;
    private Integer anvilAssetUIBackgroundWidth = 169;
    private Integer anvilAssetUIBackgroundHeight = 200;
    /* UI面板颜色 */
    private Integer anvilAssetUIForegroundColorR = 198;
    private Integer anvilAssetUIForegroundColorG = 198;
    private Integer anvilAssetUIForegroundColorB = 198;
    private Integer anvilAssetUIForegroundColorA = 255;
    /* UI格子阴影色 */
    private Integer anvilAssetUISlotDarkColorR = 55;
    private Integer anvilAssetUISlotDarkColorG = 55;
    private Integer anvilAssetUISlotDarkColorB = 55;
    private Integer anvilAssetUISlotDarkColorA = 255;
    /* UI格子颜色 */
    private Integer anvilAssetUISlotColorR = 139;
    private Integer anvilAssetUISlotColorG = 139;
    private Integer anvilAssetUISlotColorB = 139;
    private Integer anvilAssetUISlotColorA = 255;
    /* UI格子高亮色 */
    private Integer anvilAssetUISlotLightColorR = 255;
    private Integer anvilAssetUISlotLightColorG = 255;
    private Integer anvilAssetUISlotLightColorB = 255;
    private Integer anvilAssetUISlotLightColorA = 255;
    /* 素材UI背包格子位置 */
    private Integer anvilAssetUIBackpackX = 7;
    private Integer anvilAssetUIBackpackY = 124;
    private Integer anvilAssetUIBackpackWidth = 162;
    private Integer anvilAssetUIBackpackHeight = 76;
    /* 素材UI冲压位置 */
    private Integer anvilAssetUIPunchX = 0;
    private Integer anvilAssetUIPunchY = 224;
    private Integer anvilAssetUIPunchWidth = 32;
    private Integer anvilAssetUIPunchHeight = 32;
    /* 素材UI弯曲位置 */
    private Integer anvilAssetUIBendX = 32;
    private Integer anvilAssetUIBendY = 224;
    private Integer anvilAssetUIBendWidth = 32;
    private Integer anvilAssetUIBendHeight = 32;
    /* 素材UI镦锻位置 */
    private Integer anvilAssetUIUpsetX = 64;
    private Integer anvilAssetUIUpsetY = 224;
    private Integer anvilAssetUIUpsetWidth = 32;
    private Integer anvilAssetUIUpsetHeight = 32;
    /* 素材UI收缩位置 */
    private Integer anvilAssetUIShrinkX = 96;
    private Integer anvilAssetUIShrinkY = 224;
    private Integer anvilAssetUIShrinkWidth = 32;
    private Integer anvilAssetUIShrinkHeight = 32;
    /* 素材UI轻击位置 */
    private Integer anvilAssetUIHitLightX = 128;
    private Integer anvilAssetUIHitLightY = 224;
    private Integer anvilAssetUIHitLightWidth = 32;
    private Integer anvilAssetUIHitLightHeight = 32;
    /* 素材UI击打位置 */
    private Integer anvilAssetUIHitMediumX = 160;
    private Integer anvilAssetUIHitMediumY = 224;
    private Integer anvilAssetUIHitMediumWidth = 32;
    private Integer anvilAssetUIHitMediumHeight = 32;
    /* 素材UI重击位置 */
    private Integer anvilAssetUIHitHeavyX = 192;
    private Integer anvilAssetUIHitHeavyY = 224;
    private Integer anvilAssetUIHitHeavyWidth = 32;
    private Integer anvilAssetUIHitHeavyHeight = 32;
    /* 素材UI牵拉位置 */
    private Integer anvilAssetUIDrawX = 224;
    private Integer anvilAssetUIDrawY = 224;
    private Integer anvilAssetUIDrawWidth = 32;
    private Integer anvilAssetUIDrawHeight = 32;
    /* 素材UI冲压按钮位置(16x16) */
    private Integer anvilAssetUITechPunchX = 89;
    private Integer anvilAssetUITechPunchY = 50;
    /* 素材UI弯曲按钮位置(16x16) */
    private Integer anvilAssetUITechBendX = 107;
    private Integer anvilAssetUITechBendY = 50;
    /* 素材UI镦锻按钮位置(16x16) */
    private Integer anvilAssetUITechUpsetX = 89;
    private Integer anvilAssetUITechUpsetY = 68;
    /* 素材UI收缩按钮位置(16x16) */
    private Integer anvilAssetUITechShrinkX = 107;
    private Integer anvilAssetUITechShrinkY = 68;
    /* 素材UI轻击按钮位置(16x16) */
    private Integer anvilAssetUITechHitLightX = 53;
    private Integer anvilAssetUITechHitLightY = 50;
    /* 素材UI击打按钮位置(16x16) */
    private Integer anvilAssetUITechHitMediumX = 71;
    private Integer anvilAssetUITechHitMediumY = 50;
    /* 素材UI重击按钮位置(16x16) */
    private Integer anvilAssetUITechHitHeavyX = 53;
    private Integer anvilAssetUITechHitHeavyY = 68;
    /* 素材UI牵拉按钮位置(16x16) */
    private Integer anvilAssetUITechDrawX = 71;
    private Integer anvilAssetUITechDrawY = 68;
    /* UI配方框架颜色 */
    private Integer anvilAssetUIRecipeFrameColorRSrc = 216;
    private Integer anvilAssetUIRecipeFrameColorGSrc = 216;
    private Integer anvilAssetUIRecipeFrameColorBSrc = 216;
    private Integer anvilAssetUIRecipeFrameColorASrc = 255;
    private Integer anvilAssetUIRecipeFrameColorR = 0;
    private Integer anvilAssetUIRecipeFrameColorG = 130;
    private Integer anvilAssetUIRecipeFrameColorB = 43;
    private Integer anvilAssetUIRecipeFrameColorA = 255;
    /* UI配方框架阴影色 */
    private Integer anvilAssetUIRecipeFrameDarkerColorRSrc = 174;
    private Integer anvilAssetUIRecipeFrameDarkerColorGSrc = 174;
    private Integer anvilAssetUIRecipeFrameDarkerColorBSrc = 174;
    private Integer anvilAssetUIRecipeFrameDarkerColorASrc = 255;
    private Integer anvilAssetUIRecipeFrameDarkerColorR = 0;
    private Integer anvilAssetUIRecipeFrameDarkerColorG = 104;
    private Integer anvilAssetUIRecipeFrameDarkerColorB = 35;
    private Integer anvilAssetUIRecipeFrameDarkerColorA = 255;
    /* UI配方框架高亮色 */
    private Integer anvilAssetUIRecipeFrameLighterColorRSrc = 255;
    private Integer anvilAssetUIRecipeFrameLighterColorGSrc = 255;
    private Integer anvilAssetUIRecipeFrameLighterColorBSrc = 255;
    private Integer anvilAssetUIRecipeFrameLighterColorASrc = 255;
    private Integer anvilAssetUIRecipeFrameLighterColorR = 0;
    private Integer anvilAssetUIRecipeFrameLighterColorG = 146;
    private Integer anvilAssetUIRecipeFrameLighterColorB = 49;
    private Integer anvilAssetUIRecipeFrameLighterColorA = 255;
    /* UI配方框架过渡色 */
    private Integer anvilAssetUIRecipeFrameMiddleColorRSrc = 243;
    private Integer anvilAssetUIRecipeFrameMiddleColorGSrc = 243;
    private Integer anvilAssetUIRecipeFrameMiddleColorBSrc = 243;
    private Integer anvilAssetUIRecipeFrameMiddleColorASrc = 255;
    private Integer anvilAssetUIRecipeFrameMiddleColorR = 0;
    private Integer anvilAssetUIRecipeFrameMiddleColorG = 153;
    private Integer anvilAssetUIRecipeFrameMiddleColorB = 51;
    private Integer anvilAssetUIRecipeFrameMiddleColorA = 255;
    /* 素材UI配方框架末尾位置 */
    private Integer anvilAssetUIRecipeFrameLastX = 198;
    private Integer anvilAssetUIRecipeFrameLastY = 0;
    private Integer anvilAssetUIRecipeFrameLastWidth = 20;
    private Integer anvilAssetUIRecipeFrameLastHeight = 22;
    /* 素材UI配方框架倒数第二位置 */
    private Integer anvilAssetUIRecipeFrameNextToLastX = 198;
    private Integer anvilAssetUIRecipeFrameNextToLastY = 22;
    private Integer anvilAssetUIRecipeFrameNextToLastWidth = 20;
    private Integer anvilAssetUIRecipeFrameNextToLastHeight = 22;
    /* 素材UI配方框架倒数第三位置 */
    private Integer anvilAssetUIRecipeFrameThirdFromLastX = 198;
    private Integer anvilAssetUIRecipeFrameThirdFromLastY = 44;
    private Integer anvilAssetUIRecipeFrameThirdFromLastWidth = 20;
    private Integer anvilAssetUIRecipeFrameThirdFromLastHeight = 22;
    /* 素材UI配方框架非最末位置 */
    private Integer anvilAssetUIRecipeFrameNotLastX = 198;
    private Integer anvilAssetUIRecipeFrameNotLastY = 66;
    private Integer anvilAssetUIRecipeFrameNotLastWidth = 20;
    private Integer anvilAssetUIRecipeFrameNotLastHeight = 22;
    /* 素材UI配方框架任意位置 */
    private Integer anvilAssetUIRecipeFrameAnyX = 198;
    private Integer anvilAssetUIRecipeFrameAnyY = 88;
    private Integer anvilAssetUIRecipeFrameAnyWidth = 20;
    private Integer anvilAssetUIRecipeFrameAnyHeight = 22;
    /* 素材UI配方框架空框架位置 */
    private Integer anvilAssetUIRecipeFrameNotAnyX = 198;
    private Integer anvilAssetUIRecipeFrameNotAnyY = 110;
    private Integer anvilAssetUIRecipeFrameNotAnyWidth = 20;
    private Integer anvilAssetUIRecipeFrameNotAnyHeight = 22;
    /* 素材UI配方框架放置位置1 */
    private Integer anvilAssetUIRecipeFramePos1X = 59;
    private Integer anvilAssetUIRecipeFramePos1Y = 7;
    /* 素材UI配方框架放置位置2 */
    private Integer anvilAssetUIRecipeFramePos2X = 78;
    private Integer anvilAssetUIRecipeFramePos2Y = 7;
    /* 素材UI配方框架放置位置3 */
    private Integer anvilAssetUIRecipeFramePos3X = 97;
    private Integer anvilAssetUIRecipeFramePos3Y = 7;
    /* 素材UI配方框架内部相对位置和大小 */
    private Integer anvilAssetUIRecipeFrameX = 5;
    private Integer anvilAssetUIRecipeFrameY = 3;
    private Integer anvilAssetUIRecipeFrameWidth = 10;
    private Integer anvilAssetUIRecipeFrameHeight = 10;
    /* 素材UI当前进度指示器位置 */
    private Integer anvilAssetUITargetNowX = 176;
    private Integer anvilAssetUITargetNowY = 0;
    private Integer anvilAssetUITargetNowWidth = 5;
    private Integer anvilAssetUITargetNowHeight = 5;
    /* 素材UI当前进度指示器起始第0的位置 */
    private Integer anvilAssetUITargetNowStartX = 13;
    private Integer anvilAssetUITargetNowStartY = 100;
    /* 素材UI当前进度指示器结束第145的位置 */
    private Integer anvilAssetUITargetNowEndX = 158;
    private Integer anvilAssetUITargetNowEndY = 100;
    /* 素材UI进度指示器位置 */
    private Integer anvilAssetUITargetX = 181;
    private Integer anvilAssetUITargetY = 0;
    private Integer anvilAssetUITargetWidth = 5;
    private Integer anvilAssetUITargetHeight = 5;
    /* 素材UI进度指示器起始第0的位置 */
    private Integer anvilAssetUITargetStartX = 13;
    private Integer anvilAssetUITargetStartY = 94;
    /* 素材UI进度指示器结束第145的位置 */
    private Integer anvilAssetUITargetEndX = 158;
    private Integer anvilAssetUITargetEndY = 94;
    /* 素材UI空按钮位置 */
    private Integer anvilAssetUIButtonX = 218;
    private Integer anvilAssetUIButtonY = 0;
    private Integer anvilAssetUIButtonWidth = 18;
    private Integer anvilAssetUIButtonHeight = 18;
    /* 素材UI彩色卷轴图标位置 */
    private Integer anvilAssetUIScrollX = 236;
    private Integer anvilAssetUIScrollY = 0;
    private Integer anvilAssetUIScrollWidth = 16;
    private Integer anvilAssetUIScrollHeight = 16;
    /* 素材UI打开合成配方按钮位置 */
    private Integer anvilAssetUIOpenRecipeButtonX = 20;
    private Integer anvilAssetUIOpenRecipeButtonY = 39;
    private Integer anvilAssetUIOpenRecipeButtonWidth = 18;
    private Integer anvilAssetUIOpenRecipeButtonHeight = 18;
    /* 素材UI打开素材1按钮位置(18x18) */
    private Integer anvilAssetUIOpenMaterial1ButtonX = 30;
    private Integer anvilAssetUIOpenMaterial1ButtonY = 67;
    private Integer anvilAssetUIOpenMaterial1ButtonWidth = 18;
    private Integer anvilAssetUIOpenMaterial1ButtonHeight = 18;
    /* 素材UI打开素材2按钮位置(18x18) */
    private Integer anvilAssetUIOpenMaterial2ButtonX = 12;
    private Integer anvilAssetUIOpenMaterial2ButtonY = 67;
    private Integer anvilAssetUIOpenMaterial2ButtonWidth = 18;
    private Integer anvilAssetUIOpenMaterial2ButtonHeight = 18;
    /* 素材UI输入当前值输入框位置 */
    private Integer anvilAssetUIInputTargetNowX = 128;
    private Integer anvilAssetUIInputTargetNowY = 86;
    private Integer anvilAssetUIInputTargetNowWidth = 18;
    private Integer anvilAssetUIInputTargetNowHeight = 7;
    /* 素材UI输入目标值输入框位置 */
    private Integer anvilAssetUIInputTargetX = 146;
    private Integer anvilAssetUIInputTargetY = 86;
    private Integer anvilAssetUIInputTargetWidth = 18;
    private Integer anvilAssetUIInputTargetHeight = 7;
    /* 素材UI输入种子输入框位置 */
    private Integer anvilAssetUIInputSeedX = 12;
    private Integer anvilAssetUIInputSeedY = 86;
    private Integer anvilAssetUIInputSeedWidth = 111;
    private Integer anvilAssetUIInputSeedHeight = 7;
    /* Tooltip Margin */
    private Integer tooltipMargin = 4;
    private Integer tooltipScale = 2;
    private Float tooltipFontScale = 1.5f;
    /* Tooltip背景及外边框颜色 */
    private Integer tooltipBackgroundColorR = 16;
    private Integer tooltipBackgroundColorG = 1;
    private Integer tooltipBackgroundColorB = 16;
    private Integer tooltipBackgroundColorA = 255;
    /* Tooltip边框颜色 */
    private Integer tooltipBorderColorR = 36;
    private Integer tooltipBorderColorG = 1;
    private Integer tooltipBorderColorB = 91;
    private Integer tooltipBorderColorA = 255;
    /* Tooltip主文本颜色 */
    private Integer tooltipNamedTextColorR = 252;
    private Integer tooltipNamedTextColorG = 252;
    private Integer tooltipNamedTextColorB = 252;
    private Integer tooltipNamedTextColorA = 255;
    /* Tooltip副文本颜色 */
    private Integer tooltipDescTextColorR = 84;
    private Integer tooltipDescTextColorG = 84;
    private Integer tooltipDescTextColorB = 84;
    private Integer tooltipDescTextColorA = 255;
    /* TooltipModId文本颜色 */
    private Integer tooltipModIdColorR = 84;
    private Integer tooltipModIdColorG = 84;
    private Integer tooltipModIdColorB = 252;
    private Integer tooltipModIdColorA = 255;

}
