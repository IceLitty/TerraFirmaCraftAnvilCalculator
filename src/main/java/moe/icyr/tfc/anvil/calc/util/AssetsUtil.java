package moe.icyr.tfc.anvil.calc.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import moe.icyr.tfc.anvil.calc.resource.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Icy
 * @since 2023/9/16
 */
@Slf4j
public class AssetsUtil {

    private static final Pattern pathPattern = Pattern.compile("^(.+)\\..+$");
    private static final Map<Class<? extends ResourceLocation>, Pattern> resourcePathPattern = new HashMap<>() {{
        put(Texture.class, Pattern.compile("^assets/.*?/textures/.*$"));
        put(RecipeAnvil.class, Pattern.compile("^data/.*?/recipes/.*$"));
        put(Tag.class, Pattern.compile("^data/.*?/tags/.*$"));
        put(Lang.class, Pattern.compile("^assets/.*?/lang/.*$"));
    }};

    /**
     * 处理资源包文件读取为对象
     *
     * @param resourcePath 资源文件路径
     * @param resourceData 资源数据
     * @param <T>          资源类型
     * @return 资源对象
     * @throws Exception Just use log.error(e) to print stderr.
     */
    @SuppressWarnings("unchecked")
    public static <T extends ResourceLocation> T readResourceFromData(@NonNull String fileName,
                                                                      @NonNull String resourcePath,
                                                                      byte @NonNull [] resourceData) throws Exception {
        Class<T> anClass = null;
        for (Map.Entry<Class<? extends ResourceLocation>, Pattern> entry : resourcePathPattern.entrySet()) {
            if (entry.getValue().matcher(resourcePath).matches()) {
                anClass = (Class<T>) entry.getKey();
                break;
            }
        }
        if (anClass == null) {
            throw new IllegalArgumentException("Can't parse resource path " + resourcePath + " to a defined resource type.");
        }
        return readResourceFromData(fileName, resourcePath, resourceData, anClass);
    }

    /**
     * 处理资源包文件读取为对象
     *
     * @param resourcePath 资源文件路径
     * @param resourceData 资源数据
     * @param resourceType 资源类型
     * @param <T>          资源类型
     * @return 资源对象
     * @throws Exception Just use log.error(e) to print stderr.
     */
    public static <T extends ResourceLocation> T readResourceFromData(@NonNull String fileName,
                                                                      @NonNull String resourcePath,
                                                                      byte @NonNull [] resourceData,
                                                                      @NonNull Class<T> resourceType) throws Exception {
        if (resourcePath.endsWith("/")) {
            throw new IllegalArgumentException("Can't parse directory to resource.");
        }
        String originalPath = fileName + "!/" + resourcePath;
        // 解析文件路径前缀
        String namespace;
        String minecraftResourceType;
        if (resourcePath.startsWith("data")) {
            // 资源包
            resourcePath = resourcePath.substring(5);
        } else if (resourcePath.startsWith("assets")) {
            // 材质包
            resourcePath = resourcePath.substring(7);
        } else {
            throw new UnsupportedOperationException("Resource file path is not correctly or new version of minecraft? Can not resolve: " + resourcePath);
        }
        int firstPathSeparator = resourcePath.indexOf("/");
        if (firstPathSeparator == -1) {
            throw new IllegalArgumentException("Resource path can't resolve namespace: " + resourcePath);
        }
        namespace = resourcePath.substring(0, firstPathSeparator);
        resourcePath = resourcePath.substring(firstPathSeparator + 1);
        firstPathSeparator = resourcePath.indexOf("/");
        if (firstPathSeparator == -1) {
            throw new IllegalArgumentException("Resource path can't resolve resource type: " + resourcePath);
        }
        minecraftResourceType = resourcePath.substring(0, firstPathSeparator);
        resourcePath = resourcePath.substring(firstPathSeparator + 1);
        String thirdType = null;
        if ("textures".equals(minecraftResourceType)) {
            firstPathSeparator = resourcePath.indexOf("/");
            if (firstPathSeparator == -1) {
                throw new IllegalArgumentException("Resource path can't resolve resource type: " + resourcePath);
            }
            // block colormap entity gui item misc mob_effect models painting particle etc.
            thirdType = resourcePath.substring(0, firstPathSeparator);
            resourcePath = resourcePath.substring(firstPathSeparator + 1);
        } else if ("tags".equals(minecraftResourceType)) {
            firstPathSeparator = resourcePath.indexOf("/");
            if (firstPathSeparator == -1) {
                throw new IllegalArgumentException("Resource path can't resolve resource type: " + resourcePath);
            }
            // blocks entity_types fluids items worldgen etc.
            thirdType = resourcePath.substring(0, firstPathSeparator);
            resourcePath = resourcePath.substring(firstPathSeparator + 1);
        }
        // assets -> blockstates lang models particles sounds textures | data -> advancements loot_tables recipes structures tags worldgen
        // 验证资源类型
        switch (minecraftResourceType) {
            case "recipes" -> {
                if (resourceType != RecipeAnvil.class)
                    throw new IllegalArgumentException("Resource type " + minecraftResourceType + " can't matched with " + RecipeAnvil.class.getSimpleName() + " type!");
            }
            case "textures" -> {
                if (resourceType != Texture.class)
                    throw new IllegalArgumentException("Resource type " + minecraftResourceType + " can't matched with " + Texture.class.getSimpleName() + " type!");
            }
            case "tags" -> {
                if (resourceType != Tag.class)
                    throw new IllegalArgumentException("Resource type " + minecraftResourceType + " can't matched with " + Tag.class.getSimpleName() + " type!");
            }
            case "lang" -> {
                if (resourceType != Lang.class)
                    throw new IllegalArgumentException("Resource type " + minecraftResourceType + " can't matched with " + Lang.class.getSimpleName() + " type!");
            }
        }
        // 去除文件名后缀
        Matcher resPathMatcher = pathPattern.matcher(resourcePath);
        if (resPathMatcher.matches()) {
            resourcePath = resPathMatcher.group(1);
        }
        if (resourceType == RecipeAnvil.class) {
            try {
                String s = new String(resourceData, StandardCharsets.UTF_8);
                T recipe = JsonUtil.INSTANCE.readValue(s, resourceType);
                recipe.setOriginalPath(originalPath);
                recipe.setNamespace(namespace);
                recipe.setPath(resourcePath);
                return recipe;
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Json process error found on " + namespace + ":" + resourcePath + " recipe file.", e);
            }
        } else if (resourceType == Texture.class) {
            try {
                BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(resourceData));
                Constructor<T> constructor = resourceType.getConstructor(BufferedImage.class);
                T t = constructor.newInstance(bufferedImage);
                t.setOriginalPath(originalPath);
                t.setNamespace(namespace);
                t.setPath(resourcePath);
                Method setTextureType = Texture.class.getMethod("setTextureType", String.class);
                setTextureType.invoke(t, thirdType);
                return t;
            } catch (IOException e) {
                throw new IllegalArgumentException("IOException when read image on " + namespace + ":" + resourcePath + ".", e);
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException |
                     InvocationTargetException e) {
                throw new IllegalArgumentException("If software changed something but util missed?", e);
            }
        } else if (resourceType == Tag.class) {
            try {
                String s = new String(resourceData, StandardCharsets.UTF_8);
                T t = JsonUtil.INSTANCE.readValue(s, resourceType);
                t.setOriginalPath(originalPath);
                t.setNamespace(namespace);
                t.setPath(resourcePath);
                Method setTagType = Tag.class.getMethod("setTagType", String.class);
                setTagType.invoke(t, thirdType);
                return t;
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Json process error found on " + namespace + ":" + resourcePath + " tag file.", e);
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new IllegalArgumentException("If software changed something but util missed?", e);
            }
        } else if (resourceType == Lang.class) {
            try {
                String s = new String(resourceData, StandardCharsets.UTF_8);
                Lang.LangSets langSets = new Lang.LangSets();
                langSets.setOriginalPath(originalPath);
                langSets.setNamespace(namespace);
                langSets.setPath(resourcePath);
                String langId = resourcePath;
                List<Lang> storage = langSets.getStorage();
                Map<String, String> langMap = JsonUtil.INSTANCE.readValue(s, new TypeReference<>() {
                });
                langMap.forEach((k, v) -> {
                    boolean found = false;
                    for (Lang stor : storage) {
                        if (k.equals(stor.getFullKey())) {
                            stor.getLangValues().put(langId, v);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        Lang lang = Lang.parseLang(k, langId, v);
                        if (lang != null) {
                            lang.setNamespace(langSets.getNamespace());
                            lang.setPath(k);
                            storage.add(lang);
                        }
                    }
                });
                return (T) langSets;
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Json process error found on " + namespace + ":" + resourcePath + " lang file.", e);
            }
        } else {
            throw new UnsupportedOperationException("Resource type " + resourceType.getName() + " is not supported.");
        }
    }

}
