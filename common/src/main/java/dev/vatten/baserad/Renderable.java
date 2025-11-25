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
import net.kyori.adventure.text.ObjectComponent;
import net.kyori.adventure.text.TextComponent;

public interface Renderable {
    Component asComponent();
    default int getLength() {
        return getLength(asComponent());
    }

    default int estimateWidth() {
        return estimateWidth(asComponent());
    }

    private int getLength(Component component) {
        int length = 0;
        if(component instanceof TextComponent textComponent) {
            length += textComponent.content().length();
        } else {
            length += 1;
        }
        for(Component child : component.children()) {
            length += getLength(child);
        }
        return length;
    }

    private int estimateWidth(Component component) {
        int width = 0;
        if(component instanceof TextComponent textComponent) {
            width += textComponent.content().length() * 5; // 5 is kind of average width of a character idk (its more of a guess)
        } else if(component instanceof ObjectComponent) {
            width += 8;
        }
        for(Component child : component.children()) {
            width += estimateWidth(child);
        }
        return width;
    }
}
