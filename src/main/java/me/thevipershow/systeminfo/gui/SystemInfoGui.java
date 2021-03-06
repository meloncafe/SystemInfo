package me.thevipershow.systeminfo.gui;

import me.thevipershow.systeminfo.SystemInfo;
import me.thevipershow.systeminfo.oshi.SystemValues;
import me.thevipershow.systeminfo.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SystemInfoGui {

    private Player player;
    private Inventory inventory;
    private final List<Integer> backgroundSlots = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 18, 27, 26, 25, 24, 23, 22, 21, 20, 19, 10));

    /**
     * This is the constructor and is used to open a new GUI to a player
     * by instantiating the class.
     *
     * @param player this is the target player that will receive the GUI Inventory.
     */
    public SystemInfoGui(Player player) {
        this.player = player;
        this.inventory = Bukkit.createInventory(player, 27, "SystemInfo");
        player.openInventory(this.inventory);
        fillBackground(Material.BLACK_STAINED_GLASS_PANE, this.inventory, this.backgroundSlots);
        Bukkit.getScheduler().runTaskTimer(SystemInfo.instance, r -> {
            if (player.getOpenInventory().getTitle().equals("SystemInfo")) {
                updateInventory(inventory);
            } else {
                r.cancel();
            }
        }, 2L, 20L);
    }

    /**
     * This method generates an animation that consists in taking a list of integers that represents
     * inventory slots, then generating items with material parameter for each slot creating a cool effect
     *
     * @param material  the material that the generated items will have.
     * @param inventory the inventory where the items will be set.
     */
    private void fillBackground(Material material, Inventory inventory, List<Integer> backgroundSlots) {
        AtomicInteger arr = new AtomicInteger();
        Bukkit.getScheduler().runTaskTimer(SystemInfo.instance, r -> {
            if (arr.get() < backgroundSlots.size()) {
                createCustomItem(inventory, material, 1, backgroundSlots.get(arr.get()), "", "");
                arr.getAndIncrement();
            } else {
                r.cancel();
            }
        }, 1L, 1L);
    }

    /**
     * This methods creates and sets in an inventory a new custom ItemStack from the given parameters:
     *
     * @param inv         this is the inventory where the item should be set.
     * @param material    this is the material that the new ItemStack will have.
     * @param amount      the amount of items to set.
     * @param invSlot     this is in which slot of the inventory the item will be set.
     * @param displayName the display name of the new ItemStack (this does support color codes with &).
     * @param loreText    the lore of the new ItemStack (this does support color codes with & and multiple lines with \n).
     * @throws IllegalArgumentException if amount of items is illegal, or the slot is illegal.
     */
    private void createCustomItem(Inventory inv, Material material, int amount, int invSlot, String displayName, String... loreText) throws IllegalArgumentException {
        if ((amount <= 64 && amount > 0) && (invSlot >= 0 && invSlot <= inv.getSize())) {
            ItemStack item;
            List<String> lore = new ArrayList<>();
            item = new ItemStack(material, amount);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(Utils.color(displayName));
            for (String s : loreText) {
                lore.add(Utils.color(s));
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(invSlot - 1, item);
        } else {
            throw new IllegalArgumentException("Illegal values chosen for creating a custom item.");
        }
    }

    /**
     * This methods updates the inventory with new items
     *
     * @param inventory the target inventory where items will be set.
     */
    private void updateInventory(Inventory inventory) {
        createCustomItem(inventory, Material.BLACK_CONCRETE, 1, 11, "&2Processor",
                String.format("&7Vendor: &a%s", SystemValues.getCpuIdentifier().getVendor()),
                String.format("&7Model: &a%s %s", SystemValues.getCpuIdentifier().getModel(), SystemValues.getCpuIdentifier().getName()),
                String.format("&7Clock speed: &a%s GHz", SystemValues.getCentralProcessor().getMaxFreq() / 1e9),
                String.format("&7Cores: &a%d", SystemValues.getCentralProcessor().getPhysicalProcessorCount()),
                String.format("&7Threads: &a%d", SystemValues.getCentralProcessor().getLogicalProcessorCount()));

        createCustomItem(inventory, Material.LIGHT_GRAY_CONCRETE, 1, 13, "&2Memory",
                String.format("&7Total:&a %.3f GB", SystemValues.getMemory().getTotal() / 1e9),
                String.format("&7Available:&a %.3f GB", SystemValues.getMemory().getAvailable() / 1e9),
                String.format("&7Swap:&a %.0f/%.0f MB", SystemValues.getMemory().getVirtualMemory().getSwapUsed() / 1e6, SystemValues.getMemory().getVirtualMemory().getSwapTotal() / 1e6));

        createCustomItem(inventory, Material.YELLOW_CONCRETE, 1, 15, "&2Operating system",
                String.format("&7Name: &a%s %s %s", SystemValues.getOperatingSystem().getFamily(), SystemValues.getOperatingSystem().getManufacturer(), SystemValues.getOperatingSystem().getVersionInfo().getVersion()),
                String.format("&7Processes: &a%d", SystemValues.getOperatingSystem().getProcessCount()));

        createCustomItem(inventory, Material.BLUE_CONCRETE, 1, 17, "&2Uptime",
                String.format("&7Jvm uptime: &a%d min", ChronoUnit.MINUTES.between(SystemInfo.time, LocalDateTime.now())),
                String.format("&7Current time: &a%s", LocalDateTime.now().format(DateTimeFormatter.ofPattern("d\\M\\u h:m:s a"))));
    }

}
