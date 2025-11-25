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

package dev.vatten.baserad.commands;

import dev.vatten.baserad.VattenPlayer;
import dev.vatten.baserad.VattenPlugin;
import lombok.Getter;

import java.util.List;
import java.util.Locale;

public abstract class Command {
    @Getter
    protected final VattenPlugin plugin;
    @Getter
    protected final String name;
    @Getter
    protected final List<String> aliases;

    public Command(String name, List<String> aliases, VattenPlugin plugin) {
        this.name = name;
        this.aliases = aliases.stream().map(s -> s.toLowerCase(Locale.ENGLISH)).toList();
        this.plugin = plugin;
    }

    public abstract void execute(VattenPlayer player, String[] args);

    public List<String> onTabComplete(VattenPlayer player, String[] args) {
        return List.of();
    }

    public String permission() {
        return "vattenbaserad.command";
    }
}
