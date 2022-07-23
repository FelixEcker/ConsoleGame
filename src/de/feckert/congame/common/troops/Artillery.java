package de.feckert.congame.common.troops;

import de.feckert.congame.common.CapturePoint;
import de.feckert.congame.util.ActionResult;

public class Artillery extends Troop {
    public Artillery(int owner) {
        super(owner);

        name = "Artillery";
        displayChar = 'A';
        attackDmg = 0.125f;
        dmgAbsorption = 0.4f;
        defDmgAbsorption = .45f;
        waterTravel = true;
        movement = 4;
    }

}
