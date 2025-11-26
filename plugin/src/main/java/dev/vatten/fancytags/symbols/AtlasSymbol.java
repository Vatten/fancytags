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

package dev.vatten.fancytags.symbols;

import de.exlll.configlib.Configuration;
import lombok.NoArgsConstructor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.object.ObjectContents;

@Configuration
@NoArgsConstructor
public class AtlasSymbol extends Symbol {
    private String atlas;
    private String sprite;

    public AtlasSymbol(String atlas, String sprite) {
        this.atlas = atlas;
        this.sprite = sprite;
    }

    @Override
    public Component serialize() {
        return Component.object(ObjectContents.sprite(Key.key(this.atlas), Key.key(this.sprite)));
    }
}
