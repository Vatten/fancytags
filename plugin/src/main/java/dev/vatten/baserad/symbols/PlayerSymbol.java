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

package dev.vatten.baserad.symbols;

import de.exlll.configlib.Configuration;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;

@Configuration
@NoArgsConstructor
public class PlayerSymbol extends Symbol {
    private String value;
    private String signature;

    private static final boolean HAS_FALLBACK;
    static {
        boolean hasMethod = false;
        try {
            net.kyori.adventure.text.ObjectComponent.class.getMethod("fallback", net.kyori.adventure.text.ComponentLike.class);
            hasMethod = true;
        } catch (Throwable ignored) {}
        HAS_FALLBACK = hasMethod;
        if (!hasMethod) {
            System.err.println("[FancyTags] Adventure 5+ is not available on this server version. Fallback tags will not be used.");
        }
    }

    public PlayerSymbol(String value, String signature) {
        this.value = value;
        this.signature = signature;
    }

    @Override
    public Component serialize() {
        return Component.object(ObjectContents.playerHead().profileProperty(PlayerHeadObjectContents.property("textures", this.value, this.signature)).build());
    }

    @Override
    public Component serialize(Component fallback) {
        Component component = Component.object(ObjectContents.playerHead().profileProperty(PlayerHeadObjectContents.property("textures", this.value, this.signature)).build());
        if (HAS_FALLBACK) {
            try {
                return applyFallback(component, fallback);
            } catch (LinkageError e) {
                // Return gracefully without fallback if linkage error occurs
            }
        }
        return component;
    }

    private Component applyFallback(Component component, Component fallback) {
        if (component instanceof net.kyori.adventure.text.ObjectComponent objectComponent) {
            return objectComponent.fallback(fallback);
        }
        return component;
    }

    @Override
    public String serializeMiniMessage() {
        return "<signed_head:" + this.value + ":" +  this.signature + ">";
    }
}
