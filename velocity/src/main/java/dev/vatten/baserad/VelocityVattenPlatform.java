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

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import dev.vatten.baserad.commands.Command;
import dev.vatten.baserad.events.PlayerJoinEvent;
import dev.vatten.baserad.events.PlayerLeaveEvent;
import dev.vatten.baserad.events.PlayerLoadInEvent;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class VelocityVattenPlatform implements VattenPlatform<Player, ScheduledTask> {
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private VattenPlugin plugin;

    @Inject
    public VelocityVattenPlatform(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        this.plugin = VattenPlugin.builder()
                .setPlatformInterface(this)
                .setType(VattenPlugin.Type.PROXY)
                .setDataDirectory(this.dataDirectory)
                .build();
    }

    @Override
    public dev.vatten.baserad.ScheduledTask<ScheduledTask> scheduleTask(Runnable runnable, int delay) {
        return new VelocityScheduledTask(server.getScheduler().buildTask(this, runnable).delay(delay, TimeUnit.MILLISECONDS).schedule());
    }

    @Override
    public dev.vatten.baserad.ScheduledTask<ScheduledTask> scheduleRepeatingTask(Runnable runnable, int delay, int period) {
        return new VelocityScheduledTask(server.getScheduler().buildTask(this, runnable).delay(delay, TimeUnit.MILLISECONDS).repeat(period, TimeUnit.MILLISECONDS).schedule());
    }

    @Override
    public void registerCommand(Command command) {
        CommandManager commandManager = server.getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder(command.getName())
                .aliases(command.getAliases().toArray(String[]::new))
                .plugin(this)
                .build();
        commandManager.register(commandMeta, new SimpleCommand() {
            @Override
            public void execute(Invocation invocation) {
                command.execute(wrapPlayer((Player) invocation.source()), invocation.arguments());
            }

            @Override
            public List<String> suggest(Invocation invocation) {
                return command.onTabComplete(wrapPlayer((Player) invocation.source()), invocation.arguments());
            }

            @Override
            public boolean hasPermission(Invocation invocation) {
                return invocation.source().hasPermission(command.permission());
            }
        });
    }

    @Override
    public VattenPlayer wrapPlayer(Player player) {
        VattenPlayer existingPlayer = plugin.getPlayer(player.getUniqueId());
        if(existingPlayer != null) return existingPlayer;
        return new VattenPlayer(player.getUniqueId(), player.getUsername(), player);
    }

    @Subscribe
    public void onPlayerJoin(ServerConnectedEvent event) {
        plugin.getEventHandler().dispatchEvent(new PlayerJoinEvent(wrapPlayer(event.getPlayer())));
    }

    @Subscribe
    public void onPlayerLeave(DisconnectEvent event) {
        plugin.getEventHandler().dispatchEvent(new PlayerLeaveEvent(wrapPlayer(event.getPlayer())));
    }

    @Subscribe
    public void onPlayerPostConnect(ServerPostConnectEvent event) {
        plugin.getEventHandler().dispatchEvent(new PlayerLoadInEvent(wrapPlayer(event.getPlayer())));
    }
}
