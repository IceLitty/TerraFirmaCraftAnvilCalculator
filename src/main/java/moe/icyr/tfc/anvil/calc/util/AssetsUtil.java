package moe.icyr.tfc.anvil.calc.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import moe.icyr.tfc.anvil.calc.resource.Recipe;
import moe.icyr.tfc.anvil.calc.resource.ResourceLocation;
import moe.icyr.tfc.anvil.calc.resource.Texture;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Icy
 * @since 2023/9/16
 */
@Slf4j
public class AssetsUtil {

    private static final Pattern pathPattern = Pattern.compile("^(.+)\\..+$");
    private static final Map<Class<? extends ResourceLocation>, Pattern> resourcePathPattern = new HashMap<>() {{
        put(Recipe.class, Pattern.compile("^data/.*?/recipes/.*$"));
        put(Texture.class, Pattern.compile("^assets/.*?/textures/.*$"));
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
    public static <T extends ResourceLocation> T readResourceFromData(@NonNull String resourcePath,
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
        return readResourceFromData(resourcePath, resourceData, anClass);
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
    public static <T extends ResourceLocation> T readResourceFromData(@NonNull String resourcePath,
                                                                      byte @NonNull [] resourceData,
                                                                      @NonNull Class<T> resourceType) throws Exception {
        if (resourcePath.endsWith("/")) {
            throw new IllegalArgumentException("Can't parse directory to resource.");
        }
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
        resourcePath = resourcePath.substring(firstPathSeparator + 1); // TODO 验证下MC逻辑是否需要移除该层？
        // TODO 也顺带验证下recipe的ID是否正确，可以通过tfc debug mc进行断点验证
        // TODO 根据recipes里的路径来看，材质还需要移除一层，有专门的目录层级
        String textureType = null;
        if ("textures".equals(minecraftResourceType)) {
            firstPathSeparator = resourcePath.indexOf("/");
            if (firstPathSeparator == -1) {
                throw new IllegalArgumentException("Resource path can't resolve resource type: " + resourcePath);
            }
            textureType = resourcePath.substring(0, firstPathSeparator);
            resourcePath = resourcePath.substring(firstPathSeparator + 1);
        }
        // 验证资源类型
        switch (minecraftResourceType) {
            case "recipes" -> {
                if (resourceType != Recipe.class)
                    throw new IllegalArgumentException("Resource type " + minecraftResourceType + " can't matched with " + Recipe.class.getSimpleName() + " type!");
            }
            case "textures" -> {
                if (resourceType != Texture.class)
                    throw new IllegalArgumentException("Resource type " + minecraftResourceType + " can't matched with " + Texture.class.getSimpleName() + " type!");
            }
        }
        // 去除文件名后缀
        Matcher resPathMatcher = pathPattern.matcher(resourcePath);
        if (resPathMatcher.matches()) {
            resourcePath = resPathMatcher.group(1);
        }
        if (resourceType == Recipe.class) {
            try {
                String s = new String(resourceData, StandardCharsets.UTF_8);
                T recipe = JsonUtil.INSTANCE.readValue(s, resourceType);
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
                t.setNamespace(namespace);
                t.setPath(resourcePath);
                Method setTextureType = Texture.class.getMethod("setTextureType", String.class);
                setTextureType.invoke(t, textureType);
                return t;
            } catch (IOException e) {
                throw new IllegalArgumentException("IOException when read image on " + namespace + ":" + resourcePath + ".", e);
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException |
                     InvocationTargetException e) {
                throw new IllegalArgumentException("If software changed something but util missed?", e);
            }
        } else {
            throw new UnsupportedOperationException("Resource type " + resourceType.getName() + " is not supported.");
        }
    }

    public static ResourceLocation forgeIngot2ModItemTexture(@NonNull String forgeIngotId) {
        // (itemId) forge:ingots/bismuth_bronze -> (itemTextureId) tfc:metal/ingot/bismuth_bronze
        if (!forgeIngotId.startsWith("forge:ingots/")) return null;
        // TODO 等校验完材质ID是否需要移除item/前缀后才能继续写逻辑
        return null;
    }

}
