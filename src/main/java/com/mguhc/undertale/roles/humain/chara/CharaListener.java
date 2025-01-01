package com.mguhc.undertale.roles.humain.chara;

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
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;

public class CharaListener implements Listener {

    private PecherAbility pecherAbility;
    private RageAbility rageAbility;
    private EffectManager effectManager;
    private AbilityManager abilityManager;
    private CooldownManager cooldownManager;
    private RoleManager roleManager;
    private PlayerManager playerManager;
    private boolean canUseRoad = true;

    public CharaListener() {
        UhcAPI api = UhcAPI.getInstance();
        this.playerManager = api.getPlayerManager();
        this.roleManager = api.getRoleManager();
        this.cooldownManager = api.getCooldownManager();
        this.abilityManager = api.getAbilityManager();
        this.effectManager = UhcAPI.getInstance().getEffectManager();

        UhcRole charaRole = roleManager.getUhcRole("Chara");
        if(charaRole != null) {
            this.rageAbility = new RageAbility();
            this.pecherAbility = new PecherAbility();
            List<Ability> abiilities = Arrays.asList(rageAbility, pecherAbility);
            abilityManager.registerAbility(charaRole, abiilities);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                UhcPlayer uhc_player = roleManager.getPlayerWithRole("Chara");
                if (uhc_player != null) {
                    long time = uhc_player.getPlayer().getWorld().getTime(); // Obtenir le temps actuel dans le monde
                    if (time >= 0 && time < 12000) { // Vérifier si c'est le jour
                        effectManager.setResistance(uhc_player.getPlayer(), 20);
                    } else {
                        effectManager.removeEffect(uhc_player.getPlayer(), PotionEffectType.DAMAGE_RESISTANCE);
                    }
                }
            }
        }.runTaskTimer(UndertaleUHC.getInstance(), 0, 3 * 20); // Vérifie toutes les secondes
    }

    @EventHandler
    private void OnDamage(EntityDamageByEntityEvent event) {
        if(event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player victim = (Player) event.getEntity();
            Player damager = (Player) event.getDamager();
            if(isChara(playerManager.getPlayer(damager))) {
                if(cooldownManager.isInCooldown(damager, rageAbility)) {
                    cooldownManager.startCooldown(damager, rageAbility);
                    event.setDamage(event.getDamage() + 4);
                    damager.sendMessage("Vous avez utiliser votre Rage Genocidaire");
                }
            }
        }
    }

    @EventHandler
    private void OnCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String[] args = event.getMessage().split(" ");
        if(args.length == 2 && args[0].equals("/ut") && args[1].equals("road")) {
            if(isChara(playerManager.getPlayer(player)) && canUseRoad) {
                openRoadInventory(player);
            }
        }
    }

    private void openRoadInventory(Player player) {
        // Créer un inventaire avec un titre et une taille de 9 slots
        Inventory inventory = Bukkit.createInventory(null, 9, ChatColor.DARK_GRAY + "Voulez vous trahir les humains");

        // Créer la laine verte pour "Oui"
        ItemStack yesWool = new ItemStack(Material.WOOL, 1, (short) 5); // 5 est l'ID de la laine verte
        ItemMeta yesWoolMeta = yesWool.getItemMeta();
        if (yesWoolMeta != null) {
            yesWoolMeta.setDisplayName(ChatColor.GREEN + "Oui");
            yesWool.setItemMeta(yesWoolMeta);
        }

        // Créer la laine rouge pour "Non"
        ItemStack noWool = new ItemStack(Material.WOOL, 1, (short) 14); // 14 est l'ID de la laine rouge
        ItemMeta noWoolMeta = noWool.getItemMeta();
        if (noWoolMeta != null) {
            noWoolMeta.setDisplayName(ChatColor.RED + "Non");
            noWool.setItemMeta(noWoolMeta);
        }

        // Ajouter les objets à l'inventaire
        inventory.setItem(3, yesWool); // Oui
        inventory.setItem(5, noWool);   // Non

        // Ouvrir l'inventaire pour le joueur
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Vérifier si l'inventaire est celui que nous avons créé
        if (event.getView().getTitle().equals(ChatColor.DARK_GRAY + "Voulez vous trahir les humains")) {
            event.setCancelled(true); // Annuler l'événement pour éviter de déplacer les objets

            // Vérifier quel item a été cliqué
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.hasItemMeta()) {
                Player player = (Player) event.getWhoClicked();
                String choose = clickedItem.getItemMeta().getDisplayName();
                if(choose.equals(ChatColor.GREEN + "Oui")) {
                    canUseRoad = false;
                    player.sendMessage(ChatColor.RED + "Vous avez choisi de trahir les humains");
                    roleManager.setCamp(playerManager.getPlayer(player), roleManager.getCamps().get(1));
                    effectManager.setStrength(player, 20);
                    effectManager.setSpeed(player, 120);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            effectManager.removeEffect(player, PotionEffectType.INCREASE_DAMAGE);
                            effectManager.removeEffect(player, PotionEffectType.SPEED);
                            player.sendMessage("Vos avantages de trahison sont finis");
                        }
                    }.runTaskLater(UndertaleUHC.getInstance(), 5*60*20);
                }
                if(choose.equals(ChatColor.RED + "Non")) {
                    canUseRoad = false;
                    player.sendMessage(ChatColor.GREEN + "Vous avez choisi de rester avec les humains");
                }
            }
        }
    }

    private boolean isChara(UhcPlayer player) {
        UhcRole role = player.getRole();
        return role != null && role.getName().equals("Chara");
    }
}
