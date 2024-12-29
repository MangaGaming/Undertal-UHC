package com.mguhc.undertale.roles.humain.cody;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.mguhc.ability.Ability;

public class HealAbility implements Ability {
    private final double healAmount = 4.0; // Montant de soin
	private double cooldownDuration = 20*60*1000;

    @Override
    public void activate(Player player) {
        if (player != null) {
            player.setHealth(Math.min(player.getHealth() + healAmount, player.getMaxHealth())); // Ne pas dépasser la santé maximale
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 10 * 20, 0)); // Ajoute un effet d'absorption
            player.sendMessage(ChatColor.GREEN + "Vous vous êtes soigné de " + healAmount + " PV.");
        }
    }

	@Override
	public void deactivate(Player player) {
		
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
