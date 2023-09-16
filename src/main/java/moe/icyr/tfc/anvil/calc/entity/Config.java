package moe.icyr.tfc.anvil.calc.entity;

import lombok.Data;

/**
 * @author Icy
 * @since 2023/9/16
 */
@Data
public class Config {

    private Boolean isResetScreenLocation = false;
    private Integer screenX = 0;
    private Integer screenY = 0;

}
