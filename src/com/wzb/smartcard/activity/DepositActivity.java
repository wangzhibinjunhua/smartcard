package com.wzb.smartcard.activity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wzb.smartcard.R;
import com.wzb.smartcard.util.CustomDialog;
import com.wzb.smartcard.util.LogUtil;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author wzb<wangzhibin_x@qq.com>
 * @date Nov 4, 2017 9:26:17 PM	
 */
public class DepositActivity extends BaseActivity{
	private TextView titleView;
	private ImageView backView;
	private Context mContext;
	
	private EditText et_debt_amount,et_pay_amount;
	private Button btn_debt_details,btn_deposit;
	private TextView tv_actual_pay;
	
	private String debt_amount="";
	private String debt_details="";
	private String sp="  :  ";
	MyTextWathcer myTextWathcer; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_deposit);
		mContext=DepositActivity.this;
		Intent intent=getIntent();
		debt_amount=intent.getStringExtra("debt_amount");
		debt_details=intent.getStringExtra("debt_details");
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
	
	private void initView(){
		et_debt_amount=(EditText)findViewById(R.id.et_debt_amount);
		et_debt_amount.setEnabled(false);
		et_debt_amount.setText(debt_amount);
		
		et_pay_amount=(EditText)findViewById(R.id.et_payment_amount);
		myTextWathcer=new MyTextWathcer();
		et_pay_amount.addTextChangedListener(myTextWathcer);
		
		btn_debt_details=(Button)findViewById(R.id.btn_debt_details);
		btn_debt_details.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				show_debt_details();
			}
		});
		
		btn_deposit=(Button)findViewById(R.id.btn_deposit_confirm);
		btn_deposit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				deposit();
			}
		});
		
		tv_actual_pay=(TextView)findViewById(R.id.tv_actual_pay);
	}
	
	Handler mHandler=new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1005:
				String num=(String)msg.obj;
				refresh_actual_pay(num);
				break;
			case 1006:
				tv_actual_pay.setText("");
				break;
			default:
				break;
			}
		};
	};
	
	private class MyTextWathcer implements TextWatcher{

		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub
			try {
				String num=et_pay_amount.getText().toString();
				if(!TextUtils.isEmpty(num)){
					Message msg=new Message();
					msg.what=1005;
					msg.obj=num;
					mHandler.sendMessage(msg);
				}else{
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
	
	private void refresh_actual_pay(String num){
		LogUtil.logMessage("wzb", "pay num:"+num);
		float actual_num=Float.parseFloat(num)-Float.parseFloat(debt_amount);
		tv_actual_pay.setText("实际充值"+actual_num+"元");
	}
	
	private void show_debt_details(){
		JSONArray jsonArray=null;
		try {
			jsonArray=new JSONArray(debt_details);
			String show_info="DebtName"+sp+"DebtPrice"+"\n"+"\n";
			for(int i=0;i<jsonArray.length();i++){
				JSONObject jsonObject=jsonArray.getJSONObject(i);
				String debt_name=jsonObject.getString("DebtName");
				String debt_price=jsonObject.getString("DebtPrice");
				show_info+=debt_name+sp+debt_price+"\n"+"\n";
			}
			LogUtil.logMessage("wzb", "details:"+show_info);
			CustomDialog.showOkAndCalcelDialog(mContext, "DebtDetails",show_info,true, new OnClickListener() {
				
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
	
	private void deposit(){
		
	}

}
