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

import lombok.Getter;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TagStore {
    private final Plugin plugin;
    private final Map<String, Tag> tags;
    @Getter
    private List<String> tagNames;
    @Getter
    private Component tagAtlas;
    private int tagAtlasLength = 0;

    TagStore(Plugin plugin, Map<String, Tag> tags) {
        this.plugin = plugin;
        this.tags = tags;
        refresh();
    }

    public Tag getTag(String tagName) {
        return tags.get(tagName);
    }

    public boolean addTag(String tagName, Tag tag) {
        if(tags.containsKey(tagName)) {
            return false;
        }
        tags.put(tagName, tag);
        refresh();
        plugin.saveTags();
        return true;
    }

    public boolean removeTag(String tagName) {
        if(tags.containsKey(tagName)) {
            tags.remove(tagName);
            refresh();
            plugin.saveTags();
            return true;
        }
        return false;
    }

    Map<String, Tag> getTags() {
        return tags;
    }

    int getTagAtlasLength() {
        return tagAtlasLength;
    }

    private void refresh() {
        List<String> names = new ArrayList<>();
        Component atlas = Component.empty();
        for(Map.Entry<String, Tag> entry : tags.entrySet()) {
            Component serialized = entry.getValue().serialize();
            names.add(entry.getKey());
            atlas = atlas.append(serialized);
            tagAtlasLength += entry.getValue().getLength();
        }
        this.tagNames = Collections.unmodifiableList(names);
        this.tagAtlas = atlas;
    }
}
