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

            Server.ooStreams[Server.whoseTurn].writeObject("What troop do you want to heal (cant be obstructed or more than 5 fields away)");
            String rawCoords = Server.getInput(Server.whoseTurn);
            int[] coordPair = Server.translateCoordinates(rawCoords);
            int tX = coordPair[0], tY = coordPair[1];

            if (movementDistance(ownCoords[0], ownCoords[1], tX, tY) > pMaxHealDistance) {
                Server.ooStreams[Server.whoseTurn].writeObject("msg#action.primary.medic.target_oor");
                return ActionResult.FAILED;
            }
            if (!obstacleOnRoute(ownCoords[0], ownCoords[1], pMaxHealDistance, Direction.determineDirections(ownCoords[0], ownCoords[1], tX, tY))) {
                Server.ooStreams[Server.whoseTurn].writeObject("msg#action.primary.medic.target_obstructed");
                return ActionResult.FAILED;
            }

            Server.world.troop(tX, tY).health += .5f;
            if (Server.world.troop(tX, tY).health > 1f) Server.world.troop(tX, tY).health = 1f;

            Server.ooStreams[Server.whoseTurn].writeObject(String.format("msg#action.primary.medic.healed;%s;%.2f%%", rawCoords, Server.world.troop(tX, tY).health));
            Server.ooStreams[Server.whoseTurn].writeObject(String.format("msg#opplayer.enemy_troop.healed;%s;%.2f%%", rawCoords, Server.world.troop(tX, tY).health));
            return ActionResult.SUCCESS;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ActionResult attackCP(CapturePoint target) {
        return ActionResult.INVALID;
    }

    @Override
    public void defend(Troop attacker) {

    }
}
