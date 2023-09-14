package moe.icyr.tfc.anvil.calc.entity;

import lombok.Data;

/**
 * @author Icy
 * @since 2023/9/13
 */
@Data
public class ModsToml {

    private Mods mods;

    @Data
    public static class Mods {
        private String modId;
    }

}
