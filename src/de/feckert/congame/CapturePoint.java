package de.feckert.congame;

import de.feckert.congame.troops.Troop;
import de.feckert.congame.util.ActionResult;
import de.feckert.congame.util.Console;

public class CapturePoint extends Troop {
	public int owner;
	public float defenseHealth;
	public float defenseDmg;
	public int x, y;
	public boolean fullHealedPostCapture;
	
	public CapturePoint(int owner, int x, int y, float damage) {
		super(false);
		this.owner = owner;
		this.y = y;
		this.defenseHealth = 1f;
		this.health = 1f;
		this.attackDmg = damage;
		this.defenseDmg = damage/2;
		this.movement = 0;
		this.x = x;
	}
	
	public void captured(int newOwner) {
		this.owner = newOwner;
		this.defenseHealth = .5f;
		this.health = .3f;
		this.fullHealedPostCapture = false;
	}
	
	public void defend(Troop attacker, int aX, int aY) {
		if (movementDistance(x, y, aX, aY) <= 2) {
			float dealingDamage = attackDmg-attacker.dmgAbsorption;
			if (dealingDamage > 0) {
				attacker.health -= attackDmg;
			}
		}
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
	
	public void update() {
		// Healing logic according to scraps.txt (See "Capture Points")
		if (defenseHealth != 1f) {
			if (!fullHealedPostCapture) {
				defenseHealth += .2f;
				if (defenseHealth >= 1f) {
					defenseHealth = 1f;
					fullHealedPostCapture = true;
				}
			} else {
				defenseHealth += .1f;
				if (defenseHealth >= 1f)defenseHealth = 1f;
			}
		}
		
		if (health != 1f) {
			if (health > .85f) health += .075f;
			if (health < .85f) health += .15f;
			if (health > 1f) health = 1f;
		}
	}
	
	public static boolean capturable(CapturePoint cp) {
		return cp.health == 0 && cp.defenseHealth < .05f;
	}
}
