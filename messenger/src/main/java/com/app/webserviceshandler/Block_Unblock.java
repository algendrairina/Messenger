package com.app.webserviceshandler;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.app.messenger.GlobalConstant;
import com.app.messenger.R;
import com.app.util.GlobalUtills;
import com.app.util.TransparentProgressDialog;

public class Block_Unblock extends AsyncTask<Void , Void , String>
{

	String						response	= "";
	Context						con;

	String						userid, frndID, status, chkblock;

	TransparentProgressDialog	pd;

	Handler						responseHandler;

	public Block_Unblock(Context con , String user , String frndID , String index , Handler responseHandlerRatings)
	{

		this.con = con;
		this.userid = user;
		this.frndID = frndID;
		this.status = index;
		this.chkblock = "N";
		this.responseHandler = responseHandlerRatings;
	}

	public Block_Unblock(Context con , String user , String frndID , Handler responseHandlerRatings)
	{
		this.con = con;
		this.userid = user;
		this.frndID = frndID;
		this.status = "B";
		this.chkblock = "Y";
		this.responseHandler = responseHandlerRatings;
	}

	@Override
	protected void onPreExecute()
	{

		pd = new TransparentProgressDialog(con, R.drawable.loading_spinner_icon);
		if( chkblock.equals("Y") )
		{

		}
		else
		{
			pd.show();

		}

		super.onPreExecute();
	}

	@Override
	protected String doInBackground(Void... params)
	{

		List<NameValuePair> param = new ArrayList<NameValuePair>();
		param.add(new BasicNameValuePair(GlobalConstant.POST_TYPE, "post"));
		param.add(new BasicNameValuePair(GlobalConstant.U_TYPE, "set_block_unblock"));

		param.add(new BasicNameValuePair(GlobalConstant.JOIN_USER_ID, "" + userid));

		param.add(new BasicNameValuePair("friend_id", "" + frndID));

		param.add(new BasicNameValuePair("status", "" + status));

		param.add(new BasicNameValuePair("chkblock", "" + chkblock));

		response = (new WebServiceHandler()).makeServiceCall(GlobalConstant.URL, WebServiceHandler.GET, param);

		return response;
	}

	@Override
	protected void onPostExecute(String result)
	{
		try
		{

			if( pd.isShowing() )
			{
				pd.dismiss();
			}

			JSONObject jsonObject = new JSONObject(result);
			result = jsonObject.getString(GlobalConstant.MESSAGE);
			
			
			if( result.contains(GlobalConstant.SUCCESS) )
			{
				if( chkblock.equals("Y") )
				{
					Bundle data = new Bundle();
					data.putString("Block", "N");
					Message msg = Message.obtain();
					msg.setData(data);
					responseHandler.sendMessage(msg);
				}
				else
				{
					// GlobalUtills.showToast("Success", con);
					Bundle data = new Bundle();
					data.putString("Block", status);
					Message msg = Message.obtain();
					msg.setData(data);
					responseHandler.sendMessage(msg);
				}

			}
			else if (result.contains(GlobalConstant.FAILURE)) {
			
				if( chkblock.equals("Y") && result.equals(GlobalConstant.FAILURE) )
				{
					Bundle data = new Bundle();
					data.putString("Block", "Y_me");
					Message msg = Message.obtain();
					msg.setData(data);
					responseHandler.sendMessage(msg);
				}
				else if ( chkblock.equals("Y") && result.equals("Failure_Other")) {
				
					Bundle data = new Bundle();
					data.putString("Block", "Y_other");
					Message msg = Message.obtain();
					msg.setData(data);
					responseHandler.sendMessage(msg);
				}

			}

		}
		catch(Exception e)
		{
			if( pd.isShowing() )
			{
				pd.dismiss();
			}
		}
		super.onPostExecute(result);
	}

}
