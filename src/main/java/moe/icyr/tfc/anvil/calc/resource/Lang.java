package moe.icyr.tfc.anvil.calc.resource;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 语言文件
 * tfc声明的格式为/分隔，原版仅区分namespace，此处语言均使用.分隔
 *
 * @author Icy
 * @since 2023/9/24
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class Lang extends ResourceLocation {

    private static final Pattern takePattern = Pattern.compile("^([a-zA-Z0-9-_]*)(?:\\.([a-zA-Z0-9-_.]*?))?(?:\\.([a-zA-Z0-9-_.]*))?$");

    /**
     * <ul>
     *     <li>advancements.{group}.{key}</li>
     *     <li>attribute.{source}.{key}</li>
     *     <li>biome.{namespace}.{biomeName}</li>
     *     <li>block.{namespace}.{blockName}</li>
     *     <li>container.{containerNameAndKey}</li>
     *     <li>effect.{namespace}.{effectId}</li>
     *     <li>enchantment.{namespace}.{enchantId}</li>
     *     <li>entity.{namespace}.{entityId}</li>
     *     <li>gui.{key}</li>
     *     <li>item.{namespace}.{itemId}</li>
     *     <li>itemGroup.{tag}</li>
     *     <li>stat.{namespace}.{statId}</li>
     * </ul>
     */
    private String fullKey;
    /**
     * en_US, value
     * zh_CN, 值
     */
    private Map<String, String> langValues;

    public Lang() {
        this("", new HashMap<>());
    }

    public Lang(String fullKey, Map<String, String> langValues) {
        this.fullKey = fullKey;
        this.langValues = langValues;
    }

    /**
     * 获取本地化名称
     */
    public @NonNull String getDisplayName() {
        Locale sysLocale = Locale.getDefault();
        String language = sysLocale.getLanguage().toLowerCase();
        String country = sysLocale.getCountry().toLowerCase();
        if (this.langValues.containsKey(language + "_" + country)) {
            String v = this.langValues.get(language + "_" + country);
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        for (Map.Entry<String, String> entry : this.langValues.entrySet()) {
            if (entry.getKey().startsWith(language)) {
                if (entry.getValue() != null && !entry.getValue().isBlank()) {
                    return entry.getValue();
                }
            }
        }
        if (!"en_us".equals(language + "_" + country) && this.langValues.containsKey("en_us")) {
            String v = this.langValues.get("en_us");
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return "";
    }

    /**
     * 获取一级类型名称
     *
     * @return NULLABLE!!!
     */
    public String getTypeStr() {
        Matcher matcher = takePattern.matcher(this.fullKey);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 不知道需不需要提供可空的初始化方案
     */
    @Deprecated
    static <T extends Lang> T parseLang(String fullKey) {
        return parseLang(fullKey, new HashMap<>());
    }

    public static <T extends Lang> T parseLang(String fullKey, String langId, String value) {
        Map<String, String> langVal = new HashMap<>();
        langVal.put(langId, value);
        return parseLang(fullKey, langVal);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Lang> T parseLang(String fullKey, Map<String, String> langVal) {
        Matcher matcher = takePattern.matcher(fullKey);
        if (matcher.matches()) {
            String firstKey = matcher.group(1);
            switch (firstKey) {
                case "advancements" -> {
                    return (T) new Advancements(fullKey, langVal);
                }
                case "attribute" -> {
                    return (T) new Attribute(fullKey, langVal);
                }
                case "biome" -> {
                    return (T) new Biome(fullKey, langVal);
                }
                case "block" -> {
                    return (T) new Block(fullKey, langVal);
                }
                case "container" -> {
                    return (T) new Container(fullKey, langVal);
                }
                case "effect" -> {
                    return (T) new Effect(fullKey, langVal);
                }
                case "enchantment" -> {
                    return (T) new Enchantment(fullKey, langVal);
                }
                case "entity" -> {
                    return (T) new Entity(fullKey, langVal);
                }
                case "gui" -> {
                    return (T) new Gui(fullKey, langVal);
                }
                case "item" -> {
                    return (T) new Item(fullKey, langVal);
                }
                case "itemGroup" -> {
                    return (T) new ItemGroup(fullKey, langVal);
                }
                case "stat" -> {
                    return (T) new Stat(fullKey, langVal);
                }
            }
        }
        return null;
    }

    /**
     * 用来在资源管理器之间动态加载的中间类
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class LangSets extends Lang {
        private List<Lang> storage;
        public LangSets() {
            super();
            this.storage = new ArrayList<>();
        }
    }

    /**
     * 该类型不具有MOD规范性
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Advancements extends Lang {
        public Advancements(String fullKey, Map<String, String> langValues) {
            super(fullKey, langValues);
        }
        public String getGroup() {
            Matcher matcher = takePattern.matcher(super.getFullKey());
            if (matcher.matches()) {
                if (matcher.groupCount() > 1) {
                    return matcher.group(2);
                }
            }
            return null;
        }
        public String getKey() {
            Matcher matcher = takePattern.matcher(super.getFullKey());
            if (matcher.matches()) {
                if (matcher.groupCount() > 2) {
                    return matcher.group(3);
                }
            }
            return null;
        }
    }

    /**
     * 该类型不具有MOD规范性
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Attribute extends Lang {
        public Attribute(String fullKey, Map<String, String> langValues) {
            super(fullKey, langValues);
        }
        public String getSource() {
            Matcher matcher = takePattern.matcher(super.getFullKey());
            if (matcher.matches()) {
                if (matcher.groupCount() > 1) {
                    return matcher.group(2);
                }
            }
            return null;
        }
        public String getKey() {
            Matcher matcher = takePattern.matcher(super.getFullKey());
            if (matcher.matches()) {
                if (matcher.groupCount() > 2) {
                    return matcher.group(3);
                }
            }
            return null;
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Biome extends Lang {
        public Biome(String fullKey, Map<String, String> langValues) {
            super(fullKey, langValues);
        }
        public String getNamespace() {
            Matcher matcher = takePattern.matcher(super.getFullKey());
            if (matcher.matches()) {
                if (matcher.groupCount() > 1) {
                    return matcher.group(2);
                }
            }
            return null;
        }
        public String getBiomeKey() {
            Matcher matcher = takePattern.matcher(super.getFullKey());
            if (matcher.matches()) {
                if (matcher.groupCount() > 2) {
                    return matcher.group(3);
                }
            }
            return null;
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Block extends Lang {
        public Block(String fullKey, Map<String, String> langValues) {
            super(fullKey, langValues);
        }
        public String getNamespace() {
            Matcher matcher = takePattern.matcher(super.getFullKey());
            if (matcher.matches()) {
                if (matcher.groupCount() > 1) {
                    return matcher.group(2);
                }
            }
            return null;
        }
        public String getBlockKey() {
            Matcher matcher = takePattern.matcher(super.getFullKey());
            if (matcher.matches()) {
                if (matcher.groupCount() > 2) {
                    return matcher.group(3);
                }
            }
            return null;
        }
    }

    /**
     * 该类型不具有MOD规范性
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Container extends Lang {
        public Container(String fullKey, Map<String, String> langValues) {
            super(fullKey, langValues);
        }
        public String getContainerName() {
            Matcher matcher = takePattern.matcher(super.getFullKey());
            if (matcher.matches()) {
                if (matcher.groupCount() > 1) {
                    return matcher.group(2);
                }
            }
            return null;
        }
        public String getContainerSubKey() {
            Matcher matcher = takePattern.matcher(super.getFullKey());
            if (matcher.matches()) {
                if (matcher.groupCount() > 2) {
                    return matcher.group(3);
                }
            }
            return null;
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Effect extends Lang {
        public Effect(String fullKey, Map<String, String> langValues) {
            super(fullKey, langValues);
        }
        public String getNamespace() {
            Matcher matcher = takePattern.matcher(super.getFullKey());
            if (matcher.matches()) {
                if (matcher.groupCount() > 1) {
                    return matcher.group(2);
                }
            }
            return null;
        }
        public String getEffectKey() {
            Matcher matcher = takePattern.matcher(super.getFullKey());
            if (matcher.matches()) {
                if (matcher.groupCount() > 2) {
                    return matcher.group(3);
                }
            }
            return null;
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Enchantment extends Lang {
        public Enchantment(String fullKey, Map<String, String> langValues) {
            super(fullKey, langValues);
        }
        public String getNamespace() {
            Matcher matcher = takePattern.matcher(super.getFullKey());
            if (matcher.matches()) {
                if (matcher.groupCount() > 1) {
                    return matcher.group(2);
                }
            }
            return null;
        }
        public String getEnchantmentKey() {
            Matcher matcher = takePattern.matcher(super.getFullKey());
            if (matcher.matches()) {
                if (matcher.groupCount() > 2) {
                    return matcher.group(3);
                }
            }
            return null;
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Entity extends Lang {
        public Entity(String fullKey, Map<String, String> langValues) {
            super(fullKey, langValues);
        }
        public String getNamespace() {
            Matcher matcher = takePattern.matcher(super.getFullKey());
            if (matcher.matches()) {
                if (matcher.groupCount() > 1) {
                    return matcher.group(2);
                }
            }
            return null;
        }
        /**
         * 可能包含状态
         * <ul>
         *     <li>entity.tfc.alpaca</li>
         *     <li>entity.tfc.alpaca.female</li>
         *     <li>entity.tfc.alpaca.male</li>
         * </ul>
         */
        public String getEntityKey() {
            Matcher matcher = takePattern.matcher(super.getFullKey());
            if (matcher.matches()) {
                if (matcher.groupCount() > 2) {
                    return matcher.group(3);
                }
            }
            return null;
        }
    }

    /**
     * 该类型不具有MOD规范性
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Gui extends Lang {
        public Gui(String fullKey, Map<String, String> langValues) {
            super(fullKey, langValues);
        }
        public String getGuiKey() {
            Matcher matcher = takePattern.matcher(super.getFullKey());
            if (matcher.matches()) {
                if (matcher.groupCount() > 1) {
                    return matcher.group(2);
                }
            }
            return null;
        }
        public String getGuiSubKey() {
            Matcher matcher = takePattern.matcher(super.getFullKey());
            if (matcher.matches()) {
                if (matcher.groupCount() > 2) {
                    return matcher.group(3);
                }
            }
            return null;
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Item extends Lang {
        public Item(String fullKey, Map<String, String> langValues) {
            super(fullKey, langValues);
        }
        public String getNamespace() {
            Matcher matcher = takePattern.matcher(super.getFullKey());
            if (matcher.matches()) {
                if (matcher.groupCount() > 1) {
                    return matcher.group(2);
                }
            }
            return null;
        }
        public String getItemKey() {
            Matcher matcher = takePattern.matcher(super.getFullKey());
            if (matcher.matches()) {
                if (matcher.groupCount() > 2) {
                    return matcher.group(3);
                }
            }
            return null;
        }
    }

    /**
     * 该类型不具有MOD规范性
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class ItemGroup extends Lang {
        public ItemGroup(String fullKey, Map<String, String> langValues) {
            super(fullKey, langValues);
        }
        public String getTagKey() {
            Matcher matcher = takePattern.matcher(super.getFullKey());
            if (matcher.matches()) {
                if (matcher.groupCount() > 1) {
                    return matcher.group(2);
                }
            }
            return null;
        }
        public String getTagSubKey() {
            Matcher matcher = takePattern.matcher(super.getFullKey());
            if (matcher.matches()) {
                if (matcher.groupCount() > 2) {
                    return matcher.group(3);
                }
            }
            return null;
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Stat extends Lang {
        public Stat(String fullKey, Map<String, String> langValues) {
            super(fullKey, langValues);
        }
        public String getNamespace() {
            Matcher matcher = takePattern.matcher(super.getFullKey());
            if (matcher.matches()) {
                if (matcher.groupCount() > 1) {
                    return matcher.group(2);
                }
            }
            return null;
        }
        public String getStatKey() {
            Matcher matcher = takePattern.matcher(super.getFullKey());
            if (matcher.matches()) {
                if (matcher.groupCount() > 2) {
                    return matcher.group(3);
                }
            }
            return null;
        }
    }

}
