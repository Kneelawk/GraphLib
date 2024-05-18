package com.kneelawk.transferbeams.datagen;

import java.util.Optional;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import com.kneelawk.transferbeams.TransferBeamsMod;

import static com.kneelawk.transferbeams.TransferBeamsMod.id;

public class TransferBeamsModelGen extends FabricModelProvider {
    private static final TextureSlot NODE = TextureSlot.create("node");
    private static final ModelTemplate NODE_MODEL =
        new ModelTemplate(Optional.of(id("block/transfer_node")), Optional.empty(), TextureSlot.PARTICLE, NODE);

    public TransferBeamsModelGen(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators gen) {
        for (DyeColor color : DyeColor.values()) {
            ResourceLocation modelId = id("block/" + color.getName() + "_item_transfer_node");
            NODE_MODEL.create(modelId, new TextureMapping().put(TextureSlot.PARTICLE, modelId).put(NODE, modelId),
                gen.modelOutput);
            gen.delegateItemModel(TransferBeamsMod.ITEM_NODE_ITEMS[color.getId()], modelId);
        }
    }

    @Override
    public void generateItemModels(ItemModelGenerators gen) {
        gen.generateFlatItem(TransferBeamsMod.CONFIG_TOOL_ITEM, ModelTemplates.FLAT_ITEM);
        gen.generateFlatItem(TransferBeamsMod.LINK_TOOL_ITEM, ModelTemplates.FLAT_ITEM);
    }
}
