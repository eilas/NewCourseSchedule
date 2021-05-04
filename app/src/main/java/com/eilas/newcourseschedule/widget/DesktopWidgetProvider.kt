package com.eilas.newcourseschedule.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.widget.RemoteViews
import com.eilas.newcourseschedule.R

/**
 * Implementation of App Widget functionality.
 */
class DesktopWidgetProvider : AppWidgetProvider() {
    companion object {
        val ACTION_DATASET_UPDATE: String =
            "com.eilas.newcourseschedule.widget.action.DATASET_UPDATE"
    }

    lateinit var remoteViews: RemoteViews

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {

            Log.i("widget appWidgetId in \$onUpdate", appWidgetId.toString())

            // Construct the RemoteViews object
            remoteViews = RemoteViews(context.packageName, R.layout.desktop_widget_provider)
//            textview"当前课程"
            remoteViews.setTextViewText(R.id.desktop_courseName, "当前课程：...")
//            textview"刷新"
            remoteViews.setOnClickPendingIntent(
                R.id.desktopListRefresh,
                PendingIntent.getBroadcast(
                    context,
                    0,
                    Intent().setPackage(context.packageName).setAction(ACTION_DATASET_UPDATE),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
//            list view
            remoteViews.setRemoteAdapter(
                R.id.desktopListView,
                Intent(context, ListRemoteViewsService::class.java).apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                }
            )

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)


            Log.i("widget", "register TIME_TICK")
            context.applicationContext.registerReceiver(this, IntentFilter(Intent.ACTION_TIME_TICK))
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i("widget", "receive intent_action ${intent?.action}")
        when (intent?.action) {
            AppWidgetManager.ACTION_APPWIDGET_UPDATE -> {
                val widgetManager = AppWidgetManager.getInstance(context)
                val appWidgetIds = widgetManager.getAppWidgetIds(context?.let {
                    ComponentName(it, DesktopWidgetProvider::class.java)
                })
                Log.i(
                    "widget appWidgetId in \$onReceive",
                    appWidgetIds.clone().toMutableList().toString()
                )
                widgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.desktopListView)
            }
            ACTION_DATASET_UPDATE -> {
                ListRemoteViewsService.listRemoteViewsFactory.refresh()
            }
            Intent.ACTION_TIME_TICK -> {
                val widgetManager = AppWidgetManager.getInstance(context)
                val appWidgetIds = widgetManager.getAppWidgetIds(context?.let {
                    ComponentName(it, DesktopWidgetProvider::class.java)
                })
                widgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.desktopListView)
                remoteViews.setTextViewText(
                    R.id.desktop_courseName,
                    "当前课程：${ListRemoteViewsService.listRemoteViewsFactory.getActiveItem()}"
                )
                widgetManager.updateAppWidget(appWidgetIds, remoteViews)
            }
        }

        super.onReceive(context, intent)
    }

    override fun onEnabled(context: Context) {

    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

}