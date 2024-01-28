package com.andrei1058.bedwars.sidebar;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import com.andrei1058.bedwars.api.events.sidebar.PlayerSidebarInitEvent;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.api.server.ServerType;
import com.andrei1058.bedwars.api.sidebar.ISidebar;
import com.andrei1058.bedwars.api.sidebar.ISidebarService;
import com.andrei1058.spigot.sidebar.SidebarManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.andrei1058.bedwars.BedWars.config;
import static com.andrei1058.bedwars.api.language.Language.getScoreboard;

public class SidebarService implements ISidebarService {

    private static SidebarService instance;

    private final SidebarManager sidebarHandler;
    private final HashMap<UUID, BwSidebar> sidebars = new HashMap<>();


    public static boolean init() {
        if (null == instance) {
            instance = new SidebarService();
        }
        return instance.sidebarHandler != null;
    }

    private SidebarService() {
        sidebarHandler = SidebarManager.init();
    }

    public void giveSidebar(@NotNull Player player, @Nullable IArena arena, boolean delay) {
        BwSidebar sidebar = sidebars.getOrDefault(player.getUniqueId(), null);

        // check if we might need to remove the existing sidebar
        if (null != sidebar) {
            if (null == arena) {
                // if sidebar is disabled in lobby on shared or multi-arena mode
                if (!config.getBoolean(ConfigPath.SB_CONFIG_SIDEBAR_USE_LOBBY_SIDEBAR) ||
                        BedWars.getServerType() == ServerType.SHARED) {
                    this.remove(sidebar);
                    return;
                }
            } else {
                // if sidebar is disabled in game
                if (!config.getBoolean(ConfigPath.SB_CONFIG_SIDEBAR_USE_GAME_SIDEBAR)) {
                    this.remove(sidebar);
                    return;
                }
            }
        }

        // if sidebar was null but still disabled for lobbies
        if (!config.getBoolean(ConfigPath.SB_CONFIG_SIDEBAR_USE_LOBBY_SIDEBAR) && null == arena) {
            return;
        }
        // if sidebar was null but still disabled in game
        if (!config.getBoolean(ConfigPath.SB_CONFIG_SIDEBAR_USE_GAME_SIDEBAR) && null != arena) {
            return;
        }

        // set sidebar lines based on game state or lobby
        List<String> lines = null;
        List<String> title;
        if (null == arena) {
            if (BedWars.getServerType() != ServerType.SHARED) {
                lines = Language.getList(player, Messages.SCOREBOARD_LOBBY);
            }
        } else {
            if (arena.getStatus() == GameState.waiting) {
                if (arena.isSpectator(player)) {
                    lines = getScoreboard(player, "sidebar." + arena.getGroup() + ".waiting.spectator", Messages.SCOREBOARD_DEFAULT_WAITING_SPEC);
                } else {
                    lines = getScoreboard(player, "sidebar." + arena.getGroup() + ".waiting.player", Messages.SCOREBOARD_DEFAULT_WAITING);
                }
            } else if (arena.getStatus() == GameState.starting) {
                if (arena.isSpectator(player)) {
                    lines = getScoreboard(player, "sidebar." + arena.getGroup() + ".starting.spectator", Messages.SCOREBOARD_DEFAULT_STARTING_SPEC);
                } else {
                    lines = getScoreboard(player, "sidebar." + arena.getGroup() + ".starting.player", Messages.SCOREBOARD_DEFAULT_STARTING);
                }
            } else if (arena.getStatus() == GameState.playing) {
                if (arena.isSpectator(player)) {
                    lines = getScoreboard(player, "sidebar." + arena.getGroup() + ".playing.spectator", Messages.SCOREBOARD_DEFAULT_PLAYING_SPEC);
                } else {
                    lines = getScoreboard(player, "sidebar." + arena.getGroup() + ".playing.alive", Messages.SCOREBOARD_DEFAULT_PLAYING);
                }
            } else if (arena.getStatus() == GameState.restarting) {

                ITeam holderTeam = arena.getTeam(player);
                ITeam holderExTeam = null == holderTeam ? arena.getExTeam(player.getUniqueId()) : null;

                if (null == holderTeam && null == holderExTeam) {
                    lines = getScoreboard(player, "sidebar." + arena.getGroup() + ".restarting.spectator", Messages.SCOREBOARD_DEFAULT_RESTARTING_SPEC);
                } else {
                    if (null == holderTeam && holderExTeam.equals(arena.getWinner())) {
                        lines = getScoreboard(player, "sidebar." + arena.getGroup() + ".restarting.winner-eliminated", Messages.SCOREBOARD_DEFAULT_RESTARTING_WIN2);
                    } else if (null == holderExTeam && holderTeam.equals(arena.getWinner())) {
                        lines = getScoreboard(player, "sidebar." + arena.getGroup() + ".restarting.winner-alive", Messages.SCOREBOARD_DEFAULT_RESTARTING_WIN1);
                    } else {
                        lines = getScoreboard(player, "sidebar." + arena.getGroup() + ".restarting.loser", Messages.SCOREBOARD_DEFAULT_RESTARTING_LOSER);
                    }
                }
            }
        }

        // if we do not have lines we eventually remove the sidebar
        if (null == lines || lines.isEmpty()) {
            if (null != sidebar) {
                this.remove(sidebar);
            }
            return;
        }

        // title is the first line from array
        title = new ArrayList<>(Arrays.asList(lines.get(0).split(",")));
        if (lines.size() == 1) {
            lines = new ArrayList<>();
        }
        lines = lines.subList(1, lines.size());

        // at this point we are sure we need a sidebar instance
        boolean newlyAdded = false;
        if (null == sidebar) {
            sidebar = new BwSidebar(player);
            newlyAdded = true;

            PlayerSidebarInitEvent event = new PlayerSidebarInitEvent(player, sidebar);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }
        }
        sidebar.setContent(title, lines, arena);

        if (newlyAdded) {
            sidebars.put(player.getUniqueId(), sidebar);
        }
    }

    /**
     * Kill a sidebar lifecycle.
     */
    public void remove(@NotNull BwSidebar sidebar) {
        this.sidebars.remove(sidebar.getPlayer().getUniqueId());
        sidebar.remove();
    }

    public void remove(@NotNull Player player) {
        BwSidebar sidebar = this.sidebars.remove(player.getUniqueId());
        if (null != sidebar) {
            sidebar.remove();
        }
    }

    public static SidebarService getInstance() {
        return instance;
    }

    protected SidebarManager getSidebarHandler() {
        return sidebarHandler;
    }

    public void refreshTitles() {
        this.sidebars.forEach((k, v) -> v.getHandle().refreshTitle());
    }

    public void refreshPlaceholders() {
        this.sidebars.forEach((k, v) -> v.getHandle().refreshPlaceholders());
    }

    public void refreshPlaceholders(IArena arena) {
        this.sidebars.forEach((k, v) -> {
            if (v.getArena() != null)
                if (v.getArena().equals(arena)) {
                    v.getHandle().refreshPlaceholders();
                }
        });
    }

    public void refreshTabList() {
        this.sidebars.forEach((k, v) -> v.getHandle().playerTabRefreshAnimation());
    }

    public void refreshTabHeaderFooter() {
        this.sidebars.forEach((k, v) -> {
            if (null != v && null != v.getHeaderFooter()) {
                this.sidebarHandler.sendHeaderFooter(v.getPlayer(), v.getHeaderFooter());
            }
        });
    }

    public void refreshHealth() {
        this.sidebars.forEach((k, v) -> {
            if (null != v.getArena()) {
                v.getHandle().playerHealthRefreshAnimation();
                for (Player player : v.getArena().getPlayers()) {
                    v.getHandle().setPlayerHealth(player, (int) Math.ceil(player.getHealth()));
                }
            }
        });
    }

    @Override
    public @Nullable ISidebar getSidebar(@NotNull Player player) {
        return this.sidebars.getOrDefault(player.getUniqueId(), null);
    }

    public void refreshHealth(IArena arena, Player player, int health) {
        this.sidebars.forEach((k, v) -> {
            if (null != v.getArena() && v.getArena().equals(arena)) {
                v.getHandle().setPlayerHealth(player, health);
            }
        });
    }

    public void handleReJoin(IArena arena, Player player) {
        this.sidebars.forEach((k, v) -> {
            if (null != v.getArena() && v.getArena().equals(arena)) {
                v.giveUpdateTabFormat(player, false);
            }
        });
    }

    public void handleJoin(IArena arena, Player player, @Nullable Boolean spectator) {
        this.sidebars.forEach((k, v) -> {
            if (null != v.getArena() && v.getArena().equals(arena)) {
                if (!v.getPlayer().equals(player)) {
                    v.giveUpdateTabFormat(player, false, spectator);
                }
            }
        });
    }

    public void applyLobbyTab(Player player) {
        this.sidebars.forEach((k, v) -> {
            if (null == v.getArena()) {
                if (!v.getPlayer().equals(player)) {
                    v.giveUpdateTabFormat(player, false);
                }
            }
        });
    }

    public void handleInvisibility(ITeam team, Player player) {
        this.sidebars.forEach((k, v) -> {
            if (null != v.getArena() && v.getArena().equals(team.getArena())) {
                v.handleInvisibilityPotion(player);
            }
        });
    }
}
