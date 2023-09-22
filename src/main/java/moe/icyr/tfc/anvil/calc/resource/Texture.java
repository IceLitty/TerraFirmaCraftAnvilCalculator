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

    /**
     * <p>材质类型</p>
     * <ul>
     *     <li>block</li>
     *     <li>colormap</li>
     *     <li>entity</li>
     *     <li>gui</li>
     *     <li>item</li>
     *     <li>misc</li>
     *     <li>mob_effect</li>
     *     <li>models</li>
     *     <li>painting</li>
     *     <li>particle</li>
     * </ul>
     */
    private String textureType;
    private BufferedImage img;

    public Texture() {
    }

    public Texture(BufferedImage img) {
        this.img = img;
    }

}
