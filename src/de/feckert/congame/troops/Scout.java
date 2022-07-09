package de.feckert.congame.troops;

import de.feckert.congame.CapturePoint;
import de.feckert.congame.util.ActionResult;
import de.feckert.congame.util.Console;

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

		if (dealingDamage <= 0 || attacked) {
			return ActionResult.FAILED;
		}

		attacked = true;
		target.health -= dealingDamage;
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
	public ActionResult attackCP(CapturePoint target) {
		// Maybe some extra shit???
		return super.attackCP(target);
	}
	
	@Override
	public void defend(Troop attacker) {
		super.defend(attacker);
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
