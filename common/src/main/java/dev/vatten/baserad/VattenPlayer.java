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
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.UUID;

public class VattenPlayer implements ForwardingAudience {
    @Getter
    private final UUID uuid;
    @Getter
    private final String name;
    private final Audience audience;

    VattenPlayer(UUID uuid, String name, Audience audience) {
        this.uuid = uuid;
        this.name = name;
        this.audience = audience;
    }

    @Override
    public @NotNull Iterable<? extends Audience> audiences() {
        return Collections.singleton(audience);
    }
}
