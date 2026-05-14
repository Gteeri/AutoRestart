package dev.gteeri.autorestart;

import dev.gteeri.autorestart.commands.RestartCommand;
import dev.gteeri.autorestart.lang.Messages;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class AutoRestart extends JavaPlugin {

    private static AutoRestart instance;
    private Messages messages;
    private RestartScheduler scheduler;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        saveResources();

        messages = new Messages();
        loadMessages();

        scheduler = new RestartScheduler(this);
        scheduler.start();

        RestartCommand command = new RestartCommand(this);
        getCommand("autorestart").setExecutor(command);
        getCommand("autorestart").setTabCompleter(command);

        getLogger().info("AutoRestart enabled.");
    }

    @Override
    public void onDisable() {
        if (scheduler != null) {
            scheduler.stop();
        }
        getLogger().info("AutoRestart disabled.");
    }

    public void reload() {
        reloadConfig();
        loadMessages();
        scheduler.stop();
        scheduler.start();
    }

    private void loadMessages() {
        String lang = getConfig().getString("language", "en");
        File langFile = new File(getDataFolder(), "lang/" + lang + ".yml");
        if (!langFile.exists()) {
            langFile = new File(getDataFolder(), "lang/en.yml");
        }
        messages.load(langFile);
    }

    private void saveResources() {
        if (!new File(getDataFolder(), "lang/en.yml").exists()) {
            saveResource("lang/en.yml", false);
        }
        if (!new File(getDataFolder(), "lang/ru.yml").exists()) {
            saveResource("lang/ru.yml", false);
        }
    }

    public Messages getMessages() {
        return messages;
    }

    public RestartScheduler getScheduler() {
        return scheduler;
    }

    public static AutoRestart getInstance() {
        return instance;
    }
}
