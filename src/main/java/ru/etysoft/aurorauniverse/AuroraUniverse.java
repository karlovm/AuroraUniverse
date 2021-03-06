package ru.etysoft.aurorauniverse;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Event;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import ru.etysoft.aurorauniverse.commands.EconomyCommands;
import ru.etysoft.aurorauniverse.commands.PluginCommands;
import ru.etysoft.aurorauniverse.commands.TownCommands;
import ru.etysoft.aurorauniverse.commands.TownTabCompliter;
import ru.etysoft.aurorauniverse.data.DataManager;
import ru.etysoft.aurorauniverse.economy.AuroraEconomy;
import ru.etysoft.aurorauniverse.listeners.PluginListener;
import ru.etysoft.aurorauniverse.listeners.ProtectionListener;
import ru.etysoft.aurorauniverse.permissions.AuroraPermissions;
import ru.etysoft.aurorauniverse.utils.AuroraConfiguration;
import ru.etysoft.aurorauniverse.utils.LanguageSetup;
import ru.etysoft.aurorauniverse.utils.Timer;
import ru.etysoft.aurorauniverse.world.Region;
import ru.etysoft.aurorauniverse.world.Resident;
import ru.etysoft.aurorauniverse.world.Town;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class AuroraUniverse extends JavaPlugin {

    public static Map<String, Town> townlist = new ConcurrentHashMap<>();
    public static Map<Chunk, Region> alltownblocks = new ConcurrentHashMap<>();
    public static Map<String, Resident> residentlist = new ConcurrentHashMap<>();
    public static int minTownBlockDistanse = 1;
    public static boolean debugmode = true;

    private static String warnings = "";
    private static String prefix = ChatColor.GRAY + "[" + ChatColor.AQUA + "AuroraUniverse" + ChatColor.GRAY +"]" + ChatColor.RESET;
    private AuroraEconomy auroraEconomy;
    private static AuroraUniverse instance;
    private File languagefile;
    private static FileConfiguration language;
    private static boolean haswarnings = false;

    private final static String langver = "0.1.1.0";
    private final static String confver = "0.1.0.0";
    private final static String permsver = "0.1.0.0";

    public AuroraEconomy getEconomy()
    {
        return auroraEconomy;
    }

    @Override
    public void onEnable() {
        Logger.info(">> &bAuroraUniverse &r" + getDescription().getVersion() + " by " + getDescription().getAuthors() + "<<");
        Timer timer = new Timer();
        instance = this;

        try {
            Logger.info("Loading configuration...");
            saveDefaultConfig();
            String w1 = setupLanguageFile();
            if (!w1.equals("ok")) {
                addWarning(w1);
            }
            prefix = AuroraConfiguration.getColorString("prefix");
            if (getConfig().contains("file-version")) {
                try {
                    if (!getConfig().getString("file-version").equals(confver)) {
                        addWarning("&eOutdated configuration file!");
                    }
                    if (language.contains("file-version")) {
                        if (!AuroraConfiguration.getColorString("file-version").equals(langver)) {
                            addWarning("&eOutdated language file!");
                        }
                    } else {
                        addWarning("&eCan't find file-version in language file! You can add it manually with new params and plugin version(" + getDescription().getVersion() + ")");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    addWarning("&eERROR: " + e.getMessage());
                }
            } else {
                addWarning("&eCan't find file-version in config.yml!");
            }
            if (haswarnings) {
                Logger.info("&eConfiguration loaded with warnings.");
            }
        }
        catch (Exception e)
        {
            addWarning("&cCONFIGURATION ERROR(Probably language file is outdated): " + e.getMessage());
            if(AuroraConfiguration.getDebugMode())
            {
                Logger.debug("Configuration error: ");
                e.printStackTrace();
            }
        }

        // LISTENERS
        Logger.info("Initializing listeners and commands...");
        try
        {
            registerListeners();
            registerCommands();
        }
        catch (Exception e)
        {
            addWarning("&cLISTENERS ERROR: " + e.getMessage());
        }
        Logger.info("Initializing AuroraPemissions...");
        try {
            AuroraPermissions.initialize();
        } catch (Exception e) {
            addWarning("&cAPERMS ERROR: " + e.getMessage());
        }

        Logger.info("Initializing AuroraEconomy...");
        try
        {
            auroraEconomy = new AuroraEconomy();
        }
        catch (Exception e){
            if(AuroraConfiguration.getDebugMode())
            {
                Logger.debug("Can't create EconomyCore:");
                e.printStackTrace();
            }
        }
        if (!setupEconomy()) {
            addWarning("Can't find Vault! Economy can't start!.");
            return;
        }


       if(!haswarnings)
       {
           //If no warnings
           Logger.info("AuroraUniverse successfully enabled in " + timer.getStringSeconds().substring(0, 4) + " seconds!");
       }
       else
       {
           //Some warnings catched
           Logger.info("&cAuroraUniverse was enabled with warnings in " + timer.getStringSeconds().substring(0, 4) + " seconds: &e" + warnings);
       }
        if(AuroraConfiguration.getDebugMode())
        {
            Logger.debug("You running AuroraUniverse in debug mode (more console messages)");
        }

    }

    public String setupLanguageFile() {
        boolean ok = true;
        languagefile = new File( AuroraUniverse.getInstance().getDataFolder(),  AuroraUniverse.getInstance().getConfig().getString("language-file"));
        if (!languagefile.exists()) {
            try
            {
                languagefile.getParentFile().mkdirs();
                AuroraUniverse.getInstance().saveResource(AuroraUniverse.getInstance().getConfig().getString("language-file"), false);
            }
            catch (Exception e)
            {
                AuroraUniverse.getInstance().saveResource("english.yml", false);
                languagefile = new File( AuroraUniverse.getInstance().getDataFolder(), "english.yml");
                languagefile.getParentFile().mkdirs();






                ok = false;
            }
        }

        language = new YamlConfiguration();
        try {
            language.load(languagefile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        LanguageSetup.setup(language);

        if(ok)
        {
            return "ok";
        }
        else
        {
            File file2 = new File( AuroraUniverse.getInstance().getDataFolder(), AuroraUniverse.getInstance().getConfig().getString("language-file"));

            try
            {
                Files.copy(AuroraUniverse.getInstance().getResource("english.yml"), Paths.get(file2.getAbsolutePath()));
            }
            catch (Exception e2)
            {
                e2.printStackTrace();
            }
            return "Looks like you tried to use new language file, but that file doesn't exists. Using english.yml";
        }

    }

    public static String getWarnings()
    {
        return warnings;
    }


    private void addWarning(String s)
    {
        haswarnings = true;
        warnings += "\n" + s;
    }

    public static String getPrefix()
    {
        return prefix;
    }

    public static void callEvent(Event event) {
        Bukkit.getPluginManager().callEvent(event);
    }

    public static Map<String, Town> getTownlist()
    {
        return townlist;
    }

    public static Map<Chunk, Region> getTownBlocks()
    {
        return alltownblocks;
    }

    public static AuroraUniverse getInstance()
    {
        return instance;
    }

    public static FileConfiguration getLanguage() {
        return language;
    }

    private void registerCommands()
    {
        registerCommand("auntown", new TownCommands(), new TownTabCompliter());
        registerCommand("aurorauniverse", new PluginCommands(), null);
        registerCommand("auneco", new EconomyCommands(), null);
    }

    private void registerListeners()
    {
       registerListener(new PluginListener());
        registerListener(new ProtectionListener());
    }


    private boolean registerCommand(String name, CommandExecutor executor, TabCompleter tabCompleter) {
        try
        {
            PluginCommand command = getCommand(name);
            command.setExecutor(executor);
            if (tabCompleter != null) {
                command.setTabCompleter(tabCompleter);
            }
            if(AuroraConfiguration.getDebugMode())
            {
                Logger.debug("Registered command &b/" + name);
            }
            return true;
        }
       catch (Exception e)
       {
           Logger.error("Can't register /" + name + " command!");
           return false;
       }
    }

    private void registerListener(org.bukkit.event.Listener listener) {
        if(AuroraConfiguration.getDebugMode())
        {
            Logger.debug("Registered listener &b" + listener.getClass().getSimpleName());
        }
        getServer().getPluginManager().registerEvents(listener, this);
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        getServer().getServicesManager().register(Economy.class, auroraEconomy, this, ServicePriority.Highest);
        Logger.info("Economy has been registered.");
        return true;
    }

    //Disabling
    @Override
    public void onDisable() {
        Logger.info("Disabling AuroraUniverse...");
        DataManager.saveTowns("towns.json");
        Logger.info("AuroraUniverse successfully disabled!");
    }
}
