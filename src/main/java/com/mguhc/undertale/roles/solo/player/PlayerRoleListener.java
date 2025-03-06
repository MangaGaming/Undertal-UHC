package com.mguhc.undertale.roles.solo.player;

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
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;

public class PlayerRoleListener implements Listener {
    private Ability manipulationAbility;
    private EffectManager effectManager;
    private AbilityManager abilityManager;
    private CooldownManager cooldownManager;
    private RoleManager roleManager;
    private PlayerManager playerManager;

    private boolean needToDamage = false;
    private int dispertionCount = 0;
    private Location ancientLocation;
    private Ability espionAbility;
    private boolean canUseBrouiller = true;
    private List<Player> playerSpectated;

    public PlayerRoleListener() {
        UhcAPI api = UhcAPI.getInstance();
        this.playerManager = api.getPlayerManager();
        this.roleManager = api.getRoleManager();
        this.cooldownManager = api.getCooldownManager();
        this.abilityManager = api.getAbilityManager();
        this.effectManager = api.getEffectManager();

        UhcRole role = roleManager.getUhcRole("Player");
        if (role != null) {
            this.manipulationAbility = new Ability("/ut manipulation", 20*60*1000);
            this.espionAbility = new Ability("/ut espion", 20*60*1000);
            abilityManager.registerAbility(role, Arrays.asList(manipulationAbility, espionAbility));
        }
    }

    @EventHandler
    private void OnRoleGive(RoleGiveEvent event) {
        UhcPlayer uhcPlayer = roleManager.getPlayerWithRole("Player");
        if (uhcPlayer != null) {
            Player player = uhcPlayer.getPlayer();
            player.getInventory().addItem(getDispertionItem());
        }
    }

    @EventHandler
    private void OnCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String[] args = event.getMessage().split(" ");

        if (args.length == 4 && args[0].equals("/ut") && args[1].equals("manipulation") && isPlayer(player)) {
            Player forced =  Bukkit.getPlayer(args[2]);
            Player target = Bukkit.getPlayer(args[3]);
            if (forced != null && target != null) {
                if (cooldownManager.getRemainingCooldown(player, manipulationAbility) == 0) {
                    cooldownManager.startCooldown(player, manipulationAbility);
                    needToDamage = true;
                    forced.sendMessage(ChatColor.RED + "Vous etes manipules, vous devez attaquer ce Joueur : " + target.getName() + " si vous n'obéissez pas vous en payerez les consequences.");
                    Bukkit.getPluginManager().registerEvents(new Listener() {
                        @EventHandler
                        private void OnDamage(EntityDamageByEntityEvent e) {
                            if (e.getEntity().equals(target) && e.getDamager().equals(forced) && needToDamage) {
                                needToDamage = false;
                                forced.sendMessage(ChatColor.GREEN + "Vous avez respecter les ordres qui vous avez été donné");
                            }
                        }
                    },UndertaleUHC.getInstance());
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (needToDamage) {
                                forced.sendMessage(ChatColor.RED + "Vous n'avez pas respecté les ordres donnés");
                                forced.setMaxHealth(forced.getMaxHealth() - 4);
                            }
                        }
                    }.runTaskLater(UndertaleUHC.getInstance(), 10*60+20);
                }
                else {
                    player.sendMessage("Vous êtes en cooldown pour " + cooldownManager.getRemainingCooldown(player, manipulationAbility) / 1000 + " secondes");
                }
            }
        }
        if (args.length == 3 && args[0].equals("/ut") && args[1].equals("espion") && isPlayer(player)) {
            Player target = Bukkit.getPlayer(args[2]);
            if (target != null) {
                if (cooldownManager.getRemainingCooldown(player, espionAbility) == 0) {
                    cooldownManager.startCooldown(player, espionAbility);
                    ancientLocation = player.getLocation();
                    player.teleport(target.getLocation());
                    player.setGameMode(GameMode.SPECTATOR);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.teleport(ancientLocation);
                            player.setGameMode(GameMode.SURVIVAL);
                        }
                    }.runTaskLater(UndertaleUHC.getInstance(), 5*60*20);
                }
                else {
                    player.sendMessage("Vous êtes en cooldown pour " + cooldownManager.getRemainingCooldown(player, espionAbility) / 1000 + " secondes");
                }
            }
        }

        if (args.length == 2 && args[0].equals("/ut") && args[1].equals("brouiller") && isPlayer(player)) {
            if (canUseBrouiller) {
                canUseBrouiller = false;
                for (Entity e : player.getNearbyEntities(15, 15, 15)) {
                    if (e instanceof Player) {
                        Player p = (Player) e;
                        p.sendMessage(ChatColor.RED + "Vous avez été touché par le pouvoir du /ut brouiller de Player veuillez couper votre micro");
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                p.sendMessage("Vous pouvez remettre votre micro");
                            }
                        }.runTaskLater(UndertaleUHC.getInstance(), 10*20);
                    }
                }
            }
        }
    }

    @EventHandler
    private void OnInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item != null && item.equals(getDispertionItem()) && dispertionCount <= 3) {
            dispertionCount ++;
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 5*60*20, 0));
            effectManager.setNoFall(player, true);
            new BukkitRunnable() {
                @Override
                public void run() {
                    effectManager.setNoFall(player, false);
                }
            }.runTaskLater(UndertaleUHC.getInstance(), 5*60*20);
        }
    }

    @EventHandler
    private void OnMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (isPlayer(player)) {
            for (Entity e : player.getNearbyEntities(5, 5, 5)) {
                if (e instanceof Player) {
                    Player p = (Player) e;
                    if(p.getInventory().contains(getGasterItem()) &&
                       !playerSpectated.contains(p)) {
                        player.sendMessage(ChatColor.DARK_PURPLE + "Une aura obscure vous menace");
                        playerSpectated.add(p);
                    }
                }
            }
        }
    }

    private ItemStack getDispertionItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + "Dispertion");
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack getGasterItem() {
        ItemStack gaster = new ItemStack(Material.NETHER_STAR);
        ItemMeta gasterMeta = gaster.getItemMeta();
        if(gasterMeta != null) {
            gasterMeta.setDisplayName(ChatColor.DARK_PURPLE + "Fragment de Gaster");
            gaster.setItemMeta(gasterMeta);
        }
        return gaster;
    }

    private boolean isPlayer(Player player) {
        UhcPlayer uhcPlayer = playerManager.getPlayer(player);
        return uhcPlayer != null && uhcPlayer.getRole() != null && uhcPlayer.getRole().getName().equals("Player");
    }
}
