package de.feckert.congame.util;

/**
 * This enum is used to make path"finding" less clunky.
 * 
 * @see de.feckert.congame.common.troops.Troop#obstacleOnRoute(int, int, int, Direction) 
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

    public static Direction[] determineDirections(int originX, int originY, int destX, int destY) {
        Direction direction = determineDirection(originX, originY, destX, destY);

        switch (direction) {
            case NORTH: return new Direction[] {NORTH, NONE};
            case EAST: return new Direction[] {NONE, EAST};
            case SOUTH: return new Direction[] {SOUTH, NONE};
            case WEST: return new Direction[] {NONE, WEST};
            case NORTH_EAST: return new Direction[] {NORTH, EAST};
            case NORTH_WEST: return new Direction[] {NORTH, WEST};
            case SOUTH_EAST: return new Direction[] {SOUTH, EAST};
            case SOUTH_WEST: return new Direction[] {SOUTH, WEST};
            default: return new Direction[] {NONE, NONE}; // This should never happen
        }
    }
}
