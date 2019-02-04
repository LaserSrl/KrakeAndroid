package com.krake.core.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.krake.core.R;

/**
 * Helper class that generates and manages a {@link TabLayout}.
 * The layout is built through two possible builder classes.
 * <li>
 * <ul>{@link InflaterBuilder}: the {@link TabLayout} is inflated</ul>
 * <il>{@link CreationBuilder}: the {@link TabLayout} is generated programmatically</il>
 * </li>
 */
public final class TabLayoutHelper {
    private TabLayout mTabLayout;
    private TabConfig mTabConfig;

    /**
     * Creates a new instance of {@link TabLayoutHelper} to manage a {@link TabLayout}
     *
     * @param tabLayout generated layout with {@link ViewGroup.LayoutParams}
     * @param tabConfig configurations related to the {@link TabLayout.Tab} view
     */
    private TabLayoutHelper(@NonNull TabLayout tabLayout, @NonNull TabConfig tabConfig) {
        mTabLayout = tabLayout;
        mTabConfig = tabConfig;
    }

    /**
     * Add the {@link TabLayout} in a container
     *
     * @param root  view in which the {@link TabLayout} will be added
     * @param index index inside the {@link ViewGroup} at which the {@link TabLayout} will be added
     */
    public void addToView(@NonNull ViewGroup root, int index) {
        root.addView(mTabLayout, index);
    }

    /**
     * Add a {@link TabLayout.Tab} to the {@link TabLayout} at the last index with a null tag.
     * The {@link TabLayout.Tab} will be selected if it's the first added tab to the {@link TabLayout}.
     * The text and the icon will have the same selector (also if the {@link TabLayout.Tab} has a custom view).
     *
     * @param text text that will be shown on the tab
     * @param icon icon that will be shown on the tab
     * @return the added {@link TabLayout.Tab}
     */
    public TabLayout.Tab addTab(@Nullable String text, @Nullable Drawable icon) {
        return addTab(text, icon, null);
    }

    /**
     * Add a {@link TabLayout.Tab} to the {@link TabLayout} at the last index.
     * The {@link TabLayout.Tab} will be selected if it's the first added tab to the {@link TabLayout}.
     * The text and the icon will have the same selector (also if the {@link TabLayout.Tab} has a custom view).
     *
     * @param text text that will be shown on the tab
     * @param icon icon that will be shown on the tab
     * @param tag  tag related to the tab
     * @return the added {@link TabLayout.Tab}
     */
    public TabLayout.Tab addTab(@Nullable String text, @Nullable Drawable icon, @Nullable Object tag) {
        return addTab(text, icon, tag, mTabLayout.getTabCount() == 0);
    }

    /**
     * Add a {@link TabLayout.Tab} to the {@link TabLayout} at the last index.
     * The text and the icon will have the same selector (also if the {@link TabLayout.Tab} has a custom view).
     *
     * @param text     text that will be shown on the tab
     * @param icon     icon that will be shown on the tab
     * @param tag      tag related to the tab
     * @param selected true if the tab will be selected after it was added
     * @return the added {@link TabLayout.Tab}
     */
    public TabLayout.Tab addTab(@Nullable String text, @Nullable Drawable icon, @Nullable Object tag, boolean selected) {
        // creates a new tab
        TabLayout.Tab tab = mTabLayout.newTab();
        tab.setTag(tag);

        @LayoutRes Integer customViewRes = mTabConfig.customLayout;
        // set a custom view to the tab
        if (customViewRes != null) {
            tab.setCustomView(customViewRes);
        }
        View customView;
        if (showTabTitle() && (customView = tab.getCustomView()) != null) {
            // tint the custom TextView with id android:id/text1
            tintCustomText(customView);
        }

        if (showTabTitle()) {
            setTabTitle(tab, text);
        }

        if (showTabImage()) {
            setTabIcon(tab, icon);
        }

        // add tab to the TabLayout at last index
        mTabLayout.addTab(tab, selected);
        return tab;
    }

    /**
     * Set the {@link TabLayout.Tab}'s title
     *
     * @param tab  tab in which the title will be set
     * @param text text to set
     */
    public void setTabTitle(@NonNull TabLayout.Tab tab, @Nullable String text) {
        tab.setText(text);
    }

    /**
     * Set the {@link TabLayout.Tab}'s icon and tint it with the {@link TabLayout}'s content selector
     *
     * @param tab  tab in which the icon will be set
     * @param icon icon to set
     */
    public void setTabIcon(@NonNull TabLayout.Tab tab, @Nullable Drawable icon) {
        if (icon != null) {
            tintIcon(icon);
        }
        tab.setIcon(icon);
    }

    /**
     * @return true if the tabs' title can be shown
     */
    public boolean showTabTitle() {
        return mTabConfig.showTitle;
    }

    /**
     * @return true if the tabs' icon can be shown
     */
    public boolean showTabImage() {
        return mTabConfig.showImage;
    }

    /**
     * @return the {@link TabLayout} managed in this helper class
     */
    public TabLayout layout() {
        return mTabLayout;
    }

    /**
     * Tint all tabs' content with the {@link TabLayout}'s content selector.
     * Every time a {@link TabLayout.Tab} is added, its content is tinted so this method is useful only to invalidate the tint process.
     */
    @SuppressWarnings("ConstantConditions")
    public void tintTabs() {
        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            final TabLayout.Tab tab = mTabLayout.getTabAt(i);

            final View customView;
            if (showTabTitle() && (customView = tab.getCustomView()) != null) {
                // tint the custom TextView with id android:id/text1
                tintCustomText(customView);
            }

            final Drawable icon;
            if (showTabImage() && (icon = tab.getIcon()) != null) {
                tintIcon(icon);
            }
        }
    }

    /**
     * Tint the icon with the {@link TabLayout}'s content selector.
     * The icon can be tinted only if the subclass of {@link Drawable} implements {@link Drawable#setTintList(ColorStateList)}.
     *
     * @param icon image to tint
     */
    private void tintIcon(@NonNull Drawable icon) {
        icon = DrawableCompat.wrap(icon);
        // mutate the Drawable to avoid an immutable change in the whole app
        DrawableCompat.setTintList(icon.mutate(), mTabLayout.getTabTextColors());
    }

    /**
     * Tint the custom {@link TextView} with id {@link android.R.id#text1} with the {@link TabLayout}'s content selector.
     *
     * @param customView custom {@link TabLayout.Tab}'s view in which the {@link TextView} will be found
     */
    private void tintCustomText(@NonNull View customView) {
        TextView textView = customView.findViewById(android.R.id.text1);
        textView.setTextColor(mTabLayout.getTabTextColors());
    }

    /**
     * Creates an instance of {@link TabLayoutHelper} generating a {@link TabLayout} with a {@link LayoutInflater}
     */
    public final static class InflaterBuilder extends BaseBuilder<InflaterBuilder> {
        @LayoutRes
        Integer layout;

        /**
         * Creates an instance of {@link InflaterBuilder} to inflate a {@link TabLayout}
         *
         * @param context {@link Context} in which the {@link TabLayout}'s view must be created
         */
        public InflaterBuilder(@NonNull Context context) {
            super(context);
        }

        /**
         * Set the layout that will be inflated. This layout must contains a {@link TabLayout} as root view.
         * The <i>layout_width</i> and <i>layout_height</i> properties contained in the XML {@link TabLayout} will be ignored.
         * DEFAULT: null
         *
         * @param layout layout of the {@link TabLayout}
         * @return same instance of {@link InflaterBuilder}
         */
        public InflaterBuilder layout(@LayoutRes int layout) {
            this.layout = layout;
            return this;
        }

        /**
         * Creates a new {@link TabLayoutHelper} with the {@link TabLayout.Tab}'s configs and the generated {@link TabLayout}
         *
         * @return new instance of {@link TabLayoutHelper}
         */
        @Override
        public TabLayoutHelper build() {
            internalBuild();

            final LayoutInflater inflater = LayoutInflater.from(context);

            if (layout == null) {
                throw new IllegalArgumentException("You have to call the method layout()");
            }

            View view = inflater.inflate(layout, null);
            if (!(view instanceof TabLayout)) {
                throw new ClassCastException("The inflated layout must be a " + TabLayout.class.getName());
            }
            TabLayout tabLayout = (TabLayout) view;
            tabLayout.setLayoutParams(layoutParams);
            return new TabLayoutHelper(tabLayout, tabConfig);
        }
    }

    /**
     * Creates an instance of {@link TabLayoutHelper} generating a {@link TabLayout} programmatically
     */
    public final static class CreationBuilder extends BaseBuilder<CreationBuilder> {
        @IdRes
        Integer id;

        @TabLayout.Mode
        Integer mode;

        @ColorInt
        Integer bgColor;

        @ColorInt
        Integer contentSelectedColor;

        @ColorInt
        Integer contentUnselectedColor;

        @ColorInt
        Integer indicatorColor;

        Integer indicatorHeight;

        /**
         * Set the id.
         * DEFAULT: null
         *
         * @param id id that will be assigned to the {@link TabLayout}
         * @return same instance of {@link CreationBuilder}
         */
        public CreationBuilder id(@IdRes int id) {
            this.id = id;
            return this;
        }

        /**
         * Set the tabs' mode.
         * DEFAULT: {@link TabLayout#MODE_FIXED}
         *
         * @param mode tabs' {@link TabLayout.Mode}
         * @return same instance of {@link CreationBuilder}
         */
        public CreationBuilder mode(@TabLayout.Mode int mode) {
            this.mode = mode;
            return this;
        }

        /**
         * Set the background color of the {@link TabLayout}
         * DEFAULT: {@link R.attr#colorPrimary}
         *
         * @param bgColor background resolved color
         * @return same instance of {@link CreationBuilder}
         */
        public CreationBuilder bgColor(@ColorInt int bgColor) {
            this.bgColor = bgColor;
            return this;
        }

        /**
         * Set the color that will be related to the selected content (image, title)
         * DEFAULT: {@link R.color#tab_selected_text_color}
         *
         * @param contentSelectedColor resolved color that will be the tint for the selected content (image, title)
         * @return same instance of {@link CreationBuilder}
         */
        public CreationBuilder contentSelectedColor(@ColorInt int contentSelectedColor) {
            this.contentSelectedColor = contentSelectedColor;
            return this;
        }

        /**
         * Set the color that will be related to the selected content (image, title)
         * DEFAULT: {@link R.color#tab_selected_text_color}
         *
         * @param contentUnselectedColor resolved color that will be the tint for the unselected content (image, title)
         * @return same instance of {@link CreationBuilder}
         */
        public CreationBuilder contentUnselectedColor(@ColorInt int contentUnselectedColor) {
            this.contentUnselectedColor = contentUnselectedColor;
            return this;
        }


        /**
         * Set the color of the indicator that will be shown below the tabs
         * DEFALT: null
         *
         * @param indicatorColor resolved color of the indicator below the tabs
         * @return same instance of {@link CreationBuilder}
         */
        public CreationBuilder indicatorColor(@ColorInt int indicatorColor) {
            this.indicatorColor = indicatorColor;
            return this;
        }

        /**
         * Set the indicator height in pixels
         *
         * @param indicatorHeight indicator height
         * @return same instance of {@link CreationBuilder}
         */
        public CreationBuilder indicatorHeight(@Px int indicatorHeight) {
            this.indicatorHeight = indicatorHeight;
            return this;
        }

        public CreationBuilder(@NonNull Context context) {
            super(context);
        }

        /**
         * Creates a new {@link TabLayoutHelper} with the {@link TabLayout.Tab}'s configs and the generated {@link TabLayout}
         *
         * @return new instance of {@link TabLayoutHelper}
         */
        @Override
        public TabLayoutHelper build() {
            internalBuild();

            if (contentSelectedColor == null) {
                contentSelectedColor = ContextCompat.getColor(context, R.color.tab_selected_text_color);
            }

            if (contentUnselectedColor == null) {
                contentUnselectedColor = ContextCompat.getColor(context, R.color.tab_unselected_text_color);
            }

            if (bgColor == null) {
                final Resources.Theme theme = context.getTheme();
                TypedValue value = new TypedValue();
                // get the color primary from the app theme
                theme.resolveAttribute(R.attr.colorPrimary, value, true);
                bgColor = value.data;
            }

            if (indicatorHeight == null) {
                indicatorHeight = context.getResources().getDimensionPixelSize(R.dimen.tab_indicator_height);
            }

            final TabLayout tabLayout = new TabLayout(context);
            if (id != null) {
                tabLayout.setId(id);
            }
            if (mode != null) {
                tabLayout.setTabMode(mode);
            }
            tabLayout.setBackgroundColor(bgColor);
            if (indicatorColor != null) {
                tabLayout.setSelectedTabIndicatorColor(indicatorColor);
            }
            tabLayout.setSelectedTabIndicatorHeight(indicatorHeight);
            tabLayout.setTabTextColors(contentUnselectedColor, contentSelectedColor);
            tabLayout.setLayoutParams(layoutParams);
            return new TabLayoutHelper(tabLayout, tabConfig);
        }
    }

    /**
     * Abstract basic builder used to generate a {@link TabLayout}.
     * It provides the common methods between the {@link InflaterBuilder} and {@link CreationBuilder}.
     */
    @SuppressWarnings("unchecked")
    public abstract static class BaseBuilder<T extends BaseBuilder<T>> {
        protected Context context;
        ViewGroup.LayoutParams layoutParams;
        TabConfig tabConfig;

        /**
         * Creates an instance of {@link BaseBuilder} to inflate a {@link TabLayout}
         *
         * @param context {@link Context} in which the {@link TabLayout}'s view must be created
         */
        BaseBuilder(@NonNull Context context) {
            this.context = context;
            tabConfig = new TabConfig();
        }

        /**
         * Set the {@link ViewGroup.LayoutParams} that will be attached to the generated {@link TabLayout}
         * DEFAULT: width -> MATCH_PARENT, height -> WRAP_CONTENT
         *
         * @param layoutParams params related to the {@link TabLayout}
         * @return same instance of {@link BaseBuilder}
         */
        public T layoutParams(@NonNull ViewGroup.LayoutParams layoutParams) {
            this.layoutParams = layoutParams;
            return (T) this;
        }

        /**
         * Set a custom layout for the {@link TabLayout.Tab}'s view
         * DEFAULT: null
         *
         * @param tabCustomLayout custom layout resource
         * @return same instance of {@link BaseBuilder}
         */
        public T tabCustomView(@LayoutRes int tabCustomLayout) {
            tabConfig.customLayout = tabCustomLayout;
            return (T) this;
        }

        /**
         * Toggle the visibility of the {@link TabLayout.Tab}'s title.
         * DEFAULT: true
         *
         * @param tabShowTitle true if the {@link TabLayout.Tab}'s title will be shown
         * @return same instance of {@link BaseBuilder}
         */
        public T tabShowTitle(boolean tabShowTitle) {
            tabConfig.showTitle = tabShowTitle;
            return (T) this;
        }

        /**
         * Toggle the visibility of the {@link TabLayout.Tab}'s icon.
         * DEFAULT: false
         *
         * @param tabShowImage true if the {@link TabLayout.Tab}'s icon will be shown
         * @return same instance of {@link BaseBuilder}
         */
        public T tabShowImage(boolean tabShowImage) {
            tabConfig.showImage = tabShowImage;
            return (T) this;
        }

        /**
         * Common build that must be called inside {@link #build()} implementation
         */
        void internalBuild() {
            if (layoutParams == null) {
                layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }

            if (tabConfig.showTitle == null) {
                tabConfig.showTitle = true;
            }

            if (tabConfig.showImage == null) {
                tabConfig.showImage = false;
            }

            if (!tabConfig.showTitle && !tabConfig.showImage) {
                throw new IllegalArgumentException("You must show the title or the image in a " + TabLayout.Tab.class.getCanonicalName());
            }
        }

        /**
         * Creates a new {@link TabLayoutHelper} with the {@link TabLayout.Tab}'s configs and the generated {@link TabLayout}
         *
         * @return new instance of {@link TabLayoutHelper}
         */
        public abstract TabLayoutHelper build();
    }

    /**
     * Helper class to avoid the implementation of unused method of {@link TabLayout.OnTabSelectedListener}
     */
    public static abstract class OnTabSelectedListenerHelper implements TabLayout.OnTabSelectedListener {

        /**
         * Called when a tab enters the selected state.
         *
         * @param tab The tab that was selected
         */
        @Override
        public void onTabSelected(TabLayout.Tab tab) { /* empty */ }

        /**
         * Called when a tab exits the selected state.
         *
         * @param tab The tab that was unselected
         */
        @Override
        public void onTabUnselected(TabLayout.Tab tab) { /* empty */ }

        /**
         * Called when a tab that is already selected is chosen again by the user. Some applications
         * may use this action to return to the top level of a category.
         *
         * @param tab The tab that was reselected.
         */
        @Override
        public void onTabReselected(TabLayout.Tab tab) { /* empty */ }
    }

    /**
     * Manages {@link TabLayout.Tab}'s configurations
     */
    private static class TabConfig {
        @LayoutRes
        Integer customLayout;
        Boolean showImage;
        Boolean showTitle;
    }
}