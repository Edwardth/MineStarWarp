/*
 * Copyright (C) 2011 MineStar.de 
 * 
 * This file is part of MineStarWarp.
 * 
 * MineStarWarp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 * 
 * MineStarWarp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MineStarWarp.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.minestar.MineStarWarp.commands.teleport;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.minestar.MineStarWarp.commands.Command;

public class TeleportPlayerToCommand extends Command {

    public TeleportPlayerToCommand(String syntax, String arguments, String node,
            Server server) {
        super(syntax, arguments, node, server);
    }

    @Override
    /**
     * Representing the command <br>
     * /tphere PLAYERNAME TARGETSNAME <br>
     * This teleports first player to the other player
     * 
     * @param player
     *            Called the command
     * @param args
     *            args[0] is the player to teleport name <br>
     *            args[1] is the targets name
     * 
     */
    public void execute(String[] args, Player player) {

        Player playerToTeleport = server.getPlayer(args[0]);
        if (playerToTeleport == null) {
            player.sendMessage("Can't find player named " + args[0]
                    + ". Maybe he is offline?");
            return;
        }
        Player target = server.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage("Can't find player named " + args[1]
                    + ". Maybe he is offline?");
            return;
        }
        playerToTeleport.teleport(target.getLocation());
    }
}
