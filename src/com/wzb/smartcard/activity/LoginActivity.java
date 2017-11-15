package com.wzb.smartcard.activity;

import com.wzb.smartcard.R;
import com.wzb.smartcard.util.CardManager;
import com.wzb.smartcard.util.LogUtil;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends BaseActivity implements OnClickListener {

	private Button btn_login;
	private Button btn_readcard;
	private EditText et_ps;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		initView();
		Log.e("wzb", "loginactivity oncreate");
	}

	private void initView() {
		btn_login = (Button) findViewById(R.id.btn_login);
		btn_login.setOnClickListener(this);
		btn_readcard=(Button)findViewById(R.id.btn_read_card);
		btn_readcard.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_login:
			login();
			break;
		case R.id.btn_read_card:
			read_card();
			break;
		default:
			break;

		}
	}
	
	private void read_card(){
		Log.e("wzb", "read_card");
		boolean ret=CardManager.SelectCPU_EF();
		Log.e("wzb", "ret:"+ret);
	}

	private void login() {
		Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		LoginActivity.this.startActivity(intent);
		
		//finish();
	}

}
