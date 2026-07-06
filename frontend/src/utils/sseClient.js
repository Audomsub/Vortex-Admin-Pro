/**
 * Fetch-based SSE client that sends the JWT via Authorization header
 * instead of a URL query parameter (which would be logged by servers).
 *
 * `token` may be a string or a function returning the current token, so
 * reconnects pick up a refreshed JWT instead of reusing a stale one.
 *
 * Returns a cleanup function that cancels the stream.
 */
export function createSseClient(url, token, handlers) {
    const getToken = typeof token === 'function' ? token : () => token;
    const RECONNECT_DELAY_MS = 5000;

    let active = true;
    const abortController = new AbortController();

    function scheduleReconnect() {
        if (active) setTimeout(connect, RECONNECT_DELAY_MS);
    }

    async function connect() {
        if (!active) return;
        try {
            const response = await fetch(url, {
                headers: { Authorization: `Bearer ${getToken()}` },
                signal: abortController.signal,
            });

            if (!active) return;
            if (!response.ok) {
                scheduleReconnect();
                return;
            }

            const reader = response.body.getReader();
            const decoder = new TextDecoder();
            let buffer = '';
            let currentEvent = 'message';

            while (active) {
                const { done, value } = await reader.read();
                if (done) break;

                buffer += decoder.decode(value, { stream: true });
                const lines = buffer.split('\n');
                buffer = lines.pop();

                for (const line of lines) {
                    if (line.startsWith('event:')) {
                        currentEvent = line.slice(6).trim();
                    } else if (line.startsWith('data:')) {
                        const data = line.slice(5).trim();
                        const handler = handlers[currentEvent];
                        if (handler) handler(data);
                    } else if (line === '') {
                        currentEvent = 'message';
                    }
                }
            }

            // Stream ended normally (e.g. server-side emitter timeout) — reconnect
            scheduleReconnect();
        } catch (err) {
            if (active && err.name !== 'AbortError') {
                scheduleReconnect();
            }
        }
    }

    connect();

    return () => {
        active = false;
        abortController.abort();
    };
}
