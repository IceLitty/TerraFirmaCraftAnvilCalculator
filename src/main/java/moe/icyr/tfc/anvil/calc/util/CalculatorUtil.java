package moe.icyr.tfc.anvil.calc.util;

import lombok.extern.slf4j.Slf4j;
import moe.icyr.tfc.anvil.calc.entity.Tree;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Icy
 * @since 2023/09/28
 */
@Slf4j
public class CalculatorUtil {

    // draw, heavy, middle, light hit, punch, bend, upset, shrink
    private static final int[] func = new int[]{-15, -9, -6, -3, 2, 7, 13, 16};

    /**
     * 计算入口
     *
     * @param targetNow 初始值
     * @param target    目标值
     * @param rules     末尾规则（与顺序无关）
     * @return 步骤（末尾不含规则步骤）
     */
    public static Integer[] calc(int targetNow, int target, int[] rules) {
        for (int rule : rules) {
            target = target - rule;
        }
        Tree root = new Tree(null);
        calcInternal(targetNow, target, root, func.length - 1);
        Tree shorterPath = root.getShorterEndLeaf();
        List<Integer> funcList = new ArrayList<>();
        do {
            funcList.add(0, shorterPath.getFunc());
            shorterPath = shorterPath.getParent();
        } while (shorterPath.getParent() != null);
        return funcList.toArray(new Integer[0]);
    }

    /**
     * 计算原型
     *
     * @param targetNow 当前值
     * @param target    目标值
     * @param treeNode  当前节点
     * @param funcIndex 用于递归减除的操作数下标
     */
    private static void calcInternal(int targetNow, int target, Tree treeNode, int funcIndex) {
        if (targetNow == target) {
            // 不用计算，结束
        } else if (targetNow + func[funcIndex] < target || funcIndex == 0) {
            // 追进目标值 或 找到最小操作数，但目标值仍比结果数小，追退目标值
            Tree treeLesser = new Tree(func[funcIndex]);
            treeNode.addChildren(treeLesser);
            calcInternal(targetNow + func[funcIndex], target, treeLesser, func.length - 1);
        } else if (targetNow + func[funcIndex] == target) {
            // 找到路径，结束
            treeNode.addChildren(new Tree(func[funcIndex]));
        } else if (targetNow + func[funcIndex - 1] == target) {
            // 找到路径，结束
            treeNode.addChildren(new Tree(func[funcIndex - 1]));
        } else if (targetNow + func[funcIndex - 1] < target) {
            // 找到包围目标的操作数
            Tree treeGreeter = new Tree(func[funcIndex]);
            Tree treeLesser = new Tree(func[funcIndex - 1]);
            treeNode.addChildren(treeGreeter);
            treeNode.addChildren(treeLesser);
            calcInternal(targetNow + func[funcIndex], target, treeGreeter, func.length - 1);
            calcInternal(targetNow + func[funcIndex - 1], target, treeLesser, func.length - 1);
        } else {
            // 寻找包围目标值的操作数
            funcIndex--;
            calcInternal(targetNow, target, treeNode, funcIndex);
        }
    }

}
