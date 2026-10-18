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

import de.exlll.configlib.Configuration;
import dev.vatten.baserad.symbols.Symbol;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;

import dev.vatten.baserad.symbols.PlayerSymbol;
import java.util.ArrayList;
import java.util.List;

@Configuration
@NoArgsConstructor
public class Tag implements Renderable {
    private List<Symbol> symbols;
    private String fallback;
    private transient Component component;
    private transient String miniMessage;
    private transient MiniMessage lastMiniMessage;
    private transient String lastKey;

    public Tag(List<Symbol> symbols) {
        this.symbols = symbols;
    }

    public Tag(List<Symbol> symbols, String fallback) {
        this.symbols = symbols;
        this.fallback = fallback;
    }

    public String getFallback() {
        return fallback;
    }

    public void setFallback(String fallback) {
        this.fallback = fallback;
        if (this.lastMiniMessage != null) {
            serialize(this.lastMiniMessage, this.lastKey != null ? this.lastKey : "");
        }
    }

    Component serialize(MiniMessage miniMessage) {
        return serialize(miniMessage, "");
    }

    Component serialize(MiniMessage miniMessage, String key) {
        this.lastMiniMessage = miniMessage;
        this.lastKey = key;
        TextComponent.Builder componentBuilder = Component.text();
        StringBuilder minimessageBuilder = new StringBuilder();

        List<PlayerSymbol> headSymbols = new ArrayList<>();
        for (Symbol symbol : symbols) {
            if (symbol instanceof PlayerSymbol playerSymbol) {
                headSymbols.add(playerSymbol);
            }
        }

        int numHeads = headSymbols.size();
        String fallbackStr = (this.fallback != null && !this.fallback.isEmpty()) ? this.fallback : key;

        int headIndex = 0;
        for (Symbol symbol : symbols) {
            if (symbol instanceof PlayerSymbol playerSymbol) {
                Component fallbackComponent;
                if (fallbackStr != null && !fallbackStr.isEmpty()) {
                    Component parsedFallback = miniMessage != null ? miniMessage.deserialize(fallbackStr) : Component.text(fallbackStr);
                    if (numHeads > 1) {
                        if (parsedFallback.children().size() == numHeads) {
                            fallbackComponent = parsedFallback.children().get(headIndex);
                        } else if (fallbackStr.length() == numHeads) {
                            String charStr = String.valueOf(fallbackStr.charAt(headIndex));
                            fallbackComponent = miniMessage != null ? miniMessage.deserialize(charStr) : Component.text(charStr);
                        } else {
                            if (headIndex == 0) {
                                fallbackComponent = parsedFallback;
                            } else {
                                fallbackComponent = Component.empty();
                            }
                        }
                    } else {
                        fallbackComponent = parsedFallback;
                    }
                } else {
                    fallbackComponent = Component.empty();
                }
                componentBuilder.append(playerSymbol.serialize(fallbackComponent));
                headIndex++;
            } else {
                componentBuilder.append(symbol.serialize());
            }
            minimessageBuilder.append(symbol.serializeMiniMessage());
        }
        this.component = componentBuilder.build();
        this.miniMessage = minimessageBuilder.toString();
        return this.component;
    }

    @Override
    public Component asComponent() {
        return component;
    }

    public String asMiniMessage() {
        return miniMessage;
    }
}
