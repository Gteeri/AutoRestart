package dev.gteeri.autorestart.lang;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Messages {

    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    private final Map<String, String> messages = new HashMap<>();

    public void load(File langFile) {
        messages.clear();
        YamlConfiguration config = YamlConfiguration.loadConfiguration(langFile);
        for (String key : config.getKeys(false)) {
            messages.put(key, config.getString(key, ""));
        }
    }

    public String getRaw(String key) {
        return messages.getOrDefault(key, key);
    }

    public String getRaw(String key, String... placeholders) {
        String message = getRaw(key);
        for (int i = 0; i < placeholders.length - 1; i += 2) {
            message = message.replace(placeholders[i], placeholders[i + 1]);
        }
        return message;
    }

    public Component get(String key) {
        return SERIALIZER.deserialize(getRaw("prefix") + getRaw(key));
    }

    public Component get(String key, String... placeholders) {
        return SERIALIZER.deserialize(getRaw("prefix") + getRaw(key, placeholders));
    }

    public Component format(String text) {
        return SERIALIZER.deserialize(text);
    }

    public Component format(String text, String... placeholders) {
        String message = text;
        for (int i = 0; i < placeholders.length - 1; i += 2) {
            message = message.replace(placeholders[i], placeholders[i + 1]);
        }
        return SERIALIZER.deserialize(message);
    }
}
