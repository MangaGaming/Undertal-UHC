package com.mguhc.undertale.roles.solo.gaster;

import com.mguhc.UhcAPI;
import com.mguhc.game.UhcGame;
import com.mguhc.player.UhcPlayer;
import com.mguhc.roles.RoleManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class TruLabTask extends BukkitRunnable implements Listener {
    private final UhcGame uhcGame;
    private final RoleManager roleManager;
    private Location centerLocation;

    public TruLabTask() {
        this.uhcGame = UhcAPI.getInstance().getUhcGame();
        this.roleManager = UhcAPI.getInstance().getRoleManager();
    }

    @Override
    public void run() {
        if (uhcGame.getTimePassed() == 10) {
            UhcPlayer gaster = roleManager.getPlayerWithRole("W.D Gaster");
            UhcPlayer sans = roleManager.getPlayerWithRole("Sans");
            Location trueLabEntryLocation = getTrueLabEntryLocation();

            String message = String.format("Les coordonnées de l'entrée du laboratoire vrai sont : X: %d, Y: %d, Z: %d",
                    trueLabEntryLocation.getBlockX(),
                    trueLabEntryLocation.getBlockY(),
                    trueLabEntryLocation.getBlockZ());

            if (gaster != null) {
                gaster.getPlayer().sendMessage(message);
            }
            if (sans != null) {
                sans.getPlayer().sendMessage(message);
            }

            // Téléportation de la structure
            teleportStructure(new Location(Bukkit.getWorld("world"), 250, 247, 249), trueLabEntryLocation);

            // Obtenir la position centrale de la structure
            centerLocation = getCenterLocation(trueLabEntryLocation);
            // Vous pouvez utiliser centerLocation ici si nécessaire
        }
    }

    public Location getTrueLabEntryLocation() {
        World world = Bukkit.getWorld("world");
        int x = (int) (Math.random() * (-400 + 250)) + 250; // Génère un x entre -250 et -400
        int z = (int) (Math.random() * (400 + 250)) - 250; // Génère un z entre 250 et 400
        int y = world.getHighestBlockYAt(x, z); // Obtient la hauteur du bloc le plus élevé à la position (x, z)

        return new Location(world, x, y, z);
    }

    private void teleportStructure(Location originalLocation, Location newLocation) {
        World world = originalLocation.getWorld();

        int width = 10; // Largeur de la structure
        int height = 10; // Hauteur de la structure
        int depth = 10; // Profondeur de la structure

        // Parcourir chaque bloc de la structure
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    Block originalBlock = world.getBlockAt(originalLocation.clone().add(x, y, z));
                    Material material = originalBlock.getType(); // Obtenir le type de bloc

                    // Définir le bloc à la nouvelle position
                    world.getBlockAt(newLocation.clone().add(x, y, z)).setType(material);
                }
            }
        }
    }

    public Location getCenterLocation(Location newLocation) {
        // Coordonnées du centre de la structure
        int centerX = newLocation.getBlockX() + 3;
        int centerY = newLocation.getBlockY() + 1;
        int centerZ = newLocation.getBlockZ() + 4;

        return new Location(newLocation.getWorld(), centerX, centerY, centerZ);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location playerLocation = player.getLocation();

        // Vérifier si le joueur est proche de la centerLocation
        if (centerLocation != null &&
            playerLocation.getBlockX() == centerLocation.getBlockX() &&
            playerLocation.getBlockY() == centerLocation.getBlockY() &&
            playerLocation.getBlockZ() == centerLocation.getBlockZ()) {
            player.teleport(new Location(Bukkit.getWorld("world"), -28, 222, 159));
        }
    }
}