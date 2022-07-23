package de.feckert.congame.util;

/**
 * This enum is used to make path"finding" less clunky.
 * 
 * @see de.feckert.congame.common.troops.Troop#obstacleOnRoute(int, int, int, Direction[])
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
}
