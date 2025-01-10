package com.mguhc.undertale.roles.monstre.papyrus;

import com.mguhc.UhcAPI;
import com.mguhc.ability.AbilityManager;
import com.mguhc.ability.CooldownManager;
import com.mguhc.effect.EffectManager;
import com.mguhc.events.RoleGiveEvent;
import com.mguhc.player.PlayerManager;
import com.mguhc.player.UhcPlayer;
import com.mguhc.roles.RoleManager;
import com.mguhc.roles.UhcRole;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;

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
        }
    }

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
    private void OnDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (isPapyrus(playerManager.getPlayer(player)) && event.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
                event.setCancelled(true);
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
        // Méthode simplifiée pour détecter un joueur devant celui qui a utilisé l'item
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target != player && target.getLocation().distance(player.getLocation()) <= 20) { // distance approximative de 5 blocs
                return target;
            }
        }
        return null; // Aucun joueur trouvé
    }

    @EventHandler
    private void OnDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity().getPlayer();
        Player killer = event.getEntity().getKiller();
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
        UhcRole role = uhcPlayer.getRole();
        return role != null && role.getName().equals("Papyrus");
    }
}
