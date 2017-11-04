package com.wzb.smartcard.activity;

import com.wzb.smartcard.R;

import android.app.Application.OnProvideAssistDataListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author wzb<wangzhibin_x@qq.com>
 * @date Nov 4, 2017 9:13:35 PM
 */
public class UserInfoActivity extends BaseActivity {

	private TextView titleView;
	private ImageView backView;
	private Button btn_read_userinfo, btn_deposit;
	private TextView tv_userinfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_userinfo);
		initTitleView();
		initView();
	}

	private void initView() {
		btn_read_userinfo = (Button) findViewById(R.id.btn_read_userinfo);
		btn_read_userinfo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

			}
		});

		btn_deposit = (Button) findViewById(R.id.btn_deposit);
		btn_deposit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setClass(UserInfoActivity.this, DepositActivity.class);
				startActivity(intent);
			}
		});

		tv_userinfo = (TextView) findViewById(R.id.tv_user_info);
	}

	private void initTitleView() {
		backView = (ImageView) findViewById(R.id.title_back);
		titleView = (TextView) findViewById(R.id.title_text);
		titleView.setText("UserInfo");
		backView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});

	}

}
