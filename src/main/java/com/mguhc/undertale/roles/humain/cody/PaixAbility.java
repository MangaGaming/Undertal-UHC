package com.mguhc.undertale.roles.humain.cody;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.mguhc.ability.Ability;

public class PaixAbility implements Ability {
    private final int duration = 5 * 20; // Durée de l'effet de paix en ticks (5 secondes)
	private double cooldownDuration = 60*1000;

    @Override
    public void activate(Player player) {
        if (player != null) {
            player.sendMessage(ChatColor.GREEN + "Vous avez activé l'effet de paix !");
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, duration, 0)); // Ajoute un effet de lenteur
        }
    }

    @Override
    public void deactivate(Player player) {
        // Pas de logique de désactivation nécessaire pour PaixAbility
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