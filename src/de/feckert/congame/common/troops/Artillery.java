package de.feckert.congame.common.troops;

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
