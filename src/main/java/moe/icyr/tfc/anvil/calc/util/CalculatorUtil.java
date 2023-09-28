package moe.icyr.tfc.anvil.calc.util;

import lombok.extern.slf4j.Slf4j;
import moe.icyr.tfc.anvil.calc.entity.Tree;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Icy
 * @since 2023/09/28
 */
@Slf4j
public class CalculatorUtil {

    // draw, heavy, middle, light hit, punch, bend, upset, shrink
    public static final int[] func = new int[]{-15, -9, -6, -3, 2, 7, 13, 16};

//    public static void calc() {
//        int targetNow = 0;
//        int target = 75;
//        int[] rules = new int[] {-3, 7, 2};
//        for (int rule : rules) {
//            target = target + rule;
//        }
//        Tree stored = new Tree(null);
//        Instant start = Instant.now();
//        calcInternal(targetNow, target, stored, func.length - 1);
//        Instant end = Instant.now();
//        System.out.println("Spend time: " + Duration.between(start, end).toMillis());
//        System.out.println(stored);
//        verify(targetNow, stored);
//    }
//
//    public static void testAll() {
//        for (int target = 0; target <= 145; target++) {
//            for (int targetNow = 0; targetNow <= 145; targetNow++) {
//                Tree root = new Tree(null);
//                Instant start = Instant.now();
//                calcInternal(targetNow, target, root, func.length - 1);
//                Instant end = Instant.now();
//                boolean verify = verify(targetNow, target, root);
//                log.info("Target " + target + " TargetNow " + targetNow + " Spend time: " + Duration.between(start, end).toMillis() + " Verify: " + verify);
//            }
//        }
//    }

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
            target = target + rule;
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
        if (targetNow + func[funcIndex] < target || funcIndex == 0) {
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

//    private static void verify(int initVal, Tree root) {
//        List<Tree> endLeafs = root.getEndLeaf();
//        StringBuilder strBuilder = new StringBuilder("TreeValid{\n");
//        for (Tree endLeaf : endLeafs) {
//            StringBuilder builder = new StringBuilder();
//            int valNow = initVal;
//            Tree leafNode = endLeaf;
//            do {
//                valNow = valNow + leafNode.getFunc();
//                builder.insert(0, leafNode.getChildren().isEmpty() ? leafNode.getFunc() + "(" + valNow + ")" : leafNode.getFunc() + "(" + valNow + ") -> ");
//                leafNode = leafNode.getParent();
//            } while (leafNode.getParent() != null);
//            strBuilder.append(builder).append("\n");
//        }
//        String string = strBuilder.append("}").toString();
//        System.out.println(string);
//    }

    private static boolean verify(int initVal, int target, Tree root) {
        List<Tree> endLeafs = root.getEndLeaf();
        for (Tree endLeaf : endLeafs) {
            int valNow = initVal;
            Tree leafNode = endLeaf;
            do {
                valNow = valNow + leafNode.getFunc();
                leafNode = leafNode.getParent();
            } while (leafNode.getParent() != null);
            if (valNow != target) {
                return false;
            }
        }
        return true;
    }

}
