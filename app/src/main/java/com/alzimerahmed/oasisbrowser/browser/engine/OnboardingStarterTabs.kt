package com.alzimerahmed.oasisbrowser.browser.engine

/** Trusted starter pages used only by the hidden first-run quick-start gesture. */
object OnboardingStarterTabs {
    const val EXTRA_URLS = "com.alzimerahmed.oasisbrowser.extra.ONBOARDING_STARTER_URLS"

    val urls: ArrayList<String> = arrayListOf(
        "https://www.amazon.com/",
        "https://www.youtube.com/",
        "https://www.google.com/search?q=OasisBrowser+Browser",
    )
}
