/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.inlong.sdk.dataproxy.utils;

import org.apache.inlong.sdk.dataproxy.config.HostInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.SortedMap;
import java.util.stream.Collectors;

public class HashRing {
    private int virtualNode = 1000;
    private TreeMap<String, HostInfo> virtualNode2RealNode = new TreeMap<>();
    private List<HostInfo> nodeList = new ArrayList<>();
    private static final HashRing instance = new HashRing();
    private static final Logger LOGGER = LoggerFactory.getLogger(HashRing.class);

    public static HashRing getInstance() {
        return instance;
    }

    public TreeMap<String, HostInfo> getVirtualNode2RealNode() {
        return virtualNode2RealNode;
    }

    public String node2VirtualNode(HostInfo node, int index) {
        return  "virtual&&" + index + "&&" + node.toString();
    }

    public void initHashRing(List<HostInfo> ipList) {
        this.virtualNode2RealNode = new TreeMap<>();
        this.nodeList = ipList;
        for (HostInfo host : this.nodeList) {
            for (int i = 0; i < this.virtualNode; i++) {
                String key = node2VirtualNode(host, i);
                String hash = ConsistencyHashUtil.hashMurMurHash(key);
                virtualNode2RealNode.put(hash, host);
            }
        }
        LOGGER.info("init hash ring {}", this.virtualNode2RealNode);
    }

    private void setVirtualNode(int virtualNode) {
        this.virtualNode = virtualNode;
    }

    public HostInfo getNode(String key) {
        String hash = ConsistencyHashUtil.hashMurMurHash(key);
        SortedMap<String, HostInfo> tailMap = this.virtualNode2RealNode.tailMap(hash);
        HostInfo node;
        if (tailMap.isEmpty()) {
            node = this.virtualNode2RealNode.get(this.virtualNode2RealNode.firstKey());
        } else {
            node = this.virtualNode2RealNode.get(tailMap.firstKey());
        }
        LOGGER.info("{} located to {}", key, node);
        return node;
    }

    public void appendNode(HostInfo host) {
        this.nodeList.add(host);
        for (int i = 0; i < this.virtualNode; i++) {
            String key = node2VirtualNode(host, i);
            String hash = ConsistencyHashUtil.hashMurMurHash(key);
            virtualNode2RealNode.put(hash, host);
            LOGGER.info("append node {}", host);
        }
    }

    public void extendNode(List<HostInfo> nodes) {
        this.nodeList.addAll(nodes);
        for (HostInfo host : this.nodeList) {
            for (int i = 0; i < this.virtualNode; i++) {
                String key = node2VirtualNode(host, i);
                String hash = ConsistencyHashUtil.hashMurMurHash(key);
                virtualNode2RealNode.put(hash, host);
            }
        }
        LOGGER.info("append node list {}", nodes);
    }

    public void deleteNode(HostInfo host) {
        this.nodeList.remove(host);
        for (int i = 0; i < this.virtualNode; i++) {
            String hash = ConsistencyHashUtil.hashMurMurHash(node2VirtualNode(host, i));
            virtualNode2RealNode.remove(hash);
        }
        LOGGER.info("remove node {}", host);
    }

    public void removeNode(List<HostInfo> hosts) {
        for (HostInfo host : hosts) {
            this.nodeList.remove(host);
            for (int i = 0; i < this.virtualNode; i++) {
                String hash = ConsistencyHashUtil.hashMurMurHash(node2VirtualNode(host, i));
                virtualNode2RealNode.remove(hash);
            }
        }
        LOGGER.info("remove node list {}", hosts);
    }

    public void updateNode(List<HostInfo> nodes) {
        List<HostInfo> newHosts = new ArrayList<>(nodes);
        List<HostInfo> oldHosts = new ArrayList<>(this.nodeList);
        List<HostInfo> append = newHosts.stream().filter(host -> !oldHosts.contains(host)).collect(Collectors.toList());
        List<HostInfo> remove = oldHosts.stream().filter(host -> !newHosts.contains(host)).collect(Collectors.toList());
        extendNode(append);
        removeNode(remove);
    }
}
