package moe.icyr.tfc.anvil.calc.resource;

import lombok.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

/**
 * 资源文件管理池
 *
 * @author Icy
 * @since 2023/9/14
 */
public class ResourceManager {

    private static final Map<String, CopyOnWriteArrayList<ResourceLocation>> POOL = new ConcurrentHashMap<>();

    /**
     * 获取命名空间内全部资源
     *
     * @param namespace 命名空间
     * @return Nullable!!!
     */
    public static List<ResourceLocation> getAllResources(@NonNull String namespace) {
        return POOL.get(namespace);
    }

    /**
     * 获取指定条件的资源对象
     *
     * @param namespace     命名空间
     * @param pathPredicate 资源路径条件
     * @return 资源对象列表
     */
    public static @NonNull List<ResourceLocation> getResources(@NonNull String namespace, @NonNull Predicate<String> pathPredicate) {
        CopyOnWriteArrayList<ResourceLocation> resourceLocations = POOL.get(namespace);
        if (resourceLocations == null) return null;
        List<ResourceLocation> collection = new ArrayList<>();
        Iterator<ResourceLocation> iterator = resourceLocations.stream().iterator();
        while (iterator.hasNext()) {
            ResourceLocation resourceLocation = iterator.next();
            if (pathPredicate.test(resourceLocation.getPath())) {
                collection.add(resourceLocation);
            }
        }
        return collection;
    }

    /**
     * 获取指定资源对象
     *
     * @param namespace 命名空间
     * @param path      资源路径
     * @return Nullable!!!
     */
    public static ResourceLocation getResource(@NonNull String namespace, @NonNull String path) {
        CopyOnWriteArrayList<ResourceLocation> resourceLocations = POOL.get(namespace);
        if (resourceLocations == null) return null;
        Iterator<ResourceLocation> iterator = resourceLocations.stream().iterator();
        while (iterator.hasNext()) {
            ResourceLocation resourceLocation = iterator.next();
            if (path.equals(resourceLocation.getPath())) {
                return resourceLocation;
            }
        }
        return null;
    }

    /**
     * 存入资源对象至管理池
     *
     * @param resourceLocation 资源对象
     * @return 资源存入成功与否
     */
    public static boolean putResource(ResourceLocation resourceLocation) {
        if (resourceLocation == null || resourceLocation.getNamespace() == null || resourceLocation.getPath() == null)
            return false;
        if (POOL.containsKey(resourceLocation.getNamespace())) {
            return POOL.get(resourceLocation.getNamespace()).add(resourceLocation);
        } else {
            return null == POOL.putIfAbsent(resourceLocation.getNamespace(), new CopyOnWriteArrayList<>(List.of(resourceLocation)));
        }
    }

}
