package com.kneelawk.transferbeams.datagen;

import java.awt.Color;
import java.awt.image.BufferedImage;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;

import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

import com.kneelawk.transferbeams.TransferBeamsMod;

public class NodeTextureGen extends AbstractTextureGenerator {
    private static final Color ITEM_CORNER_COLOR = new Color(0xffababab);

    public NodeTextureGen(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generate(TextureGenerator gen) {
        for (DyeColor color : DyeColor.values()) {
            Identifier id = TransferBeamsMod.id("block/" + color.getName() + "_item_transfer_node");
            BufferedImage concrete = loadTexture(new Identifier("block/" + color.getName() + "_concrete"));
            Color colorColor = new Color(concrete.getRGB(0, 0));

            gen.addTexture(id, gfx -> {
                gfx.setPaint(colorColor);
                gfx.drawRect(5, 4, 6, 0);
                gfx.drawRect(4, 5, 0, 6);
                gfx.drawRect(5, 12, 6, 0);
                gfx.drawRect(12, 5, 0, 6);

                gfx.setPaint(ITEM_CORNER_COLOR);
                gfx.drawRect(4, 4, 0, 0);
                gfx.drawRect(4, 12, 0, 0);
                gfx.drawRect(12, 4, 0, 0);
                gfx.drawRect(12, 12, 0, 0);
            });
        }
    }
}
