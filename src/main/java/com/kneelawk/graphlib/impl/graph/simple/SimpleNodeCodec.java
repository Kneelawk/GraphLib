package com.kneelawk.graphlib.impl.graph.simple;

// Translated from 2xsaiko's HCTM-Base WireNetworkState code:
// https://github.com/2xsaiko/hctm-base/blob/119df440743543b8b4979b450452d73f2c3c4c47/src/main/kotlin/common/wire/WireNetworkState.kt

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import com.kneelawk.graphlib.api.node.BlockNode;
import com.kneelawk.graphlib.api.node.BlockNodeDecoder;
import com.kneelawk.graphlib.api.node.BlockNodeFactory;
import com.kneelawk.graphlib.api.node.LegacyBlockNodeDecoder;
import com.kneelawk.graphlib.api.node.NodeKey;
import com.kneelawk.graphlib.api.node.NodeKeyDecoder;
import com.kneelawk.graphlib.api.node.PosNodeKey;
import com.kneelawk.graphlib.impl.GLLog;
import com.kneelawk.graphlib.impl.graph.GraphUniverseImpl;

public record SimpleNodeCodec(@NotNull PosNodeKey pos, @NotNull BlockNodeFactory node) {
    public static @NotNull NbtCompound encode(@NotNull PosNodeKey pos, @NotNull BlockNode node) {
        NbtCompound tag = new NbtCompound();

        tag.putInt("x", pos.getX());
        tag.putInt("y", pos.getY());
        tag.putInt("z", pos.getZ());

        NbtElement keyTag = pos.nodeKey().toTag();
        if (keyTag != null) {
            tag.put("key", keyTag);
        }

        tag.putString("keyType", pos.nodeKey().getTypeId().toString());

        NbtElement valueTag = node.toTag();
        if (valueTag != null) {
            tag.put("value", valueTag);
        }

        tag.putString("valueType", node.getTypeId().toString());

        return tag;
    }

    @Nullable
    public static SimpleNodeCodec decode(@NotNull GraphUniverseImpl universe, @NotNull NbtCompound tag) {
        BlockPos pos = new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));

        if (tag.contains("keyType", NbtElement.STRING_TYPE) && tag.contains("valueType", NbtElement.STRING_TYPE)) {
            Identifier keyTypeId = new Identifier(tag.getString("keyType"));
            Identifier valueTypeId = new Identifier(tag.getString("valueType"));

            NodeKeyDecoder keyDecoder = universe.getNodeKeyDecoder(keyTypeId);
            if (keyDecoder == null) {
                GLLog.warn("Tried to load unknown node key: {} @ {}", keyTypeId, pos);
                return null;
            }

            BlockNodeDecoder valueDecoder = universe.getNodeDecoder(valueTypeId);
            if (valueDecoder == null) {
                GLLog.warn("Tried to load unknown block node: {} @ {}", valueTypeId, pos);
                return null;
            }

            NbtElement keyTag = tag.get("key");
            NodeKey key = keyDecoder.decode(keyTag);
            if (key == null) {
                GLLog.warn("Unable to load NodeKey with type: {} @ {}", keyTypeId, pos);
                return null;
            }
            PosNodeKey posKey = new PosNodeKey(pos, key);

            NbtElement valueTag = tag.get("value");
            BlockNodeFactory factory = ctx -> valueDecoder.decode(valueTag, ctx);

            return new SimpleNodeCodec(posKey, factory);
        } else if (tag.contains("type", NbtElement.STRING_TYPE)) {
            Identifier typeId = new Identifier(tag.getString("type"));
            LegacyBlockNodeDecoder decoder = universe.getLegacyDecoder(typeId);

            if (decoder == null) {
                GLLog.warn("Tried to load legacy block node but there was no legacy decoder: {} @ {}", typeId, pos);
                return null;
            }

            NbtElement nodeTag = tag.get("node");

            NodeKey key = decoder.decodeNodeKey(nodeTag);
            if (key == null) {
                GLLog.warn("Unable to load legacy NodeKey with type: {} @ {}", typeId, pos);
                return null;
            }
            PosNodeKey posKey = new PosNodeKey(pos, key);

            BlockNodeFactory factory = ctx -> decoder.decodeBlockNode(nodeTag, ctx);

            return new SimpleNodeCodec(posKey, factory);
        } else {
            GLLog.warn("Tried to load block node @ {}, but there were no type tags.", pos);
            return null;
        }
    }
}
