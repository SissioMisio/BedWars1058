package com.andrei1058.bedwars.money.internal;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.arena.Arena;
import com.andrei1058.bedwars.configuration.MoneyConfig;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import static com.andrei1058.bedwars.arena.Arena.getModMultiplier;

public class MoneyPerMinuteTask {

    private final int money = MoneyConfig.money.getInt("money-rewards.per-minute");

    private BukkitTask task;

    /**
     * Create a new per minute money reward.
     */
    public MoneyPerMinuteTask(Arena arena) {
        if (money < 1) {
            return;
        }
        task = Bukkit.getScheduler().runTaskTimer(BedWars.plugin, () -> {
            if (null == arena){
                this.cancel();
                return;
            }
            int actualMoney = money;
            int currentMod = Integer.parseInt(PlaceholderAPI.setPlaceholders(arena.getPlayers().get(0), "%mercuriorandomevents_currentmodifier_num%"));
            String currentModName = PlaceholderAPI.setPlaceholders(arena.getPlayers().get(0), "%mercuriorandomevents_currentmodifier%");
            Double multiplier = getModMultiplier(currentMod);

            if (currentMod != 0) { //se c'è un modifier
                if (multiplier != null) {
                    actualMoney *= multiplier;
                } else {
                    Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "helpop ERRORE! UNHANDLED EXCEPTION BW1058: multiplier == null!!!!!!! (coins)\ncurrentMod: " + currentMod + "\ncurrentModName: " + currentModName);
                }
            }

            for (Player p : arena.getPlayers()) {
                BedWars.getEconomy().giveMoney(p, actualMoney);

                if(currentMod != 0 && multiplier != null) {
                    p.sendMessage("§6+" + actualMoney + " Coins: " + money + " * " + multiplier + " (" + currentModName + ") -- (Tempo giocato).");
                } else {
                    p.sendMessage(Language.getMsg(p, Messages.MONEY_REWARD_PER_MINUTE).replace("{money}", String.valueOf(money)));
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
