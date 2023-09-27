package moe.icyr.tfc.anvil.calc.resource;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import moe.icyr.tfc.anvil.calc.util.MessageUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * 资源文件管理池
 *
 * @author Icy
 * @since 2023/9/14
 */
@Slf4j
public class ResourceManager {

    private static final Map<String, String> MODNAME = new HashMap<>();
    private static final Map<String, CopyOnWriteArrayList<ResourceLocation>> POOL = new ConcurrentHashMap<>();

    /**
     * 获取指定条件的资源对象
     *
     * @param namespaceAndResourcePredicate 命名空间和资源条件
     * @return 资源对象列表
     */
    public static @NonNull Map<String, List<ResourceLocation>> getResources(@NonNull BiPredicate<String, ResourceLocation> namespaceAndResourcePredicate) {
        Map<String, List<ResourceLocation>> collection = new HashMap<>();
        for (Map.Entry<String, CopyOnWriteArrayList<ResourceLocation>> entry : POOL.entrySet()) {
            if (entry.getValue() == null) continue;
            Iterator<ResourceLocation> iterator = entry.getValue().stream().iterator();
            while (iterator.hasNext()) {
                ResourceLocation resourceLocation = iterator.next();
                if (namespaceAndResourcePredicate.test(entry.getKey(), resourceLocation)) {
                    if (!collection.containsKey(entry.getKey())) {
                        collection.put(entry.getKey(), new ArrayList<>());
                    }
                    collection.get(entry.getKey()).add(resourceLocation);
                }
            }
        }
        return collection;
    }

    /**
     * 获取指定条件的资源对象
     *
     * @param namespace         命名空间
     * @param resourcePredicate 资源条件
     * @return 资源对象列表
     */
    public static @NonNull List<ResourceLocation> getResources(@NonNull String namespace,
                                                               @NonNull Predicate<ResourceLocation> resourcePredicate) {
        CopyOnWriteArrayList<ResourceLocation> resourceLocations = POOL.get(namespace);
        if (resourceLocations == null) return new ArrayList<>();
        List<ResourceLocation> collection = new ArrayList<>();
        Iterator<ResourceLocation> iterator = resourceLocations.stream().iterator();
        while (iterator.hasNext()) {
            ResourceLocation resourceLocation = iterator.next();
            if (resourcePredicate.test(resourceLocation)) {
                collection.add(resourceLocation);
            }
        }
        return collection;
    }

    /**
     * 存入资源对象至管理池，除已实现的类型外，不检测MC资源ID（命名空间:资源路径）重复，通过path获取时会取第一个
     * 不可以异步调用，会导致特殊类型除重逻辑工作不完全
     *
     * @param resourceLocation 资源对象
     * @return 资源存入成功与否
     */
    public static boolean putResource(ResourceLocation resourceLocation) {
        if (resourceLocation == null || resourceLocation.getNamespace() == null || resourceLocation.getPath() == null)
            return false;
        if (POOL.containsKey(resourceLocation.getNamespace())) {
            CopyOnWriteArrayList<ResourceLocation> pool = POOL.get(resourceLocation.getNamespace());
            // 特殊处理除重逻辑
            if (resourceLocation instanceof Tag tagNew) {
                boolean found = false;
                Iterator<ResourceLocation> iterator = pool.stream().iterator();
                while (iterator.hasNext()) {
                    ResourceLocation r = iterator.next();
                    if (resourceLocation.getPath().equals(r.getPath()) && r instanceof Tag tagExist) {
                        tagExist.getValues().addAll(tagNew.getValues());
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    pool.add(resourceLocation);
                }
                log.debug(MessageUtil.getMessage("log.load.resource.loaded", resourceLocation.toResourceLocationStr(), resourceLocation.getOriginalPath()));
            } else if (resourceLocation instanceof Lang.LangSets langSets) {
                // 解包并合并
                long successCounter = 0;
                for (Lang lang : langSets.getStorage()) {
                    boolean found = false;
                    Iterator<ResourceLocation> iterator = pool.stream().iterator();
                    while (iterator.hasNext()) {
                        ResourceLocation r = iterator.next();
                        if (r instanceof Lang langExist && lang.getFullKey().equals(langExist.getFullKey())) {
                            langExist.getLangValues().putAll(lang.getLangValues());
                            successCounter++;
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        pool.add(lang);
                        successCounter++;
                    }
                }
                log.debug(MessageUtil.getMessage("log.load.resource.lang.loaded", resourceLocation.getOriginalPath(), successCounter, langSets.getStorage().size()));
            } else {
                pool.add(resourceLocation);
                log.debug(MessageUtil.getMessage("log.load.resource.loaded", resourceLocation.toResourceLocationStr(), resourceLocation.getOriginalPath()));
            }
            return true;
        } else {
            // 新插入
            if (resourceLocation instanceof Lang.LangSets langSets) {
                // 解包并合并
                long successCounter = 0;
                POOL.putIfAbsent(resourceLocation.getNamespace(), new CopyOnWriteArrayList<>());
                CopyOnWriteArrayList<ResourceLocation> pool = POOL.get(resourceLocation.getNamespace());
                for (Lang lang : langSets.getStorage()) {
                    boolean found = false;
                    Iterator<ResourceLocation> iterator = pool.stream().iterator();
                    while (iterator.hasNext()) {
                        ResourceLocation r = iterator.next();
                        if (r instanceof Lang langExist && lang.getFullKey().equals(langExist.getFullKey())) {
                            langExist.getLangValues().putAll(lang.getLangValues());
                            successCounter++;
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        pool.add(lang);
                        successCounter++;
                    }
                }
                log.debug(MessageUtil.getMessage("log.load.resource.lang.loaded", resourceLocation.getOriginalPath(), successCounter, langSets.getStorage().size()));
                return true;
            } else {
                boolean success = null == POOL.putIfAbsent(resourceLocation.getNamespace(), new CopyOnWriteArrayList<>(List.of(resourceLocation)));
                if (success)
                    log.debug(MessageUtil.getMessage("log.load.resource.loaded", resourceLocation.toResourceLocationStr(), resourceLocation.getOriginalPath()));
                return success;
            }
        }
    }

    public static void putModDisplayNameWithModId(@NonNull String modId, @NonNull String modDisplayName) {
        MODNAME.put(modId, modDisplayName);
    }

    /**
     * 获取MOD显示名称
     * 存在不可靠风险！
     *
     * @param modId modId
     * @return mod名称
     */
    public static @NonNull String getModDisplayNameByModId(@NonNull String modId) {
        if ("minecraft".equals(modId))
            return "Minecraft";
        String s = MODNAME.get(modId);
        if (s == null || s.isBlank()) {
            return modId;
        } else {
            return s;
        }
    }

}
