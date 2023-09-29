package moe.icyr.tfc.anvil.calc.resource;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * @author Icy
 * @since 2023/9/29
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Model extends ResourceLocation {

    private String parent;
    private Map<String, String> textures;

}
