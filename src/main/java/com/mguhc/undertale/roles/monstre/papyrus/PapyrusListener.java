package com.mguhc.undertale.roles.monstre.papyrus;

import com.mguhc.UhcAPI;
import com.mguhc.ability.AbilityManager;
import com.mguhc.ability.CooldownManager;
import com.mguhc.effect.EffectManager;
import com.mguhc.events.RoleGiveEvent;
import com.mguhc.events.UhcDeathEvent;
import com.mguhc.player.PlayerManager;
import com.mguhc.player.UhcPlayer;
import com.mguhc.roles.RoleManager;
import com.mguhc.roles.UhcRole;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;

public class PapyrusListener implements Listener {

    private BlueAbility blueAbility;
    private EffectManager effectManager;
    private AbilityManager abilityManager;
    private CooldownManager cooldownManager;
    private RoleManager roleManager;
    private PlayerManager playerManager;
    private boolean canUseSpag = true;

    public PapyrusListener() {
        UhcAPI api = UhcAPI.getInstance();
        this.playerManager = api.getPlayerManager();
        this.roleManager = api.getRoleManager();
        this.cooldownManager = api.getCooldownManager();
        this.abilityManager = api.getAbilityManager();
        this.effectManager = api.getEffectManager();

        UhcRole role = roleManager.getUhcRole("Papyrus");
        if (role != null) {
            this.blueAbility = new BlueAbility();
            abilityManager.registerAbility(role, Arrays.asList(blueAbility));
        }
    }

    @EventHandler
    private void OnRoleGive(RoleGiveEvent event) {
        UhcPlayer uhcPlayer = roleManager.getPlayerWithRole("Papyrus");
        if(uhcPlayer != null) {
            Player player = uhcPlayer.getPlayer();
            UhcPlayer sans = roleManager.getPlayerWithRole("Sans");

            if(sans != null) {
                player.sendMessage("Sans : " + sans.getPlayer().getName());
            }

            ItemStack blue = new ItemStack(Material.NETHER_STAR);
            ItemMeta blue_meta = blue.getItemMeta();
            if (blue_meta != null) {
                blue_meta.setDisplayName(ChatColor.BLUE + "Blue");
                blue.setItemMeta(blue_meta);
            }
            player.getInventory().addItem(blue);

            effectManager.setNoFall(player, true);
        }
    }

    @EventHandler
    private void OnInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (isPapyrus(playerManager.getPlayer(player))) {
            if (item != null && item.hasItemMeta()) {
                if (item.getItemMeta().getDisplayName().equals(ChatColor.BLUE + "Blue")) {
                    if (!cooldownManager.isInCooldown(player, blueAbility)) {
                        cooldownManager.startCooldown(player, blueAbility);
                        Player target = getTargetPlayer(player);
                        if (target != null) {
                            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 10 * 20, 0, true, false));
                        }
                    }
                    else {
                        player.sendMessage("Vous êtes en cooldown pour" + cooldownManager.getRemainingCooldown(player, blueAbility));
                    }
                }
            }
        }
    }

    @EventHandler
    private void OnMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (isPapyrus(playerManager.getPlayer(player))) {
            UhcPlayer sans = roleManager.getPlayerWithRole("Sans");
            if (sans != null) {
                if (player.getNearbyEntities(15, 15, 15).contains(sans.getPlayer())) {
                    effectManager.setResistance(player, 20);
                }
            }
        }
    }

    @EventHandler
    private void OnCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String[] args = event.getMessage().split(" ");

        if (args.length == 3 && args[0].equalsIgnoreCase("/ut") && args[1].equalsIgnoreCase("spag")) {
            if (isPapyrus(playerManager.getPlayer(player)) &&
                canUseSpag) {
                Player target = Bukkit.getPlayer(args[2]);
                if (target != null) {
                    canUseSpag = false;
                    player.setMaxHealth(player.getMaxHealth() + 2);
                    effectManager.setSpeed(player, 20);
                }
            }
        }
    }

    private Player getTargetPlayer(Player player) {
        // Get the player's eye location and direction
        Location eyeLocation = player.getEyeLocation();
        Vector direction = eyeLocation.getDirection().normalize();

        // Get nearby entities within the specified distance
        List<Entity> nearbyEntities = player.getNearbyEntities(20, 20, 20);

        for (Entity entity : nearbyEntities) {
            if (entity instanceof Player && entity != player) {
                // Check if the entity is in the line of sight
                Vector toEntity = entity.getLocation().toVector().subtract(eyeLocation.toVector()).normalize();
                double dotProduct = direction.dot(toEntity);

                // Check if the entity is within the player's line of sight
                if (dotProduct > 0.9) { // Adjust the threshold for precision
                    return (Player) entity; // Return the target player
                }
            }
        }
        return null; // No target player found
    }




    @EventHandler
    private void OnDeath(UhcDeathEvent event) {
        Player victim = event.getPlayer();
        Player killer = event.getKiller();
        if (killer != null) {
            UhcRole role = playerManager.getPlayer(victim).getRole();
            if (role != null && role.getName().equals("Undyne")) {
                UhcPlayer papyrus = roleManager.getPlayerWithRole("Papyrus");
                if (papyrus != null) {
                    papyrus.getPlayer().sendMessage("Undyne a été tué par " + killer.getName());
                }
            }
        }
    }

    private boolean isPapyrus(UhcPlayer uhcPlayer) {
        return uhcPlayer != null && uhcPlayer.getRole() != null && uhcPlayer.getRole().getName().equals("Papyrus");
    }
}
