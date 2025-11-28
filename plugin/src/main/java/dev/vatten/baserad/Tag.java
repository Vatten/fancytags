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

import de.exlll.configlib.Configuration;
import dev.vatten.baserad.symbols.Symbol;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;

@Configuration
@NoArgsConstructor
public class Tag implements Renderable {
    private List<Symbol> symbols;
    private transient Component component;
    private transient String miniMessage;

    public Tag(List<Symbol> symbols) {
        this.symbols = symbols;
    }

    Component serialize(MiniMessage miniMessage) {
        TextComponent.Builder componentBuilder = Component.text();
        for(Symbol symbol : symbols) {
            componentBuilder.append(symbol.serialize());
        }
        this.component = componentBuilder.build();
        this.miniMessage = miniMessage.serialize(this.component);
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
