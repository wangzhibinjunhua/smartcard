package com.wzb.smartcard.activity;

import com.wzb.smartcard.R;
import com.wzb.smartcard.interf.WApplication;
import com.wzb.smartcard.view.MyGridAdapter;
import com.wzb.smartcard.view.MyGridView;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class HomeActivity extends BaseActivity implements OnClickListener{

	private TextView titleView;
	private ImageView backView;
	private MyGridView mGridView;
	private String[] img_text;
	private int[] img_icon = { R.drawable.deposit };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		initTitleView();
		initView();
	}

	private void initView() {
		Resources res = getResources();
		img_text = res.getStringArray(R.array.img_text);

		mGridView = (MyGridView) findViewById(R.id.gridview);
		mGridView.setAdapter(new MyGridAdapter(this, img_text, img_icon));
		mGridView.setOnItemClickListener(new MyItemClickListener());
	}
	
	private void initTitleView() {
		backView = (ImageView) findViewById(R.id.title_back);
		titleView = (TextView) findViewById(R.id.title_text);
		titleView.setText("HomePage");
		backView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});

	}

	private class MyItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			// TODO Auto-generated method stub
			Intent intent = new Intent();
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			switch (arg2) {

			case 0:
				// intent.setClass(MainActivity.this,
				// UserManagerActivity.class);
				// startActivity(intent);
				break;

			default:
				break;
			}

		}

	}
	
	private long firstTime = 0;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			long secondTime = System.currentTimeMillis();
			if (secondTime - firstTime > 2000) {// 如果两次按键时间间隔大于2秒，则不退出
				Toast.makeText(this, getString(R.string.exit_dialog), 666).show();
				firstTime = secondTime;// 更新firstTime
				return true;
			} else {// 两次按键小于2秒时，退出应用
				exit();
				return false;
			}
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}
	
	private void exit() {
		for (Activity activity : WApplication.activityList) {
			activity.finish();
		}
		finish();
		System.exit(0);
		System.gc();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

}
