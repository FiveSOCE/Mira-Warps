package com.mira.warps.listener;

import com.mira.warps.MiraWarpsPlugin;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.Locale;

public final class SpawnFeatureListener implements Listener {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();
    private static final String PREFIX = "&5&lMira &8&l>> &r";

    private final MiraWarpsPlugin plugin;

    public SpawnFeatureListener(MiraWarpsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!plugin.getConfig().getBoolean("spawn.enabled", true)) return;

        String raw = event.getMessage().substring(1).trim();
        if (raw.isBlank()) return;
        String[] parts = raw.split("\\s+");
        String label = parts[0].toLowerCase(Locale.ROOT);

        if (label.equals("spawn")) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            if (!player.hasPermission("mirawarps.spawn")) {
                message(player, "&cYou do not have permission to use spawn.");
                return;
            }
            Bukkit.dispatchCommand(player, "essentials:spawn");
            return;
        }

        if (label.equals("setspawn")) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            if (!player.hasPermission("mirawarps.setspawn")) {
                message(player, "&cYou do not have permission to set spawn.");
                return;
            }
            Bukkit.dispatchCommand(player, "essentials:setspawn");
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!plugin.getConfig().getBoolean("spawn.enabled", true)
                || !plugin.getConfig().getBoolean("spawn.first-join", true)
                || event.getPlayer().hasPlayedBefore()) {
            return;
        }
        Bukkit.getScheduler().runTask(plugin, () ->
                Bukkit.dispatchCommand(event.getPlayer(), "essentials:spawn"));
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (!plugin.getConfig().getBoolean("spawn.enabled", true)
                || !plugin.getConfig().getBoolean("spawn.respawn-at-spawn", false)) {
            return;
        }
        Bukkit.getScheduler().runTask(plugin, () ->
                Bukkit.dispatchCommand(event.getPlayer(), "essentials:spawn"));
    }

    private void message(Player player, String raw) {
        player.sendMessage(LEGACY.deserialize(PREFIX + raw));
    }
}
