package moe.icyr.tfc.anvil.calc.entity;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Icy
 * @since 2023/09/28
 */
@Getter
public class Tree {

    @Setter
    private Integer func;
    private Tree parent;
    private final List<Tree> children;

    public Tree(Integer func) {
        this.func = func;
        this.children = new ArrayList<>();
    }

    public void addChildren(Tree child) {
        child.parent = this;
        this.children.add(child);
    }

    public @NonNull List<Tree> getEndLeaf() {
        List<Tree> endLeafs = new ArrayList<>();
        if (this.getChildren().isEmpty()) {
            endLeafs.add(this);
        } else {
            collectEndLeaf(this, endLeafs);
        }
        return endLeafs;
    }

    private void collectEndLeaf(Tree node, @NonNull List<Tree> endLeafs) {
        for (Tree t : node.getChildren()) {
            if (t.getChildren().isEmpty()) {
                endLeafs.add(t);
            } else {
                collectEndLeaf(t, endLeafs);
            }
        }
    }

    public @NonNull Tree getShorterEndLeaf() {
        List<Tree> endLeafs = this.getEndLeaf();
        endLeafs.sort((t1, t2) -> {
            Tree t11 = t1;
            int t1Count = 1;
            do {
                t11 = t11.getParent();
                t1Count++;
            } while (t11.getParent() != null);
            Tree t22 = t2;
            int t2Count = 1;
            do {
                t22 = t22.getParent();
                t2Count++;
            } while (t22.getParent() != null);
            return t1Count - t2Count;
        });
        return endLeafs.get(0);
    }

    @Override
    public String toString() {
        List<Tree> endLeafs = getEndLeaf();
        StringBuilder strBuilder = new StringBuilder("Tree{\n");
        for (Tree endLeaf : endLeafs) {
            StringBuilder builder = new StringBuilder();
            Tree leafNode = endLeaf;
            do {
                builder.insert(0, leafNode.getChildren().isEmpty() ? leafNode.getFunc() : leafNode.getFunc() + " -> ");
                leafNode = leafNode.getParent();
            } while (leafNode != null && leafNode.getParent() != null);
            strBuilder.append(builder).append("\n");
        }
        return strBuilder.append("}").toString();
    }

}
