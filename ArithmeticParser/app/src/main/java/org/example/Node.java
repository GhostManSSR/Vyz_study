package org.example;


import java.util.ArrayList;
import java.util.List;

public class Node {

    private final String name;
    private final List<Node> children;

    public Node(String name) {
        this.name = name;
        this.children = new ArrayList<>();
    }

    public void addChild(Node child) {
        children.add(child);
    }

    public String getName() {
        return name;
    }

    public List<Node> getChildren() {
        return children;
    }

    public void print(String prefix, boolean isLast) {
        System.out.println(
                prefix
                        + (isLast ? "└── " : "├── ")
                        + name
        );

        for (int i = 0; i < children.size(); i++) {
            boolean last = i == children.size() - 1;

            children.get(i).print(
                    prefix + (isLast ? "    " : "│   "),
                    last
            );
        }
    }

    public void printTree() {
        System.out.println(name);

        for (int i = 0; i < children.size(); i++) {
            children.get(i).print(
                    "",
                    i == children.size() - 1
            );
        }
    }
}
