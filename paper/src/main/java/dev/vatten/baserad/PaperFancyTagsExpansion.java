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

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PaperFancyTagsExpansion extends PlaceholderExpansion {
    private final Plugin plugin;

    public PaperFancyTagsExpansion(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "tags";
    }

    @Override
    public @NotNull String getAuthor() {
        return "vatten";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginInfo().version();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @NotNull List<String> getPlaceholders() {
        return plugin.getTagStore().getTagNames();
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if(plugin.getTagStore().getTagNames().contains(params)) {
            return Plugin.MINIMESSAGE.serialize(plugin.getTagStore().getTag(params).asComponent());
        }
        return null;
    }
}
