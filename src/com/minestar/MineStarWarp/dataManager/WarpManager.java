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

package com.minestar.MineStarWarp.dataManager;

import java.util.HashMap;
import java.util.TreeMap;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

import com.gemo.utils.UtilPermissions;
import com.minestar.MineStarWarp.Warp;

public class WarpManager {

    // Key = Name of Warp
    private TreeMap<String, Warp> warps;

    private final DatabaseManager dbManager;

    private static WarpManager instance;

    private final static int DEFAULTS = 0;
    private final static int PROBE = 1;
    private final static int FREE = 2;
    private final static int PAY = 3;

    private final int[] maximumWarps = new int[4];

    private WarpManager(DatabaseManager dbManager, Configuration config) {
        this.dbManager = dbManager;
        warps = dbManager.loadWarpsFromDatabase();

        maximumWarps[DEFAULTS] = config.getInt("warps.default", 0);
        maximumWarps[PROBE] = config.getInt("warps.probe", 2);
        maximumWarps[FREE] = config.getInt("warps.default", 5);
        maximumWarps[PAY] = config.getInt("warps.default", 9);
    }

    public static WarpManager getInstance(DatabaseManager dbManager,
            Configuration config) {
        if (instance == null)
            instance = new WarpManager(dbManager, config);
        return instance;
    }

    public boolean isWarpExisting(String name) {
        return warps.containsKey(name);
    }

    public void addWarp(Player creator, String name, Warp warp) {
        warps.put(name, warp);
        if (dbManager.addWarp(creator, name, warp)) {
            creator.sendMessage(ChatColor.AQUA + name
                    + "was sucessfully created!");
        }
        else {
            warps.remove(name);
            creator.sendMessage(ChatColor.RED
                    + "ERROR! Can't save the warp in the database! The warp was not created! Contact an admin!");
        }
    }

    public void deleteWarp(Player player, String name) {
        Warp warp = warps.remove(name);
        if (dbManager.deleteWarp(name))
            player.sendMessage(ChatColor.AQUA + name
                    + " was sucessfully deleted!");
        else {
            warps.put(name, warp);
            player.sendMessage(ChatColor.RED
                    + "ERROR! Can't delete the warp from the database! Warp was not deleted! Contact an admin!");
        }

    }

    public void addGuest(Player player, String warpName, String guest) {
        Warp warp = warps.get(warpName);
        warp.invitePlayer(guest);
        if (dbManager.changeGuestList(warp.getGuestsAsString(), warpName))
            player.sendMessage(ChatColor.AQUA + guest
                    + " was sucessfully invited into " + warpName);
        else {
            warp.uninvitePlayer(guest);
            player.sendMessage(ChatColor.RED
                    + "ERROR! Can't add "
                    + guest
                    + " as an guest to the database! He was not invited! Contact an admin!");
        }
    }

    public void removeGuest(Player player, String warpName, String guest) {
        Warp warp = warps.get(warpName);
        warp.uninvitePlayer(guest);
        if (dbManager.changeGuestList(warp.getGuestsAsString(), warpName))
            player.sendMessage(ChatColor.AQUA + guest
                    + " was sucessfully uninvited from " + warpName);
        else {
            warp.invitePlayer(guest);
            player.sendMessage(ChatColor.RED
                    + "ERROR! Can't remove "
                    + guest
                    + " as an guest to the database! He was not uninvited! Contact an admin!");
        }
    }

    public int countWarpsCreatedBy(Player player) {
        int counter = 0;
        for (Warp warp : warps.values()) {
            if (!warp.isPublic() && warp.getOwner().equals(player.getName()))
                ++counter;
        }
        return counter;
    }

    public int countWarpsCanUse(Player player) {
        int counter = 0;
        for (Warp warp : warps.values()) {
            if (warp.canUse(player.getName()))
                ++counter;
        }
        return counter;
    }

    public boolean hasFreeWarps(Player player) {
        if (player.isOp())
            return true;
        int warpCount = countWarpsCreatedBy(player);
        String groupName = UtilPermissions.getGroupName(player);

        if (groupName.equals("default"))
            return warpCount < this.maximumWarps[DEFAULTS];
        else if (groupName.equals("probe"))
            return warpCount < this.maximumWarps[PROBE];
        else if (groupName.equals("free"))
            return warpCount < this.maximumWarps[FREE];
        else if (groupName.equals("pay"))
            return warpCount < this.maximumWarps[PAY];

        return false;
    }

    /**
     * @param name
     *            The case-sensitive name
     * @return Warp have the same, case-sensitive name as given. Null if no warp
     *         exists
     */
    public Warp getWarp(String name) {
        return warps.get(name);
    }

    /**
     * Return a warp that have the same name. If no warp exists with the same
     * name, the first warp, that starts with the name will returned.
     * 
     * @param name
     *            Same name or similiar name
     * @return Warp matching name
     */
    public Warp getSimiliarWarp(String name) {
        Warp warp = warps.get(name);
        if (warp != null)
            return warp;
        for (String tempName : warps.keySet())
            if (tempName.startsWith(name))
                return warps.get(tempName);
        return null;
    }

    /**
     * Example : Returning warps Castle, Castle1 and astl, because the param was
     * 'astl'
     * 
     * @return Returns a list of all warps that contains the name.
     */
    public HashMap<String, Warp> getSimiliarWarps(String name) {
        HashMap<String, Warp> similiarWarps = new HashMap<String, Warp>();
        for (String warpName : warps.keySet()) {
            if (warpName.contains(name))
                similiarWarps.put(warpName, warps.get(warpName));
        }
        if (similiarWarps.size() == 0)
            return null;
        return similiarWarps;
    }

    public HashMap<String, Warp> getWarpsPlayerIsOwner(String playerName) {
        HashMap<String, Warp> warpList = new HashMap<String, Warp>();
        for (String warpName : warps.keySet()) {
            Warp tempWarp = warps.get(warpName);
            if (tempWarp.isOwner(playerName))
                warpList.put(warpName, tempWarp);
        }
        return warpList.size() > 0 ? warpList : null;
    }

    public HashMap<String, Warp> getWarpsForList(int pageNumber,
            int warpsPerPage) {
        HashMap<String, Warp> warpList = new HashMap<String, Warp>();
        String[] keys = new String[warps.size()];
        keys = warps.keySet().toArray(keys);
        for (int i = 0; i < warpsPerPage; ++i) {
            String key = keys[((pageNumber - 1) * warpsPerPage) + i];
            warpList.put(key, warps.get(key));
        }
        return warpList.size() > 0 ? warpList : null;
    }

    public void changeAccess(Player player, boolean toPublic, String name) {
        if (toPublic) {
            if (dbManager.removeGuestsList(name)) {
                player.sendMessage(ChatColor.AQUA + "Warp " + name
                        + " is now public!");
                warps.get(name).setAccess(toPublic);
            }
            else
                player.sendMessage(ChatColor.RED
                        + "ERROR! Can't change access in the database! The access is not changed! Contact an admin!");
        }
        else {
            if (dbManager.changeGuestList("", name)) {
                player.sendMessage(ChatColor.AQUA + "Warp " + name
                        + " is now private.!");
                player.sendMessage(ChatColor.AQUA
                        + "Use /warp invite <Player> " + name
                        + " to invite the player to this warp!");
                warps.get(name).setAccess(toPublic);
            }
            else
                player.sendMessage(ChatColor.RED
                        + "ERROR! Can't change access in the database! The access is not changed! Contact an admin!");
        }
    }
}
