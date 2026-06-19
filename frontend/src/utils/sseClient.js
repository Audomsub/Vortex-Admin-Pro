/**
 * Fetch-based SSE client that sends the JWT via Authorization header
 * instead of a URL query parameter (which would be logged by servers).
 *
 * Returns a cleanup function that cancels the stream.
 */
export function createSseClient(url, token, handlers) {
    let active = true;
    let abortController = new AbortController();

    async function connect() {
        try {
            const response = await fetch(url, {
                headers: { Authorization: `Bearer ${token}` },
                signal: abortController.signal,
            });

            if (!response.ok || !active) return;

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
        } catch (err) {
            if (active && err.name !== 'AbortError') {
                setTimeout(connect, 5000);
            }
        }
    };

    connect();

    return () => {
        active = false;
        abortController.abort();
    };
}
