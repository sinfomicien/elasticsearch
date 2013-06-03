/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.cluster.node;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.UnmodifiableIterator;
import org.elasticsearch.ElasticSearchIllegalArgumentException;
import org.elasticsearch.common.Booleans;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.collect.MapBuilder;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.regex.Regex;
import org.elasticsearch.common.transport.TransportAddress;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

/**
 * This class holds all {@link DiscoveryNode} in the cluster and provides convinience methods to
 * access, modify merge / diff discovery nodes.  
 */
public class DiscoveryNodes implements Iterable<DiscoveryNode> {

    public static final DiscoveryNodes EMPTY_NODES = newNodesBuilder().build();

    private final ImmutableMap<String, DiscoveryNode> nodes;

    private final ImmutableMap<String, DiscoveryNode> dataNodes;

    private final ImmutableMap<String, DiscoveryNode> masterNodes;

    private final String masterNodeId;

    private final String localNodeId;

    private DiscoveryNodes(ImmutableMap<String, DiscoveryNode> nodes, ImmutableMap<String, DiscoveryNode> dataNodes, ImmutableMap<String, DiscoveryNode> masterNodes, String masterNodeId, String localNodeId) {
        this.nodes = nodes;
        this.dataNodes = dataNodes;
        this.masterNodes = masterNodes;
        this.masterNodeId = masterNodeId;
        this.localNodeId = localNodeId;
    }

    @Override
    public UnmodifiableIterator<DiscoveryNode> iterator() {
        return nodes.values().iterator();
    }

    /**
     * Is this a valid nodes that has the minimal information set. The minimal set is defined
     * by the localNodeId being set.
     */
    public boolean valid() {
        return localNodeId != null;
    }

    /**
     * Returns <tt>true</tt> if the local node is the master node.
     */
    public boolean localNodeMaster() {
        if (localNodeId == null) {
            // we don't know yet the local node id, return false
            return false;
        }
        return localNodeId.equals(masterNodeId);
    }

    /**
     * Get the number of known nodes 
     * @return number of nodes
     */
    public int size() {
        return nodes.size();
    }

    /**
     * Get the number of known nodes 
     * @return number of nodes
     */
    public int getSize() {
        return size();
    }

    /**
     * Get a {@link Map} of the discovered nodes arranged by their ids 
     * @return {@link Map} of the discovered nodes arranged by their ids
     */
    public ImmutableMap<String, DiscoveryNode> nodes() {
        return this.nodes;
    }

    /**
     * Get a {@link Map} of the discovered nodes arranged by their ids 
     * @return {@link Map} of the discovered nodes arranged by their ids
     */
    public ImmutableMap<String, DiscoveryNode> getNodes() {
        return nodes();
    }

    /**
     * Get a {@link Map} of the discovered data nodes arranged by their ids 
     * @return {@link Map} of the discovered data nodes arranged by their ids
     */
    public ImmutableMap<String, DiscoveryNode> dataNodes() {
        return this.dataNodes;
    }

    /**
     * Get a {@link Map} of the discovered data nodes arranged by their ids 
     * @return {@link Map} of the discovered data nodes arranged by their ids
     */
    public ImmutableMap<String, DiscoveryNode> getDataNodes() {
        return dataNodes();
    }

    /**
     * Get a {@link Map} of the discovered master nodes arranged by their ids 
     * @return {@link Map} of the discovered master nodes arranged by their ids
     */
    public ImmutableMap<String, DiscoveryNode> masterNodes() {
        return this.masterNodes;
    }

    /**
     * Get a {@link Map} of the discovered master nodes arranged by their ids 
     * @return {@link Map} of the discovered master nodes arranged by their ids
     */
    public ImmutableMap<String, DiscoveryNode> getMasterNodes() {
        return masterNodes();
    }

    /**
     * Get a {@link Map} of the discovered master and data nodes arranged by their ids 
     * @return {@link Map} of the discovered master and data nodes arranged by their ids
     */
    public ImmutableMap<String, DiscoveryNode> masterAndDataNodes() {
        return MapBuilder.<String, DiscoveryNode>newMapBuilder().putAll(dataNodes).putAll(masterNodes).immutableMap();
    }

    /**
     * Get a node by its id
     * @param nodeId id of the wanted node 
     * @return wanted node if it exists. Otherwise <code>null</code>
     */
    public DiscoveryNode get(String nodeId) {
        return nodes.get(nodeId);
    }

    /**
     * Determine if a given node exists
     * @param nodeId id of the node which existence should be verified
     * @return <code>true</code> if the node exists. Otherwise <code>false</code>
     */
    public boolean nodeExists(String nodeId) {
        return nodes.containsKey(nodeId);
    }

    /**
     * Get the id of the master node 
     * @return id of the master
     */
    public String masterNodeId() {
        return this.masterNodeId;
    }

    /**
     * Get the id of the master node 
     * @return id of the master
     */
    public String getMasterNodeId() {
        return masterNodeId();
    }

    /**
     * Get the id of the local node 
     * @return id of the local node
     */
    public String localNodeId() {
        return this.localNodeId;
    }

    /**
     * Get the id of the local node 
     * @return id of the local node
     */
    public String getLocalNodeId() {
        return localNodeId();
    }

    /**
     * Get the local node 
     * @return local node
     */
    public DiscoveryNode localNode() {
        return nodes.get(localNodeId);
    }

    /**
     * Get the local node 
     * @return local node
     */
    public DiscoveryNode getLocalNode() {
        return localNode();
    }

    /**
     * Get the master node 
     * @return master node
     */
    public DiscoveryNode masterNode() {
        return nodes.get(masterNodeId);
    }

    /**
     * Get the master node 
     * @return master node
     */
    public DiscoveryNode getMasterNode() {
        return masterNode();
    }

    /**
     * Get a node by its address 
     * @param address {@link TransportAddress} of the wanted node
     * @return node identified by the given address or <code>null</code> if no such node exists
     */
    public DiscoveryNode findByAddress(TransportAddress address) {
        for (DiscoveryNode node : nodes.values()) {
            if (node.address().equals(address)) {
                return node;
            }
        }
        return null;
    }

    public boolean isAllNodes(String... nodesIds) {
        return nodesIds == null || nodesIds.length == 0 || (nodesIds.length == 1 && nodesIds[0].equals("_all"));
    }

    /**
     * Resolve a node with a given id
     * @param node id of the node to discover
     * @return discovered node matching the given id
     * @throws ElasticSearchIllegalArgumentException if more than one node matches the request or no nodes have been resolved
     */
    public DiscoveryNode resolveNode(String node) {
        String[] resolvedNodeIds = resolveNodesIds(node);
        if (resolvedNodeIds.length > 1) {
            throw new ElasticSearchIllegalArgumentException("resolved [" + node + "] into [" + resolvedNodeIds.length + "] nodes, where expected to be resolved to a single node");
        }
        if (resolvedNodeIds.length == 0) {
            throw new ElasticSearchIllegalArgumentException("failed to resolve [" + node + " ], no matching nodes");
        }
        return nodes.get(resolvedNodeIds[0]);
    }
    
    public String[] resolveNodesIds(String... nodesIds) {
        if (isAllNodes(nodesIds)) {
            int index = 0;
            nodesIds = new String[nodes.size()];
            for (DiscoveryNode node : this) {
                nodesIds[index++] = node.id();
            }
            return nodesIds;
        } else {
            Set<String> resolvedNodesIds = new HashSet<String>(nodesIds.length);
            for (String nodeId : nodesIds) {
                if (nodeId.equals("_local")) {
                    String localNodeId = localNodeId();
                    if (localNodeId != null) {
                        resolvedNodesIds.add(localNodeId);
                    }
                } else if (nodeId.equals("_master")) {
                    String masterNodeId = masterNodeId();
                    if (masterNodeId != null) {
                        resolvedNodesIds.add(masterNodeId);
                    }
                } else if (nodeExists(nodeId)) {
                    resolvedNodesIds.add(nodeId);
                } else {
                    // not a node id, try and search by name
                    for (DiscoveryNode node : this) {
                        if (Regex.simpleMatch(nodeId, node.name())) {
                            resolvedNodesIds.add(node.id());
                        }
                    }
                    for (DiscoveryNode node : this) {
                        if (node.address().match(nodeId)) {
                            resolvedNodesIds.add(node.id());
                        }
                    }
                    int index = nodeId.indexOf(':');
                    if (index != -1) {
                        String matchAttrName = nodeId.substring(0, index);
                        String matchAttrValue = nodeId.substring(index + 1);
                        if ("data".equals(matchAttrName)) {
                            if (Booleans.parseBoolean(matchAttrValue, true)) {
                                resolvedNodesIds.addAll(dataNodes.keySet());
                            } else {
                                resolvedNodesIds.removeAll(dataNodes.keySet());
                            }
                        } else if ("master".equals(matchAttrName)) {
                            if (Booleans.parseBoolean(matchAttrValue, true)) {
                                resolvedNodesIds.addAll(masterNodes.keySet());
                            } else {
                                resolvedNodesIds.removeAll(masterNodes.keySet());
                            }
                        } else {
                            for (DiscoveryNode node : this) {
                                for (Map.Entry<String, String> entry : node.attributes().entrySet()) {
                                    String attrName = entry.getKey();
                                    String attrValue = entry.getValue();
                                    if (Regex.simpleMatch(matchAttrName, attrName) && Regex.simpleMatch(matchAttrValue, attrValue)) {
                                        resolvedNodesIds.add(node.id());
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return resolvedNodesIds.toArray(new String[resolvedNodesIds.size()]);
        }
    }
    
    public DiscoveryNodes removeDeadMembers(Set<String> newNodes, String masterNodeId) {
        Builder builder = new Builder().masterNodeId(masterNodeId).localNodeId(localNodeId);
        for (DiscoveryNode node : this) {
            if (newNodes.contains(node.id())) {
                builder.put(node);
            }
        }
        return builder.build();
    }
    
    public DiscoveryNodes newNode(DiscoveryNode node) {
        return new Builder().putAll(this).put(node).build();
    }

    /**
     * Returns the changes comparing this nodes to the provided nodes.
     */
    public Delta delta(DiscoveryNodes other) {
        List<DiscoveryNode> removed = newArrayList();
        List<DiscoveryNode> added = newArrayList();
        for (DiscoveryNode node : other) {
            if (!this.nodeExists(node.id())) {
                removed.add(node);
            }
        }
        for (DiscoveryNode node : this) {
            if (!other.nodeExists(node.id())) {
                added.add(node);
            }
        }
        DiscoveryNode previousMasterNode = null;
        DiscoveryNode newMasterNode = null;
        if (masterNodeId != null) {
            if (other.masterNodeId == null || !other.masterNodeId.equals(masterNodeId)) {
                previousMasterNode = other.masterNode();
                newMasterNode = masterNode();
            }
        }
        return new Delta(previousMasterNode, newMasterNode, localNodeId, ImmutableList.copyOf(removed), ImmutableList.copyOf(added));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (DiscoveryNode node : this) {
            sb.append(node).append(',');
        }
        sb.append("}");
        return sb.toString();
    }

    public String prettyPrint() {
        StringBuilder sb = new StringBuilder();
        sb.append("nodes: \n");
        for (DiscoveryNode node : this) {
            sb.append("   ").append(node);
            if (node == localNode()) {
                sb.append(", local");
            }
            if (node == masterNode()) {
                sb.append(", master");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
    
    public Delta emptyDelta() {
        return new Delta(null, null, localNodeId, DiscoveryNode.EMPTY_LIST, DiscoveryNode.EMPTY_LIST);
    }

    public static class Delta {

        private final String localNodeId;
        private final DiscoveryNode previousMasterNode;
        private final DiscoveryNode newMasterNode;
        private final ImmutableList<DiscoveryNode> removed;
        private final ImmutableList<DiscoveryNode> added;

        public Delta(String localNodeId, ImmutableList<DiscoveryNode> removed, ImmutableList<DiscoveryNode> added) {
            this(null, null, localNodeId, removed, added);
        }

        public Delta(@Nullable DiscoveryNode previousMasterNode, @Nullable DiscoveryNode newMasterNode, String localNodeId, ImmutableList<DiscoveryNode> removed, ImmutableList<DiscoveryNode> added) {
            this.previousMasterNode = previousMasterNode;
            this.newMasterNode = newMasterNode;
            this.localNodeId = localNodeId;
            this.removed = removed;
            this.added = added;
        }

        public boolean hasChanges() {
            return masterNodeChanged() || !removed.isEmpty() || !added.isEmpty();
        }

        public boolean masterNodeChanged() {
            return newMasterNode != null;
        }

        public DiscoveryNode previousMasterNode() {
            return previousMasterNode;
        }

        public DiscoveryNode newMasterNode() {
            return newMasterNode;
        }

        public boolean removed() {
            return !removed.isEmpty();
        }

        public ImmutableList<DiscoveryNode> removedNodes() {
            return removed;
        }

        public boolean added() {
            return !added.isEmpty();
        }

        public ImmutableList<DiscoveryNode> addedNodes() {
            return added;
        }

        public String shortSummary() {
            StringBuilder sb = new StringBuilder();
            if (!removed() && masterNodeChanged()) {
                if (newMasterNode.id().equals(localNodeId)) {
                    // we are the master, no nodes we removed, we are actually the first master
                    sb.append("new_master ").append(newMasterNode());
                } else {
                    // we are not the master, so we just got this event. No nodes were removed, so its not a *new* master
                    sb.append("detected_master ").append(newMasterNode());
                }
            } else {
                if (masterNodeChanged()) {
                    sb.append("master {new ").append(newMasterNode());
                    if (previousMasterNode() != null) {
                        sb.append(", previous ").append(previousMasterNode());
                    }
                    sb.append("}");
                }
                if (removed()) {
                    if (masterNodeChanged()) {
                        sb.append(", ");
                    }
                    sb.append("removed {");
                    for (DiscoveryNode node : removedNodes()) {
                        sb.append(node).append(',');
                    }
                    sb.append("}");
                }
            }
            if (added()) {
                // don't print if there is one added, and it is us
                if (!(addedNodes().size() == 1 && addedNodes().get(0).id().equals(localNodeId))) {
                    if (removed() || masterNodeChanged()) {
                        sb.append(", ");
                    }
                    sb.append("added {");
                    for (DiscoveryNode node : addedNodes()) {
                        if (!node.id().equals(localNodeId)) {
                            // don't print ourself
                            sb.append(node).append(',');
                        }
                    }
                    sb.append("}");
                }
            }
            return sb.toString();
        }
    }
    
    public static Builder newNodesBuilder() {
        return new Builder();
    }
    
    public static class Builder {

        private Map<String, DiscoveryNode> nodes = newHashMap();

        private String masterNodeId;

        private String localNodeId;

        public Builder putAll(DiscoveryNodes nodes) {
            this.masterNodeId = nodes.masterNodeId();
            this.localNodeId = nodes.localNodeId();
            for (DiscoveryNode node : nodes) {
                put(node);
            }
            return this;
        }

        public Builder put(DiscoveryNode node) {
            nodes.put(node.id(), node);
            return this;
        }

        public Builder putAll(Iterable<DiscoveryNode> nodes) {
            for (DiscoveryNode node : nodes) {
                put(node);
            }
            return this;
        }

        public Builder remove(String nodeId) {
            nodes.remove(nodeId);
            return this;
        }

        public Builder masterNodeId(String masterNodeId) {
            this.masterNodeId = masterNodeId;
            return this;
        }

        public Builder localNodeId(String localNodeId) {
            this.localNodeId = localNodeId;
            return this;
        }

        public DiscoveryNodes build() {
            ImmutableMap.Builder<String, DiscoveryNode> dataNodesBuilder = ImmutableMap.builder();
            ImmutableMap.Builder<String, DiscoveryNode> masterNodesBuilder = ImmutableMap.builder();
            for (Map.Entry<String, DiscoveryNode> nodeEntry : nodes.entrySet()) {
                if (nodeEntry.getValue().dataNode()) {
                    dataNodesBuilder.put(nodeEntry.getKey(), nodeEntry.getValue());
                }
                if (nodeEntry.getValue().masterNode()) {
                    masterNodesBuilder.put(nodeEntry.getKey(), nodeEntry.getValue());
                }
            }
            return new DiscoveryNodes(ImmutableMap.copyOf(nodes), dataNodesBuilder.build(), masterNodesBuilder.build(), masterNodeId, localNodeId);
        }

        public static void writeTo(DiscoveryNodes nodes, StreamOutput out) throws IOException {
            if (nodes.masterNodeId() == null) {
                out.writeBoolean(false);
            } else {
                out.writeBoolean(true);
                out.writeString(nodes.masterNodeId);
            }
            out.writeVInt(nodes.size());
            for (DiscoveryNode node : nodes) {
                node.writeTo(out);
            }
        }

        public static DiscoveryNodes readFrom(StreamInput in, @Nullable DiscoveryNode localNode) throws IOException {
            Builder builder = new Builder();
            if (in.readBoolean()) {
                builder.masterNodeId(in.readString());
            }
            if (localNode != null) {
                builder.localNodeId(localNode.id());
            }
            int size = in.readVInt();
            for (int i = 0; i < size; i++) {
                DiscoveryNode node = DiscoveryNode.readNode(in);
                if (localNode != null && node.id().equals(localNode.id())) {
                    // reuse the same instance of our address and local node id for faster equality
                    node = localNode;
                }
                builder.put(node);
            }
            return builder.build();
        }
    }
}
