package com.mguhc.undertale.roles.monstre.mettaton;

import com.mguhc.UhcAPI;
import com.mguhc.ability.AbilityManager;
import com.mguhc.ability.CooldownManager;
import com.mguhc.effect.EffectManager;
import com.mguhc.events.RoleGiveEvent;
import com.mguhc.player.PlayerManager;
import com.mguhc.player.UhcPlayer;
import com.mguhc.roles.RoleManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class MettatonListener implements Listener {

    private EffectManager effectManager;
    private AbilityManager abilityManager;
    private CooldownManager cooldownManager;
    private RoleManager roleManager;
    private PlayerManager playerManager;
    private boolean formeEx = false;

    public MettatonListener() {
        UhcAPI api = UhcAPI.getInstance();
        this.playerManager = api.getPlayerManager();
        this.roleManager = api.getRoleManager();
        this.cooldownManager = api.getCooldownManager();
        this.abilityManager = api.getAbilityManager();
        this.effectManager = api.getEffectManager();
    }

    @EventHandler
    private void OnRoleGive(RoleGiveEvent event) {
        UhcPlayer uhcPlayer = roleManager.getPlayerWithRole("Mettaton");
        if (uhcPlayer != null) {
            Player player = uhcPlayer.getPlayer();
            effectManager.setWeakness(player, 20);
            effectManager.setResistance(player, 20);
            effectManager.setSpeed(player, 20);
        }
    }

    @EventHandler
    private void OnDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player player = (Player) event.getEntity();
            Player damager = (Player) event.getDamager();
            if (isMettaton(player) &&
                player.getHealth() - event.getFinalDamage() <= 0 &&
                !formeEx) {
                event.setCancelled(true);
                formeEx = true;
                teleportPlayerToRandomLocation(player);
                player.setMaxHealth(player.getMaxHealth() - 4);
                effectManager.removeEffect(player, PotionEffectType.WEAKNESS);
                effectManager.removeEffect(player, PotionEffectType.DAMAGE_RESISTANCE);
                effectManager.setStrength(player, 20);
                damager.sendMessage("Mettaton a changé de forme ! Il a été téléporter en " + player.getLocation().getBlockX() + " " + player.getLocation().getBlockZ());
            }
        }
    }

    private void teleportPlayerToRandomLocation(Player player) {
        Random random = new Random();
        int x = random.nextInt(201) + 400; // Coordonnée X aléatoire entre 400 et 600
        int z = random.nextInt(201) + 400; // Coordonnée Z aléatoire entre 400 et 600
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 255, 5 * 20));
        player.setHealth(player.getMaxHealth());
        player.teleport(new Location(player.getWorld(), x, 100, z));
    }

    private boolean isMettaton(Player player) {
        UhcPlayer uhcPlayer = playerManager.getPlayer(player);
        return uhcPlayer != null && uhcPlayer.getRole() != null && uhcPlayer.getRole().getName().equals("Mettaton");
    }
}
