package com.app.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.messenger.Facebook_ProfilePictureView_rounded;
import com.app.messenger.Global;
import com.app.messenger.R;
import com.app.model.FriendInfo;

public class AddSocialFriendAdapter extends BaseAdapter {
	ArrayList<Integer> photo = new ArrayList<Integer>();
	int i = 0;
	Boolean click = true;
	Global global;
	ArrayList<FriendInfo> list_of_allfriend = new ArrayList<FriendInfo>();
	ArrayList<String> list_of_ids = new ArrayList<String>();
	HashMap<Integer, String> ids_map = new HashMap<Integer, String>();
	HashMap<Integer, String> ids_map_PH = new HashMap<Integer, String>();

	// Boolean click_array[];

	Context context;
	public static ArrayList<Boolean> radio_checkCONtacts = new ArrayList<Boolean>();
	RadioButton check_button;

	Facebook_ProfilePictureView_rounded freinds_image;
	TextView name,ph_no;

	RelativeLayout paddingLayout;
	ImageView imgVph_FB;
	 

	public AddSocialFriendAdapter(Context context,ArrayList<FriendInfo> list_of_allfriend)
	{
		this.context = context;
		global = new Global();
		this.list_of_allfriend = list_of_allfriend;
		// click_array = new Boolean[list_of_allfriend.size()];

		
		if(!(radio_checkCONtacts.size()>0))
		{
			

			for (int i = 0; i < list_of_allfriend.size(); i++) {
				radio_checkCONtacts.add(false);
			}
		}
		

	}

	@Override
	public int getCount() {
		return list_of_allfriend.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {

		return arg0;
	}

	@Override
	public View getView(final int arg0, View view, ViewGroup container)

	{

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = inflater.inflate(R.layout.friend_adapter, null);

		name = (TextView) view.findViewById(R.id.Friend_name);
		ph_no = (TextView) view.findViewById(R.id.txtV_FBphno);
		freinds_image = (Facebook_ProfilePictureView_rounded) view
				.findViewById(R.id.friends_title_image);

		check_button = (RadioButton) view.findViewById(R.id.friend_check_box);
		paddingLayout = (RelativeLayout) view.findViewById(R.id.top_container);
		imgVph_FB = (ImageView) view.findViewById(R.id.imgV_ph_FB);

		if (radio_checkCONtacts.get(arg0)) {
			check_button.setChecked(true);
		} else {
			check_button.setChecked(false);
		}

		if (list_of_allfriend.get(arg0).getId().equals("")) {
			freinds_image.setVisibility(View.GONE);
			name.setText(list_of_allfriend.get(arg0).getName());
			ph_no.setText(""+list_of_allfriend.get(arg0).getMobile_no());
			
			
//			if(list_of_allfriend.get(arg0).getName().contains("(")){
//				imgVph_FB.setImageResource(R.drawable.app_icon_small);
//			}
		
			

		} else {
			freinds_image.setVisibility(View.VISIBLE);
			name.setText(list_of_allfriend.get(arg0).getName());

			freinds_image.setProfileId(list_of_allfriend.get(arg0).getId());
			imgVph_FB.setImageResource(R.drawable.app_icon_g);
		}

		check_button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!radio_checkCONtacts.get(arg0)) {
					i++;
					((RadioButton) v).setChecked(true);

					// click_array[arg0] = false;
					radio_checkCONtacts.set(arg0, true);



					if (!list_of_allfriend.get(arg0).getId().equals("")) {
						ids_map.put(arg0, list_of_allfriend.get(arg0).getId());

						global.setHashMap(ids_map);
					} else {

						ids_map_PH.put(arg0, list_of_allfriend.get(arg0)
								.getMobile_no());

						global.setHashMap_ph(ids_map_PH);

					}

				} else {

					i--;

					((RadioButton) v).setChecked(false);
					radio_checkCONtacts.set(arg0, false);

					if (!list_of_allfriend.get(arg0).getId().equals("")) {

						if (ids_map.size() > 0) {
							ids_map.remove(arg0);

							global.setHashMap(ids_map);

						}

					} else {

						if (ids_map_PH.size() > 0) {
							ids_map_PH.remove(arg0);

							global.setHashMap_ph(ids_map_PH);
						}
					}

					// click_array[arg0] = true;

				}

			}
		});

		
		
		
		if (arg0 % 2 != 0) {
			view.setBackgroundColor(Color.parseColor("#ebebeb"));
		} else {
			view.setBackgroundColor(Color.parseColor("#ffffff"));
		}

		/*
		 * ImageLoading.BitmapManager.INSTANCE.loadBitmap(list_of_allfriend.get(arg0
		 * ).get(GlobalConstants.Friends_pic),holder.freinds_image,100 ,100);
		 * 
		 * String k
		 * =list_of_allfriend.get(arg0).get(GlobalConstants.Friend_Name); int
		 * index =k.indexOf("*"); String sub_str =k.substring(0, index);
		 */

		return view;
	}

	/*
	 * private class Holder { ProfilePictureView freinds_image; TextView name;
	 * 
	 * RelativeLayout paddingLayout; ImageView imgVph_FB;
	 * 
	 * }
	 */

}
