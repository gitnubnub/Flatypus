package si.uni_lj.fe.tnuv.flatypus.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.content.ContextCompat;

import si.uni_lj.fe.tnuv.flatypus.MainActivity;
import si.uni_lj.fe.tnuv.flatypus.R;
import si.uni_lj.fe.tnuv.flatypus.ui.home.HomeViewModel;

public class WidgetProvider extends AppWidgetProvider {

    public static final String ACTION_HEART_COUNT_UPDATED = "si.uni_lj.fe.tnuv.flatypus.HEART_COUNT_UPDATED";
    private static final String TAG = "WidgetProvider";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        // Use application context for persistent registration
        Context appContext = context.getApplicationContext();
        IntentFilter filter = new IntentFilter(ACTION_HEART_COUNT_UPDATED);
        ContextCompat.registerReceiver(appContext, this, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
        Log.d(TAG, "Receiver registered with context: " + appContext);

        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(appContext, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_HEART_COUNT_UPDATED.equals(intent.getAction())) {
            int heartCount = intent.getIntExtra("heartCount", HomeViewModel.getCurrentHeartCount());
            Log.d(TAG, "Received heartCount: " + heartCount + ", Intent extra: " + intent.getIntExtra("heartCount", -1) + ", Static value: " + HomeViewModel.getCurrentHeartCount());
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, WidgetProvider.class));
            for (int appWidgetId : appWidgetIds) {
                updateAppWidget(context.getApplicationContext(), appWidgetManager, appWidgetId, heartCount);
            }
        }
    }

    @Override
    public void onDisabled(Context context) {
        context.unregisterReceiver(this);
    }

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        updateAppWidget(context, appWidgetManager, appWidgetId, HomeViewModel.getCurrentHeartCount());
    }

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, int heartCount) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        views.setOnClickPendingIntent(R.id.openApplication, pendingIntent);

        views.setTextViewText(R.id.heart_count, String.valueOf(heartCount > 0 ? heartCount : "0"));
        Log.d(TAG, "Updating widget with heartCount: " + heartCount);

        appWidgetManager.updateAppWidget(appWidgetId, views);
        // Ensure UI refresh
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.heart_count);
    }

    public static void updateWidgets(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, WidgetProvider.class));
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }
}