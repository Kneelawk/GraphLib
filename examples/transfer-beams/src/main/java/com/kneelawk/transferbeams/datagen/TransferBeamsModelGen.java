package com.kneelawk.transferbeams.datagen;

import java.util.Optional;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;

import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.model.BlockStateModelGenerator;
import net.minecraft.data.client.model.Model;
import net.minecraft.data.client.model.Models;
import net.minecraft.data.client.model.Texture;
import net.minecraft.data.client.model.TextureKey;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

import com.kneelawk.transferbeams.TransferBeamsMod;

import static com.kneelawk.transferbeams.TransferBeamsMod.id;

public class TransferBeamsModelGen extends FabricModelProvider {
    private static final TextureKey NODE = TextureKey.of("node");
    private static final Model NODE_MODEL =
        new Model(Optional.of(id("block/transfer_node")), Optional.empty(), TextureKey.PARTICLE, NODE);

    public TransferBeamsModelGen(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator gen) {
        for (DyeColor color : DyeColor.values()) {
            Identifier modelId = id("block/" + color.getName() + "_item_transfer_node");
            NODE_MODEL.upload(modelId, new Texture().put(TextureKey.PARTICLE, modelId).put(NODE, modelId),
                gen.modelCollector);
            gen.registerParentedItemModel(TransferBeamsMod.ITEM_NODE_ITEMS[color.getId()], modelId);
        }
    }

    @Override
    public void generateItemModels(ItemModelGenerator gen) {
        gen.register(TransferBeamsMod.CONFIG_TOOL_ITEM, Models.SINGLE_LAYER_ITEM);
    }
}
