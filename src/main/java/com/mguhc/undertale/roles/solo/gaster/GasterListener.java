package com.mguhc.undertale.roles.solo.gaster;

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
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class GasterListener implements Listener {

    private EffectManager effectManager;
    private AbilityManager abilityManager;
    private CooldownManager cooldownManager;
    private RoleManager roleManager;
    private PlayerManager playerManager;

    private boolean trueLabForm = false;
    private int gasterBlasterCount;

    public GasterListener() {
        UhcAPI api = UhcAPI.getInstance();
        this.playerManager = api.getPlayerManager();
        this.roleManager = api.getRoleManager();
        this.cooldownManager = api.getCooldownManager();
        this.abilityManager = api.getAbilityManager();
        this.effectManager = api.getEffectManager();
    }

    @EventHandler
    private void OnRoleGive(RoleGiveEvent event) {
        UhcPlayer uhcPlayer = roleManager.getPlayerWithRole("W.D Gaster");
        if (uhcPlayer != null) {
            Player player = uhcPlayer.getPlayer();
            effectManager.setWeakness(player, 20);

            new BukkitRunnable() {
                @Override
                public void run() {
                    long time = player.getWorld().getTime(); // Obtenir le temps actuel dans le monde
                    if (time >= 0 && time < 12000) {
                        effectManager.removeEffect(player, PotionEffectType.SPEED);
                    } else if (!trueLabForm) {
                        effectManager.setSpeed(player, 20);
                    }
                }
            }.runTaskTimer(UndertaleUHC.getInstance(), 0, 20 * 3);
        }
    }

    @EventHandler
    private void OnInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        Block block = event.getClickedBlock();

        // Interaction avec le coffre
        if (block != null && block.getType() == Material.CHEST && block.getLocation().getY() == 247) {
            int gasterFrag = 0;
            for (ItemStack i : player.getInventory().getContents()) {
                if (i != null && i.equals(getGasterItem())) {
                    gasterFrag++;
                    if (gasterFrag >= 3) {
                        break;
                    }
                }
            }
            if (gasterFrag == 3) {
                player.getInventory().remove(getGasterItem());
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!player.isDead() && player.isOnline()) {
                            addTrueLabFormEffect(player);
                        }
                    }
                }.runTaskLater(UndertaleUHC.getInstance(), 5 * 60 * 20);
            }
        }

        // Tirer le Gaster Blaster
        if (item != null && item.equals(getGasterBlaster()) && trueLabForm && gasterBlasterCount < 5) {
            shootGasterBlaster(player);
            gasterBlasterCount++;
        }
    }

    private void shootGasterBlaster(Player player) {
        // Nombre de flèches à tirer
        int numberOfArrows = 5;

        // Délai entre chaque flèche (en ticks)
        int delayBetweenArrows = 2;

        for (int i = 0; i < numberOfArrows; i++) {
            // Nécessaire pour la référence dans la tâche
            new BukkitRunnable() {
                @Override
                public void run() {
                    // Tirer une flèche
                    Arrow arrow = player.getWorld().spawnArrow(player.getEyeLocation(), player.getLocation().getDirection(), 1.6f, 12.0f);
                    arrow.setCustomName("Gaster Blaster");
                    arrow.setCustomNameVisible(false);
                    arrow.setShooter(player);
                }
            }.runTaskLater(UndertaleUHC.getInstance(), i * delayBetweenArrows);
        }
    }

    @EventHandler
    private void onArrowHit(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity entity = event.getEntity();

        if (damager instanceof Arrow) {
            Arrow arrow = (Arrow) damager;
            if (arrow.getShooter() instanceof Player) {
                Player shooter = (Player) arrow.getShooter();
                if (entity instanceof Player) {
                    Player hitPlayer = (Player) entity;
                    if (arrow.getCustomName().equals("Gaster Blaster")) {
                        hitPlayer.getWorld().strikeLightning(hitPlayer.getLocation());
                    }
                }
            }
        }
    }

    private void addTrueLabFormEffect(Player player) {
        trueLabForm = true;
        effectManager.setResistance(player, 20);
        effectManager.setStrength(player, 20);
        effectManager.setStrength(player, 40);
        effectManager.setNoFall(player, true);
        player.setMaxHealth(player.getMaxHealth() + 4);
        effectManager.removeEffect(player, PotionEffectType.WEAKNESS);
    }

    private ItemStack getGasterItem() {
        ItemStack gaster = new ItemStack(Material.NETHER_STAR);
        ItemMeta gasterMeta = gaster.getItemMeta();
        if (gasterMeta != null) {
            gasterMeta.setDisplayName(ChatColor.DARK_PURPLE + "Fragment de Gaster");
            gaster.setItemMeta(gasterMeta);
        }
        return gaster;
    }

    private ItemStack getGasterBlaster() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(ChatColor.DARK_PURPLE + "Gaster Blaster");
            item.setItemMeta(itemMeta);
        }
        return item;
    }
}