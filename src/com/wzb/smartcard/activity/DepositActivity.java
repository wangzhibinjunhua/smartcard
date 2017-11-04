package com.wzb.smartcard.activity;

import com.wzb.smartcard.R;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author wzb<wangzhibin_x@qq.com>
 * @date Nov 4, 2017 9:26:17 PM	
 */
public class DepositActivity extends BaseActivity{
	private TextView titleView;
	private ImageView backView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_deposit);
		initTitleView();
	}
	
	private void initTitleView() {
		backView = (ImageView) findViewById(R.id.title_back);
		titleView = (TextView) findViewById(R.id.title_text);
		titleView.setText("Deposit");
		backView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});

	}

}
