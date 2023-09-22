package moe.icyr.tfc.anvil.calc.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Icy
 * @since 2023/9/13
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RecipeAnvil extends ResourceLocation {

    @JsonProperty("__comment__")
    private String comment;
    private String type;
    private Ingredient ingredient;
    private Input input;
    private Result result;
    private Integer tier;
    private List<String> rules;
    private Boolean applyForgingBonus;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Ingredient {
        private String tag;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Input {
        private String tag;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Result {
        private String item;
        private Integer count;
    }

}
