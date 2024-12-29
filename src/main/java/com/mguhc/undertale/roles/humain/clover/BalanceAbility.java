package com.mguhc.undertale.roles.humain.clover;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.mguhc.ability.Ability;

public class BalanceAbility implements Ability {
	
    private double cooldownDuration = 20*60*1000;

	@Override
    public void activate(Player player) {
        // Logique pour activer la capacité de balance
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10 * 20, 0));
        player.sendMessage(ChatColor.GREEN + "Vous avez activé la capacité de Balance !");
    }

    @Override
    public void deactivate(Player player) {
        // Pas de logique de désactivation nécessaire pour BalanceAbility
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
