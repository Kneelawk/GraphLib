package com.kneelawk.transferbeams.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

@SuppressWarnings("unused")
public class TransferBeamsDatagen implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(TransferBeamsModelGen::new);
        pack.addProvider(NodeTextureGen::new);
        pack.addProvider(NodeLangGen::new);
        pack.addProvider(TransferBeamsItemTagGen::new);
    }
}
