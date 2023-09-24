package info.kgeorgiy.ja.stafeev.exam.translator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

public class Dictionary {
    private static class Node {
        final Map<Character, Node> nodeMap;
        final Set<String> strings;

        Node() {
            nodeMap = new HashMap<>();
            strings = new HashSet<>();
        }
        void addNode(final Character character, final Node node) {
            nodeMap.putIfAbsent(character, node);
        }
        Node getNode(final Character character) {
            return nodeMap.get(character);
        }

        String getString() {
            return strings.iterator().next();
        }
    }

    private final Node root;
    Dictionary() {
        root = new Node();
    }

    private void addString(final String string, final String translate, final int index, final Node node) {
        if (index == string.length()) {
            node.strings.add(translate);
            return;
        }
        node.addNode(string.charAt(index), new Node());
        addString(string, translate, index + 1, node.getNode(string.charAt(index)));
        node.strings.add(translate);
    }

    public void addString(final String string, final String translate) {
        addString(string, translate, 0, root);
    }

    private void translateText(final String string, final BufferedWriter writer, int index, final Node node) throws IOException {
        if (index == string.length()) {
            if (node != root) {
                writer.write(node.getString());
            }
            writer.write(System.lineSeparator());
            return;
        }
        if (node.nodeMap.containsKey(string.charAt(index))) {
            translateText(string, writer, index + 1, node.getNode(string.charAt(index)));
        } else {
            if (node != root) {
                writer.write(node.getString());
            }
            if (!Character.isLetter(string.charAt(index))) {
                writer.write(string.charAt(index));
            }
            translateText(string, writer, index + 1, root);
        }
    }
    public void translateText(final String string, final BufferedWriter writer) throws IOException {
        translateText(string, writer, 0, root);
    }
}
