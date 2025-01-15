package com.mguhc.undertale.roles.monstre.undyne;

import com.mguhc.UhcAPI;
import com.mguhc.ability.AbilityManager;
import com.mguhc.ability.CooldownManager;
import com.mguhc.effect.EffectManager;
import com.mguhc.events.RoleGiveEvent;
import com.mguhc.player.PlayerManager;
import com.mguhc.player.UhcPlayer;
import com.mguhc.roles.RoleManager;
import com.mguhc.undertale.UndertaleUHC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class UndyneListener implements Listener {

    private EffectManager effectManager;
    private AbilityManager abilityManager;
    private CooldownManager cooldownManager;
    private RoleManager roleManager;
    private PlayerManager playerManager;
    private String currentForm = "distance";

    public UndyneListener() {
            UhcAPI api = UhcAPI.getInstance();
            this.playerManager = api.getPlayerManager();
            this.roleManager = api.getRoleManager();
            this.cooldownManager = api.getCooldownManager();
            this.abilityManager = api.getAbilityManager();
            this.effectManager = api.getEffectManager();
    }

    @EventHandler
    private void OnRoleGive(RoleGiveEvent event) {
        UhcPlayer uhcPlayer = roleManager.getPlayerWithRole("Undyne");
        if (uhcPlayer != null) {
            Player player = uhcPlayer.getPlayer();

            UhcPlayer asgore = roleManager.getPlayerWithRole("Asgore");
            if (asgore != null) {
                player.sendMessage("Asgore : " + asgore.getPlayer().getName());
            }

            player.getInventory().addItem(getSpearItem());

            new BukkitRunnable() {
                @Override
                public void run() {
                    long time = player.getWorld().getTime(); // Obtenir le temps actuel dans le monde
                    if (time >= 0 && time < 12000) {
                        effectManager.removeEffect(player, PotionEffectType.INCREASE_DAMAGE);// Vérifier si c'est le jour
                        effectManager.setResistance(player, 20);
                    } else {
                        effectManager.removeEffect(player.getPlayer(), PotionEffectType.DAMAGE_RESISTANCE);
                        effectManager.setStrength(player, 20);
                    }
                }
            }.runTaskTimer(UndertaleUHC.getInstance(), 0, 20*3);
        }
        else {
            Bukkit.getLogger().warning("Le joueur Undyne n'a pas été trouvé.");
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        Action action = event.getAction();

        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return;
        }

        if (isUndyne(playerManager.getPlayer(player)) &&
            item.equals(getSpearItem())) {

            event.setCancelled(true);

            if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
                switchToOffensiveForm(player);
            } else if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                switchToDistanceForm(player);
            }
        }
    }

    private void switchToOffensiveForm(Player player) {
        if ("offensive".equals(currentForm)) {
            player.sendMessage(ChatColor.YELLOW + "Vous êtes déjà en forme Offensive!");
            return;
        }

        effectManager.removeEffects(player);
        player.setMaxHealth(player.getMaxHealth() + 6); // Ajoute 3 cœurs
        player.setHealth(player.getHealth() + 6);
        player.sendMessage(ChatColor.RED + "Vous êtes passé en forme Offensive!");
        currentForm = "offensive";
    }

    private void switchToDistanceForm(Player player) {
        if ("distance".equals(currentForm)) {
            player.sendMessage(ChatColor.YELLOW + "Vous êtes déjà en forme Distance!");
            return;
        }

        effectManager.removeEffects(player);
        player.setMaxHealth(player.getMaxHealth() - 6);
        effectManager.setSpeed(player, 40);
        player.sendMessage(ChatColor.BLUE + "Vous êtes passé en forme Distance!");
        currentForm = "distance";
    }

    private void OnMsg(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String[] args = event.getMessage().split(" ");

        if (args[0].equalsIgnoreCase("/ut") && args[1].equalsIgnoreCase("msg")) {
            if (isUndyne(playerManager.getPlayer(player))) {
                UhcPlayer asgore = roleManager.getPlayerWithRole("Asgore");
                if (asgore != null) {
                    // Construire le message à partir des arguments
                    StringBuilder messageBuilder = new StringBuilder();
                    for (int i = 2; i < args.length; i++) { // Commencer à partir de l'index 2
                        messageBuilder.append(args[i]).append(" "); // Ajouter chaque argument au message
                    }
                    String message = messageBuilder.toString().trim(); // Convertir en chaîne et enlever les espaces superflus

                    // Envoyer le message à Asgore
                    asgore.getPlayer().sendMessage(ChatColor.YELLOW + "[Undyne] " + message);
                }
            }
        }
    }

    private boolean isUndyne(UhcPlayer player) {
        return player != null && player.getRole() != null && player.getRole().getName().equals("Undyne");
    }

    private ItemStack getSpearItem() {
        ItemStack spear = new ItemStack(Material.NETHER_STAR);
        ItemMeta spear_meta = spear.getItemMeta();
        if (spear_meta != null) {
            spear_meta.setDisplayName(ChatColor.RED + "Spear of Justice");
            spear.setItemMeta(spear_meta);
        }
        return spear;
    }
}
