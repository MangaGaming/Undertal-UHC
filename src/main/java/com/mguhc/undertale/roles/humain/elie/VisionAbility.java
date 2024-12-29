package com.mguhc.undertale.roles.humain.elie;

import org.bukkit.entity.Player;

import com.mguhc.ability.Ability;

public class VisionAbility implements Ability {

	private double cooldownDuration = 3*60*1000;

	@Override
	public void activate(Player player) {
		
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
		cooldownDuration  = n;
	}
}
