package com.mguhc.undertale.roles.humain.aliza;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mguhc.ability.Ability;
import com.mguhc.ability.AbilityManager;
import com.mguhc.ability.CooldownManager;
import com.mguhc.effect.EffectManager;
import com.mguhc.events.RoleGiveEvent;
import com.mguhc.events.UhcDeathEvent;
import com.mguhc.roles.RoleManager;
import com.mguhc.roles.UhcRole;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.mguhc.UhcAPI;
import com.mguhc.player.PlayerManager;
import com.mguhc.player.UhcPlayer;
import com.mguhc.undertale.UndertaleUHC;

public class AlizaListener implements Listener {
    private EffectManager effectManager;
    private CooldownManager cooldownManager;
    private InstinctAbility instinctAbility;
    private AbilityManager abilityManager;
    private RoleManager roleManager;
    private List<Player> playerWhoUsedSoul = new ArrayList<>();
    private int instinctCount = 0;
    private PlayerManager playerManager;
    private boolean canUseForce = true;

    public AlizaListener() {
        this.playerManager = UhcAPI.getInstance().getPlayerManager();
        this.roleManager = UhcAPI.getInstance().getRoleManager();
        this.cooldownManager = UhcAPI.getInstance().getCooldownManager();
        this.abilityManager = UhcAPI.getInstance().getAbilityManager();
        this.effectManager = UhcAPI.getInstance().getEffectManager();

        UhcRole alizaRole = roleManager.getUhcRole("Aliza");
        if(alizaRole != null) {
            this.instinctAbility = new InstinctAbility();
            List<Ability> abilities = Arrays.asList(instinctAbility);
            abilityManager.registerAbility(alizaRole, abilities);
        }
    }

    @EventHandler
    private void OnRoleGive(RoleGiveEvent event) {
        UhcPlayer uhcPlayer  = roleManager.getPlayerWithRole("Aliza");
        if(uhcPlayer != null) {
            Player player = uhcPlayer.getPlayer();
            // Items pour Aliza
            ItemStack alizaItem = new ItemStack(Material.NETHER_STAR);
            ItemMeta alizaMeta = alizaItem.getItemMeta(); // Obtenir l'ItemMeta
            if (alizaMeta != null) {
                alizaMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Ame de Perseverance");
                alizaItem.setItemMeta(alizaMeta); // Appliquer l'ItemMeta à l'ItemStack
            }
            player.getInventory().addItem(alizaItem);
            
            effectManager.setSpeed(player, 20);
            effectManager.setWeakness(player, 20);
        }
    }

    @EventHandler
    public void OnDamage(EntityDamageByEntityEvent event) {
        if (!UhcAPI.getInstance().getUhcGame().getCurrentPhase().getName().equals("Playing")) {
            return;
        }
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player victim = (Player) event.getEntity();
            if (isAliza(playerManager.getPlayer(victim)) && victim.getHealth() <= 6) {
                if (canUseForce) {
                    canUseForce = false;
                    victim.removePotionEffect(PotionEffectType.WEAKNESS);
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 10 * 20, 0));
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 120 * 20, 0));
                    victim.sendMessage("Force Utilisé");
                }
            }
            if (victim.getInventory().contains(getSoulItem())) {
                if (victim.getHealth() <= 8 && !playerWhoUsedSoul.contains(victim)) {
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 10 * 20, 0));
                    victim.sendMessage("Ame Utilisé");
                    playerWhoUsedSoul.add(victim);
                } else {
                    victim.sendMessage("[Debug] Vous avez déjà utilisé l'âme");
                }
                event.setDamage(event.getDamage() - 0.5);
                victim.sendMessage("Damage réduit");
            }
        }
    }

    @EventHandler
    public void OnCommand(PlayerCommandPreprocessEvent event) {
        if (!UhcAPI.getInstance().getUhcGame().getCurrentPhase().getName().equals("Playing")) {
            return;
        }
        if (event.getMessage().equals("/ut instinct")) {
            Player player = event.getPlayer();
            if (isAliza(playerManager.getPlayer(player))) {
                if (instinctCount < 3 && cooldownManager.isInCooldown(player, instinctAbility)) {
                    instinctCount++;
                    cooldownManager.startCooldown(player, instinctAbility);
                    new BukkitRunnable() {
                        int timer = 0;

                        @Override
                        public void run() {
                            if (timer <= 300) {
                                if (player.getInventory().getHelmet() == null &&
                                        player.getInventory().getChestplate() == null &&
                                        player.getInventory().getLeggings() == null &&
                                        player.getInventory().getBoots() == null) {
                                    player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 300 * 20, 0));
                                } else {
                                    player.removePotionEffect(PotionEffectType.INVISIBILITY);
                                }
                            } else {
                                this.cancel();
                            }
                            timer++;
                        }
                    }.runTaskTimer(UndertaleUHC.getInstance(), 0, 20);
                } else {
                    player.sendMessage("Vous devez attendre " + (long) cooldownManager.getRemainingCooldown(player, instinctAbility)/1000 + " secondes");
                }
            }
        }
    }

    @EventHandler
    private void OnDeath(UhcDeathEvent event) {
        Player victim = event.getPlayer();
        Player killer = event.getKiller();
        if (killer != null && isAliza(playerManager.getPlayer(victim))) {
            List<ItemStack> drops = event.getDrops();
            Location soulDrop = victim.getLocation();
            drops.remove(getSoulItem());
            victim.getWorld().dropItem(soulDrop, getSoulItem());
            killer.sendMessage("L'ame de persévérance est apparue");
        }
    }

    private boolean isAliza(UhcPlayer uhc_player) {
        return uhc_player != null && uhc_player.getRole() != null && uhc_player.getRole().getName().equals("Aliza");
    }

    private ItemStack getSoulItem() {
        ItemStack soul = new ItemStack(Material.NETHER_STAR);
        ItemMeta soul_meta = soul.getItemMeta();
        soul_meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Ame de Perseverance");
        soul.setItemMeta(soul_meta);
        return soul;
    }
}