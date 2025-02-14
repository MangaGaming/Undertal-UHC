package com.mguhc.undertale.roles.humain.cody;

import java.util.Arrays;
import java.util.List;

import com.mguhc.events.RoleGiveEvent;
import com.mguhc.events.UhcDeathEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.mguhc.UhcAPI;
import com.mguhc.ability.Ability;
import com.mguhc.ability.AbilityManager;
import com.mguhc.ability.CooldownManager;
import com.mguhc.effect.EffectManager;
import com.mguhc.player.PlayerManager;
import com.mguhc.player.UhcPlayer;
import com.mguhc.roles.RoleManager;
import com.mguhc.roles.UhcRole;
import com.mguhc.undertale.UndertaleUHC;

public class CodyListener implements Listener {
    
    private PlayerManager playerManager;
    private RoleManager roleManager;
    private CooldownManager cooldownManager;
    private AbilityManager abilityManager;
    private HealAbility healAbility;
    private CodyAbility codyAbility;
    private PaixAbility paixAbility;
	private EffectManager effectManager;
    protected Player playerWithSoul;

    public CodyListener() {
        this.playerManager = UhcAPI.getInstance().getPlayerManager();
        this.roleManager = UhcAPI.getInstance().getRoleManager();
        this.cooldownManager = UhcAPI.getInstance().getCooldownManager();
        this.abilityManager = UhcAPI.getInstance().getAbilityManager();
        this.effectManager = UhcAPI.getInstance().getEffectManager();

        // Enregistrer les abilities pour le rôle "Cody"
        UhcRole codyRole = roleManager.getUhcRole("Cody"); // Assurez-vous que le rôle "Cody" existe
        if (codyRole != null) {
            this.healAbility = new HealAbility();
            this.codyAbility = new CodyAbility();
            this.paixAbility = new PaixAbility();
            
            List<Ability> abilities = Arrays.asList(
            	healAbility, codyAbility, paixAbility
            );

            abilityManager.registerAbility(codyRole, abilities);
        }
    }

    @EventHandler
    private void OnRoleGive(RoleGiveEvent event) {
        UhcPlayer uhcPlayer = roleManager.getPlayerWithRole("Cody");
        if(uhcPlayer != null) {
            Player player = uhcPlayer.getPlayer();
            // Items pour Cody
            ItemStack codyItem = new ItemStack(Material.NETHER_STAR);
            ItemMeta codyMeta = codyItem.getItemMeta(); // Obtenir l'ItemMeta
            if (codyMeta != null) {
                codyMeta.setDisplayName(ChatColor.GREEN + "Ame de Gentillesse");
                codyItem.setItemMeta(codyMeta); // Appliquer l'ItemMeta à l'ItemStack
            }
            player.getInventory().addItem(codyItem);

            // Ajouter les potions jetables
            ItemStack weaknessPotion = new ItemStack(Material.POTION, 1, (short) 16386); // Potion de Weakness
            ItemStack poisonPotion = new ItemStack(Material.POTION, 1, (short) 16388); // Potion de Poison
            ItemStack healingPotion = new ItemStack(Material.POTION, 2, (short) 16373); // Potion de Soin (Instant Health
            player.getInventory().addItem(weaknessPotion);
            player.getInventory().addItem(poisonPotion);
            player.getInventory().addItem(healingPotion);
        }
    }
    
    @EventHandler
    private void OnCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String[] args = event.getMessage().split(" ");

        // Commande pour /ut heal
        if (args.length == 3 && args[0].equalsIgnoreCase("/ut") && args[1].equalsIgnoreCase("heal") && isCody(playerManager.getPlayer(player))) {
            if (!cooldownManager.isInCooldown(player, healAbility)) {
                Player aimedPlayer = Bukkit.getPlayer(args[2]);
                if (aimedPlayer != null) {
                    healAbility.activate(aimedPlayer); // Utiliser la méthode activate de HealAbility
                    player.sendMessage("Vous avez soigné " + aimedPlayer.getName() + " de 4 PV.");

                    // Démarrer le cooldown
                    cooldownManager.startCooldown(player, healAbility);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.sendMessage("Cooldown de /ut heal fini.");
                        }
                    }.runTaskLater(UndertaleUHC.getInstance(), 20 * 60 * 20);
                } else {
                    player.sendMessage(ChatColor.RED + "Le joueur ciblé n'est pas en ligne.");
                }
            } else {
                double remaining = cooldownManager.getRemainingCooldown(player, healAbility);
                player.sendMessage(ChatColor.RED + "Vous devez attendre " + (remaining / 1000) + " secondes avant de réutiliser cette commande.");
            }
        }

        // Commande pour /ut cody
        if (args.length == 3 && args[0].equalsIgnoreCase("/ut") && args[1].equalsIgnoreCase("cody") && isCody(playerManager.getPlayer(player))) {
            if (!cooldownManager.isInCooldown(player, codyAbility)) {
                Player aimedPlayer = Bukkit.getPlayer(args[2]);
                if (aimedPlayer != null) {
                    codyAbility.activate(aimedPlayer); // Utiliser la méthode activate de CodyAbility
                    player.sendMessage("Vous avez donné une résistance aux dégâts à " + aimedPlayer.getName() + ".");

                    // Démarrer le cooldown
                    cooldownManager.startCooldown(player, codyAbility);
                    effectManager.setResistance(aimedPlayer, 5);
                } else {
                    player.sendMessage(ChatColor.RED + "Le joueur ciblé n'est pas en ligne.");
                }
            } else {
                double remaining = cooldownManager.getRemainingCooldown(player, codyAbility);
                player.sendMessage(ChatColor.RED + "Vous devez attendre " + (remaining / 1000) + " secondes avant de réutiliser cette commande.");
            }
        }
    }
    
    @EventHandler
    private void OnMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (isCody(playerManager.getPlayer(player))) {
            for (Entity nearbyEntity : player.getNearbyEntities(5, 5, 5)) {
                if (nearbyEntity instanceof Player) {
                    Player nearbyPlayer = (Player) nearbyEntity;
                    if (!roleManager.getCamp(playerManager.getPlayer(player)).getName().equals("Humain")) {
                            paixAbility.activate(nearbyPlayer); // Utiliser la méthode activate de PaixAbility
                            cooldownManager.startCooldown(player, paixAbility);
                    }
                }
            }
        }
        // Vérifier si le joueur a l'item de l'âme
        if (player.getInventory().contains(getSoulItem())) {
            player.setMaxHealth(24);
            playerWithSoul = player; // Mettre à jour le joueur avec l'âme
        } else if (playerWithSoul == player) {
            player.setMaxHealth(20);
        }
    }
    
    @EventHandler
    private void OnDeath(UhcDeathEvent event) {
        Player victim = event.getPlayer();
        Player killer = event.getKiller();
        if (killer != null && victim.getInventory().contains(getSoulItem())) {
            List<ItemStack> drops = event.getDrops();
            drops.remove(getSoulItem());
            killer.getInventory().addItem(getSoulItem());
        }
    }
    
    private ItemStack getSoulItem() {
        ItemStack soul = new ItemStack(Material.NETHER_STAR);
        ItemMeta soul_meta = soul.getItemMeta();
        soul_meta.setDisplayName(ChatColor.GREEN + "Ame de Gentillesse");
        soul.setItemMeta(soul_meta);
        return soul;
    }
    
    private boolean isCody(UhcPlayer uhc_player) {
        return uhc_player != null && uhc_player.getRole() != null && uhc_player.getRole().getName().equals("Cody");
    }
}