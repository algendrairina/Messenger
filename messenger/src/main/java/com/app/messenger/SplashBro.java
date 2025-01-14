//==============================================================================================================================
package com.app.messenger;


//==============================================================================================================================
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;


//==============================================================================================================================
public class SplashBro extends Activity
{

    //--------------------------------------------------------------------------------------------------------------------------
    public String getPathfromUri(Uri uri)
    {
        if (uri.toString().startsWith("file://"))
            return uri.getPath();

        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor   cursor     = managedQuery(uri, projection, null, null, null);

        startManagingCursor(cursor);

        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();

        String path = cursor.getString(columnIndex);

        return path;
    }

    //--------------------------------------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash_bro);

        sharedPref_ = getSharedPreferences("login", MODE_PRIVATE);

        if (sharedPref_.contains("icon"))
        {
            Editor ed = sharedPref_.edit();

            ed.remove("icon");
            ed.apply();

            AppIconManager.setBadgeValue(this, 0);

            NotificationManager notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notifManager.cancel(sharedPref_.getInt("icon", 0));
        }

        Intent intent = getIntent();

        if (Intent.ACTION_SEND.equals(intent.getAction()))
        {
            Uri uri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);

            if (uri != null)
            {
                ChatOneToOne.setShareImage(getPathfromUri(uri));
            }
        }

        Intent notificationIntent = new Intent(getApplicationContext(), SplashActivity.class);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(notificationIntent);

        finish();
    }

    //--------------------------------------------------------------------------------------------------------------------------
    private SharedPreferences sharedPref_;
}
