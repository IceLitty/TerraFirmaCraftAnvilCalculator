package moe.icyr.tfc.anvil.calc;

import lombok.extern.slf4j.Slf4j;
import moe.icyr.tfc.anvil.calc.resource.ResourceLocation;
import moe.icyr.tfc.anvil.calc.resource.ResourceManager;
import moe.icyr.tfc.anvil.calc.util.AssetsUtil;
import moe.icyr.tfc.anvil.calc.util.JarUtil;
import org.tomlj.Toml;
import org.tomlj.TomlArray;
import org.tomlj.TomlParseResult;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
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

    private static final Pattern otherModsLoadPattern = Pattern.compile("^(assets/.*?/textures/item/.*|data/.*?/recipes/.*|data/.*?/tags/.*)$");

    /**
     * 加载mod文件内资源
     */
    public void loadMods() {
        // 散装Debug时为项目根路径，打包运行后为exe所在路径
        File runLocation = new File(System.getProperty("user.dir")).getAbsoluteFile();
        log.debug("Application working directory: " + runLocation);
        File modsFolder = new File(runLocation, "mods");
        if (!modsFolder.exists()) {
            modsFolder.mkdir();
        }
        if (modsFolder.exists() && modsFolder.isDirectory() && modsFolder.canRead()) {
            File[] files = modsFolder.listFiles();
            if (files == null || files.length == 0) {
                log.warn("Their are no jar file in mods folder! Can't load TFC assets.");
                return;
            }
            for (File mod : files) {
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
                    log.error("Not found META-INF/mods.toml file in mod jar: " + mod.getPath());
                    continue;
                } catch (IOException e) {
                    log.error("Error when loading mod file: " + mod.getPath(), e);
                    continue;
                }
                TomlParseResult tomlParseResult = Toml.parse(new String(modsToml, StandardCharsets.UTF_8));
                if (!tomlParseResult.hasErrors()) {
                    TomlArray mods = tomlParseResult.getArray("mods");
                    if (mods != null && mods.size() > 0) {
                        String modId = mods.getTable(0).getString("modId");
                        if ("tfc".equals(modId)) {
                            // 群峦本体
                            loadMods(mod, name ->
                                    name.startsWith("assets/tfc/textures/gui/") ||
                                            name.startsWith("assets/tfc/textures/item/") ||
                                            name.startsWith("data/tfc/recipes/anvil/") ||
                                            name.startsWith("data/tfc/tags/") ||
                                            name.startsWith("data/forge/tags/"));
                        } else {
                            // 其他mod
                            loadMods(mod, name -> otherModsLoadPattern.matcher(name).matches());
                        }
                    }
                }
            }
        }
    }

    private void loadMods(File mod, Predicate<String> ifFileNeeded) {
        Map<String, byte[]> modMod;
        try {
            modMod = JarUtil.fullyLoadInMemory(mod, ifFileNeeded);
        } catch (IOException e) {
            log.error("Load mod file failed: " + mod.getPath(), e);
            return;
        }
        for (Map.Entry<String, byte[]> entry : modMod.entrySet()) {
            ResourceLocation resourceLocation;
            try {
                resourceLocation = AssetsUtil.readResourceFromData(mod.getName(), entry.getKey(), entry.getValue());
            } catch (Exception e) {
                log.error("Load resource error:", e);
                continue;
            }
            if (!ResourceManager.putResource(resourceLocation)) {
                log.warn("Resource " + resourceLocation + " can't use.");
            }
        }
    }

}
