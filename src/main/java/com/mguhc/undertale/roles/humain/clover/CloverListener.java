package com.mguhc.undertale.roles.humain.clover;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.mguhc.events.RoleGiveEvent;
import com.mguhc.events.UhcDeathEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.mguhc.UhcAPI;
import com.mguhc.ability.Ability;
import com.mguhc.ability.AbilityManager;
import com.mguhc.ability.CooldownManager;
import com.mguhc.player.PlayerManager;
import com.mguhc.player.UhcPlayer;
import com.mguhc.roles.Camp;
import com.mguhc.roles.RoleManager;
import com.mguhc.roles.UhcRole;
import com.mguhc.undertale.UndertaleUHC;

public class CloverListener implements Listener {

    private PlayerManager playerManager;
    private RoleManager roleManager;
    private CooldownManager cooldownManager;
    private AbilityManager abilityManager;

    private Ability justiceAbility;
    private Ability balanceAbility;
    private Ability soulPowerAbility;
    private Ability justiceIAbility;

    public CloverListener() {
        this.playerManager = UhcAPI.getInstance().getPlayerManager();
        this.roleManager = UhcAPI.getInstance().getRoleManager();
        this.cooldownManager = UhcAPI.getInstance().getCooldownManager();
        this.abilityManager = UhcAPI.getInstance().getAbilityManager();

        // Enregistrer les abilities pour le rôle "Clover"
        UhcRole cloverRole = roleManager.getUhcRole("Clover");
        if (cloverRole != null) {
            this.justiceAbility = new Ability("/ut justice", 5*60*1000);
            this.balanceAbility = new Ability("Balance", 20*60*1000);
            this.justiceIAbility = new Ability("Justice Implacable", 30*1000);
            this.soulPowerAbility = new Ability("Pouvoir de l'âme", 20*60*1000);

            List<Ability> abilities = Arrays.asList(justiceAbility, balanceAbility, justiceIAbility, soulPowerAbility);

            abilityManager.registerAbility(cloverRole, abilities);
        }
    }

    @EventHandler
    private void OnRoleGive(RoleGiveEvent event) {
        UhcPlayer uhcPlayer = roleManager.getPlayerWithRole("Clover");
        if(uhcPlayer != null) {
            Player player = uhcPlayer.getPlayer();
            // Items pour Clover
            ItemStack cloverItem = new ItemStack(Material.NETHER_STAR);
            ItemMeta cloverMeta = cloverItem.getItemMeta(); // Obtenir l'ItemMeta
            if (cloverMeta != null) {
                cloverMeta.setDisplayName(ChatColor.YELLOW + "Ame de Justice");
                cloverItem.setItemMeta(cloverMeta); // Appliquer l'ItemMeta à l'ItemStack
            }
            player.getInventory().addItem(cloverItem);
        }
    }

    @EventHandler
    public void OnDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player && event.getEntity() instanceof Player)) {
            return;
        }
        if (!UhcAPI.getInstance().getUhcGame().getCurrentPhase().getName().equals("Playing")) {
            return;
        }
        Player damager = (Player) event.getDamager();
        Player victim = (Player) event.getEntity();

        UhcPlayer uhcPlayerDamager = playerManager.getPlayer(damager);
        UhcPlayer uhcPlayerVictim = playerManager.getPlayer(victim);

        if (uhcPlayerDamager == null || uhcPlayerVictim == null) {
            return;
        }

        if (isClover(uhcPlayerDamager)) {
            if (!cooldownManager.isInCooldown(damager, justiceIAbility)) {
                event.setDamage(event.getDamage() + 0.5);
                cooldownManager.startCooldown(damager, justiceIAbility);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        damager.sendMessage("Cooldown Finie");
                    }
                }.runTaskLater(UndertaleUHC.getInstance(), 30 * 20);
            } else {
                damager.sendMessage("Vous êtes en cooldown pour : " + cooldownManager.getRemainingCooldown(damager, justiceIAbility)/20 + " secondes");
            }
        }

        if (damager.getInventory().contains(getSoulItem())) {
            event.setDamage(event.getDamage() + 0.5);
        }

        UhcPlayer soulUhcPlayer = getPlayerWithSoul();
        if (soulUhcPlayer != null &&
            !isAlly(soulUhcPlayer, uhcPlayerDamager) &&
            isAlly(soulUhcPlayer, uhcPlayerVictim) &&
            soulUhcPlayer.getPlayer().getNearbyEntities(15, 15, 15).contains(damager) &&
            !cooldownManager.isInCooldown(soulUhcPlayer.getPlayer(), soulPowerAbility)) {
            soulUhcPlayer.getPlayer().sendMessage(ChatColor.GREEN + "Un ennemi proche de vous a attaqué un de vos alliés !");
            cooldownManager.startCooldown(soulUhcPlayer.getPlayer(), soulPowerAbility);
        }
    }

    @EventHandler
    public void OnCommand(PlayerCommandPreprocessEvent event) {
        if (event.getMessage().equals("/ut justice")) {
            Player player = event.getPlayer();
            if (cooldownManager.getRemainingCooldown(player, justiceAbility) == 0) {
                cooldownManager.startCooldown(player, justiceAbility);
                for (Entity nearbyEntity : player.getNearbyEntities(5, 5, 5)) {
                    if (nearbyEntity instanceof Player) {
                        Player nearbyPlayer = (Player) nearbyEntity;
                        UhcPlayer uhcPlayerNearby = playerManager.getPlayer(nearbyPlayer);
                        UhcPlayer uhcPlayer = playerManager.getPlayer(player);
                        if (uhcPlayer != null && uhcPlayerNearby != null && isAlly(uhcPlayer, uhcPlayerNearby)) {
                            if (nearbyPlayer.getHealth() <= 10) {
                                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 5 * 20, 0));
                                player.sendMessage(ChatColor.GREEN + "Vous avez activé la capacité de Justice !");

                                nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 5 * 20, 0));
                                nearbyPlayer.sendMessage(ChatColor.GREEN + "Vous avez reçu la capacité de Justice !");
                            }
                        }
                    }
                }
            }
            else {
                player.sendMessage("§cVous êtes en cooldown pour : " + (long) cooldownManager.getRemainingCooldown(player, justiceAbility) / 1000 + "s");
            }
        }
    }

    @EventHandler
    public void OnDeath(UhcDeathEvent event) {
        Player victim = event.getPlayer();
        Player killer = event.getKiller();
        if (killer != null &&
                isClover(playerManager.getPlayer(killer)) &&
                !cooldownManager.isInCooldown(killer, balanceAbility)) {
            killer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10 * 20, 0));
            killer.sendMessage(ChatColor.GREEN + "Vous avez activé la capacité de Balance !");
            cooldownManager.startCooldown(killer, balanceAbility);
        }
        if (isClover(playerManager.getPlayer(victim))) {
            List<ItemStack> drops = event.getDrops();
            if (drops.contains(getSoulItem())) {
                drops.remove(getSoulItem());
                Location soulDrop = new Location(victim.getWorld(), victim.getLocation().getX(), victim.getLocation().getY(), victim.getLocation().getZ() + 15);
                victim.getWorld().dropItem(soulDrop, getSoulItem());
                Bukkit.broadcastMessage("Ame de Justice drop en " + soulDrop.getX() + " " + soulDrop.getY() + " " + soulDrop.getZ());
            }
        }
    }

    private ItemStack getSoulItem() {
        ItemStack soul = new ItemStack(Material.NETHER_STAR);
        ItemMeta soulMeta = soul.getItemMeta();
        if (soulMeta != null) {
            soulMeta.setDisplayName(ChatColor.YELLOW + "Ame de Justice");
            soul.setItemMeta(soulMeta);
        }
        return soul;
    }

    private boolean isAlly(UhcPlayer player, UhcPlayer potentialAlly) {
        Camp playerCamp = UhcAPI.getInstance().getRoleManager().getCamp(player);
        Camp allyCamp = UhcAPI.getInstance().getRoleManager().getCamp(potentialAlly);
        return playerCamp != null && playerCamp.equals(allyCamp);
    }

    private boolean isClover(UhcPlayer uhc_player) {
        return uhc_player != null && uhc_player.getRole() != null && uhc_player.getRole().getName().equals("Clover");
    }

    private UhcPlayer getPlayerWithSoul() {
        for (Map.Entry<Player, UhcPlayer> entry : playerManager.getPlayers().entrySet()) {
            Player player = entry.getKey();
            if (player.getInventory().contains(getSoulItem())) {
                return playerManager.getPlayer(player);
            }
        }
        return null;
    }
}