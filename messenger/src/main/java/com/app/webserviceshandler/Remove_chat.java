package com.app.webserviceshandler;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.os.AsyncTask;

import com.app.messenger.ChatFragment;
import com.app.messenger.GlobalConstant;
import com.app.messenger.R;
import com.app.util.GlobalUtills;
import com.app.util.TransparentProgressDialog;

public class Remove_chat extends AsyncTask<Void, Void, String> 
{

	String response="";
	Context con;

	String userid,frndID;
	
	TransparentProgressDialog pd;
	int indexx;
	
	public Remove_chat(Context con,String user,String frndID,int index) {
		
		this.con=con;
		this.userid=user;
		this.frndID=frndID;
	this.indexx=index;
		
		
		
	}
	
	
	@Override
	protected void onPreExecute() {
		
		pd=new TransparentProgressDialog(con, R.drawable.loading_spinner_icon);
		pd.show();
		
		super.onPreExecute();
	}
	
	@Override
	protected String doInBackground(Void... params) {
		
		
//		http://get-groupy.com/webservice/get_posts/?mtype=remove_recent_chat&post_type=post
//			user_id, facebook_id
		
		List<NameValuePair> param = new ArrayList<NameValuePair>();
		param.add(new BasicNameValuePair(GlobalConstant.POST_TYPE, "post"));
		param.add(new BasicNameValuePair(GlobalConstant.U_TYPE, "remove_recent_chat"));
	
		param.add(new BasicNameValuePair(GlobalConstant.JOIN_USER_ID, ""+userid));
		
		param.add(new BasicNameValuePair(GlobalConstant.FACE_BOOK_ID, ""+frndID));
		
		
		
		  response = (new WebServiceHandler()).makeServiceCall(
		 GlobalConstant.URL, WebServiceHandler.GET, param);
		
		 
		  
		  
		 
		 
		
		return response;
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
			ChatFragment.recentChatFriendList().remove(indexx);
			ChatFragment.recentChatAdapter().notifyDataSetChanged();
			
		}
		else
		{
			GlobalUtills.showToast("Error..!",con);
		}
		
		
		
		
		
		
	} catch (Exception e) {
		if(pd.isShowing())
		{
			pd.dismiss();
		}
	}
		super.onPostExecute(result);
	}

	
	
}