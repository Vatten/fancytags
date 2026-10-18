/*
 *    Copyright 2026 vatten <vatten.dev>
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
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.Nullable;
import space.arim.morepaperlib.MorePaperLib;

import java.util.Collection;

public class PaperVattenPlatform extends JavaPlugin implements VattenPlatform<Player, space.arim.morepaperlib.scheduling.ScheduledTask>, Listener {
    private VattenPlugin plugin;
    private MorePaperLib morePaperLib;
    private boolean papiRegistered;
    private boolean miniPlaceholdersRegistered;

    @Override
    public void onEnable() {
        this.morePaperLib = new MorePaperLib(this);
        this.plugin = new Plugin(
                this,
                VattenPlugin.Type.SERVER,
                this.getDataFolder().toPath()
        );

        getServer().getPluginManager().registerEvents(this, this);

        registerExpansions();

        int pluginId = 27900;
        Metrics metrics = new Metrics(this, pluginId);
    }

    @Override
    public BukkitScheduledTask scheduleTask(Runnable runnable, int delay) {
        long initialDelayTicks = Math.max(1L, delay / 50L);
        return new BukkitScheduledTask(morePaperLib.scheduling().globalRegionalScheduler().runDelayed(runnable, initialDelayTicks));
    }

    @Override
    public BukkitScheduledTask scheduleRepeatingTask(Runnable runnable, int delay, int period) {
        long initialDelayTicks = Math.max(1L, delay / 50L);
        return new BukkitScheduledTask(morePaperLib.scheduling().globalRegionalScheduler().runAtFixedRate(runnable, initialDelayTicks, period / 50L));
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

    private void registerExpansions() {
        if (!papiRegistered && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PaperFancyTagsExpansion((Plugin) this.plugin).register();
            papiRegistered = true;
        }

        if (!miniPlaceholdersRegistered && Bukkit.getPluginManager().isPluginEnabled("MiniPlaceholders")) {
            ((Plugin) this.plugin).registerMiniPlaceholders();
            miniPlaceholdersRegistered = true;
        }
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        String name = event.getPlugin().getName();
        if ("PlaceholderAPI".equals(name) || "MiniPlaceholders".equals(name)) {
            registerExpansions();
        }
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

    public VattenPlugin getPlugin() {
        return this.plugin;
    }
}
