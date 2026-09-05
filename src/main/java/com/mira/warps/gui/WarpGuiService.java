package com.mira.warps.gui;

import net.ess3.api.IEssentials;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class WarpGuiService {
    private static final int MAX_ROWS = 6;
    private static final int MAX_WARPS_PER_PAGE = 28;

    private final JavaPlugin plugin;
    private final IEssentials essentials;
    private final NamespacedKey warpKey;

    public WarpGuiService(JavaPlugin plugin, IEssentials essentials) {
        this.plugin = plugin;
        this.essentials = essentials;
        this.warpKey = new NamespacedKey(plugin, "warp_name");
    }

    public void open(Player player) {
        open(player, 0);
    }

    public void open(Player player, int page) {
        if (!player.hasPermission("mirawarps.use")) {
            player.sendMessage(Component.text("You do not have permission to use warps.", NamedTextColor.RED));
            return;
        }

        List<String> warps = new ArrayList<>(warpNames());
        if (plugin.getConfig().getBoolean("spawn.show-in-warps-gui", false)) {
            warps.add(0, "__spawn__");
        }
        int maxPage = Math.max(0, (warps.size() - 1) / MAX_WARPS_PER_PAGE);
        int safePage = Math.max(0, Math.min(page, maxPage));
        int start = safePage * MAX_WARPS_PER_PAGE;
        int remaining = Math.max(0, warps.size() - start);
        int shown = Math.min(MAX_WARPS_PER_PAGE, remaining);

        int rows = rowsFor(shown, maxPage > 0);
        int size = rows * 9;
        String title = plugin.getConfig().getString("gui.title", "Warps");
        Inventory inventory = Bukkit.createInventory(new Holder(safePage), size,
                Component.text(title, NamedTextColor.DARK_PURPLE));

        ItemStack filler = glowingFiller();
        for (int slot = 0; slot < size; slot++) {
            inventory.setItem(slot, filler);
        }

        List<Integer> contentSlots = contentSlots(rows);
        for (int i = 0; i < shown && i < contentSlots.size(); i++) {
            String warp = warps.get(start + i);
            inventory.setItem(contentSlots.get(i), warpItem(warp));
        }

        int bottomStart = size - 9;
        if (safePage > 0) inventory.setItem(bottomStart + 1, control(Material.ARROW, "Previous Page", NamedTextColor.YELLOW));
        inventory.setItem(bottomStart + 4, control(configMaterial("gui.close-material", Material.BARRIER), "Close", NamedTextColor.RED));
        if (safePage < maxPage) inventory.setItem(bottomStart + 7, control(Material.ARROW, "Next Page", NamedTextColor.YELLOW));

        player.openInventory(inventory);
    }

    public String getWarp(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(warpKey, PersistentDataType.STRING);
    }

    public List<String> warpNames() {
        List<String> names = new ArrayList<>(essentials.getWarps().getList());
        names.sort(String.CASE_INSENSITIVE_ORDER);
        return names;
    }

    private int rowsFor(int count, boolean paged) {
        int contentRows = Math.max(1, (count + 6) / 7);
        int rows = contentRows + 2;
        if (paged) rows = MAX_ROWS;
        return Math.max(3, Math.min(MAX_ROWS, rows));
    }

    private List<Integer> contentSlots(int rows) {
        List<Integer> slots = new ArrayList<>();
        for (int row = 1; row < rows - 1; row++) {
            for (int col = 1; col <= 7; col++) {
                slots.add(row * 9 + col);
            }
        }
        return slots;
    }

    private ItemStack warpItem(String warp) {
        boolean spawn = "__spawn__".equalsIgnoreCase(warp);
        ItemStack item = new ItemStack(spawn
                ? configMaterial("spawn.gui-material", Material.NETHER_STAR)
                : configMaterial("gui.warp-material", Material.ENDER_EYE));
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(spawn ? "Spawn" : warp,
                spawn ? NamedTextColor.LIGHT_PURPLE : NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false));
        meta.getPersistentDataContainer().set(warpKey, PersistentDataType.STRING, spawn ? "__spawn__" : warp);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack glowingFiller() {
        ItemStack item = new ItemStack(configMaterial("gui.filler-material", Material.GRAY_STAINED_GLASS_PANE));
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(" ").decoration(TextDecoration.ITALIC, false));
        meta.setEnchantmentGlintOverride(true);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack control(Material material, String name, NamedTextColor color) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name, color).decoration(TextDecoration.ITALIC, false));
        item.setItemMeta(meta);
        return item;
    }

    private Material configMaterial(String path, Material fallback) {
        String configured = plugin.getConfig().getString(path, fallback.name());
        Material material = Material.matchMaterial(configured == null ? fallback.name() : configured);
        return material == null ? fallback : material;
    }

    public static final class Holder implements InventoryHolder {
        private final int page;

        public Holder(int page) {
            this.page = page;
        }

        public int page() {
            return page;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
