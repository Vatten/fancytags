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
import dev.vatten.baserad.events.PlayerJoinEvent;
import dev.vatten.baserad.events.PlayerLeaveEvent;
import lombok.AccessLevel;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class VattenPlugin {

    @Getter(AccessLevel.PACKAGE)
    protected final EventHandler eventHandler = new EventHandler(this);

    protected final VattenPlatform<?, ?> pluginInterface;
    @Getter
    protected final PluginInfo pluginInfo;

    @Getter
    protected final API api = new API();
    @Getter(AccessLevel.PACKAGE)
    protected final Path path;
    @Getter(AccessLevel.PACKAGE)
    protected final Map<UUID, VattenPlayer> players = new HashMap<>();

    protected VattenPlugin(VattenPlatform<?, ?> pluginInterface, Type type, Path path) {
        this.pluginInterface = pluginInterface;
        this.path = path;

        Properties p = new Properties();
        try {
            p.load(VattenPlugin.class.getResourceAsStream("/plugin.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.pluginInfo = new PluginInfo(p.getProperty("name"), p.getProperty("displayName"), p.getProperty("version"), type);

        onEnable();
        reload();
    }

    VattenPlayer getPlayer(UUID uuid) {
        return players.get(uuid);
    }

    protected void onEnable() {
        eventHandler.registerEventHandler(PlayerJoinEvent.class, this::onPlayerJoin);
        eventHandler.registerEventHandler(PlayerLeaveEvent.class, this::onPlayerLeave);
    }

    protected void reload() {

    }

    protected void registerCommands(Command... commands) {
        for (Command command : commands) {
            pluginInterface.registerCommand(command);
        }
    }

    private void onPlayerJoin(PlayerJoinEvent event) {
        players.put(event.getPlayer().getUuid(), event.getPlayer());
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
}