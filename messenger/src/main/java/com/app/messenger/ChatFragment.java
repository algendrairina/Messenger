//==============================================================================================================================
package com.app.messenger;


//==============================================================================================================================
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

//------------------------------------------------------------------------------------------------------------------------------
import com.app.adapter.ChatContactAdapter;
import com.app.adapter.ShowSocialFriends;
import com.app.model.FriendInfo;
import com.app.util.GlobalUtills;
import com.app.util.NetworkCheck;
import com.app.util.TransparentProgressDialog;
import com.app.webserviceshandler.Remove_chat;
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
import org.apache.http.NameValuePair;  // TODO:
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


//==============================================================================================================================
public class ChatFragment extends Fragment
{

    //--------------------------------------------------------------------------------------------------------------------------
    class ActiveFriendsGetter extends AsyncTask<String, Void, String>
    {

        //----------------------------------------------------------------------------------------------------------------------
        @Override
        protected void onPreExecute()
        {
            listViewRecentChats_.setVisibility(View.GONE);
            GlobalUtills.list_chat.setVisibility(View.GONE);
            listViewGroupMemberChat_.setVisibility(View.VISIBLE);

            progressDialog_ = new TransparentProgressDialog(getActivity(), R.drawable.loading_spinner_icon);

            if (usersList_.isEmpty())
                progressDialog_.show();

            super.onPreExecute();
        }

        //----------------------------------------------------------------------------------------------------------------------
        @Override
        protected String doInBackground(String... params)
        {
            List<NameValuePair> param = new ArrayList<NameValuePair>();  // TODO:

            param.add(new BasicNameValuePair(GlobalConstant.POST_TYPE,    "post"));
            param.add(new BasicNameValuePair(GlobalConstant.U_TYPE,       "get_active_members"));
            param.add(new BasicNameValuePair(GlobalConstant.JOIN_USER_ID, global_.getUser_id() + ""));

            String jsonString = (new WebServiceHandler()).makeServiceCall(GlobalConstant.URL, WebServiceHandler.GET, param);

            if (jsonString.equals(GlobalConstant.ERROR_CODE_STRING))
                return jsonString;

            else
            {
                try
                {
                    JSONObject jsonObject = new JSONObject(jsonString);

                    message_ = jsonObject.getString(GlobalConstant.MESSAGE);

                    if (message_.equalsIgnoreCase(GlobalConstant.SUCCESS))
                    {
                        usersList_.clear();

                        JSONArray groupsInfo = jsonObject.getJSONArray("user_info");

                        for (int index = 0; index < groupsInfo.length(); ++index)
                        {
                            FriendInfo friendInfo = new FriendInfo();
                            JSONObject groupInfo  = groupsInfo.getJSONObject(index);

                            if (!facebookID_.equals(groupInfo.getString(GlobalConstant.FACE_BOOK_ID)))
                            {
                                friendInfo.setId(groupInfo.getString(GlobalConstant.FACE_BOOK_ID) + "");
                                friendInfo.setImage(groupInfo.getString("userImage") + "");
                                friendInfo.setName(groupInfo.getString("userName") + "");
                                friendInfo.setUnread_count(groupInfo.getString("unread_count"));
                                friendInfo.setMobileNumber(groupInfo.getString("user_telephone") + "");

                                friendInfo.setLastseen(groupInfo.optString("lastseen"));

                                usersList_.add(friendInfo);
                            }
                        }
                    }
                }
                catch (JSONException e)
                {
                    message_ = "Error";

                    e.printStackTrace();
                }
                catch (Exception e)
                {
                    message_ = "Error";

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
                if (!result.equalsIgnoreCase(GlobalConstant.SUCCESS))
                    GlobalUtills.showToast("No Group Members found..!", getActivity());

                else
                {
                    if( socialUsers_ == null )
                    {
                        socialUsers_ = new ShowSocialFriends(getActivity(), usersList_, true, false);

                        listViewGroupMemberChat_.setAdapter(socialUsers_);
                    }

                    else if (listViewGroupMemberChat_.getAdapter() != socialUsers_)
                        listViewGroupMemberChat_.setAdapter(socialUsers_);

                    else
                        listViewGroupMemberChat_.invalidateViews();
                }

                positionFB_.clear();
                positionPH_.clear();
                positionC_.clear();

                if (progressDialog_.isShowing())
                    progressDialog_.dismiss();
            }
            catch (Exception e)
            {
                if (progressDialog_.isShowing())
                    progressDialog_.dismiss();

                e.printStackTrace();
            }
            catch (Error e)
            {
            }
        }

        //----------------------------------------------------------------------------------------------------------------------
        private String                    message_        = "";
        private TransparentProgressDialog progressDialog_;
    }

    //--------------------------------------------------------------------------------------------------------------------------
    class ValidContactsGetter extends AsyncTask<String, Void, String>
    {

        //----------------------------------------------------------------------------------------------------------------------
        @Override
        protected void onPreExecute()
        {
            getPhoneContacts();

            super.onPreExecute();
        }

        //----------------------------------------------------------------------------------------------------------------------
        @Override
        protected String doInBackground(String... params)
        {
            String jsonString = "";

            try
            {
                List<NameValuePair> param = new ArrayList<NameValuePair>();

                param.add(new BasicNameValuePair("phones",                    phoneNumber_ + ""));
                param.add(new BasicNameValuePair(GlobalConstant.JOIN_USER_ID, global_.getUser_id() + ""));

                jsonString = (new WebServiceHandler()).makeServiceCall(GlobalConstant.URL + "?post_type=post&mtype=phone_find",
                                                                       WebServiceHandler.POST, param);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            if (jsonString.equals(GlobalConstant.ERROR_CODE_STRING))
                return jsonString;

            else
            {
                try
                {
                    JSONObject jsonObject = new JSONObject(jsonString);

                    message_ = jsonObject.getString(GlobalConstant.MESSAGE);

                    JSONArray groupsInfo = jsonObject.getJSONArray(GlobalConstant.GROUP_USERS_ID);

                    for (int index = 0; index < groupsInfo.length(); ++index)
                    {
                        FriendInfo contactDetail = new FriendInfo();
                        JSONObject groupInfo     = groupsInfo.getJSONObject(index);

                        contactDetail.setMobileNumber(groupInfo.getString("user_telephone"));
                        contactDetail.setId          (groupInfo.getString(GlobalConstant.FACE_BOOK_ID));
                        contactDetail.setUnread_count(groupInfo.getString("unread_count"));

                        String name = groupInfo.getString("user_name");

                        for (int removeIndex = 0; removeIndex < allFriendsList_.size(); ++removeIndex)
                        {
                            if (groupInfo.getString("user_telephone").equals(allFriendsList_.get(removeIndex).getMobile_no()))
                            {
                                allFriendsList_.remove(removeIndex);

                                break;
                            }
                        }

                        for (int phoneIndex = 0; phoneIndex < phoneListR_.size(); ++phoneIndex)
                        {
                            String phoneNumber = phoneListR_.get(phoneIndex).getMobile_no();

                            if (phoneNumber.startsWith("0"))
                                phoneNumber = phoneNumber.substring(1);

                            if (groupInfo.getString("user_telephone").contains(phoneNumber))
                            {
                                name = phoneListR_.get(phoneIndex).getName() + "(" + name + ")";

                                phoneListR_.remove(phoneIndex);

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
                catch (Error e)
                {
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
                if (result.equals(GlobalConstant.ERROR_CODE_STRING))
                {
                    if (progressDialog_.isShowing())
                        progressDialog_.dismiss();

                    GlobalUtills.showToast("Oops an error has occur..!", getActivity());
                }
                else
                {
                    Collections.sort(phoneListR_, new Comparator<FriendInfo>()
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

                    completeFriendsList_.clear();

                    phoneList_.addAll(phoneListR_);
                    completeFriendsList_.addAll(allFriendsList_);
                    completeFriendsList_.addAll(phoneList_);

                    phoneContactAdapter_ = new ChatContactAdapter(getActivity(), completeFriendsList_);

                    GlobalUtills.list_chat.setAdapter(phoneContactAdapter_);

                    positionFB_.clear();
                    positionPH_.clear();
                    positionC_.clear();

                    if (progressDialog_.isShowing())
                        progressDialog_.dismiss();
                }
            }
            catch (Exception e)
            {
                if (progressDialog_.isShowing())
                    progressDialog_.dismiss();

                e.printStackTrace();
            }
            catch (Error e)
            {
            }
        }

        //----------------------------------------------------------------------------------------------------------------------
        private String message_ = "";
    }

    //--------------------------------------------------------------------------------------------------------------------------
    class MessageRequestSender extends AsyncTask<String , Void , String>
    {

        //----------------------------------------------------------------------------------------------------------------------
        @Override
        protected void onPreExecute()
        {
            progressDialog_ = new TransparentProgressDialog(getActivity(), R.drawable.loading_spinner_icon);

            progressDialog_.show();

            super.onPreExecute();
        }

        //----------------------------------------------------------------------------------------------------------------------
        @Override
        protected String doInBackground(String... params)
        {
            String phoneNumber = params[0];

            List<NameValuePair> param = new ArrayList<NameValuePair>();

            param.add(new BasicNameValuePair(GlobalConstant.POST_TYPE, "post"));
            param.add(new BasicNameValuePair(GlobalConstant.U_TYPE,                  "send_msg_request"));
            param.add(new BasicNameValuePair(GlobalConstant.USER_ID,                 global_.getUser_id() + ""));
            param.add(new BasicNameValuePair("phone", phoneNumber + ""));

            String jsonString = (new WebServiceHandler()).makeServiceCall(GlobalConstant.URL, WebServiceHandler.GET, param);

            System.out.println(jsonString.toString() + "response send request");

            return message_;
        }

        //----------------------------------------------------------------------------------------------------------------------
        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);

            progressDialog_.dismiss();

            GlobalUtills.showToast("Request has been successfully sent..!", getActivity());
        }

        //----------------------------------------------------------------------------------------------------------------------
        private String                    message_        = "";
        private TransparentProgressDialog progressDialog_;
    }

    //--------------------------------------------------------------------------------------------------------------------------
    class RecentChatsGetter extends AsyncTask<Void , Void , String>
    {

        //----------------------------------------------------------------------------------------------------------------------
        public RecentChatsGetter(String user)
        {
            this.userID_ = user;
        }

        //----------------------------------------------------------------------------------------------------------------------
        @Override
        protected void onPreExecute()
        {
            progressDialog_ = new TransparentProgressDialog(getActivity(), R.drawable.loading_spinner_icon);

            if (recentChatFriendList_.isEmpty())
                progressDialog_.show();

            groupMember_ = "C";

            super.onPreExecute();
        }

        //----------------------------------------------------------------------------------------------------------------------
        @Override
        protected String doInBackground(Void... params)
        {
            List<NameValuePair> param = new ArrayList<NameValuePair>();

            param.add(new BasicNameValuePair(GlobalConstant.POST_TYPE, "post"));
            param.add(new BasicNameValuePair(GlobalConstant.U_TYPE,     "recent_chat"));
            param.add(new BasicNameValuePair("userid",                  "" + userID_));  // TODO:

            response_ = (new WebServiceHandler()).makeServiceCall(GlobalConstant.URL, WebServiceHandler.GET, param);
            Log.d("Receiver","Receiver" + "  Response  "  + response_);
            try
            {
                JSONObject jsonObject = new JSONObject(response_);

                message_ = jsonObject.getString("message");
                Log.d("Receiver","Receiver" + " Message  "  + response_);
                if (message_.equalsIgnoreCase(GlobalConstant.SUCCESS))
                {
                    JSONArray groupsInfo = jsonObject.getJSONArray("response");  // TODO:

                    recentChatFriendList_.clear();

                    for (int index = 0; index < groupsInfo.length(); ++index)
                    {
                        FriendInfo friendInfo = new FriendInfo();

                        JSONObject recentChats = groupsInfo.getJSONObject(index);

                        friendInfo.setId(recentChats.getString(GlobalConstant.FACE_BOOK_ID) + "");
                        friendInfo.setImage(recentChats.getString("userImage") + "");
                        friendInfo.setName(recentChats.getString("userName") + "");
                        friendInfo.setUnread_count(recentChats.getString("unread_count"));
                        friendInfo.setMobileNumber(recentChats.getString("user_telephone") + "");

                        friendInfo.setLastseen(recentChats.optString("lastseen"));



                        recentChatFriendList_.add(friendInfo);
                    }
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
            catch (Error e)
            {
            }
            Log.d("Receiver","Receiver" + " Retrun message  "  + response_);
            return message_;
        }

        //----------------------------------------------------------------------------------------------------------------------
        @Override
        protected void onPostExecute(String result)
        {
            try
            {
                if (progressDialog_.isShowing())
                    progressDialog_.dismiss();

                if (!result.contains(GlobalConstant.SUCCESS))
                    GlobalUtills.showToast("No recent chat found ..!", getActivity());

                else
                {
                    if (recentChatAdapter_ == null)
                    {
                        recentChatAdapter_ = new ShowSocialFriends(getActivity(), recentChatFriendList_, true, false);

                        listViewRecentChats_.setAdapter(recentChatAdapter_);
                    }

                    else if (listViewRecentChats_.getAdapter() != recentChatAdapter_)
                        listViewRecentChats_.setAdapter(recentChatAdapter_);

                    else
                        listViewRecentChats_.invalidateViews();
                }
            }
            catch (Exception e)
            {
                if (progressDialog_.isShowing())
                    progressDialog_.dismiss();
            }
            catch (Error e)
            {
            }

            super.onPostExecute(result);
        }

        //----------------------------------------------------------------------------------------------------------------------
        private String                    response_       = "";
        private String                    message_        = "";
        private String                    userID_         = "";
        private TransparentProgressDialog progressDialog_;
    }

    //--------------------------------------------------------------------------------------------------------------------------
    public class ValidFriendsGetter extends AsyncTask<Void, Void, String>
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
            try
            {
                List<NameValuePair> param = new ArrayList<NameValuePair>();

                param.add(new BasicNameValuePair(GlobalConstant.POST_TYPE,      "post"));
                param.add(new BasicNameValuePair(GlobalConstant.U_TYPE,         "get_valid_fb_users"));
                param.add(new BasicNameValuePair(GlobalConstant.GROUP_USERS_ID, facebookIdCheckValidation_));
                param.add(new BasicNameValuePair(GlobalConstant.JOIN_USER_ID,   global_.getUser_id()));



                return (new WebServiceHandler()).makeServiceCall(GlobalConstant.URL, WebServiceHandler.GET, param);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            return "";
        }

        //----------------------------------------------------------------------------------------------------------------------
        @Override
        protected void onPostExecute(String result)
        {
            try
            {
                friendInfoList_.clear();

                JSONObject jsonResponse = new JSONObject(result);

                if (jsonResponse.getString(GlobalConstant.MESSAGE).equalsIgnoreCase(GlobalConstant.SUCCESS))
                {
                    JSONArray jsonArray = jsonResponse.getJSONArray(GlobalConstant.GROUP_USERS_ID);

                    for (int index = 0; index < jsonArray.length(); ++index)
                    {
                        FriendInfo friend     = new FriendInfo();
                        JSONObject jsonObject = jsonArray.getJSONObject(index);

                        friend.setId          (jsonObject.getString(GlobalConstant.FACE_BOOK_ID));
                        friend.setName        (jsonObject.getString("user_name"));
                        friend.setMobileNumber(jsonObject.getString("user_telephone"));
                        friend.setUnread_count(jsonObject.getString("unread_count"));

                        friendInfoList_.add(friend);
                    }

                    global_.setFriend_info_list(friendInfoList_);

                    allFriendsList_.clear();

                    allFriendsList_.addAll(friendInfoList_);

                    Collections.sort(allFriendsList_, new Comparator<FriendInfo>()
                    {
                        @Override
                        public int compare(FriendInfo text1, FriendInfo text2)
                        {
                            return text1.getUnread_count().compareToIgnoreCase(text2.getUnread_count());
                        }
                    });

                    Collections.reverse(allFriendsList_);

                    new ValidContactsGetter().execute();
                }
            }
            catch (Exception e)
            {
                if (progressDialog_.isShowing())
                    progressDialog_.dismiss();

                e.printStackTrace();
            }
            catch (Error e)
            {
            }

            super.onPostExecute(result);
        }
    }

    //--------------------------------------------------------------------------------------------------------------------------
    public static void setNewMessage(boolean value)
    {
        newMessage_ = value;
    }

    //--------------------------------------------------------------------------------------------------------------------------
    public static ArrayList<FriendInfo> completeFriendsList()
    {
        return completeFriendsList_;
    }

    //--------------------------------------------------------------------------------------------------------------------------
    public static String groupMember()
    {
        return groupMember_;
    }

    //--------------------------------------------------------------------------------------------------------------------------
    public static boolean newSingleMessage()
    {
        return newSingleMessage_;
    }

    //--------------------------------------------------------------------------------------------------------------------------
    public static ChatContactAdapter phoneContactAdapter()
    {
        return phoneContactAdapter_;
    }

    //--------------------------------------------------------------------------------------------------------------------------
    public static ShowSocialFriends recentChatAdapter()
    {
        return recentChatAdapter_;
    }

    //--------------------------------------------------------------------------------------------------------------------------
    public static ArrayList<FriendInfo> recentChatFriendList()
    {
        return recentChatFriendList_;
    }

    //--------------------------------------------------------------------------------------------------------------------------
    public static ShowSocialFriends socialUsers()
    {
        return socialUsers_;
    }

    //--------------------------------------------------------------------------------------------------------------------------
    public static ArrayList<FriendInfo> usersList()
    {
        return usersList_;
    }

    //--------------------------------------------------------------------------------------------------------------------------
    @Override
    public void onResume()
    {
        globalUtills_ = new GlobalUtills();

        if (GlobalUtills.badge1 != null)
        {
            if (GlobalUtills.badge1.isShown())
            {
                GlobalUtills.badge1.toggle();

                SharedPreferences preferences = getActivity().getSharedPreferences("login", Context.MODE_PRIVATE);
                Editor            editor      = preferences.edit();

                editor.remove("notification_flag_single_chat");
                editor.commit();
            }
        }

        newSingleMessage_ = true;

        if (NetworkCheck.checkInternetConnection(getActivity()))
        {
            phoneList_.clear();
            positionFB_.clear();
            positionPH_.clear();
            positionC_.clear();

            global_ = new Global();

            SharedPreferences preferences = getActivity().getSharedPreferences("login", Context.MODE_PRIVATE);

            global_.setUser_id(preferences.getString("UserID", ""));

            if (groupMember_.equals("C"))
            {
                changeTab(groupMember_);

                newMessage_ = false;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                    new RecentChatsGetter(global_.getUser_id()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                else
                    new RecentChatsGetter(global_.getUser_id()).execute();
            }
            else if (groupMember_.equals("PH"))
            {
                changeTab(groupMember_);

                if (completeFriendsList_.size() <= 0 || newMessage_)
                {
                    newMessage_ = false;

                    getFriends();
                }
            }
            else if (groupMember_.equals("FB_M"))
            {
                changeTab(groupMember_);

                newMessage_ = false;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                    new ActiveFriendsGetter().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                else
                    new ActiveFriendsGetter().execute();
            }
        }
        else
        {
            if (groupMember_.equals("C"))
                changeTab(groupMember_);

            else if (groupMember_.equals("PH"))
                changeTab(groupMember_);

            else if (groupMember_.equals("FB_M"))
                changeTab(groupMember_);
        }

        super.onResume();
    }

    //--------------------------------------------------------------------------------------------------------------------------
    @Override
    public void onPause()
    {
        newSingleMessage_ = false;

        super.onPause();
    }

    //--------------------------------------------------------------------------------------------------------------------------
    @Override
    public void onDestroy()
    {
        newSingleMessage_ = false;

        super.onDestroy();
    }

    //--------------------------------------------------------------------------------------------------------------------------
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);

        container        = (ViewGroup) inflater.inflate(R.layout.chat_activity, container, false);  // TODO:
        actionBarCommon_ = new ActionBarCommon(getActivity(), null);

        getValues(container);

        actionBarCommon_.setActionText("Chat");
        actionBarCommon_.rightImage().setImageResource(R.drawable.invite);

        actionBarCommon_.setLayoutRightClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // TODO:
                String shareBody     = "Hey ..! i found get-groupy an osm application to connect with your friends.. https://play.google.com/store/apps/details?id=com.app.messenger&hl=en";
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);

                sharingIntent.setType ("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Get-Groupy");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);

                startActivity(Intent.createChooser(sharingIntent, "Invite Friends"));
            }
        });

        newSingleMessage_ = true;

        SharedPreferences preferences = getActivity().getSharedPreferences("fbID", Context.MODE_PRIVATE);

        facebookID_ = preferences.getString("fb", "");
        global_     = new Global();

        editTextContactSearch_.setCursorVisible(false);

        editTextContactSearch_.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_SEARCH)
                {
                    InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

                    inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                    if (editTextContactSearch_.getText().toString().trim().equals(""))
                        editTextContactSearch_.setCursorVisible(false);
                }

                return false;
            }
        });

        editTextContactSearch_.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                editTextContactSearch_.setCursorVisible(true);
            }
        });

        editTextContactSearch_.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                ArrayList<FriendInfo> temporaryFriendsList = new ArrayList<FriendInfo>();
                ArrayList<FriendInfo> temporaryPhonesList = new ArrayList<FriendInfo>();

                temporaryFriendsList.clear();

                positionFB_.clear();
                positionPH_.clear();
                positionC_.clear();

                if (groupMember_.equals("FB_M"))
                {
                    for (int index = 0; index < usersList_.size(); ++index)
                    {
                        if (usersList_.get(index).getName().toLowerCase().contains(s.toString().toLowerCase()))
                        {
                            positionFB_.add(index);

                            temporaryFriendsList.add(usersList_.get(index));
                        }
                    }

                    if (temporaryFriendsList.size() <= 0)
                        GlobalUtills.showToast(getString(R.string.no_records), getActivity().getApplicationContext());

                    else
                    {
                        socialUsers_ = new ShowSocialFriends(getActivity(), temporaryFriendsList, true, false);

                        listViewGroupMemberChat_.setAdapter(socialUsers_);
                    }
                }
                else if (groupMember_.equals("C"))
                {
                    if (recentChatFriendList_.size() <= 0)
                        listViewRecentChats_.setAdapter(null);

                    else
                    {
                        for (int index = 0; index < recentChatFriendList_.size(); ++index)
                        {
                            if (recentChatFriendList_.get(index).getName().toLowerCase().contains(s.toString().toLowerCase()) ||
                                    recentChatFriendList_.get(index).getMobile_no().contains(s.toString()))
                             {
                                positionC_.add(index);

                                temporaryFriendsList.add(recentChatFriendList_.get(index));
                            }
                        }

                        // TODO: c/p
                        if (temporaryFriendsList.size() <= 0)
                            GlobalUtills.showToast(getString(R.string.no_records), getActivity().getApplicationContext());

                        else
                        {
                            recentChatAdapter_ = new ShowSocialFriends(getActivity(), temporaryFriendsList, true, false);

                            listViewRecentChats_.setAdapter(recentChatAdapter_);
                        }
                    }
                }
                else if (groupMember_.equals("PH"))
                {
                    for (int index = 0; index < completeFriendsList_.size(); ++index)
                    {
                        if (completeFriendsList_.get(index).getName().toLowerCase().contains(s.toString().toLowerCase()) ||
                                completeFriendsList_.get(index).getMobile_no().contains(s.toString()))
                        {
                            positionPH_.add(index);

                            temporaryPhonesList.add(completeFriendsList_.get(index));
                        }
                    }

                    if (temporaryPhonesList.size() <= 0)
                        GlobalUtills.showToast(getString(R.string.no_records), getActivity().getApplicationContext());

                    else
                    {
                        phoneContactAdapter_ = new ChatContactAdapter(getActivity(), temporaryPhonesList);

                        GlobalUtills.list_chat.setAdapter(phoneContactAdapter_);
                    }
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

        buttonContacts_.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (viewContact_.getVisibility() != View.VISIBLE)
                {
                    newSingleMessage_ = true;

                    if (globalUtills_.haveNetworkConnection(getActivity()))
                    {
                        changeTab("PH");

                        phoneNumber_ = "";

                        phoneList_.clear();
                        positionFB_.clear();
                        positionPH_.clear();
                        positionC_.clear();

                        System.out.println("phone no." + phoneNumber_);

                        if (completeFriendsList_.size() > 0 && !newMessage_)
                        {
                            if (phoneContactAdapter_ == null)
                            {
                                phoneContactAdapter_ = new ChatContactAdapter(getActivity(), completeFriendsList_);

                                GlobalUtills.list_chat.setAdapter(phoneContactAdapter_);
                            }
                        }
                        else
                        {
                            newMessage_ = false;

                            getFriends();
                        }
                    }
                    else
                    {
                        if (completeFriendsList_.size() <= 0)
                            GlobalUtills.showToast(GlobalConstant.ERROR_NO_NETWORK_CONNECTION, getActivity());

                        else
                        {
                            phoneNumber_ = "";

                            phoneList_.clear();
                            positionFB_.clear();
                            positionPH_.clear();
                            positionC_.clear();

                            phoneContactAdapter_ = new ChatContactAdapter(getActivity(), completeFriendsList_);

                            GlobalUtills.list_chat.setAdapter(phoneContactAdapter_);
                        }
                    }
                }

                groupMember_ = "g";

                editTextContactSearch_.setText("");

                groupMember_ = "PH";
            }
        });

        buttonFacebookMembers_.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (viewFacebookGroupMember_.getVisibility() != View.VISIBLE)
                {
                    newSingleMessage_ = true;

                    if( globalUtills_.haveNetworkConnection(getActivity()) )
                    {
                        changeTab("FB_M");

                        phoneList_.clear();
                        positionFB_.clear();
                        positionPH_.clear();
                        positionC_.clear();

                        newMessage_ = false;

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                            new ActiveFriendsGetter().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                        else
                            new ActiveFriendsGetter().execute();
                    }
                    else
                    {
                        if (usersList_.size() <= 0)
                            GlobalUtills.showToast(GlobalConstant.ERROR_NO_NETWORK_CONNECTION, getActivity());

                        else
                        {
                            phoneList_.clear();

                            socialUsers_ = new ShowSocialFriends(getActivity(), usersList_, true, false);

                            listViewGroupMemberChat_.setAdapter(socialUsers_);
                        }
                    }

                    groupMember_ = "g";

                    editTextContactSearch_.setText("");

                    groupMember_ = "FB_M";

                    System.gc();
                }
            }
        });

        buttonRecentChats_.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (viewRecentChats_.getVisibility() != View.VISIBLE)
                {
                    changeTab("C");

                    phoneList_.clear();
                    positionFB_.clear();
                    positionPH_.clear();
                    positionC_.clear();

                    newSingleMessage_ = true;

                    if (!globalUtills_.haveNetworkConnection(getActivity()))
                        GlobalUtills.showToast(GlobalConstant.ERROR_NO_NETWORK_CONNECTION, getActivity());

                    else
                    {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                            new RecentChatsGetter(global_.getUser_id()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                        else
                            new RecentChatsGetter(global_.getUser_id()).execute();
                    }
                }

                groupMember_ = "g";

                editTextContactSearch_.setText("");

                groupMember_ = "C";
            }
        });

        GlobalUtills.list_chat.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id)
            {
                final int positionFBM;

                if (GlobalUtills.badge1 != null && GlobalUtills.badge1.isShown())
                {
                    GlobalUtills.msgCountSingle = "";
                    GlobalUtills.badge1.toggle();

                    SharedPreferences preferences = getActivity().getSharedPreferences("login", Context.MODE_PRIVATE);
                    Editor editor = preferences.edit();

                    editor.remove("notification_flag_single_chat");
                    editor.commit();
                }

                if (positionPH_.size() > 0)
                    positionFBM = positionPH_.get(position);

                else
                    positionFBM = position;

                if (completeFriendsList_.get(positionFBM).getId().equals(""))
                {
                    optionsDialog(completeFriendsList_.get(positionFBM).getName() + " is not Registered..!", "Contact No.: " +
                            completeFriendsList_.get(positionFBM).getMobile_no(), positionFBM, false);

                    if (!editTextContactSearch_.getText().toString().equals(""))
                        editTextContactSearch_.setText("");
                }
                else
                {
                    Intent gotoChatting = new Intent(getActivity(), ChatOneToOne.class);

                    gotoChatting.putExtra("name",                      completeFriendsList_.get(positionFBM).getName() + "");
                    gotoChatting.putExtra("fbID",                      completeFriendsList_.get(positionFBM).getId() + "");
                    gotoChatting.putExtra(GlobalConstant.PHONE_NUMBER, completeFriendsList_.get(positionFBM).getMobile_no() + "");

                    startActivity(gotoChatting);

                    if (!editTextContactSearch_.getText().toString().equals(""))
                        editTextContactSearch_.setText("");
                }

                positionFB_.clear();
            }
        });

        listViewGroupMemberChat_.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (GlobalUtills.badge1 != null && GlobalUtills.badge1.isShown())
                {
                    GlobalUtills.msgCountSingle = "";
                    GlobalUtills.badge1.toggle();

                    SharedPreferences preferneces = getActivity().getSharedPreferences("login", Context.MODE_PRIVATE);
                    Editor            editor      = preferneces.edit();

                    editor.remove("notification_flag_single_chat");
                    editor.commit();
                }

                final int positionFBM;

                if (positionFB_.size() > 0)
                    positionFBM = positionFB_.get(position);

                else
                    positionFBM = position;

                Intent gotoChatting = new Intent(getActivity(), ChatOneToOne.class);

                positionFB_.clear();

                gotoChatting.putExtra("name",                      usersList_.get(positionFBM).getName() + "");
                gotoChatting.putExtra("fbID",                      usersList_.get(positionFBM).getId() + "");
                gotoChatting.putExtra(GlobalConstant.PHONE_NUMBER, usersList_.get(positionFBM).getMobile_no() + "");

                startActivity(gotoChatting);

                if (!usersList_.get(position).getUnread_count().equalsIgnoreCase("0"))
                {
                    usersList_.get(position).setUnread_count("0");

                    socialUsers_.notifyDataSetChanged();

                    newMessage_ = true;
                }

                if (!editTextContactSearch_.getText().toString().equals(""))
                    editTextContactSearch_.setText("");
            }
        });

        registerForContextMenu(listViewRecentChats_);
        listViewRecentChats_.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)  // TODO: c/p
            {
                if (GlobalUtills.badge1 != null && GlobalUtills.badge1.isShown())
                {
                        GlobalUtills.msgCountSingle = "";
                        GlobalUtills.badge1.toggle();

                        SharedPreferences preferences = getActivity().getSharedPreferences("login", Context.MODE_PRIVATE);
                        Editor editor = preferences.edit();

                        editor.remove("notification_flag_single_chat");
                        editor.commit();
                }

                final int positionCM;

                if (positionC_.size() > 0)
                    positionCM = positionC_.get(position);

                else
                    positionCM = position;

                Intent gotoChatting = new Intent(getActivity(), ChatOneToOne.class);

                positionC_.clear();

                gotoChatting.putExtra("name",                      recentChatFriendList_.get(positionCM).getName() + "");
                gotoChatting.putExtra("fbID",                      recentChatFriendList_.get(positionCM).getId() + "");
                gotoChatting.putExtra(GlobalConstant.PHONE_NUMBER, recentChatFriendList_.get(positionCM).getMobile_no() + "");

                startActivity(gotoChatting);

                if (!recentChatFriendList_.get(position).getUnread_count().equalsIgnoreCase("0"))
                {
                    recentChatFriendList_.get(position).setUnread_count("0");

                    recentChatAdapter_.notifyDataSetChanged();

                    newMessage_ = true;
                }

                if (!editTextContactSearch_.getText().toString().equals(""))
                    editTextContactSearch_.setText("");
            }
        });

        return container;
    }

    //--------------------------------------------------------------------------------------------------------------------------
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);

        getActivity().getMenuInflater().inflate(R.menu.tab_chat_fragment, menu);
    }

    //--------------------------------------------------------------------------------------------------------------------------
    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

        switch (item.getItemId())
        {
        case R.id.remove_chat:
            new Remove_chat(getActivity(), global_.getUser_id(), recentChatFriendList_.get(info.position).getId(),
                            info.position).execute();

            return true;

        default:
            return super.onContextItemSelected(item);
        }
    }

    //--------------------------------------------------------------------------------------------------------------------------
    public int getSpecialCharacterCount(String string)
    {
        if (string == null || string.trim().isEmpty())
            return 0;

        int count = 0;

        for (int index = 0; index < string.length(); ++index)
            if (string.substring(index, 1).matches("[^A-Za-z0-9 ]"))
                ++count;

        return count;
    }

    //--------------------------------------------------------------------------------------------------------------------------
    private void changeTab(String tabName)
    {
        if (tabName.equals("C"))
        {
            buttonFacebookMembers_.setTextColor(GlobalConstant.COLOR_BLACK);
            buttonContacts_.setTextColor(GlobalConstant.COLOR_BLACK);
            buttonRecentChats_.setTextColor(GlobalConstant.COLOR_RED);

            GlobalUtills.list_chat.setVisibility(View.GONE);
            listViewGroupMemberChat_.setVisibility(View.GONE);
            listViewRecentChats_.setVisibility(View.VISIBLE);
            viewContact_.setVisibility(View.INVISIBLE);
            viewRecentChats_.setVisibility(View.VISIBLE);
            viewFacebookGroupMember_.setVisibility(View.INVISIBLE);
        }
        else if (tabName.equals("PH"))
        {
            buttonContacts_.setTextColor(GlobalConstant.COLOR_RED);
            buttonFacebookMembers_.setTextColor(GlobalConstant.COLOR_BLACK);
            buttonRecentChats_.setTextColor(GlobalConstant.COLOR_BLACK);

            listViewGroupMemberChat_.setVisibility(View.GONE);
            GlobalUtills.list_chat.setVisibility(View.VISIBLE);
            listViewRecentChats_.setVisibility(View.GONE);
            viewContact_.setVisibility(View.VISIBLE);
            viewRecentChats_.setVisibility(View.INVISIBLE);
            viewFacebookGroupMember_.setVisibility(View.INVISIBLE);
        }
        else if( tabName.equals("FB_M") )
        {
            buttonFacebookMembers_.setTextColor(GlobalConstant.COLOR_RED);
            buttonContacts_.setTextColor(GlobalConstant.COLOR_BLACK);
            buttonRecentChats_.setTextColor(GlobalConstant.COLOR_BLACK);

            listViewRecentChats_.setVisibility(View.GONE);
            GlobalUtills.list_chat.setVisibility(View.GONE);
            listViewGroupMemberChat_.setVisibility(View.VISIBLE);
            viewContact_.setVisibility(View.INVISIBLE);
            viewRecentChats_.setVisibility(View.INVISIBLE);
            viewFacebookGroupMember_.setVisibility(View.VISIBLE);
        }
    }

    //--------------------------------------------------------------------------------------------------------------------------
    private void getValues(ViewGroup container)
    {
        GlobalUtills.list_chat   = (ListView) container.findViewById(R.id.list_chat);
        listViewGroupMemberChat_ = (ListView) container.findViewById(R.id.list_chatGmember);
        listViewRecentChats_     = (ListView) container.findViewById(R.id.ListVChats_recent);

        actionBarCommon_         = (ActionBarCommon) container.findViewById(R.id.action_bar);
        buttonRecentChats_       = (Button)          container.findViewById(R.id.btnChats_recent);
        buttonContacts_          = (Button)          container.findViewById(R.id.btnContacts);
        buttonFacebookMembers_   = (Button)          container.findViewById(R.id.btnFbGroupmembers);
        editTextContactSearch_   = (EditText)        container.findViewById(R.id.search_edit_text_chat_activity);
        viewRecentChats_         = (View)            container.findViewById(R.id.viewChats_recent);
        viewFacebookGroupMember_ = (View)            container.findViewById(R.id.viewbtnFBgroupmember);
        viewContact_             = (View)            container.findViewById(R.id.viewbtncontact);
    }

    //--------------------------------------------------------------------------------------------------------------------------
    private void getPhoneContacts()
    {
        if (phoneListR_.size() > 0)
            phoneListR_.clear();

        ContentResolver contentResolver = getActivity().getContentResolver();

        Cursor cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[ ]
            {
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.RawContacts.ACCOUNT_TYPE
            },
            ContactsContract.RawContacts.ACCOUNT_TYPE + " <> 'google' ", null, null);

        if (cursor.getCount() <= 0)
            Toast.makeText(getActivity(), "No Phone Contact Found..!", Toast.LENGTH_SHORT).show();

        else
        {
            StringBuilder stringBuilder = new StringBuilder();

            while (cursor.moveToNext())
            {
                FriendInfo contactDetail    = new FriendInfo();
                String     phoneNumber      = cursor.getString(cursor.getColumnIndex(
                                                  ContactsContract.CommonDataKinds.Phone.NUMBER));

                String     name             = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String     validPhoneNumber = phoneNumber.replaceAll(" ", "");

                System.out.println("ph_noValid----->" + validPhoneNumber);

                if (validPhoneNumber.length() > 6   && !validPhoneNumber.contains("*") &&
                    !validPhoneNumber.contains("#") && !validPhoneNumber.contains("&") && !validPhoneNumber.contains("%") )
                {
                    try
                    {
                        if(validPhoneNumber.contains("("))  // TODO:
                            validPhoneNumber = validPhoneNumber.replace("(", "");

                        if (validPhoneNumber.contains(")"))
                            validPhoneNumber = validPhoneNumber.replace(")", "");

                        if (validPhoneNumber.contains("-"))
                            validPhoneNumber = validPhoneNumber.replace("-", "");

                        contactDetail.setMobileNumber("" + validPhoneNumber);
                        contactDetail.setName        (name);
                        contactDetail.setId          ("");
                        contactDetail.setUnread_count("0");

                        phoneListR_.add(contactDetail);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    try
                    {
                        if (stringBuilder.length() > 0)
                            stringBuilder.append(',');

                        stringBuilder.append(validPhoneNumber);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    phoneNumber_ = stringBuilder.toString();
                }
            }
        }
    }

    //--------------------------------------------------------------------------------------------------------------------------
    private void getFriends()
    {
        try
        {
            progressDialog_ = new TransparentProgressDialog(getActivity(), R.drawable.loading_spinner_icon);

            if (completeFriendsList_.isEmpty())
                progressDialog_.show();

            Session activeSession = Session.getActiveSession();

            if (activeSession == null)
                activeSession = Session.openActiveSessionFromCache(getActivity());

                if (activeSession.getState().isOpened())
                {
                    Request friendRequest = Request.newMyFriendsRequest(activeSession, new GraphUserListCallback()
                    {
                        public void onCompleted(List<GraphUser> users, Response response)
                        {
                            try
                            {
                                String            friendsList = response.getGraphObject().getProperty("data").toString();
                                SharedPreferences preferences = getActivity().getSharedPreferences("FacebookFrnd",
                                                                                                   Context.MODE_PRIVATE);
                                Editor            editor      = preferences.edit();

                                editor.clear    ();
                                editor.commit   ();
                                editor.putString("FriendList", friendsList + "");
                                editor.commit   ();

                                setFriendInfo();
                            }
                            catch (Exception e)
                            {
                                if (progressDialog_.isShowing())
                                    progressDialog_.dismiss();
                                setFriendInfo();
                                e.printStackTrace();
                            }
                        }
                    });

                    Bundle params = new Bundle();

                    params.putString("fields", "id,name,email,picture,gender");  // TODO:

                    friendRequest.setParameters(params);
                    friendRequest.executeAsync ();
                }
            else {
                    if (progressDialog_.isShowing())
                        progressDialog_.dismiss();
                }

        }
        catch (Exception e)
        {
            if (progressDialog_.isShowing())
                progressDialog_.dismiss();

            e.printStackTrace();
        }
    }

    //--------------------------------------------------------------------------------------------------------------------------
    private void setFriendInfo()
    {
        try
        {
            if (!friendInfoList_.isEmpty())
                friendInfoList_.clear();

            ArrayList<HashMap<String, String>> friendsListArray = new ArrayList<HashMap<String, String>>();
            SharedPreferences                  preferences      = getActivity().getSharedPreferences("FacebookFrnd",
                                                                                                     Context.MODE_PRIVATE);
            String                             friendsList      = preferences.getString("FriendList", "");
            JSONArray                          jsonArray        = new JSONArray();

            facebookIdCheckValidation_ = "";

            JSONArray jsonFriendsList = new JSONArray(friendsList);

            for (int index = 0; index < jsonFriendsList.length(); ++index)
            {
                JSONObject object  = jsonFriendsList.getJSONObject(index);
                JSONObject picture = object.getJSONObject("picture");
                JSONObject url     = picture.getJSONObject("data");

                String id = object.getString("id");

                friendInfo_ = new FriendInfo();

                String imageURL = url.getString("url");


                friendInfo_.setId(id);

                String name = object.getString("name");

                friendInfo_.setImage(imageURL);

                HashMap<String, String> map = new HashMap<String, String>();

                map.put("id", id);

                friendsListArray.add(map);

                JSONObject jsonObject = new JSONObject();

                jsonObject.put("userid", id);

                jsonArray.put(jsonObject);

                friendInfo_.setName(name);

                if (facebookIdCheckValidation_.equals(""))
                    facebookIdCheckValidation_ = id + "";

                else
                    facebookIdCheckValidation_ = facebookIdCheckValidation_ + "," + id;
            }

            new ValidFriendsGetter().execute();
        }
        catch (Exception e)
        {
            if (progressDialog_.isShowing())
                progressDialog_.dismiss();

            e.printStackTrace();
        }
    }

    //--------------------------------------------------------------------------------------------------------------------------
    private void optionsDialog(final String Name, final String subHeading, final int positionFBM, final boolean registered)
    {
        final Dialog   dialog = globalUtills_.prepararDialog(getActivity(), R.layout.dialog_three_options);
              TextView title  = (TextView) dialog.findViewById(R.id.txtVmainTitle);

        title.setText(Name.toString());

        TextView textViewSubHeading = (TextView) dialog.findViewById(R.id.txtVsubheading);

        textViewSubHeading.setText(subHeading.toString());

        Button      buttonChat   = (Button)      dialog.findViewById(R.id.btnChat);  // TODO: c/p
        Button      buttonCall   = (Button)      dialog.findViewById(R.id.btncall);
        Button      buttonGroups = (Button)      dialog.findViewById(R.id.btngroups);
        ImageButton buttonClose  = (ImageButton) dialog.findViewById(R.id.btnCloseDialog);

        if (!registered)
        {
            buttonGroups.setVisibility(View.GONE);
            buttonChat.setText("Send Request");
        }

        buttonChat.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if( registered )
                {
                    Intent gotoChatting = new Intent(getActivity(), ChatOneToOne.class);

                    gotoChatting.putExtra("name", completeFriendsList_.get(positionFBM).getName() + "");
                    gotoChatting.putExtra("fbID", completeFriendsList_.get(positionFBM).getId() + "");

                    startActivity(gotoChatting);
                }
                else
                {
                    String temporaryPhone = completeFriendsList_.get(positionFBM).getMobile_no();

                    if (completeFriendsList_.get(positionFBM).getMobile_no().charAt(0) == '+')
                        temporaryPhone = completeFriendsList_.get(positionFBM).getMobile_no().substring(1, completeFriendsList_.get(positionFBM).getMobile_no().length());

                    temporaryPhone = temporaryPhone.replaceAll(" ", "");

                    // TODO:
                    String shareBody     = "Hey ..! i found get-groupy an osm application to connect with your friends..   https://play.google.com/store/apps/details?id=com.app.messenger&hl=en";
                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);

                    sharingIntent.setType ("text/plain");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Get-Groupy");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);

                    startActivity(Intent.createChooser(sharingIntent, "Invite Friends"));
                }

                dialog.dismiss();
            }
        });

        buttonCall.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    Intent callIntent = new Intent("android.intent.action.CALL_PRIVILEGED");

                    callIntent.setData(Uri.parse("tel:" + completeFriendsList_.get(positionFBM).getMobile_no()));

                    startActivity(callIntent);
                }
                catch (Exception e)
                {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);

                    callIntent.setData (Uri.parse("tel:" + completeFriendsList_.get(positionFBM).getMobile_no()));
                    callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    startActivity(callIntent);
                }

                dialog.dismiss();
            }
        });

        buttonGroups.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String FriendID = completeFriendsList_.get(positionFBM).getId();

                System.out.println(FriendID + "friend's id");

                Intent gotoHangout_groups = new Intent(getActivity(), HangoutFriendGroup.class);

                positionPH_.clear();

                gotoHangout_groups.putExtra("FrndID", FriendID + "");  // TODO:

                startActivity(gotoHangout_groups);

                dialog.dismiss();
            }
        });

        buttonClose.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    //--------------------------------------------------------------------------------------------------------------------------
    private static ArrayList<FriendInfo> recentChatFriendList_      = new ArrayList<FriendInfo>();
    private static ArrayList<FriendInfo> completeFriendsList_       = new ArrayList<FriendInfo>();
    private static ArrayList<FriendInfo> usersList_                 = new ArrayList<FriendInfo>();
    private static boolean               newSingleMessage_          = false;
    private static boolean               newMessage_                = false;
    private static ChatContactAdapter    phoneContactAdapter_;
    private static ShowSocialFriends     socialUsers_;
    private static ShowSocialFriends     recentChatAdapter_;
    private static String                facebookIdCheckValidation_ = "";
    private static String                groupMember_               = "C";

    //--------------------------------------------------------------------------------------------------------------------------
    private ActionBarCommon           actionBarCommon_;
    private ArrayList<FriendInfo>     allFriendsList_          = new ArrayList<FriendInfo>();
    private ArrayList<FriendInfo>     friendInfoList_          = new ArrayList<FriendInfo>();
    private ArrayList<FriendInfo>     phoneList_               = new ArrayList<FriendInfo>();
    private ArrayList<FriendInfo>     phoneListR_              = new ArrayList<FriendInfo>();  // TODO: Used while searching
    private ArrayList<Integer>        positionFB_              = new ArrayList<Integer>();
    private ArrayList<Integer>        positionPH_              = new ArrayList<Integer>();
    private ArrayList<Integer>        positionC_               = new ArrayList<Integer>();  // TODO: Used while searching
    private Button                    buttonRecentChats_;
    private Button                    buttonContacts_;
    private Button                    buttonFacebookMembers_;
    private EditText                  editTextContactSearch_;
    private FriendInfo                friendInfo_;
    private Global                    global_;
    private GlobalUtills              globalUtills_;
    private ListView                  listViewGroupMemberChat_;
    private ListView                  listViewRecentChats_;
    private String                    facebookID_              = "";
    private String                    phoneNumber_             = "";
    private TransparentProgressDialog progressDialog_;
    private View                      viewRecentChats_;
    private View                      viewFacebookGroupMember_;
    private View                      viewContact_;
}
