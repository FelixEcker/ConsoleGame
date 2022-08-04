package de.feckert.congame.util;

import de.feckert.congame.common.CapturePoint;
import de.feckert.congame.common.World;
import de.feckert.congame.server.Server;

import java.io.IOException;
import java.util.Map;

public class FactoryHelper {
    // Todo: Decide proper production durations
    public static final Map<String, Integer> productionDurations = Map.of(
            "scout",           2,
            "artillery",       2,
            "heavyartillery",  2,
            "infantry",        2,
            "medic",           2
    );

    public static void update(World world, CapturePoint cp, int factory, String production, int progress) {
        if (productionDurations.containsKey(production)) {
            if (progress == productionDurations.get(production)) {
                // Message clients about finished production
                try {
                    int code = world.createTroopByName(production.toLowerCase(), cp.owner, cp.x, cp.y);
                    String coordString = Server.coordString(cp.x, cp.y).toLowerCase().replace(";", ":");
                    switch (code) {
                        case 0 -> {
                            Server.ooStreams[cp.owner].writeObject(String.format("msg#player.production.finished;%s;%s", production, coordString));
                            Server.ooStreams[cp.owner == 1 ? 0 : 1].writeObject(
                                    String.format("msg#opplayer.enemy.deployed_troop;%s;%s", production, coordString));
                        }
                        case 1 ->
                                Server.ooStreams[cp.owner].writeObject(String.format("msg#player.production.finished_deploy_failed;%s", production));
                        case 2 ->
                                Server.ooStreams[cp.owner].writeObject(String.format("msg#player.production.finished_deploy_failed.nvf;%s;%s", production, coordString));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Server.logger.err("Failed to message clients about finished troop production!");
                }

                // Update values for CP
                cp.factoriesInUse--;
                cp.factoryProgress[factory] = 0;
                cp.factoryProductions[factory] = "";
            } else {
                cp.factoryProgress[factory]++;
            }
        }
    }
}
