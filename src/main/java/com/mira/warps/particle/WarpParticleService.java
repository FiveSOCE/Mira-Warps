package com.mira.warps.particle;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class WarpParticleService {
    private final JavaPlugin plugin;

    public WarpParticleService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void play(Player player) {
        if (!plugin.getConfig().getBoolean("particles.enabled", true)) return;

        int duration = Math.max(2, plugin.getConfig().getInt("particles.duration-ticks", 24));
        int interval = Math.max(1, plugin.getConfig().getInt("particles.interval-ticks", 2));
        int points = Math.max(8, plugin.getConfig().getInt("particles.points-per-ring", 20));
        double radius = Math.max(0.2D, plugin.getConfig().getDouble("particles.radius", 0.85D));
        float size = (float) Math.max(0.2D, plugin.getConfig().getDouble("particles.size", 1.15D));
        double head = plugin.getConfig().getDouble("particles.head-height", 1.75D);
        double body = plugin.getConfig().getDouble("particles.body-height", 0.95D);
        double feet = plugin.getConfig().getDouble("particles.feet-height", 0.20D);

        Particle.DustOptions blue = new Particle.DustOptions(Color.fromRGB(45, 140, 255), size);
        Particle.DustOptions red = new Particle.DustOptions(Color.fromRGB(255, 70, 70), size);
        Particle.DustOptions green = new Particle.DustOptions(Color.fromRGB(70, 255, 110), size);

        final BukkitTask[] holder = new BukkitTask[1];
        final int[] elapsed = {0};

        holder[0] = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline() || elapsed[0] >= duration) {
                holder[0].cancel();
                return;
            }

            Location base = player.getLocation();
            double rotation = elapsed[0] * 0.18D;
            ring(player, base, head, radius, points, rotation, blue);
            ring(player, base, body, radius, points, -rotation + Math.PI / 3D, red);
            ring(player, base, feet, radius, points, rotation + (Math.PI * 2D / 3D), green);
            elapsed[0] += interval;
        }, 2L, interval);
    }

    private void ring(Player viewer, Location base, double yOffset, double radius, int points,
                      double rotation, Particle.DustOptions dust) {
        for (int i = 0; i < points; i++) {
            double angle = rotation + (Math.PI * 2D * i / points);
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            Location particle = base.clone().add(x, yOffset, z);
            viewer.getWorld().spawnParticle(Particle.DUST, particle, 1, 0D, 0D, 0D, 0D, dust);
        }
    }
}
