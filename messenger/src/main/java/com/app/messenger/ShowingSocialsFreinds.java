//==============================================================================================================================
package com.app.messenger;


//==============================================================================================================================
import android.app.Activity;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

//------------------------------------------------------------------------------------------------------------------------------
import com.app.adapter.AddSocialFriendAdapter;
import com.app.model.FriendInfo;
import com.app.util.GlobalUtills;
import com.app.util.NetworkCheck;
import com.app.util.TransparentProgressDialog;
import com.app.webserviceshandler.WebServiceHandler;
import com.facebook.Request;
import com.facebook.Request.GraphUserListCallback;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;

//------------------------------------------------------------------------------------------------------------------------------
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

//------------------------------------------------------------------------------------------------------------------------------
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


//==============================================================================================================================
public class ShowingSocialsFreinds extends Activity
{

    //--------------------------------------------------------------------------------------------------------------------------
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

            param.add(new BasicNameValuePair(GlobalConstant.POST_TYPE,      "post"));
            param.add(new BasicNameValuePair(GlobalConstant.U_TYPE,         "get_valid_fb_users"));
            param.add(new BasicNameValuePair(GlobalConstant.GROUP_USERS_ID, fbIdCheckValidation_));



            return (new WebServiceHandler()).makeServiceCall(GlobalConstant.URL, WebServiceHandler.GET, param);
        }

        //----------------------------------------------------------------------------------------------------------------------
        @Override
        protected void onPostExecute(String result)
        {
            try
            {
                if (!friendInfoList_.isEmpty())
                    friendInfoList_.clear();

                JSONObject jsonResponse = new JSONObject(result);

                if (jsonResponse.getString(GlobalConstant.MESSAGE).equalsIgnoreCase(GlobalConstant.SUCCESS))
                {
                    JSONArray jsonArr = jsonResponse.getJSONArray(GlobalConstant.GROUP_USERS_ID);

                    for (int g = 0; g < jsonArr.length(); ++g)
                    {
                        FriendInfo friend    = new FriendInfo();
                        JSONObject innerJson = jsonArr.getJSONObject(g);

                        friend.setId       (innerJson.getString(GlobalConstant.FACE_BOOK_ID));
                        friend.setName     (innerJson.getString("user_name"));
                        friend.setMobileNumber(innerJson.getString("user_telephone"));

                        friendInfoList_.add(friend);
                    }

                    global_.setFriend_info_list(friendInfoList_);

                    clearListsSetData();

                    if (progressDialog_.isShowing())
                    {
                        try
                        {
                            progressDialog_.dismiss();
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                        finally
                        {
                            progressDialog_ = null;
                        }
                    }
                }
            }
            catch (Exception e)
            {
                if (progressDialog_.isShowing())
                    progressDialog_.dismiss();

                e.printStackTrace();
            }

            super.onPostExecute(result);
        }
    }

    //--------------------------------------------------------------------------------------------------------------------------
    class GetValidContacts extends AsyncTask<String, Void, String>
    {

        //----------------------------------------------------------------------------------------------------------------------
        @Override
        protected void onPreExecute()
        {
            if (allFriendPhonesList_.size() > 0)
                allFriendPhonesList_.clear();

            pd_ = new TransparentProgressDialog(ShowingSocialsFreinds.this, R.drawable.loading_spinner_icon);

            pd_.show();

            gettingPhoneContacts();

            super.onPreExecute();
        }

        //----------------------------------------------------------------------------------------------------------------------
        @Override
        protected String doInBackground(String... params)
        {
            List<NameValuePair> param = new ArrayList<NameValuePair>();

            param.add(new BasicNameValuePair("phones",  phoneNumber_ + ""));
            param.add(new BasicNameValuePair(GlobalConstant.JOIN_USER_ID, global_.getUser_id() + ""));

            String jsonString = (new WebServiceHandler()).makeServiceCall(GlobalConstant.URL +
                                                                          "?post_type=post&mtype=phone_find",
                                                                          WebServiceHandler.POST, param);

            if (jsonString.equals(GlobalConstant.ERROR_CODE_STRING))
                return jsonString;

            else
            {
                try
                {
                    JSONObject jsonObject = new JSONObject(jsonString);

                    message_ = jsonObject.getString(GlobalConstant.MESSAGE);

                    JSONArray groupListInfo = jsonObject.getJSONArray(GlobalConstant.GROUP_USERS_ID);

                    for (int i = 0; i < groupListInfo.length(); ++i)
                    {
                        FriendInfo contactDetail = new FriendInfo();
                        JSONObject groupInfo     = groupListInfo.getJSONObject(i);

                        contactDetail.setMobileNumber(groupInfo.getString("user_telephone"));
                        contactDetail.setId          (groupInfo.getString(GlobalConstant.FACE_BOOK_ID));

                        String name = groupInfo.getString("user_name");

                        for (int fb_remove = 0; fb_remove < allFriendList_.size(); fb_remove++)
                        {
                            if (groupInfo.getString("user_telephone").equals(allFriendList_.get(fb_remove).getMobile_no()))
                            {
                                allFriendList_.remove(fb_remove);

                                break;
                            }
                        }

                        for (int g = 0; g < allFriendPhonesList_.size(); ++g)
                        {
                            String phoneNumber = allFriendPhonesList_.get(g).getMobile_no().replaceAll(" ", "");
                            if (phoneNumber.startsWith("0"))
                                phoneNumber = phoneNumber.substring(1);

                            if (groupInfo.getString("user_telephone").contains(phoneNumber))
                            {
                                name = allFriendPhonesList_.get(g).getName() + "(" + name + ")";

                                allFriendPhonesList_.remove(g);

                                break;
                            }
                        }

                        contactDetail.setName(name);

                        phoneList_.add(contactDetail);
                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                return message_;
            }
        }

        //----------------------------------------------------------------------------------------------------------------------
        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);

            try
            {
                pd_.dismiss();
                Collections.sort(allFriendPhonesList_, new Comparator<FriendInfo>()
                {
                    @Override
                    public int compare(FriendInfo text1, FriendInfo text2)
                    {
                        return text1.getName().compareToIgnoreCase(text2.getName());
                    }
                });

                Collections.sort(phoneList_, new Comparator<FriendInfo>()
                {
                    @Override
                    public int compare(FriendInfo text1, FriendInfo text2)
                    {
                        return text1.getName().compareToIgnoreCase(text2.getName());
                    }
                });

                phoneList_.addAll(allFriendPhonesList_);
                allFriendList_.addAll(phoneList_);

                friendAdapter_ = new AddSocialFriendAdapter(ShowingSocialsFreinds.this, allFriendList_);

                facebookFriendListView_.setAdapter(friendAdapter_);


            }
            catch (Exception e)
            {
                if (pd_.isShowing())
                    pd_.dismiss();

                e.printStackTrace();
            }
        }

        //----------------------------------------------------------------------------------------------------------------------
        private TransparentProgressDialog pd_;
        private String                    message_ = "";
    }

    //--------------------------------------------------------------------------------------------------------------------------
    public static ArrayList<FriendInfo> allFriendList()
    {
        return allFriendList_;
    }

    //--------------------------------------------------------------------------------------------------------------------------
    @Override
    public void onBackPressed()
    {
        if (global_.getHashMap().size() != 0 || global_.getHashMap_ph().size() != 0)
            GlobalUtills.showToast("Friends successfully added", ShowingSocialsFreinds.this);

        else
            GlobalUtills.showToast("No friend added", ShowingSocialsFreinds.this);

        finish();

        super.onBackPressed();
    }

    //--------------------------------------------------------------------------------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        setContentView(R.layout.showingfriends);

        global_                 = new Global();
        facebookFriendListView_ = (ListView) findViewById(R.id.mainListView);
        etSearchContact_        = (EditText) findViewById(R.id.search_edit_text_phone_contact_activity);

        etSearchContact_.setCursorVisible  (false);
        etSearchContact_.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                etSearchContact_.setCursorVisible(true);
            }
        });

        etSearchContact_.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_SEARCH)
                {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

                    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                                                               InputMethodManager.HIDE_NOT_ALWAYS);

                    if (etSearchContact_.getText().toString().trim().equals(""))
                        etSearchContact_.setCursorVisible(false);
                }

                return false;
            }
        });

        etSearchContact_.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                ArrayList<FriendInfo> tempFriendList = new ArrayList<FriendInfo>();

                if (allFriendList_.size() > 0)
                {
                    for (int i = 0; i < allFriendList_.size(); ++i)
                        if (allFriendList_.get(i).getName().toLowerCase().contains(s.toString().toLowerCase()) ||
                            allFriendList_.get(i).getMobile_no().contains(s.toString()))
                            tempFriendList.add(allFriendList_.get(i));

                    friendAdapter_ = new AddSocialFriendAdapter(ShowingSocialsFreinds.this, tempFriendList);

                    facebookFriendListView_.setAdapter(friendAdapter_);

                    if (tempFriendList.size() < 1)  // TODO:
                        GlobalUtills.showToast("No contacts Found..!", ShowingSocialsFreinds.this);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void afterTextChanged(Editable s)
            {
            }
        });

        getFriends();
    }

    //--------------------------------------------------------------------------------------------------------------------------
    public void clearListsSetData()
    {
        if (allFriendList_.size() > 0)
            allFriendList_.clear();

        if (allFriendPhonesList_.size() > 0)
            allFriendPhonesList_.clear();

        for (int fb = 0; fb < global_.getFriend_info_list().size(); ++fb)
            allFriendList_.add(global_.getFriend_info_list().get(fb));

        if (NetworkCheck.getConnectivityStatusString(ShowingSocialsFreinds.this).equals("true"))
            new GetValidContacts().execute();

        else
            GlobalUtills.showToast(GlobalConstant.ERROR_NO_NETWORK_CONNECTION, ShowingSocialsFreinds.this);
    }

    //--------------------------------------------------------------------------------------------------------------------------
    public void setFriendInfoJson()  // TODO: c/p
    {
        try
        {
            if (!friendInfoList_.isEmpty())
                friendInfoList_.clear();

            ArrayList<HashMap<String, String>> friendListArray = new ArrayList<HashMap<String, String>>();
            SharedPreferences                  sharedPref      = getSharedPreferences("FacebookFrnd", MODE_PRIVATE);
            String                             friendList      = sharedPref.getString("FriendList", "");
            JSONArray                          jsonArr         = new JSONArray();

            fbIdCheckValidation_ = "";

            JSONArray jArr = new JSONArray(friendList);

            for (int i = 0; i < jArr.length(); ++i)
            {
                JSONObject obj           = jArr.getJSONObject(i);
                JSONObject picture       = obj.getJSONObject("picture");
                JSONObject jsonObjectURL = picture.getJSONObject("data");
                String     id            = obj.getString("id");

                friendInfo_ = new FriendInfo();

                String imageURL = jsonObjectURL.getString("url");

                Log.e("Friend ID", id);

                friendInfo_.setId(id);

                String name = obj.getString("name");

                friendInfo_.setImage(imageURL);

                HashMap<String, String> map = new HashMap<String, String>();

                map.put("id", id);

                friendListArray.add(map);

                JSONObject pnObj = new JSONObject();

                pnObj.put("userid", id);

                Log.e("list of friends ", "" + friendListArray.size());
                Log.e("Friends Detail  ", "" + friendListArray.toString());

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
            if (progressDialog_.isShowing())
                progressDialog_.dismiss();

            e.printStackTrace();
        }

        new GetValidFriends().execute();
    }

    //--------------------------------------------------------------------------------------------------------------------------
    @Override
    protected void onResume()
    {
        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

        GlobalUtills.country_iso_code = tm.getSimCountryIso();

        if (GlobalUtills.country_iso_code.equals(""))
            GlobalUtills.country_iso_code = "IL";

        super.onResume();
    }

    //--------------------------------------------------------------------------------------------------------------------------
    private void gettingPhoneContacts()
    {
        ContentResolver cr = getContentResolver();

        Cursor c = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                       new String[ ]
                       {
                           ContactsContract.Contacts._ID,
                           ContactsContract.Contacts.DISPLAY_NAME,
                           ContactsContract.CommonDataKinds.Phone.NUMBER,
                           ContactsContract.RawContacts.ACCOUNT_TYPE
                       },
                       ContactsContract.RawContacts.ACCOUNT_TYPE + " <> 'google' ", null, null);

        if (c.getCount() <= 0)
            Toast.makeText(ShowingSocialsFreinds.this, "No Phone Contact Found..!", Toast.LENGTH_LONG).show();

        else
        {
            StringBuilder sb = new StringBuilder();

            phoneNumber_ = "";

            while (c.moveToNext())
            {
                FriendInfo contactDetail    = new FriendInfo();
                String     phoneNumber      = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                String     name             = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String     phoneNumberValid = phoneNumber.replaceAll(" ", "");

                System.out.println("ph_noValid----->" + phoneNumberValid);

                if ( phoneNumberValid.length() > 6  && !phoneNumberValid.contains("*") && !phoneNumberValid.contains("#") &&
                    !phoneNumberValid.contains("&") && !phoneNumberValid.contains("%"))
                {
                    try
                    {
                        if (phoneNumberValid.contains("("))
                            phoneNumberValid = phoneNumberValid.replace("(", "");

                        if (phoneNumberValid.contains(")"))
                            phoneNumberValid = phoneNumberValid.replace(")", "");

                        if (phoneNumberValid.contains("-"))
                            phoneNumberValid = phoneNumberValid.replace("-", "");

                        Log.e("Phone Number valid--->", phoneNumberValid);

                        contactDetail.setMobileNumber("" + phoneNumberValid);
                        contactDetail.setName        (name);
                        contactDetail.setId          ("");

                        allFriendPhonesList_.add(contactDetail);

                        Log.e("NAme", name);
                    }
                    catch (Exception e)
                    {
                        if(progressDialog_.isShowing())
                            progressDialog_.dismiss();



                        e.printStackTrace();
                    }

                    if (sb.length() > 0)
                        sb.append(',');

                    sb.append(phoneNumberValid);

                    phoneNumber_ = sb.toString();
                }
            }
        }
    }

    //--------------------------------------------------------------------------------------------------------------------------
    private void getFriends()
    {
        progressDialog_ = new TransparentProgressDialog(ShowingSocialsFreinds.this, R.drawable.loading_spinner_icon);

        progressDialog_.show();

        Session activeSession = Session.getActiveSession();

        if (activeSession == null)
            activeSession = Session.openActiveSessionFromCache(ShowingSocialsFreinds.this);

      if (activeSession.getState().isOpened())
        {
            Request friendRequest = Request.newMyFriendsRequest(activeSession, new GraphUserListCallback()
            {
                public void onCompleted(List<GraphUser> users, Response response)
                {
                    try
                    {

                        String            friendList = response.getGraphObject().getProperty("data").toString();
                        SharedPreferences sharedPref = getSharedPreferences("FacebookFrnd", MODE_PRIVATE);
                        Editor            editorPref = sharedPref.edit();

                        editorPref.clear    ();
                        editorPref.commit   ();
                        editorPref.putString("FriendList", friendList + "");
                        editorPref.commit   ();

                        setFriendInfoJson();
                    }
                    catch (Exception e)
                    {
                        if (progressDialog_.isShowing())
                            progressDialog_.dismiss();
                        setFriendInfoJson();
                    }
                }
            });

            Bundle params = new Bundle();

            params.putString("fields", "id,name,email,picture,gender");

            friendRequest.setParameters(params);
            friendRequest.executeAsync ();
        }
        else
      {
          if (progressDialog_.isShowing())
              progressDialog_.dismiss();
      }
    }

    //--------------------------------------------------------------------------------------------------------------------------
    private static ArrayList<FriendInfo> allFriendList_ = new ArrayList<FriendInfo>();

    //--------------------------------------------------------------------------------------------------------------------------
    private ListView                  facebookFriendListView_;
    private EditText                  etSearchContact_;
    private ArrayList<FriendInfo>     allFriendPhonesList_    = new ArrayList<FriendInfo>();
    private AddSocialFriendAdapter    friendAdapter_;
    private Global                    global_;
    private ArrayList<FriendInfo>     phoneList_              = new ArrayList<FriendInfo>();
    private TransparentProgressDialog progressDialog_;
    private ArrayList<FriendInfo>     friendInfoList_         = new ArrayList<FriendInfo>();
    private String                    fbIdCheckValidation_    = "";
    private FriendInfo                friendInfo_;
    private String                    phoneNumber_            = "";
}
