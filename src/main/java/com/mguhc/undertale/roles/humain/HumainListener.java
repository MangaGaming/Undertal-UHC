package com.mguhc.undertale.roles.humain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.mguhc.UhcAPI;
import com.mguhc.events.RoleGiveEvent;
import com.mguhc.player.UhcPlayer;
import com.mguhc.roles.Camp;
import com.mguhc.roles.RoleManager;
import com.mguhc.roles.UhcRole;

public class HumainListener implements Listener {

    private static HumainListener instance;
    private Map<Player, Player> duos = new HashMap<>(); // Map pour stocker les duos

    @EventHandler
    public void onRoleGive(RoleGiveEvent event) {
        RoleManager roleManager = UhcAPI.getInstance().getRoleManager();

        // Obtenir le camp "Humain"
        Camp humainCamp = roleManager.getCamps().stream()
                .filter(camp -> camp.getName().equalsIgnoreCase("Humain"))
                .findFirst()
                .orElse(null);

        if (humainCamp == null) {
            return; // Si le camp n'existe pas, on sort
        }

        // Récupérer tous les rôles du camp "Humain"
        List<UhcRole> humainRoles = humainCamp.getAssociatedRoles();
        List<UhcPlayer> playersInHumain = new ArrayList<>();

        for (UhcRole role : humainRoles) {
            // Supposons que UhcRole a une méthode pour obtenir le joueur
            UhcPlayer player = roleManager.getPlayerWithRole(role.getName());
            if (player != null) {
                playersInHumain.add(player); // Ajouter le joueur à la liste
            }
        }

        // Mélanger la liste pour randomiser les paires
        Collections.shuffle(playersInHumain);

        // Former des duos
        List<String> pairs = new ArrayList<>();
        for (int i = 0; i < playersInHumain.size(); i += 2) {
            if (i + 1 < playersInHumain.size()) {
                UhcPlayer player1 = playersInHumain.get(i);
                UhcPlayer player2 = playersInHumain.get(i + 1);
                pairs.add(player1.getPlayer().getName() + " est associé à " + player2.getPlayer().getName());

                // Stocker le duo
                duos.put(player1.getPlayer(), player2.getPlayer());
                duos.put(player2.getPlayer(), player1.getPlayer());
            } else {
                // Si le nombre de joueurs est impair, le dernier reste sans paire
                pairs.add(playersInHumain.get(i).getPlayer().getName() + " n'a pas de paire.");
            }
        }

        // Annonce des paires
        for (String pair : pairs) {
            // Envoyer le message à tous les joueurs dans le camp "Humain"
            for (UhcPlayer player : playersInHumain) {
                player.getPlayer().sendMessage(pair);
            }
        }
    }

    public Player getDuo(Player player) {
        return duos.get(player); // Retourne le duo du joueur
    }

    public static HumainListener getInstance() {
        return instance;
    }
}