package com.mguhc.undertale.roles.monstre.sans;

import com.mguhc.UhcAPI;
import com.mguhc.ability.AbilityManager;
import com.mguhc.ability.CooldownManager;
import com.mguhc.effect.EffectManager;
import com.mguhc.events.RoleGiveEvent;
import com.mguhc.player.PlayerManager;
import com.mguhc.player.UhcPlayer;
import com.mguhc.roles.RoleManager;
import com.mguhc.roles.UhcRole;
import com.mguhc.undertale.UndertaleUHC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class SansListener implements Listener {

    private EffectManager effectManager;
    private AbilityManager abilityManager;
    private CooldownManager cooldownManager;
    private RoleManager roleManager;
    private PlayerManager playerManager;
    private int specCount = 0;
    private boolean canUseTp = true;
    private List<Player> playerSpectated = new ArrayList<>();
    private int judgeCount;

    public SansListener() {
        UhcAPI api = UhcAPI.getInstance();
        this.playerManager = api.getPlayerManager();
        this.roleManager = api.getRoleManager();
        this.cooldownManager = api.getCooldownManager();
        this.abilityManager = api.getAbilityManager();
        this.effectManager = api.getEffectManager();
    }

    @EventHandler
    private void OnRoleGive(RoleGiveEvent event) {
        UhcPlayer uhcPlayer = roleManager.getPlayerWithRole("Sans");
        if(uhcPlayer != null) {
            Player player = uhcPlayer.getPlayer();
            // Ajouter le livre de Protection III
            ItemStack protectionBook = new ItemStack(Material.ENCHANTED_BOOK);
            ItemMeta bookMeta = protectionBook.getItemMeta(); // Obtenir l'ItemMeta
            if (bookMeta != null) {
                bookMeta.setDisplayName(ChatColor.AQUA + "Livre de Protection III");
                protectionBook.setItemMeta(bookMeta); // Appliquer l'ItemMeta à l'ItemStack
            }
            player.getInventory().addItem(protectionBook);

            UhcPlayer papyrus = roleManager.getPlayerWithRole("Papyrus");
            if (papyrus != null) {
                player.sendMessage("Papyrus : " + papyrus.getPlayer().getName());
            }
        }
    }

    @EventHandler
    private void OnCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String[] args = event.getMessage().split(" ");

        if (args.length == 3 && args[0].equalsIgnoreCase("/ut") &&
            args[1].equalsIgnoreCase("spec") &&
            isSans(playerManager.getPlayer(player))) {
            if (specCount <= 2) {
                Player aimedPlayer = Bukkit.getPlayer(args[2]);
                if (aimedPlayer != null) {
                    specCount ++;
                    openSpecInventory(player, aimedPlayer);
                }
            }
        }
         if (args.length == 3 && args[0].equalsIgnoreCase("/ut") &&
             args[1].equalsIgnoreCase("tp") &&
             isSans(playerManager.getPlayer(player))) {
             if(canUseTp) {
                 Player aimedPlayer = Bukkit.getPlayer(args[2]);
                 if(aimedPlayer != null) {
                     canUseTp = false;
                     player.teleport(aimedPlayer);
                 }
             }
         }
         if (args.length == 2 && args[0].equalsIgnoreCase("/ut") && args[1].equals("judge")) {
             if(judgeCount <= 2) {
                 final boolean[] isJudgeOn = {true};
                 judgeCount++;
                 player.sendMessage("Judge Utilisé");
                 // Trouver un joueur à proximité
                 List<Location> trueLabLocations = Arrays.asList(
                         new Location(Bukkit.getWorld("world"), 100, 100, 100),
                         new Location(Bukkit.getWorld("world"), 50, 100, 50),
                         new Location(Bukkit.getWorld("world"), 100, 100, 50));

                 List<Player> nearbyPlayers = new ArrayList<>();
                 Random random = new Random();

                 Map<Player, Map<PotionEffectType, Integer>> playerEffects = new HashMap<>();
                 Map<Player, Location> playerLocations = new HashMap<>();

                 for (Entity entity : player.getNearbyEntities(15, 15, 15)) {
                     if (entity instanceof Player) {
                         Player nearbyPlayer = (Player) entity;
                         nearbyPlayers.add(nearbyPlayer);
                     }
                 }

                 player.teleport(trueLabLocations.get(random.nextInt(trueLabLocations.size())));
                 effectManager.setResistance(player, 20);
                 effectManager.setStrength(player, 40);

                 for(Player nearbyPlayer : nearbyPlayers) {
                     playerEffects.put(nearbyPlayer, effectManager.getEffectsMap(player));
                     playerLocations.put(nearbyPlayer, nearbyPlayer.getLocation());
                     nearbyPlayer.teleport(trueLabLocations.get(random.nextInt(trueLabLocations.size())));
                     effectManager.removeEffects(nearbyPlayer);
                     effectManager.setWeakness(nearbyPlayer, 20);
                 }
                 
                 Bukkit.getPluginManager().registerEvents(new Listener() {
                     @EventHandler
                     private void OnDeath(PlayerDeathEvent event) {
                         Player victim = event.getEntity().getPlayer();
                         Player killer = event.getEntity().getKiller();
                         if (isSans(playerManager.getPlayer(victim))) {
                             if (isJudgeOn[0]) {
                                 isJudgeOn[0] = false;
                                 for(Map.Entry<Player, Map<PotionEffectType, Integer>> entry : playerEffects.entrySet()) {
                                     Player nearbyPlayer = entry.getKey();
                                     Map<PotionEffectType, Integer> effects = entry.getValue();
                                     effectManager.setEffects(nearbyPlayer, effects);
                                 }
                                 for(Map.Entry<Player, Location> entry : playerLocations.entrySet()) {
                                     Player nearbyPlayer = entry.getKey();
                                     Location location = entry.getValue();
                                     nearbyPlayer.teleport(location);
                                 }
                             }
                         }
                     }
                 }, UndertaleUHC.getInstance());

                 new BukkitRunnable() {
                     @Override
                     public void run() {
                         if (isJudgeOn[0]) {
                             for(Map.Entry<Player, Map<PotionEffectType, Integer>> entry : playerEffects.entrySet()) {
                                 Player nearbyPlayer = entry.getKey();
                                 Map<PotionEffectType, Integer> effects = entry.getValue();
                                 effectManager.setEffects(nearbyPlayer, effects);
                             }
                             for(Map.Entry<Player, Location> entry : playerLocations.entrySet()) {
                                 Player nearbyPlayer = entry.getKey();
                                 Location location = entry.getValue();
                                 nearbyPlayer.teleport(location);
                             }
                         }
                     }
                 }.runTaskLater(UndertaleUHC.getInstance(), 5*60*20);
             }
        }
    }

    @EventHandler
    private void OnMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (isSans(playerManager.getPlayer(player))) {
            for (Entity nearbyEntity : player.getNearbyEntities(5, 5, 5)) {
                if (nearbyEntity instanceof Player) {
                    Player nearbyPlayer = (Player) nearbyEntity;
                    if(nearbyPlayer.getInventory().contains(getGasterItem()) &&
                        !playerSpectated.contains(nearbyPlayer)) {
                        player.sendMessage(ChatColor.DARK_PURPLE + "Une puissance familière émane près de vous...");
                        playerSpectated.add(nearbyPlayer);
                    }
                }
            }
        }
    }

    private void openSpecInventory(Player player, Player aimedPlayer) {
        Inventory inventory = Bukkit.createInventory(null, 36, "Inventaire du joueur visée");
        ItemStack[] content = aimedPlayer.getInventory().getContents();
        inventory.setContents(content);
        player.openInventory(inventory);
    }

    @EventHandler
    private void OnInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (event.getInventory().getName().equals("Inventaire du joueur visée")) {
            event.setCancelled(true);
        }
    }

    private ItemStack getGasterItem() {
        ItemStack gaster = new ItemStack(Material.NETHER_STAR);
        ItemMeta gasterMeta = gaster.getItemMeta();
        if(gasterMeta != null) {
            gasterMeta.setDisplayName(ChatColor.DARK_PURPLE + "Fragment de Gaster");
            gaster.setItemMeta(gasterMeta);
        }
        return gaster;
    }

    private boolean isSans(UhcPlayer player) {
        UhcRole role = player.getRole();
        return role != null && role.getName().equals("Sans");
    }
}
