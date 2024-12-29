package com.mguhc.undertale.roles.humain.clover;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.mguhc.ability.Ability;

public class SoulPowerAbility implements Ability {
    private double cooldownDuration = 20*60*1000;

	@Override
    public void activate(Player player) {
        // Logique pour activer la capacité de pouvoir de l'âme
        player.sendMessage(ChatColor.GREEN + "Un ennemi proche de vous a attaqué un de vos alliés !");
    }

    @Override
    public void deactivate(Player player) {
        // Pas de logique de désactivation nécessaire pour SoulPowerAbility
    }

	@Override
	public double getCooldownDuration() {
		return cooldownDuration;
	}

	@Override
	public void setCooldownDuration(double n) {
		cooldownDuration  = n;
	}
}
