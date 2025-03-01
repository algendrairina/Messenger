package com.app.webserviceshandler;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar.LayoutParams;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.app.adapter.ShowSocialFriends;
import com.app.messenger.ChatOneToOne;
import com.app.messenger.Global;
import com.app.messenger.GlobalConstant;
import com.app.messenger.R;
import com.app.model.FriendInfo;
import com.app.util.GlobalUtills;
import com.app.util.TransparentProgressDialog;

public class Getchat_history extends AsyncTask<Void, Void, String> 
{

	String response="";
	Context con;

	String userid;
	 String	message="";
	TransparentProgressDialog pd;
	ArrayList<FriendInfo> list_of_RECENTChats = new ArrayList<FriendInfo>();
	
	int distance=2;
	
	public Getchat_history(Context con,String user) {
		
		this.con=con;
		this.userid=user;
		
		list_of_RECENTChats.clear();
		
		
		SharedPreferences preferences = con.getSharedPreferences(
				"CountryPreferences", Context.MODE_PRIVATE);

	
		if (preferences.contains("km")) {
			distance = preferences.getInt("km", 2);
		} else {
			distance = 2;
		}
		
		
	}
	
	
	@Override
	protected void onPreExecute() {
		
		pd=new TransparentProgressDialog(con, R.drawable.loading_spinner_icon);
		pd.show();
		
		super.onPreExecute();
	}
	
	@Override
	protected String doInBackground(Void... params) {
		
		
//		http://ameba.get-groupy.com/webservice/get_posts/?post_type=post&mtype=mybuddy&userid=198&longitude=30.713409&latitude=76.699036&radius=10000
		
		List<NameValuePair> param = new ArrayList<NameValuePair>();
		param.add(new BasicNameValuePair(GlobalConstant.POST_TYPE, "post"));
		param.add(new BasicNameValuePair(GlobalConstant.U_TYPE, "mybuddy"));
	
		param.add(new BasicNameValuePair("userid", ""+userid));
		
		param.add(new BasicNameValuePair(GlobalConstant.LONGITUDE, "" + Global.longi));
		param.add(new BasicNameValuePair(GlobalConstant.LATITUDE, "" + Global.lati));
		param.add(new BasicNameValuePair("radius", ""+distance));
		
		
		
		  response = (new WebServiceHandler()).makeServiceCall(
		 GlobalConstant.URL, WebServiceHandler.GET, param);
		
		 
		  
		  
		  
		  
		  
		  
		  
			try {

			

				JSONObject jsonObject = new JSONObject(response);
			 	message = jsonObject.getString("message");

				if(message.equalsIgnoreCase(GlobalConstant.SUCCESS))
				{
					
				JSONArray jsonArray_group_list_info = jsonObject
						.getJSONArray("response");
			

				for (int i = 0; i < jsonArray_group_list_info.length(); i++) {

					FriendInfo friendinfo = new FriendInfo();

					JSONObject json_array_group_info = jsonArray_group_list_info
							.getJSONObject(i);

					

						friendinfo.setId(json_array_group_info
								.getString(GlobalConstant.FACE_BOOK_ID) + "");
						friendinfo.setImage(json_array_group_info
								.getString("userImage") + "");
						friendinfo.setName(json_array_group_info
								.getString("userName") + "");
						friendinfo.setUnread_count(json_array_group_info
								.getString("unread_count"));
						friendinfo.setMobileNumber(json_array_group_info
								.getString("user_telephone") + "");

						list_of_RECENTChats.add(friendinfo);

					

				}
			}

			} catch (JSONException e) {

				e.printStackTrace();
			} catch (Exception e) {

				e.printStackTrace();
			}
		  
		  
		  
		  
		  
		 
		 
		
		return message;
	}
	@Override
	protected void onPostExecute(String result) {
	try {
		
	
		
		if(pd.isShowing())
		{
			pd.dismiss();
		}
		
		
		if(result.contains(GlobalConstant.SUCCESS))
		{
						
			ShowChatlisty_recent(list_of_RECENTChats);
		}
		else
		{
			GlobalUtills.showToast("No chat history found ..!",con);
		}
		
		
		
		
		
		
	} catch (Exception e) {
		if(pd.isShowing())
		{
			pd.dismiss();
		}
	}
		super.onPostExecute(result);
	}

	
	
	
	
	
	
	
	
	
	public void ShowChatlisty_recent(final ArrayList<FriendInfo> list) {
		final Dialog dialogLoader = new Dialog(con, R.style.Theme_Dialog);
		dialogLoader.setTitle("Chats");
		dialogLoader.setContentView(R.layout.chat_history_list);
		dialogLoader.getWindow().setLayout(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
	
		dialogLoader.setCancelable(false);
		
		
		
		ListView listV=(ListView)dialogLoader.findViewById(R.id.Listv_recentChat);
		
		
		
		ShowSocialFriends socialusers = new ShowSocialFriends(con,
				list, true, false);
		listV.setAdapter(socialusers);
		
		
		
		listV.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
			
				Intent gotoChatting = new Intent(con,
						ChatOneToOne.class);
				gotoChatting.putExtra("name", list
						.get(position).getName() + "");
				gotoChatting.putExtra("fbID", list
						.get(position).getId() + "");
				gotoChatting.putExtra(GlobalConstant.PHONE_NUMBER, list
						.get(position).getMobile_no() + "");
				con.startActivity(gotoChatting);
				
				dialogLoader.dismiss();
				
			}
		});
		
		
		dialogLoader.show();
		
	}
	
	
	
	
}