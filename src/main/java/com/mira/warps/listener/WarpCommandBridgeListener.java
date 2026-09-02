package com.mira.warps.listener;

import com.mira.warps.gui.WarpGuiService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Locale;

public final class WarpCommandBridgeListener implements Listener {
    private final WarpGuiService gui;

    public WarpCommandBridgeListener(WarpGuiService gui) {
        this.gui = gui;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String raw = event.getMessage().trim();
        String normalized = raw.toLowerCase(Locale.ROOT);
        if (!normalized.equals("/warp") && !normalized.equals("/warps")) return;

        Player player = event.getPlayer();
        event.setCancelled(true);
        gui.open(player);
    }
}
