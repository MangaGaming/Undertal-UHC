package com.mguhc.undertale.roles.humain.elie;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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

import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;

public class ElieListener implements Listener {
	
	private RoleManager roleManager;
	private PlayerManager playerManager;
	private CooldownManager cooldownManager;
	private AbilityManager abilityManager;
	private RevelationAbility revelationability;
	private VisionAbility visionability;
	private Player playerWhoHasSoulPower;
	private Set<Player> playersWhoUsedSoulPower = new HashSet<>();
    private final Map<Player, Location> lastLocation = new HashMap<>();
    private final Map<Player, Long> immobileTime = new HashMap<>();
    private Map<Player, Integer> timers = new HashMap<>();

	public ElieListener() {
		this.roleManager = UhcAPI.getInstance().getRoleManager();
		this.playerManager = UhcAPI.getInstance().getPlayerManager();
		this.cooldownManager = UhcAPI.getInstance().getCooldownManager();
		this.abilityManager = UhcAPI.getInstance().getAbilityManager();
		
		this.revelationability = new RevelationAbility();
		this.visionability = new VisionAbility();
		
		List<Ability> abilities = Arrays.asList(revelationability, visionability);
		abilityManager.registerAbility(roleManager.getUhcRole("Elie"), abilities);
		
		startImmobilityCheck();
		
		new BukkitRunnable() {
		    @Override
		    public void run() {
		        if (UhcAPI.getInstance().getUhcGame().getTimePassed() == 40 * 60) {
		            UhcPlayer uhc_elie = roleManager.getPlayerWithRole("Elie");
		            if (uhc_elie == null) {
		                this.cancel();
		            } else {
		                Player elie = uhc_elie.getPlayer();

		                // Récupérer les joueurs dans le camp humain
		                List<UhcPlayer> humanPlayers = roleManager.getPlayersInCamp("Humain");
		                StringBuilder message = new StringBuilder();
		                message.append("Joueurs dans le camp humain :\n");

		                // Ajouter chaque joueur à la liste
		                for (UhcPlayer uhcPlayer : humanPlayers) {
		                    message.append("- ").append(uhcPlayer.getPlayer().getName()).append("\n");
		                }

		                // Envoyer le message à Elie
		                elie.sendMessage(message.toString());

		                this.cancel();
		            }
		        }
		    }
		}.runTaskTimer(UndertaleUHC.getInstance(), 0, 3 * 20);
	}
	
	private void startImmobilityCheck() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<Player, UhcPlayer> entry : playerManager.getPlayers().entrySet()) {
                	Player player = entry.getKey();
                    Location currentLocation = player.getLocation();
                    if (lastLocation.containsKey(player)) {
                        Location previousLocation = lastLocation.get(player);

                        // Vérifier si le joueur est immobile
                        if (currentLocation.getX() == previousLocation.getX() &&
                            currentLocation.getY() == previousLocation.getY() &&
                            currentLocation.getZ() == previousLocation.getZ()) {
                            
                            // Incrémente le temps immobile
                            long currentTime = System.currentTimeMillis();
                            immobileTime.put(player, immobileTime.getOrDefault(player, currentTime));

                            // Vérifier si le joueur est immobile depuis 10 secondes
                            if (currentTime - immobileTime.get(player) >= 10000) { // 10 secondes
                            	double health = player.getHealth();
                            	if(health < 20) {
                            		player.setHealth(player.getHealth() + 0.5);
                            	}
                                List<Entity> nearbyEntities = player.getNearbyEntities(5, 5, 5);
                                boolean hasNearbyPlayers = false;

                                for (Entity entity : nearbyEntities) {
                                    if (entity instanceof Player) {
                                        hasNearbyPlayers = true;
                                        break; // On a trouvé au moins un joueur à proximité
                                    }
                                }
                                if(hasNearbyPlayers &&
                                	isElie(playerManager.getPlayer(player)))  {
                                	if(cooldownManager.getRemainingCooldown(player, revelationability) == 0) {
                                    	cooldownManager.startCooldown(player, revelationability);
                                    	openPlayerListInventory(player);
                                	}
                                	else {
                                		player.sendMessage("Vous êtes en cooldown pour " + cooldownManager.getRemainingCooldown(player, revelationability) / 1000 + " secondes");
                                	}
                                }
                            }
                        } else {
                            // Réinitialiser le temps immobile si le joueur a bougé
                            immobileTime.remove(player);
                        }
                    }

                    // Mettre à jour la dernière position
                    lastLocation.put(player, currentLocation);
                }
            }
        }.runTaskTimer(UndertaleUHC.getInstance(), 0, 20); // Vérifie toutes les secondes
    }

	@EventHandler
	private void OnMove(PlayerMoveEvent event) {
	    if (UhcAPI.getInstance().getUhcGame().getCurrentPhase().getName().equals("Playing")) {
	        Player player = event.getPlayer();
	        immobileTime.remove(player);

	        if (player.getInventory().contains(getSoulItem())) {
	            // Vérifier si le joueur a déjà utilisé le pouvoir de l'âme
	            if (!playersWhoUsedSoulPower.contains(player)) {
	                playerWhoHasSoulPower = player;
	                UhcRole role = roleManager.getRole(playerManager.getPlayer(player));
	                if (role == null) {
	                    player.sendMessage("Erreur : votre rôle est introuvable.");
	                    return; // Sortir de la méthode si le rôle est null
	                }

	                List<Ability> abilities = abilityManager.getAbilitys(role);
	                if (abilities != null) {

		                for (Ability ability : abilities) {
		                    ability.setCooldownDuration((long) (ability.getCooldownDuration() * 0.8));
		                    player.sendMessage("Le cooldown de l'" + ability.getClass().getName() + " a été réduit et fait maintenant : " + ability.getCooldownDuration());
		                }

		                // Marquer le pouvoir de l'âme comme utilisé
		                playersWhoUsedSoulPower.add(player);
	                }
	            }
	        } else if (playerWhoHasSoulPower == player) {
	            UhcRole role = roleManager.getRole(playerManager.getPlayer(player));
	            if (role != null) {
	                List<Ability> abilities = abilityManager.getAbilitys(role);
	                if (abilities != null) {
	                    for (Ability ability : abilities) {
	                        ability.setCooldownDuration((long) (ability.getCooldownDuration() / 0.8));
	                        player.sendMessage("Le cooldown de l'" + ability.getClass().getName() + " a été augmenté et fait maintenant : " + ability.getCooldownDuration());
	                    }
	                }
	            }
	            playerWhoHasSoulPower = null;
	        }

	        if (isElie(playerManager.getPlayer(player ))) {
	            if (!cooldownManager.isInCooldown(player, visionability)) {
	                // Démarrer le cooldown pour la capacité de vision
	                cooldownManager.startCooldown(player, visionability);
	                
	                // Afficher les informations sur le biome
	                displayBiomeInTab(player);
	                
	                // Utiliser un BukkitRunnable pour masquer le biome après 15 secondes
	                new BukkitRunnable() {
	                    @Override
	                    public void run() {
	                        hideBiomeInTab(player);
	                    }
	                }.runTaskLater(UndertaleUHC.getInstance(), 15 * 20); // 15 secondes en ticks (1 seconde = 20 ticks)
	            }
	        }
	    }
	}
    
    public void displayBiomeInTab(Player viewer) {
        for (Player target : Bukkit.getOnlinePlayers()) {
            // Obtenir le biome actuel du joueur cible
            Location location = target.getLocation();
            String biomeName = location.getWorld().getBiome(location.getBlockX(), location.getBlockZ()).toString().replace("_", " ");

            // Créer le nom coloré avec le biome
            String coloredName = ChatColor.GREEN + target.getName() + " - " + ChatColor.YELLOW + biomeName;

            // Obtenir l'entité du joueur cible
            CraftPlayer craftTarget = (CraftPlayer) target;
            EntityPlayer entityPlayer = craftTarget.getHandle();

            // Mettre à jour le nom du joueur avec le biome
            entityPlayer.listName = net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + coloredName + "\"}");

            // Packet pour retirer le joueur de la liste du tab
            PacketPlayOutPlayerInfo removePacket = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, entityPlayer);

            // Packet pour ajouter le joueur avec le nom coloré
            PacketPlayOutPlayerInfo addPacket = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, entityPlayer);

            // Envoyer le packet au joueur qui a utilisé la capacité
            ((CraftPlayer) viewer).getHandle().playerConnection.sendPacket(removePacket);
            ((CraftPlayer) viewer).getHandle().playerConnection.sendPacket(addPacket);
        }
    }

    public void hideBiomeInTab(Player viewer) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            // Retirer le joueur de la liste du tab pour tous les autres joueurs
            if (!onlinePlayer.equals(viewer)) {
                CraftPlayer craftViewer = (CraftPlayer) viewer;
                EntityPlayer entityViewer = craftViewer.getHandle();
                PacketPlayOutPlayerInfo removePacket = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, entityViewer);
                ((CraftPlayer) onlinePlayer).getHandle().playerConnection.sendPacket(removePacket);
            }
        }

        // Réinitialiser le nom du joueur pour le viewer
        CraftPlayer craftViewer = (CraftPlayer) viewer;
        EntityPlayer entityViewer = craftViewer.getHandle();
        entityViewer.listName = net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + viewer.getName() + "\"}");

        // Ajouter le joueur avec son nom d'origine pour le viewer
        PacketPlayOutPlayerInfo addPacket = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, entityViewer);
        ((CraftPlayer) viewer).getHandle().playerConnection.sendPacket(addPacket);
    }
    
    // Méthode pour marquer le pouvoir de l'âme comme utilisé
    public void markSoulPowerUsed(Player player) {
        playersWhoUsedSoulPower.add(player);
    }
	
	private void openPlayerListInventory(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, "Joueurs à Proximité");
        
        // Récupérer tous les joueurs en ligne
        for (Entity entity : player.getNearbyEntities(15, 15, 15)) {
        	if(entity instanceof Player) {
        		Player nearbyPlayer = (Player) entity;
                // Créer un item pour chaque joueur
                ItemStack playerItem = new ItemStack(Material.SKULL_ITEM, 1, (short) 3); // Utiliser une tête de joueur
                ItemMeta meta = playerItem.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(nearbyPlayer.getName()); // Nom du joueur
                    playerItem.setItemMeta(meta);
                }
                // Ajouter l'item à l'inventaire
                inventory.addItem(playerItem);
            }
        }
        // Ouvrir l'inventaire pour le joueur
        player.openInventory(inventory);
    }

    @EventHandler
    private void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("Joueurs à Proximité")) {
            event.setCancelled(true); // Empêche de retirer des items de l'inventaire

            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem != null && clickedItem.getType() == Material.SKULL_ITEM) {
                Player clickedPlayer = Bukkit.getPlayer(clickedItem.getItemMeta().getDisplayName());
                if(clickedPlayer != null &&
                	playerManager.getPlayer(clickedPlayer).getRole() != null) {
                	String roleName = playerManager.getPlayer(clickedPlayer).getRole().getName();
                	String campName = roleManager.getCamp(playerManager.getPlayer(clickedPlayer)).getName();
                	if(roleName.equals("W.D Gaster")) {
                		player.sendMessage(ChatColor.RED + "Une intention meurtrière dégage de son ame");
                	}
                	else if(roleName.equals("Betty")) {
                		player.sendMessage(ChatColor.GOLD + "La creature rode");
                	}
                	else if(roleName.equals("Player")) {
                		player.sendMessage(ChatColor.GREEN + "Un humain se cache dans son ame");
                	}
                	else if(campName.equals("Humain")) {
                		player.sendMessage(ChatColor.GREEN + "Un humain se cache dans son ame");
                	}
                	else if(campName.equals("Monstre")) {
                		player.sendMessage(ChatColor.RED + "Une intention meurtrière dégage de son ame");
                	}
                }
            }
        }
    }
    
    @EventHandler
    private void OnDeath(PlayerDeathEvent event) {
    	Player victim = event.getEntity();
    	Player killer = event.getEntity().getKiller();
    	if(isElie(playerManager.getPlayer(victim))) {
    		if(killer != null) {
    			startTimer(killer);
    		}
    	}
    }

    private void startTimer(Player player) {
        timers.put(player, 5); // Initialiser le timer à 5 minutes

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                int timer = timers.get(player); // Récupérer le timer pour le joueur
                if (timer > 0) {
                    // Informer le joueur du temps restant
                    player.sendMessage("Temps restant : " + timer + " minutes");
                    timers.put(player, timer - 1); // Décrémenter le timer
                } else {
                    // Accorder l'âme
                    player.getInventory().addItem(getSoulItem());
                    timers.remove(player); // Supprimer le joueur du map
                    cancel();
                }
            }
        };
        task.runTaskTimer(UndertaleUHC.getInstance(), 0, 1200); // 1200 ticks = 1 minute
    }

    private int getCurrentTimer(Player player) {
        return timers.getOrDefault(player, 0); // Retourne 0 si le joueur n'a pas de timer
    }

    private boolean isInTimer(Player player) {
        return timers.containsKey(player); // Retourne true si le joueur a un timer
    }

    private void updateTimer(Player player, int newTimer) {
        timers.put(player, newTimer); // Met à jour le timer pour le joueur
    }

    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            // Vérifier si le joueur est dans le timer
            if (isInTimer(attacker)) {
                // Augmenter le timer d'une minute
                increaseTimer(attacker);
                attacker.sendMessage("Vous avez frappé quelqu'un ! Timer augmenté.");
            }
        }
    }

    private void increaseTimer(Player player) {
        int currentTimer = getCurrentTimer(player);
        if (currentTimer < 20) {
            currentTimer++;
            player.sendMessage("Nouveau timer : " + currentTimer + " minutes");
            updateTimer(player, currentTimer); // Mettre à jour le timer pour le joueur
        }
    }

	private ItemStack getSoulItem() {
        // Items pour Elie
        ItemStack soul = new ItemStack(Material.NETHER_STAR);
        ItemMeta soul_meta = soul.getItemMeta(); // Obtenir l'ItemMeta
        if (soul_meta != null) {
        	soul_meta.setDisplayName(ChatColor.BLUE + "Ame de Patience");
            soul.setItemMeta(soul_meta); // Appliquer l'ItemMeta à l'ItemStack
        }
        return soul;
	}
	

    private boolean isElie(UhcPlayer uhc_player) {
        return uhc_player.getRole() != null && uhc_player.getRole().getName().equals("Elie");
    }
}
