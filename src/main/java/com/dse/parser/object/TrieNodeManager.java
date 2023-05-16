package com.dse.parser.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrieNodeManager {
    static TrieNodeManager instance;
    Map<String, TrieNode> allTries = new HashMap<>();

    public static TrieNodeManager getInstance() {
        if (instance == null) {
            instance = new TrieNodeManager();
        }
        return instance;
    }

    public boolean checkTrie(String name) {
        return allTries.containsKey(name);
    }

    public TrieNode getTrie(String name) {
        if (allTries.containsKey(name)) {
            return allTries.get(name);
        } else {
            TrieNode trie = new TrieNode();
            allTries.put(name, trie);
            return trie;
        }
    }

    public void addToTrie(String name, INode node) {
        TrieNode trie = getTrie(name);
        String path = node.getAbsolutePath();
        for (int i = path.length() - 1; i > -1; i--) {
            char c = path.charAt(i);
            if (trie.getChildren().containsKey(c)) {
                trie = trie.getChildren().get(c);
            } else {
                TrieNode newTrie = new TrieNode();
                trie.getChildren().put(c, newTrie);
                trie = newTrie;
            }
        }

        // Just make sure that there will be no duplicate nodes with the same path
        // If there are then we will have to rewrite this code
        trie.setNode(node);
    }

    public List<INode> findInTrie(String name, String path) {
        TrieNode trie = getTrie(name);
        for (int i = path.length() - 1; i > -1; i--) {
            char c = path.charAt(i);
            if (trie.getChildren().containsKey(c)) {
                trie = trie.getChildren().get(c);
            } else {
                return new ArrayList<>();
            }
        }
        return getAllNodesInTrie(trie);
    }

    public List<INode> getAllNodesInTrie(TrieNode trie) {
        List<INode> nodes = new ArrayList<>();
        if (trie.isNode()) {
            nodes.add(trie.getNode());
        }
        for (TrieNode child : trie.getChildren().values()) {
            nodes.addAll(getAllNodesInTrie(child));
        }
        return nodes;
    }
}
