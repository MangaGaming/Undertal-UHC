package com.mguhc.undertale.roles.monstre.flowey;

import com.mguhc.UhcAPI;
import com.mguhc.ability.Ability;
import com.mguhc.ability.AbilityManager;
import com.mguhc.ability.CooldownManager;
import com.mguhc.effect.EffectManager;
import com.mguhc.events.RoleGiveEvent;
import com.mguhc.player.PlayerManager;
import com.mguhc.player.UhcPlayer;
import com.mguhc.roles.RoleManager;
import com.mguhc.roles.UhcRole;
import com.mguhc.undertale.UndertaleUHC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class FloweyListener implements Listener {

    private Ability scanAbility;
    private Ability cibleAbility;
    private EffectManager effectManager;
    private AbilityManager abilityManager;
    private CooldownManager cooldownManager;
    private RoleManager roleManager;
    private PlayerManager playerManager;

    private boolean isInOmega = false;

    public FloweyListener() {
        UhcAPI api = UhcAPI.getInstance();
        this.playerManager = api.getPlayerManager();
        this.roleManager = api.getRoleManager();
        this.cooldownManager = api.getCooldownManager();
        this.abilityManager = api.getAbilityManager();
        this.effectManager = api.getEffectManager();

        UhcRole role = roleManager.getUhcRole("Flowey");
        if (role != null) {
            this.cibleAbility = new CibleAbility();
            this.scanAbility = new ScanAbility();
            abilityManager.registerAbility(role, Arrays.asList(cibleAbility, scanAbility));
        }
    }

    @EventHandler
    private void OnRoleGive(RoleGiveEvent event) {
        UhcPlayer uhcPlayer = roleManager.getPlayerWithRole("Flowey");
        if (uhcPlayer != null) {
            Player player = uhcPlayer.getPlayer();

            new BukkitRunnable() {
                @Override
                public void run() {
                    long time = player.getWorld().getTime(); // Obtenir le temps actuel dans le monde
                    if (time >= 0 && time < 12000) {
                        effectManager.removeEffect(player, PotionEffectType.INCREASE_DAMAGE);
                    } else if (!isInOmega) {
                        effectManager.setStrength(player, 20);
                    }
                }
            }.runTaskTimer(UndertaleUHC.getInstance(), 0, 20*3);
        }
    }

    @EventHandler
    private void OnCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String[] args = event.getMessage().split(" ");

        if (isFlowey(player) && args.length == 2 && args[0].equals("/ut") && args[1].equals("cible")) {
            if (cooldownManager.getRemainingCooldown(player, cibleAbility) == 0) {
                cooldownManager.startCooldown(player, cibleAbility);
                // Récupérer tous les joueurs en ligne
                List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
                List<Player> playersWithSoul = new ArrayList<>();

                // Filtrer les joueurs qui ont une âme
                for (Player p : onlinePlayers) {
                    if (hasSoul(p)) {
                        playersWithSoul.add(p);
                    }
                }

                // Vérifier qu'il y a au moins 2 joueurs avec une âme
                if (playersWithSoul.size() < 2) {
                    player.sendMessage(ChatColor.RED + "Il n'y a pas assez de joueurs avec une âme.");
                    return;
                }

                // Créer la liste finale de joueurs
                List<Player> selectedPlayers = new ArrayList<>(playersWithSoul.subList(0, 2)); // Ajouter les 2 joueurs avec une âme

                // Ajouter 4 autres joueurs aléatoires
                Random random = new Random();
                while (selectedPlayers.size() < 6) {
                    Player randomPlayer = onlinePlayers.get(random.nextInt(onlinePlayers.size()));
                    if (!selectedPlayers.contains(randomPlayer) && !playersWithSoul.contains(randomPlayer)) {
                        selectedPlayers.add(randomPlayer);
                    }
                }

                // Envoyer la liste au joueur
                StringBuilder message = new StringBuilder(ChatColor.GREEN + "Joueurs sélectionnés :\n");
                for (Player selected : selectedPlayers) {
                    message.append("- ").append(selected.getName()).append("\n");
                }
                player.sendMessage(message.toString());
            }
            else {
                player.sendMessage("Vous êtes en cooldown pour " + cooldownManager.getRemainingCooldown(player, scanAbility) / 1000);
            }
        }

        if (isFlowey(player) && args.length == 3 && args[0].equals("/ut") && args[1].equals("scan")) {
            Player target = Bukkit.getPlayer(args[2]);
            if (target != null) {
                if (cooldownManager.getRemainingCooldown(player, scanAbility) == 0) {
                    cooldownManager.startCooldown(player, scanAbility);
                    if (hasGasterFragment(target) || hasSoul(target)) {
                        player.sendMessage(ChatColor.RED + "Vous etes empli de determination");
                    }
                }
                else {
                    player.sendMessage("Vous êtes en cooldown pour " + cooldownManager.getRemainingCooldown(player, scanAbility) / 1000);
                }
            }
            else {
                player.sendMessage("Joueur offline");
            }
        }

        if (isFlowey(player) && args.length == 2 && args[0].equals("/ut") && args[1].equals("omega")) {
            int soulCount = 0;
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                    if (item.getItemMeta().getDisplayName().contains("Ame")) {
                        soulCount++;
                    }
                }
            }
            if (soulCount >= 6) {
                isInOmega = true;
                player.sendMessage(ChatColor.GREEN + "Vous etes passe en forme Omega");
                effectManager.setSpeed(player, 40);
                effectManager.setStrength(player, 40);

                UhcPlayer frisk = roleManager.getPlayerWithRole("Frisk");
                if (frisk != null) {
                    Location location = frisk.getPlayer().getLocation();
                    player.sendMessage("Frisk est en x : " + location.getBlockX() + " z : " + location.getBlockZ());
                }
            }
        }
    }

    private boolean hasSoul(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                if (item.getItemMeta().getDisplayName().contains("Ame")) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasGasterFragment(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.equals(getGasterItem())) {
                return true;
            }
        }
        return false;
    }

    private ItemStack getGasterItem() {
        ItemStack gaster = new ItemStack(Material.NETHER_STAR);
        ItemMeta gasterMeta = gaster.getItemMeta();
        if(gasterMeta != null) {
            gasterMeta.setDisplayName(ChatColor.DARK_PURPLE + "Fragment de Gaster");
            gaster.setItemMeta(gasterMeta);
        }
        return gaster;
    }

    private boolean isFlowey(Player player) {
        UhcPlayer uhcPlayer = playerManager.getPlayer(player);
        return uhcPlayer != null && uhcPlayer.getRole() != null && uhcPlayer.getRole().getName().equals("Flowey");
    }
}
