//==============================================================================================================================
package com.app.messenger;


//==============================================================================================================================

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

//------------------------------------------------------------------------------------------------------------------------------
import com.app.model.CountryCodeDetail;
import com.app.model.FriendInfo;
import com.app.util.GlobalUtills;
import com.app.util.NetworkCheck;
import com.app.webserviceshandler.WebServiceHandler;
import com.appnext.appnextsdk.AppnextTrack;

//------------------------------------------------------------------------------------------------------------------------------
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

//------------------------------------------------------------------------------------------------------------------------------
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


//==============================================================================================================================
public class SplashActivity extends Activity
{

    //--------------------------------------------------------------------------------------------------------------------------
    public class GetCountryCodes extends AsyncTask<Void, Void, String>
    {

        //----------------------------------------------------------------------------------------------------------------------
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        //----------------------------------------------------------------------------------------------------------------------
        @Override
        protected String doInBackground(Void... params)
        {
//            String jsonString = "";
//
//            SharedPreferences countries = getSharedPreferences("country", MODE_PRIVATE);
//
//            if (countries.contains("data"))
//                jsonString = countries.getString("data", "");
//
//            else
//            {
//                List<NameValuePair> param = new ArrayList<NameValuePair>();
//
//                param.add(new BasicNameValuePair(GlobalConstant.POST_TYPE, "post"));
//                param.add(new BasicNameValuePair(GlobalConstant.U_TYPE, "get_countries"));
//                jsonString = (new WebServiceHandler()).makeServiceCall(com.app.messenger.GlobalConstant.URL,
//                        WebServiceHandler.GET, param);
//            }
//            return jsonString;
            return GlobalUtills.countryCode_jsonString;
        }

        //----------------------------------------------------------------------------------------------------------------------
        @SuppressWarnings("deprecation")
        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);

            if (result.equals(GlobalConstant.ERROR_CODE_STRING))
            {
                GlobalUtills.showToast("try again..!", SplashActivity.this);

                finish();
            }
            else
            {
                try
                {
                    JSONObject jsonObject = new JSONObject(result);
                    String successMessageString = jsonObject.getString(GlobalConstant.MESSAGE);

                    if (successMessageString.contains(GlobalConstant.FAILURE))
                    {
                        final AlertDialog alertDialog = new AlertDialog.Builder(SplashActivity.this).create();

                        alertDialog.setTitle("Information");
                        alertDialog.setMessage("unable to get data\ntrying again...");
                        alertDialog.setButton("OK", new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int which)
                            {
                                alertDialog.dismiss();

                                new GetCountryCodes().execute();
                            }
                        });

                        alertDialog.show();
                    }
                    else if (successMessageString.equalsIgnoreCase(GlobalConstant.SUCCESS))
                    {
                        JSONArray json_array = jsonObject.getJSONArray("countries");

                        if (GlobalUtills.country_code_list.size() < 1)  // TODO:
                        {
                            SharedPreferences countries = getSharedPreferences("country", MODE_PRIVATE);
                            Editor editCountry = countries.edit();

                            editCountry.putString("data", result);
                            editCountry.commit();
                        }

                        for (int i = 0; i < json_array.length(); ++i)
                        {
                            JSONObject temporaryJsonObject = json_array.getJSONObject(i);
                            CountryCodeDetail countryDetailObject = new CountryCodeDetail(
                                    temporaryJsonObject.getInt("country_id"), temporaryJsonObject.getString("name"),
                                    temporaryJsonObject.getString("country_code"), temporaryJsonObject.getString("iso_code"));

                            GlobalUtills.country_code_list.add(countryDetailObject);
                        }

                        Thread thread = new Thread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                try
                                {
                                    Thread.sleep(1500);  // TODO:

                                    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"),
                                            Locale.getDefault());
                                    Date currentLocalTime = calendar.getTime();
                                    SimpleDateFormat date = new SimpleDateFormat("Z");
                                    String localTime = date.format(currentLocalTime);

                                    Global.timeZone = localTime + "";

                                    System.out.println(Global.timeZone + "<--TimeZone----->");  // TODO:

                                    Intent intent;

                                    if (!sharedPref_.contains("UserID"))
                                        intent = waitForNativeAdsAndGetActivity();

                                    else
                                    {
                                        global_.setUser_id(sharedPref_.getString("UserID", ""));

                                        if (!sharedPref_.contains("FbID"))
                                            intent = waitForNativeAdsAndGetActivity();

                                        else
                                        {
                                            GlobalUtills.allNotification = sharedPref_.getBoolean("noti", true);

                                            setFriendInfoJson();

                                            intent = new Intent(SplashActivity.this, Tab.class);
                                        }
                                    }

                                    startActivity(intent);

                                    finish();
                                }
                                catch (InterruptedException e)
                                {
                                    e.printStackTrace();
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }

                            private Intent waitForNativeAdsAndGetActivity() throws InterruptedException
                            {
                                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"),
                                        Locale.getDefault());
                                long startWaitingTime = calendar.getTimeInMillis();

                                while (InAppAds.nativeAdsStatus(Global.nativeAdsProvider()) ==
                                        AdsProvider.NativeAdsStatus.UNDEFINED &&
                                        calendar.getTimeInMillis() <= startWaitingTime + NATIVE_ADS_WAITING_TIMEOUT)
                                {
                                    final long NEXT_CHECK_DELAY = 100;

                                    Thread.sleep(NEXT_CHECK_DELAY);
                                }

                                if (InAppAds.nativeAdsStatus(Global.nativeAdsProvider()) == AdsProvider.NativeAdsStatus.LOADED)
                                    return new Intent(SplashActivity.this, UponInstallActivity.class);

                                return new Intent(SplashActivity.this, PhoneNumberRegistertationScreen.class);
                            }
                        });

                        thread.start();
                    }


                }
                catch (JSONException e)
                {
                    System.out.println("Exception in the main object");

                    e.printStackTrace();
                }
            }
        }
    }

    //--------------------------------------------------------------------------------------------------------------------------
    // TODO: c/p
    public class GetValidFriends extends AsyncTask<Void, Void, String>
    {
        //----------------------------------------------------------------------------------------------------------------------
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        //----------------------------------------------------------------------------------------------------------------------
        @Override
        protected String doInBackground(Void... params)
        {
            List<NameValuePair> param = new ArrayList<NameValuePair>();

            param.add(new BasicNameValuePair(GlobalConstant.POST_TYPE, "post"));
            param.add(new BasicNameValuePair(GlobalConstant.U_TYPE, "get_valid_fb_users"));
            param.add(new BasicNameValuePair(GlobalConstant.GROUP_USERS_ID, fbIdCheckValidation_));


            return (new WebServiceHandler()).makeServiceCall(com.app.messenger.GlobalConstant.URL, WebServiceHandler.GET,
                    param);
        }

        //----------------------------------------------------------------------------------------------------------------------
        @Override
        protected void onPostExecute(String result)
        {
            try
            {
                if (!friendInfoList_.isEmpty())
                    friendInfoList_.clear();

                if (!result.equals(GlobalConstant.ERROR_CODE_STRING) && !result.equals("Slow"))
                {
                    JSONObject jsonResponse = new JSONObject(result);

                    if (jsonResponse.getString(GlobalConstant.MESSAGE).equalsIgnoreCase(GlobalConstant.SUCCESS))
                    {
                        JSONArray jsonArr = jsonResponse.getJSONArray(GlobalConstant.GROUP_USERS_ID);

                        for (int g = 0; g < jsonArr.length(); ++g)
                        {
                            FriendInfo friend = new FriendInfo();
                            JSONObject innerJson = jsonArr.getJSONObject(g);

                            friend.setId(innerJson.getString(GlobalConstant.FACE_BOOK_ID));
                            friend.setName(innerJson.getString("user_name"));
                            friend.setMobileNumber(innerJson.getString("user_telephone"));

                            friendInfoList_.add(friend);
                        }

                        global_.setFriend_info_list(friendInfoList_);
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            super.onPostExecute(result);
        }
    }

    //--------------------------------------------------------------------------------------------------------------------------
    // TODO: c/p
    public void setFriendInfoJson()
    {
        try
        {
            if (!friendInfoList_.isEmpty())
                friendInfoList_.clear();

            ArrayList<HashMap<String, String>> friend_list_array = new ArrayList<HashMap<String, String>>();

            SharedPreferences sharedPref = getSharedPreferences("FacebookFrnd", MODE_PRIVATE);
            String friendList = sharedPref.getString("FriendList", "");
            JSONArray jsonArr = new JSONArray();
            JSONArray jArr = new JSONArray(friendList);

            for (int i = 0; i < jArr.length(); ++i)
            {
                JSONObject obj = jArr.getJSONObject(i);
                JSONObject picture = obj.getJSONObject("picture");
                JSONObject jsonObjectURL = picture.getJSONObject("data");
                String id = obj.getString("id");

                friendInfo_ = new FriendInfo();

                String IMAGE_URL = jsonObjectURL.getString("url");

                Log.e("Friend ID", id);

                friendInfo_.setId(id);

                String name = obj.getString("name");

                friendInfo_.setImage(IMAGE_URL);

                HashMap<String, String> map = new HashMap<String, String>();

                map.put("id", id);

                friend_list_array.add(map);

                JSONObject pnObj = new JSONObject();

                pnObj.put("userid", id);


                jsonArr.put(pnObj);

                friendInfo_.setName(name);

                if (fbIdCheckValidation_.equals(""))
                    fbIdCheckValidation_ = id + "";

                else
                    fbIdCheckValidation_ = fbIdCheckValidation_ + "," + id;
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        new GetValidFriends().execute();
    }

    //--------------------------------------------------------------------------------------------------------------------------
    @Override
    protected void onResume()
    {
        super.onResume();
    }

    //--------------------------------------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.splash);

//        PackageInfo info;
//        try {
//            info = getPackageManager().getPackageInfo("com.app.messenger", PackageManager.GET_SIGNATURES);
//            for (Signature signature : info.signatures) {
//                MessageDigest md;
//                md = MessageDigest.getInstance("SHA");
//                md.update(signature.toByteArray());
//                String something = new String(Base64.encode(md.digest(), 0));
//                //String something = new String(Base64.encodeBytes(md.digest()));
//                Log.e("hash key", something);
//            }
//        } catch (PackageManager.NameNotFoundException e1) {
//            Log.e("name not found", e1.toString());
//        } catch (NoSuchAlgorithmException e) {
//            Log.e("no such an algorithm", e.toString());
//        } catch (Exception e) {
//            Log.e("exception", e.toString());
//        }


        try
        {
            AppnextTrack.track(this);
        }
        catch (Exception e)
        {
        }

        splashLogo_ = (ImageView) findViewById(R.id.splash_logo);
        splashIcon_ = (ImageView) findViewById(R.id.Splash_icon);
        splashIcon_.setVisibility(View.INVISIBLE);
        PlayANim(splashLogo_, true);

        try
        {
            sharedPref_ = getSharedPreferences("login", MODE_PRIVATE);
            global_ = new Global();

            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

            GlobalUtills.country_iso_code = tm.getSimCountryIso();

            if (GlobalUtills.country_iso_code.equals(""))
                GlobalUtills.country_iso_code = "IL";

            if (NetworkCheck.isNetworkConnection(SplashActivity.this))
            {
                boolean needToShowNativeAds = !sharedPref_.contains("UserID") || !sharedPref_.contains("FbID");

                if (needToShowNativeAds)
                    Global.setNativeAdsProvider(Global.chooseAdsProvider());

                InAppAds.init(InAppAds.Provider.APPNEXT, SplashActivity.this,
                        needToShowNativeAds && Global.nativeAdsProvider() == InAppAds.Provider.APPNEXT);
                InAppAds.init(InAppAds.Provider.MOBILECORE, SplashActivity.this,
                        needToShowNativeAds && Global.nativeAdsProvider() == InAppAds.Provider.MOBILECORE);

                new GetCountryCodes().execute();
            }

            else
                GlobalUtills.showToast(GlobalConstant.ERROR_NO_NETWORK_CONNECTION, SplashActivity.this);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (sharedPref_.contains("icon"))
        {
            Editor ed = sharedPref_.edit();

            ed.remove("icon");
            ed.apply();
            AppIconManager.setBadgeValue(this,0);
            NotificationManager notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notifManager.cancel(sharedPref_.getInt("icon", 0));
        }
    }
    //--------------------------------------------------------------------------------------------------------------------------
    private void  PlayANim(final View target, final boolean animateWhich)
    {
        final AnimatorSet mAnimatorSet = new AnimatorSet();
        if (animateWhich)
        {
            mAnimatorSet.play(ObjectAnimator.ofFloat(target, "rotationY", 0f, 45f, 90f, 135f, 180f, 225f, 270f, 315f, 360f));
            mAnimatorSet.setDuration(1700);
        }
        else
        {
            mAnimatorSet.playTogether(ObjectAnimator.ofFloat(target, "alpha", 0, 1),
                    ObjectAnimator.ofFloat(target, "translationY", target.getHeight() / 4, 0));
            mAnimatorSet.setDuration(1000);
        }
        // TODO:
        mAnimatorSet.start();
        mAnimatorSet.addListener(new AnimatorListener()
        {
            @Override
            public void onAnimationEnd(Animator animation)
            {

                if (animateWhich)
                {
                    splashIcon_.setVisibility(View.VISIBLE);
                    PlayANim(splashIcon_, false);
                }
            }
            @Override
            public void onAnimationStart(Animator animation)
            {
            }
            @Override
            public void onAnimationCancel(Animator animation)
            {
            }
            @Override
            public void onAnimationRepeat(Animator animation)
            {
            }
        });
    }
    //--------------------------------------------------------------------------------------------------------------------------
    private static final long NATIVE_ADS_WAITING_TIMEOUT = 1500;

    //--------------------------------------------------------------------------------------------------------------------------
    private Global global_;
    private ArrayList<FriendInfo> friendInfoList_ = new ArrayList<FriendInfo>();
    private FriendInfo friendInfo_;
    private SharedPreferences sharedPref_;
    private String fbIdCheckValidation_ = "";
    private ImageView splashLogo_;
    private ImageView splashIcon_;
}
