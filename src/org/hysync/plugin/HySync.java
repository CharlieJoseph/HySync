package org.hysync.plugin;

import co.aikar.commands.BukkitCommandManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.hysync.plugin.command.AddKeyCommand;
import org.hysync.plugin.command.ListKeyCommand;
import org.hysync.plugin.event.KeyWelcomeEvent;
import org.hysync.plugin.message.ConfigWrapper;
import org.hysync.plugin.message.Lang;
import org.hysync.plugin.storage.Key;
import org.hysync.plugin.storage.KeyManager;

import java.util.UUID;

public class HySync extends JavaPlugin {
    private KeyManager keyManager;
    private BukkitCommandManager commandManager;
    private ConfigWrapper keyConfig;

    public KeyManager getKeyManager() {
        return keyManager;
    }

    public BukkitCommandManager getCommandManager() {
        return commandManager;
    }

    public ConfigWrapper getKeyConfig() {
        return keyConfig;
    }

    // Setup
    public UUID activeSetupUser;

    @Override
    public void onEnable(){
        activeSetupUser = null;

        // Config Handling
        keyConfig = new ConfigWrapper(this, "keys.yml");
        Lang.init(new ConfigWrapper(this, "lang.yml"));

        // Key Manager
        keyManager = new KeyManager(this);
        loadKeys();

        // Events
        new KeyWelcomeEvent(this);

        // Commands
        commandManager = new BukkitCommandManager(this);
        new AddKeyCommand(this);
        new ListKeyCommand(this);
    }

    private void loadKeys(){
        FileConfiguration keyConfigFile = keyConfig.getConfig();
        if(keyConfigFile.getConfigurationSection("storage") == null){
            getLogger().warning("No API Keys have been configured.");
            return;
        }
        for(String keyId : keyConfigFile.getConfigurationSection("storage").getKeys(false)){
            KeyManager.getKeys().add(new Key(UUID.fromString(keyId),
                    UUID.fromString(keyConfigFile.getString("storage."+keyId+".added-by"))));
            getLogger().info("Loading '" + keyId + "'");
        }
        int keys = KeyManager.getKeys().size();
        getLogger().info("Registered " + keys + " API " + (keys > 1 ? "Keys" : "Key") + ".");
    }

    private void unloadKeys(){
        FileConfiguration keyConfigFile = keyConfig.getConfig();
        KeyManager.getKeys().forEach(key -> {
            keyConfigFile.set("storage."+key.getKeyUuid()+".added-by", key.getPlayerUuid().toString());
            getLogger().info("Unloading '" + key.getKeyUuid() + "'");
        });
        keyConfig.saveConfig();
    }

    @Override
    public void onDisable(){
        unloadKeys();
    }
}