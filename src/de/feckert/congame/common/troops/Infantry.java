package de.feckert.congame.common.troops;

public class Infantry extends Troop {
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
}
