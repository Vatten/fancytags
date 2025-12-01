package dev.vatten.baserad;

import dev.vatten.baserad.commands.TestCommand;
import dev.vatten.baserad.configs.LocalesConfig;
import dev.vatten.baserad.configs.PluginConfig;
import dev.vatten.baserad.events.PlayerJoinEvent;
import net.kyori.adventure.text.Component;

import java.nio.file.Path;

public class Plugin extends VattenPlugin {
    private ConfigInstance<PluginConfig> PLUGIN_CONFIG;
    private ConfigInstance<LocalesConfig> LOCALES_CONFIG;

    protected Plugin(VattenPlatform<?, ?> pluginInterface, Type type, Path path) {
        super(pluginInterface, type, path);
    }

    @Override
    protected void onEnable() {
        super.onEnable();

        PLUGIN_CONFIG = new ConfigInstance<>(this, "config", PluginConfig.class);
        LOCALES_CONFIG = new ConfigInstance<>(this, "locales", LocalesConfig.class);

        registerCommands(
                // Commands here
                new TestCommand(this)
        );

        getEventHandler().registerEventHandler(PlayerJoinEvent.class, this::onJoin);
    }

    @Override
    protected void reload() {
        super.reload();

        PLUGIN_CONFIG.load();
        LOCALES_CONFIG.load();
    }

    private void onJoin(PlayerJoinEvent event) {
        event.getPlayer().sendMessage(Component.text("hello " + event.getPlayer().getName()));
        ScheduledTask<?> task = pluginInterface.scheduleRepeatingTask(() -> {
            event.getPlayer().sendMessage(Component.text("Hello from repeating task"));
        }, 0, 5000);
        pluginInterface.scheduleTask(() -> {
            event.getPlayer().sendMessage(Component.text("Hello from task"));
            task.cancel();
        }, 15000);
    }
}