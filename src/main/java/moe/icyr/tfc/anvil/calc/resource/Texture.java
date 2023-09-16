package moe.icyr.tfc.anvil.calc.resource;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.awt.image.BufferedImage;

/**
 * @author Icy
 * @since 2023/9/14
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Texture extends ResourceLocation {

    private String textureType;
    private BufferedImage img;

    public Texture() {
    }

    public Texture(BufferedImage img) {
        this.img = img;
    }

}
