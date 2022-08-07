package de.feckert.congame.common.troops;

import de.feckert.congame.util.ActionResult;

public class Infantry extends Troop {
    public static final float defenseBuff = .8f;
    public int remainingRoundsPrimary = 0;
    public int primaryCooldown = 0;

    public Infantry(int owner) {
        super(owner);

        name = "Infantry";
        displayChar = 'I';
        attackDmg = .2f;
        dmgAbsorption = .3f;
        defDmgAbsorption = .35f;
        waterTravel = true;
        movement = 10;
    }

    @Override
    public ActionResult primaryAction() {
        if (primaryCooldown != 0 || remainingRoundsPrimary != 0) return ActionResult.FAILED;
        primaryCooldown = 6;
        remainingRoundsPrimary = 3;

        defDmgAbsorption += defenseBuff;
        dmgAbsorption += defenseBuff;

        return ActionResult.SUCCESS;
    }

    @Override
    public void update() {
        super.update();
        if (remainingRoundsPrimary != 0) remainingRoundsPrimary--;
        if (primaryCooldown != 0 && remainingRoundsPrimary == 0) primaryCooldown--;
    }
}
