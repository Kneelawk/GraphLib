package com.kneelawk.transferbeams.datagen;

import java.awt.Color;
import java.awt.image.BufferedImage;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import com.kneelawk.transferbeams.TransferBeamsMod;

public class NodeTextureGen extends AbstractTextureGenerator {
    private static final Color ITEM_CORNER_COLOR = new Color(0xffababab);

    public NodeTextureGen(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generate(TextureGenerator gen) {
        for (DyeColor color : DyeColor.values()) {
            ResourceLocation id = TransferBeamsMod.id("block/" + color.getName() + "_item_transfer_node");
            BufferedImage concrete = loadTexture(new ResourceLocation("block/" + color.getName() + "_concrete"));
            Color colorColor = new Color(concrete.getRGB(0, 0));

            gen.addTexture(id, gfx -> {
                gfx.setPaint(colorColor);
                gfx.drawRect(6, 4, 3, 1);
                gfx.drawRect(4, 6, 1, 3);
                gfx.drawRect(6, 10, 3, 1);
                gfx.drawRect(10, 6, 1, 3);

                gfx.setPaint(ITEM_CORNER_COLOR);
                gfx.drawRect(4, 4, 1, 1);
                gfx.drawRect(4, 10, 1, 1);
                gfx.drawRect(10, 4, 1, 1);
                gfx.drawRect(10, 10, 1, 1);
            });
        }
    }
}
