package com.krake.test.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.krake.core.extension.getClass
import com.krake.core.extension.putClass

/**
 * Simple [AppCompatActivity] that will create a [View] through reflection and will
 * add it to the root container.
 */
class ViewActivity : AppCompatActivity() {

    companion object {
        private const val ARG_VIEW_CLASS = "argViewClass"

        /**
         * Creates the [Intent] to use in tests.
         * The [Intent] will be a main [Intent].
         *
         * @param viewClass the class of the [View] that must be created through reflection.
         */
        fun createTestIntent(viewClass: Class<out View>): Intent {
            val intent = Intent(Intent.ACTION_MAIN)
            val extras = Bundle()
            extras.putClass(ARG_VIEW_CLASS, viewClass)
            intent.putExtras(extras)
            return intent
        }
    }

    private val container by lazy { FrameLayout(this) }

    /**
     * Get the [View] created through reflection.
     */
    lateinit var view: View
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(container, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        // Get the class from the Intent.
        val viewClass = intent.extras.getClass<View>(ARG_VIEW_CLASS) ?:
                throw IllegalArgumentException("You must pass the class of the view.")

        // Creates the View through reflection.
        view = viewClass.getDeclaredConstructor(Context::class.java).newInstance(this)
        // Add the View to the container using the default layout params.
        container.addView(view)
    }
}