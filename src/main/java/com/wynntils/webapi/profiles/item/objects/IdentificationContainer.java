/*
 *  * Copyright © Wynntils - 2018 - 2020.
 */

package com.wynntils.webapi.profiles.item.objects;

import com.wynntils.core.utils.StringUtils;
import com.wynntils.webapi.profiles.item.enums.IdentificationModifier;
import org.apache.commons.lang3.math.Fraction;

public class IdentificationContainer {

    private IdentificationModifier type;
    private int baseValue;
    private boolean isFixed;

    private transient int min, max;
    private transient Fraction perfectChance;

    public IdentificationContainer(IdentificationModifier type, int baseValue, boolean isFixed) {
        this.type = type; this.baseValue = baseValue; this.isFixed = isFixed;
        calculateMinMax();
    }

    public void calculateMinMax() {
        if (isFixed || (-1 <= baseValue && baseValue <= 1)) {
            min = max = baseValue;
            return;
        }

        min = (int) Math.round(baseValue * (baseValue < 0 ? 1.3 : 0.3));
        max = (int) Math.round(baseValue * (baseValue < 0 ? 0.7 : 1.3));
    }

    public IdentificationModifier getType() {
        return type;
    }

    public int getMax() {
        return max;
    }

    public int getMin() {
        return min;
    }

    public int getBaseValue() {
        return baseValue;
    }

    public boolean isFixed() {
        return isFixed;
    }

    public boolean hasConstantValue() {
        return isFixed || min == max;
    }

    public static String getAsLongName(String shortName) {
        if (shortName.startsWith("raw")) {
            shortName = shortName.substring(3);
            shortName = Character.toLowerCase(shortName.charAt(0)) + shortName.substring(1);
        }

        StringBuilder nameBuilder = new StringBuilder();
        for (char c : shortName.toCharArray()) {
            if (Character.isUpperCase(c)) nameBuilder.append(" ").append(c);
            else nameBuilder.append(c);
        }

        return StringUtils.capitalizeFirst(nameBuilder.toString());
    }

    public static class ReidentificationChances {
        // All fractions 0 to 1; decrease + remain + increase = 1
        public final Fraction decrease;  // Chance to decrease
        public final Fraction remain;  // Chance to remain the same (Usually 1/61 or 1/131)
        public final Fraction increase;  // Chance to increase

        public ReidentificationChances(Fraction decrease, Fraction remain, Fraction increase) {
            this.decrease = decrease; this.remain = remain; this.increase = increase;
        }
    }

    /**
     * Return the chances for this identification to decrease/remain the same/increase after reidentification
     *
     * @param currentValue The current value of this identification
     * @return A {@link ReidentificationChances} of the result (All from 0 to 1)
     */
    public strictfp ReidentificationChances getChances(int currentValue) {
        if (hasConstantValue()) {
            return new ReidentificationChances(
                currentValue > baseValue ? Fraction.ONE : Fraction.ZERO,
                currentValue == baseValue ? Fraction.ONE : Fraction.ZERO,
                currentValue < baseValue ? Fraction.ONE : Fraction.ZERO
            );
        }

        if (currentValue > max || currentValue < min) {
            return new ReidentificationChances(
                currentValue > max ? Fraction.ONE : Fraction.ZERO,
                Fraction.ZERO,
                currentValue < min ? Fraction.ONE : Fraction.ZERO
            );
        }

        int increaseDirection = baseValue > 0 ? +1 : -1;

        int lowerRawRoll;

        if (currentValue != min) {
            double lowerRawRollUnrounded = (currentValue * 100D - 50) / baseValue;
            if (baseValue > 0) {
                lowerRawRoll = (int) Math.floor(lowerRawRollUnrounded);
                if (lowerRawRollUnrounded == lowerRawRoll) --lowerRawRoll;
            } else {
                lowerRawRoll = (int) Math.ceil(lowerRawRollUnrounded);
                if (lowerRawRollUnrounded == lowerRawRoll) ++lowerRawRoll;
            }

            if (Math.round(baseValue * (lowerRawRoll / 100D)) >= currentValue) {
                lowerRawRoll -= increaseDirection;
            } else if (Math.round(baseValue * ((lowerRawRoll + increaseDirection) / 100D)) < currentValue) {
                lowerRawRoll += increaseDirection;
            }
        } else {
            lowerRawRoll = baseValue > 0 ? 29 : 131;
        }

        int higherRawRoll;

        if (currentValue != max) {
            double higherRawRollUnrounded = (currentValue * 100D + 50) / baseValue;
             higherRawRoll = baseValue > 0 ? (int) Math.ceil(higherRawRollUnrounded) : (int) Math.floor(higherRawRollUnrounded);

            if (Math.round(baseValue * (higherRawRoll / 100D)) < max) {
                higherRawRoll += increaseDirection;
            } else if (Math.round(baseValue * ((higherRawRoll - increaseDirection) / 100D)) >= max) {
                higherRawRoll -= increaseDirection;
            }
        } else {
            higherRawRoll = baseValue > 0 ? 131 : 69;
        }

        Fraction decrease, increase;

        if (baseValue > 0) {
            // chance to be (<= lowerRawRoll) and (>= higherRawRoll)
            decrease = Fraction.getFraction(lowerRawRoll - 29, 101);
            increase = Fraction.getFraction(131 - higherRawRoll, 101);
        } else {
            decrease = Fraction.getFraction(131 - lowerRawRoll, 61);
            increase = Fraction.getFraction(higherRawRoll - 69, 61);
        }

        int remainNumerator = Math.abs(higherRawRoll - lowerRawRoll) - 1;
        // assert remainNumerator >= 0 : "Reid math is wrong";

        return new ReidentificationChances(
            decrease,
            Fraction.getFraction(remainNumerator, baseValue > 0 ? 101 : 61),
            increase
        );
    }

    /**
     * @return The chance for this identification to become perfect (From 0 to 1)
     */
    public Fraction getPerfectChance() {
        return perfectChance == null ? (perfectChance = getChances(max).remain) : perfectChance;
    }

    /**
     * @param currentValue Current value of this identification
     * @return true if this is a valid value (If false, the API is probably wrong)
     */
    public boolean isValidValue(int currentValue) {
        return getChances(currentValue).remain.getNumerator() != 0;  // Not a 0% chance to remain as this value after reid
    }

}
