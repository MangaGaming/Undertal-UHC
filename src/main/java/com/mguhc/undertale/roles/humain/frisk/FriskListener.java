package com.mguhc.undertale.roles.humain.frisk;

import com.mguhc.UhcAPI;
import com.mguhc.ability.Ability;
import com.mguhc.ability.AbilityManager;
import com.mguhc.ability.CooldownManager;
import com.mguhc.effect.EffectManager;
import com.mguhc.player.PlayerManager;
import com.mguhc.player.UhcPlayer;
import com.mguhc.roles.Camp;
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
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class FriskListener implements Listener {

	private EffectManager effectManager;
	private SaveAbility saveAbility;
	private ActAbility actAbility;
	private PlayerManager playerManager;
	private RoleManager roleManager;
	private CooldownManager cooldownManager;
	private AbilityManager abilityManager;

	private final List<String> possibleObjectives = Arrays.asList(
			"Manger 2 pommes d'or",
			"Donner 10 coups d'épée",
			"Faire un kill",
			"Casser 64 blocs de pierre",
			"Prendre un dégât de chute"
	);

	private Map<UUID, List<String>> playerObjectives = new HashMap<>();
	private Map<UUID, Integer> playerDetermination = new HashMap<>();
	private Map<UUID, Boolean> playerCanUseMercy = new HashMap<>();
	private int gapleEaten = 0;
	private int hitHeaten = 0;
	private int blockBroken = 0;
	private boolean hasUsedMercy = false;
	private Player duo;
	private boolean canUseSoulPower = true;

	public FriskListener() {
		UhcAPI api = UhcAPI.getInstance();
		this.playerManager = api.getPlayerManager();
		this.roleManager = api.getRoleManager();
		this.cooldownManager = api.getCooldownManager();
		this.abilityManager = api.getAbilityManager();
		this.effectManager = UhcAPI.getInstance().getEffectManager();

		UhcRole friskRole = roleManager.getUhcRole("Frisk");
		if (friskRole != null) {
			this.saveAbility = new SaveAbility();
			this.actAbility = new ActAbility();
			List<Ability> abilities = Arrays.asList(saveAbility, actAbility);
			abilityManager.registerAbility(friskRole, abilities);
		}
	}

	@EventHandler
	private void OnCommand(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		String[] args = event.getMessage().split(" ");

		// Commande pour activer les objectifs
		if (args.length == 2 && args[0].equals("/ut") && args[1].equals("act")) {
			if (isFrisk(playerManager.getPlayer(player))) {
				if(cooldownManager.getRemainingCooldown(player, actAbility) == 0) {
					if (!playerObjectives.containsKey(player.getUniqueId())) {
						// Sélectionner deux objectifs aléatoires
						List<String> objectives = new ArrayList<>(possibleObjectives);
						Collections.shuffle(objectives);
						List<String> selectedObjectives = objectives.subList(0, 2);
						playerObjectives.put(player.getUniqueId(), selectedObjectives);
						player.sendMessage(ChatColor.GREEN + "Vos objectifs sont : " + selectedObjectives);
					} else {
						player.sendMessage(ChatColor.RED + "Vous devez compléter vos objectifs avant d'en obtenir de nouveaux.");
					}
				}
				else {
					player.sendMessage("Vous êtes en cooldown pour " + cooldownManager.getRemainingCooldown(player, actAbility) / 1000 + " secondes");
				}
			}
		}
		if(args.length == 3 && args[0].equals("/ut") && args[1].equals("save")) {
			if(isFrisk(playerManager.getPlayer(player))) {
				Player aimedPlayer = Bukkit.getPlayer(args[2]);
				if(aimedPlayer != null) {
					if(cooldownManager.getRemainingCooldown(player, saveAbility) == 0) {
						cooldownManager.startCooldown(aimedPlayer, saveAbility);
						effectManager.setWeakness(aimedPlayer, effectManager.getEffect(aimedPlayer, PotionEffectType.WEAKNESS) + 25);
						player.sendMessage(ChatColor.GREEN + "Vous avez baissé les dégats de " + aimedPlayer.getName() + " de 25%");
						aimedPlayer.sendMessage(ChatColor.RED + "Vos dégats ont été baissé de 25%");
						new BukkitRunnable() {
							@Override
							public void run() {
								effectManager.setWeakness(aimedPlayer, effectManager.getEffect(aimedPlayer, PotionEffectType.WEAKNESS) - 25);
								player.sendMessage(ChatColor.RED + "Les dégats de " + aimedPlayer.getName() + " sont de nouveau normal");
								aimedPlayer.sendMessage(ChatColor.GREEN + "Vos dégats sont de nouveau normal");
							}
						}.runTaskLater(UndertaleUHC.getInstance(), 5*20);
					}
					else {
						player.sendMessage("Vous êtes en cooldown pour " + cooldownManager.getRemainingCooldown(player, saveAbility) / 1000 + " secondes");
					}
				}
				else {
					player.sendMessage("Le joueur visée n'est pas en ligne");
				}
			}
		}
		if(args.length == 2 && args[0].equals("/ut") && args[1].equals("mercy")) {
			if(playerCanUseMercy.getOrDefault(player.getUniqueId(), true)) {
				Random random = new Random();
				for(Map.Entry<Player, UhcPlayer> entry : playerManager.getPlayers().entrySet()) {
					if(roleManager.getCamp(entry.getValue()).getName().equals("Monstre")) {
						Player chosenPlayer = entry.getKey();
						Camp camp = roleManager.getCamps().get(3);
						roleManager.setCamp(playerManager.getPlayer(chosenPlayer), camp);
						duo = chosenPlayer;
						chosenPlayer.sendMessage("Vous avez été assigner au camp " + camp.getName() + ", vous êtes avec " + player.getName());
						roleManager.setCamp(playerManager.getPlayer(player), camp);
						effectManager.setResistance(player, effectManager.getEffect(player, PotionEffectType.DAMAGE_RESISTANCE) + 20);
						effectManager.setSpeed(player, effectManager.getEffect(player, PotionEffectType.SPEED) + 110);
						effectManager.removeEffect(player, PotionEffectType.WEAKNESS);
						player.sendMessage("Vous avez été assigner au camp " + camp.getName() + ", vous êtes avec " + player.getName() + ". Vous avez gagné vos effets");
						break;
					}
				}
				hasUsedMercy = true;
                playerCanUseMercy.put(player.getUniqueId(), false);
			}
		}
	}

	// Méthode pour vérifier si le joueur a complété ses objectifs
	public void checkObjectivesCompletion(Player player, String completedObjective) {
		UUID playerId = player.getUniqueId();
		if (playerObjectives.containsKey(playerId)) {
			List<String> objectives = playerObjectives.get(playerId);
			objectives.remove(completedObjective);
			player.sendMessage(ChatColor.GREEN + "Vous avez complété l'objectif : " + completedObjective);

			// Vérifier si tous les objectifs sont complétés
			if (objectives.isEmpty()) {
				// Récompense de détermination
				int currentDetermination = playerDetermination.getOrDefault(playerId, 0);
				currentDetermination += 20; // Ajoute 20% de détermination
				playerDetermination.put(playerId, currentDetermination);
				player.sendMessage(ChatColor.GOLD + "Vous avez gagné 20% de détermination !");

				// Vérifier si la détermination atteint 100%
				if (currentDetermination >= 100) {
					playerCanUseMercy.put(playerId, true);
					player.sendMessage(ChatColor.GREEN + "Vous pouvez maintenant utiliser la capacité de Miséricorde !");
				}

				// Supprimer les objectifs du joueur
				playerObjectives.remove(playerId);
			}
		}
	}

	@EventHandler
	public void onPlayerEatGoldenApple(PlayerItemConsumeEvent event) {
		Player player = event.getPlayer();
		if (event.getItem().getType() == Material.GOLDEN_APPLE) {
			UUID playerId = player.getUniqueId();
			List<String> objectives = playerObjectives.get(playerId);
			if(objectives != null && objectives.contains("Manger 2 pommes d'or")) {
				gapleEaten ++;
				if(gapleEaten == 2) {
					gapleEaten = 0;
					checkObjectivesCompletion(player, "Manger 2 pommes d'or");
				}
			}
		}
	}

	@EventHandler
	public void onPlayerDealDamage(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player) {
			Player damager = (Player) event.getDamager();
			UUID playerId = damager.getUniqueId();
			List<String> objectives = playerObjectives.get(playerId);
			if(objectives != null && objectives.contains("Donner 10 coups d'épée")) {
				hitHeaten ++;
				if(hitHeaten == 10) {
					hitHeaten = 0;
					checkObjectivesCompletion(damager, "Donner 10 coups d'épée");
				}
			}
		}
	}

	@EventHandler
	public void onPlayerKill(PlayerDeathEvent event) {
		Player killer = event.getEntity().getKiller();
		if (killer != null) {
			List<String> objectives = playerObjectives.get(killer.getUniqueId());
			if(objectives != null  && objectives.contains("Faire un kill")) {
				checkObjectivesCompletion(killer, "Faire un kill");
			}
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		List<String> objectives = playerObjectives.get(player.getUniqueId());
		if(objectives != null && objectives.contains("Casser 64 blocs de pierre")) {
			blockBroken ++;
			if(blockBroken == 64) {
				checkObjectivesCompletion(player, "Casser 64 blocs de pierre");
			}
		}
	}

	@EventHandler
	public void onPlayerTakeFallDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
			Player player = (Player) event.getEntity();
			List<String> objectives = playerObjectives.get(player.getUniqueId());
			if(objectives != null && objectives.contains("Prendre un dégât de chute")) {
                checkObjectivesCompletion(player, "Prendre un dégât de chute");
            }
		}
	}
	
	@EventHandler
	private void OnInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		ItemStack item = event.getItem();
		if(isFrisk(playerManager.getPlayer(player)) &&
			item.equals(getSoulItem()) &&
			hasUsedMercy &&
			duo != null &&
			canUseSoulPower) {
			canUseSoulPower = false;
			player.sendMessage("Vous avez donner un coeur en plus à votre duo");
			duo.sendMessage("Vous avez reçu un coeur en plus de la part de votre duo");
			duo.setMaxHealth(duo.getMaxHealth() + 0.5);
		}
	}

	@EventHandler
	private void OnDeath(PlayerDeathEvent event) {
		Player victim = event.getEntity().getPlayer();
		Player killer = event.getEntity().getKiller();
		if(victim.getInventory().contains(getSoulItem())) {
			List<ItemStack> drops = event.getDrops();
			drops.remove(getSoulItem());
		}

		if(isFrisk(playerManager.getPlayer(victim))) {
			duo.getInventory().addItem(getSoulItem());
			duo.setMaxHealth(duo.getMaxHealth() + 0.5);
			duo.sendMessage("Vous avez reçu l'âme de votre partenaire");
		}
	}

	private boolean isFrisk(UhcPlayer player) {
		return player.getRole() != null && player.getRole().getName().equals("Frisk");
	}

	private ItemStack getSoulItem() {
		ItemStack soul = new ItemStack(Material.NETHER_STAR);
		ItemMeta soul_meta = soul.getItemMeta();
		if (soul_meta != null) {
			soul_meta.setDisplayName(ChatColor.RED + "Ame de Determination");
			soul.setItemMeta(soul_meta);
		}
		return soul;
	}
}