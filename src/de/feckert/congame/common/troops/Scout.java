package de.feckert.congame.common.troops;

import de.feckert.congame.common.CapturePoint;
import de.feckert.congame.server.Server;
import de.feckert.congame.util.ActionResult;

import java.io.IOException;

public class Scout extends Troop {
	public Scout(int team) {
		super(team);

		name = "Scout";
		displayChar = 'S';
		attackDmg = 1f/16f;
		dmgAbsorption = 1/18f;
		defDmgAbsorption = 1/17f;
		waterTravel = true;
		movement = 8;
		movementThisTurn = movement;
	}
	
	@Override
	public ActionResult attack(Troop target) throws IOException {
		float dealingDamage = attackDmg-target.dmgAbsorption;

		if (dealingDamage <= 0 || attacked) {
			return ActionResult.FAILED;
		}

		attacked = true;
		target.health -= dealingDamage;
		if (target.health <= 0) {
			return ActionResult.TARGET_DIED;
		} else {
			Server.ooStreams[Server.whoseTurn].writeObject("msg#action.attack.target_defends");
			target.defend(this);
		}

		if (health <= 0) {
			return ActionResult.TROOP_DIED;
		}

		return ActionResult.SUCCESS;
	}

	@Override
	public ActionResult attackCP(CapturePoint target) throws IOException {
		// Maybe some extra shit???
		return super.attackCP(target);
	}
	
	@Override
	public void defend(Troop attacker) {
		super.defend(attacker);
	}

	@Override
	public boolean canTravelTo(int originX, int originY, int destX, int destY) {
		return super.canTravelTo(originX, originY, destX, destY);
	}

	@Override
	public boolean canAttack(int x, int y, int tX, int tY) {
		// here we just need to check if the scout can travel to the coordinates
		// of the target since the scout can not do ranged attacks
		return canTravelTo(x, y, tX, tY);
	}
}
