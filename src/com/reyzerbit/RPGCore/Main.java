package com.reyzerbit.RPGCore;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.reyzerbit.RPGCore.DataStructures.RPGPlayer;
import com.reyzerbit.RPGCore.Events.Core;

import net.milkbowl.vault.permission.Permission;

public class Main extends JavaPlugin {
	
	// Used to determine if config values are whitelisted, blacklisted, or neither
	public enum protectValues {
		
		WHITE, BLACK, NONE
		
	}
	
	//Config
	public static FileConfiguration config;
	public static File configFile;
	
	//ConfigVals
	public static boolean pluginEnabled;
	
	//Data Location
	public static File playerDataDir;
	
	//Data
	public static Map<UUID, RPGPlayer> playerData;
	
	public static List<String> races;
	public static List<String> classes;
	public static List<String> bodytypes;
	public static List<String> names;
	public static List<String> hometowns;
	
	public static protectValues protectedRaces;
	public static protectValues protectedClasses;
	public static protectValues protectedBodyTypes;
	public static protectValues protectedNames;
	public static protectValues protectedHometowns;
	
	public static int minAge;
	public static int maxAge;
	public static int minHeight;
	public static int maxHeight;
	
	//Messages
	public static final String enableMsg = "ENABLED!";
	public static final String disableMsg = "DISABLED!";
	public static final String errorMsg = "An internal error has occured!";
	
	//SQL
	public static String sqlName;
	public static String sqlDatabase;
	public static String sqlUser;
	public static String sqlPassword;
	public static int sqlPort;
	public static Connection sqlConnection;
	
	//Vault API
	public static Permission perms = null;
	
	// Fired when plugin is first enabled
    @Override
    public void onEnable() {
    	
		//congif.yml
    	config = this.getConfig();
    	configFile = new File(this.getDataFolder(), "config.yml");

    	//Generate files if missing
    	if(!configFile.exists()) {

    		saveResource("config.yml", false);
    		
    	}
    	
    	playerDataDir = new File(this.getDataFolder(), "playerdata");
    	
    	//Saves Init
    	IO.playerSavesConfig = new HashMap<String, FileConfiguration>();
    	playerData = new HashMap<UUID, RPGPlayer>();
    	
    	if(getServer().getPluginManager().getPlugin("Vault") == null) {
    		
    		getServer().getLogger().log(Level.SEVERE, "Unable to detect Vault! Disabling RPG Core.");
    		getServer().getPluginManager().disablePlugin(this);
    		return;
    		
    	}
		
    	setupPermissions();
    	
		reload();
		
    	this.getCommand("rpg").setExecutor(new Core());
    	
    	//WIP
    	//this.getCommand("rpg").setTabCompleter(new TabCompleterRPG());
    	
    }
    
    // Fired when plugin is disabled
    @Override
    public void onDisable() {

    	IO.save();
    	
    }
    
    @SuppressWarnings("unchecked")
	public static void reload() {
    	
    	IO.load();
    	
    	config = YamlConfiguration.loadConfiguration(configFile);
    	
    	//Enabled
    	pluginEnabled = config.getBoolean("enabled");
    	
    	//Character Constraints
    	names = (List<String>) config.getList("names");
    	hometowns = (List<String>) config.getList("hometowns");
    	races = (List<String>) config.getList("races");
    	classes = (List<String>) config.getList("classes");
    	bodytypes = (List<String>) config.getList("bodytypes");
    	
    	protectedNames = convertConfigEnums(config.getString("protectedNames"));
    	protectedHometowns = convertConfigEnums(config.getString("protectedHometowns"));
    	protectedRaces = convertConfigEnums(config.getString("protectedRaces"));
    	protectedClasses = convertConfigEnums(config.getString("protectedClasses"));
    	protectedBodyTypes = convertConfigEnums(config.getString("protectedBodyTypes"));
    	
    	minAge = config.getInt("min_age");
    	maxAge = config.getInt("max_age");
    	minHeight = config.getInt("min_height");
    	maxHeight = config.getInt("max_height");
    	
    	//SQL
    	sqlName = config.getString("hostname");
    	sqlDatabase = config.getString("database");
    	sqlUser = config.getString("username");
    	sqlPassword = config.getString("password");
    	sqlPort = config.getInt("port");
    	
    }
    
    @SuppressWarnings("unused")
	private static void sqlConnect() throws SQLException, ClassNotFoundException {
    	
    	if(sqlConnection != null && !sqlConnection.isClosed()) {
    		
    		return;
    		
    	}
    	
    	sqlConnection = DriverManager.getConnection("jdbc:mysql://"
                + sqlName + ":" + sqlPort + "/" + sqlDatabase,
                sqlUser, sqlPassword);
    	
    }

    private static protectValues convertConfigEnums(String value) {
    	
    	if(value.toLowerCase().equals("black")) {
    		
    		return protectValues.BLACK;
    		
    	} else if(value.toLowerCase().equals("white")) {
    		
    		return protectValues.WHITE;
    		
    	} else if(value.toLowerCase().equals("none")) {
    		
    		return protectValues.NONE;
    		
    	} else {
    		
    		Main.getPlugin(Main.class).getLogger().log(Level.SEVERE, "Something is wrong with your config! En error occured while reading protected values (whitelist, blacklist, none).");
    		return null;
    		
    	}
    	
    }
    
    private boolean setupPermissions() {
    	
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
        
    }
    
}
