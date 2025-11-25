package dev.vatten.baserad;

import dev.vatten.baserad.commands.TestCommand;
import dev.vatten.baserad.events.PlayerJoinEvent;
import net.kyori.adventure.text.Component;

import java.nio.file.Path;

public class Plugin extends VattenPlugin {

    protected Plugin(VattenPlatform<?, ?> pluginInterface, Type type, Path path) {
        super(pluginInterface, type, path);

        registerCommands(
                // Commands here
                new TestCommand(this)
        );

        getEventHandler().registerEventHandler(PlayerJoinEvent.class, this::onJoin);
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