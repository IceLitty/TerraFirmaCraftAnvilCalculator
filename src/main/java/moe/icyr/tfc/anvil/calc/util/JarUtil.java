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
            throw new FileNotFoundException("Not found " + fileFullName + " in " + jarFile.getPath() + ".");
        }
        return os.toByteArray();
    }

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
