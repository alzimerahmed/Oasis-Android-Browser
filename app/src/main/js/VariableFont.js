(function () {
    'use strict';
    const styleId = 'oasis-variable-font';
    if (document.getElementById(styleId)) {
        return;
    }
    const style = document.createElement('style');
    style.id = styleId;
    style.textContent = [
        '@font-face {',
        '  font-family: "OasisVariable";',
        '  src: url("file:///android_asset/fonts/google_sans_flex_500.ttf") format("truetype");',
        '}',
        'body, body * {',
        '  font-family: "OasisVariable", sans-serif !important;',
        '  font-optical-sizing: auto;',
        '}'
    ].join('\n');
    (document.head || document.documentElement).appendChild(style);
})();
