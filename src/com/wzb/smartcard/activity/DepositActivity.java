package com.wzb.smartcard.activity;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wzb.smartcard.R;
import com.wzb.smartcard.interf.WApplication;
import com.wzb.smartcard.print.PrintBillService;
import com.wzb.smartcard.util.CardManager;
import com.wzb.smartcard.util.CustomDialog;
import com.wzb.smartcard.util.LogUtil;
import com.wzb.smartcard.util.ToastUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import cn.trinea.android.common.util.HttpUtils;
import cn.trinea.android.common.util.TimeUtils;

/**
 * @author wzb<wangzhibin_x@qq.com>
 * @date Nov 4, 2017 9:26:17 PM
 */
public class DepositActivity extends BaseActivity {
	private TextView titleView;
	private ImageView backView;
	private Context mContext;

	private EditText et_debt_amount, et_pay_amount;
	private Button btn_debt_details, btn_deposit;
	private TextView tv_actual_pay;

	private String debt_amount = "";
	private String debt_details = "";
	private String sp = "  :  ";
	MyTextWathcer myTextWathcer;
	private String result = "";
	private float f_deposit_num = 0.0f;
	private float f_deposit_num_result = 0.0f;
	private String hexstr = "";
	private String deposit_num="";
	private String show_details_info="";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_deposit);
		mContext = DepositActivity.this;
		Intent intent = getIntent();
		debt_amount = intent.getStringExtra("debt_amount");
		debt_details = intent.getStringExtra("debt_details");
		initTitleView();
		initView();
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

	private void initView() {
		et_debt_amount = (EditText) findViewById(R.id.et_debt_amount);
		et_debt_amount.setEnabled(false);
		et_debt_amount.setText(debt_amount);

		et_pay_amount = (EditText) findViewById(R.id.et_payment_amount);
		myTextWathcer = new MyTextWathcer();
		et_pay_amount.addTextChangedListener(myTextWathcer);

		btn_debt_details = (Button) findViewById(R.id.btn_debt_details);
		btn_debt_details.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				show_debt_details();
			}
		});

		btn_deposit = (Button) findViewById(R.id.btn_deposit_confirm);
		btn_deposit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				deposit();
			}
		});

		tv_actual_pay = (TextView) findViewById(R.id.tv_actual_pay);
	}

	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1005:
				String num = (String) msg.obj;
				refresh_actual_pay(num);
				break;
			case 1006:
				tv_actual_pay.setText("");
				break;
			case 1001:// write card
				hexstr = (String) msg.obj;
				new Thread(new Runnable() {
					public void run() {
						write_card();
					}
				}).start();
				break;
				
			case 1007:
				CustomDialog.dismissDialog();
				ToastUtil.showLongToast(mContext, "读卡失败!");
				break;
			case 1008:
				CustomDialog.dismissDialog();
				ToastUtil.showLongToast(mContext, "卡校验失败!");
				break;
			case 1100:

				CustomDialog.dismissDialog();
				ToastUtil.showLongToast(mContext, "充值失败!");
				break;
			case 1101:

				CustomDialog.dismissDialog();
				ToastUtil.showLongToast(mContext, "充值失败!");
				break;
			case 1102:
				CustomDialog.dismissDialog();
				ToastUtil.showLongToast(mContext, "充值成功!");
				Thread nt = new Thread(WriteCardResp);
				nt.start();
				show_print();
				break;
			case 1103:
				CustomDialog.dismissDialog();
				ToastUtil.showLongToast(mContext, "获取充值信息失败!");
				break;
			default:
				break;
			}
		};
	};
	
	private BroadcastReceiver mPrtReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            int ret = intent.getIntExtra("ret", 0);
            CustomDialog.dismissDialog();
            if(ret == -1){
            	ToastUtil.showLongToast(mContext, "缺纸");
            }
        }
    };
    
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    	register_broadcast();
    }
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		unregisterReceiver(mPrtReceiver);
	}
	
	private void show_print(){
		CustomDialog.showOkAndCalcelDialog(mContext, "打印小票", "充值完成\n\n是否打印详细信息?\n", false, new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//print
				CustomDialog.dismissDialog();
				CustomDialog.showWaitDialog(mContext,"打印中...");
				print();
			}
		}, new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//exit
				CustomDialog.dismissDialog();
				finish();
			}
		},"打印","不打印");
	}
	
	private void print(){
		String msg="";
		String time="日期: "+TimeUtils.getCurrentTimeInString();
		String user_name="用户名: "+WApplication.sp.get("cname", "UNKNOW");
		StringBuilder sb_phone=new StringBuilder(WApplication.sp.get("cphone", "00000000000000"));
		sb_phone.replace(4, 10, "******");
		String phone_number="电话: "+sb_phone.toString();
		StringBuilder sb_cid=new StringBuilder(WApplication.sp.get("cid", "00000000000000"));
		sb_cid.replace(4, 10, "******");
		String icard_number="身份证号码: "+sb_cid.toString();
		String operid="操作员 ID: "+WApplication.sp.get("operid", "00000000000000");
		String sp="\n";
		String pay="付款金额: "+f_deposit_num+"元"+"    "+"债务金额: "+debt_amount+"元";
		String debtdetails="债务明细:";
		String result="净充值金额:"+f_deposit_num_result+"元";
		msg=time+sp+user_name+sp+icard_number+sp+phone_number+sp+operid+sp+sp+sp+
				pay+sp+debtdetails+sp+show_details_info+sp+sp+result+sp+sp;
		LogUtil.logMessage("wzb", "print msg:"+msg);
		Intent intentService = new Intent(mContext, PrintBillService.class);
        intentService.putExtra("SPRT", msg);
        startService(intentService);
	}
	
	private void register_broadcast(){
		IntentFilter filter = new IntentFilter();
        filter.addAction("android.prnt.message");
        registerReceiver(mPrtReceiver, filter);
	}

	private void write_card() {
		boolean ret = CardManager.SelectCPU_EF();
		LogUtil.logMessage("wzb", "write_card ret:" + ret);
		if (ret) {
			//test//
//			if(true){
//				byte test[]={(byte)0x00,(byte)0xD6,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x08};
//				CardManager.write_card(0, 1, "08");
//				return;
//			}
			//end
			int offset = 0;
			int default_len = 180;
			int hexstr_offset=0;
			int i;
			int all_write_data_len = hexstr.length()/2;
			for (i = 0; i < all_write_data_len / default_len; i++) {
				String status = CardManager.write_card(offset, default_len,
						hexstr.substring(hexstr_offset, hexstr_offset + default_len*2));
				offset += default_len;
				hexstr_offset=offset*2;
				if (!status.equals("6982")) {
					LogUtil.logMessage("wzb", "write card err");
					mHandler.sendEmptyMessage(1100);
					break;
				}
			}
			int other = all_write_data_len % default_len;
			if (other > 0) {
				if ((other + offset) == all_write_data_len) {
					String status = CardManager.write_card(offset, other, hexstr.substring(hexstr_offset, hexstr_offset + other*2));
					if (!status.equals("6982")) {
						LogUtil.logMessage("wzb", "11 write card err");
						mHandler.sendEmptyMessage(1101);
					} else {
						LogUtil.logMessage("wzb", "write card completed");

						mHandler.sendEmptyMessage(1102);
					}
				}
			}
		}else{
			mHandler.sendEmptyMessage(1008);
		}
	}

	private class MyTextWathcer implements TextWatcher {

		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub
			try {
				String num = et_pay_amount.getText().toString();
				if (!TextUtils.isEmpty(num)) {
					Message msg = new Message();
					msg.what = 1005;
					msg.obj = num;
					mHandler.sendMessage(msg);
				} else {
					mHandler.sendEmptyMessage(1006);
				}

			} catch (Exception e) {
				// TODO: handle exception
			}

		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			// TODO Auto-generated method stub

		}

	}

	private void refresh_actual_pay(String num) {
		LogUtil.logMessage("wzb", "pay num:" + num);
		float actual_num = Float.parseFloat(num) - Float.parseFloat(debt_amount);
		tv_actual_pay.setText("实际充值" + actual_num + "元");
	}

	private void show_debt_details() {
		JSONArray jsonArray = null;
		try {
			jsonArray = new JSONArray(debt_details);
			show_details_info = "DebtName" + sp + "DebtPrice" + "\n" + "\n";
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String debt_name = jsonObject.getString("DebtName");
				String debt_price = jsonObject.getString("DebtPrice");
				show_details_info += debt_name + sp + debt_price + "\n" + "\n";
			}
			LogUtil.logMessage("wzb", "details:" + show_details_info);
			CustomDialog.showOkAndCalcelDialog(mContext, "DebtDetails", show_details_info, true, new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					CustomDialog.dismissDialog();
				}
			}, null);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void deposit() {
		deposit_num = et_pay_amount.getText().toString();
	
		if (TextUtils.isEmpty(deposit_num)) {
			ToastUtil.showLongToast(mContext, "充值金额不能为空");
			return;
		}

		f_deposit_num = Float.parseFloat(deposit_num);
		float f_debt_amount = Float.parseFloat(debt_amount);
		f_deposit_num_result=f_deposit_num-f_debt_amount;
		if (f_deposit_num_result < 0) {
			ToastUtil.showLongToast(mContext, "实际充值金额不能为负数");
			return;
		}
		CustomDialog.showWaitDialog(mContext, "正在充值中...");
		Thread nt = new Thread(WriteCard);
		nt.start();

	}

	private Thread WriteCard = new Thread() {
		public void run() {
			String url = "http://58.251.74.101:6662/jmservice.asmx/WriteCard";
			Map<String, String> params = new HashMap<String, String>();
			params.put("cid", WApplication.sp.get("cid", "0"));
			params.put("operId", WApplication.sp.get("operid", "0"));
			params.put("amount", String.valueOf(f_deposit_num));
			String res = HttpUtils.httpPostString(url, params);
			LogUtil.logMessage("wzb", "WriteCard res:" + res);
			if (res == null) {

				mHandler.sendEmptyMessage(1103);
			} else {
				String result_data = res.substring(res.indexOf(">", 40) + 1, res.lastIndexOf("<"));
				LogUtil.logMessage("wzb", "result:" + result_data);
				LogUtil.logMessage("wzb", "result len:" + result_data.length());
				if (TextUtils.isEmpty(result_data)) {

					mHandler.sendEmptyMessage(1103);
				} else {
					Message message = mHandler.obtainMessage();
					message.what = 1001;
					message.obj = result_data;
					mHandler.sendMessage(message);
				}
			}

		}
	};

	private Thread WriteCardResp = new Thread() {
		public void run() {
			String url = "http://58.251.74.101:6662/jmservice.asmx/WriteCardResp";
			Map<String, String> params = new HashMap<String, String>();
			params.put("cid", WApplication.sp.get("cid", "0"));
			params.put("operId", WApplication.sp.get("operid", "0"));
			params.put("amount", String.valueOf(f_deposit_num));
			String res = HttpUtils.httpPostString(url, params);
			LogUtil.logMessage("wzb", "WriteCard res:" + res);

		}
	};

}
