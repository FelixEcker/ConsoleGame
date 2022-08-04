package de.feckert.congame.util;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Enum representing Cardinal Directions, comes with several utility
 * functions.
 *
 * Originally for ConsoleGame by Felix Eckert
 * */
public enum Direction {
    NORTH,
    NORTH_WEST,
    NORTH_EAST,
    SOUTH,
    SOUTH_WEST,
    SOUTH_EAST,
    EAST,
    WEST,
    NONE;

    /**
     * Helper function to get in which direction the end point lies.
     *
     * @param originX X of the start point
     * @param originY Y of the start point
     * @param destX X of the end point
     * @param destY Y of the end point
     * @return The appropriate Direction Enum.
     * */
    @SuppressWarnings("unused")
    public static Direction determineDirection(int originX, int originY, int destX, int destY) {
        if (originX > destX) { // West
            if (originY > destY) return NORTH_WEST;
            if (originY < destY) return SOUTH_WEST;
            return WEST;
        } else if (originX < destX) { // East
            if (originY > destY) return NORTH_EAST;
            if (originY < destY) return SOUTH_EAST;
            return EAST;
        } else if (originY > destY) {
            return NORTH;
        }

        return SOUTH;
    }
    /**
     * Helper function to get in which direction the end point lies.
     * Does not return the enum explicitly defined for the direction,
     * rather a pair of enums that make up the direction
     *
     * @param originX X of the start point
     * @param originY Y of the start point
     * @param destX X of the end point
     * @param destY Y of the end point
     * @return The appropriate Direction Enum Array.
     * */
    public static Direction[] determineDirections(int originX, int originY, int destX, int destY) {
        Direction[] pair = {NONE, NONE};

        if (originX > destX) { // West
            pair[1] = WEST;
            if (originY > destY) pair[0] = NORTH;
            if (originY < destY) pair[0] = SOUTH;
        } else if (originX < destX) { // East
            pair[1] = EAST;
            if (originY > destY) pair[0] = NORTH;
            if (originY < destY) pair[0] = SOUTH;
        } else if (originY > destY) {
            pair[0] = NORTH;
        } else {
            pair[1] = SOUTH;
        }

        return pair;
    }

    /**
     * Returns true if the direction is West or East
     *
     * @param direction The Direction to check
     * @return Is the direction "Horizontal"
     * */
    public static boolean isHorizontal(Direction direction) {
        return direction == WEST || direction == EAST;
    }

    /**
     * Returns a randomly chosen direction.
     *
     * @param intercardinalDirections Include directions like NORTH_EAST or SOUTH_WEST in the pick
     * @return A randomly picked direction
     * */
    public static Direction randomDirection(boolean intercardinalDirections) {
        Direction[] directions;
        if (intercardinalDirections) {
            directions = new Direction[] {NORTH, NORTH_EAST, NORTH_WEST, SOUTH, SOUTH_EAST, SOUTH_WEST, EAST, WEST};
        } else {
            directions = new Direction[] {NORTH, SOUTH, EAST, WEST};
        }

        Random random = new Random(ThreadLocalRandom.current().nextInt());
        return directions[random.nextInt(directions.length)];
    }
}
