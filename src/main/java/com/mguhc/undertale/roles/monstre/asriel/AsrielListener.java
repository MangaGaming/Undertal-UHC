package com.mguhc.undertale.roles.monstre.asriel;

import com.mguhc.UhcAPI;
import com.mguhc.ability.Ability;
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
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

public class AsrielListener implements Listener {

    private EffectManager effectManager;
    private AbilityManager abilityManager;
    private CooldownManager cooldownManager;
    private RoleManager roleManager;
    private PlayerManager playerManager;

    private int starCount = 0;
    private boolean canUseHyperdash = true;
    private Ability starAbility;

    public AsrielListener() {
        UhcAPI api = UhcAPI.getInstance();
        this.playerManager = api.getPlayerManager();
        this.roleManager = api.getRoleManager();
        this.cooldownManager = api.getCooldownManager();
        this.abilityManager = api.getAbilityManager();
        this.effectManager = api.getEffectManager();

        UhcRole asriel = roleManager.getUhcRole("Asriel");
        if (asriel != null) {
            this.starAbility = new StarAbility();
            abilityManager.registerAbility(asriel, Arrays.asList(starAbility));
        }
    }

    @EventHandler
    private void OnRoleGive(RoleGiveEvent event) {
        UhcPlayer uhcPlayer = roleManager.getPlayerWithRole("Asriel");
        if (uhcPlayer != null) {
            Player player = uhcPlayer.getPlayer();
            player.getInventory().addItem(getStarItem());
        }
    }

    @EventHandler
    private void OnInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (isAsriel(player) && item != null && item.equals(getStarItem())) {
            if (starCount < 2) {
                if (cooldownManager.getRemainingCooldown(player, starAbility) == 0) {
                    cooldownManager.startCooldown(player, starAbility);
                    // Lancer l'étoile
                    player.sendMessage(ChatColor.GREEN + "Vous avez lancé une étoile !");
                    starCount++;

                    // Déterminer la position du joueur
                    Location playerLocation = player.getLocation();

                    // Créer une explosion qui ne cause pas de dégâts
                    player.getWorld().createExplosion(playerLocation, 8f, false); // Rayon de 4 blocs

                    // Appliquer l'effet de blightness à tous les joueurs dans un rayon de 9 blocs
                    for (Entity entity : player.getNearbyEntities(9, 9, 9)) {
                        if (entity instanceof Player) {
                            Player target = (Player) entity;
                            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0)); // 3 secondes
                        }
                    }
                }
                else {
                    player.sendMessage("Vous êtes en cooldown pour " + (long) cooldownManager.getRemainingCooldown(player, starAbility) / 1000);
                }
            } else {
                player.sendMessage(ChatColor.RED + "Vous avez déjà lancé le maximum d'étoiles !");
            }
        }
    }

    @EventHandler
    private void OnCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String[] args = event.getMessage().split(" ");
        if (args.length == 2 && args[0].equals("/ut") && args[1].equals("hyperdash") &&
            canUseHyperdash) {
            canUseHyperdash = false;
            player.sendMessage("Vous avez gagnés vos effets");
            effectManager.setSpeed(player, 20);
            effectManager.setResistance(player , 20);
            effectManager.setStrength(player, 20);
            effectManager.setNoFall(player, true);
            new BukkitRunnable() {
                @Override
                public void run() {
                    effectManager.removeEffect(player, PotionEffectType.SPEED);
                    effectManager.removeEffect(player, PotionEffectType.DAMAGE_RESISTANCE);
                    effectManager.removeEffect(player, PotionEffectType.INCREASE_DAMAGE);
                    effectManager.setNoFall(player, false);
                    effectManager.setWeakness(player, 20);
                    player.setMaxHealth(player.getMaxHealth() - 4);
                    player.sendMessage("Vous avez perdu vos effets");
                }
            }.runTaskLater(UndertaleUHC.getInstance(), 10*20);
        }
    }

    @EventHandler
    private void OnDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (event.getCause().equals(EntityDamageEvent.DamageCause.BLOCK_EXPLOSION)) {
                event.setCancelled(true);
            }
        }
    }

    private boolean isAsriel(Player player) {
        UhcPlayer uhcPlayer = playerManager.getPlayer(player);
        return uhcPlayer != null && uhcPlayer.getRole() != null && uhcPlayer.getRole().getName().equals("Asriel");
    }

    private ItemStack getStarItem() {
        ItemStack star = new ItemStack(org.bukkit.Material.NETHER_STAR);
        ItemMeta starMeta = star.getItemMeta();
        if (starMeta != null) {
            starMeta.setDisplayName(ChatColor.RED + "Star Meteor");
            star.setItemMeta(starMeta);
        }
        return star;
    }
}
