package de.feckert.congame.util;

/**
 * An Action Result is used to tell if an action succeeded or failed,
 * for a failure because a troop doesnt have the action attempted to be
 * executed, "INVALID" should be used, if it doesnt succeed for any other
 * reason "FAILED" should be used.
 * */
public enum ActionResult {
	SUCCESS,
	FAILED,
	INVALID,
	TARGET_DIED,
	TROOP_DIED
}
