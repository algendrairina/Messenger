//==============================================================================================================================
package com.app.messenger;


//==============================================================================================================================
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

import me.leolin.shortcutbadger.ShortcutBadger;


//==============================================================================================================================
public class AppIconManager
{



    //--------------------------------------------------------------------------------------------------------------------------
    public static void setBadgeValue(Context context, int messagesNumber)
    {
        ShortcutBadger.with(context.getApplicationContext()).count(messagesNumber);
    }

    //--------------------------------------------------------------------------------------------------------------------------





    //--------------------------------------------------------------------------------------------------------------------------
    public static void showAppropriateIcon(Context context, int messagesNumber)
    {
        if (messagesNumberValid(messagesNumber))
            showNotifyIcon(context, messagesNumber);

        else
            showMainIcon(context);
    }

    //--------------------------------------------------------------------------------------------------------------------------
    private static void showMainIcon(Context context)
    {
        hideAllIcons(context);

        setComponentState(context, BUNDLE_SPLASH_MAIN, PackageManager.COMPONENT_ENABLED_STATE_ENABLED);
    }

    //--------------------------------------------------------------------------------------------------------------------------
    private static void showNotifyIcon(Context context, int messagesCount)
    {
        hideAllIcons(context);

        setComponentState(context, BUNDLE_SPLASH_ALIAS_TEMPLATE + activitySuffix(messagesCount),PackageManager.COMPONENT_ENABLED_STATE_ENABLED);
    }

    //--------------------------------------------------------------------------------------------------------------------------
    private static void hideAllIcons(Context context)
    {
        for (int i = SUFFIX_INDEX_BEGIN; i != SUFFIX_INDEX_END; ++i)
            setComponentState(context, BUNDLE_SPLASH_ALIAS_TEMPLATE + SUFFIXES[i],
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED);

        setComponentState(context, BUNDLE_SPLASH_MAIN, PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
    }

    //--------------------------------------------------------------------------------------------------------------------------
    private static void setComponentState(Context context, String name, int state)
    {
        ComponentName componentName = new ComponentName(BUNDLE_APP, name);

        PackageManager packageManager = context.getApplicationContext().getPackageManager();

        packageManager.setComponentEnabledSetting(componentName, state, PackageManager.DONT_KILL_APP);
    }


//    PackageManager pm          = context.getPackageManager();
//    String         lastEnabled = "";
//
//    lastEnabled = "com.app.messenger.SplashActivity";
//
//    ComponentName componentName = new ComponentName("com.app.messenger", lastEnabled);
//
//    pm.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
//    PackageManager.DONT_KILL_APP);
//
//    lastEnabled = "com.app.messenger.Splash2";
//
//    componentName = new ComponentName("com.app.messenger", lastEnabled);
//    pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
//    PackageManager.DONT_KILL_APP);

    //--------------------------------------------------------------------------------------------------------------------------
    private static boolean messagesNumberValid(int messagesNumber)
    {
        return messagesNumber > 0;
    }

    //--------------------------------------------------------------------------------------------------------------------------
    private static String activitySuffix (int messagesNumber)
    {
        final int MESSAGES_NUMBER_1  = 1;
        final int MESSAGES_NUMBER_10 = 10;
        final int MESSAGES_NUMBER_20 = 20;
        final int MESSAGES_NUMBER_30 = 30;
        final int MESSAGES_NUMBER_40 = 40;
        final int MESSAGES_NUMBER_50 = 50;
        final int MESSAGES_NUMBER_99 = 99;

        if (messagesNumber >= MESSAGES_NUMBER_1 && messagesNumber < MESSAGES_NUMBER_10)
            return SUFFIXES[SUFFIX_INDEX_BEGIN + messagesNumber - 1];

        if (messagesNumber >= MESSAGES_NUMBER_10 && messagesNumber < MESSAGES_NUMBER_20)
            return SUFFIXES[SUFFIX_INDEX_TEN_PLUS];

        if (messagesNumber >= MESSAGES_NUMBER_20 && messagesNumber < MESSAGES_NUMBER_30)
            return SUFFIXES[SUFFIX_INDEX_TWENTY_PLUS];

        if (messagesNumber >= MESSAGES_NUMBER_30 && messagesNumber < MESSAGES_NUMBER_40)
            return SUFFIXES[SUFFIX_INDEX_THIRTY_PLUS];

        if (messagesNumber >= MESSAGES_NUMBER_40 && messagesNumber < MESSAGES_NUMBER_50)
            return SUFFIXES[SUFFIX_INDEX_FOURTY_PLUS];

        if (messagesNumber >= MESSAGES_NUMBER_50 && messagesNumber < MESSAGES_NUMBER_99)
            return SUFFIXES[SUFFIX_INDEX_FIFTY_PLUS];

        if (messagesNumber >= MESSAGES_NUMBER_99)
            return SUFFIXES[SUFFIX_INDEX_NINETY_NINE_PLUS];

        return "";
    }

    //--------------------------------------------------------------------------------------------------------------------------
    private final static String[] SUFFIXES = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "20", "30", "40", "50", "99"};

    private final static String BUNDLE_APP                   = "com.app.messenger";
    private final static String BUNDLE_SPLASH_MAIN           = "com.app.messenger.SplashActivity";
    private final static String BUNDLE_SPLASH_ALIAS_TEMPLATE = "com.app.messenger.Splash";

    private final static int SUFFIX_INDEX_BEGIN              = 0;
    private final static int SUFFIX_INDEX_TEN_PLUS           = 9;
    private final static int SUFFIX_INDEX_TWENTY_PLUS        = 10;
    private final static int SUFFIX_INDEX_THIRTY_PLUS        = 11;
    private final static int SUFFIX_INDEX_FOURTY_PLUS        = 12;
    private final static int SUFFIX_INDEX_FIFTY_PLUS         = 13;
    private final static int SUFFIX_INDEX_NINETY_NINE_PLUS   = 14;

    private final static int SUFFIX_INDEX_END                = 15;
}
