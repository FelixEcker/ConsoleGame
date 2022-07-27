package de.feckert.congame.common.troops;

import de.feckert.congame.common.CapturePoint;
import de.feckert.congame.server.Server;
import de.feckert.congame.util.ActionResult;
import de.feckert.congame.util.Direction;

import java.io.IOException;

public class Medic extends Troop {
    public static final int pMaxHealDistance = 3;
    public static final int sHealSquareLength = 5;
    public int primaryCooldown = 0;
    public int secondaryCooldown = 0;

    public Medic(int owner) {
        super(owner);

        name = "Medic";
        displayChar = 'M';
        attackDmg = 0f;
        dmgAbsorption = .25f;
        defDmgAbsorption = .25f;
        waterTravel = true;
        movement = 5;
    }

    @Override
    public void update() {
        super.update();

        if (primaryCooldown != 0) primaryCooldown--;
        if (secondaryCooldown != 0) secondaryCooldown--;
    }

    @Override
    public ActionResult primaryAction() {
        if (primaryCooldown != 0) return ActionResult.FAILED;
        pUsed = true;

        try {
            int[] ownCoords = Server.world.troopCoords(this);

            // Get coordinates for healing target
            Server.ooStreams[Server.whoseTurn].writeObject("msg#action.primary.medic.choose_target");
            String rawCoords = Server.getInput(Server.whoseTurn);
            int[] coordPair = Server.translateCoordinates(rawCoords);
            int tX = coordPair[0], tY = coordPair[1];

            // Check for any errors
            if (ownCoords[0] == -1 || ownCoords[1] == -1) { // This should realistically never happen, but I went to make sure
                Server.broadcast("An error occured whilst executing the primary action of a medic.\n The World#troopCoords function returned the values for a unfound troop");
                return ActionResult.FAILED;
            }
            if (movementDistance(ownCoords[0], ownCoords[1], tX, tY) > pMaxHealDistance) { // Check if the target is in healing range
                Server.ooStreams[Server.whoseTurn].writeObject("msg#action.primary.medic.target_oor");
                return ActionResult.FAILED;
            } // Check if the target is obstructed
            if (obstacleOnRoute(ownCoords[0], ownCoords[1], pMaxHealDistance, Direction.determineDirections(ownCoords[0], ownCoords[1], tX, tY))) {
                Server.ooStreams[Server.whoseTurn].writeObject("msg#action.primary.medic.target_obstructed");
                return ActionResult.FAILED;
            }

            // Heal
            Server.world.troop(tX, tY).health += .5f;
            if (Server.world.troop(tX, tY).health > 1f) Server.world.troop(tX, tY).health = 1f; // make sure not to "overheal"

            Server.ooStreams[Server.whoseTurn].writeObject(String.format("msg#action.primary.medic.healed;%s;%.2f%%", rawCoords.replace(";", ":"), Server.world.troop(tX, tY).health*100));
            Server.ooStreams[Server.oppositePlayer].writeObject(String.format("msg#opplayer.enemy_troop.healed;%s;%.2f%%", rawCoords.replace(";", ":"), Server.world.troop(tX, tY).health*100));
            return ActionResult.SUCCESS;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ActionResult secondaryAction() {
        // TODO: Do testing, cant be bothered right now
        Troop[] troops = new Troop[25];
        int i = 0;

        int[] ownCoords = Server.world.troopCoords(this);
        int startX = ownCoords[0]-2;
        int startY = ownCoords[1]-2;
        if (startX < 0) startX = 0;
        if (startY < 0) startY = 0;

        for (int y = startY ; y < startY+5; y++) {
            for (int x = startX ; x < startX+5; x++) {
                if (Server.world.troopAt(x ,y)) {
                    troops[i] = Server.world.troop(x, y);
                }
                i++;
            }
        }

        for (Troop troop : troops) {
            if (troop != null) {
                troop.health += .1f;
                if (troop.health > 1f) troop.health = 1f;
            }
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public ActionResult attackCP(CapturePoint target) {
        return ActionResult.INVALID;
    }

    @Override
    public void defend(Troop attacker) {

    }
}
