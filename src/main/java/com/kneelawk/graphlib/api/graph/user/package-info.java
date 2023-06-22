/**
 * This package contains the interfaces that are expected to be implemented by users of GraphLib.
 * <p>
 * For minimal use, only {@link com.kneelawk.graphlib.api.graph.user.BlockNode} and
 * {@link com.kneelawk.graphlib.api.graph.user.BlockNodeDecoder} need be implemented.
 * <p>
 * {@link com.kneelawk.graphlib.api.graph.user.BlockNodeDiscoverer} is often typically implemented as well, as this
 * allows automatic node discovery, using the
 * {@link com.kneelawk.graphlib.api.graph.GraphWorld#updateNodes(net.minecraft.util.math.BlockPos)} family of methods.
 *
 * <h2>Node Entities</h2>
 * As a rule, block nodes cannot store arbitrary data, only storing the data that makes them unique. This is because
 * block nodes are used as keys and have effectively no identity.
 * <p>
 * {@link com.kneelawk.graphlib.api.graph.user.NodeEntity}s on the other hand, are designed for storing arbitrary data
 * in a node, as well as executing arbitrary logic on that data.
 *
 * <h2>Link Keys</h2>
 * Under normal circumstances, links only represent a connection between two nodes, and there can only be one connection
 * between any two nodes. However, if there needs to be multiple links allowed between two nodes, or if custom link
 * detection and removal logic is used (e.g. via
 * {@link com.kneelawk.graphlib.api.graph.GraphWorld#connectNodes(com.kneelawk.graphlib.api.util.NodePos, com.kneelawk.graphlib.api.util.NodePos, com.kneelawk.graphlib.api.graph.user.LinkKey)}
 * and
 * {@link com.kneelawk.graphlib.api.graph.user.LinkKey#isAutomaticRemoval(com.kneelawk.graphlib.api.graph.LinkHolder)}),
 * then it may be reasonable to have a custom implementation of {@link com.kneelawk.graphlib.api.graph.user.LinkKey}.
 * <p>
 * Note, the default implementation of {@link com.kneelawk.graphlib.api.graph.user.LinkKey} is
 * {@link com.kneelawk.graphlib.api.util.EmptyLinkKey}.
 *
 * <h2>Link Entities</h2>
 * Like with block nodes, links also have a mechanism for storing arbitrary data. For example, if a GraphLib graph were
 * to be directional, then {@link com.kneelawk.graphlib.api.graph.user.LinkEntity}s could be used to indicate the
 * direction of the link, while still keeping a limit on only one link between any two nodes.
 *
 * <h2>Graph Entities</h2>
 * {@link com.kneelawk.graphlib.api.graph.user.GraphEntity}s can be used to store arbitrary data in an entire graph.
 * Graph entity merging and splitting is handled via
 * {@link com.kneelawk.graphlib.api.graph.user.GraphEntity#merge(com.kneelawk.graphlib.api.graph.user.GraphEntity)} and
 * {@link com.kneelawk.graphlib.api.graph.user.GraphEntitySplitter}.
 */
package com.kneelawk.graphlib.api.graph.user;
