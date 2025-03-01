//==============================================================================================================================
package com.app.messenger;


//==============================================================================================================================
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

//------------------------------------------------------------------------------------------------------------------------------
import com.app.util.GlobalUtills;
import com.app.util.NetworkCheck;
import com.app.util.loginStatus_notification;
import com.facebook.widget.LoginButton;

//------------------------------------------------------------------------------------------------------------------------------
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;


//==============================================================================================================================
public class SettingActivity extends Activity implements OnItemSelectedListener
{

    //--------------------------------------------------------------------------------------------------------------------------
    @Override
    public void onBackPressed()
    {
        Intent gotoLogin = new Intent(SettingActivity.this, Tab.class);

        gotoLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(gotoLogin);

        finish();
    }

    //--------------------------------------------------------------------------------------------------------------------------
    @SuppressLint("NewApi")
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        if (click_)
        {
            countrySelectionTxt_.setText(countryName_);

            click_ = false;
        }
        else
            countrySelectionTxt_.setText(countries_.get(position));
    }

    //--------------------------------------------------------------------------------------------------------------------------
    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
    }

    //--------------------------------------------------------------------------------------------------------------------------
    public void gettingValues()
    {
        actionBarCommon_     = (ActionBarCommon) findViewById(R.id.action_bar);
        requestLayout_       = (RelativeLayout)  findViewById(R.id.request_container);
        openSpiner_          = (ImageView)       findViewById(R.id.img_drop_down_group);
        countrySpinner_      = (Spinner)         findViewById(R.id.private_public_spinner);
        countrySelectionTxt_ = (TextView)        findViewById(R.id.text_View_group_selection);
        seekDistance_        = (SeekBar)         findViewById(R.id.seekbarDistance);
        tvDistance_          = (TextView)        findViewById(R.id.current_seekbar_txt);
        tgbtnNotification_   = (ToggleButton)    findViewById(R.id.notification_setting_toggleButton);

        sharedPrefNoti_      = getSharedPreferences("login", MODE_PRIVATE);
    }

    //--------------------------------------------------------------------------------------------------------------------------
    public void gettingAllCounteries()
    {
        countries_.add("All");

        Locale[] locale = Locale.getAvailableLocales();

        String country;

        for (Locale loc : locale)
        {
            country = loc.getDisplayCountry();

            if (country.length() > 0 && !countries_.contains(country))
                countries_.add(country);
        }

        Collections.sort(countries_, String.CASE_INSENSITIVE_ORDER);
    }

    //--------------------------------------------------------------------------------------------------------------------------
    // TODO:
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.setting);

        actionBarCommon_ = new ActionBarCommon(SettingActivity.this, null);

        gettingValues       ();
        gettingAllCounteries();

        global_       = new Global();
        logoutButton_ = (LoginButton) findViewById(R.id.logout_button);

        logoutButton_.setApplicationId  (FACEBOOK_APP_ID);
        logoutButton_.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                new loginStatus_notification().execute("N", global_.getUser_id());

                SharedPreferences sharedChatDataS       = getSharedPreferences("Chat", MODE_PRIVATE);
                Editor            editorsharedChatDataS = sharedChatDataS.edit();

                editorsharedChatDataS.clear ();
                editorsharedChatDataS.commit();

                logoutButton_.setVisibility(View.GONE);

                SharedPreferences sharedPrefFbFriend = getSharedPreferences("FacebookFrnd", MODE_PRIVATE);
                Editor            editorPrefFbFriend = sharedPrefFbFriend.edit();

                editorPrefFbFriend.clear ();
                editorPrefFbFriend.commit();

                GlobalUtills.email_id = "";

                SharedPreferences sharedPref = getSharedPreferences("login", MODE_PRIVATE);
                Editor            editorPref = sharedPref.edit();

                editorPref.clear ();
                editorPref.commit();

                Intent intent = new Intent(SettingActivity.this, PhoneNumberRegistertationScreen.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

                startActivity(intent);

                SettingActivity.this.finish();
            }
        });

        actionBarCommon_.setActionText("Settings");
        actionBarCommon_.rightText().setText("Save");

        preferences_ = getSharedPreferences("CountryPreferences", Context.MODE_PRIVATE);
        countryName_ = preferences_.getString("CountryNAme", "All");

        countrySelectionTxt_.setText(countryName_);

        requestLayout_.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(SettingActivity.this, RequestActivity.class);

                startActivity(intent);

                finish();
            }
        });

        OnClickListener right_ClickListener = new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (!NetworkCheck.getConnectivityStatusString(SettingActivity.this).equalsIgnoreCase("true"))
                    NetworkCheck.openInternetDialog(SettingActivity.this);

                else
                {
                    sharedPrefNoti_ = getSharedPreferences("login", MODE_PRIVATE);

                    Editor editorPref = sharedPrefNoti_.edit();

                    editorPref.putBoolean("noti", notificationEnabled_);
                    editorPref.commit    ();

                    GlobalUtills.allNotification = notificationEnabled_;

                    preferences_ = getSharedPreferences("CountryPreferences", Context.MODE_PRIVATE);

                    SharedPreferences.Editor editor = preferences_.edit();

                    editor.putString("CountryNAme", countrySelectionTxt_.getText().toString());
                    editor.putInt   ("km", distance_);
                    editor.commit   ();

                    Toast.makeText(SettingActivity.this, "Settings Saved Successfully", Toast.LENGTH_LONG).show();

                    Intent gotoLogin = new Intent(SettingActivity.this, Tab.class);

                    gotoLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    startActivity(gotoLogin);

                    finish();
                }
            }
        };

        actionBarCommon_.leftImage().setImageResource(R.drawable.icon_back_arrow);

        OnClickListener left_ClickListener = new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent gotoLogin = new Intent(SettingActivity.this, Tab.class);

                gotoLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(gotoLogin);

                finish();
            }
        };

        actionBarCommon_.setLayoutLeftClickListener (left_ClickListener);
        actionBarCommon_.setLayoutRightClickListener(right_ClickListener);

        ArrayAdapter<String> counteries_adapter = new ArrayAdapter<>(this, R.layout.spinner_layout, countries_);

        countrySpinner_.setAdapter(counteries_adapter);

        openSpiner_.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                countrySpinner_.performClick();
            }
        });

        countrySpinner_.setOnItemSelectedListener(this);

        preferences_ = getSharedPreferences("CountryPreferences", Context.MODE_PRIVATE);

        if (preferences_.contains("km"))
            distance_ = preferences_.getInt("km", 100);

        else
            distance_=100;

        tvDistance_.setText("" + distance_ + " km");

        seekDistance_.setProgress               (distance_);
        seekDistance_.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
        {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                tvDistance_.setText("" + progress + " km");

                distance_ = progress;
            }
        });

        tgbtnNotification_.setChecked                (GlobalUtills.allNotification);
        tgbtnNotification_.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                notificationEnabled_ = isChecked;
            }
        });
    }

    //--------------------------------------------------------------------------------------------------------------------------
    private static String FACEBOOK_APP_ID = "649385061834207";  // TODO:

    //--------------------------------------------------------------------------------------------------------------------------
    private ActionBarCommon   actionBarCommon_;
    private ImageView         openSpiner_;
    private Spinner           countrySpinner_;
    private TextView          countrySelectionTxt_;
    private TextView          tvDistance_;
    private RelativeLayout    requestLayout_;
    private LoginButton       logoutButton_;
    private String            countryName_;
    private SeekBar           seekDistance_;
    private ToggleButton      tgbtnNotification_;
    private int               distance_ = 0;
    private boolean           click_               = true;
    private SharedPreferences preferences_;
    private SharedPreferences sharedPrefNoti_;
    private ArrayList<String> countries_           = new ArrayList<String>();
    private boolean           notificationEnabled_ = true;
    private Global            global_;
}
