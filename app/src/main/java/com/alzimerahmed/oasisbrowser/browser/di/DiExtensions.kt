@file:JvmName("Injector")

package com.alzimerahmed.oasisbrowser.browser.di

import com.alzimerahmed.oasisbrowser.BrowserApp
import android.content.Context
import androidx.fragment.app.Fragment

/**
 * The [AppComponent] attached to the application [Context].
 */
val Context.injector: AppComponent
    get() = (applicationContext as BrowserApp).applicationComponent

/**
 * The [AppComponent] attached to the context, note that the fragment must be attached.
 */
val Fragment.injector: AppComponent
    get() = (context!!.applicationContext as BrowserApp).applicationComponent
