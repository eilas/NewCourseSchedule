package com.eilas.newcourseschedule.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.RemoteViews
import com.eilas.newcourseschedule.R

/**
 * Implementation of App Widget functionality.
 */
class DesktopWidgetProvider : AppWidgetProvider() {
    var views: RemoteViews? = null

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            // Construct the RemoteViews object
            views = RemoteViews(context.packageName, R.layout.desktop_widget_provider)
//            progress bar
            views!!.setProgressBar(R.id.progressBar, 100, 50, false)
//            list view
            views!!.setRemoteAdapter(
                R.id.desktopListView,
                Intent(context, ListRemoteViewsService::class.java).apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                }
            )

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)

            Log.i("widget", "update app widget finished")

        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE))
            AppWidgetManager.getInstance(context).apply {
                val appWidgetId = intent?.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID
                )

                val intent1 = Intent(context, ListRemoteViewsService::class.java)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                intent1.data = Uri.parse(intent1.toUri(Intent.URI_INTENT_SCHEME))

                appWidgetId?.let {
                    val remoteViews =
                        RemoteViews(context?.packageName, R.layout.desktop_widget_provider)
                    remoteViews.setRemoteAdapter(it, R.id.desktopListView, intent1)
                    val appWidgetIds = this.getAppWidgetIds(context?.let { it1 ->
                        ComponentName(it1, DesktopWidgetProvider::class.java)
                    })
                    this.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.desktopListView)


                    updateAppWidget(it, views)
                    notifyAppWidgetViewDataChanged(it, R.id.desktopListView)
                }
            }

        super.onReceive(context, intent)
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}