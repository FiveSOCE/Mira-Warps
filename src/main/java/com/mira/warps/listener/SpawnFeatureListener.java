package com.mira.warps.listener;

import com.mira.warps.MiraWarpsPlugin;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public final class SpawnFeatureListener implements Listener {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();
    private static final String PREFIX = "&5&lMira &8&l>> &r";

    private final MiraWarpsPlugin plugin;
    private final Map<UUID, PendingSpawn> pending = new HashMap<>();
    private final Map<UUID, Long> cooldownUntil = new HashMap<>();
    private final File dataFile;
    private YamlConfiguration data;

    public SpawnFeatureListener(MiraWarpsPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "spawn-data.yml");
        load();
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
            beginSpawn(event.getPlayer());
            return;
        }

        if (label.equals("setspawn")) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            if (!player.hasPermission("mirawarps.setspawn")) {
                message(player, "&cYou do not have permission to set spawn.");
                return;
            }
            Plugin essentialsSpawn = Bukkit.getPluginManager().getPlugin("EssentialsSpawn");
            if (essentialsSpawn == null || !essentialsSpawn.isEnabled()) {
                message(player, "&cEssentialsX Spawn is not installed or enabled.");
                return;
            }
            boolean dispatched = Bukkit.dispatchCommand(player, "essentialsspawn:setspawn");
            if (!dispatched) {
                message(player, "&cSpawn point could not be set because EssentialsX Spawn did not accept /setspawn.");
            }
        }
    }

    private void beginSpawn(Player player) {
        if (!player.hasPermission("mirawarps.spawn")) {
            message(player, "&cYou do not have permission to use spawn.");
            return;
        }

        long now = System.currentTimeMillis();
        long remaining = cooldownUntil.getOrDefault(player.getUniqueId(), 0L) - now;
        if (remaining > 0L && !player.hasPermission("mirawarps.spawn.bypass.cooldown")) {
            message(player, "&eYou can use spawn again in &f" + formatDuration(remaining) + "&e.");
            return;
        }

        cancelPending(player.getUniqueId(), false);

        int warmup = Math.max(0, plugin.getConfig().getInt("spawn.warmup-seconds", 5));
        if (warmup <= 0 || player.hasPermission("mirawarps.spawn.bypass.warmup")) {
            executeSpawn(player, true);
            return;
        }

        Location start = player.getLocation().clone();
        playCosmeticsWarmup(player, warmup);
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            pending.remove(player.getUniqueId());
            if (!player.isOnline()) return;
            executeSpawn(player, true);
        }, warmup * 20L);
        pending.put(player.getUniqueId(), new PendingSpawn(start, task));
        message(player, "&7Teleporting to spawn in &f" + warmup + "s&7. Do not move.");
    }

    private void executeSpawn(Player player, boolean applyCooldown) {
        Plugin essentialsSpawn = Bukkit.getPluginManager().getPlugin("EssentialsSpawn");
        if (essentialsSpawn == null || !essentialsSpawn.isEnabled()) {
            message(player, "&cEssentialsX Spawn is not installed or enabled.");
            return;
        }

        boolean dispatched = Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                "essentialsspawn:spawn " + player.getName());
        if (!dispatched) {
            message(player, "&cSpawn teleport could not be started because EssentialsX Spawn did not accept /spawn.");
            return;
        }
        if (applyCooldown && !player.hasPermission("mirawarps.spawn.bypass.cooldown")) {
            long seconds = Math.max(0L, plugin.getConfig().getLong("spawn.cooldown-seconds", 30L));
            if (seconds > 0L) {
                cooldownUntil.put(player.getUniqueId(), System.currentTimeMillis() + seconds * 1000L);
                save();
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!plugin.getConfig().getBoolean("spawn.cancel-on-move", true) || event.getTo() == null) return;
        PendingSpawn current = pending.get(event.getPlayer().getUniqueId());
        if (current == null) return;

        Location from = current.start();
        Location to = event.getTo();
        if (from.getWorld() != to.getWorld()
                || from.getBlockX() != to.getBlockX()
                || from.getBlockY() != to.getBlockY()
                || from.getBlockZ() != to.getBlockZ()) {
            cancelPending(event.getPlayer().getUniqueId(), true);
            playCosmeticsEvent(event.getPlayer(), "teleport_cancel");
            message(event.getPlayer(), "&cSpawn teleport cancelled because you moved.");
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!plugin.getConfig().getBoolean("spawn.enabled", true)
                || !plugin.getConfig().getBoolean("spawn.first-join", true)
                || event.getPlayer().hasPlayedBefore()) {
            return;
        }
        Bukkit.getScheduler().runTask(plugin, () -> executeSpawn(event.getPlayer(), false));
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (!plugin.getConfig().getBoolean("spawn.enabled", true)
                || !plugin.getConfig().getBoolean("spawn.respawn-at-spawn", false)) {
            return;
        }
        Bukkit.getScheduler().runTask(plugin, () -> executeSpawn(event.getPlayer(), false));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cancelPending(event.getPlayer().getUniqueId(), false);
    }

    private void cancelPending(UUID player, boolean cancelTask) {
        PendingSpawn removed = pending.remove(player);
        if (removed != null && cancelTask) removed.task().cancel();
    }

    private void load() {
        plugin.getDataFolder().mkdirs();
        data = YamlConfiguration.loadConfiguration(dataFile);
        var section = data.getConfigurationSection("cooldowns");
        if (section == null) return;
        long now = System.currentTimeMillis();
        for (String raw : section.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(raw);
                long until = section.getLong(raw, 0L);
                if (until > now) cooldownUntil.put(uuid, until);
            } catch (IllegalArgumentException ignored) { }
        }
    }

    private synchronized void save() {
        data.set("cooldowns", null);
        long now = System.currentTimeMillis();
        cooldownUntil.entrySet().removeIf(entry -> entry.getValue() <= now);
        for (Map.Entry<UUID, Long> entry : cooldownUntil.entrySet()) {
            data.set("cooldowns." + entry.getKey(), entry.getValue());
        }
        try {
            data.save(dataFile);
        } catch (IOException exception) {
            plugin.getLogger().warning("Could not save spawn-data.yml: " + exception.getMessage());
        }
    }

    private String formatDuration(long millis) {
        long seconds = Math.max(1L, (millis + 999L) / 1000L);
        long minutes = seconds / 60L;
        long remainder = seconds % 60L;
        return minutes > 0L ? minutes + "m " + remainder + "s" : remainder + "s";
    }

    private void playCosmeticsWarmup(Player player, int durationSeconds) {
        Plugin cosmetics = Bukkit.getPluginManager().getPlugin("MiraCosmetics");
        if (cosmetics == null || !cosmetics.isEnabled()) return;
        for (RegisteredServiceProvider<?> registration : Bukkit.getServicesManager().getRegistrations(cosmetics)) {
            if (!registration.getService().getName().endsWith("CosmeticsApi")) continue;
            try {
                registration.getProvider().getClass()
                        .getMethod("playTeleportWarmup", Player.class, int.class)
                        .invoke(registration.getProvider(), player, durationSeconds);
            } catch (ReflectiveOperationException exception) {
                plugin.getLogger().fine("MiraCosmetics warmup API unavailable: " + exception.getMessage());
            }
            return;
        }
    }

    private void playCosmeticsEvent(Player player, String eventId) {
        Plugin cosmetics = Bukkit.getPluginManager().getPlugin("MiraCosmetics");
        if (cosmetics == null || !cosmetics.isEnabled()) return;
        try {
            cosmetics.getClass().getMethod("playEvent", Player.class, String.class, Location.class)
                    .invoke(cosmetics, player, eventId, player.getLocation());
        } catch (NoSuchMethodException ignored) {
            try {
                cosmetics.getClass().getMethod("playVisualEvent", Player.class, String.class, Location.class)
                        .invoke(cosmetics, player, eventId, player.getLocation());
            } catch (ReflectiveOperationException ignoredToo) {
                plugin.getLogger().fine("MiraCosmetics event API unavailable: " + ignoredToo.getMessage());
            }
        } catch (ReflectiveOperationException exception) {
            plugin.getLogger().fine("MiraCosmetics event API unavailable: " + exception.getMessage());
        }
    }

    private void message(Player player, String raw) {
        player.sendMessage(LEGACY.deserialize(PREFIX + raw));
    }

    private record PendingSpawn(Location start, BukkitTask task) { }
}
