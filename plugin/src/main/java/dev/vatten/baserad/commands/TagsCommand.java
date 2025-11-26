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

import dev.vatten.baserad.*;
import dev.vatten.baserad.interfaces.RenderableComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TagsCommand extends Command {
    private final GenerateCommand generateCommand;
    private final Plugin plugin;

    public TagsCommand(Plugin plugin, Path importDirectory) {
        super("tags", List.of("fancytags"));
        this.generateCommand = new GenerateCommand(plugin, importDirectory);
        this.plugin = plugin;
    }

    // Welcome to command hell
    // TODO: Redo commands into OOP brigadier-style commands, not whatever this thrown together pile of if statements is
    @Override
    public void execute(VattenPlayer player, String[] args) {
        TagStore tagStore = plugin.getTagStore();
        if(args.length >= 1) {
            if(args[0].equalsIgnoreCase("reload")) {
                plugin.getApi().reload();
                player.sendMessage(plugin.getPluginTextFormatter().format(TextFormatter.SUCCESS.format("Tags plugin has been reloaded!")));
                return;
            }
            if(args[0].equalsIgnoreCase("list")) {
                player.sendMessage(plugin.getPluginTextFormatter().format(TextFormatter.INFO.format("Listing all registered tags:")));
                player.sendMessage(plugin.getInterfaces().createTagListInterface(tagStore).asComponent());
                return;
            }
            if(args[0].equalsIgnoreCase("test")) {
                player.sendMessage(Component.empty());
                player.sendMessage(tagStore.getTag("team").asComponent().color(TextColor.color(0xff0000)).append(Component.text(" Vattendroppen236: I'm on the red team").color(NamedTextColor.WHITE)));
                player.sendMessage(tagStore.getTag("team").asComponent().color(TextColor.color(0x00ff00)).append(Component.text(" Vattendroppen236: I'm on the green team").color(NamedTextColor.WHITE)));
                player.sendMessage(tagStore.getTag("team").asComponent().color(TextColor.color(0x0000ff)).append(Component.text(" Vattendroppen236: I'm on the blue team").color(NamedTextColor.WHITE)));
                player.sendMessage(tagStore.getTag("team").asComponent().color(TextColor.color(0xffff00)).append(Component.text(" Vattendroppen236: I'm on the yellow team").color(NamedTextColor.WHITE)));
                player.sendMessage(Component.empty());

                player.sendMessage(Component.empty());
                player.sendMessage(tagStore.getTag("owner").asComponent().append(Component.text(" Vattendroppen236: i'm the owner")));
                player.sendMessage(tagStore.getTag("helper").asComponent().append(Component.text(" Vattendroppen237: W")));
                player.sendMessage(tagStore.getTag("twitch").asComponent().append(tagStore.getTag("dev").asComponent()).append(Component.text(" Vattendroppen238: yo nice rank Vattendroppen239")));
                player.sendMessage(tagStore.getTag("admin").asComponent().append(Component.text(" Vattendroppen239: thanks")));
                player.sendMessage(tagStore.getTag("player").asComponent().append(Component.text(" Steve: Wait how are there so many of you")));
                player.sendMessage(tagStore.getTag("player").asComponent().append(Component.text(" Steve was banned by ")).append(tagStore.getTag("mod").asComponent().append(Component.text(" Vattendroppen240"))));
                player.sendMessage(tagStore.getTag("builder").asComponent().append(Component.text(" Vattendroppen241: lmao")));
                player.sendMessage(tagStore.getTag("youtube").asComponent().append(Component.text(" Vattendroppen242: say hi to youtube")));
                player.sendMessage(Component.empty());

                player.sendMessage(Component.empty());
                player.sendMessage(RenderableComponent.centered().component(plugin.getInternalTagStore().getTag("fancytags_logo").asComponent()).build().asComponent());
                player.sendMessage(Component.empty());
                player.sendMessage(RenderableComponent.centered().component(
                        Component.text()
                                .append(plugin.getInternalTagStore().getTag("simple").asComponent().shadowColor(ShadowColor.shadowColor(0, 0, 0, 255)))
                                .append(Component.text(" & "))
                                .append(plugin.getInternalTagStore().getTag("stylish").asComponent())
                                .append(Component.text(" tags"))
                                .build()
                ).build().asComponent());
                player.sendMessage(RenderableComponent.centered().component(Component.text().append(Component.text("100%", Style.style(TextDecoration.BOLD))).append(Component.text(" Vanilla")).build()).build().asComponent());
                player.sendMessage(Component.empty());
                return;
            }
            if(args.length == 2) {
                if(args[0].equalsIgnoreCase("view")) {
                    if(!tagStore.getTagNames().contains(args[1])) {
                        player.sendMessage(TextFormatter.WARN.format("No tag corresponding to the name '" + args[1] + "' was found."));
                        return;
                    }
                    player.sendMessage(plugin.getPluginTextFormatter().format(TextFormatter.INFO.format("Viewing information about the tag '" + args[1] + "'.")));
                    player.sendMessage(plugin.getInterfaces().createTagInterface(args[1]).asComponent());
                    return;
                }
                if(args[0].equalsIgnoreCase("delete")) {
                    if(!tagStore.getTagNames().contains(args[1])) {
                        player.sendMessage(TextFormatter.WARN.format("No tag corresponding to the name '" + args[1] + "' was found."));
                        return;
                    }
                    player.sendMessage(plugin.getInterfaces().createConfirmInterface(Component.textOfChildren(Component.text("delete '" + args[1] + "' ("), TextFormatter.RESET.format(tagStore.getTag(args[1]).asComponent()), Component.text(")")),
                            (audience) -> {
                                tagStore.removeTag(args[1]);
                                player.sendMessage(plugin.getPluginTextFormatter().format(TextFormatter.SUCCESS.format("Tag '" + args[1] + "' was deleted.")));
                            }
                    ).asComponent());
                    return;
                }
            }
            if(args.length == 3) {
                if(args[0].equalsIgnoreCase("rename")) {
                    if(!tagStore.getTagNames().contains(args[1])) {
                        player.sendMessage(TextFormatter.WARN.format("No tag corresponding to the name '" + args[1] + "' was found."));
                        return;
                    }
                    if(tagStore.getTagNames().contains(args[2])) {
                        player.sendMessage(TextFormatter.WARN.format("A tag with the name '" + args[1] + "' already exists."));
                        return;
                    }
                    player.sendMessage(plugin.getInterfaces().createConfirmInterface(Component.textOfChildren(Component.text("rename '" + args[1] + "' ("), TextFormatter.RESET.format(tagStore.getTag(args[1]).asComponent()), Component.text(") to '" + args[2] + "'")),
                            (audience) -> {
                                Tag tag = tagStore.getTag(args[1]);
                                tagStore.removeTag(args[1]);
                                tagStore.addTag(args[2], tag);
                                player.sendMessage(plugin.getPluginTextFormatter().format(TextFormatter.SUCCESS.format("Tag '" + args[1] + "' was renamed to '" + args[2] + "'.")));
                            }
                    ).asComponent());
                    return;
                }
            }

            if(args[0].equalsIgnoreCase("generate")) {
                String[] newArgs = new String[args.length - 1];
                System.arraycopy(args, 1, newArgs, 0, newArgs.length);
                generateCommand.execute(player, newArgs);
                return;
            }
        }
        player.sendMessage(TextFormatter.SEVERE.format(Component.text("You're it!")));
    }

    @Override
    public List<String> onTabComplete(VattenPlayer player, String[] args) {
        List<String> completions = new ArrayList<>();
        if(args.length <= 1) {
            completions.addAll(List.of("reload", "list", "view", "rename", "delete"));
            completions.add(generateCommand.getName());
        }
        if(args.length == 2) {
            if(List.of("view", "rename", "delete").contains(args[0].toLowerCase())) {
                completions.addAll(plugin.getTagStore().getTagNames());
            }
        }
        if(args.length > 1) {
            List<String> check = new ArrayList<>();
            check.add(generateCommand.getName());
            if(check.contains(args[0])) {
                String[] newArgs = new String[args.length - 1];
                System.arraycopy(args, 1, newArgs, 0, newArgs.length);
                return generateCommand.onTabComplete(player, newArgs);
            }
        }
        return completions.stream().filter(s -> args.length == 0 || s.startsWith(args[args.length - 1].toLowerCase())).toList();
    }
}
