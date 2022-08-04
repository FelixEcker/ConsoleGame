package de.feckert.congame.common;

import de.feckert.congame.common.troops.Troop;
import de.feckert.congame.server.Server;
import de.feckert.congame.util.ActionResult;
import de.feckert.congame.util.FactoryHelper;

import java.io.IOException;

public class CapturePoint extends Troop {
	public int owner;
	public float defenseHealth;
	public float defenseDmg;
	public int x, y;
	public boolean fullHealedPostCapture;

	public int nFactories = 4;
	public int factoriesInUse = 0;
	public String[] factoryProductions;
	public int[] factoryProgress;

	public CapturePoint(int owner, int x, int y, float damage) {
		super(owner);
		this.owner = owner;
		this.y = y;
		this.defenseHealth = 1f;
		this.health = 1f;
		this.attackDmg = damage;
		this.defenseDmg = damage/2;
		this.movement = 0;
		this.x = x;

		this.factoryProgress = new int[nFactories];
		this.factoryProductions = new String[nFactories];
		for (int i = 0; i < nFactories; i++) { // initialise arrays to avoid errors
			factoryProductions[i] = "";
			factoryProgress[i] = 0;
		}
	}
	
	public void captured(int newOwner) {
		this.owner = newOwner;
		this.defenseHealth = .5f;
		this.health = .3f;
		this.fullHealedPostCapture = false;
	}

	public void defend(Troop attacker, int aX, int aY) {
		if (movementDistance(x, y, aX, aY) <= 2) {
			float dealingDamage = defenseDmg-attacker.dmgAbsorption;
			if (dealingDamage > 0) {
				attacker.health -= defenseDmg;
			}
		}
	}
	
	public ActionResult attack(Troop target) throws IOException {
		float dealingDamage = attackDmg-target.dmgAbsorption;

		if (dealingDamage <= 0) {
			return ActionResult.FAILED;
		}

		target.health -= dealingDamage;
		if (target.health <= 0) {
			return ActionResult.TARGET_DIED;
		} else {
			Server.ooStreams[Server.whoseTurn].writeObject("msg#action.attack.target_defends");
			target.defend(this);
		}

		if (health <= 0) {
			return ActionResult.POINT_CAPTURABLE;
		}

		return ActionResult.SUCCESS;
	}
	
	public void update() {
		// Healing logic according to scraps.txt (See "Capture Points")
		if (factoriesInUse < nFactories) {
			if (defenseHealth != 1f) {
				if (!fullHealedPostCapture) {
					defenseHealth += .2f;
					if (defenseHealth >= 1f) {
						defenseHealth = 1f;
						fullHealedPostCapture = true;
					}
				} else {
					defenseHealth += .1f;
					if (defenseHealth >= 1f) defenseHealth = 1f;
				}
			}

			if (health != 1f) {
				if (health > .85f) health += .075f;
				if (health < .85f) health += .15f;
				if (health > 1f) health = 1f;
			}
		}

		if (factoriesInUse != 0) {
			for (int i = 0; i < nFactories; i++) {
				String production  = factoryProductions[i];
				if (production.matches("")) continue;

				int progress = factoryProgress[i];
				FactoryHelper.update(Server.world, this, i, production, progress);
			}
		}
	}

	public boolean addProduction(String production) {
		if (factoriesInUse >= nFactories) return false;
		for (int i = 0; i < nFactories; i++) { // Find free factory and make it busy
			if (!factoryProductions[i].matches("")) continue;
			factoryProductions[i] = production;
			factoryProgress[i] = 0;
			break;
		}

		factoriesInUse++;
		return true;
	}

	@Override
	public String toString() {
		return "Coordinates: "+Server.coordString(x, y)+", Owner: "+owner+", Defense: "+(defenseHealth*100)+"% Core Health: "+(health*100);
	}
	
	public static boolean capturable(CapturePoint cp) {
		return cp.health == 0 && cp.defenseHealth < .05f;
	}
}
