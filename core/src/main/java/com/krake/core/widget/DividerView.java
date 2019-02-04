package com.krake.core.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;

import com.fondesa.recyclerviewdivider.RecyclerViewDivider;
import com.krake.core.R;

/**
 * View che costruisce il divider per una RecyclerView che ha in comune lo stesso parent con questa.
 * <br>
 * Questa view non sarà mai visibile perché a prescindere la height e la width verranno impostate a 0.
 * <br>
 * La visibilità del divider viene gestita dall'attributo "visibility" di android che non influirà sulla visibilità di questa view.
 * <br>
 * Le possibilità di configurazione tramite attr sono molteplici:
 * <ul>
 * <li>divColor: colore del divider, ignorato se definito l'attributo "divDrawable" - default: R.color.recycler_view_divider_color</li>
 * <li>divDrawable: drawable del divider, annulla gli effetti dell'attributo "divColor" - default: null</li>
 * <li>divSize: altezza del divider se è orizzontale, larghezza se è verticale - default: R.dimen.recycler_view_divider_size</li>
 * <li>divSize: margine a right/left del divider se è orizzontale, top/bottom se è verticale - default: R.dimen.recycler_view_divider_margin_size</li>
 * <li>divLastItemVisible: booleano che specifica se l'ultimo divider (quello dopo l'ultimo elemento della lista) è visibile o meno - default: true</li>
 * <li>divRecyclerView: id della RecyclerView - default: android.R.id.list</li>
 * <li>divAsSpace: boolean che indica se il divider è un semplice spazio. In questo caso, gli attributi per il colore, la drawable e il margine verrano ignorati - default: false</li>
 * </ul>
 */
public class DividerView extends View implements ViewTreeObserver.OnGlobalLayoutListener {

    /**
     * Builder del divider della RecyclerView
     */
    private RecyclerViewDivider mDivider;

    /**
     * Id della RecyclerView
     */
    private int mRecyclerViewId;

    public DividerView(Context context) {
        super(context);
        init(context, null);
    }

    public DividerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public DividerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DividerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    /**
     * Inizializza tutti i parametri di default per configurare il divider
     *
     * @param context context corrente
     * @param attrs   attributi da xml
     */
    private void init(@NonNull Context context, @Nullable AttributeSet attrs) {
        // crea dei nuovi LayoutParams e setta le dimensioni a 0
        ViewGroup.LayoutParams layoutParams = new RecyclerView.LayoutParams(context, attrs);
        layoutParams.height = 0;
        layoutParams.width = 0;
        setLayoutParams(layoutParams);

        if (getVisibility() == View.VISIBLE && attrs != null) {
            // legge attributi da xml
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DividerView, 0, 0);

            try {
                // inizializza il builder
                RecyclerViewDivider.Builder builder = RecyclerViewDivider.with(context);

                mRecyclerViewId = a.getResourceId(R.styleable.DividerView_divRecyclerView, android.R.id.list);

                boolean asSpace = a.getBoolean(R.styleable.DividerView_divAsSpace, false);
                if (asSpace) {
                    builder.asSpace();
                } else {
                    Drawable drawable = a.getDrawable(R.styleable.DividerView_divDrawable);
                    @ColorInt int color = a.getColor(R.styleable.DividerView_divColor, 0);
                    int marginSize = a.getDimensionPixelSize(R.styleable.DividerView_divMarginSize, 0);

                    if (drawable != null) {
                        builder.drawable(drawable);
                    } else if (color != 0) {
                        // il colore viene settato solo se la drawable è nulla
                        builder.color(color);
                    }

                    if (marginSize != 0) {
                        builder.inset(marginSize, marginSize);
                    }
                }

                int size = a.getDimensionPixelSize(R.styleable.DividerView_divSize, 0);
                if (size != 0) {
                    builder.size(size);
                }

                boolean lastDividerVisible = a.getBoolean(R.styleable.DividerView_divLastItemVisible, false);
                if (!lastDividerVisible) {
                    builder.hideLastDivider();
                }

                mDivider = builder.build();
                // aggiunge un layout listener per avere la callback di quando il layout è stato disegnato completamente
                getViewTreeObserver().addOnGlobalLayoutListener(this);
            } finally {
                a.recycle();
            }
        }
    }

    @Override
    public void onGlobalLayout() {
        ViewParent parent = getParent();
        if (parent != null && parent instanceof ViewGroup) {

            View view = ((ViewGroup) parent).findViewById(mRecyclerViewId);
            if (view != null && view instanceof RecyclerView) {
                // rimuove il listener per evitare chiamate aggiuntive
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                // setta il divider sulla RecyclerView
                mDivider.addTo((RecyclerView) view);
            }
        }
    }
}