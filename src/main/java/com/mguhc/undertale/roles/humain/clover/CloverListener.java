package com.mguhc.undertale.roles.humain.clover;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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

    private JusticeAbility justiceAbility;
    private BalanceAbility balanceAbility;
    private SoulPowerAbility soulPowerAbility;

    public CloverListener() {
        this.playerManager = UhcAPI.getInstance().getPlayerManager();
        this.roleManager = UhcAPI.getInstance().getRoleManager();
        this.cooldownManager = UhcAPI.getInstance().getCooldownManager();
        this.abilityManager = UhcAPI.getInstance().getAbilityManager();

        // Enregistrer les abilities pour le rôle "Clover"
        UhcRole cloverRole = roleManager.getUhcRole("Clover"); // Assurez-vous que le rôle "Clover" existe
        if (cloverRole != null) {
            this.justiceAbility = new JusticeAbility();
            this.balanceAbility = new BalanceAbility();
            this.soulPowerAbility = new SoulPowerAbility();

            List<Ability> abilities = Arrays.asList(justiceAbility, balanceAbility, soulPowerAbility);

            abilityManager.registerAbility(cloverRole, abilities);
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
            if (!cooldownManager.isInCooldown(damager, justiceAbility)) {
                event.setDamage(event.getDamage() + 0.5);
                cooldownManager.startCooldown(damager, justiceAbility);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        damager.sendMessage("Cooldown Finie");
                    }
                }.runTaskLater(UndertaleUHC.getInstance(), 30 * 20);
            } else {
                damager.sendMessage("Vous êtes en cooldown pour : " + cooldownManager.getRemainingCooldown(damager, justiceAbility)/20 + " secondes");
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
            soulPowerAbility.activate(soulUhcPlayer.getPlayer());
            cooldownManager.startCooldown(soulUhcPlayer.getPlayer(), soulPowerAbility);
        }
    }

    @EventHandler
    public void OnCommand(PlayerCommandPreprocessEvent event) {
        if (event.getMessage().equals("/ut justice")) {
            Player player = event.getPlayer();
            for (Entity nearbyEntity : player.getNearbyEntities(5, 5, 5)) {
                if (nearbyEntity instanceof Player) {
                    Player nearbyPlayer = (Player) nearbyEntity;
                    UhcPlayer uhcPlayerNearby = playerManager.getPlayer(nearbyPlayer);
                    UhcPlayer uhcPlayer = playerManager.getPlayer(player);
                    if (uhcPlayer != null && uhcPlayerNearby != null && isAlly(uhcPlayer, uhcPlayerNearby)) {
                        if (nearbyPlayer.getHealth() <= 10) {
                            justiceAbility.activate(player);
                            justiceAbility.activate(nearbyPlayer);
                        } else {
                            player.sendMessage("Vous n'avez pas d'allié blessé à proximité");
                        }
                    } else {
                        player.sendMessage("Vous n'avez pas d'allié blessé à proximité");
                    }
                } else {
                    player.sendMessage("Vous n'avez pas d'allié blessé à proximité");
                }
            }
        }
    }

    @EventHandler
    public void OnDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity().getPlayer();
        Player killer = event.getEntity().getKiller();
        if (killer != null &&
                isClover(playerManager.getPlayer(killer)) &&
                !cooldownManager.isInCooldown(killer, balanceAbility)) {
            balanceAbility.activate(killer);
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
        return playerCamp != null && allyCamp != null && playerCamp.equals(allyCamp);
    }

    private boolean isClover(UhcPlayer uhc_player) {
        return uhc_player.getRole() != null && uhc_player.getRole().getName().equals("Clover");
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