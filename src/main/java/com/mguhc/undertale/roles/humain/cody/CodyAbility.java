package com.mguhc.undertale.roles.humain.cody;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.mguhc.ability.Ability;

public class CodyAbility implements Ability {
    private final int duration = 15 * 20; // Durée de la résistance en ticks (15 secondes)
	private double cooldownDuration = 2*1000;

    @Override
    public void activate(Player player) {
        if (player != null) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, duration, 0)); // Ajoute un effet de résistance
            player.sendMessage(ChatColor.GREEN + "Vous avez reçu une résistance aux dégâts pendant " + (duration / 20) + " secondes.");
        }
    }

    @Override
    public void deactivate(Player player) {
        // Pas de logique de désactivation nécessaire pour CodyAbility
    }

	@Override
	public double getCooldownDuration() {
		return cooldownDuration ;
	}
	
	@Override
	public void setCooldownDuration(double n) {
		cooldownDuration = n;
	}
}