package moe.icyr.tfc.anvil.calc.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author Icy
 * @since 2023/9/13
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Recipe extends ResourceLocation {

    @JsonProperty("__comment__")
    private String comment;
    private String type;
    private Input input;
    private Result result;
    private Integer tier;
    private List<String> rules;
    private Boolean applyForgingBonus;

    @Data
    public static class Input {
        private String tag;
    }

    @Data
    public static class Result {
        private String item;
    }

    @Override
    public String toString() {
        return this.getNamespace() + ":" + this.getPath();
    }

}
