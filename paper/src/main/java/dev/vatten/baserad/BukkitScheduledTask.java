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

public class BukkitScheduledTask extends ScheduledTask<space.arim.morepaperlib.scheduling.ScheduledTask> {
    public BukkitScheduledTask(space.arim.morepaperlib.scheduling.ScheduledTask task) {
        super(task);
    }

    @Override
    public void cancel() {
        if(!isCancelled()) {
            this.task.cancel();
        }
    }

    @Override
    public boolean isCancelled() {
        return task == null || task.isCancelled();
    }
}
