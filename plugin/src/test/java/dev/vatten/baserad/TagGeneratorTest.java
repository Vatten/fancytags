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
import org.jsoup.Connection;
import org.junit.jupiter.api.Test;
import org.mineskin.exception.MineskinException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TagGeneratorTest {

    @Test
    void testInterceptCloudflareDnsError() {
        Connection.Response response = mock(Connection.Response.class);
        when(response.statusCode()).thenReturn(530);
        when(response.statusMessage()).thenReturn("Origin DNS error");
        when(response.contentType()).thenReturn("text/html; charset=UTF-8");
        String cfHtml = """
                <!doctype html>
                <html lang="en-US">
                <head>
                    <title>Origin DNS error | api.mineskin.org | Cloudflare</title>
                </head>
                <body>
                    <h1>Error 1016</h1>
                    <h2>Origin DNS error</h2>
                </body>
                </html>
                """;
        when(response.body()).thenReturn(cfHtml);

        MineskinException ex = assertThrows(MineskinException.class, () -> TagGenerator.interceptResponse(response));
        assertTrue(ex.getMessage().contains("530"), "Should contain HTTP status 530");
        assertTrue(ex.getMessage().contains("Cloudflare Error 1016"), "Should extract Cloudflare Error 1016");
        assertTrue(ex.getMessage().contains("Origin DNS error"), "Should extract page title");
        assertFalse(ex.getMessage().contains("<!doctype"), "Should not dump raw HTML");
    }

    @Test
    void testInterceptHtml502Error() {
        Connection.Response response = mock(Connection.Response.class);
        when(response.statusCode()).thenReturn(502);
        when(response.statusMessage()).thenReturn("Bad Gateway");
        when(response.contentType()).thenReturn("text/html");
        when(response.body()).thenReturn("<html><head><title>502 Bad Gateway</title></head><body>Bad Gateway</body></html>");

        MineskinException ex = assertThrows(MineskinException.class, () -> TagGenerator.interceptResponse(response));
        assertTrue(ex.getMessage().contains("502"));
        assertTrue(ex.getMessage().contains("502 Bad Gateway"));
    }

    @Test
    void testInterceptEmptyBody() {
        Connection.Response response = mock(Connection.Response.class);
        when(response.statusCode()).thenReturn(500);
        when(response.statusMessage()).thenReturn("Internal Server Error");
        when(response.contentType()).thenReturn("text/plain");
        when(response.body()).thenReturn("");

        MineskinException ex = assertThrows(MineskinException.class, () -> TagGenerator.interceptResponse(response));
        assertTrue(ex.getMessage().contains("500"));
    }

    @Test
    void testAllowsValidJsonResponse() {
        Connection.Response response = mock(Connection.Response.class);
        when(response.statusCode()).thenReturn(200);
        when(response.statusMessage()).thenReturn("OK");
        when(response.contentType()).thenReturn("application/json");
        when(response.body()).thenReturn("{\"success\":true,\"data\":{}}");

        assertDoesNotThrow(() -> TagGenerator.interceptResponse(response));
    }

    @Test
    void testAllowsValidJsonErrorResponse() {
        Connection.Response response = mock(Connection.Response.class);
        when(response.statusCode()).thenReturn(400);
        when(response.statusMessage()).thenReturn("Bad Request");
        when(response.contentType()).thenReturn("application/json");
        when(response.body()).thenReturn("{\"success\":false,\"errors\":[{\"code\":\"user_not_found\",\"message\":\"User not found\"}]}");

        assertDoesNotThrow(() -> TagGenerator.interceptResponse(response));
    }

    @Test
    void testSafeQueueOptionsDefaults() {
        PluginConfig.MineSkin.Limits limits = new PluginConfig.MineSkin.Limits();
        SafeQueueOptions queueOptions = new SafeQueueOptions(limits);

        assertEquals(1000, queueOptions.intervalMillis());
        assertEquals(1, queueOptions.concurrency());

        queueOptions.reportFailure();
        assertTrue(queueOptions.intervalMillis() > 1000, "Should apply penalty on failure");

        queueOptions.shutdown();
    }
}
