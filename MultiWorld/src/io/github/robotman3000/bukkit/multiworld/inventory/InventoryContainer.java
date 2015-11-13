package io.github.robotman3000.bukkit.multiworld.inventory;

import io.github.robotman3000.bukkit.multiworld.MultiWorld;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class InventoryContainer {

    public static boolean bool = false;

    /**
     * Updates the contents of the provided inventory with the given player
     * 
     * @param player
     * @param inv
     */
    public static void updateInventoryContents(Player player, BukkitInventory inv) {
        inv.setCanFly(player.getAllowFlight());
        inv.setBedSpawnPoint(player.getBedSpawnLocation());
        inv.setCompassTarget(player.getCompassTarget());
        inv.setDisplayName(player.getDisplayName());
        inv.setExhaustion(player.getExhaustion());
        inv.setXpPoints(player.getExp());
        inv.setFallDistance(player.getFallDistance());
        inv.setFireTicks(player.getFireTicks());
        inv.setFlying(player.isFlying());
        inv.setFoodLevel(player.getFoodLevel());
        inv.setHealthPoints(player.getHealth());
        inv.setXpLevel(player.getLevel());
        inv.setRemainingAir(player.getRemainingAir());
        inv.setFoodSaturation(player.getSaturation());
        inv.setVelocity(player.getVelocity());
        inv.setArmorContents(player.getInventory().getArmorContents());
        inv.setInventoryContents(player.getInventory().getContents());
        inv.setEnderChest(player.getEnderChest().getContents());
        inv.setLocation(player.getLocation());
    }

    /**
     * Update the inventory of the given player with the provided inventory
     * 
     * @param player
     * @param inventory
     */
    public static void updatePlayerInventory(Player player, BukkitInventory inventory) {
        player.setAllowFlight(inventory.canFly());
        player.setBedSpawnLocation(inventory.getBedSpawnPoint());
        player.setCompassTarget(inventory.getCompassTarget());
        player.setDisplayName(inventory.getDisplayName());
        player.setExhaustion(inventory.getExhaustion());
        player.setExp(inventory.getXpPoints());
        player.setFallDistance(inventory.getFallDistance());
        player.setFireTicks(inventory.getFireTicks());
        player.setFlying(inventory.isFlying());
        player.setFoodLevel(inventory.getFoodLevel());
        player.setHealth(inventory.getHealthPoints());
        player.setLevel(inventory.getXpLevel());
        player.setRemainingAir(inventory.getRemainingAir());
        player.setSaturation(inventory.getFoodSaturation());
        player.setVelocity(inventory.getVelocity());
        player.getInventory().setArmorContents(inventory.getArmorContents());
        player.getInventory().setContents(inventory.getInventoryContents());
        player.getEnderChest().setContents(inventory.getEnderChest());
        // player.teleport(inventory.getLocation());
    }

    public static void zeroPlayerInventory(Player player, GameMode gameMode) {
        // TODO: Find out the proper default values
        // The player should be allowed to fly if in creative mode or spectator mode
        player.setAllowFlight((gameMode == GameMode.CREATIVE || gameMode == GameMode.SPECTATOR));
        player.setBedSpawnLocation(player.getWorld().getSpawnLocation());
        player.setCompassTarget(player.getWorld().getSpawnLocation());
        player.setDisplayName(player.getName());
        player.setExhaustion(0);
        player.setExp(0);
        player.setFallDistance(0);
        player.setFireTicks(0);
        player.setFlying(false);
        player.setFoodLevel(100);
        player.setHealth(20);
        player.setLevel(0);
        player.setRemainingAir(300);
        player.setSaturation(0);
        player.setVelocity(new Vector());
        player.getActivePotionEffects().clear();
        player.getEnderChest().clear();
        player.getEquipment().clear();
        player.getInventory().clear();
    }

    private final HashMap<InventoryKey, BukkitInventory> unusedInventories = new HashMap<>();

    private final HashMap<InventoryKey, BukkitInventory> usedInventories = new HashMap<>();

    public synchronized boolean checkSanityForPlayer(Player player, int i) {
        int invCounter = 0;
        for (InventoryKey key : usedInventories.keySet()) {
            if (key.getPlayerKey().equals(player.getUniqueId())) {
                invCounter++;
            }
        }
        return (invCounter == i);
    }

    public synchronized boolean hasUnusedInventory(InventoryKey theKey) {
        for (InventoryKey key : unusedInventories.keySet()) {
            if (key.equals(theKey)) {
                return true;
            }
        }
        return false;
    }

    public synchronized boolean hasUsedInventory(InventoryKey theKey) {
        for (InventoryKey key : usedInventories.keySet()) {
            if (key.equals(theKey)) {
                return true;
            }
        }
        return false;
    }

    protected synchronized boolean listInvCommand(CommandSender sender, Command cmd, String label,
            String[] args) {
        if (usedInventories.size() == 0) {
            sender.sendMessage(ChatColor.GOLD + "There are no Registered inventories");
        } else {
            for (InventoryKey key : usedInventories.keySet()) {
                BukkitInventory inv = usedInventories.get(key);
                if (inv != null) {
                    sender.sendMessage("Registered Inv: " + ChatColor.BLUE
                            + showPlayerName(key.getPlayerKey()) + " " + ChatColor.GREEN
                            + key.getWorldKey() + " " + ChatColor.YELLOW
                            + key.getGamemodeKey().toString() + " " + ChatColor.WHITE
                            + inv.getInventoryId());
                }
            }
        }

        if (unusedInventories.size() == 0) {
            sender.sendMessage(ChatColor.GOLD + "There are no Unegistered inventories");
        } else {
            for (InventoryKey key : unusedInventories.keySet()) {
                BukkitInventory inv = unusedInventories.get(key);
                if (inv != null) {
                    sender.sendMessage("Unregistered Inv: " + ChatColor.BLUE
                            + showPlayerName(key.getPlayerKey()) + " " + ChatColor.GREEN
                            + key.getWorldKey() + " " + ChatColor.YELLOW
                            + key.getGamemodeKey().toString() + " " + ChatColor.WHITE
                            + inv.getInventoryId());
                }
            }
        }
        return true;
    }

    protected synchronized void readInventoryConfig(MultiWorld plugin) {
        File inventoryFolder = new File(plugin.getDataFolder(), "inventories");
        inventoryFolder.mkdirs();

        for (File confFile : inventoryFolder.listFiles()) {
            if (confFile.getName().contains("playerInv-") && confFile.isFile()) {
                YamlConfiguration yaml = YamlConfiguration.loadConfiguration(confFile);
                // Load the inventory keys
                String invKeyPath = "invKey";
                if (!yaml.contains(invKeyPath)) {
                    yaml.createSection(invKeyPath);
                }
                ConfigurationSection invKeysSection = yaml.getConfigurationSection(invKeyPath);
                for (String playerKey : invKeysSection.getKeys(false)) {
                    String playerKeyPath = invKeyPath + "." + playerKey;
                    ConfigurationSection playerKeysSection = yaml
                            .getConfigurationSection(playerKeyPath);
                    for (String worldKey : playerKeysSection.getKeys(false)) {
                        String worldKeyPath = playerKeyPath + "." + worldKey;
                        ConfigurationSection worldKeysSection = yaml
                                .getConfigurationSection(worldKeyPath);
                        for (String gamemodeKey : worldKeysSection.getKeys(false)) {
                            String gamemodeKeyPath = worldKeyPath + "." + gamemodeKey;
                            ConfigurationSection gamemodeConfSection = yaml
                                    .getConfigurationSection(gamemodeKeyPath);

                            BukkitInventory theInv = (BukkitInventory) gamemodeConfSection
                                    .get("inventory");
                            InventoryKey invKey = new InventoryKey(playerKey, worldKey, gamemodeKey);
                            unusedInventories.put(invKey, theInv);
                        }
                    }
                }
            }
        }
    }

    public synchronized void registerInventory(InventoryKey theKey) {
        Player player = Bukkit.getPlayer(theKey.getPlayerKey());
        if (player != null) {
            Bukkit.getLogger().info("[SpigotPlus] Reg: Player is good");
            // Bukkit.getLogger().info("[SpigotPlus] Reg: usedInvs: " + usedInventories);
            // Bukkit.getLogger().info("[SpigotPlus] Reg: unusedInvs: " + unusedInventories);
            // Bukkit.getLogger().info("[SpigotPlus] Reg: Key: " + theKey);
            InventoryContainer.bool = false;
            // Bukkit.getLogger().info("[SpigotPlus] Reg: <" + Thread.currentThread().getName()
            // + "> Bool1: " + InventoryContainer.bool);

            BukkitInventory newInv = unusedInventories.get(theKey);
            if (newInv != null) {
                Bukkit.getLogger().info("[SpigotPlus] Reg: Inventory is good");
                InventoryContainer.updatePlayerInventory(player, newInv);
                unusedInventories.remove(theKey);
                usedInventories.put(theKey, newInv);
                /*                Bukkit.getLogger().info("[SpigotPlus] Reg: Cond: usedInvs: " + usedInventories);
                                Bukkit.getLogger().info("[SpigotPlus] Reg: Cond: unusedInvs: " + unusedInventories);*/
                return;
            }
            /*            if (hasUnusedInventory(theKey)) {
                            Bukkit.getLogger().info("[SpigotPlus] Reg: Inv we want exists");
                            for (InventoryKey key : unusedInventories.keySet()) {
                                if (key.equals(theKey)) {
                                    Bukkit.getLogger()
                                            .info("[SpigotPlus] Reg: Inv Returned from iterator key: "
                                                          + unusedInventories.get(key));
                                    BukkitInventory newInv = unusedInventories.get(key);
                                    Bukkit.getLogger().info("[SpigotPlus] Reg: Loc2: usedInvs: "
                                                                    + usedInventories);
                                    Bukkit.getLogger().info("[SpigotPlus] Reg: Loc2: unusedInvs: "
                                                                    + unusedInventories);
                                    Bukkit.getLogger().info("[SpigotPlus] Reg: newInv: " + newInv);

                                    if (newInv != null) {
                                        Bukkit.getLogger().info("[SpigotPlus] Reg: Inventory is good");
                                        InventoryContainer.updatePlayerInventory(player, newInv);
                                        unusedInventories.remove(key);
                                        usedInventories.put(key, newInv);
                                        Bukkit.getLogger().info("[SpigotPlus] Reg: Cond: usedInvs: "
                                                                        + usedInventories);
                                        Bukkit.getLogger().info("[SpigotPlus] Reg: Cond: unusedInvs: "
                                                                        + unusedInventories);
                                        return;
                                    }
                                }
                            }
                        } else {
                            Bukkit.getLogger().info("[SpigotPlus] Reg: Inv we want does not exist");
                        }*/

            /*            BukkitInventory newInv = unusedInventories.get(theKey);
                        Bukkit.getLogger().info("[SpigotPlus] Reg: <" + Thread.currentThread().getName()
                                                        + "> Bool2: " + InventoryContainer.bool);
                        if (newInv != null) {
                            Bukkit.getLogger().info("[SpigotPlus] Reg: Inventory is good");
                            InventoryContainer.updatePlayerInventory(player, newInv);
                            unusedInventories.remove(theKey);
                            usedInventories.put(theKey, newInv);
                            Bukkit.getLogger().info("[SpigotPlus] Reg: Cond: usedInvs: " + usedInventories);
                            Bukkit.getLogger().info("[SpigotPlus] Reg: Cond: unusedInvs: " + unusedInventories);
                            return;
                        }*/
        }
        Bukkit.getLogger().info("[SpigotPlus] Reg: Inventory register failed");
    }

    public synchronized void resetInventory(InventoryKey theKey) {
        unusedInventories.put(theKey, new BukkitInventory(Bukkit.getPlayer(theKey.getPlayerKey()),
                true));
    }

    protected synchronized void saveInventoryConfig(MultiWorld plugin) {

        // Save the inventory keys
        String invKeyPath = "invKey";
        File invDataFolder = new File(plugin.getDataFolder(), "inventories");
        invDataFolder.mkdirs();
        for (InventoryKey key : unusedInventories.keySet()) {
            String sectionPath = invKeyPath + "." + key.getPlayerKey().toString() + "."
                    + key.getWorldKey() + "." + key.getGamemodeKey();

            // Save the inventory data
            File invConfFile = new File(invDataFolder, "playerInv-" + key.getPlayerKey().toString()
                    + ".yml");
            YamlConfiguration yaml = new YamlConfiguration();
            try {
                if (invConfFile.exists()) {
                    yaml.load(invConfFile);
                }
                yaml.set(sectionPath + ".inventory", unusedInventories.get(key));
                yaml.save(invConfFile);
            } catch (IOException e) {
                e.printStackTrace();
                Bukkit.getLogger()
                        .severe("InventoryManager: Failed to save an inventory to disk, the inventory has been lost!!");
                Bukkit.getLogger().severe("InventoryManager: The player id of the inventory was: "
                                                  + key.getPlayerKey());
            } catch (InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }

    }

    private String showPlayerName(UUID playerKey) {
        Player player = Bukkit.getPlayer(playerKey);
        if (player == null) {
            return playerKey.toString();
        }
        return player.getName();
    }

    public synchronized void unregisterInventory(InventoryKey theKey) {
        Player player = Bukkit.getPlayer(theKey.getPlayerKey());
        if (player != null) {
            Bukkit.getLogger().info("[SpigotPlus] UReg: Player is good");

            BukkitInventory newInv = usedInventories.get(theKey);
            if (newInv != null) {
                Bukkit.getLogger().info("[SpigotPlus] UReg: Inventory is good");
                InventoryContainer.updateInventoryContents(player, newInv);
                usedInventories.remove(theKey);
                unusedInventories.put(theKey, newInv);
                /*                Bukkit.getLogger().info("[SpigotPlus] UReg: Cond: usedInvs: "
                                                                + usedInventories);
                                Bukkit.getLogger().info("[SpigotPlus] UReg: Cond: unusedInvs: "
                                                                + unusedInventories);*/
                return;
            }
            /*            Bukkit.getLogger().info("[SpigotPlus] UReg: usedInvs: " + usedInventories);
                        Bukkit.getLogger().info("[SpigotPlus] UReg: unusedInvs: " + unusedInventories);
                        Bukkit.getLogger().info("[SpigotPlus] UReg: Key: {" + theKey.hashCode() + "} {"
                                                        + System.identityHashCode(theKey) + "} " + theKey);

                        if (hasUsedInventory(theKey)) {
                            Bukkit.getLogger().info("[SpigotPlus] UReg: Inv we want exists");
                            for (InventoryKey key : usedInventories.keySet()) {
                                if (key.equals(theKey)) {
                                    Bukkit.getLogger()
                                            .info("[SpigotPlus] UReg: Inv Returned from iterator key: "
                                                          + usedInventories.get(key));
                                    BukkitInventory newInv = usedInventories.get(key);
                                    Bukkit.getLogger().info("[SpigotPlus] UReg: Loc2: usedInvs: "
                                                                    + usedInventories);
                                    Bukkit.getLogger().info("[SpigotPlus] UReg: Loc2: unusedInvs: "
                                                                    + unusedInventories);
                                    Bukkit.getLogger().info("[SpigotPlus] UReg: newInv: " + newInv);

                                    if (newInv != null) {
                                        Bukkit.getLogger().info("[SpigotPlus] UReg: Inventory is good");
                                        InventoryContainer.updateInventoryContents(player, newInv);
                                        usedInventories.remove(key);
                                        unusedInventories.put(key, newInv);
                                        Bukkit.getLogger().info("[SpigotPlus] UReg: Cond: usedInvs: "
                                                                        + usedInventories);
                                        Bukkit.getLogger().info("[SpigotPlus] UReg: Cond: unusedInvs: "
                                                                        + unusedInventories);
                                        return;
                                    }
                                }
                            }
                        } else {
                            Bukkit.getLogger().info("[SpigotPlus] UReg: Inv we want does not exist");
                        }*/
            // BukkitInventory newInv = usedInventories.get(theKey); // This is the faulty line of
            // code

        }
        Bukkit.getLogger().info("[SpigotPlus] UReg: Inventory unregister failed");
    }
}