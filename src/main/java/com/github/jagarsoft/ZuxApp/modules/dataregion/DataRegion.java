package com.github.jagarsoft.ZuxApp.modules.dataregion;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DataRegion {
    private Node root;
    private int entries;
    private int invalidEntries;
    Node currentNode;

    private static class Node {
        int startAddress;
        int endAddress;
        String label;
        String comment;
        Node left;
        Node right;
        
        Node(int startAddress, int endAddress, String label, String comment) {
            this.startAddress = startAddress;
            this.endAddress = endAddress;
            this.label = label;
            this.comment = comment;
            this.left = null;
            this.right = null;
        }
    }
    
    public DataRegion() {
        this.root = null;
    }
    
    public void load(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                entries++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith(";") ) {
                    continue;
                }
                
                String[] parts = line.split("\\s+", 4);
                if (parts.length >= 3) {
                    try {
                        String comment = null;
                        int address = parseAddress(parts[0]);
                        String label = parts[1];
                        int size = Integer.parseInt(parts[2]);
                        if( parts.length == 4 ) {
                            comment = parts[3];
                        }
                        addDataRegion(address, size, label, comment);
                    } catch (NumberFormatException e) {
                        // Skip invalid lines
                        invalidEntries++;
                    }
                } else  {
                    invalidEntries++;
                }
            }
        }
    }
    
    private int parseAddress(String addressStr) {
        if (addressStr.startsWith("0x") || addressStr.startsWith("0X")) {
            return Integer.parseInt(addressStr.substring(2), 16);
        } else if (addressStr.endsWith("h") || addressStr.endsWith("H")) {
            return Integer.parseInt(addressStr.substring(0, addressStr.length() - 1), 16);
        } else {
            return Integer.parseInt(addressStr);
        }
    }
    
    public void addDataRegion(int addr, int size, String label, String comment) {
        int endAddr = addr + size - 1;
        root = insertNode(root, addr, endAddr, label, comment);
    }
    
    private Node insertNode(Node node, int startAddr, int endAddr, String label, String comment) {
        if (node == null) {
            return new Node(startAddr, endAddr, label, comment);
        }
        
        if (startAddr < node.startAddress) {
            node.left = insertNode(node.left, startAddr, endAddr, label, comment);
        } else if (startAddr > node.startAddress) {
            node.right = insertNode(node.right, startAddr, endAddr, label, comment);
        }
        
        return node;
    }
    
    public boolean isDataRegion(int addr) {
        return searchAddress(root, addr);
    }
    
    private boolean searchAddress(Node node, int addr) {
        currentNode = node;

        if (node == null) {
            return false;
        }
        
        if (addr >= node.startAddress && addr <= node.endAddress) {
            return true;
        }
        
        if (addr < node.startAddress) {
            return searchAddress(node.left, addr);
        } else {
            return searchAddress(node.right, addr);
        }
    }
    
    public List<String> getAllRegions() {
        List<String> regions = new ArrayList<>();
        collectRegions(root, regions);
        return regions;
    }
    
    private void collectRegions(Node node, List<String> regions) {
        if (node != null) {
            collectRegions(node.left, regions);
            regions.add(String.format("0x%04X-0x%04X: %s %s",
                node.startAddress, node.endAddress, node.label, node.comment));
            collectRegions(node.right, regions);
        }
    }

    public int getEntries() {
        return entries;
    }

    public int getInvalidEntries() {
        return invalidEntries;
    }

    public int getRegionsCount() {
        return getAllRegions().size();
    }

    public String getLabel(int pc) {
        searchAddress(root, pc);
        return currentNode.label;
    }

    public int getSize() {
        return currentNode.endAddress - currentNode.startAddress + 1;
    }

    public int getEnd() {
        return currentNode.endAddress;
    }

    public String getComment() {
        return currentNode.comment;
    }
}