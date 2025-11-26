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

package dev.vatten.baserad.configs;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import lombok.Getter;
import org.mineskin.data.Visibility;

@Configuration
public class PluginConfig {
    @Getter
    @Comment({
            "",
            "The tags are only cached on the client for 5 minutes after rendering,",
            "which means to have a seamless experience with no visual \"skin loading\"",
            "the players have to be sent all the tags regularly.",
            "This is done by sending a title pushed off-screen on join, and continuously on a timer."
    })
    private CacheSettings cacheSettings = new CacheSettings();

    @Configuration
    public static class CacheSettings {
        @Getter
        @Comment({
                "",
                "Whether cache updates should be sent."
        })
        private boolean enabled = true;

        @Getter
        @Comment({
                "",
                "Whether players should be sent cache updates on join."
        })
        private boolean onJoin = true;

        @Getter
        @Comment({
                "",
                "Whether players should continue being sent cache updates."
        })
        private boolean onTimer = true;

        @Getter
        @Comment({
                "",
                "How frequent the timer runs, in seconds."
        })
        private int intervalDuration = 300;

        @Getter
        @Comment({
                "",
                "The ratio of how much spacing is needed to push the tags off-screen in the title",
                "Each \"spacing\" consists of 2 spaces, which is as wide as a player head component,",
                "and the ratio is how many spacings per player head.",
                "If the tags are visible on-screen, consider raising this number.",
                "(This is mostly because the title feature has not been tested on larger screen sizes,",
                "this is very likely subject to change)"
        })
        private double offScreenRatio = 2.5;
    }

    @Getter
    @Comment({
            "",
            "Settings for the MineSkin API (mineskin.org).",
            "MineSkin is used for the skin signature generation."
    })
    private MineSkin mineSkinSettings = new MineSkin();

    @Configuration
    public static class MineSkin {
        @Getter
        @Comment({
                "",
                "Your project's MineSkin API key.",
                "If you don't have one already, create an account and get one at https://account.mineskin.org/keys/"
        })
        private String apiKey = "";

        @Getter
        @Comment({
                "",
                "The visibility of the generated skin.",
                "PUBLIC - Will be publicly shown on the MineSkin website",
                "UNLISTED - Can only be accessed via the link",
                "PRIVATE - Not accessible by anyone besides the account owner. NOTE: This option is locked to paid plans only."
        })
        private Visibility visibility = Visibility.UNLISTED;

        @Getter
        @Comment({
                "",
                "If you have a paid plan on MineSkin for faster generation,",
                "you can put your higher limits below.",
                "If you don't have a paid plan, this can be ignored",
                "If you however don't know what to put, leave the mode at AUTO and it will sort itself out"
        })
        private Limits limits = new Limits();

        @Configuration
        public static class Limits {
            @Getter
            @Comment({
                    "",
                    "The mode for what limits to use. Can be AUTO for automatic allowance retrieving, or CUSTOM to use your own values."
            })
            private Mode mode = Mode.AUTO;

            @Getter
            @Comment({
                    "",
                    "How many milliseconds between each request.",
                    "This value is ignored when mode is set to AUTO"
            })
            private int intervalMillis = 200;

            @Getter
            @Comment({
                    "",
                    "How many requests to handle concurrently.",
                    "This value is ignored when mode is set to AUTO"
            })
            private int concurrency = 1;

            public enum Mode {
                AUTO,
                CUSTOM
            }
        }
    }
}