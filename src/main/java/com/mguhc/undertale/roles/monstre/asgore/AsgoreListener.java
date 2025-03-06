package com.mguhc.undertale.roles.monstre.asgore;

import com.mguhc.UhcAPI;
import com.mguhc.ability.AbilityManager;
import com.mguhc.ability.CooldownManager;
import com.mguhc.effect.EffectManager;
import com.mguhc.events.RoleGiveEvent;
import com.mguhc.player.PlayerManager;
import com.mguhc.player.UhcPlayer;
import com.mguhc.roles.RoleManager;
import com.mguhc.undertale.UndertaleUHC;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class AsgoreListener implements Listener {

    private EffectManager effectManager;
    private AbilityManager abilityManager;
    private CooldownManager cooldownManager;
    private RoleManager roleManager;
    private PlayerManager playerManager;

    private static final int FIREBALL_COUNT = 6;
    private static final double RADIUS = 10.0;
    private int fireBallCount = 0;

    public AsgoreListener() {
        UhcAPI api = UhcAPI.getInstance();
        this.playerManager = api.getPlayerManager();
        this.roleManager = api.getRoleManager();
        this.cooldownManager = api.getCooldownManager();
        this.abilityManager = api.getAbilityManager();
        this.effectManager = api.getEffectManager();
    }

    @EventHandler
    private void OnRoleGive(RoleGiveEvent event) {
        UhcPlayer uhcPlayer = roleManager.getPlayerWithRole("Asgore");
        if (uhcPlayer != null) {
            Player player = uhcPlayer.getPlayer();

            effectManager.setResistance(player, 2);
            player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0));

            // Récupérer les joueurs dans le camp humain
            List<UhcPlayer> monstrePlayers = roleManager.getPlayersInCamp("Monstre");
            StringBuilder message = new StringBuilder();
            message.append("Joueurs dans le camp monstre :\n");

            // Ajouter chaque joueur à la liste
            for (UhcPlayer monstre : monstrePlayers) {
                message.append("- ").append(monstre.getPlayer().getName()).append("\n");
            }

            // Envoyer le message à Asgore
            player.sendMessage(message.toString());

            player.getInventory().addItem(getFireballItem());

            ItemStack trident = new ItemStack(Material.DIAMOND_SWORD);
            ItemMeta tridentMeta = trident.getItemMeta();
            if (tridentMeta != null) {
                tridentMeta.setDisplayName(ChatColor.RED + "Trident des Enfers");
                tridentMeta.addEnchant(Enchantment.FIRE_ASPECT, 1, true);
                tridentMeta.addEnchant(Enchantment.DAMAGE_ALL, 3, true);
                trident.setItemMeta(tridentMeta);
            }
            player.getInventory().addItem(trident);
        }
    }

    @EventHandler
    private void OnInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (isAsgore(player) &&
            item.equals(getFireballItem()) &&
            fireBallCount < 3) {
            fireBallCount ++;
            unleashFireballs(player);
        }
    }

    private void unleashFireballs(Player player) {
        Location playerLocation = player.getLocation();
        Random random = new Random();

        for (int i = 0; i < FIREBALL_COUNT; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double x = Math.cos(angle) * RADIUS;
            double z = Math.sin(angle) * RADIUS;

            Location fireballLocation = playerLocation.clone().add(x, 0, z);
            Vector direction = playerLocation.toVector().subtract(fireballLocation.toVector()).normalize();

            Fireball fireball = player.getWorld().spawn(fireballLocation, Fireball.class);
            fireball.setDirection(direction);
            fireball.setYield(4F);
            fireball.setIsIncendiary(true);

            fireball.setMetadata("asgore_fireball", new FixedMetadataValue(UndertaleUHC.getInstance(), true));
            fireball.setMetadata("asgore_caster", new FixedMetadataValue(UndertaleUHC.getInstance(), player.getUniqueId()));
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Fireball) {
            Fireball fireball = (Fireball) event.getDamager();
            if (fireball.hasMetadata("asgore_fireball") && event.getEntity() instanceof Player) {
                Player victim = (Player) event.getEntity();

                // Vérifier si la victime est le lanceur
                if (fireball.hasMetadata("asgore_caster")) {
                    List<MetadataValue> metadataList = fireball.getMetadata("asgore_caster");
                    if (!metadataList.isEmpty() && metadataList.get(0) instanceof FixedMetadataValue) {
                        UUID casterUUID = (UUID) metadataList.get(0).value();
                        if (victim.getUniqueId().equals(casterUUID)) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }

                victim.removePotionEffect(PotionEffectType.ABSORPTION);
                victim.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 200, -1));
            }
        }
    }

    @EventHandler
    private void OnMsg(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String[] args = event.getMessage().split(" ");

        if (args[0].equalsIgnoreCase("/ut") && args[1].equalsIgnoreCase("msg")) {
            if (isAsgore(player)) {
                UhcPlayer undyne = roleManager.getPlayerWithRole("Undyne");
                if (undyne != null) {
                    // Construire le message à partir des arguments
                    StringBuilder messageBuilder = new StringBuilder();
                    for (int i = 2; i < args.length; i++) { // Commencer à partir de l'index 2
                        messageBuilder.append(args[i]).append(" "); // Ajouter chaque argument au message
                    }
                    String message = messageBuilder.toString().trim(); // Convertir en chaîne et enlever les espaces superflus

                    // Envoyer le message à Asgore
                    undyne.getPlayer().sendMessage(ChatColor.YELLOW + "[Asgore] " + message);
                }
            }
        }
    }

    private boolean isAsgore(Player player) {
        UhcPlayer uhcPlayer = playerManager.getPlayer(player);
        return uhcPlayer != null && uhcPlayer.getRole() != null && uhcPlayer.getRole().getName().equals("Asgore");
    }

    private ItemStack getFireballItem() {
        ItemStack fireball = new ItemStack(org.bukkit.Material.NETHER_STAR);
        ItemMeta fireballMeta = fireball.getItemMeta();
        if (fireballMeta != null) {
            fireballMeta.setDisplayName(ChatColor.RED + "Fireball");
            fireball.setItemMeta(fireballMeta);
        }
        return fireball;
    }
}
