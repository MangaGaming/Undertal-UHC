package com.mguhc.undertale.roles.humain.frisk;

import com.mguhc.UhcAPI;
import com.mguhc.ability.Ability;
import com.mguhc.ability.AbilityManager;
import com.mguhc.ability.CooldownManager;
import com.mguhc.effect.EffectManager;
import com.mguhc.player.PlayerManager;
import com.mguhc.player.UhcPlayer;
import com.mguhc.roles.RoleManager;
import com.mguhc.roles.UhcRole;
import com.mguhc.undertale.UndertaleUHC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.mguhc.undertale.roles.humain.HumainListener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;

public class FriskListener implements Listener {

	private EffectManager effectManager;
	private SaveAbility saveAbility;
	private ActAbility actAbility;
	private PlayerManager playerManager;
	private RoleManager roleManager;
	private CooldownManager cooldownManager;
	private AbilityManager abilityManager;
	private HumainListener humainListener;

	public FriskListener(HumainListener humainListener) {
    	UhcAPI api = UhcAPI.getInstance();
    	this.playerManager = api.getPlayerManager();
    	this.roleManager = api.getRoleManager();
    	this.cooldownManager = api.getCooldownManager();
    	this.abilityManager = api.getAbilityManager();
		this.effectManager = api.getEffectManager();

    	this.humainListener = humainListener;
		UhcRole friskRole = roleManager.getUhcRole("Frisk");
		if(friskRole != null) {
	        this.saveAbility = new SaveAbility();
			this.actAbility = new ActAbility();
			List<Ability> abilities = Arrays.asList(saveAbility, actAbility);
			abilityManager.registerAbility(friskRole, abilities);
		}
	}

	@EventHandler
	private void OnCommand(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		String[] args = event.getMessage().split(" ");
		if(args.length == 3 && args[0].equals("/ut") && args[1].equals("save")) {
			if(isFirsk(playerManager.getPlayer(player))) {
				Player aimedPlayer = Bukkit.getPlayer(args[2]);
				if(aimedPlayer != null) {
					if(cooldownManager.isInCooldown(player, saveAbility)) {
						cooldownManager.startCooldown(player, saveAbility);
						effectManager.setWeakness(player, effectManager.getEffect(player, PotionEffectType.WEAKNESS) + 25);
						new BukkitRunnable() {
							@Override
							public void run() {
								effectManager.setWeakness(player, effectManager.getEffect(player, PotionEffectType.WEAKNESS) - 25);
							}
						}.runTaskLater(UndertaleUHC.getInstance(), 5*20);
					}
					else {
						player.sendMessage("Vous êtes en cooldown pour " + cooldownManager.getRemainingCooldown(player, saveAbility) / 1000 + " secondes");
					}
				}
			}
		}

	}

	private boolean isFirsk(UhcPlayer player) {
		return player.getRole() != null && player.getRole().getName().equals("Frisk");
	}
}
