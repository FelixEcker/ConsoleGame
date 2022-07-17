package de.feckert.congame.common.troops;

import java.io.Serializable;
import java.util.ArrayList;

import de.feckert.congame.client.Console;
import de.feckert.congame.common.CapturePoint;
import de.feckert.congame.server.Server;
import de.feckert.congame.util.ActionResult;

/**
 * Base Class for all Troops.
 * */
public abstract class Troop implements Serializable {
	public static final ArrayList<String> NAMES = new ArrayList<>();
	
	static {
		NAMES.add("scout");
		NAMES.add("artillery");
		NAMES.add("heavy artillery");
		NAMES.add("infantry");
		NAMES.add("medic");
	}
	
	public static int movementDistance(int originX, int originY, int destX, int destY) {
		return Math.abs(destX - originX) + Math.abs(destY - originY);
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////
	
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


	public ActionResult attack(Troop target) {
		return ActionResult.INVALID;
	}

	// Attacking Capture Points takes different damage calculation, logic and return values
	// Thus requires seperate method; The code for damage calculation and determining the ActionResult
	// should remain the same in all cases, the method could be overriden for extra things on attack
	// like inflicting certain effects on the capture points as part of the attack.
	public ActionResult attackCP(CapturePoint target) {
		if (attacked) {
			Console.message("troop.attack.used");
			return ActionResult.FAILED;
		}
		
		attacked = true;
		float targetDHealth = target.defenseHealth;
		float targetCHealth = target.health;
		
		if (targetDHealth <= .85) {
			targetDHealth -= attackDmg*.75;
			targetCHealth -= attackDmg*.25;
		} else {
			targetDHealth -= attackDmg;
		}

		if (targetDHealth < 0) targetDHealth = 0;
		if (targetCHealth < 0) targetCHealth = 0;
		target.defenseHealth = targetDHealth;
		target.health = targetCHealth;
		
		if (targetCHealth <= 0 && targetDHealth <= .05) {
			return ActionResult.POINT_CAPTURABLE;
		}
		
		Console.message("attack.cp_defends");
		target.defend(this);
		
		if (health <= 0) {
			return ActionResult.TROOP_DIED;
		}
		
		return ActionResult.SUCCESS;
	}
	
	public void defend(Troop attacker) {
		float dealingDamage = attackDmg-attacker.defDmgAbsorption;
		if (dealingDamage <= 0) return;
		attacker.health -= dealingDamage;
	}

	public ActionResult primaryAction() {
		return ActionResult.INVALID;
	}

	public ActionResult secondaryAction() {
		return ActionResult.INVALID;
	}
	
	public void update() {
		movementThisTurn = movement;
		attacked = false;
	}

	// Contains some universal movement checks
	public boolean canTravelTo(int originX, int originY, int destX, int destY) {
		if (!Server.world.troopAt(originX, originY)) return false;
		if (movementThisTurn <= 0) return false;
		if (movementDistance(originX, originY, destX, destY) > movementThisTurn) return false;
		if (Server.world.map[destY][destX] == '^') return false;
		return true;
	}

	public String toString() {
		return ""+displayChar;
	}
}
