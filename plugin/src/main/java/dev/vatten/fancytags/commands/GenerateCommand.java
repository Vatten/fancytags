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

package dev.vatten.fancytags.commands;

import dev.vatten.fancytags.*;
import net.kyori.adventure.text.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GenerateCommand extends Command {
    private final Plugin plugin;
    private final Path importDirectory;

    public GenerateCommand(Plugin plugin, Path importDirectory) {
        super("generate", List.of());
        this.plugin = plugin;
        this.importDirectory = importDirectory;
    }

    @Override
    public void execute(VattenPlayer player, String[] args) {
        if(args.length < 3) {
            player.sendMessage(plugin.getPluginTextFormatter().format(TextFormatter.WARN.format("Usage: /tags generate <name> [url|file] <url/file>")));
            return;
        }
        String name = args[0];
        String type =  args[1];
        String path =  args[2];

        TagStore tagStore = plugin.getTagStore();
        if(tagStore.getTagNames().contains(name)) {
            player.sendMessage(plugin.getPluginTextFormatter().format(TextFormatter.WARN.format("A tag with the name '" + name + "' already exists.")));
            return;
        }

        player.sendMessage(plugin.getPluginTextFormatter().format(TextFormatter.INFO.format("Starting generation of tag image located at '" + path + "'")));
        BufferedImage image;
        try {
            if(type.equalsIgnoreCase("url")) {
                try {
                    URL url = URI.create(path).toURL();
                    image = ImageIO.read(url);

                } catch(MalformedURLException e) {
                    player.sendMessage(TextFormatter.SEVERE.format(Component.text("Malformed URL. Double check that this is correct: '" + path + "'")));
                    return;
                }
            } else if(type.equalsIgnoreCase("file")) {
                image = ImageIO.read(new File(importDirectory.resolve(path).toUri()));
            } else {
                player.sendMessage(TextFormatter.SEVERE.format(Component.text("Invalid import type '" + type + "'. Possible choices are 'url' and 'file'.")));
                return;
            }
        } catch(Exception e) {
            e.printStackTrace();
            player.sendMessage(TextFormatter.SEVERE.format(Component.text("An error occurred while trying to read the image. '" + e.getMessage() + "'. See more details in the console.")));
            return;
        }
        if(image == null) {
            player.sendMessage(TextFormatter.SEVERE.format(Component.text("Failed to read the image. Perhaps the format is not supported?")));
            return;
        }
        player.sendMessage(plugin.getPluginTextFormatter().format(TextFormatter.INFO.format("Generating valid tag symbols. This may take a while...")));
        plugin.getTagGenerator().generate(name, image, progress -> {
            player.sendMessage(TextFormatter.SUCCESS.format(Component.text( "[" + progress.getCurrent() + "/" + progress.getMax() + "] Successfully generated a tag symbol.")));
        }).thenAccept(tag -> {
            tagStore.addTag(name, tag);
            player.sendMessage(plugin.getPluginTextFormatter().format(TextFormatter.SUCCESS.format("Done!")));
            player.sendMessage(plugin.getInterfaces().createTagInterface(name).asComponent());
        }).exceptionally(e -> {
            e.printStackTrace();
            player.sendMessage(TextFormatter.SEVERE.format(Component.text("An error occurred while uploading the tag. '" + e.getMessage() + "'. See more details in the console.")));
            return null;
        });
    }

    @Override
    public List<String> onTabComplete(VattenPlayer player, String[] args) {
        List<String> completions = new ArrayList<>();
        if(args.length == 2) {
            completions.addAll(List.of("url", "file"));
        }
        return completions.stream().filter(s -> s.startsWith(args[args.length - 1].toLowerCase())).toList();
    }
}
