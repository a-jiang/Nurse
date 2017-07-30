package com.jerry.nurse.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.jerry.nurse.R;
import com.jerry.nurse.constant.ServiceConstant;
import com.jerry.nurse.model.ShortMessage;
import com.jerry.nurse.model.UserRegisterInfo;
import com.jerry.nurse.net.FilterStringCallback;
import com.jerry.nurse.util.AccountValidatorUtil;
import com.jerry.nurse.util.L;
import com.jerry.nurse.util.StringUtil;
import com.jerry.nurse.util.T;
import com.zhy.http.okhttp.OkHttpUtils;

import org.litepal.crud.DataSupport;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.MediaType;

import static com.jerry.nurse.constant.ServiceConstant.EASE_MOB_PASSWORD;
import static com.jerry.nurse.constant.ServiceConstant.REQUEST_SUCCESS;
import static com.jerry.nurse.constant.ServiceConstant.USER_COLON;

public class ChangeCellphoneActivity extends BaseActivity {

    private static final int REQUEST_CHANGE_CELLPHONE = 0x00000101;

    @Bind(R.id.cet_cellphone)
    EditText mCellphoneEditText;

    @Bind(R.id.et_verification_code)
    EditText mVerificationCodeEditText;

    @Bind(R.id.tv_send_verification_code)
    TextView mGetVerificationCodeTextView;

    @Bind(R.id.tv_cellphone)
    TextView mCellphoneTextView;

    @Bind(R.id.acb_next)
    AppCompatButton mNextButton;

    @BindColor(R.color.primary)
    int mPrimaryColor;

    @BindColor(R.color.gray_textColor)
    int mGrayColor;

    private UserRegisterInfo mUserRegisterInfo;

    /**
     * 验证码获取间隔
     */
    private int mValidateCountDown = 60;

    private Runnable mValidateRunnable = new Runnable() {
        @Override
        public void run() {
            // 验证码倒计时
            mValidateCountDown--;
            if (mValidateCountDown == 0) {
                mGetVerificationCodeTextView.setText(R.string.send_verification_code);
                mValidateCountDown = 60;
                mGetVerificationCodeTextView.setEnabled(true);
                mGetVerificationCodeTextView.setTextColor(mPrimaryColor);
                mHandler.removeCallbacks(mValidateRunnable);
            } else {
                mGetVerificationCodeTextView.setText("(" + mValidateCountDown + "秒)");
                mHandler.postDelayed(mValidateRunnable, 1000);
            }
        }
    };

    private Handler mHandler = new Handler();

    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context, ChangeCellphoneActivity.class);
        return intent;
    }

    @Override
    public int getContentViewResId() {
        return R.layout.activity_change_cellphone;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        mUserRegisterInfo = DataSupport.findFirst(UserRegisterInfo.class);
        String cellphone = mUserRegisterInfo.getPhone();
        if (cellphone == null) {
            mCellphoneTextView.setText(R.string.cellphone_not_exist);
            mGetVerificationCodeTextView.setEnabled(false);
            mNextButton.setEnabled(false);
        } else {
            mCellphoneTextView.setText(cellphone.substring(0, 3) + "XXXX" + cellphone.substring(7));
            mGetVerificationCodeTextView.setEnabled(true);
            mNextButton.setEnabled(true);
        }
    }

    @OnClick(R.id.tv_send_verification_code)
    void onSendVerificationCode(View view) {
        // 首先验证手机号是否为空和是否合法
        String cellphone = mCellphoneEditText.getText().toString();
        if (cellphone.isEmpty()) {
            T.showLong(this, R.string.cellphone_empty);
            return;
        }
        if (!AccountValidatorUtil.isMobile(cellphone)) {
            T.showLong(this, R.string.cellphone_invalid);
            return;
        }
        if (!mUserRegisterInfo.getPhone().equals(cellphone)) {
            T.showShort(this, R.string.cellphone_dismatch);
            return;
        }

        mProgressDialog.setMessage("发送验证码...");
        mProgressDialog.show();
        OkHttpUtils.get().url(ServiceConstant.GET_VERIFICATION_CODE)
                .addParams("Phone", cellphone)
                .build()
                .execute(new FilterStringCallback() {

                    @Override
                    public void onFilterError(Call call, Exception e, int id) {
                        mProgressDialog.dismiss();
                    }

                    @Override
                    public void onFilterResponse(String response, int id) {
                        mProgressDialog.dismiss();
                        if (response.equals(ServiceConstant.REQUEST_SUCCESS)) {
                            // 控制发送验证码的状态
                            mGetVerificationCodeTextView.setEnabled(false);
                            mGetVerificationCodeTextView.setTextColor(mGrayColor);
                            mGetVerificationCodeTextView.setText("(" + mValidateCountDown + "秒)");
                            mHandler.postDelayed(mValidateRunnable, 1000);
                            T.showLong(ChangeCellphoneActivity.this, R.string.get_verification_finished);
                        } else {
                            T.showLong(ChangeCellphoneActivity.this, R.string.get_verification_code_failed);
                        }
                    }
                });
    }

    @OnClick(R.id.acb_next)
    void onNext(View view) {
        // 本地验证用户名和密码格式是否符合
        String cellphone = mCellphoneEditText.getText().toString();
        String verificationCode = mVerificationCodeEditText.getText().toString();

        String errorMessage = SignupActivity.localValidate(cellphone, verificationCode);
        if (errorMessage != null) {
            T.showLong(this, errorMessage);
            return;
        }

        mNextButton.setEnabled(false);

        // 远端服务验证
        validateVerificationCode(cellphone, verificationCode);
    }

    /**
     * 验证验证码服务
     *
     * @param cellphone
     * @param code
     */
    private void validateVerificationCode(final String cellphone, String code) {
        ShortMessage shortMessage = new ShortMessage(
                mUserRegisterInfo.getRegisterId(), cellphone, code, 0);
        OkHttpUtils.postString()
                .url(ServiceConstant.VALIDATE_VERIFICATION_CODE)
                .content(StringUtil.addModelWithJson(shortMessage))
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .build()
                .execute(new FilterStringCallback() {
                    @Override
                    public void onFilterError(Call call, Exception e, int id) {
                        mNextButton.setEnabled(true);
                    }

                    @Override
                    public void onFilterResponse(String response, int id) {
                        if (response.equals(REQUEST_SUCCESS)) {
                            Intent intent = NewCellphoneActivity.getIntent(ChangeCellphoneActivity.this);
                            startActivityForResult(intent, REQUEST_CHANGE_CELLPHONE);
                        } else {
                            T.showShort(ChangeCellphoneActivity.this, R.string.validate_failed);
                        }
                        mNextButton.setEnabled(true);
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CHANGE_CELLPHONE) {
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mValidateRunnable);
    }
}
