package com.mguhc.undertale.roles.humain.frisk;

import com.mguhc.UhcAPI;
import com.mguhc.ability.Ability;
import com.mguhc.ability.AbilityManager;
import com.mguhc.ability.CooldownManager;
import com.mguhc.player.PlayerManager;
import com.mguhc.roles.RoleManager;
import com.mguhc.roles.UhcRole;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.mguhc.undertale.roles.humain.HumainListener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Arrays;
import java.util.List;

public class FriskListener implements Listener {

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

	}
}
