package com.mguhc.undertale.roles.humain.erik;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.mguhc.UhcAPI;
import com.mguhc.ability.Ability;
import com.mguhc.ability.CooldownManager;

public class ChargeAudacieuseAbility implements Ability {
    private final double dashDistance = 10.0; // Distance du dash (10 blocs)
    private final double damage = 2.0; // Dégâts infligés
    private double cooldownDuration = 7 * 60 * 1000; // Cooldown de 7 minutes en millisecondes

    @Override
    public void activate(Player player) {
        // Vérifier si le joueur peut utiliser la capacité
        if (player == null) {
            return;
        }

        // Obtenir la direction du joueur
        Vector direction = player.getLocation().getDirection().normalize();
        player.setVelocity(direction.multiply(dashDistance)); // Déplacer le joueur

        // Infliger des dégâts aux joueurs sur le chemin
        for (Entity entity : player.getNearbyEntities(dashDistance, dashDistance, dashDistance)) {
            if (entity instanceof Player && !entity.equals(player)) {
                Player target = (Player) entity;
                // Vérifier si le joueur est sur le chemin du dash
                if (isInDashPath(player, target, direction, dashDistance)) {
                    target.damage(damage); // Infliger des dégâts
                }
            }
        }

        // Message de confirmation
        player.sendMessage(ChatColor.GOLD + "Vous avez utilisé la Charge Audacieuse !");
        
        // Démarrer le cooldown
        startCooldown(player);
    }

    @Override
    public void deactivate(Player player) {
        // Logique pour désactiver la capacité si nécessaire
        // Dans ce cas, la capacité est instantanée, donc rien à faire ici
    }

    private void startCooldown(Player player) {
        // Utiliser le CooldownManager pour démarrer le cooldown
        CooldownManager cooldownManager = UhcAPI.getInstance().getCooldownManager();
        cooldownManager.startCooldown(player, this);
    }

    private boolean isInDashPath(Player player, Player target, Vector direction, double distance) {
        // Vérifier si le joueur cible est sur le chemin du dash
        Vector playerLocation = player.getLocation().toVector();
        Vector targetLocation = target.getLocation().toVector();
        Vector endLocation = playerLocation.add(direction.multiply(distance));

        // Vérifier si la cible est dans un rayon de 1 bloc du chemin
        return targetLocation.isInAABB(playerLocation, endLocation);
    }

	@Override
	public double getCooldownDuration() {
		return cooldownDuration;
	}

	@Override
	public void setCooldownDuration(double n) {
		cooldownDuration = n;
	}
}