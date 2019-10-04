package com.krake.core.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.appbar.AppBarLayout;
import com.krake.core.R;

/**
 * SafeBottomSheetBehavior che ridimensiona il BottomSheet container per evitare che quest'ultimo passi sopra l'AppBarLayout.
 * <br/>
 * L'altezza del container viene modificata nel metodo {@link #onDependentViewChanged(CoordinatorLayout, View, View)}, questo vuol dire che la height cambierà ogni volta che verrà
 * richiamato l'onLayout() dell'AppBarLayout.
 * <br/>
 * Un esempio di richiami multipli può essere quello dei tab aggiunti dopo aver scaricato i dati da Orchard. In questi casi il metodo {@link #onDependentViewChanged(CoordinatorLayout, View, View)}
 * viene richiamato prima di mostrare i tab e subito dopo, settando due altezze differenti al BottomSheet container.
 *
 * Per ottenere le notifiche complete è necessario chiamare il metodo {@link #setStateAndNotify(int)}
 */
public class BottomSheetNotUnderActionBehavior<V extends View> extends SafeBottomSheetBehavior<V> {

    private int mTopPadding = 0;


    /**
     * Costruttore di default per istanziare un BottomSheetNotUnderActionBehavior
     */
    public BottomSheetNotUnderActionBehavior() {
        super();
    }

    /**
     * Costruttore di default per l'inflate di un BottomSheetNotUnderActionBehavior da layout
     *
     * @param context Context corrente
     * @param attrs   AttributeSet del tema
     */
    public BottomSheetNotUnderActionBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, V child, View dependency) {

        return dependency instanceof AppBarLayout || super.layoutDependsOn(parent, child, dependency);
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, V child, View dependency) {
        boolean changedSize = false;

        CoordinatorLayout.LayoutParams dependencyLayoutParams = (CoordinatorLayout.LayoutParams) dependency.getLayoutParams();

        if (dependency instanceof AppBarLayout) {
            AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) dependencyLayoutParams.getBehavior();
            View heightView = dependency;

            int newTop;
            if (((AppBarLayout) dependency).getChildAt(0) instanceof ShadowCollapsingToolbarLayout) {
                heightView = dependency.findViewById(R.id.toolbar_actionbar);
                newTop = heightView.getMeasuredHeight();
            } else {
                newTop = heightView.getMeasuredHeight() + behavior.getTopAndBottomOffset();
            }

            if (mTopPadding != newTop && parent.getMeasuredHeight() != 0) {
                mTopPadding = newTop;

                changedSize = true;

                CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) child.getLayoutParams();

                lp.height = parent.getMeasuredHeight() - mTopPadding;
                child.setLayoutParams(lp);
            }
        }

        return super.onDependentViewChanged(parent, child, dependency) || changedSize;
    }
}