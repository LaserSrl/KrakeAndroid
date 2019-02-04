package com.krake.core.widget;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

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
    private boolean mAllowUserDrag = true;


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
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, V child, View directTargetChild, View target, int nestedScrollAxes) {
        return mAllowUserDrag && super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes);
    }

    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, V child, MotionEvent event) {
        return mAllowUserDrag && super.onInterceptTouchEvent(parent, child, event);
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
            if (((AppBarLayout) dependency).getChildAt(0) instanceof CollapsingToolbarLayout) {
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

    /**
     * Ottiene il valore del filtro per i touch event
     *
     * @return true se l BottomSheetBehavior deve intercettare i touch event, false se il touch event deve essere redirezionato al parent
     */
    public boolean isAllowedUserDrag() {
        return mAllowUserDrag;
    }

    /**
     * Setta il filtro per i touch events in modo da controllare il redirezionamento degli stessi
     *
     * @param handleTouch true se l BottomSheetBehavior deve intercettare i touch event, false se il touch event deve essere redirezionato al parent
     */
    public void setAllowUserDrag(boolean handleTouch) {
        this.mAllowUserDrag = handleTouch;
    }

    @Deprecated
    static public abstract class BottomSheetStateCallback extends SafeBottomSheetBehavior.BottomSheetCallback {
    }
}