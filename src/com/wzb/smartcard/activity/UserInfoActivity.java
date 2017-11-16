package com.wzb.smartcard.activity;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.wzb.smartcard.R;
import com.wzb.smartcard.util.CardManager;
import com.wzb.smartcard.util.CustomDialog;
import com.wzb.smartcard.util.LogUtil;
import com.wzb.smartcard.util.ToastUtil;

import android.app.Application.OnProvideAssistDataListener;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import cn.trinea.android.common.util.FileUtils;
import cn.trinea.android.common.util.HttpUtils;

/**
 * @author wzb<wangzhibin_x@qq.com>
 * @date Nov 4, 2017 9:13:35 PM
 */
public class UserInfoActivity extends BaseActivity {

	private TextView titleView;
	private ImageView backView;
	private Button btn_read_userinfo, btn_deposit;
	private TextView tv_userinfo;
	private String result = "";
	private Context mContext;
	private String debt_amount="";
	private String amount_details="";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_userinfo);
		mContext = UserInfoActivity.this;
		initTitleView();
		initView();
	}

	private void initView() {
		btn_read_userinfo = (Button) findViewById(R.id.btn_read_userinfo);
		btn_read_userinfo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// read_userinfo();
				mHandler.sendEmptyMessage(1002);
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
				intent.putExtra("debt_amount", debt_amount);
				intent.putExtra("debt_details", amount_details);
				startActivity(intent);
			}
		});
		
		btn_deposit.setEnabled(false);

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

	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1001:// read userinfo ok
				CustomDialog.dismissDialog();
				String userinfo=(String)msg.obj;
				tv_userinfo.setText(userinfo);
				btn_deposit.setEnabled(true);
				break;

			case 1002:// start read info
				CustomDialog.showWaitDialog(mContext, "正在读取用户信息...");
				new Thread(new Runnable() {
					public void run() {
						read_userinfo();
					}
				}).start();
				break;
			case 1009:
				CustomDialog.dismissDialog();
				ToastUtil.showLongToast(mContext, "卡类型错误!");
				break;
			case 1008:
				CustomDialog.dismissDialog();
				ToastUtil.showLongToast(mContext, "请检测卡是否正常插入!");
				break;
			default:
				break;
			}
		};

	};

	private void read_userinfo() {
		Log.e("wzb", "read_userinfo");
		boolean ret = CardManager.SelectCPU_EF();
		Log.e("wzb", "ret:" + ret);

		if (ret) {
			int offset = 0;
			int default_len = 200;
			result = "";
			int i;
			for (i = 0; i < 163; i++) {
				String hexstr = CardManager.read_card(offset, default_len);
				offset += default_len;
				LogUtil.logMessage("wzb", "rece:" + hexstr);

				result += hexstr.trim();
			}
			String hexstr = CardManager.read_card(32599, 168);
			result += hexstr.trim();

			LogUtil.logMessage("wzb", "all result:" + result);
			LogUtil.logMessage("wzb", "all result len:" + result.length());
			// boolean status=FileUtils.writeFile("/sdcard/2.txt", result);
			// LogUtil.logMessage("wzb", "write to file:"+status);
			Thread nt = new Thread(UserInfo);
			nt.start();
		}else{
			mHandler.sendEmptyMessage(1008);
		}
	}

	private Thread UserInfo = new Thread() {
		public void run() {
			String url = "http://58.251.74.101:6662/jmservice.asmx/UserInfo";
			Map<String, String> params = new HashMap<String, String>();
			params.put("s", result);
			String res = HttpUtils.httpPostString(url, params);
			LogUtil.logMessage("wzb", "UserInfo res:" + res);
			if (res == null) {
				CustomDialog.dismissDialog();
				ToastUtil.showLongToast(mContext, "读卡失败!");
			} else {
				String result_data = res.substring(res.indexOf("{"), res.lastIndexOf("}") + 1);
				LogUtil.logMessage("wzb", "result:" + result_data);
				try {
					JSONObject result_json = new JSONObject(result_data);
					String susccess = result_json.getString("Success");
					if (susccess.equals("1")) {
						String CustomerId = result_json.getString("CustomerId");
						String CustomerName = result_json.getString("CustomerName");
						String NationalId = result_json.getString("NationalId");

						String MobilePhone = result_json.getString("MobilePhone");
						String DebtAmount = result_json.getString("DebtAmount");
						String DebtDetails = result_json.getString("DebtDetails");
						LogUtil.logMessage("wzb", "CustomerId=" + CustomerId);
						LogUtil.logMessage("wzb", "operName=" + CustomerName);
						LogUtil.logMessage("wzb", "pwd=" + NationalId);
						LogUtil.logMessage("wzb", "susccess=" + susccess);
						LogUtil.logMessage("wzb", "MobilePhone=" + MobilePhone);
						String show_info="ID:"+CustomerId+"\n"
								+"\n"
								+"Name:"+CustomerName+"\n"
								+"\n"
								+"MobliePhone:"+MobilePhone+"\n"
								+"\n"
								+"DebtAmount:"+DebtAmount+"\n";
						
						debt_amount=DebtAmount;
						amount_details=DebtDetails;
						Message message=mHandler.obtainMessage();
						message.what=1001;
						message.obj=show_info;
						
						mHandler.sendMessage(message);
								
					}else{
						mHandler.sendEmptyMessage(1009);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	};

}
