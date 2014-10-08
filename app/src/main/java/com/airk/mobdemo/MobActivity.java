package com.airk.mobdemo;

import android.app.Activity;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

/**
 * Created by kevin on 2014/10/8.
 */
@EActivity(R.layout.activity_mob)
public class MobActivity extends Activity {
    private final String TAG = MobActivity.class.getSimpleName();

    @ViewById(R.id.phone)
    EditText mPhoneNumber;
    @ViewById(R.id.verify)
    Button mVerify;
    @ViewById(R.id.code)
    EditText mCode;
    @ViewById(R.id.submit)
    Button mSubmit;
    @ViewById(R.id.result)
    TextView mResult;

    boolean isOk = false;
    MyHandler handler = new MyHandler();
    private int minute = 60;

    @AfterViews
    void initViews() {
        mVerify.setEnabled(false);
        mSubmit.setEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SMSSDK.registerEventHandler(handler);
        SMSSDK.getSupportedCountries();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SMSSDK.unregisterEventHandler(handler);
    }

    @UiThread
    void enableVerify() {
        mVerify.setEnabled(true);
    }

    @UiThread
    void verifying() {
        mVerify.setEnabled(false);
        mPhoneNumber.setEnabled(false);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                changeVerifyText("" + minute);
                minute--;
                if (minute <= 0) {
                    minute = 60;
                    resetVerify();
                    cancel();
                }
            }
        }, 0, 1000);
    }

    @UiThread
    void changeVerifyText(String str) {
        mVerify.setText(str);
    }

    @UiThread
    void resetVerify() {
        mVerify.setEnabled(true);
        mVerify.setText("Verify");
        mPhoneNumber.setEnabled(true);
    }

    @UiThread
    void enableSubmit() {
        mSubmit.setEnabled(true);
    }

    @UiThread
    void changeResult(boolean success) {
        mResult.setText(success ? "Verify success!" : "Verify failed!");
        mCode.setText("");
    }

    @Click(R.id.verify)
    void doVerify() {
        if (!isOk) {
            return;
        }
        if (TextUtils.isEmpty(mPhoneNumber.getText())) {
            return;
        }
        SMSSDK.getVerificationCode("86", mPhoneNumber.getText().toString());
    }

    @Click(R.id.submit)
    void doSubmit() {
        if (TextUtils.isEmpty(mCode.getText())) {
            return;
        }
        SMSSDK.submitVerificationCode("86", mPhoneNumber.getText().toString(),
                mCode.getText().toString());
    }

    private class MyHandler extends EventHandler {
        @Override
        public void onRegister() {
            super.onRegister();
        }

        @Override
        public void beforeEvent(int event, Object data) {
            super.beforeEvent(event, data);
        }

        @Override
        public void afterEvent(int event, int result, Object data) {
            if (result == SMSSDK.RESULT_COMPLETE) {
                if (event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES) {
                    // 返回支持发送验证码的国家列表
                    // ArrayList<HashMap<String, Object>>
                    ArrayList<HashMap<String, Object>> contries =
                            (ArrayList<HashMap<String, Object>>) data;
                    for (HashMap<String, Object> country : contries) {
                        String zone = (String) country.get("zone");
                        String rule = (String) country.get("rule");

                        if (zone.equals("86")) {
                            isOk = true;
                            break;
                        }
                    }
                    enableVerify();
                } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                    // 请求发送验证码，无返回
                    // null
                    enableSubmit();
                    verifying();
                } else if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                    // 校验验证码，返回校验的手机和国家代码
                    // HashMap<String, Object>
                    changeResult(true);
                } else if (event == SMSSDK.EVENT_GET_CONTACTS) {
                    // 获取手机内部的通信录列表
                    // ArrayList<HashMap<String, Object>>
                } else if (event == SMSSDK.EVENT_SUBMIT_USER_INFO) {
                    // 提交应用内的用户资料
                    // null
                } else if (event == SMSSDK.EVENT_GET_FRIENDS_IN_APP) {
                    // 获取手机通信录在当前应用内的用户列表
                    // ArrayList<HashMap<String, Object>>
                } else if (event == SMSSDK.EVENT_GET_NEW_FRIENDS_COUNT) {
                    // 获取手机通信录在当前应用内的新用户个数
                    // Integer
                }
            } else if (result == SMSSDK.RESULT_ERROR) {
                ((Throwable) data).printStackTrace();
                changeResult(false);
            }
            super.afterEvent(event, result, data);
        }

        @Override
        public void onUnregister() {
            super.onUnregister();
        }
    }

}
