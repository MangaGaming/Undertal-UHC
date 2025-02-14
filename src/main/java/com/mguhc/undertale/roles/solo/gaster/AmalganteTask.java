package com.mguhc.undertale.roles.solo.gaster;

import com.mguhc.UhcAPI;
import com.mguhc.game.UhcGame;
import com.mguhc.player.UhcPlayer;
import net.minecraft.server.v1_8_R3.EntityBlaze;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftBlaze;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.Random;

public class AmalganteTask extends BukkitRunnable implements Listener {
    private final Random random = new Random();

    @Override
    public void run() {
        UhcGame uhcGame = UhcAPI.getInstance().getUhcGame();
        if (uhcGame.getTimePassed() == 30 * 60) {
            spawnFrag("Calm Girl", getRandomLocation());
        }

        if (uhcGame.getTimePassed() == 43 * 60) {
            spawnFrag("Goner Kid's", getRandomLocation());
        }

        if (uhcGame.getTimePassed() == 60 * 60) {
            spawnFrag("Amalgamate", getRandomLocation());
        }
    }

    private void spawnFrag(String name, Location location) {
        // Spawner le frag (entité) à la location donnée
        Blaze frag = (Blaze) location.getWorld().spawnEntity(location, EntityType.BLAZE); // Remplacez EntityType.ZOMBIE par le type d'entité souhaité
        disableAi(frag);
        frag.setCustomName(name); // Définir le nom de l'entité
        frag.setCustomNameVisible(true); // Rendre le nom visible
        UhcPlayer gaster = UhcAPI.getInstance().getRoleManager().getPlayerWithRole("W.D Gaster");
        if (gaster != null) {
            Player player = gaster.getPlayer();
            player.sendMessage(ChatColor.GREEN + "Un fragment de Gaster est apparue en " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
        }
    }

    private void disableAi(Blaze frag) {
        EntityBlaze nmsBlaze = ((CraftBlaze) frag).getHandle();
        NBTTagCompound tag = new NBTTagCompound();
        nmsBlaze.e(tag); // Récupérer les données de l'entité
        tag.setBoolean("NoAI", true); // Définir le tag NoAI à true
        nmsBlaze.f(tag); // Appliquer les données modifiées à l'entité
    }

    private Location getRandomLocation() {
        // Obtenir le monde
        World world = Bukkit.getWorld("world");

        // Générer des coordonnées aléatoires à 250 blocs du point d'origine
        int x = random.nextInt(501) - 250;
        int z = random.nextInt(501) - 250;

        // Créer la nouvelle location
        return new Location(world, x, world.getHighestBlockYAt(x, z), z);
    }

    @EventHandler
    private void OnEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            if (entity.getName().equals("Calm Girl") ||
                entity.getName().equals("Goner Kid's") ||
                entity.getName().equals("Amalgamate")) {
                killer.getInventory().addItem(getGasterItem());
            }
        }
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
}