package com.krake.core.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.speech.RecognizerIntent
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.IdRes
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.krake.core.R
import com.krake.core.app.ActivityResultCallback
import com.krake.core.app.ResultManager
import com.krake.core.util.LayoutUtils
import java.util.*

/**
 * A type of [SearchView] that will be placed above another [View] when visible (e.g. [Toolbar]) using
 * the behavior [FloatSearchView.Behavior].
 * This widget supports internally the voice search.
 * To enable it, the [Activity] that contains this widget, must include in the AndroidManifest.xml:
 * ```xml
 * <meta-data
 *      android:name="android.app.searchable"
 *      android:resource="@xml/searchable_voice" />
 * <intent-filter>
 *      <action android:name="android.intent.action.SEARCH" />
 * </intent-filter>
 * ```
 * This widget supports some XML attributes:
 * <ul>
 *     <li><i>dependencyId</i>: id of the view used to define the size and the position of this widget</li>
 *     <li><i>textColor</i>: color of the main text</li>
 *     <li><i>hintTextColor</i>: color of the hint text that will be visible when the main text is empty</li>
 *     <li><i>iconsColor</i>: color of every icon in this widget</li>
 *     <li><i>shapeBackgroundColor</i>: background color that will be set on the rectangular shape</li>
 *     <li><i>cornersRadius</i>: radius of the rectangular shape's corners</li>
 * </ul>
 * This widget saves and restores its state automatically.
 */
@CoordinatorLayout.DefaultBehavior(FloatSearchView.Behavior::class)
class FloatSearchView : SearchView, ActivityResultCallback, View.OnClickListener {
    companion object {
        @IdRes
        val DEFAULT_VIEW_ID = R.id.float_search_view

        private const val ANIMATION_DURATION_MS = 250L

        private const val REQ_CODE_SPEECH_INPUT = 89
        private const val OUT_STATE_SUPER_BUNDLE = "otsSuperBundle"
        private const val OUT_STATE_SEARCH_VISIBILITY = "otsVisibleState"

        /**
         * Create and attach the [FloatSearchView] to a [CoordinatorLayout].
         *
         * @param parent [CoordinatorLayout] used as direct parent of the [FloatSearchView].
         * @param dependencyId view's id of the dependency that will be overlapped by the [FloatSearchView].
         * @param viewId id of the [FloatSearchView]. DEFAULT: [R.id.float_search_view]
         */
        fun attach(parent: CoordinatorLayout, @IdRes dependencyId: Int, @IdRes viewId: Int = DEFAULT_VIEW_ID): FloatSearchView {
            val searchView = FloatSearchView(parent.context)
            searchView.id = viewId
            parent.addView(searchView)
            searchView.dependencyId = dependencyId
            return searchView
        }
    }

    constructor(context: Context) : super(context) {
        postInit(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        postInit(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        postInit(context, attrs)
    }

    /**
     * Id of the dependency that will be overlapped by the [FloatSearchView].
     */
    @IdRes
    var dependencyId: Int = View.NO_ID

    /**
     * True if the [FloatSearchView] is actually visible above the dependency, false otherwise.
     */
    var isSearchVisible: Boolean = false
        private set

    /**
     * GET: get the background color of the shape.
     * SET: change the background color of the shape. The shape must be invalidated after each change.
     */
    @ColorInt
    var shapeBackgroundColor: Int = 0
        set(value) {
            field = value
            // Create the shape.
            val shapeBg = GradientDrawable()
            shapeBg.shape = GradientDrawable.RECTANGLE
            shapeBg.cornerRadius = cornerRadius.toFloat()
            shapeBg.setColor(value)
            background = shapeBg
        }

    /**
     * GET: get the color of the icons.
     * SET: change the color of the icons. All the [ImageView] inside the [FloatSearchView] will be colored recursively.
     */
    @ColorInt
    var iconsColor: Int = 0
        set(value) {
            field = value
            setColorFilterOnChildren(this, value)
        }

    /**
     * GET: get the color of the main text.
     * SET: set the color of the main text.
     */
    @ColorInt
    var textColor: Int = 0
        set(value) {
            field = value
            mainTextView.setTextColor(value)
        }

    /**
     * GET: get the color of the hint text used when the main text is empty.
     * SET: set the color of the hint text used when the main text is empty.
     */
    @ColorInt
    var hintTextColor: Int = 0
        set(value) {
            field = value
            mainTextView.setHintTextColor(value)
        }

    /**
     * GET: get the shape corners' radius.
     * SET: change the shape corners' radius. The shape must be invalidated after each change.
     */
    var cornerRadius: Int = 0
        set(value) {
            field = value
            // Create the shape.
            val shapeBg = GradientDrawable()
            shapeBg.shape = GradientDrawable.RECTANGLE
            shapeBg.cornerRadius = value.toFloat()
            shapeBg.setColor(shapeBackgroundColor)
            background = shapeBg
        }

    private val mainTextView: SearchAutoComplete by lazy { findViewById<SearchAutoComplete>(R.id.search_src_text) }
    private var appearanceListener: AppearanceListener? = null

    private fun postInit(context: Context, attrs: AttributeSet?) {
        visibility = View.INVISIBLE
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            alpha = 0f
        }

        isClickable = false
        setIconifiedByDefault(false)

        val voiceBtn: ImageView = findViewById(R.id.search_voice_btn)
        val collapsedImageView: ImageView = findViewById(R.id.search_mag_icon)

        voiceBtn.setOnClickListener(this)
        collapsedImageView.setImageDrawable(null)

        mainTextView.setHint(R.string.search_hint)

        val searchPlate: View = findViewById(R.id.search_plate)
        // Remove underline.
        searchPlate.setBackgroundColor(Color.TRANSPARENT)

        val submitArea: View = findViewById(R.id.submit_area)
        // Remove underline.
        submitArea.setBackgroundColor(Color.TRANSPARENT)

        // Copy the configurations of the voice button to the up button and add it to the layout.
        val upBtn = ImageView(context)
        upBtn.id = R.id.btn_close_search
        // Creates a new drawable using the voice button configurations
        upBtn.background = voiceBtn.background.constantState?.newDrawable()
        upBtn.setImageResource(R.drawable.ic_up_icon)
        val upBtnParams = LayoutParams(voiceBtn.layoutParams.width, voiceBtn.layoutParams.height)
        val insetStart = resources.getDimensionPixelSize(R.dimen.search_view_close_btn_inset_start)
        upBtn.setPadding(voiceBtn.paddingStart + insetStart, voiceBtn.paddingTop, voiceBtn.paddingEnd, voiceBtn.paddingBottom)
        // Set the listener.
        upBtn.setOnClickListener(this)
        // Add the up button to the layout at position 0
        addView(upBtn, 0, upBtnParams)

        // Get default attributes.
        val defaultTextColor = ContextCompat.getColor(context, R.color.search_view_default_text_color)
        val defaultHintTextColor = ContextCompat.getColor(context, R.color.search_view_default_hint_text_color)
        val defaultIconsColor = ContextCompat.getColor(context, R.color.search_view_default_icons_color)
        val defaultBackgroundColor = ContextCompat.getColor(context, R.color.search_view_default_background_color)
        val defaultCornerRadius = resources.getDimensionPixelSize(R.dimen.search_view_default_corner_radius)

        if (attrs != null) {
            // Get XML attributes and set them. If not found, the value will be the default one.
            val a = context.obtainStyledAttributes(attrs, R.styleable.FloatSearchView)
            dependencyId = a.getResourceId(R.styleable.FloatSearchView_dependencyId, View.NO_ID)
            textColor = a.getColor(R.styleable.FloatSearchView_textColor, defaultTextColor)
            hintTextColor = a.getColor(R.styleable.FloatSearchView_hintTextColor, defaultHintTextColor)
            iconsColor = a.getColor(R.styleable.FloatSearchView_iconsColor, defaultIconsColor)
            shapeBackgroundColor = a.getColor(R.styleable.FloatSearchView_shapeBackgroundColor, defaultBackgroundColor)
            cornerRadius = a.getDimensionPixelSize(R.styleable.FloatSearchView_cornersRadius, defaultCornerRadius)
            a.recycle()
        } else {
            // Set default attributes.
            textColor = defaultTextColor
            hintTextColor = defaultHintTextColor
            iconsColor = defaultIconsColor
            shapeBackgroundColor = defaultBackgroundColor
            cornerRadius = defaultCornerRadius
        }

        // Check if SearchManager is correctly set from AndroidManifest.xml.
        val searchManager = context.getSystemService(Context.SEARCH_SERVICE) as? SearchManager
        if (searchManager != null) {
            setSearchableInfo(searchManager.getSearchableInfo((context as Activity).componentName))
        } else {
            Log.w(SearchManager::class.java.simpleName, "If you want to enable vocal search in your Activity, " +
                    "check the javadoc of " + FloatSearchView::class.java.name)
        }
    }

    /**
     * Show the [FloatSearchView] above the dependency widget.
     * The [AppearanceListener] will be notified when the animation finishes or immediately if there's no animation.
     *
     * @param animated true if the show action must be animated.
     */
    fun show(animated: Boolean = true) {
        if (isSearchVisible)
            return

        isSearchVisible = true
        val startAnim: () -> Unit = {
            visibility = View.VISIBLE
        }

        val endAnim = {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                alpha = 1f
            }
            // Request the focus when the keyboard will be visible.
            requestFocus()
            LayoutUtils.showKeyboard(context, mainTextView)
            // Notify the listener.
            appearanceListener?.onSearchViewAppeared(this)
        }
        if (animated) {
            val animator: Animator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // Circular reveal from api 21.
                animator = createCircularReveal(0, Math.max(width, height))
            } else {
                // Color fade in animation below api 21.
                animator = ObjectAnimator.ofFloat(this, View.ALPHA, 1f)
            }
            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    startAnim()
                }

                override fun onAnimationEnd(animation: Animator) {
                    endAnim()
                }
            })
            animator.duration = ANIMATION_DURATION_MS
            animator.start()
        } else {
            startAnim()
            endAnim()
        }
    }

    /**
     * Hide the [FloatSearchView] above the dependency widget.
     * The [AppearanceListener] will be notified when the animation finishes or immediately if there's no animation.
     *
     * @param animated true if the hide action must be animated.
     */
    fun hide(animated: Boolean = true) {
        if (!isSearchVisible)
            return

        isSearchVisible = false
        setQuery(null, true)
        val startAnim = {
            // Clear the keyboard focus before starting the animation.
            clearFocus()
        }

        val endAnim = {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                alpha = 0f
            }
            visibility = View.INVISIBLE
            // Notify the listener.
            appearanceListener?.onSearchViewDisappeared(this)
        }
        if (animated) {
            val animator: Animator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // Circular reveal from api 21.
                animator = createCircularReveal(Math.max(width, height), 0)
            } else {
                // Color fade out animation below api 21.
                animator = ObjectAnimator.ofFloat(this, View.ALPHA, 0f)
            }
            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    startAnim()
                }

                override fun onAnimationEnd(animation: Animator) {
                    endAnim()
                }
            })
            animator.duration = ANIMATION_DURATION_MS
            animator.start()
        } else {
            startAnim()
            endAnim()
        }
    }

    /**
     * Set an [AppearanceListener] that will be notified on visibility changes.
     *
     * @param listener [AppearanceListener] to set.
     */
    fun setAppearanceListener(listener: AppearanceListener) {
        appearanceListener = listener
    }

    override fun onSaveInstanceState(): Parcelable {
        super.onSaveInstanceState()
        val bundle = Bundle()
        // Save the View's super state in the out state Bundle.
        bundle.putParcelable(OUT_STATE_SUPER_BUNDLE, super.onSaveInstanceState())
        // Save the View's visibility in the out state Bundle.
        bundle.putBoolean(OUT_STATE_SEARCH_VISIBILITY, isSearchVisible)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var stateToDispatch: Parcelable? = state
        (stateToDispatch as? Bundle)?.let {
            // Restore the View's super state.
            stateToDispatch = it.getParcelable(OUT_STATE_SUPER_BUNDLE)
            // Restore the View's visibility.
            val wasVisible = it.getBoolean(OUT_STATE_SEARCH_VISIBILITY)
            if (wasVisible) {
                // If the View was visible before the rotation, it will be visible again after the rotation.
                show(animated = false)
            }
        }
        super.onRestoreInstanceState(stateToDispatch)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // Register this widget to onActivityResult() callbacks.
        (context as? ResultManager.Provider)?.provideResultManager()?.registerForResult(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // Register this widget to onActivityResult() callbacks.
        (context as? ResultManager.Provider)?.provideResultManager()?.unregisterForResult(this)
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        // Change the default layout params to avoid to override them in most of the cases.
        return LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.search_voice_btn -> {
                openVoiceDialog()
            }

            R.id.btn_close_search -> {
                hide()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQ_CODE_SPEECH_INPUT && resultCode == Activity.RESULT_OK && data != null) {
            // Get result returned from Google voice dialog.
            val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (result.isNotEmpty()) {
                val query = result[0]
                // Update the query in the FloatSearchView.
                setQuery(query, true)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun createCircularReveal(startRadius: Int, endRadius: Int): Animator {
        // The center of the circle is centered vertically and placed at the rightmost pixel of the FloatSearchView.
        return ViewAnimationUtils.createCircularReveal(this, right, top + height / 2, startRadius.toFloat(), endRadius.toFloat())
    }

    private fun openVoiceDialog() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.search_voice_title))
        try {
            // An instance of Activity is needed to use the Google voice dialog.
            (context as? Activity)?.startActivityForResult(intent, REQ_CODE_SPEECH_INPUT)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, R.string.search_voice_error_snack, Toast.LENGTH_LONG).show()
        }
    }

    private fun setColorFilterOnChildren(view: ViewGroup, @ColorInt color: Int) {
        (0..view.childCount - 1)
                .map { view.getChildAt(it) }
                .forEach {
                    if (it is ImageView) {
                        // Set the color filter.
                        it.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
                    } else if (it is ViewGroup) {
                        // The color must be set in a recursive way on all children
                        setColorFilterOnChildren(it, color)
                    }
                }
    }

    /**
     * Default behavior of [FloatSearchView] used to overlap a widget with the search view.
     * The [FloatSearchView] will copy the position, the size and the elevation of the widget.
     */
    internal class Behavior : CoordinatorLayout.Behavior<FloatSearchView>() {
        companion object {
            private const val DEFAULT_ELEVATION = -1f
        }

        private var permanentElevation: Float = DEFAULT_ELEVATION

        override fun layoutDependsOn(parent: CoordinatorLayout, child: FloatSearchView, dependency: View): Boolean =
                // Search the dependency cycling on the ViewGroups
                dependency.findViewById<View?>(child.dependencyId) != null

        override fun onDependentViewChanged(parent: CoordinatorLayout, child: FloatSearchView, dependency: View): Boolean {
            // Get the child of the dependency or the dependency itself.
            val dependencyChild: View? = dependency.findViewById(child.dependencyId)
            dependencyChild?.let {
                val searchViewParams = child.layoutParams as MarginLayoutParams
                val topMargin = searchViewParams.topMargin
                val startMargin = searchViewParams.marginStart
                // Change the size.
                searchViewParams.height = it.height - topMargin - searchViewParams.bottomMargin
                searchViewParams.width = it.width - startMargin - searchViewParams.marginEnd
                child.layoutParams = searchViewParams
                // Change the position.
                child.x = it.x + startMargin
                child.y = it.y + topMargin
                val dependencyElevation = ViewCompat.getElevation(dependency)
                if (permanentElevation == DEFAULT_ELEVATION) {
                    // In this way, we avoid the sum of the elevation multiple times.
                    permanentElevation = ViewCompat.getElevation(child)
                }
                // Change the elevation
                ViewCompat.setElevation(child, permanentElevation + dependencyElevation)
                return true
            }
            return false
        }
    }

    /**
     * Listener used to notify changes about [FloatSearchView]'s visibility.
     */
    interface AppearanceListener {

        /**
         * Called when the widget appears, if the appearing action was animated,
         * it will be called at the end of the animation.
         *
         * @param view [FloatSearchView] that appeared.
         */
        fun onSearchViewAppeared(view: FloatSearchView)

        /**
         * Called when the widget disappears, if the disappearing action was animated,
         * it will be called at the end of the animation.
         *
         * @param view [FloatSearchView] that disappeared.
         */
        fun onSearchViewDisappeared(view: FloatSearchView)
    }
}