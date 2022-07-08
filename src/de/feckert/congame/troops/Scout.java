package de.feckert.congame.troops;

import de.feckert.congame.Console;
import de.feckert.congame.util.ActionResult;

public class Scout extends Troop {
	public Scout(boolean team) {
		super(team);

		name = "Scout";
		displayChar = 'S';
		attackDmg = 1f/16f;
		dmgAbsorption = 1/18f;
		defDmgAbsorption = 1/17f;
		waterTravel = true;
		movement = 8;
	}

	@Override
	public ActionResult attack(Troop target) {
		float dealingDamage = attackDmg-target.dmgAbsorption;

		if (dealingDamage <= 0) {
			return ActionResult.FAILED;
		}

		target.health -= attackDmg;
		if (target.health <= 0) {
			return ActionResult.TARGET_DIED;
		} else {
			Console.message("attack.target_defends");
			target.defend(this);
		}

		if (health <= 0) {
			return ActionResult.TROOP_DIED;
		}

		return ActionResult.SUCCESS;
	}

	@Override
	public void defend(Troop attacker) {
		float dealingDamage = attackDmg-attacker.defDmgAbsorption;
		if (dealingDamage <= 0) return;
		attacker.health -= dealingDamage;
	}

	@Override
	public boolean canTravelTo(int originX, int originY, int destX, int destY) {
		if (super.canTravelTo(originX, originY, destX, destY)) {
			return true;
		} else {
			return false;
		}
	}
}
