package moe.icyr.tfc.anvil.calc;

import lombok.extern.slf4j.Slf4j;
import moe.icyr.tfc.anvil.calc.exception.SkipNotUsingResourceLoaded;
import moe.icyr.tfc.anvil.calc.resource.ResourceLocation;
import moe.icyr.tfc.anvil.calc.resource.ResourceManager;
import moe.icyr.tfc.anvil.calc.util.AssetsUtil;
import moe.icyr.tfc.anvil.calc.util.JarUtil;
import moe.icyr.tfc.anvil.calc.util.MessageUtil;
import org.tomlj.Toml;
import org.tomlj.TomlArray;
import org.tomlj.TomlParseResult;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * 资源包加载
 *
 * @author Icy
 * @since 2023/9/13
 */
@Slf4j
public class AssetsLoader {

    private static final Pattern otherModsLoadPattern = Pattern.compile("^(?:assets/.*?/lang/.*|assets/tfc/textures/gui/anvil\\.png|assets/.*?/models/(?:item|block)/.*|assets/.*?/textures/(?:item|items|block|blocks)/.*|data/.*?/recipes/anvil/.*|data/.*?/tags/(?:blocks|items)/.*)$");

    /**
     * 加载mod文件内资源
     */
    public void loadMods(Consumer<String> progressFeedback) {
        // 散装Debug时为项目根路径，打包运行后为exe所在路径
        if (progressFeedback != null)
            progressFeedback.accept(MessageUtil.getMessage("ui.title.loading.resource", 0, 0, ""));
        File runLocation = new File(System.getProperty("user.dir")).getAbsoluteFile();
        log.debug(MessageUtil.getMessage("log.load.work.dir", runLocation));
        File modsFolder = new File(runLocation, "mods");
        if (!modsFolder.exists()) {
            //noinspection unused
            boolean success = modsFolder.mkdir();
        }
        if (modsFolder.exists() && modsFolder.isDirectory() && modsFolder.canRead()) {
            File[] files = modsFolder.listFiles();
            if (files == null || files.length == 0) {
                log.warn(MessageUtil.getMessage("ui.label.no.tfc.jar"));
                return;
            }
            for (int i = 0; i < files.length; i++) {
                File mod = files[i];
                if (progressFeedback != null)
                    progressFeedback.accept(MessageUtil.getMessage("ui.title.loading.resource", i + 1, files.length, ""));
                if (!mod.isFile() || !mod.canRead()) {
                    continue;
                }
                if (!mod.getName().endsWith(".jar") && !mod.getName().endsWith(".zip")) {
                    continue;
                }
                byte[] modsToml;
                try {
                    modsToml = JarUtil.readFileFromJar(mod, "META-INF/mods.toml");
                } catch (FileNotFoundException e) {
                    log.warn(MessageUtil.getMessage("log.load.mod.cant.find.mod.toml", mod.getPath()));
                    // 也许是mc本体
                    loadMods(mod, name -> otherModsLoadPattern.matcher(name).matches(), progressFeedback);
                    continue;
                } catch (IOException e) {
                    log.error(MessageUtil.getMessage("log.load.mod.io.error", mod.getPath()), e);
                    continue;
                }
                TomlParseResult tomlParseResult = Toml.parse(new String(modsToml, StandardCharsets.UTF_8));
                if (!tomlParseResult.hasErrors()) {
                    TomlArray mods = tomlParseResult.getArray("mods");
                    if (mods != null && !mods.isEmpty()) {
                        String modId = mods.getTable(0).getString("modId");
                        String displayName = mods.getTable(0).getString("displayName");
                        if (modId != null && !modId.isBlank() && displayName != null && !displayName.isBlank()) {
                            ResourceManager.putModDisplayNameWithModId(modId, displayName);
                        }
                        loadMods(mod, name -> otherModsLoadPattern.matcher(name).matches(), progressFeedback);
//                        if ("tfc".equals(modId)) {
//                            // 群峦本体
//                            loadMods(mod, name ->
//                                    name.startsWith("assets/tfc/textures/gui/") ||
//                                            name.startsWith("assets/tfc/lang/") ||
//                                            name.startsWith("assets/tfc/textures/item/") ||
//                                            name.startsWith("assets/tfc/textures/block/") ||
//                                            name.startsWith("data/tfc/recipes/anvil/") ||
//                                            name.startsWith("data/tfc/tags/") ||
//                                            name.startsWith("data/forge/tags/"),
//                                    progressFeedback);
//                        } else {
//                            // 其他mod
//                            loadMods(mod, name -> otherModsLoadPattern.matcher(name).matches(), progressFeedback);
//                        }
                    }
                }
            }
        }
    }

    private void loadMods(File mod, Predicate<String> ifFileNeeded, Consumer<String> progressFeedback) {
        Map<String, byte[]> modMod;
        try {
            modMod = JarUtil.fullyLoadInMemory(mod, ifFileNeeded);
        } catch (IOException e) {
            log.error(MessageUtil.getMessage("log.load.mod.entry.failed", mod.getPath()), e);
            return;
        }
        int i = 0, imax = modMod.entrySet().size();
        String modFileName = mod.getName();
        if (modFileName.lastIndexOf("/") != -1) {
            modFileName = modFileName.substring(modFileName.lastIndexOf("/") + 1);
        }
        for (Map.Entry<String, byte[]> entry : modMod.entrySet()) {
            if (progressFeedback != null)
                progressFeedback.accept(MessageUtil.getMessage("ui.title.loading.resource", i + 1, imax, modFileName));
            ResourceLocation resourceLocation;
            try {
                resourceLocation = AssetsUtil.readResourceFromData(mod.getName(), entry.getKey(), entry.getValue());
            } catch (SkipNotUsingResourceLoaded e) {
                log.warn(e.getMessage());
                continue;
            } catch (Exception e) {
                log.error(MessageUtil.getMessage("log.load.mod.resource.error"), e);
                continue;
            }
            if (!ResourceManager.putResource(resourceLocation)) {
                log.warn(MessageUtil.getMessage("log.load.mod.cant.store", resourceLocation));
            }
            i++;
        }
    }

}
