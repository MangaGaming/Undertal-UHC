package com.mguhc.undertale.roles.humain.frisk;

import com.mguhc.ability.Ability;
import org.bukkit.entity.Player;

public class SaveAbility implements Ability {
    private double cooldownDuration = 5*1000;

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
    public void setCooldownDuration(double v) {
        cooldownDuration = v;
    }
}
