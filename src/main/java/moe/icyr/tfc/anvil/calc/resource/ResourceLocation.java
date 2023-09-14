package moe.icyr.tfc.anvil.calc.resource;

import lombok.Data;

/**
 * @author Icy
 * @since 2023/9/14
 */
@Data
public abstract class ResourceLocation {

    private String namespace;
    private String path;

    @Override
    public String toString() {
        return namespace + ":" + path;
    }

}
