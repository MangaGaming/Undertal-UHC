package com.mguhc.undertale.roles.solo.betty;

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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;

public class BettyListener implements Listener {

    private EffectManager effectManager;
    private AbilityManager abilityManager;
    private CooldownManager cooldownManager;
    private RoleManager roleManager;
    private PlayerManager playerManager;
    private String currentForme = "Terrifiante";
    private EffetDePeurAbility effetDePeurAbility;

    public BettyListener() {
        UhcAPI api = UhcAPI.getInstance();
        this.playerManager = api.getPlayerManager();
        this.roleManager = api.getRoleManager();
        this.cooldownManager = api.getCooldownManager();
        this.abilityManager = api.getAbilityManager();
        this.effectManager = api.getEffectManager();
    }

    @EventHandler
    private void OnRoleGive(RoleGiveEvent event) {
        UhcPlayer uhcPlayer = roleManager.getPlayerWithRole("Betty");
        if (uhcPlayer != null) {
            Player player = uhcPlayer.getPlayer();
            player.getInventory().addItem(getFormItem());
            player.getInventory().addItem(getSoulItem());

            effetDePeurAbility = new EffetDePeurAbility();
            abilityManager.registerAbility(roleManager.getUhcRole("Betty"), Collections.singletonList(effetDePeurAbility));

            startArmorTask(player);
        }
    }

    @EventHandler
    private void OnInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item != null && item.equals(getFormItem())) {
            switch (currentForme) {
                case "Ethérée":
                    effectManager.setStrength(player, 20);
                    currentForme = "Terrifiante";
                    break;
                case "Terrifiante":
                    effectManager.removeEffect(player, PotionEffectType.INCREASE_DAMAGE);
                    currentForme = "Protectrice";
                    break;
                case "Protectrice":
                    effectManager.removeEffect(player, PotionEffectType.DAMAGE_RESISTANCE);
                    currentForme = "Ethérée";
                    break;
            }
            player.sendMessage("Vous êtes passé en forme : " + currentForme);
        }
    }

    @EventHandler
    private void OnDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Player victim = (Player) event.getEntity();
            if (isBetty(damager)) {
                if (currentForme.equals("Terrifiante")) {
                    if (cooldownManager.getRemainingCooldown(damager, effetDePeurAbility) == 0) {
                        cooldownManager.startCooldown(damager, effetDePeurAbility);
                        victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 5 * 20, 0));
                        victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5 * 20, 0));
                        damager.sendMessage("Vous avez utilisé l'effet de peur");
                    }
                }
            }
        }
    }

    @EventHandler
    private void OnDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity().getPlayer();
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            if (isBetty(killer) && currentForme.equals("Protectrice")) {
                killer.setHealth(killer.getHealth() + 2);
            }
        }
    }

    private void startArmorTask(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (currentForme.equals("Ethérée")) {
                    if (player.getInventory().getArmorContents().length == 0) {
                        effectManager.removeEffect(player, PotionEffectType.DAMAGE_RESISTANCE);
                        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 40, 0));
                    }
                    else {
                        effectManager.setResistance(player, 20);
                    }
                }
            }
        }.runTaskTimer(UndertaleUHC.getInstance(), 0, 20);
    }

    @EventHandler
    private void OnCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String[] args = event.getMessage().split(" ");

        if (args.length == 2 && args[0].equals("/ut") && args[1].equals("akumu") && isBetty(player)) {
            if (isNight(player.getWorld().getTime()) || isInShadow(player.getLocation())) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 5 * 60 * 20, 0));
                effectManager.setSpeed(player, 50);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        effectManager.removeEffect(player, PotionEffectType.SPEED);
                    }
                }.runTaskLater(UndertaleUHC.getInstance(), 5*60*20);
            }
        }
    }

    private boolean isNight(long time) {
        return time >= 13000 && time < 23000; // Heure de nuit dans Minecraft
    }

    private boolean isInShadow(Location location) {
        // Vérifier les blocs autour du joueur dans une zone 3x3
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Location blockLocation = location.clone().add(x, 0, z);
                if (blockLocation.getBlock().getLightLevel() < 7) { // Condition d'ombre (niveau de lumière)
                    return true;
                }
            }
        }
        return false;
    }

    private ItemStack getFormItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(ChatColor.DARK_PURPLE + "Forme");
            item.setItemMeta(itemMeta);
        }
        return item;
    }

    private ItemStack getSoulItem() {
        ItemStack soul = new ItemStack(Material.NETHER_STAR);
        ItemMeta soul_meta = soul.getItemMeta();
        soul_meta.setDisplayName(ChatColor.DARK_PURPLE + "Ame de Peur");
        soul.setItemMeta(soul_meta);
        return soul;
    }

    private boolean isBetty(Player player) {
        UhcPlayer uhcPlayer = playerManager.getPlayer(player);
        return uhcPlayer != null && uhcPlayer.getRole() != null && uhcPlayer.getRole().getName().equals("Betty");
    }
}
