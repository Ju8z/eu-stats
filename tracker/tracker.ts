(() => {
    // Self-executing wrapper (IIFE):
    // The function is created and immediately invoked by the trailing `();`.
    // This means tracking bootstraps as soon as the script file is loaded by the browser.
    type TrackerEventType = 'pageview' | 'event';

    interface CollectPayload {
        siteId: number;
        url: string;
        title: string;
        referrer: string;
        screenResolution: string;
        viewport: string;
        language: string;
        userAgent: string;
        eventType: TrackerEventType;
        eventName: string | null;
        timestamp: string;
    }

    interface TrackerWindow extends Window {
        doNotTrack?: string;
    }

    try {
        const trackerWindow = globalThis.window as TrackerWindow;
        const trackerDocument = trackerWindow.document;

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

        // Build the payload expected by backend CollectPayload.
        function buildPayload(eventType: TrackerEventType, eventName: string | null): CollectPayload {
            const screenWidth = trackerWindow.screen.width || 0;
            const screenHeight = trackerWindow.screen.height || 0;
            const viewportWidth = trackerWindow.innerWidth || 0;
            const viewportHeight = trackerWindow.innerHeight || 0;

            return {
                siteId,
                url: resolvePath(trackerWindow.location.href),
                title: trackerDocument.title || '',
                referrer: resolveReferrerDomain(),
                screenResolution: screenWidth + 'x' + screenHeight,
                viewport: viewportWidth + 'x' + viewportHeight,
                language: navigator.language || '',
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

        // Track a standard pageview event, but only when the path actually changes.
        // Guards against browsers and frameworks calling replaceState on load,
        // which would otherwise fire a duplicate pageview immediately after the initial one.
        function createTrackPageview(): () => void {
            const trackerState = {
                lastTrackedPath: null as string | null
            };

            return function trackPageview(): void {
                const currentPath = resolvePath(trackerWindow.location.href);
                if (currentPath === trackerState.lastTrackedPath) {
                    return;
                }

                trackerState.lastTrackedPath = currentPath;
                sendEvent('pageview', null);
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
        trackPageview();
    } catch {
        // Never break the host page because of analytics.
    }
})();
