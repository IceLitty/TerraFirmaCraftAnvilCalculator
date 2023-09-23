package moe.icyr.tfc.anvil.calc.resource;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

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
            if (resourceLocation.getClass() == Tag.class) {
                // 特殊处理除重逻辑
                boolean found = false;
                Iterator<ResourceLocation> iterator = pool.stream().iterator();
                while (iterator.hasNext()) {
                    ResourceLocation r = iterator.next();
                    if (resourceLocation.getPath().equals(r.getPath())) {
                        if (resourceLocation.getClass() == Tag.class && r.getClass() == Tag.class) {
                            Tag tagNew = (Tag) resourceLocation;
                            Tag tagExist = (Tag) r;
                            tagExist.getValues().addAll(tagNew.getValues());
                            found = true;
                        }
                        break;
                    }
                }
                if (!found) {
                    pool.add(resourceLocation);
                }
            } else {
                pool.add(resourceLocation);
            }
            log.debug("Resource " + resourceLocation.toResourceLocationStr() + " (" + resourceLocation.getOriginalPath() + ") loaded.");
            return true;
        } else {
            boolean success = null == POOL.putIfAbsent(resourceLocation.getNamespace(), new CopyOnWriteArrayList<>(List.of(resourceLocation)));
            if (success)
                log.debug("Resource " + resourceLocation.toResourceLocationStr() + " (" + resourceLocation.getOriginalPath() + ") loaded.");
            return success;
        }
    }

}
