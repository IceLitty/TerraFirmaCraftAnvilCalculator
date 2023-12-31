package moe.icyr.tfc.anvil.calc.util;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Icy
 * @since 2023/9/13
 */
@Slf4j
public class JarUtil {

    /**
     * 读取单个文件
     *
     * @param jarFile      jar文件
     * @param fileFullName 文件路径
     * @return 字节数组
     * @throws IOException IO异常
     */
    public static byte[] readFileFromJar(@NonNull File jarFile, @NonNull String fileFullName) throws IOException {
        ByteArrayOutputStream os = null;
        try (ZipInputStream is = new ZipInputStream(new FileInputStream(jarFile), StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = is.getNextEntry()) != null) {
                if (fileFullName.equals(entry.getName()) && !entry.isDirectory()) {
                    os = new ByteArrayOutputStream();
                    int len;
                    byte[] cache = new byte[1024];
                    while ((len = is.read(cache)) != -1) {
                        os.write(cache, 0, len);
                    }
                    break;
                }
            }
        }
        if (os == null) {
            throw new FileNotFoundException(MessageUtil.getMessage("log.load.jar.file.not.found", fileFullName, jarFile.getPath()));
        }
        return os.toByteArray();
    }

    /**
     * 加载进内存
     *
     * @param jarFile  jar文件
     * @param contains 文件名筛选，若为NULL则不进行筛选
     * @return Map<文件名, 字节数组>
     * @throws IOException IO异常
     */
    public static Map<String, byte[]> fullyLoadInMemory(@NonNull File jarFile, Predicate<String> contains) throws IOException {
        Map<String, byte[]> zip = new HashMap<>();
        try (ZipInputStream is = new ZipInputStream(new FileInputStream(jarFile), StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = is.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    if (contains != null && !contains.test(entry.getName())) {
                        continue;
                    }
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    int len;
                    byte[] cache = new byte[1024];
                    while ((len = is.read(cache)) != -1) {
                        os.write(cache, 0, len);
                    }
                    zip.put(entry.getName(), os.toByteArray());
                }
            }
        }
        return zip;
    }

}
