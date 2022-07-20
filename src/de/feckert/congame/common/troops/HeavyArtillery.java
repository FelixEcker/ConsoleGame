package de.feckert.congame.common.troops;

public class HeavyArtillery extends Troop {
    public HeavyArtillery(int owner) {
        super(owner);

        name = "Heavy Artillery";
        displayChar = 'H';
        attackDmg = .25f;
        dmgAbsorption = .45f;
        defDmgAbsorption = .5f;
        waterTravel = false;
        movement = 2;
    }
}
