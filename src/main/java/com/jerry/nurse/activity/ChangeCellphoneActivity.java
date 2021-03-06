package com.jerry.nurse.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.jerry.nurse.R;
import com.jerry.nurse.constant.ServiceConstant;
import com.jerry.nurse.model.BindInfo;
import com.jerry.nurse.model.CommonResult;
import com.jerry.nurse.model.ShortMessage;
import com.jerry.nurse.model.ThirdPartInfo;
import com.jerry.nurse.net.FilterStringCallback;
import com.jerry.nurse.util.AccountValidatorUtil;
import com.jerry.nurse.util.L;
import com.jerry.nurse.util.StringUtil;
import com.jerry.nurse.util.T;
import com.jerry.nurse.view.TitleBar;
import com.zhy.http.okhttp.OkHttpUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.OnClick;
import okhttp3.MediaType;

import static com.jerry.nurse.activity.CountryActivity.EXTRA_COUNTRY_CODE;
import static com.jerry.nurse.activity.CountryActivity.EXTRA_COUNTRY_NAME;
import static com.jerry.nurse.activity.SignupActivity.DEFAULT_COUNTRY_CODE;
import static com.jerry.nurse.activity.SignupActivity.TYPE_BIND_CELLPHONE;
import static com.jerry.nurse.activity.SignupActivity.TYPE_CHANGE_CELLPHONE;
import static com.jerry.nurse.activity.SignupActivity.TYPE_UNBIND_CELLPHONE;
import static com.jerry.nurse.constant.ServiceConstant.RESPONSE_SUCCESS;

public class ChangeCellphoneActivity extends BaseActivity {

    private static final int REQUEST_CHANGE_CELLPHONE = 0x101;

    private static final int REQUEST_COUNTRY = 0x00000102;

    private static final String EXTRA_BIND_INFO = "extra_bind_info";

    @Bind(R.id.tv_country)
    TextView mCountryTextView;

    @Bind(R.id.tb_change_cellphone)
    TitleBar mTitleBar;

    @Bind(R.id.tv_origin_cellphone)
    TextView mOriginCellphoneTextView;

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

    @Bind(R.id.ll_content)
    LinearLayout mContentLayout;

    @BindColor(R.color.primary)
    int mPrimaryColor;

    @BindColor(R.color.gray_textColor)
    int mGrayColor;

    private BindInfo mBindInfo;

    private int mType;

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

    public static Intent getIntent(Context context, BindInfo bindInfo) {
        Intent intent = new Intent(context, ChangeCellphoneActivity.class);
        intent.putExtra(EXTRA_BIND_INFO, bindInfo);
        return intent;
    }

    @Override
    public int getContentViewResId() {
        return R.layout.activity_change_cellphone;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        mBindInfo = (BindInfo) getIntent().getSerializableExtra(EXTRA_BIND_INFO);

        String cellphone = mBindInfo.getPhone();

        L.i("获取到的值是：" + mBindInfo.toString());

        // 绑定数量是1，并且有手机号：就是修改手机号
        if (mBindInfo.getBindCount() == 1 && !TextUtils.isEmpty(cellphone)) {
            L.i("修改手机");
            mType = TYPE_CHANGE_CELLPHONE;
            if (cellphone.length() == 11) {
                mCellphoneTextView.setText(cellphone.substring(0, 2) + "*******" + cellphone.substring(9));
            }
            mGetVerificationCodeTextView.setEnabled(true);
        }
        // 绑定数量大于等于1，并且没有手机号：就是绑定手机
        else if (mBindInfo.getBindCount() >= 1 && TextUtils.isEmpty(cellphone)) {
            L.i("绑定手机号");
            mType = TYPE_BIND_CELLPHONE;
            mTitleBar.setTitle("绑定手机");
            mOriginCellphoneTextView.setText("手机号码");
            mCellphoneTextView.setVisibility(View.GONE);
            mNextButton.setText("绑定");
        }
        // 绑定数量大于1，手机不为空，就是解绑手机号
        else if (mBindInfo.getBindCount() > 1 && !TextUtils.isEmpty(cellphone)) {
            L.i("解绑手机号");
            mType = TYPE_UNBIND_CELLPHONE;
            mTitleBar.setTitle("解绑手机");
            mOriginCellphoneTextView.setText("手机号码");
            mCellphoneTextView.setVisibility(View.GONE);
            mNextButton.setText("解绑");
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
        if (!TextUtils.isEmpty(mBindInfo.getPhone())) {
            if (!mBindInfo.getPhone().equals(cellphone)) {
                T.showShort(this, R.string.cellphone_dismatch);
                return;
            }
        }

        // 读取国家代码
        String countryCode = DEFAULT_COUNTRY_CODE;
        if (mCountryTextView.getText().toString().split(" ").length == 2) {
            countryCode = mCountryTextView.getText().toString().split(" ")[1];
        }

        mProgressDialogManager.show();
        try {
            countryCode = URLEncoder.encode(countryCode, "UTF-8");
            OkHttpUtils.get().url(ServiceConstant.GET_VERIFICATION_CODE)
                    .addParams("Phone", cellphone)
                    .addParams("CountryCode", countryCode)
                    .addParams("Type", String.valueOf(mType))
                    .build()
                    .execute(new FilterStringCallback(mProgressDialogManager) {

                        @Override
                        public void onFilterResponse(String response, int id) {
                            CommonResult commonResult = new Gson().fromJson(response, CommonResult.class);
                            if (commonResult.getCode() == RESPONSE_SUCCESS) {
                                // 控制发送验证码的状态
                                mGetVerificationCodeTextView.setEnabled(false);
                                mGetVerificationCodeTextView.setTextColor(mGrayColor);
                                mGetVerificationCodeTextView.setText("(" + mValidateCountDown + "秒)");
                                mHandler.postDelayed(mValidateRunnable, 1000);
                                T.showLong(ChangeCellphoneActivity.this, R.string.get_verification_finished);
                            } else {
                                T.showLong(ChangeCellphoneActivity.this, commonResult.getMsg());
                            }
                        }
                    });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.acb_next)
    void onNext(View view) {

        // 本地验证用户名和密码格式是否符合
        String cellphone = mCellphoneEditText.getText().toString();
        String verificationCode = mVerificationCodeEditText.getText().toString();

        int result = SignupActivity.localValidate(cellphone, verificationCode);
        if (result != 0) {
            T.showLong(this, result);
            return;
        }

        // 远端服务验证
        validateVerificationCode(cellphone, verificationCode);
    }


    /**
     * 绑定/解绑 手机号
     *
     * @param cellphone
     * @param isBind
     */
    private void bind(final String cellphone, boolean isBind) {
        String url;
        if (isBind) {
            url = ServiceConstant.BIND;
        } else {
            url = ServiceConstant.UNBIND;
        }
        ThirdPartInfo thirdPartInfo = new ThirdPartInfo();
        thirdPartInfo.setRegisterId(mBindInfo.getRegisterId());
        thirdPartInfo.setPhone(cellphone);
        thirdPartInfo.setLoginType(ThirdPartInfo.TYPE_CELLPHONE);
        mProgressDialogManager.show();
        OkHttpUtils.postString()
                .url(url)
                .content(StringUtil.addModelWithJson(thirdPartInfo))
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .build()
                .execute(new FilterStringCallback(mProgressDialogManager) {

                    @Override
                    public void onFilterResponse(String response, int id) {
                        CommonResult commonResult = new Gson().fromJson(response, CommonResult.class);
                        if (commonResult.getCode() == RESPONSE_SUCCESS) {
                            T.showShort(ChangeCellphoneActivity.this, "操作成功");
                            finish();
                        } else {
                            L.i("操作失败");
                            T.showShort(ChangeCellphoneActivity.this, commonResult.getMsg());
                        }
                    }
                });
    }

    /**
     * 验证验证码服务
     *
     * @param cellphone
     * @param code
     */
    private void validateVerificationCode(final String cellphone, String code) {

        ShortMessage shortMessage = new ShortMessage(mBindInfo.getRegisterId(),
                cellphone, code, mType);

        OkHttpUtils.postString()
                .url(ServiceConstant.VALIDATE_VERIFICATION_CODE)
                .content(StringUtil.addModelWithJson(shortMessage))
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .build()
                .execute(new FilterStringCallback(mProgressDialogManager) {

                    @Override
                    public void onFilterResponse(String response, int id) {
                        CommonResult commonResult = new Gson().fromJson(response, CommonResult.class);
                        if (commonResult.getCode() == RESPONSE_SUCCESS) {
                            if (mType == TYPE_CHANGE_CELLPHONE) {
                                Intent intent = NewCellphoneActivity.getIntent(ChangeCellphoneActivity.this);
                                startActivityForResult(intent, REQUEST_CHANGE_CELLPHONE);
                            } else if (mType == TYPE_BIND_CELLPHONE) {
                                // 绑定手机
                                bind(cellphone, true);
                            } else if (mType == TYPE_UNBIND_CELLPHONE) {
                                // 解绑手机
                                bind(cellphone, false);
                            }
                        } else {
                            T.showShort(ChangeCellphoneActivity.this, commonResult.getMsg());
                        }
                    }
                });
    }

    /**
     * 国家代码点击事件
     *
     * @param view
     */
    @OnClick(R.id.rl_country)
    void onCountry(View view) {
        if (mCountryTextView.getText().toString().split(" ").length == 2) {
            String countryCode = mCountryTextView.getText().toString().split(" ")[1];
            Intent intent = CountryActivity.getIntent(this, countryCode);
            startActivityForResult(intent, REQUEST_COUNTRY);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CHANGE_CELLPHONE) {
                finish();
            } else if (requestCode == REQUEST_COUNTRY) {
                // 获取国家地区编号并显示
                Bundle bundle = data.getExtras();
                String countryName = bundle.getString(EXTRA_COUNTRY_NAME);
                String countryCode = bundle.getString(EXTRA_COUNTRY_CODE);
                mCountryTextView.setText(countryName + " " + countryCode);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mValidateRunnable);
    }
}
