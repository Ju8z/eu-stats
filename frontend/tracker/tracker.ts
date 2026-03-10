(() => {
    // Self-executing wrapper (IIFE):
    // The function is created and immediately invoked by the trailing `();`.
    // This means tracking bootstraps as soon as the script file is loaded by the browser.
    type TrackerEventType = 'pageview' | 'event' | 'heartbeat';
    // A visible tab sends heartbeats on this cadence so the backend can infer
    // "currently active" without a dedicated presence table.
    const HEARTBEAT_EVENT_TYPE: TrackerEventType = 'heartbeat';
    const HEARTBEAT_INTERVAL_MILLIS = 5000;
    const TAB_ID_STORAGE_KEY = 'eu_stats_tab_id';

    interface CollectPayload {
        siteId: number;
        url: string;
        title: string;
        referrer: string;
        screenResolution: string;
        userAgent: string;
        eventType: TrackerEventType;
        eventName: string | null;
        timestamp: string;
    }

    interface TrackerWindow extends Window {
        doNotTrack?: string;
    }

    interface HeartbeatState {
        intervalId: number | null;
    }

    interface PageviewState {
        lastTrackedPath: string | null;
    }

    try {
        const trackerWindow = globalThis.window as TrackerWindow;
        const trackerDocument = trackerWindow.document;
        const heartbeatState: HeartbeatState = {
            intervalId: null
        };

        // Respect Do Not Track and stop before any data collection starts.
        if (navigator.doNotTrack === '1' || trackerWindow.doNotTrack === '1') {
            return;
        }

        // Read the current <script src=".../s.js?id=SITE_ID"> element. This will be forced in @TrackerController to ensure the `id` parameter is always present, but we also guard against malformed script tags here.
        // The site ID comes from the `id` query parameter.
        const trackerScriptElement = trackerDocument.currentScript;
        if (!(trackerScriptElement instanceof HTMLScriptElement) || !trackerScriptElement.src) {
            return;
        }

        const trackerScriptUrl = new URL(trackerScriptElement.src, trackerWindow.location.href);
        const siteId = Number(trackerScriptUrl.searchParams.get('id'));
        if (!siteId || Number.isNaN(siteId)) {
            return;
        }

        const collectorEndpoint = trackerScriptUrl.origin + '/api/c';
        // sessionStorage is scoped per tab, which gives us a stable tab-level
        // identifier without introducing a new backend column.
        const currentTabId = getOrCreateTabId();

        // Normalize the current page URL down to a pathname for privacy and consistency.
        function resolvePath(url: string): string {
            try {
                return new URL(url, trackerWindow.location.href).pathname || '/';
            } catch {
                return '/';
            }
        }

        // Convert document.referrer into only its domain (without `www.` prefix).
        function resolveReferrerDomain(): string {
            if (!trackerDocument.referrer) {
                return '';
            }

            try {
                return new URL(trackerDocument.referrer).hostname.replace(/^www\./, '');
            } catch {
                return '';
            }
        }

        // Generates a per-tab identifier. randomUUID is preferred, but the
        // fallback keeps the tracker working in older environments.
        function generateTabId(): string {
            if (trackerWindow.crypto?.randomUUID) {
                return trackerWindow.crypto.randomUUID();
            }

            return Date.now().toString(36) + Math.random().toString(36).slice(2, 10);
        }

        // Reuses the same tab id across reloads in the current tab so repeated
        // heartbeats can be deduplicated server-side. Other tabs get their own
        // sessionStorage instance and therefore their own id.
        function getOrCreateTabId(): string {
            try {
                const storedTabId = trackerWindow.sessionStorage.getItem(TAB_ID_STORAGE_KEY);
                if (storedTabId) {
                    return storedTabId;
                }

                const generatedTabId = generateTabId();
                trackerWindow.sessionStorage.setItem(TAB_ID_STORAGE_KEY, generatedTabId);
                return generatedTabId;
            } catch {
                return generateTabId();
            }
        }

        // Build the payload expected by backend CollectPayload.
        function buildPayload(eventType: TrackerEventType, eventName: string | null): CollectPayload {
            const screenWidth = trackerWindow.screen.width || 0;
            const screenHeight = trackerWindow.screen.height || 0;

            return {
                siteId,
                url: resolvePath(trackerWindow.location.href),
                title: trackerDocument.title || '',
                referrer: resolveReferrerDomain(),
                screenResolution: screenWidth + 'x' + screenHeight,
                userAgent: navigator.userAgent || '',
                eventType,
                eventName,
                timestamp: new Date().toISOString()
            };
        }

        // Send JSON via sendBeacon when safe, otherwise use fetch with credentials omitted.
        function postPayload(payload: CollectPayload): void {
            const requestBody = JSON.stringify(payload);
            const isSameOriginCollector = trackerScriptUrl.origin === trackerWindow.location.origin;

            // Prefer sendBeacon for same-origin cases because it is resilient during page unload.
            // Cross-origin beacon behavior can include credentials in some browsers, so we avoid it there.
            if (navigator.sendBeacon && isSameOriginCollector) {
                const beaconBody = new Blob([requestBody], { type: 'application/json' });
                if (navigator.sendBeacon(collectorEndpoint, beaconBody)) {
                    return;
                }
            }

            // Fetch fallback: never throw into host page code if transport fails.
            fetch(collectorEndpoint, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: requestBody,
                keepalive: true,
                credentials: 'omit'
            }).catch(() => null);
        }

        function sendEvent(eventType: TrackerEventType, eventName: string | null): void {
            postPayload(buildPayload(eventType, eventName));
        }

        // Heartbeats keep the current page "alive" while the tab remains
        // visible. We send the tab id via eventName so the backend can count
        // tabs without any schema change.
        function sendHeartbeat(): void {
            postPayload(buildPayload(HEARTBEAT_EVENT_TYPE, currentTabId));
        }

        // Visibility changes and page unloads both funnel through here so we do
        // not keep sending presence signals after the tab stops being active.
        function stopHeartbeat(): void {
            if (heartbeatState.intervalId == null) {
                return;
            }

            trackerWindow.clearInterval(heartbeatState.intervalId);
            heartbeatState.intervalId = null;
        }

        // Starts presence reporting only while the document is visible and
        // avoids stacking multiple intervals for the same tab.
        function startHeartbeat(): void {
            if (trackerDocument.visibilityState === 'hidden') {
                stopHeartbeat();
                return;
            }
            if (heartbeatState.intervalId != null) {
                return;
            }

            sendHeartbeat();
            heartbeatState.intervalId = trackerWindow.setInterval(() => {
                sendHeartbeat();
            }, HEARTBEAT_INTERVAL_MILLIS);
        }

        // Track a standard pageview event, but only when the path actually changes.
        // Guards against browsers and frameworks calling replaceState on load,
        // which would otherwise fire a duplicate pageview immediately after the initial one.
        function createTrackPageview(): () => void {
            const pageviewState: PageviewState = {
                lastTrackedPath: null
            };

            return function trackPageview(): void {
                const currentPath = resolvePath(trackerWindow.location.href);
                if (currentPath === pageviewState.lastTrackedPath) {
                    return;
                }

                pageviewState.lastTrackedPath = currentPath;
                sendEvent('pageview', null);
                startHeartbeat();
            };
        }

        const trackPageview = createTrackPageview();

        // Track link clicks as generic "link_click" events.
        function trackLinkClick(event: MouseEvent): void {
            if (!(event.target instanceof Element)) {
                return;
            }

            const anchorElement = event.target.closest('a[href]');
            if (!anchorElement) {
                return;
            }

            sendEvent('event', 'link_click');
        }

        // Patch History API methods so SPA navigations also emit pageviews.
        function instrumentHistoryNavigation(): void {
            const originalPushState = trackerWindow.history.pushState;
            const originalReplaceState = trackerWindow.history.replaceState;

            trackerWindow.history.pushState = function patchedPushState(
                data: unknown,
                unusedTitle: string,
                url?: string | URL | null
            ): void {
                originalPushState.call(this, data, unusedTitle, url);
                trackPageview();
            };

            trackerWindow.history.replaceState = function patchedReplaceState(
                data: unknown,
                unusedTitle: string,
                url?: string | URL | null
            ): void {
                originalReplaceState.call(this, data, unusedTitle, url);
                trackPageview();
            };
        }

        // Initialization sequence:
        // 1) Patch SPA navigation hooks
        // 2) Register listeners
        // 3) Send initial pageview
        instrumentHistoryNavigation();
        trackerWindow.addEventListener('popstate', trackPageview, { passive: true });
        trackerWindow.addEventListener('click', trackLinkClick, { capture: true, passive: true });
        trackerDocument.addEventListener('visibilitychange', startHeartbeat, { passive: true });
        trackerWindow.addEventListener('pagehide', stopHeartbeat, { passive: true });
        trackPageview();
    } catch {
        // Never break the host page because of analytics.
    }
})();
