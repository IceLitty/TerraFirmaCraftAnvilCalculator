package moe.icyr.tfc.anvil.calc.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <a href="https://terrafirmacraft.github.io/Documentation/1.18.x/data/recipes/#anvil-working">WIKI</a>
 *
 * @author Icy
 * @since 2023/9/13
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RecipeAnvil extends ResourceLocation {

    @JsonProperty("__comment__")
    private String comment;
    private String type;
    private Ingredients input;
    private ItemStack result;
    private Integer tier;
    private List<String> rules;
    private Boolean applyForgingBonus;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Ingredients {
        private String item;
        private String tag;
        private transient List<Tag> tagCache = new ArrayList<>();
        private String type;
        /**
         * <ul>
         *     <li>tfc:has_trait</li>
         *     <li>tfc:lacks_trait</li>
         * </ul>
         */
        private String trait;
        /**
         * <ul>
         *     <li>tfc:heatable</li>
         * </ul>
         */
        private Integer minTemp;
        /**
         * <ul>
         *     <li>tfc:heatable</li>
         * </ul>
         */
        private Integer maxTemp;
        /**
         * 使用type的额外参数
         */
        private Ingredient ingredient;
        /**
         * 使用type的额外参数
         * tfc:fluid_item
         */
        private Ingredient fluidIngredient;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Ingredient {
        /**
         * 仅接受物品的筛选
         * <ul>
         *     <li>tfc:not_rotten</li>
         *     <li>tfc:has_trait</li>
         *     <li>tfc:lacks_trait</li>
         *     <li>tfc:heatable</li>
         * </ul>
         */
        private String item;
        /**
         * <ul>
         *     <li>tfc:not</li>
         * </ul>
         */
        private String type;
        /**
         * <ul>
         *     <li>tfc:fluid_item</li>
         * </ul>
         */
        private String fluid;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FluidIngredient {
        private Integer amount;
        private Ingredient ingredient;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemStack {
        private String item;
        private Integer count;
        private Stack stack;
        private List<Map<String, Object>> modifiers;
        private transient Texture itemTextureCache;
        public ItemStack(String item) {
            this.item = item;
        }
        public String gotItemId() {
            return item != null && !item.isBlank() ? item : (stack == null ? null : stack.item);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Stack {
        private String item;
        private Integer count;
    }

}
