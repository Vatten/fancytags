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
import dev.vatten.baserad.events.PlayerLeaveEvent;
import dev.vatten.baserad.events.PlayerLoadInEvent;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.event.player.PlayerClientLoadedWorldEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

public class PaperVattenPlatform extends JavaPlugin implements VattenPlatform<Player, BukkitTask>, Listener {
    private VattenPlugin plugin;

    @Override
    public void onEnable() {
        this.plugin = VattenPlugin.builder()
                .setPlatformInterface(this)
                .setType(VattenPlugin.Type.SERVER)
                .setDataDirectory(this.getDataFolder().toPath())
                .build();

        getServer().getPluginManager().registerEvents(this, this);

//        int pluginId = 27900;
//        Metrics metrics = new Metrics(this, pluginId);
    }

    @Override
    public BukkitScheduledTask scheduleTask(Runnable runnable, int delay) {
        return new BukkitScheduledTask(getServer().getScheduler().runTaskLater(this, runnable, delay/50));
    }

    @Override
    public BukkitScheduledTask scheduleRepeatingTask(Runnable runnable, int delay, int period) {
        return new BukkitScheduledTask(getServer().getScheduler().runTaskTimer(this, runnable, delay/50, period/50));
    }

    @Override
    public void registerCommand(Command command) {
        registerCommand(command.getName(), command.getAliases(), new BasicCommand() {
            @Override
            public void execute(CommandSourceStack commandSourceStack, String[] args) {
                command.execute(plugin.getPlayer(((Player) commandSourceStack.getSender()).getUniqueId()), args);
            }

            @Override
            public Collection<String> suggest(CommandSourceStack commandSourceStack, String[] args) {
                return command.onTabComplete(plugin.getPlayer(((Player) commandSourceStack.getSender()).getUniqueId()), args);
            }

            @Override
            public @Nullable String permission() {
                return command.permission();
            }
        });
    }

    @Override
    public VattenPlayer wrapPlayer(Player player) {
        VattenPlayer existingPlayer = plugin.getPlayer(player.getUniqueId());
        if(existingPlayer != null) return existingPlayer;
        return new VattenPlayer(player.getUniqueId(), player.getName(), player);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getEventHandler().dispatchEvent(new dev.vatten.baserad.events.PlayerJoinEvent(wrapPlayer(event.getPlayer())));
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        plugin.getEventHandler().dispatchEvent(new PlayerLeaveEvent(wrapPlayer(event.getPlayer())));
    }

    @EventHandler
    public void onPlayerLoadedIn(PlayerClientLoadedWorldEvent event) {
        plugin.getEventHandler().dispatchEvent(new PlayerLoadInEvent(wrapPlayer(event.getPlayer())));
    }
}
