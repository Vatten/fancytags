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

package dev.vatten.fancytags.interfaces;

import dev.vatten.fancytags.*;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class Interfaces {
    private final Plugin plugin;

    public Interfaces(Plugin plugin) {
        this.plugin = plugin;
    }

    public Component createTagInterface(String tagName) {
        RenderableComponent.MultiLineBuilder tagInterface = RenderableComponent.multiLine();

        TagStore tagStore = plugin.getTagStore();
        Tag tag = tagStore.getTag(tagName);

        tagInterface.addLine(Component.empty());

        tagInterface.addLine(
                RenderableComponent.field()
                        .title(Component.text("Viewing tag: "))
                        .addComponent(
                                Component.text()
                                        .clickEvent(ClickEvent.copyToClipboard(tag.asMiniMessage()))
                                        .append(Component.text("⛨ ", TextColor.color(0xDA5AFF)))
                                        .append(tag.asComponent())
                                        .hoverEvent(HoverEvent.showText(Component.text("[Click]", NamedTextColor.DARK_GRAY).append(Component.text(" Copy MiniMessage format", NamedTextColor.GRAY))))
                                        .build()
                        )
                        .addComponent(Component.text("✎ " + tagName, TextColor.color(0x03dffc)))
                        .spacing(3)
                        .build().asComponent()
        );

        tagInterface.addLine(Component.empty());

        tagInterface.addLine(
                createActionsField(
                        Component.text()
                                .clickEvent(ClickEvent.suggestCommand("/tags rename " + tagName + " <new_name>"))
                                .append(
//                                                ButtonTextRenderer.builder()
//                                                        .content("✎ Rename")
//                                                        .style(Style.style(TextColor.color(0x005eff)))
//                                                        .build()
                                        plugin.getInternalTagStore().getTag("rename_btn").asComponent()
                                )
                                .hoverEvent(
                                        HoverEvent.showText(RenderableComponent.multiLine().addLines(TextFormatter.INFO.format("Rename this tag")).build().asComponent())
                                ).build(),
                        Component.text()
                                .clickEvent(ClickEvent.runCommand("/tags delete " + tagName))
                                .append(
//                                                ButtonTextRenderer.builder()
//                                                        .content("✘ Delete")
//                                                        .style(Style.style(TextColor.color(0xff401f)))
//                                                        .build()
                                        plugin.getInternalTagStore().getTag("delete_btn").asComponent()
                                )
                                .hoverEvent(
                                        RenderableComponent.multiLine().addLines(TextFormatter.INFO.format("Delete this tag"), TextFormatter.SEVERE.format("This action cannot be undone.")).build().asComponent()
                                ).build()
                ).build().asComponent()
        );

        return tagInterface.build().asComponent();
    }

    public Component createConfirmInterface(Component content, ClickCallback<Audience> callback) {
        RenderableComponent.MultiLineBuilder confirmInterface = RenderableComponent.multiLine();

        confirmInterface.addLine(Component.empty());

        confirmInterface.addLine(
                RenderableComponent.field()
                        .addComponent(
                                TextFormatter.INFO.format(
                                        Component.text()
                                                .append(Component.text("Please confirm the following: "))
                                                .appendNewline()
                                                .append(content)
                                                .build()
                                )
                        ).build().asComponent()
        );

        confirmInterface.addLine(Component.empty());

        confirmInterface.addLine(
                createActionsField(
                        Component.text()
                                .clickEvent(ClickEvent.callback(callback))
                                .append(
                                        plugin.getInternalTagStore().getTag("confirm_btn").asComponent()
                                ).build()
                ).build().asComponent()
        );

        return confirmInterface.build().asComponent();
    }

    public Component createTagListInterface(TagStore tagStore) {
        RenderableComponent.MultiLineBuilder listInterface = RenderableComponent.multiLine();

        listInterface.addLine(Component.empty());

        RenderableComponent.FieldBuilder currentField = RenderableComponent.field().title(Component.text("Tags: ")).spacing(3);
        int i = 0;
        for(String name : tagStore.getTagNames()) {
            Tag tag = tagStore.getTag(name);
            Renderable renderable = RenderableComponent.of(Component.text().append(Component.text(name + " (")).append(tag.asComponent()).append(Component.text(")")).clickEvent(ClickEvent.runCommand("/tags view " + name)).hoverEvent(RenderableComponent.multiLine().addLines(tag.asComponent(), Component.text("[Click]", NamedTextColor.DARK_GRAY).append(Component.text(" View more options", NamedTextColor.GRAY))).build().asComponent()).build());
            i++;
            if(currentField.build().estimateWidth() + renderable.estimateWidth() > 300) {
                listInterface.addLine(currentField.build().asComponent());
                currentField = RenderableComponent.field().padding(7).spacing(3);
            }
            currentField.addComponent(renderable.asComponent());
        }
        listInterface.addLine(currentField.build().asComponent());

        listInterface.addLine(Component.empty());

        return listInterface.build().asComponent();
    }

    public RenderableComponent.FieldBuilder createActionsField(Component... components) {
        return RenderableComponent.field()
                .title(Component.text("Actions: "))
                .addComponents(components)
                .spacing(3);
    }
}
