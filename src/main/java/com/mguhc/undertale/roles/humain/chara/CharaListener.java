package com.mguhc.undertale.roles.humain.chara;

import com.mguhc.UhcAPI;
import com.mguhc.ability.AbilityManager;
import com.mguhc.ability.CooldownManager;
import com.mguhc.effect.EffectManager;
import com.mguhc.player.PlayerManager;
import com.mguhc.player.UhcPlayer;
import com.mguhc.roles.RoleManager;
import com.mguhc.roles.UhcRole;
import com.mguhc.undertale.UndertaleUHC;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class CharaListener implements Listener {

    private RageAbility rageAbility;
    private EffectManager effectManager;
    private AbilityManager abilityManager;
    private CooldownManager cooldownManager;
    private RoleManager roleManager;
    private PlayerManager playerManager;

    public CharaListener() {
        UhcAPI api = UhcAPI.getInstance();
        this.playerManager = api.getPlayerManager();
        this.roleManager = api.getRoleManager();
        this.cooldownManager = api.getCooldownManager();
        this.abilityManager = api.getAbilityManager();
        this.effectManager = UhcAPI.getInstance().getEffectManager();

        UhcRole charaRole = roleManager.getUhcRole("Chara");
        if(charaRole != null) {
            this.rageAbility = new RageAbility();
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                UhcPlayer uhc_player = roleManager.getPlayerWithRole("Chara");
                if (uhc_player != null) {
                    long time = uhc_player.getPlayer().getWorld().getTime(); // Obtenir le temps actuel dans le monde
                    if (time >= 0 && time < 12000) { // Vérifier si c'est le jour
                        effectManager.setResistance(uhc_player.getPlayer(), 20);
                    } else {
                        effectManager.removeEffect(uhc_player.getPlayer(), PotionEffectType.DAMAGE_RESISTANCE);
                    }
                }
            }
        }.runTaskTimer(UndertaleUHC.getInstance(), 0, 3 * 20); // Vérifie toutes les secondes
    }
}
