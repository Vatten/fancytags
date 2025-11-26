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

package dev.vatten.fancytags;

import dev.vatten.fancytags.configs.PluginConfig;
import dev.vatten.fancytags.symbols.PlayerSymbol;
import dev.vatten.fancytags.symbols.Symbol;
import lombok.Getter;
import org.mineskin.JsoupRequestHandler;
import org.mineskin.MineSkinClient;
import org.mineskin.data.JobInfo;
import org.mineskin.data.SkinInfo;
import org.mineskin.options.GenerateQueueOptions;
import org.mineskin.request.GenerateRequest;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class TagGenerator {
    private final Plugin plugin;
    private final MineSkinClient CLIENT;

    public TagGenerator(Plugin plugin) {
        this.plugin = plugin;
        PluginConfig.MineSkin mineSkinSettings = plugin.getPluginConfig().getMineSkinSettings();
        PluginConfig.MineSkin.Limits limits = mineSkinSettings.getLimits();
        CLIENT = MineSkinClient.builder()
                .requestHandler(JsoupRequestHandler::new)
                .userAgent("FancyTags/v1.0")
                .apiKey(mineSkinSettings.getApiKey())
                .generateQueueOptions(limits.getMode() == PluginConfig.MineSkin.Limits.Mode.AUTO ? GenerateQueueOptions.createAuto() : GenerateQueueOptions.create().withInterval(limits.getIntervalMillis(), TimeUnit.MILLISECONDS).withConcurrency(limits.getConcurrency()))
                .build();
    }

    public CompletableFuture<Tag> generate(String name, BufferedImage bufferedImage, Consumer<Progress> onUpdate) {
        CompletableFuture<Tag> future = new CompletableFuture<>();

        if(bufferedImage.getHeight() != 8 || bufferedImage.getWidth() % 8 != 0) {
            future.completeExceptionally(new IllegalArgumentException("Image height must be 8, and width must be a multiple of 8. Dimensions provided: " + bufferedImage.getHeight() + "x" + bufferedImage.getWidth()));
            return future;
        }

        int parts = bufferedImage.getWidth() / 8;
        Progress progress = new Progress(parts);
        Symbol[] symbols = new Symbol[parts];
        for(int i = 0; i < parts; i++) {
            BufferedImage partImage = bufferedImage.getSubimage(i * 8, 0, 8, 8);
            BufferedImage skinImage = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            skinImage.getGraphics().drawImage(partImage, 8, 8, null);

            int symbolIndex = i;
            upload(name, skinImage).thenAccept(skinInfo -> {
                progress.increase();
                symbols[symbolIndex] = new PlayerSymbol(skinInfo.texture().data().value(), skinInfo.texture().data().signature());
                onUpdate.accept(progress);
                if(progress.getCurrent() == progress.getMax()) {
                    future.complete(new Tag(List.of(symbols)));
                }
            }).exceptionally(throwable -> {
                future.completeExceptionally(throwable);
                return null;
            });
        }

        return future;
    }

    private CompletableFuture<SkinInfo> upload(String name, RenderedImage image) {
        GenerateRequest request = GenerateRequest.upload(image)
                .name(name)
                .visibility(plugin.getPluginConfig().getMineSkinSettings().getVisibility());

        return CLIENT.queue().submit(request)
                .thenCompose(queueResponse -> {
                    JobInfo job = queueResponse.getJob();
                    return job.waitForCompletion(CLIENT);
                })
                .thenCompose(jobResponse -> jobResponse.getOrLoadSkin(CLIENT));
    }

    public static class Progress {
        @Getter
        private int current = 0;
        @Getter
        private final int max;
        public Progress(int max) {
            this.max = max;
        }

        public void increase() {
            current++;
        }
    }
}
