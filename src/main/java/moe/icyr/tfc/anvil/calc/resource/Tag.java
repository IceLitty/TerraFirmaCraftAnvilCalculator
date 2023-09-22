package moe.icyr.tfc.anvil.calc.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author Icy
 * @since 2023/9/22
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Tag extends ResourceLocation {

    /**
     * <p>Tag类型</p>
     * <ul>
     *     <li>blocks</li>
     *     <li>entity_types</li>
     *     <li>fluids</li>
     *     <li>items</li>
     *     <li>worldgen</li>
     * </ul>
     */
    private String tagType;

    @JsonProperty("__comment__")
    private String comment;
    private Boolean replace;
    private List<String> values;

    public Tag() {
    }

}
