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

import dev.vatten.baserad.configs.PluginConfig;
import dev.vatten.baserad.symbols.PlayerSymbol;
import dev.vatten.baserad.symbols.Symbol;
import lombok.Getter;
import org.jsoup.Connection;
import org.mineskin.JsoupRequestHandler;
import org.mineskin.MineSkinClient;
import org.mineskin.data.JobInfo;
import org.mineskin.data.SkinInfo;
import org.mineskin.exception.MineskinException;
import org.mineskin.request.GenerateRequest;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TagGenerator {
    private static final Pattern TITLE_PATTERN = Pattern.compile("<title\\b[^>]*>(.*?)</title>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern CF_ERROR_PATTERN = Pattern.compile("Error\\s+([0-9]{3,4})", Pattern.CASE_INSENSITIVE);

    static {
        // Suppress noisy third-party grant / parse warnings from MineSkinClient internal logger
        Logger mineskinLogger = Logger.getLogger(MineSkinClient.class.getName());
        mineskinLogger.setFilter(record -> {
            if (record.getMessage() != null) {
                String msg = record.getMessage();
                if (msg.contains("grants") || msg.contains("Failed to parse response body")) {
                    return false;
                }
            }
            return true;
        });
    }

    private final Plugin plugin;
    private final MineSkinClient CLIENT;
    private final SafeQueueOptions queueOptions;

    public TagGenerator(Plugin plugin) {
        this.plugin = plugin;
        PluginConfig.MineSkin mineSkinSettings = plugin.getPluginConfig().getMineSkinSettings();
        PluginConfig.MineSkin.Limits limits = mineSkinSettings.getLimits();
        this.queueOptions = new SafeQueueOptions(limits);
        CLIENT = MineSkinClient.builder()
                .requestHandler(JsoupRequestHandler.withResponseInterceptor(TagGenerator::interceptResponse))
                .userAgent("FancyTags/v1.0")
                .apiKey(mineSkinSettings.getApiKey())
                .generateQueueOptions(queueOptions)
                .build();
        this.queueOptions.init(CLIENT);
    }

    public static void interceptResponse(Connection.Response response) {
        int status = response.statusCode();
        String body = response.body();
        String contentType = response.contentType();

        boolean isHtml = (contentType != null && contentType.toLowerCase().contains("text/html"))
                || (body != null && (body.regionMatches(true, 0, "<!doctype", 0, 9)
                || body.regionMatches(true, 0, "<html", 0, 5)
                || body.trim().startsWith("<")));

        String trimmedBody = body != null ? body.trim() : "";

        if (isHtml || status >= 500 || (status >= 400 && !trimmedBody.startsWith("{") && !trimmedBody.startsWith("[")) || trimmedBody.isEmpty()) {
            String errorDetail = extractErrorDetail(status, response.statusMessage(), body);
            throw new MineskinException("MineSkin API request failed (HTTP " + status + "): " + errorDetail);
        }
    }

    private static String extractErrorDetail(int status, String statusMessage, String body) {
        if (body != null && !body.isBlank()) {
            String title = null;
            Matcher titleMatcher = TITLE_PATTERN.matcher(body);
            if (titleMatcher.find()) {
                title = titleMatcher.group(1).replaceAll("<[^>]+>", "").replaceAll("\\s+", " ").trim();
            }

            Matcher cfMatcher = CF_ERROR_PATTERN.matcher(body);
            String cfError = cfMatcher.find() ? "Cloudflare Error " + cfMatcher.group(1) : null;

            if (cfError != null && title != null && !title.isEmpty()) {
                return cfError + " (" + title + ")";
            } else if (title != null && !title.isEmpty()) {
                return title;
            } else if (cfError != null) {
                return cfError;
            }

            String trimmed = body.trim();
            if (!trimmed.startsWith("<")) {
                return trimmed.length() > 120 ? trimmed.substring(0, 120) + "..." : trimmed;
            }
        }
        if (statusMessage != null && !statusMessage.isBlank()) {
            return statusMessage;
        }
        return "Unknown error";
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
                    future.complete(new Tag(List.of(symbols), name));
                }
            }).exceptionally(throwable -> {
                future.completeExceptionally(throwable);
                return null;
            });
        }

        return future;
    }

    private CompletableFuture<SkinInfo> upload(String name, RenderedImage image) {
        queueOptions.refreshGrantsIfNeeded();

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

    public void shutdown() {
        if (queueOptions != null) {
            queueOptions.shutdown();
        }
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
