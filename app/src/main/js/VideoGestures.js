(function () {
    'use strict';
    if (window.__oasisVideoGestures) {
        return;
    }
    window.__oasisVideoGestures = true;

    const TAP_TIMEOUT = 300;
    const DOUBLE_TAP_DELAY = 300;

    let lastTapTime = 0;
    let lastTapX = 0;
    let lastTapY = 0;

    function toggleFullscreen(video) {
        if (document.fullscreenElement) {
            document.exitFullscreen().catch(() => {});
        } else if (video.requestFullscreen) {
            video.requestFullscreen().catch(() => {});
        }
    }

    function handleTap(video, clientX, clientY) {
        const now = Date.now();
        const dx = clientX - lastTapX;
        const dy = clientY - lastTapY;
        const distance = Math.sqrt(dx * dx + dy * dy);
        if (now - lastTapTime < DOUBLE_TAP_DELAY && distance < 48) {
            toggleFullscreen(video);
            lastTapTime = 0;
        } else {
            lastTapTime = now;
            lastTapX = clientX;
            lastTapY = clientY;
        }
    }

    function attach(video) {
        if (video.dataset.oasisGesturesAttached) {
            return;
        }
        video.dataset.oasisGesturesAttached = 'true';
        video.addEventListener('touchend', function (event) {
            if (event.changedTouches.length === 1) {
                const touch = event.changedTouches[0];
                handleTap(video, touch.clientX, touch.clientY);
            }
        });
        video.addEventListener('click', function (event) {
            handleTap(video, event.clientX, event.clientY);
        });
    }

    const observer = new MutationObserver(function (mutations) {
        document.querySelectorAll('video').forEach(attach);
    });
    observer.observe(document.body || document.documentElement, { childList: true, subtree: true });
    document.querySelectorAll('video').forEach(attach);
})();
