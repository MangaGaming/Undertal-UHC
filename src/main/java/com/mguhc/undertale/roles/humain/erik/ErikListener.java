package com.mguhc.undertale.roles.humain.erik;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.mguhc.effect.EffectManager;
import com.mguhc.events.RoleGiveEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.mguhc.UhcAPI;
import com.mguhc.ability.Ability;
import com.mguhc.ability.AbilityManager;
import com.mguhc.ability.CooldownManager;
import com.mguhc.player.PlayerManager;
import com.mguhc.player.UhcPlayer;
import com.mguhc.roles.RoleManager;
import com.mguhc.roles.UhcRole;
import com.mguhc.undertale.UndertaleUHC;

public class ErikListener implements Listener {

    private final EffectManager effectManager;
    private RoleManager roleManager;
    private PlayerManager playerManager;
	private AbilityManager abilityManager;
	private CooldownManager cooldownManager;
    private int fightCount;
    private boolean hasBattleStart = false;

    // Variables pour stocker les anciennes positions
    private Map<Player, Location> previousLocations = new HashMap<>();
    private Map<Player, Boolean> hasFoughtErik = new HashMap<>(); // Pour suivre si un joueur a déjà combattu Erik
    private Map<Player, ItemStack[]> ancientInventories = new HashMap<>();
    private Map<Player, ItemStack[]> ancientArmors = new HashMap<>();
	private ChargeAudacieuseAbility chargeAudacieuse;

    public ErikListener() {
        this.roleManager = UhcAPI.getInstance().getRoleManager();
        this.playerManager = UhcAPI.getInstance().getPlayerManager();
        this.abilityManager = UhcAPI.getInstance().getAbilityManager();
        this.cooldownManager = UhcAPI.getInstance().getCooldownManager();
        this.effectManager = UhcAPI.getInstance().getEffectManager();

        // Enregistrer la capacité "Charge Audacieuse" pour le rôle "Erik"
        UhcRole erikRole = roleManager.getUhcRole("Erik"); // Assurez-vous que le rôle "Erik" existe
        this.chargeAudacieuse = new ChargeAudacieuseAbility();
        List<Ability> abilities = Arrays.asList(chargeAudacieuse);
        abilityManager.registerAbility(erikRole, abilities); // Enregistrer l'ability
    }

    @EventHandler
    private void OnRoleGive(RoleGiveEvent event) {
        UhcPlayer uhcPlayer = roleManager.getPlayerWithRole("Erik");
        if(uhcPlayer != null) {
            Player player = uhcPlayer.getPlayer();

            ItemStack erikItem1 = new ItemStack(Material.NETHER_STAR);
            ItemMeta erikMeta1 = erikItem1.getItemMeta(); // Obtenir l'ItemMeta
            if (erikMeta1 != null) {
                erikMeta1.setDisplayName(ChatColor.GOLD + "Charge Audacieuse");
                erikItem1.setItemMeta(erikMeta1); // Appliquer l'ItemMeta à l'ItemStack
            }
            player.getInventory().addItem(erikItem1);

            ItemStack erikItem2 = new ItemStack(Material.NETHER_STAR);
            ItemMeta erikMeta2 = erikItem2.getItemMeta(); // Obtenir l'ItemMeta
            if (erikMeta2 != null) {
                erikMeta2.setDisplayName(ChatColor.GOLD + "Ame de Bravoure");
                erikItem2.setItemMeta(erikMeta2); // Appliquer l'ItemMeta à l'ItemStack
            }
            player.getInventory().addItem(erikItem2);

            // Démarrer un Runnable pour vérifier le temps régulièrement
            new BukkitRunnable() {
                @Override
                public void run() {
                    UhcPlayer uhc_player = roleManager.getPlayerWithRole("Erik");
                    if (uhc_player != null) {
                        long time = uhc_player.getPlayer().getWorld().getTime(); // Obtenir le temps actuel dans le monde
                        if (time >= 0 && time < 12000) { // Vérifier si c'est le jour
                            effectManager.setStrength(uhc_player.getPlayer(), 20);
                        } else {
                            effectManager.removeEffect(uhc_player.getPlayer(), PotionEffectType.INCREASE_DAMAGE);
                        }
                    }
                }
            }.runTaskTimer(UndertaleUHC.getInstance(), 0, 3 * 20); // Vérifie toutes les secondes

            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Map.Entry<Player, UhcPlayer> entry : playerManager.getPlayers().entrySet()) {
                        Player player = entry.getKey();
                        if (player.getInventory().contains(getSoulItem())) {
                            if (player.hasPotionEffect(PotionEffectType.WEAKNESS)) {
                                player.removePotionEffect(PotionEffectType.WEAKNESS);
                            }

                            if (player.hasPotionEffect(PotionEffectType.SLOW)) {
                                player.removePotionEffect(PotionEffectType.SLOW);
                            }

                            if (player.hasPotionEffect(PotionEffectType.POISON)) {
                                player.removePotionEffect(PotionEffectType.POISON);
                            }
                        }
                    }
                }
            }.runTaskTimer(UndertaleUHC.getInstance(), 0, 3 * 20);
        }
    }

    @EventHandler
    private void OnDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player victim = (Player) event.getEntity();
            Player damager = (Player) event.getDamager();

            // Vérifiez si la victime est Erik
            if (isErik(playerManager.getPlayer(victim))) {
                // Vérifiez si le joueur a déjà combattu Erik
                if (! hasFoughtErik.getOrDefault(damager, false)) {
                    // Vérifiez si la santé de la victime est tombée à 0 ou moins après les dégâts
                    if (victim.getHealth() - event.getFinalDamage() <= 0) {
                        damager.sendMessage("Vous avez tué " + victim.getName());

                        // Annuler l'événement de dégâts
                        event.setCancelled(true);
                        Location battleLocation = new Location(victim.getWorld(), 200, 200, 200);

                        // Stocker l'ancienne position
                        previousLocations.put(damager, damager.getLocation());
                        previousLocations.put(victim, victim.getLocation());

                        damager.teleport(battleLocation);
                        victim.teleport(battleLocation);
                        victim.setHealth(victim.getMaxHealth());
                        damager.setHealth(damager.getMaxHealth());

                        // Marquer que le joueur a combattu Erik
                        hasFoughtErik.put(damager, true);

                        // Commencer le combat
                        startBattle(damager, victim);
                    }

                    // Augmenter les dégâts si la santé de la victime est faible
                    if (victim.getHealth() <= 4 && fightCount <= 2) {
                        victim.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 5 * 20, 1));
                        fightCount++;
                    }
                }
            }

            // Vérifiez si le combat a commencé
            if (hasBattleStart) {
                // Vérifiez si la santé du damager est tombée à 0 ou moins après les dégâts
                if (victim.getHealth() - event.getFinalDamage() <= 0) {
                	if(isErik(playerManager.getPlayer(victim))) {
                        // Erik a perdu
                        event.setCancelled(true); // Annuler l'événement de dégâts
                        damager.sendMessage("Vous avez gagné");
                        victim.sendMessage("Vous avez perdu");
                        handleBattleOutcome(true, victim, damager);
                	}
                	else if(isErik(playerManager.getPlayer(damager))) {
                		// Erik a gagné
                        event.setCancelled(true); // Annuler l'événement de dégâts
                        damager.sendMessage("Vous avez gagné");
                        victim.sendMessage("Vous avez perdu");
                        handleBattleOutcome(false, victim, damager);
                	}
                }
            }
        }
    }

    private void startBattle(Player damager, Player victim) {
        hasBattleStart = true;
        // Équiper les joueurs en armure de fer et épée en fer
        equipPlayersForBattle(victim);
        equipPlayersForBattle(damager);
    }

    private void equipPlayersForBattle(Player player) {
        // Stocker l'inventaire du joueur avant de le vider
        ancientInventories.put(player, player.getInventory().getContents().clone());
        ancientArmors.put(player, player.getInventory().getArmorContents().clone());
        
        player.getInventory().clear();
        
        // Équiper le joueur avec une armure de fer et une épée en fer
        player.getInventory().setArmorContents(new ItemStack[] {
            new ItemStack(Material.IRON_HELMET),
            new ItemStack(Material.IRON_CHESTPLATE),
            new ItemStack(Material.IRON_LEGGINGS),
            new ItemStack(Material.IRON_BOOTS)
        });
        player.getInventory().addItem(new ItemStack(Material.IRON_SWORD));
    }

    private void handleBattleOutcome(Boolean win, Player victim, Player damager) {
        hasBattleStart = false;
        Player erik = roleManager.getPlayerWithRole("Erik").getPlayer();
        if (win) {
        	damager.getInventory().addItem(getSoulItem());
        	damager.sendMessage("Vous avez gagné le combat !");
            
            // Restaurer l'inventaire du winner
        	damager.getInventory().setContents(ancientInventories.get(damager));
        	damager.getInventory().setArmorContents(ancientArmors.get(damager));
            
            // Téléporter le winner à son ancienne position
            if (previousLocations.containsKey(damager)) {
            	damager.teleport(previousLocations.get(damager));
                previousLocations.remove(damager); // Supprimer l'ancienne position après utilisation
            }
            
            erik.setHealth(0);
        } else {
            if (previousLocations.containsKey(victim)) {
                // TP le looser à son ancienne location
            	victim.teleport(previousLocations.get(victim));
                previousLocations.remove(victim); // Supprimer l'ancienne position après utilisation
            }
            
            // Restaurer l'inventaire et l'armure du looser
            victim.getInventory().setContents(ancientInventories.get(victim));
            victim.getInventory().setArmorContents(ancientArmors.get(victim));
            victim.setMaxHealth(victim.getMaxHealth() - 2);
            
            erik.setHealth(0);
            
            new BukkitRunnable() {
				@Override
				public void run() {
		            IronGolem golem = (IronGolem) victim.getWorld().spawnEntity(victim.getLocation(), EntityType.IRON_GOLEM);
		            victim.sendMessage("Le fantome de bravoure est apparu");
		            golem.setCustomName("Fantôme de Bravoure");
		            golem.setMaxHealth(400.0); // 400 PV

		            // Attendre que le golem soit tué
		            golem.setMetadata("attacker", new FixedMetadataValue(UndertaleUHC.getInstance(), victim.getUniqueId()));
		            Bukkit.getPluginManager().registerEvents(new Listener() {
		            	@EventHandler
		            	public void onGolemDeath(EntityDeathEvent e) {
		            	    if (e.getEntity().getMetadata("attacker").size() > 0) { // Vérifiez si la liste n'est pas vide
		            	        if (e.getEntity().getMetadata("attacker").get(0).value() instanceof UUID) {
		            	            UUID attackerUUID = (UUID) e.getEntity().getMetadata("attacker").get(0).value();
		            	            if (attackerUUID.equals(victim.getUniqueId())) {
		            	            	victim.getInventory().addItem(getSoulItem());
		            	                victim.sendMessage("Vous avez tué le Fantôme de Bravoure !");
		            	            }
		            	        }
		            	    }
		            	}
		            }, UndertaleUHC.getInstance());
				}
			}.runTaskLater(UndertaleUHC.getInstance(), 20);
        }
    }

    @EventHandler
    public void OnInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null) {
            return; // Sortir de la méthode si l'item est null
        }

        // Vérifier si l'item est la Charge Audacieuse
        if (item.getItemMeta() != null &&
            item.getItemMeta().hasDisplayName() &&
            item.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Charge Audacieuse") &&
            isErik(playerManager.getPlayer(player))) {

                // Vérifier si le joueur est en cooldown pour cette capacité
                if (!cooldownManager.isInCooldown(player, chargeAudacieuse)) {
                    // Activer la capacité
                	chargeAudacieuse.activate(player);
                } else {
                	double remaining = cooldownManager.getRemainingCooldown(player, chargeAudacieuse);
                    player.sendMessage("Vous devez attendre " + (remaining / 1000) + " secondes avant de réutiliser la Charge Audacieuse.");
                }
            }
        }

    @EventHandler
    private void OnDeath(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer != null && isErik(playerManager.getPlayer(killer))) {
        	event.getDrops().remove(getSoulItem());
            killer.setMaxHealth(killer.getMaxHealth() + 1);
        }
    }

    private boolean isErik(UhcPlayer uhc_player) {
        return uhc_player != null && uhc_player.getRole() != null && uhc_player.getRole().getName().equals("Erik");
    }

    private ItemStack getSoulItem() {
        ItemStack soul = new ItemStack(Material.NETHER_STAR);
        ItemMeta soul_meta = soul.getItemMeta(); // Obtenir l'ItemMeta
        if (soul_meta != null) {
            soul_meta.setDisplayName(ChatColor.GOLD + "Ame de Bravoure");
            soul.setItemMeta(soul_meta); // Appliquer l'ItemMeta à l'ItemStack
        }
        return soul;
    }
}