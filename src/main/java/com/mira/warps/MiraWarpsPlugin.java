package com.mira.warps;

import com.mira.warps.gui.WarpGuiListener;
import com.mira.warps.gui.WarpGuiService;
import com.mira.warps.listener.WarpCommandBridgeListener;
import com.mira.warps.particle.WarpParticleService;
import net.ess3.api.IEssentials;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class MiraWarpsPlugin extends JavaPlugin {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();
    private static final String CHAT_PREFIX = "&5&lMira &8>> &r";

    @Override
    public void onEnable() {
        saveDefaultConfig();

        Plugin essentialsPlugin = getServer().getPluginManager().getPlugin("Essentials");
        if (!(essentialsPlugin instanceof IEssentials essentials)) {
            getLogger().severe("EssentialsX 2.22.0+ is required. Disabling MiraWarps.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        WarpGuiService gui = new WarpGuiService(this, essentials);
        WarpParticleService particles = new WarpParticleService(this);

        getServer().getPluginManager().registerEvents(new WarpGuiListener(gui, particles), this);
        getServer().getPluginManager().registerEvents(new WarpCommandBridgeListener(gui), this);

        PluginCommand command = getCommand("mwarps");
        if (command != null) {
            command.setExecutor((sender, cmd, label, args) -> {
                if (!(sender instanceof org.bukkit.entity.Player player)) {
                    sender.sendMessage(LEGACY.deserialize(CHAT_PREFIX + "&cMiraWarps can only be opened by a player."));
                    return true;
                }
                gui.open(player);
                return true;
            });
        }

        getLogger().info("MiraWarps enabled with " + gui.warpNames().size() + " Essentials warp(s).");
    }
}
