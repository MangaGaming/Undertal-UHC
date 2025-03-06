package com.mguhc.undertale.roles.monstre.napstablook;

import com.mguhc.UhcAPI;
import com.mguhc.ability.AbilityManager;
import com.mguhc.ability.CooldownManager;
import com.mguhc.effect.EffectManager;
import com.mguhc.events.RoleGiveEvent;
import com.mguhc.player.PlayerManager;
import com.mguhc.player.UhcPlayer;
import com.mguhc.roles.RoleManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Random;

public class NapstablookListener implements Listener {

    private EffectManager effectManager;
    private AbilityManager abilityManager;
    private CooldownManager cooldownManager;
    private RoleManager roleManager;
    private PlayerManager playerManager;

    // Pour suivre le temps d'immobilité des joueurs Napstablook
    private HashMap<Player, Long> immobilePlayers = new HashMap<>();

    public NapstablookListener() {
        UhcAPI api = UhcAPI.getInstance();
        this.playerManager = api.getPlayerManager();
        this.roleManager = api.getRoleManager();
        this.cooldownManager = api.getCooldownManager();
        this.abilityManager = api.getAbilityManager();
        this.effectManager = api.getEffectManager();
    }

    @EventHandler
    private void OnRoleGive(RoleGiveEvent event) {
        UhcPlayer uhcPlayer = roleManager.getPlayerWithRole("Napstablook");
        if (uhcPlayer != null) {
            ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
            ItemMeta book_meta = book.getItemMeta();
            if (book_meta != null) {
                book_meta.addEnchant(Enchantment.ARROW_DAMAGE, 1, true);
                book.setItemMeta(book_meta);
            }
            Player player = uhcPlayer.getPlayer();
            player.getInventory().addItem(book);
            effectManager.setWeakness(player, 1.5);
            // Initialiser le temps d'immobilité
            immobilePlayers.put(player, System.currentTimeMillis());

            // Tâche pour vérifier l'immobilité des joueurs Napstablook
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (isNapstablook(player)) {
                        long lastMoveTime = immobilePlayers.get(player);
                        if (System.currentTimeMillis() - lastMoveTime >= 10000) { // 10 secondes
                            // Régénérer 0.5 cœur
                            double newHealth = Math.min(player.getHealth() + 1, player.getMaxHealth());
                            player.setHealth(newHealth);
                            player.sendMessage("Vous avez régénéré 0.5 cœur !");
                            // Réinitialiser le temps d'immobilité
                            immobilePlayers.put(player, System.currentTimeMillis());
                        }
                    } else {
                        this.cancel(); // Annuler la tâche si le joueur n'est plus Napstablook
                    }
                }
            }.runTaskTimer(UhcAPI.getInstance(), 0L, 20L); // Vérifie toutes les secondes
        }
    }

    @EventHandler
    private void OnDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (isNapstablook(player) &&
                player.getHealth() - event.getFinalDamage() <= 0) {

                // Vérifier la santé maximale avant de la réduire
                double newMaxHealth = player.getMaxHealth() - 4;
                if (newMaxHealth > 0) {
                    event.setCancelled(true);
                    teleportPlayerToRandomLocation(player);
                    player.setMaxHealth(newMaxHealth);
                }
            }
        }
    }

    @EventHandler
    private void OnDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Projectile) {
            Player victim = (Player) event.getEntity();
            Projectile projectile = (Projectile) event.getDamager();
            Player damager = (Player) projectile.getShooter();

            if (damager != null && isNapstablook(damager)) {
                double damage = event.getDamage();
                double new_damage = (double) 110 / 100 * damage;
                event.setDamage(new_damage);
            }
        }
    }

    private void teleportPlayerToRandomLocation(Player player) {
        Random random = new Random();
        int x = random.nextInt(201) + 400; // Coordonnée X aléatoire entre 400 et 600
        int z = random.nextInt(201) + 400; // Coordonnée Z aléatoire entre 400 et 600
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 255, 5 * 20));
        player.setHealth(player.getMaxHealth());
        player.teleport(new Location(player.getWorld(), x, 100, z));
    }

    private boolean isNapstablook(Player player) {
        UhcPlayer uhcPlayer = playerManager.getPlayer(player);
        return uhcPlayer != null && uhcPlayer.getRole() != null && uhcPlayer.getRole().getName().equals("Napstablook");
    }

    // Méthode pour mettre à jour le temps d'immobilité
    @EventHandler
    public void onPlayerMove(org.bukkit.event.player.PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (isNapstablook(player)) {
            immobilePlayers.put(player, System.currentTimeMillis()); // Met à jour le temps d'immobilité
        }
    }
}
