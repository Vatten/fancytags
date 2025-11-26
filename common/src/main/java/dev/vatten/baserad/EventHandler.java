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

import dev.vatten.baserad.events.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class EventHandler {
    private final HashMap<Class<? extends Event>, List<Consumer<Event>>> eventHandlers;

    public EventHandler(VattenPlugin plugin) {
        this.eventHandlers = new HashMap<>();
    }

    public <T extends Event> void registerEventHandler(Class<T> type, Consumer<T> consumer) {
        if(!eventHandlers.containsKey(type)) {
            eventHandlers.put(type, new ArrayList<>());
        }
        eventHandlers.get(type).add((Consumer<Event>) consumer);
    }

    public <T extends Event> void dispatchEvent(T event) {
        if(!eventHandlers.containsKey(event.getClass())) return;
        for(Consumer<Event> consumer : eventHandlers.get(event.getClass())) {
            consumer.accept(event);
        }
    }
}
