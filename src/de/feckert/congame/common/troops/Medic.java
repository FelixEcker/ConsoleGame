package de.feckert.congame.common.troops;

public class Medic extends Troop {
    public Medic(int owner) {
        super(owner);

        name = "Medic";
        displayChar = 'M';
        attackDmg = 0f;
        dmgAbsorption = .25f;
        defDmgAbsorption = .25f;
        waterTravel = true;
        movement = 5;
    }
}
