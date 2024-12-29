package com.mguhc.undertale;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mguhc.undertale.roles.humain.HumainListener;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.mguhc.UhcAPI;
import com.mguhc.roles.Camp;
import com.mguhc.roles.RoleManager;
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
                initializeItems(); // Initialize items for roles
                initializeEffects(); // Initialize effects for roles
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
        pluginManager.registerEvents(new FriskListener(humainListener), this);
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

    private void initializeItems() {
        // Items pour Aliza
        ItemStack alizaItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta alizaMeta = alizaItem.getItemMeta(); // Obtenir l'ItemMeta
        if (alizaMeta != null) {
            alizaMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Ame de Perseverance");
            alizaItem.setItemMeta(alizaMeta); // Appliquer l'ItemMeta à l'ItemStack
        }
        List<ItemStack> alizaItems = new ArrayList<>();
        alizaItems.add(alizaItem);
        RoleManager roleManager = UhcAPI.getInstance().getRoleManager();
        roleManager.setItemToGive("Aliza", alizaItems);

        // Items pour Clover
        ItemStack cloverItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta cloverMeta = cloverItem.getItemMeta(); // Obtenir l'ItemMeta
        if (cloverMeta != null) {
            cloverMeta.setDisplayName(ChatColor.YELLOW + "Ame de Justice");
            cloverItem.setItemMeta(cloverMeta); // Appliquer l'ItemMeta à l'ItemStack
        }
        List<ItemStack> cloverItems = new ArrayList<>();
        cloverItems.add(cloverItem);
        roleManager.setItemToGive("Clover", cloverItems);

        // Items pour Cody
        ItemStack codyItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta codyMeta = codyItem.getItemMeta(); // Obtenir l'ItemMeta
        if (codyMeta != null) {
            codyMeta.setDisplayName(ChatColor.GREEN + "Ame de Gentillesse");
            codyItem.setItemMeta(codyMeta); // Appliquer l'ItemMeta à l'ItemStack
        }
        List<ItemStack> codyItems = new ArrayList<>();
        codyItems.add(codyItem);

        // Ajouter les potions jetables
        ItemStack weaknessPotion = new ItemStack(Material.POTION, 1, (short) 16386); // Potion de Weakness
        ItemStack poisonPotion = new ItemStack(Material.POTION, 1, (short) 16388); // Potion de Poison
        ItemStack healingPotion1 = new ItemStack(Material.POTION, 1, (short) 16373); // Potion de Soin (Instant Health)
        ItemStack healingPotion2 = new ItemStack(Material.POTION, 1, (short) 16373); // Potion de Soin (Instant Health)

        codyItems.add(weaknessPotion);
        codyItems.add(poisonPotion);
        codyItems.add(healingPotion1);
        codyItems.add(healingPotion2);

        roleManager.setItemToGive("Cody", codyItems);

        // Items pour Erik
        ItemStack erikItem1 = new ItemStack(Material.NETHER_STAR);
        ItemMeta erikMeta1 = erikItem1.getItemMeta(); // Obtenir l'ItemMeta
        if (erikMeta1 != null) {
            erikMeta1.setDisplayName(ChatColor.GOLD + "Charge Audacieuse");
            erikItem1.setItemMeta(erikMeta1); // Appliquer l'ItemMeta à l'ItemStack
        }

        ItemStack erikItem2 = new ItemStack(Material.NETHER_STAR);
        ItemMeta erikMeta2 = erikItem2.getItemMeta(); // Obtenir l'ItemMeta
        if (erikMeta2 != null) {
            erikMeta2.setDisplayName(ChatColor.GOLD + "Ame de Bravoure");
            erikItem2.setItemMeta(erikMeta2); // Appliquer l'ItemMeta à l'ItemStack
        }

        List<ItemStack> erikItems = new ArrayList<>();
        erikItems.add(erikItem1);
        erikItems.add(erikItem2);
        roleManager.setItemToGive("Erik", erikItems);

        // Items pour Elie
        ItemStack elieItem1 = new ItemStack(Material.NETHER_STAR);
        ItemMeta elieMeta1 = elieItem1.getItemMeta(); // Obtenir l'ItemMeta
        if (elieMeta1 != null) {
            elieMeta1.setDisplayName(ChatColor.BLUE + "Ame de Patience");
            elieItem1.setItemMeta(elieMeta1); // Appliquer l'ItemMeta à l'ItemStack
        }
        List<ItemStack> elieItems = new ArrayList<>();
        elieItems.add(elieItem1);

        // Ajouter le livre de Protection III
        ItemStack protectionBook = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta bookMeta = protectionBook.getItemMeta(); // Obtenir l'ItemMeta
        if (bookMeta != null) {
            bookMeta.setDisplayName(ChatColor.AQUA + "Livre de Protection III");
            protectionBook.setItemMeta(bookMeta); // Appliquer l'ItemMeta à l'ItemStack
        }
        elieItems.add(protectionBook);

        roleManager.setItemToGive("Elie", elieItems);
    }

    private void initializeEffects() {
        Map<PotionEffectType, Integer> alizaEffects = new HashMap<>();
        alizaEffects.put(PotionEffectType.SPEED, 20);
        alizaEffects.put(PotionEffectType.WEAKNESS, 20);
        RoleManager roleManager = UhcAPI.getInstance().getRoleManager();
        roleManager.setEffectsToGive("Aliza", alizaEffects);

    }

    public static UndertaleUHC getInstance() {
        return instance;
    }
}