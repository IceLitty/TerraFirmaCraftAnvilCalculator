package moe.icyr.tfc.anvil.calc.entity;

import lombok.Getter;
import lombok.NonNull;

/**
 * @author Icy
 * @since 2023/9/28
 */
@Getter
public enum AnvilFuncStep {

    DRAW("draw", -15),
    HIT_HARD("hit_hard", -9),
    HIT_MEDIUM("hit_medium", -6),
    HIT_LIGHT("hit_light", -3),
    PUNCH("punch", 2),
    BEND("bend", 7),
    UPSET("upset", 13),
    SHRINK("shrink", 16),
    //
    HIT("hit", null),
    ;

    private final String id;
    private final Integer val;

    AnvilFuncStep(String id, Integer val) {
        this.id = id;
        this.val = val;
    }

    public static AnvilFuncStep findById(String id) {
        for (AnvilFuncStep step : AnvilFuncStep.values()) {
            if (step.getId().equals(id)) {
                return step;
            }
        }
        return null;
    }

    public static AnvilFuncStep findByVal(int val) {
        for (AnvilFuncStep step : AnvilFuncStep.values()) {
            if (step.getVal() != null && step.getVal() == val) {
                return step;
            }
        }
        return null;
    }

    public static AnvilFuncStep findByKey(@NonNull String key) {
        for (AnvilFuncStep step : AnvilFuncStep.values()) {
            if (key.startsWith(step.getId())) {
                return step;
            }
        }
        return null;
    }

    public static String takeOrderFromKey(@NonNull String key) {
        for (AnvilFuncStep step : AnvilFuncStep.values()) {
            if (key.startsWith(step.getId())) {
                return key.substring(step.getId().length() + 1);
            }
        }
        return null;
    }

}
