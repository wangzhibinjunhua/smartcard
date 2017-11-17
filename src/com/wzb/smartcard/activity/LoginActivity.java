package com.wzb.smartcard.activity;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.wzb.smartcard.R;
import com.wzb.smartcard.interf.WApplication;
import com.wzb.smartcard.util.CardManager;
import com.wzb.smartcard.util.CustomDialog;
import com.wzb.smartcard.util.LogUtil;
import com.wzb.smartcard.util.ToastUtil;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.NetworkOnMainThreadException;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import cn.trinea.android.common.util.HttpUtils;
import junit.framework.Test;

public class LoginActivity extends BaseActivity implements OnClickListener {

	private Button btn_login;
	private Button btn_readcard;
	private EditText et_ps;
	private TextView tv_oper_name;
	private Context mContext;
	private String result = "";
	private String card_password="";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		mContext = LoginActivity.this;
		initView();
	}

	private void initView() {
		btn_login = (Button) findViewById(R.id.btn_login);
		btn_login.setOnClickListener(this);
		btn_readcard = (Button) findViewById(R.id.btn_read_card);
		btn_readcard.setOnClickListener(this);
		tv_oper_name = (TextView) findViewById(R.id.tv_card_user_name);
		et_ps=(EditText)findViewById(R.id.et_ps);
		et_ps.setEnabled(false);
		btn_login.setEnabled(false);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		et_ps.setText("");
		et_ps.setEnabled(false);
		btn_login.setEnabled(false);
		card_password="";
		tv_oper_name.setText("");
	}

	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1001://read ok
				CustomDialog.dismissDialog();
				String name=(String)msg.obj;
				tv_oper_name.setText(name);
				et_ps.setEnabled(true);
				btn_login.setEnabled(true);
				break;
			case 1002://start read card
				CustomDialog.showWaitDialog(mContext, "正在读卡信息...");
				new Thread(new Runnable() {
					public void run() {
						read_card();
					}
				}).start();
				break;
			case 1009:
				CustomDialog.dismissDialog();
				ToastUtil.showLongToast(mContext, "卡类型错误!");
				break;
			case 1008:
				CustomDialog.dismissDialog();
				ToastUtil.showLongToast(mContext, "卡校验失败!");
				break;
			case 1007:
				CustomDialog.dismissDialog();
				ToastUtil.showLongToast(mContext, "读卡失败!");
				break;
			default:
				break;
			}
		};
	};

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_login:
			login();
			break;
		case R.id.btn_read_card:
			mHandler.sendEmptyMessage(1002);
			break;
	
		default:
			break;

		}
	}
	
	

	private void read_card() {

		Log.e("wzb", "read_card");
		boolean ret = CardManager.SelectCPU_EF();
		Log.e("wzb", "ret:" + ret);

		if (ret) {
			int offset = 0;
			int default_len = 200;
			result = "";
			card_password="";
			int i;
			for (i = 0; i < 163; i++) {
				String hexstr = CardManager.read_card(offset, default_len);
				offset += default_len;
				LogUtil.logMessage("wzb", "rece:" + hexstr);

				result += hexstr;
			}
			String hexstr = CardManager.read_card(32599, 168);
			result += hexstr;
			LogUtil.logMessage("wzb", "all result:" + result);
			LogUtil.logMessage("wzb", "all result len:" + result.length());
			Thread nt = new Thread(ReadOperationCard);
			nt.start();
		} else {

			mHandler.sendEmptyMessage(1008);
		}
	}

	private void login() {
		String password=et_ps.getText().toString();
		if(TextUtils.isEmpty(password)){
			ToastUtil.showLongToast(mContext, "请输入密码");
			return;
		}
		
		if(password.equals(card_password)){
			Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			LoginActivity.this.startActivity(intent);
		}else{
			ToastUtil.showLongToast(mContext, "密码错误");
			return;
		}

		// finish();
	}

	private Thread ReadOperationCard = new Thread() {
		public void run() {
			String url = "http://58.251.74.101:6662/jmservice.asmx/ReadOperationCard";
			Map<String, String> params = new HashMap<String, String>();
			params.put("s", result);
			String res = HttpUtils.httpPostString(url, params);
			LogUtil.logMessage("wzb", "ReadOperationCard res:" + res);
			if (res == null) {

				mHandler.sendEmptyMessage(1007);
			} else {
				String result_data = res.substring(res.indexOf("{"), res.lastIndexOf("}") + 1);
				LogUtil.logMessage("wzb", "result:" + result_data);
				try {
					JSONObject result_json = new JSONObject(result_data);
					String operid = result_json.getString("operid");
					String operName = result_json.getString("operName");
					String pwd = result_json.getString("pwd");
					String susccess = result_json.getString("Success");
					LogUtil.logMessage("wzb", "operid=" + operid);
					LogUtil.logMessage("wzb", "operName=" + operName);
					LogUtil.logMessage("wzb", "pwd=" + pwd);
					LogUtil.logMessage("wzb", "susccess=" + susccess);
					if(susccess.equals("1")){
						Message message=mHandler.obtainMessage();
						message.what=1001;
						message.obj=operName;
						mHandler.sendMessage(message);
						card_password=pwd;
						WApplication.sp.set("operid", operid);
					}else{
						mHandler.sendEmptyMessage(1009);
					}
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					mHandler.sendEmptyMessage(1007);
				}
			}

		}
	};

}
