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

import de.exlll.configlib.YamlConfigurationProperties;
import de.exlll.configlib.YamlConfigurationStore;
import lombok.Getter;

import java.nio.file.Path;
import java.util.function.Consumer;

public class ConfigInstance<T> {
    private static final YamlConfigurationProperties properties = YamlConfigurationProperties.newBuilder().build();

    protected final VattenPlugin plugin;
    protected final String name;
    protected final Class<T> clazz;
    protected final YamlConfigurationStore<T> configStore;
    protected final Path file;

    @Getter
    protected T data;

    public ConfigInstance(VattenPlugin plugin, String name, Class<T> clazz) {
        this.plugin = plugin;
        this.name = name;
        this.clazz = clazz;
        this.configStore = new YamlConfigurationStore<>(clazz, properties);
        this.file = plugin.getPath().resolve(name + ".yml");
    }

    void load() {
        this.data = configStore.update(file);
    }

    void edit(Consumer<T> consumer) {
        consumer.accept(data);
        configStore.save(data, file);
    }
}
