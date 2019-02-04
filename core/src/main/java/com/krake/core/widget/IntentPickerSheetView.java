package com.krake.core.widget;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.krake.core.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class IntentPickerSheetView extends FrameLayout {

    protected final List<ActivityInfo> mixins = new ArrayList<>();
    protected Intent intent;
    protected GridView appGrid;
    protected Adapter adapter;
    protected Filter filter = new FilterNone();
    protected Comparator<ActivityInfo> sortMethod = new SortAlphabetically();
    private int columnWidthDp = 100;

    public IntentPickerSheetView(Context context) {
        super(context);
        initialize(context);
    }

    public IntentPickerSheetView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public IntentPickerSheetView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public IntentPickerSheetView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize(context);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (adapter != null) {
            for (ActivityInfo activityInfo : adapter.activityInfos) {
                if (activityInfo.iconLoadTask != null) {
                    activityInfo.iconLoadTask.cancel(true);
                    activityInfo.iconLoadTask = null;
                }
            }
        }
    }

    protected void initialize(Context context) {

        inflate(context, R.layout.grid_sheet_view, this);
        appGrid = findViewById(R.id.grid);

        ViewCompat.setElevation(this, dp2px(getContext(), 16f));
    }

    public void setOnIntentPicked(@NonNull final OnIntentPickedListener listener) {
        appGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listener.onIntentPicked(adapter.getItem(position));
            }
        });
    }

    public void setSortMethod(Comparator<ActivityInfo> sortMethod) {
        this.sortMethod = sortMethod;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public void setColumnWidthDp(int columnWidthDp) {
        this.columnWidthDp = columnWidthDp;
    }

    public List<ActivityInfo> getMixins() {
        return this.mixins;
    }

    /**
     * Adds custom mixins to the resulting picker sheet
     *
     * @param infos Custom ActivityInfo classes to mix in
     */
    public void setMixins(@NonNull List<ActivityInfo> infos) {
        mixins.clear();
        mixins.addAll(infos);
    }

    public void setIntent(@NonNull Intent intent) {
        this.intent = intent;
        this.adapter = new Adapter(getContext(), intent, mixins);
        appGrid.setAdapter(this.adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        final float density = getResources().getDisplayMetrics().density;
        getResources().getDimensionPixelSize(R.dimen.bottom_sheet_default_sheet_width);
        appGrid.setNumColumns((int) (width / (columnWidthDp * density)));
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * Convert a dp float value to pixels
     *
     * @param dp float value in dps to convert
     * @return DP value converted to pixels
     */
    private int dp2px(Context context, float dp) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
        return Math.round(px);
    }

    public interface Filter {
        boolean include(ActivityInfo info);
    }

    public interface OnIntentPickedListener {
        void onIntentPicked(ActivityInfo activityInfo);
    }

    /**
     * Represents an item in the picker grid
     */
    public static class ActivityInfo {
        public final String label;
        public final ComponentName componentName;
        public final ResolveInfo resolveInfo;
        public Drawable icon;
        public Object tag;
        private AsyncTask<Void, Void, Drawable> iconLoadTask;

        public ActivityInfo(Drawable icon, String label, Context context, Class<?> clazz) {
            this.icon = icon;
            resolveInfo = null;
            this.label = label;
            this.componentName = new ComponentName(context, clazz.getName());
        }

        ActivityInfo(ResolveInfo resolveInfo, CharSequence label, ComponentName componentName) {
            this.resolveInfo = resolveInfo;
            this.label = label.toString();
            this.componentName = componentName;
        }

        public Intent getConcreteIntent(Intent intent) {
            Intent concreteIntent = new Intent(intent);
            concreteIntent.setComponent(componentName);
            return concreteIntent;
        }
    }

    private class SortAlphabetically implements Comparator<ActivityInfo> {
        @Override
        public int compare(ActivityInfo lhs, ActivityInfo rhs) {
            return lhs.label.compareTo(rhs.label);
        }
    }

    private class FilterNone implements Filter {
        @Override
        public boolean include(ActivityInfo info) {
            return true;
        }
    }

    private class Adapter extends BaseAdapter {

        final List<ActivityInfo> activityInfos;
        final LayoutInflater inflater;
        private PackageManager packageManager;

        public Adapter(Context context, Intent intent, List<ActivityInfo> mixins) {
            inflater = LayoutInflater.from(context);
            packageManager = context.getPackageManager();
            List<ResolveInfo> infos = packageManager.queryIntentActivities(intent, 0);
            activityInfos = new ArrayList<>(infos.size() + mixins.size());
            activityInfos.addAll(mixins);
            for (ResolveInfo info : infos) {
                ComponentName componentName = new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
                ActivityInfo activityInfo = new ActivityInfo(info, info.loadLabel(packageManager), componentName);
                if (filter.include(activityInfo)) {
                    activityInfos.add(activityInfo);
                }
            }
            Collections.sort(activityInfos, sortMethod);
        }

        @Override
        public int getCount() {
            return activityInfos.size();
        }

        @Override
        public ActivityInfo getItem(int position) {
            return activityInfos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return activityInfos.get(position).componentName.hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.sheet_grid_item, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final ActivityInfo info = activityInfos.get(position);
            if (info.iconLoadTask != null) {
                info.iconLoadTask.cancel(true);
                info.iconLoadTask = null;
            }
            if (info.icon != null) {
                holder.icon.setImageDrawable(info.icon);
            } else {
                holder.icon.setImageDrawable(getResources().getDrawable(R.color.divider_gray));
                info.iconLoadTask = new AsyncTask<Void, Void, Drawable>() {
                    @Override
                    protected Drawable doInBackground(@NonNull Void... params) {
                        return info.resolveInfo.loadIcon(packageManager);
                    }

                    @Override
                    protected void onPostExecute(@NonNull Drawable drawable) {
                        info.icon = drawable;
                        info.iconLoadTask = null;
                        holder.icon.setImageDrawable(drawable);
                    }
                };
                info.iconLoadTask.execute();
            }
            holder.label.setText(info.label);

            return convertView;
        }

        class ViewHolder {
            final ImageView icon;
            final TextView label;

            ViewHolder(View root) {
                icon = root.findViewById(R.id.icon);
                label = root.findViewById(R.id.label);
            }
        }

    }
}
