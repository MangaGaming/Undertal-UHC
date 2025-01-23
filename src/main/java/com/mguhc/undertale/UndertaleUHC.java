package com.mguhc.undertale;

import java.util.*;

import com.mguhc.undertale.roles.humain.HumainListener;
import com.mguhc.undertale.roles.humain.chara.CharaListener;
import com.mguhc.undertale.roles.monstre.asgore.AsgoreListener;
import com.mguhc.undertale.roles.monstre.asriel.AsrielListener;
import com.mguhc.undertale.roles.monstre.flowey.FloweyListener;
import com.mguhc.undertale.roles.monstre.mettaton.MettatonListener;
import com.mguhc.undertale.roles.monstre.napstablook.NapstablookListener;
import com.mguhc.undertale.roles.monstre.papyrus.PapyrusListener;
import com.mguhc.undertale.roles.monstre.sans.SansListener;
import com.mguhc.undertale.roles.monstre.undyne.UndyneListener;
import com.mguhc.undertale.roles.solo.gaster.GasterListener;
import com.mguhc.undertale.roles.solo.player.PlayerRoleListener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.mguhc.UhcAPI;
import com.mguhc.roles.Camp;
import com.mguhc.roles.UhcRole;
import com.mguhc.undertale.roles.humain.aliza.AlizaListener;
import com.mguhc.undertale.roles.humain.clover.CloverListener;
import com.mguhc.undertale.roles.humain.cody.CodyListener;
import com.mguhc.undertale.roles.humain.elie.ElieListener;
import com.mguhc.undertale.roles.humain.erik.ErikListener;
import com.mguhc.undertale.roles.humain.frisk.FriskListener;

public class UndertaleUHC extends JavaPlugin {

    private static UndertaleUHC instance;
    private List<UhcRole> roles;
    private Map<String, UhcRole> roleMap; // Map to store roles by name

    public void onEnable() {
        instance = this;
        new BukkitRunnable() {
            @Override
            public void run() {
                UhcAPI.getInstance().setUhcName("Undertale UHC");
                roles = new ArrayList<>();
                roleMap = new HashMap<>(); // Initialize the role map
                initializeRole();
                initializeCamp();
            }
        }.runTaskLater(this, 1);

        new BukkitRunnable() {

            @Override
            public void run() {
                registerListeners();
            }
        }.runTaskLater(this, 2);
    }

    protected void registerListeners() {
        // Enregistrement des écouteurs d'événements après l'initialisation
        PluginManager pluginManager = getServer().getPluginManager();
        HumainListener humainListener = new HumainListener();
        pluginManager.registerEvents(humainListener, this);
        pluginManager.registerEvents(new AlizaListener(), this);
        pluginManager.registerEvents(new CloverListener(), this);
        pluginManager.registerEvents(new CodyListener(), this);
        pluginManager.registerEvents(new ErikListener(), this);
        pluginManager.registerEvents(new ElieListener(), this);
        pluginManager.registerEvents(new FriskListener(), this);
        pluginManager.registerEvents(new CharaListener(), this);
        pluginManager.registerEvents(new SansListener(), this);
        pluginManager.registerEvents(new PapyrusListener(), this);
        pluginManager.registerEvents(new UndyneListener(), this);
        pluginManager.registerEvents(new NapstablookListener(), this);
        pluginManager.registerEvents(new MettatonListener(), this);
        pluginManager.registerEvents(new AsrielListener(), this);
        pluginManager.registerEvents(new AsgoreListener(), this);
        pluginManager.registerEvents(new FloweyListener(), this);
        pluginManager.registerEvents(new GasterListener(), this);
        pluginManager.registerEvents(new PlayerRoleListener(), this);
    }

    private void initializeCamp() {
        // Create the Human Camp
        Camp humanCamp = new Camp("Humain", "Location of Human Camp");
        humanCamp.addRole(roleMap.get("Aliza"));
        humanCamp.addRole(roleMap.get("Clover"));
        humanCamp.addRole(roleMap.get("Cody"));
        humanCamp.addRole(roleMap.get("Erik"));
        humanCamp.addRole(roleMap.get("Elie"));
        humanCamp.addRole(roleMap.get("Frisk"));
        humanCamp.addRole(roleMap.get("Chara"));
        UhcAPI.getInstance().getRoleManager().addCamp(humanCamp);

        // Create the Monster Camp
        Camp monsterCamp = new Camp("Monstre", "Location of Monster Camp");
        monsterCamp.addRole(roleMap.get("Muffet"));
        monsterCamp.addRole(roleMap.get("Toriel"));
        monsterCamp.addRole(roleMap.get("Sans"));
        monsterCamp.addRole(roleMap.get("Papyrus"));
        monsterCamp.addRole(roleMap.get("Undyne"));
        monsterCamp.addRole(roleMap.get("Napstablook"));
        monsterCamp.addRole(roleMap.get("Alphys"));
        monsterCamp.addRole(roleMap.get("Mettaton"));
        monsterCamp.addRole(roleMap.get("Asriel"));
        monsterCamp.addRole(roleMap.get("Asgore"));
        monsterCamp.addRole(roleMap.get("Flowey"));
        UhcAPI.getInstance().getRoleManager().addCamp(monsterCamp);

        // Create the Solo Camp
        Camp soloCamp = new Camp("Solo", "Location of Solo Camp");
        soloCamp.addRole(roleMap.get("W.D Gaster"));
        soloCamp.addRole(roleMap.get("Player"));
        soloCamp.addRole(roleMap.get("Betty"));
        UhcAPI.getInstance().getRoleManager().addCamp(soloCamp);

        Camp friskCamp = new Camp("Duo de Frisk", "Location of Frisk Camp");
        UhcAPI.getInstance().getRoleManager().addCamp(friskCamp);
    }

    private void initializeRole() {
        // Créez et stockez les rôles dans la liste et la carte
        addRole("Aliza", "Vous êtes Aliza");
        addRole("Clover", "Vous êtes Clover");
        addRole("Cody", "Vous êtes Cody");
        addRole("Erik", "Vous êtes Erik");
        addRole("Elie", "Vous êtes Elie");
        addRole("Frisk", "Vous êtes Frisk");
        addRole("Chara", "Vous êtes Chara");
        addRole("Muffet", "Vous êtes Muffet");
        addRole("Toriel", "Vous êtes Toriel");
        addRole("Sans", "Vous êtes Sans");
        addRole("Papyrus", "Vous êtes Papyrus");
        addRole("Undyne", "Vous êtes Undyne");
        addRole("Napstablook", "Vous êtes Napstablook");
        addRole("Alphys", "Vous êtes Alphys ");
        addRole("Mettaton", "Vous êtes Mettaton");
        addRole("Asriel", "Vous êtes Asriel");
        addRole("Asgore", "Vous êtes Asgore");
        addRole("Flowey", "Vous êtes Flowey");
        addRole("W.D Gaster", "Vous êtes W.D Gaster");
        addRole("Player", "Vous êtes Player");
        addRole("Betty", "Vous êtes Betty");

        // Ajoutez tous les rôles au RoleManager
        for (UhcRole role : roles) {
            UhcAPI.getInstance().getRoleManager().addRole(role);
        }
    }

    private void addRole(String name , String description) {
        UhcRole role = new UhcRole(name, description);
        roles.add(role);
        roleMap.put(name, role); // Store the role in the map
    }

    public static UndertaleUHC getInstance() {
        return instance;
    }
}