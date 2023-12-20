package com.kneelawk.transferbeams.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;

import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

import com.kneelawk.transferbeams.TransferBeamsMod;

public class NodeTextureGen extends AbstractTextureGenerator {
    public NodeTextureGen(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generate(TextureGenerator gen) {
        for (DyeColor color : DyeColor.values()) {
            Identifier id = TransferBeamsMod.id("block/" + color.getName() + "_item_transfer_node");
            gen.addTexture(id, gfx -> {
                
            });
        }
    }
}
