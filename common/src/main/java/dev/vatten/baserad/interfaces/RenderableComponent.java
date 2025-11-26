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

package dev.vatten.baserad.interfaces;

import dev.vatten.baserad.Renderable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;

import java.util.ArrayList;
import java.util.List;

public class RenderableComponent implements Renderable {
    private final Component component;

    protected RenderableComponent(Component component) {
        this.component = component;
    }

    public static RenderableComponent of(Component component) {
        return new RenderableComponent(component);
    }

    public static RenderableComponent of(String text) {
        return of(Component.text(text));
    }

    public static RenderableComponent of(Renderable renderable) {
        return of(renderable.asComponent());
    }

    public static Renderable emptyLine() {
        return of(Component.empty());
    }

    public static MultiLineBuilder multiLine() {
        return new MultiLineBuilder();
    }

    public static FieldBuilder field() {
        return new FieldBuilder();
    }

    public static CenteredBuilder centered() {
        return new CenteredBuilder();
    }

    @Override
    public Component asComponent() {
        return this.component;
    }

    public static class MultiLineBuilder {
        private final List<Component> lines = new ArrayList<>();

        public MultiLineBuilder addLine(Component component) {
            this.lines.add(component);
            return this;
        }

        public MultiLineBuilder addLines(Component... components) {
            for(Component component : components) {
                addLine(component);
            }
            return this;
        }

        public RenderableComponent build() {
            TextComponent.Builder componentBuilder = Component.text();
            for(int i = 0; i < lines.size(); i++) {
                componentBuilder.append(lines.get(i));
                if(i+1 < lines.size()) {
                    componentBuilder = componentBuilder.appendNewline();
                }
            }
            return RenderableComponent.of(componentBuilder.build());
        }
    }

    public static class FieldBuilder {
        private Component title = Component.empty();
        private List<Component> components = new ArrayList<>();
        private int spacing = 1;
        private int padding = 0;

        public FieldBuilder title(Component title) {
            this.title = title.applyFallbackStyle(Style.style(NamedTextColor.GRAY));
            return this;
        }

        public FieldBuilder addComponents(Component... components) {
            for(Component component : components) {
                addComponent(component);
            }
            return this;
        }

        public FieldBuilder addComponent(Component component) {
            this.components.add(component);
            return this;
        }

        public int size() {
            return this.components.size();
        }

        public FieldBuilder spacing(int spacing) {
            this.spacing = spacing;
            return this;
        }

        public FieldBuilder padding(int padding) {
            this.padding = padding;
            return this;
        }

        public RenderableComponent build() {
            TextComponent.Builder componentBuilder = Component.text().append(Component.text(" ".repeat(this.padding))).append(title);
            for(int i = 0; i < this.components.size(); i++) {
                if(i > 0) componentBuilder.append(Component.text(" ".repeat(this.spacing)));
                componentBuilder.append(this.components.get(i).asComponent());
            }
            return RenderableComponent.of(componentBuilder.build());
        }
    }

    public static class CenteredBuilder {
        private Renderable renderable;

        public CenteredBuilder component(Renderable renderable) {
            this.renderable = renderable;
            return this;
        }

        public CenteredBuilder component(Component component) {
            return component(RenderableComponent.of(component));
        }

        public RenderableComponent build() {
            int padding = Math.max(0, 80 - renderable.estimateWidth() / 4) / 2;
            Component component = renderable.asComponent();
            if(padding > 0) {
                component = Component.text(" ".repeat(padding)).append(component);
            }
            return RenderableComponent.of(component);
        }
    }
}
