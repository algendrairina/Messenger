package com.app.webserviceshandler;

import android.content.Context;
import android.util.Log;

import com.app.messenger.GlobalConstant;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class WebServiceHandler {

	public final static int GET = 1;
	public final static int POST = 2;
	Context context;
	HttpClient httpClientG;
	
	public String makeServiceCall(String url, int method,
			List<NameValuePair> params) {
		Log.i("Sending URL1", "Sending URL1 " + "   " + url);
		String response = null;
		try {
		
			
			final int CONN_WAIT_TIME = 20000;
			final int CONN_DATA_WAIT_TIME = 20000;
//

//			DefaultHttpClient postClient = new DefaultHttpClient(httpParams);
			
			HttpParams httpParams = new BasicHttpParams();
			httpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
//			HttpConnectionParams.setConnectionTimeout(httpParams, CONN_WAIT_TIME);
//			HttpConnectionParams.setSoTimeout(httpParams, CONN_DATA_WAIT_TIME);
//			HttpConnectionParams.setTcpNoDelay(httpParams, true);
		
			 httpClientG = new DefaultHttpClient(httpParams);
			

			
//			 = null;
//			 = null;
			
			
			// Checking http request method type
			if (method == POST) {
				HttpPost httpPost = new HttpPost(url);
				
				httpPost.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);	//new by gagan
				
				if (params != null) {
					
					httpPost.setEntity(new UrlEncodedFormEntity(params));
				}

				HttpResponse httpResponse = httpClientG.execute(httpPost);
				
				
				HttpEntity httpEntity = httpResponse.getEntity();
			
				response = EntityUtils.toString(httpEntity);

			} else if (method == GET) {
				// appending params to url
				if (params != null) {
					String paramString = URLEncodedUtils
							.format(params, "utf-8");
					url += "?" + paramString;

				
				}

				try {
					
					
					
					HttpGet httpGet = new HttpGet(url);
					
//					httpGet.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);   //new by gagan
					
					HttpResponse httpResponse= httpClientG.execute(httpGet);
				
					HttpEntity httpEntity = httpResponse.getEntity();
				
					response = EntityUtils.toString(httpEntity);
				
			         
					
				}catch (Exception e) {

					httpClientG.getConnectionManager().shutdown();
					return "Slow";
				}

			}
			
			
//			httpClient.getConnectionManager().shutdown();
			
		} catch(ConnectTimeoutException e)
		{
			
			httpClientG.getConnectionManager().shutdown();
			return "Slow";
			
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Log.i("Sending3 response", "Sending3 response " + "   " + response);
		return response;

	}






	public String makeServiceCallSendchat(String url, int method,
			List<NameValuePair> params) {
		Log.i("Sending URL2", "Sending URL2 " + "   " + url);
		
		String response = null;
		try {
			// http client
			DefaultHttpClient httpClient = new DefaultHttpClient();
			
			
			
			HttpEntity httpEntity = null;
			HttpResponse httpResponse = null;
			// Checking http request method type
			if (method == POST) {
				HttpPost httpPost = new HttpPost(url);
				
				if (params != null) {
					httpPost.setEntity(new UrlEncodedFormEntity(params));
				}

				httpResponse = httpClient.execute(httpPost);
				
				
				httpEntity = httpResponse.getEntity();
			
				response = EntityUtils.toString(httpEntity);

			} else if (method == GET) {
				// appending params to url
				if (params != null) {
					String paramString = URLEncodedUtils
							.format(params, "utf-8");
					url += "?" + paramString;
					System.out.println(url);
				
				}

				try {
					
					
					
					HttpGet httpGet = new HttpGet(url);

					httpResponse = httpClient.execute(httpGet);
				
					httpEntity = httpResponse.getEntity();
				
					response = EntityUtils.toString(httpEntity);
					
					
				} catch (Exception e) {
					System.out.println("EXCEPTION FROM THE SERVER"
							+ e.getMessage());
					return response = GlobalConstant.ERROR_CODE_STRING;
				}

			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Log.i("Sending response", "Sending response " + "   " + response);
		return response;

	}

	
}
