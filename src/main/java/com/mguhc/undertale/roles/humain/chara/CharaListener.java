package com.mguhc.undertale.roles.humain.chara;

import com.mguhc.UhcAPI;
import com.mguhc.ability.Ability;
import com.mguhc.ability.AbilityManager;
import com.mguhc.ability.CooldownManager;
import com.mguhc.effect.EffectManager;
import com.mguhc.events.RoleGiveEvent;
import com.mguhc.events.UhcDeathEvent;
import com.mguhc.player.PlayerManager;
import com.mguhc.player.UhcPlayer;
import com.mguhc.roles.RoleManager;
import com.mguhc.roles.UhcRole;
import com.mguhc.undertale.UndertaleUHC;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class CharaListener implements Listener {

    private PecherAbility pecherAbility;
    private RageAbility rageAbility;
    private EffectManager effectManager;
    private AbilityManager abilityManager;
    private CooldownManager cooldownManager;
    private RoleManager roleManager;
    private PlayerManager playerManager;
    private boolean canUseRoad = true;
    private boolean hasUsedRoad = false;
    private HashMap<Player, Boolean> playerCanRevive = new HashMap<>();
    private Location soulCoordinate;
    private boolean canRecupSoul = false;

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
    }

    @EventHandler
    private void OnRoleGive(RoleGiveEvent event) {
        UhcPlayer uhcPlayer = roleManager.getPlayerWithRole("Chara");
        if(uhcPlayer != null) {
            Player player = uhcPlayer.getPlayer();

            ItemStack sharp4 = new ItemStack(Material.ENCHANTED_BOOK);
            ItemMeta sharp_meta = sharp4.getItemMeta();
            if(sharp_meta != null) {
                sharp_meta.addEnchant(Enchantment.DAMAGE_ALL, 4, true);
                sharp_meta.setDisplayName(ChatColor.BLUE + "Livre Sharpness 4");
                sharp4.setItemMeta(sharp_meta);
            }
            player.getInventory().addItem(sharp4);

            ItemStack charaItem = new ItemStack(Material.NETHER_STAR);
            ItemMeta chara_meta = charaItem.getItemMeta();
            if(chara_meta != null) {
                chara_meta.setDisplayName(ChatColor.RED + "Ame de Determination (Obstination)");
                charaItem.setItemMeta(chara_meta);
            }
            player.getInventory().addItem(charaItem);

            new BukkitRunnable() {
                @Override
                public void run() {
                    long time = uhcPlayer.getPlayer().getWorld().getTime(); // Obtenir le temps actuel dans le monde
                    if (time >= 0 && time < 12000) { // Vérifier si c'est le jour
                        effectManager.setResistance(uhcPlayer.getPlayer(), 20);
                    } else {
                        effectManager.removeEffect(uhcPlayer.getPlayer(), PotionEffectType.DAMAGE_RESISTANCE);
                    }
                }
            }.runTaskTimer(UndertaleUHC.getInstance(), 0, 3 * 20); // Vérifie toutes les secondes
        }
    }

    @EventHandler
    private void OnDamage(EntityDamageByEntityEvent event) {
        if(event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player victim = (Player) event.getEntity();
            Player damager = (Player) event.getDamager();
            if(isChara(playerManager.getPlayer(damager))) {
                if(cooldownManager.getRemainingCooldown(damager, rageAbility) == 0) {
                    cooldownManager.startCooldown(damager, rageAbility);
                    event.setDamage(event.getDamage() + 4);
                    damager.sendMessage("Vous avez utiliser votre Rage Genocidaire");
                }
            }
        }
    }

    @EventHandler
    private void OnBasicDamage(EntityDamageEvent event) {
        if(event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            if(victim.getHealth() - event.getDamage() <= 0) {
                if(victim.getInventory().contains(getSoulItem()) &&
                    playerCanRevive.getOrDefault(victim, true)) {
                    event.setCancelled(true);
                    playerCanRevive.put(victim, false);
                    victim.setHealth(victim.getMaxHealth());
                    Random random = new Random();
                    int x = random.nextInt(201) + 400; // Coordonnée X aléatoire entre 400 et 600
                    int z = random.nextInt(201) + 400; // Coordonnée Z aléatoire entre 400 et 600
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 255, 10*20));
                    victim.teleport(new Location(victim.getWorld(), x, 100, z));
                }
            }
        }
    }

    @EventHandler
    private void OnCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String[] args = event.getMessage().split(" ");
        if(args.length == 2 && args[0].equals("/ut") && args[1].equals("road")) {
            if(isChara(playerManager.getPlayer(player)) &&
               canUseRoad) {
                canUseRoad = false;
                hasUsedRoad = true;
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
                    player.closeInventory();
                    canUseRoad = false;
                    player.sendMessage(ChatColor.RED + "Vous avez choisi de trahir les humains");
                    roleManager.setCamp(playerManager.getPlayer(player), roleManager.getCamps().get(1));
                    effectManager.setStrength(player, 20);
                    effectManager.setSpeed(player, 20);
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
                    player.closeInventory();
                    canUseRoad = false;
                    player.sendMessage(ChatColor.GREEN + "Vous avez choisi de rester avec les humains");
                }
            }
        }
        if(event.getView().getTitle().equals(ChatColor.DARK_GRAY + "Voulez vous récupérer l'ame de détermination de Chara")) {
            event.setCancelled(true);

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.hasItemMeta()) {
                Player player = (Player) event.getWhoClicked();
                String choose = clickedItem.getItemMeta().getDisplayName();
                if(choose.equals(ChatColor.GREEN + "Oui")) {
                    player.sendMessage(ChatColor.RED + "Vous avez choisi de récupérer l'ame de détermination de Chara");
                    player.getInventory().addItem(getSoulItem());
                    player.setMaxHealth(player.getMaxHealth() - 10);
                    player.closeInventory();
                }
                if(choose.equals(ChatColor.RED + "Non")) {
                    player.closeInventory();
                    UhcPlayer friskUhcPlayer = roleManager.getPlayerWithRole("Frisk");
                    if(friskUhcPlayer != null) {
                        Player friskPlayer = friskUhcPlayer.getPlayer();
                        canRecupSoul = true;
                        friskPlayer.sendMessage("L'ame est disponible en x : " + (int) soulCoordinate.getX() + " y " + (int) soulCoordinate.getY() + " z " + (int) soulCoordinate.getZ());
                    }
                }
            }
        }
    }

    @EventHandler
    private void OnDeath(UhcDeathEvent event) {
        Player victim = event.getPlayer();
        Player killer = event.getKiller();
        if(killer != null) {
            if(isChara(playerManager.getPlayer(victim))) {
                soulCoordinate = killer.getLocation();
                killer.sendMessage("Vous avez tué Chara");
                event.getDrops().remove(getSoulItem());
                openChoixInventory(killer);
            }
        }
    }

    private void openChoixInventory(Player player) {
        // Créer un inventaire avec un titre et une taille de 9 slots
        Inventory inventory = Bukkit.createInventory(null, 9, ChatColor.DARK_GRAY + "Voulez vous récupérer l'ame de détermination de Chara");

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
    private void OnMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if(soulCoordinate != null &&
            canRecupSoul &&
            (long) player.getLocation().getX() == (long) soulCoordinate.getX() &&
            (long) player.getLocation().getZ() == (long) soulCoordinate.getZ()) {
            UhcRole role = playerManager.getPlayer(player).getRole();
            if(role != null &&
                role.getName().equals("Frisk")) {
                player.sendMessage("Vous avez récupérer l'ame de Chara");
                player.getInventory().addItem(getSoulItem());
                canRecupSoul = false;
            }
        }
        if(isChara(playerManager.getPlayer(player)) &&
            hasUsedRoad &&
            cooldownManager.getRemainingCooldown(player, pecherAbility) == 0) {
            Random random = new Random();
            List<Player> nearbyPlayers = new ArrayList<>();
            for(Entity entity : player.getNearbyEntities(15,15,15)) {
                if(entity instanceof Player) {
                    Player nearbyPlayer = (Player) entity;
                    if(nearbyPlayer.getGameMode().equals(GameMode.SURVIVAL) &&
                        !isChara(playerManager.getPlayer(nearbyPlayer))) {
                        nearbyPlayers.add(player);
                    }
                }
            }
            if(nearbyPlayers.toArray().length > 0) {
                cooldownManager.startCooldown(player, pecherAbility);
                Player chosenPlayer = nearbyPlayers.get(random.nextInt(nearbyPlayers.toArray().length));
                effectManager.setWeakness(chosenPlayer, effectManager.getEffect(chosenPlayer, PotionEffectType.WEAKNESS) + 20);
                player.sendMessage("Vous avez donner weakness à " + chosenPlayer.getName());
            }
        }
    }

    private boolean isChara(UhcPlayer player) {
        return player != null && player.getRole() != null && player.getRole().getName().equals("Chara");
    }

    private ItemStack getSoulItem() {
        ItemStack soul = new ItemStack(Material.NETHER_STAR);
        ItemMeta soul_meta = soul.getItemMeta();
        if (soul_meta != null) {
            soul_meta.setDisplayName(ChatColor.RED + "Ame de Determination (Obstination)");
            soul.setItemMeta(soul_meta);
        }
        return soul;
    }
}
