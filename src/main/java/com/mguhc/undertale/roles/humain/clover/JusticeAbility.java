package com.mguhc.undertale.roles.humain.clover;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.mguhc.ability.Ability;

public class JusticeAbility implements Ability {
    private double cooldownDuration = 30*1000;

    @Override
    public void activate(Player player) {
    // Logic to activate the Justice ability
    player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 5 * 20, 0));
    player.sendMessage(ChatColor.GREEN + "Vous avez activé la capacité de Justice !");
    }

    @Override
    public void deactivate(Player player) {
        // Pas de logique de désactivation nécessaire pour JusticeAbility
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