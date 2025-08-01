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

package com.andrei1058.bedwars.api.events.shop;

import com.andrei1058.bedwars.api.arena.IArena;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;

public class ShopInventoryUpdateEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final HashMap<String, Integer> shopItemIdentifiersSlots;
    private final IArena arena;
    private final Player player;
    private final Inventory inventory;

    /**
     * Triggered when the shop inventory is updated for a player (it has fully loaded a visual,
     * this is triggered before opening the updated inventory to the player)
     */
    public ShopInventoryUpdateEvent(HashMap<String, Integer> shopItemIdentifiersSlots, IArena arena, Player player, Inventory inventory) {
        this.shopItemIdentifiersSlots = shopItemIdentifiersSlots;
        this.arena = arena;
        this.player = player;
        this.inventory = inventory;
    }

    public IArena getArena() {
        return arena;
    }

    public Player getPlayer() {
        return player;
    }

    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Get the all the item's identifiers displayed on the shop gui, alongside their respective slots.
     */
    public HashMap<String, Integer> getShopItemIdentifiersSlots() {
        return shopItemIdentifiersSlots;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
