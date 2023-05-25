package com.kneelawk.graphlib.api.node;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.client.BlockNodePacketDecoder;
import com.kneelawk.graphlib.api.client.GraphLibClient;
import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.graph.NodeContext;
import com.kneelawk.graphlib.api.graph.NodeEntityContext;
import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.wire.CenterWireBlockNode;
import com.kneelawk.graphlib.api.wire.CenterWireConnectionFilter;
import com.kneelawk.graphlib.api.wire.FullWireBlockNode;
import com.kneelawk.graphlib.api.wire.FullWireConnectionFilter;
import com.kneelawk.graphlib.api.wire.SidedWireBlockNode;
import com.kneelawk.graphlib.api.wire.SidedWireConnectionFilter;
import com.kneelawk.graphlib.api.wire.WireConnectionDiscoverers;

/**
 * Interface that all block nodes should implement.
 * <p>
 * A block node is a piece of immutable data that sits in a block graph and can be used to allow utilities to determine
 * which blocks are connected to which and how.
 * <p>
 * In Wired Redstone, each wire or gate has at least one block node associated with it. For example, each red-alloy wire
 * part has a single associated red-alloy wire node. These nodes are then connected according to a set of rules and then
 * traversed when ever a wire receives a redstone signal, allowing all connected wires to receive the same signal,
 * causing them all to update at the same time with the same redstone value.
 *
 * @see WireConnectionDiscoverers
 */
public interface BlockNode {
    /**
     * Gets this block node's type ID, associated with its decoder.
     * <p>
     * A block node's {@link BlockNodeDecoder} must always be registered with
     * {@link GraphUniverse#addNodeDecoder(Identifier, BlockNodeDecoder)} under the same ID as returned here.
     *
     * @return the id of this block node.
     */
    @NotNull Identifier getTypeId();

    /**
     * Encodes this block node's data to an NBT element.
     * <p>
     * This can return null if this block node's type is all the data that needs to be stored.
     *
     * @return a (possibly null) NBT element describing this block node's data.
     */
    @Nullable NbtElement toTag();

    /**
     * Checks whether this specific node should have a node entity associated with it.
     *
     * @param ctx the node context for this node.
     * @return <code>true</code> if this node should have a node entity associated with it.
     */
    boolean shouldHaveNodeEntity(@NotNull NodeContext ctx);

    /**
     * Creates a new node entity that will be associated with this node.
     *
     * @param entityCtx the new entity's context.
     * @return a newly created node entity, or <code>null</code> if an entity could not be created.
     */
    @Nullable NodeEntity createNodeEntity(@NotNull NodeEntityContext entityCtx);

    /**
     * Collects nodes in the world that this node can connect to.
     * <p>
     * <b>Contract:</b> This method must only return nodes that
     * {@link #canConnect(NodeContext, NodeHolder)} would have returned
     * <code>true</code> for.
     *
     * @param ctx the node context for this node.
     * @return all nodes this node can connect to.
     * @see WireConnectionDiscoverers#wireFindConnections(SidedWireBlockNode, NodeContext, SidedWireConnectionFilter)
     * @see WireConnectionDiscoverers#fullBlockFindConnections(FullWireBlockNode, NodeContext, FullWireConnectionFilter)
     * @see WireConnectionDiscoverers#centerWireFindConnections(CenterWireBlockNode, NodeContext, CenterWireConnectionFilter)
     */
    @NotNull Collection<NodeHolder<BlockNode>> findConnections(@NotNull NodeContext ctx);

    /**
     * Determines whether this node can connect to another node.
     * <p>
     * <b>Contract:</b> This method must only return <code>true</code> for nodes that would be returned from
     * {@link #findConnections(NodeContext)}.
     *
     * @param ctx   the node context for this node.
     * @param other the other node to attempt to connect to.
     * @return whether this node can connect to the other node.
     * @see WireConnectionDiscoverers#wireCanConnect(SidedWireBlockNode, NodeContext, NodeHolder, SidedWireConnectionFilter)
     * @see WireConnectionDiscoverers#fullBlockCanConnect(FullWireBlockNode, NodeContext, NodeHolder, FullWireConnectionFilter)
     * @see WireConnectionDiscoverers#centerWireCanConnect(CenterWireBlockNode, NodeContext, NodeHolder, CenterWireConnectionFilter)
     */
    boolean canConnect(@NotNull NodeContext ctx, @NotNull NodeHolder<BlockNode> other);

    /**
     * Called when the block graph controller has determined that this specific node's connections have been changed.
     * <p>
     * This usually performs visual updates on the block associated with this node, but this can be used for other
     * things as well. This method is also called on nodes that have been removed from a graph, after the graph has
     * finished removing them.
     * <p>
     * Note: This is not called for every node change in a graph, only when this specific node's connection's have
     * changed.
     *
     * @param ctx the node context for this node.
     */
    void onConnectionsChanged(@NotNull NodeContext ctx);

    /**
     * Block nodes are compared based on their hash-code and equals functions.
     * <p>
     * Block nodes must always implement consistent hash-code and equals functions, as this allows the block graph
     * controller to be able to correctly evaluate if nodes need to be removed or added at a given position.
     *
     * @return the hash-code of this block node's data.
     */
    @Override
    int hashCode();

    /**
     * Block nodes are compared based on their hash-code and equals functions.
     * <p>
     * Block nodes must always implement consistent hash-code and equals functions, as this allows the block graph
     * controller to be able to correctly evaluate if nodes need to be removed or added at a given position.
     *
     * @param o the other node to compare this node to.
     * @return <code>true</code> if these two nodes hold the same data, <code>false</code> otherwise.
     */
    @Override
    boolean equals(@Nullable Object o);

    /**
     * Encodes this block node to a {@link PacketByteBuf} to be sent to the client for client-side graph debug
     * rendering.
     * <p>
     * The default implementations of this method are compatible with the default client-side block node decoders.
     * This method does <b>not</b> need to be implemented in order for client-side graph debug rendering to work.
     * This method should only be overridden to provide custom data to the client.
     * <p>
     * If custom data is being sent to the client, use
     * {@link GraphLibClient#registerDecoder(Identifier, Identifier, BlockNodePacketDecoder)}
     * to register a decoder for the custom data.
     *
     * @param ctx the node context for this node.
     * @param buf the buffer to encode this node to.
     */
    default void toPacket(@NotNull NodeContext ctx, @NotNull PacketByteBuf buf) {
        // This keeps otherwise identical-looking client-side nodes separate.
        buf.writeInt(hashCode());

        // Get the default color for our node type
        buf.writeInt(ctx.graphWorld().getUniverse().getDefaultDebugColor(getTypeId()));

        // A 0 byte to distinguish ourselves from SidedBlockNode, because both implementations use the same decoder
        buf.writeByte(0);
    }
}
