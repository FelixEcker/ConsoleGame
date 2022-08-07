package de.feckert.congame.common.troops;

import de.feckert.congame.server.Server;
import de.feckert.congame.util.ActionResult;

import java.io.IOException;

public class Artillery extends Troop {
    public static final int primaryRange = 3;
    public int primaryCooldown = 0;
    public float primaryDamage = .1f;

    public Artillery(int owner) {
        super(owner);

        name = "Artillery";
        displayChar = 'A';
        attackDmg = 0.125f;
        dmgAbsorption = 0.4f;
        defDmgAbsorption = .45f;
        waterTravel = true;
        movement = 4;
    }

    @Override
    public ActionResult primaryAction() {
        if (pUsed || primaryCooldown != 0 || attacked) return ActionResult.FAILED;
        int tx, ty, x, y;
        try {
            Server.ooStreams[Server.whoseTurn].writeObject("msg#action.primary.artillery.choose_target");
            String raw = Server.getInput(Server.whoseTurn);
            int[] cpair = Server.translateCoordinates(
                   raw
            );
            tx = cpair[0];
            ty = cpair[1];

            int[] ocpair = Server.world.troopCoords(this);
            x = ocpair[0];
            y = ocpair[1];

            // Target Validity Checking
            if (movementDistance(x, y, tx, ty) > primaryRange) {
                Server.ooStreams[Server.whoseTurn].writeObject("msg#action.primary.artillery.target_oor");
                return ActionResult.FAILED;
            }

            // Valid Troop
            if (!Server.world.troopAt(tx, ty)) {
                Server.ooStreams[Server.whoseTurn].writeObject("msg#action.primary.artillery.invalid_target");
                return ActionResult.FAILED;
            }
            Troop troop = Server.world.troop(tx, ty);
            if (troop.team == this.team) {
                Server.ooStreams[Server.whoseTurn].writeObject("msg#action.primary.artillery.invalid_target");
                return ActionResult.FAILED;
            }

            // Do Attack
            troop.health -= primaryDamage;
            Server.ooStreams[Server.oppositePlayer].writeObject(String.format("msg#opplayer.troop_attacked.success;%s;%s;%s", raw.replace(";", ":"),
                    troop.health, health));

            attacked = true;
            primaryCooldown = 4;
            return ActionResult.SUCCESS;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
