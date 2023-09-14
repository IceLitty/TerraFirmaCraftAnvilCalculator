package moe.icyr.tfc.anvil.calc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import moe.icyr.tfc.anvil.calc.resource.Recipe;
import moe.icyr.tfc.anvil.calc.resource.ResourceManager;
import moe.icyr.tfc.anvil.calc.ui.MainFrame;
import moe.icyr.tfc.anvil.calc.util.JarUtil;
import moe.icyr.tfc.anvil.calc.util.JsonUtil;
import org.tomlj.Toml;
import org.tomlj.TomlArray;
import org.tomlj.TomlParseResult;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 资源包加载
 *
 * @author Icy
 * @since 2023/9/13
 */
@Slf4j
public class AssetsLoader {

    public void load() {
        File runLocation = new File(MainFrame.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile().getAbsoluteFile();
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
                            loadTfc(mod);
                        } else {
                            // 其他mod
                            loadOtherMods(mod);
                        }
                    }
                }
            }
        }
    }

    private void loadTfc(File mod) {
        Map<String, byte[]> tfcMod;
        try {
            tfcMod = JarUtil.fullyLoadInMemory(mod, name -> name.startsWith("assets/tfc/textures/gui/") || name.startsWith("assets/tfc/textures/item/") || name.startsWith("data/tfc/recipes/anvil/"));
        } catch (IOException e) {
            log.error("Load mod file failed: " + mod.getPath(), e);
            return;
        }
        for (Map.Entry<String, byte[]> entry : tfcMod.entrySet()) {
            if (entry.getKey().startsWith("data/tfc/recipes/anvil/")) {
                Recipe resourceRecipe;
                try {
                    resourceRecipe = JsonUtil.readResourceFromData("tfc", entry.getKey().substring(17, entry.getKey().length() - 5), entry.getValue(), Recipe.class);
                } catch (Exception e) {
                    log.error("Load resource error:", e);
                    continue;
                }
                if (!ResourceManager.putResource(resourceRecipe)) {
                    log.warn("Resource " + resourceRecipe + " can't use.");
                }
            } else if (entry.getKey().startsWith("assets/tfc/textures/")) {
            }
        }
    }

    private void loadOtherMods(File mod) {
    }

}
