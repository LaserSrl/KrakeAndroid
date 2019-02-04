package com.krake.core.widget

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.support.annotation.LayoutRes
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.content.res.AppCompatResources
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.*
import com.krake.core.R
import com.krake.core.model.ContentItem
import com.krake.core.model.ContentItemWithContact
import com.krake.core.model.ContentItemWithSocial
import com.krake.core.social.SocialItems


/**
 * View per mostrare la Toolbar per accedere ai social.
 * Sono inseriti automaticamente gli elementi prestabiliti in DetailToolbarItem.
 *
 * Vengono inseriti automaticamente solo se il content item implementa ContentItemWithContact o ContentItemWithSocial.
 *
 * Le opzioni di visualizzazione sono prese da R.styleable.SocialToolbar
 * includono
 * R.styleable.SocialToolbar_socialRootLayout (default  R.layout.partial_social_toolbar), deve avere una con id R.id.social_toolbar
 * R.styleable.SocialToolbar_showNames se mostrare i nomi degli elementi
 *
 * @see SocialItems
 * @see ContentItemWithContact
 * @see ContentItemWithSocial
 */
open class SocialToolbar : FrameLayout, ContentItemView, View.OnClickListener, ViewTreeObserver.OnPreDrawListener {
    override lateinit var container: ContentItemViewContainer

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        createContentView(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        createContentView(context, attrs)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        createContentView(context, attrs)
    }

    protected lateinit var socialRootView: ViewGroup private set
    protected lateinit var inflater: LayoutInflater private set
    @LayoutRes
    protected var elementLayout: Int = 0
        private set
    protected var showNames = false
        private set
    protected var tintColorState: Int = 0
        private set

    private fun createContentView(context: Context, attrs: AttributeSet) {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.SocialToolbar, 0, 0)

        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        inflater.inflate(a.getResourceId(R.styleable.SocialToolbar_socialRootLayout, R.layout.partial_social_root), this, true)

        socialRootView = findViewById(R.id.social_toolbar_root)

        showNames = a.getBoolean(R.styleable.SocialToolbar_showNames, true)

        elementLayout = a.getResourceId(R.styleable.SocialToolbar_socialLayout, R.layout.partial_social_item)

        tintColorState = a.getResourceId(R.styleable.SocialToolbar_tintColorList, R.color.social_tint_color)

        this.visibility = View.GONE
        a.recycle()
    }

    override fun show(contentItem: ContentItem, cacheValid: Boolean) {
        val items = getSocialItems(contentItem)

        val visible: Boolean = !items.isEmpty()
        socialRootView.removeAllViews()

        for (item in items)
        {
            val socialButton = createView(item)
            socialRootView.addView(socialButton)

            socialButton.tag = item
            socialButton.setOnClickListener(this)
        }

        /**
         * Gestione scroll view: se i social button all interno occupano piu spazio della larghezza dello schermo allora è giusto che ci sia la scrollview
         * se al contrario ce ne sono di meno allora tolgo la scroll view cosi da far occupare con il weight 1 la larghezza totale dello schermo ai button
         *
         * NOTA: per maggiore performance si potrebbero fare 2 cose:
         * - sarebbe meglio fare il contrario, cioe partire senza scrollview e fare il calcolo al contrario aggiungendola successivamente, il problema è che non riesco a calcolare la larghezza
         *   effettiva dei button, si potrebbe mettendo a tutti i parent clipToChildren false, ma dovrei andare fino alla view root
         * - sarebbe possibile calcolare la larghezza effettiva dei button prima della rendering, calcolando la larghezza dell immagine e del testo, magari in futuro potremmo pensarci
         */
        socialRootView.viewTreeObserver.addOnPreDrawListener(this)

        visibility = if (visible) View.VISIBLE else View.GONE
    }

    protected open fun createView(detailToolbarItem: SocialItems.SocialToolbarItem): View
    {
        val view = inflater.inflate(elementLayout, socialRootView, false)

        if (view is Button || view is ImageButton)
        {
            val drawable = DrawableCompat.wrap(ResourcesCompat.getDrawable(resources, detailToolbarItem.image, null)!!)

            DrawableCompat.setTintList(drawable, AppCompatResources.getColorStateList(context, tintColorState))

            if (showNames && view is Button)
            {
                view.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null)
                view.setText(detailToolbarItem.name)
            }
            else if (view is ImageButton)
            {
                view.setImageDrawable(drawable)
            }
        }

        return view
    }

    protected open fun getSocialItems(contentItem: ContentItem): List<SocialItems.SocialToolbarItem>
    {
        val list = ArrayList<SocialItems.SocialToolbarItem>()

        if (contentItem is ContentItemWithSocial) {

            if (!contentItem.facebookValue.isNullOrEmpty())
                list.add(createSocialItem(SocialItems.FACEBOOK, contentItem.facebookValue!!))

            if (!contentItem.twitterValue.isNullOrEmpty())
                list.add(createSocialItem(SocialItems.TWITTER, contentItem.twitterValue!!))

            if (!contentItem.pinterestValue.isNullOrEmpty())
                list.add(createSocialItem(SocialItems.PINTEREST, contentItem.pinterestValue!!))

            if (!contentItem.instagramValue.isNullOrEmpty())
                list.add(createSocialItem(SocialItems.INSTANGRAM, contentItem.instagramValue!!))
        }

        if (contentItem is ContentItemWithContact) {
            if (!contentItem.sitoWebValue.isNullOrEmpty())
                list.add(createSocialItem(SocialItems.WEBSITE, contentItem.sitoWebValue!!))

            if (!contentItem.telefonoValue.isNullOrEmpty())
                list.add(createSocialItem(SocialItems.CALL, contentItem.telefonoValue!!))

            if (!contentItem.eMailValue.isNullOrEmpty())
                list.add(createSocialItem(SocialItems.MAIL, contentItem.eMailValue!!))
        }

        return list
    }

    protected open fun createSocialItem(@SocialItems.Type type: Int, value: String): SocialItems.SocialToolbarItem
    {
        return when (type)
        {
            SocialItems.FACEBOOK -> SocialItems.FacebookSocialToolbarItem(R.string.faceboook, R.drawable.ic_facebook, value)
            SocialItems.TWITTER -> SocialItems.SocialToolbarItem(R.string.twitter, R.drawable.ic_twitter, value)
            SocialItems.PINTEREST -> SocialItems.SocialToolbarItem(R.string.pinterest, R.drawable.ic_pinterest, value)
            SocialItems.INSTANGRAM -> SocialItems.SocialToolbarItem(R.string.instagram, R.drawable.ic_instagram, value)
            SocialItems.WEBSITE -> SocialItems.SocialToolbarItem(R.string.website, R.drawable.ic_public, value)
            SocialItems.CALL -> SocialItems.CallSocialToolbarItem(R.string.call, R.drawable.ic_call, value)
            SocialItems.MAIL -> SocialItems.MailSocialToolbarItem(R.string.mail, R.drawable.ic_email, value)
            else -> SocialItems.SocialToolbarItem(0, 0, value)
        }
    }

    override fun onClick(p0: View?) {
        val detailToolbarItem = p0?.tag as? SocialItems.SocialToolbarItem
        detailToolbarItem?.doWork(context)
    }

    override fun onPreDraw(): Boolean {
        socialRootView.viewTreeObserver.removeOnPreDrawListener(this)

        var totItemsWidth = 0
        for (i in 0 until socialRootView.childCount) {
            val itemWidth = socialRootView.getChildAt(i).measuredWidth
            totItemsWidth += itemWidth

            val diff = totItemsWidth - measuredWidth

            //if the items total width are more length than the width of the screen, then the last item must be
            //visible for half, so the user show that is possible to scroll to the right.
            //so if the last item is visible from the start to the half,
            //distribute the space from the half of the last item - 1 to the right of the screen to all the children
            //otherwise if the last item is visible from the half to the end, distribute the space
            //from the half of the last item to the end of the screen.
            //in this manner the width of all the children increases and the last visible child is visible for half
            if (diff > 0)
            {
                var d = ((itemWidth / 2) - diff)
                d = if (d < 0)
                {
                    (((itemWidth - diff) + (socialRootView.getChildAt(i - 1).measuredWidth / 2)) / (i - 0.5)).toInt()
                }
                else
                {
                    (d / (i + 0.5)).toInt()
                }

                for (z in 0 until socialRootView.childCount)
                {
                    socialRootView.getChildAt(z).layoutParams.width = socialRootView.getChildAt(z).measuredWidth + d
                }
                socialRootView.requestLayout()
                break
            }
        }

        if (totItemsWidth in 1..measuredWidth) {
            val params = socialRootView.layoutParams
            params.width = LinearLayout.LayoutParams.MATCH_PARENT
            socialRootView.layoutParams = params

            val scrollView = (socialRootView.parent as? HorizontalScrollView)

            if (scrollView != null) {
                scrollView.removeView(socialRootView)
                removeView(scrollView)
                addView(socialRootView)
            }
        }
        return false
    }
}