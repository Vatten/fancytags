/*
 *    Copyright 2025 vatten <vatten.dev>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package dev.vatten.baserad;

import dev.vatten.baserad.commands.Command;
import dev.vatten.baserad.commands.impl.TestCommand;
import dev.vatten.baserad.configs.LocalesConfig;
import dev.vatten.baserad.configs.PluginConfig;
import dev.vatten.baserad.events.PlayerJoinEvent;
import dev.vatten.baserad.events.PlayerLeaveEvent;
import lombok.AccessLevel;
import lombok.Getter;
import net.kyori.adventure.text.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class VattenPlugin {
    private final ConfigInstance<PluginConfig> PLUGIN_CONFIG;
    private final ConfigInstance<LocalesConfig> LOCALES_CONFIG;

    @Getter(AccessLevel.PACKAGE)
    private final EventHandler eventHandler = new EventHandler(this);

    private final VattenPlatform<?, ?> pluginInterface;
    @Getter
    private final PluginInfo pluginInfo;

    @Getter
    private final API api = new API();
    @Getter(AccessLevel.PACKAGE)
    private final Path path;
    @Getter(AccessLevel.PACKAGE)
    private final Map<UUID, VattenPlayer> players = new HashMap<>();

    private VattenPlugin(VattenPlatform<?, ?> pluginInterface, Type type, Path path) {
        this.pluginInterface = pluginInterface;
        this.path = path;

        Properties p = new Properties();
        try {
            p.load(VattenPlugin.class.getResourceAsStream("/plugin.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.pluginInfo = new PluginInfo(p.getProperty("name"), p.getProperty("displayName"), p.getProperty("version"), type);

        PLUGIN_CONFIG = new ConfigInstance<>(this, "config", PluginConfig.class);
        LOCALES_CONFIG = new ConfigInstance<>(this, "locales", LocalesConfig.class);

        reload();

        registerCommands(
                // Commands here
                new TestCommand(this)
        );

        eventHandler.registerEventHandler(PlayerJoinEvent.class, this::onPlayerJoin);
        eventHandler.registerEventHandler(PlayerLeaveEvent.class, this::onPlayerLeave);
    }

    VattenPlayer getPlayer(UUID uuid) {
        return players.get(uuid);
    }

    private void reload() {
        PLUGIN_CONFIG.load();
        LOCALES_CONFIG.load();
    }

    private void registerCommands(Command... commands) {
        for (Command command : commands) {
            pluginInterface.registerCommand(command);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private void onPlayerJoin(PlayerJoinEvent event) {
        players.put(event.getPlayer().getUuid(), event.getPlayer());
        event.getPlayer().sendMessage(Component.text("hello " + event.getPlayer().getName()));
        ScheduledTask<?> task = pluginInterface.scheduleRepeatingTask(() -> {
            event.getPlayer().sendMessage(Component.text("Hello from repeating task"));
        }, 0, 5000);
        pluginInterface.scheduleTask(() -> {
            event.getPlayer().sendMessage(Component.text("Hello from task"));
            task.cancel();
        }, 15000);
    }

    private void onPlayerLeave(PlayerLeaveEvent event) {
        players.remove(event.getPlayer().getUuid());
    }

    public enum Type {
        SERVER,
        PROXY
    }

    public class API {
        public void reload() {
            VattenPlugin.this.reload();
        }
    }

    public static class Builder {
        private VattenPlatform<?, ?> platformInterface;
        private Type type;
        private Path dataDirectory;

        public Builder setPlatformInterface(VattenPlatform<?, ?> platformInterface) {
            this.platformInterface = platformInterface;
            return this;
        }

        public Builder setType(Type type) {
            this.type = type;
            return this;
        }

        public Builder setDataDirectory(Path dataDirectory) {
            this.dataDirectory = dataDirectory;
            return this;
        }

        public VattenPlugin build() {
            if(platformInterface == null || type == null || dataDirectory == null) throw new NullPointerException();
            return new VattenPlugin(platformInterface, type, dataDirectory);
        }
    }
}