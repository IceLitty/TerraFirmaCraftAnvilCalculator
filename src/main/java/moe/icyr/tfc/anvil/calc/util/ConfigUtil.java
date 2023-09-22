package moe.icyr.tfc.anvil.calc.util;

import moe.icyr.tfc.anvil.calc.entity.Config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author Icy
 * @since 2023/9/16
 */
public class ConfigUtil {

    public static final Config INSTANCE;

    static {
        try {
            INSTANCE = load();
        } catch (IOException e) {
            throw new IllegalArgumentException("Parse config file is error, maybe it corrupted?", e);
        }
    }

    public static Config load() throws IOException {
        // 散装Debug时为项目根路径，打包运行后为exe所在路径
        File runLocation = new File(System.getProperty("user.dir")).getAbsoluteFile();
        File confFolder = new File(runLocation, "conf");
        if (!confFolder.exists()) {
            confFolder.mkdir();
        }
        if (confFolder.exists() && confFolder.isDirectory() && confFolder.canRead()) {
            File configFile = new File(confFolder, "config.conf");
            if (configFile.exists() && configFile.isFile() && configFile.canRead()) {
                return JsonUtil.INSTANCE.readValue(configFile, Config.class);
            } else {
                if (configFile.exists()) {
                    configFile.renameTo(new File(confFolder, "config.conf.bak"));
                }
                configFile = new File(confFolder, "config.conf");
                Config defaultConfig = new Config();
                JsonUtil.INSTANCE.writeValue(configFile, defaultConfig);
                return defaultConfig;
            }
        }
        throw new FileNotFoundException("Config directory can't be created.");
    }

}
