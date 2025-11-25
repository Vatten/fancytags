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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;

public class TextFormatter {
    private final Component symbol;
    private final Style contentStyle;

    public static TextFormatter INFO = TextFormatter.builder()
//            .symbol("ℹ")
//            .styleSymbol(Style.style(TextColor.color(0x9029ff)))
            .styleContent(Style.style(TextColor.color(0xddffff)))
            .build();

    public static TextFormatter WARN = TextFormatter.builder()
            .symbol("\uD83D\uDD14")
            .styleSymbol(Style.style(TextColor.color(0xF2A347)))
            .styleContent(Style.style(TextColor.color(0xffd54a)))
            .build();

    public static TextFormatter SEVERE = TextFormatter.builder()
            .symbol("⚠")
            .styleSymbol(Style.style(TextColor.color(0xC51E0E)))
            .styleContent(Style.style(TextColor.color(0xff401f)))
            .build();

    public static TextFormatter SUCCESS = TextFormatter.builder()
            .symbol("✔")
            .styleSymbol(Style.style(TextColor.color(0x11DE24)))
            .styleContent(Style.style(TextColor.color(0x4aff6b)))
            .build();

    public static TextFormatter CASUAL = TextFormatter.builder()
            .styleContent(Style.style(NamedTextColor.GRAY))
            .build();

    public static TextFormatter RESET = TextFormatter.builder()
            .styleContent(Style.empty().color(TextColor.color(0xffffff))).build();

    TextFormatter(Component symbol, Style contentStyle) {
        this.symbol = symbol;
        this.contentStyle = contentStyle;
    }

    public Component format(String text) {
        return format(Component.text(text));
    }

    public Component format(Component component) {
        return symbol.append(component.applyFallbackStyle(contentStyle));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String symbol = "";
        private Style symbolStyle = Style.empty();
        private Style contentStyle = Style.empty();

        public Builder symbol(String symbol) {
            this.symbol = symbol;
            return this;
        }

        public Builder styleSymbol(Style symbolStyle) {
            this.symbolStyle = symbolStyle;
            return this;
        }

        public Builder styleContent(Style contentStyle) {
            this.contentStyle = contentStyle;
            return this;
        }

        public TextFormatter build() {
            return new TextFormatter(symbol.isEmpty() ? Component.empty() : Component.text(symbol, symbolStyle).appendSpace(), contentStyle);
        }
    }
}
