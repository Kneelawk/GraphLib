package com.kneelawk.graphlib.api.node;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.client.BlockNodePacketDecoder;
import com.kneelawk.graphlib.api.client.GraphLibClient;
import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.graph.GraphView;
import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.util.SimpleNodeKey;
import com.kneelawk.graphlib.api.util.SimpleSidedNodeKey;
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
     * Gets the data unique to this block-node.
     * <p>
     * The returned objects must implement consistent hash-code and equals functions, as this allows the graph-world to
     * correctly evaluate if nodes need to be removed or added at a given position.
     * <p>
     * Often one of the pre-defined implementations will do.
     *
     * @return this block-node's unique data.
     * @see SimpleNodeKey
     * @see SimpleSidedNodeKey
     */
    @NotNull NodeKey getKey();

    /**
     * Encodes this block node's data to an NBT element.
     * <p>
     * This can return null if this block node's type is all the data that needs to be stored.
     *
     * @return a (possibly null) NBT element describing this block node's data.
     */
    @Nullable NbtElement toTag();

    /**
     * Collects nodes in the world that this node can connect to.
     * <p>
     * <b>Contract:</b> This method must only return nodes that
     * {@link #canConnect(NodeHolder, ServerWorld, GraphView, NodeHolder)} would have returned
     * <code>true</code> for.
     *
     * @param self      this node's holder.
     * @param world     the world of blocks.
     * @param graphView the world of nodes.
     * @return all nodes this node can connect to.
     * @see WireConnectionDiscoverers#wireFindConnections(SidedWireBlockNode, NodeHolder, ServerWorld, GraphView, SidedWireConnectionFilter)
     * @see WireConnectionDiscoverers#fullBlockFindConnections(FullWireBlockNode, NodeHolder, ServerWorld, GraphView, FullWireConnectionFilter)
     * @see WireConnectionDiscoverers#centerWireFindConnections(CenterWireBlockNode, NodeHolder, ServerWorld, GraphView, CenterWireConnectionFilter)
     */
    @NotNull Collection<NodeHolder<BlockNode>> findConnections(@NotNull NodeHolder<BlockNode> self,
                                                               @NotNull ServerWorld world,
                                                               @NotNull GraphView graphView);

    /**
     * Determines whether this node can connect to another node.
     * <p>
     * <b>Contract:</b> This method must only return <code>true</code> for nodes that would be returned from
     * {@link #findConnections(NodeHolder, ServerWorld, GraphView)}.
     *
     * @param self      this node's holder.
     * @param world     the world of blocks.
     * @param graphView the world of nodes.
     * @param other     the other node to attempt to connect to.
     * @return whether this node can connect to the other node.
     * @see WireConnectionDiscoverers#wireCanConnect(SidedWireBlockNode, NodeHolder, ServerWorld, NodeHolder, SidedWireConnectionFilter)
     * @see WireConnectionDiscoverers#fullBlockCanConnect(FullWireBlockNode, NodeHolder, ServerWorld, NodeHolder, FullWireConnectionFilter)
     * @see WireConnectionDiscoverers#centerWireCanConnect(CenterWireBlockNode, NodeHolder, ServerWorld, NodeHolder, CenterWireConnectionFilter)
     */
    boolean canConnect(@NotNull NodeHolder<BlockNode> self, @NotNull ServerWorld world, @NotNull GraphView graphView,
                       @NotNull NodeHolder<BlockNode> other);

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
     * @param self      this block node's holder providing information about this node's connections and graph id.
     * @param world     the block world that this node is associated with.
     * @param graphView the world of nodes.
     */
    void onConnectionsChanged(@NotNull NodeHolder<BlockNode> self, @NotNull ServerWorld world,
                              @NotNull GraphView graphView);

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
     * @param self      this block node's holder, providing information about this node's connections and graph id.
     * @param world     the block world that this node is associated with.
     * @param graphView the world of nodes.
     * @param buf       the buffer to encode this node to.
     */
    default void toPacket(@NotNull NodeHolder<BlockNode> self, @NotNull ServerWorld world, @NotNull GraphView graphView,
                          @NotNull PacketByteBuf buf) {
        // This keeps otherwise identical-looking client-side nodes separate.
        buf.writeInt(getKey().hashCode());

        // Get the default color for our node type
        buf.writeInt(graphView.getUniverse().getDefaultDebugColor(getTypeId()));

        // A 0 byte to distinguish ourselves from SidedBlockNode, because both implementations use the same decoder
        buf.writeByte(0);
    }
}
