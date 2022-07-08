package de.feckert.congame;

import de.feckert.congame.troops.Troop;
import de.feckert.congame.util.ActionResult;

public class CapturePoint extends Troop {
	public int owner;
	public float defenseHealth;
	
	public CapturePoint(int owner, float defenseHealth, float coreHealth, float damage) {
		super(false);
		this.owner = owner;
		this.defenseHealth = defenseHealth;
		this.health = coreHealth;
		this.attackDmg = damage;
		this.movement = 0;
	}
	
	public void captured(int newOwner) {
		this.owner = newOwner;
		this.defenseHealth = .5f;
		this.health = .3f;
	}
	
	public void defend(Troop attacker) {
		// TODO: defend code
	}
	
	public ActionResult attack(Troop target) {
		float dealingDamage = attackDmg-target.dmgAbsorption;

		if (dealingDamage <= 0) {
			return ActionResult.FAILED;
		}

		target.health -= dealingDamage;
		if (target.health <= 0) {
			return ActionResult.TARGET_DIED;
		} else {
			Console.message("attack.target_defends");
			target.defend(this);
		}

		if (health <= 0) {
			return ActionResult.POINT_CAPTURABLE;
		}

		return ActionResult.SUCCESS;
	}
}
