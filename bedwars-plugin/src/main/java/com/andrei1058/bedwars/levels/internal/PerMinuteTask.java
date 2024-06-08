/*
 * BedWars1058 - A bed wars mini-game.
 * Copyright (C) 2021 Andrei Dascălu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Contact e-mail: andrew.dascalu@gmail.com
 */

package com.andrei1058.bedwars.levels.internal;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.events.player.PlayerXpGainEvent;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.arena.Arena;
import com.andrei1058.bedwars.configuration.LevelsConfig;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import static com.andrei1058.bedwars.arena.Arena.getModMultiplier;

public class PerMinuteTask {

    private final int xp = LevelsConfig.levels.getInt("xp-rewards.per-minute");

    private BukkitTask task;

    /**
     * Create a new per minute xp reward.
     */
    public PerMinuteTask(Arena arena) {
        if (xp < 1){
            return;
        }
        int actualXP = xp;
        int currentMod = Integer.parseInt(PlaceholderAPI.setPlaceholders(arena.getPlayers().get(0), "%mercuriorandomevents_currentmodifier_num%"));
        String currentModName = PlaceholderAPI.setPlaceholders(arena.getPlayers().get(0), "%mercuriorandomevents_currentmodifier%");
        Double multiplier = getModMultiplier(currentMod);

        if (currentMod != 0) { //se c'è un modifier
            if(multiplier != null) {
                actualXP *= multiplier;
            } else {
                Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "helpop ERRORE! UNHANDLED EXCEPTION BW1058: multiplier == null!!!!!!! (xp)\ncurrentMod: " + currentMod + "\ncurrentModName: " + currentModName);
            }
        }

        int finalActualXP = actualXP;
        task = Bukkit.getScheduler().runTaskTimer(BedWars.plugin, () -> {
            for (Player p : arena.getPlayers()) {
                PlayerLevel.getLevelByPlayer ( p.getUniqueId () ).addXp (finalActualXP, PlayerXpGainEvent.XpSource.PER_MINUTE );

                if(currentMod != 0 && multiplier != null) {
                    p.sendMessage ("§6+" + finalActualXP + " XP: " + xp + " * " + multiplier + " (" + currentModName + ") -- (Tempo giocato).");
                } else {
                    p.sendMessage ( Language.getMsg ( p, Messages.XP_REWARD_PER_MINUTE ).replace ( "{xp}", String.valueOf ( finalActualXP ) ) );
                }
            }
        }, 60 * 20, 60 * 20);
    }

    /**
     * Cancel task.
     */
    public void cancel() {
        if (task != null) {
            task.cancel();
        }
    }
}
