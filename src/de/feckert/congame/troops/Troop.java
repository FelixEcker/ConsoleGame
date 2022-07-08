package de.feckert.congame.troops;

import de.feckert.congame.World;
import de.feckert.congame.util.ActionResult;

/**
 * Base Class for all Troops.
 * */
public abstract class Troop {
	public boolean team = false;     // False = Enemy ; True = Self
	public float health = 1.0f;      // Health of the Troop
	public float attackDmg = 0.0f;   // How much damage the Troop deals
	public float dmgAbsorption = 0.0f; // How much damage the Troop absorps on attacks
	public float defDmgAbsorption = 0.0f; // How much damage the Troop absorps on defenses
	public String name;              // Name of the Troop
	public char displayChar = '?';   // Character the troop should be displayed as
	public int movement     = 0;     // How many tiles the troop can move in a turn
	public int movementThisTurn = 0; // How far the troop can move in the current turn
	public boolean waterTravel = false; // Sets if the troop can travel on water
	public boolean canCapture = true; // Sets if the troop can capture a point
	public boolean attacked = false; // Should be set to true once the troop attacked something, reset every round
	public boolean pUsed = false; // True once primary action was used, reset every round
	public boolean sUsed = false; // True once secondary action was used, reset every round

	public Troop(boolean team) {
		this.team = team;
	}

	public static int movementDistance(int originX, int originY, int destX, int destY) {
		return Math.abs(destX - originX) + Math.abs(destY - originY);
	}

	public ActionResult attack(Troop target) {
		return ActionResult.INVALID;
	}

	public void defend(Troop attacker) {}

	public ActionResult primaryAction() {
		return ActionResult.INVALID;
	}

	public ActionResult secondaryAction() {
		return ActionResult.INVALID;
	}

	// Contains some universal movement checks
	public boolean canTravelTo(int originX, int originY, int destX, int destY) {
		if (!World.troopAt(originX, originY)) return false;
		if (movementThisTurn <= 0) return false;
		if (movementDistance(originX, originY, destX, destY) > movementThisTurn) return false;
		if (World.map[destY][destX] == '^') return false;
		return true;
	}

	public void die() {}

	public String toString() {
		return ""+displayChar;
	}

	public void update() {
		movementThisTurn = movement;
	}
}
