package com.kneelawk.graphlib.impl.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.v1.event.UniverseModifyInitializer;
import com.kneelawk.graphlib.api.v1.graph.GraphUniverse;
import com.kneelawk.graphlib.api.v1.graph.UniverseModifierRegistry;
import com.kneelawk.graphlib.impl.Constants;

public class UniverseModifierRegistryImpl implements UniverseModifierRegistry {
    private final Map<Identifier, List<Modify>> individualModifiers = new HashMap<>();
    private final List<ModifyAll> allModifiers = new ArrayList<>();

    private UniverseModifierRegistryImpl() {
    }

    public void preBuild(Identifier universeId, GraphUniverse.Builder builder) {
        List<Modify> modifyList = individualModifiers.get(universeId);
        if (modifyList != null) {
            for (Modify modify : modifyList) {
                modify.modify(builder);
            }
        }

        for (ModifyAll modifyAll : allModifiers) {
            modifyAll.modify(universeId, builder);
        }
    }

    @Override
    public void modify(@NotNull Identifier universeId, @NotNull Modify modifier) {
        List<Modify> modifyList = individualModifiers.computeIfAbsent(universeId, k -> new ArrayList<>());
        modifyList.add(modifier);
    }

    @Override
    public void modifyAll(@NotNull ModifyAll modifier) {
        allModifiers.add(modifier);
    }

    public static UniverseModifierRegistryImpl setup() {
        UniverseModifierRegistryImpl impl = new UniverseModifierRegistryImpl();

        List<UniverseModifyInitializer> modifyInitializers = FabricLoader.getInstance()
            .getEntrypoints(Constants.UNIVERSE_MODIFY_INITIALIZER, UniverseModifyInitializer.class);

        for (UniverseModifyInitializer modifyInitializer : modifyInitializers) {
            modifyInitializer.register(impl);
        }

        return impl;
    }
}
