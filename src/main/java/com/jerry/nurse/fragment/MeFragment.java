package com.jerry.nurse.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jerry.nurse.R;
import com.jerry.nurse.activity.HtmlActivity;
import com.jerry.nurse.activity.PersonalInfoActivity;
import com.jerry.nurse.activity.SettingActivity;
import com.jerry.nurse.constant.ServiceConstant;
import com.jerry.nurse.model.LoginInfo;
import com.jerry.nurse.net.FilterStringCallback;
import com.jerry.nurse.util.DensityUtil;
import com.jerry.nurse.util.ProgressDialogManager;
import com.jerry.nurse.view.ValidatedView;
import com.zhy.http.okhttp.OkHttpUtils;

import org.litepal.crud.DataSupport;

import butterknife.Bind;
import butterknife.OnClick;
import okhttp3.Call;

import static com.jerry.nurse.constant.ServiceConstant.AVATAR_ADDRESS;
import static com.jerry.nurse.constant.ServiceConstant.QR_CODE_ADDRESS;
import static com.jerry.nurse.constant.ServiceConstant.USER_COLON;
import static com.jerry.nurse.constant.ServiceConstant.USER_NAME;


/**
 * Created by Jerry on 2017/7/15.
 */

public class MeFragment extends BaseFragment {

    private static final String EVENT_REPORT_URL = "http://192.168.0.49:3300?Ruid=ru00000002&from=103";

    @Bind(R.id.civ_avatar)
    ImageView mAvatarImageView;

    @Bind(R.id.tv_name)
    TextView mNameTextView;

    @Bind(R.id.tv_nickname)
    TextView mNicknameTextView;

    @Bind(R.id.vv_valid)
    ValidatedView mValidatedView;

    private LoginInfo mLoginInfo;
    private ProgressDialogManager mProgressDialogManager;

    /**
     * 实例化方法
     *
     * @return
     */
    public static MeFragment newInstance() {
        MeFragment fragment = new MeFragment();
        return fragment;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_me;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        mProgressDialogManager = new ProgressDialogManager(getActivity());
    }

    @Override
    public void onStart() {
        super.onStart();
        // 初始化用户信息显示
        initUserInfo();
    }

    /**
     * 初始化用户信息显示
     */
    private void initUserInfo() {
        mLoginInfo = DataSupport.findFirst(LoginInfo.class);

        // 设置头像
        if (!TextUtils.isEmpty(mLoginInfo.getAvatar())) {
            if (mLoginInfo.getAvatar().startsWith("http")) {
                Glide.with(this).load(mLoginInfo.getAvatar()).into(mAvatarImageView);
            } else {
                Glide.with(this).load(AVATAR_ADDRESS + mLoginInfo.getAvatar()).into(mAvatarImageView);
            }
        }
        if (mLoginInfo.getName() != null) {
            mNameTextView.setText(mLoginInfo.getName());
        } else {
            mNameTextView.setVisibility(View.GONE);
        }
        if (mLoginInfo.getNickName() != null) {
            mNicknameTextView.setText(mLoginInfo.getNickName());
        } else {
            mNicknameTextView.setVisibility(View.GONE);
        }
        // 如果姓名和昵称都没有设置的情况就显示未设置
        if (mLoginInfo.getName() == null && mLoginInfo.getNickName() == null) {
            mNicknameTextView.setVisibility(View.VISIBLE);
            mNicknameTextView.setText("未设置");
            mNicknameTextView.setTextColor(getResources().getColor(R.color.gray_textColor));
        }

        // 判断权限，是否显示已认证
        if (OfficeFragment.checkPermission()) {
            mValidatedView.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.ll_personal_info)
    void onPersonalInfo(View view) {
        Intent intent = PersonalInfoActivity.getIntent(getActivity());
        startActivity(intent);
    }

    /**
     * 获取二维码
     *
     * @param view
     */
    @OnClick(R.id.iv_qr_code)
    void onQrCode(View view) {
        mProgressDialogManager.show();
        OkHttpUtils.get().url(ServiceConstant.GET_QR_CODE)
                .addParams("RegisterId", mLoginInfo.getRegisterId())
                .build()
                .execute(new FilterStringCallback(mProgressDialogManager) {

                    @Override
                    public void onFilterError(Call call, Exception e, int id) {
                    }

                    @Override
                    public void onFilterResponse(String response, int id) {
                        if (response.startsWith(USER_NAME)) {
                            String name = response.split(USER_COLON)[1];
                            showQrCode(name);
                        }
                    }
                });

    }

    /**
     * 显示二维码
     */
    private void showQrCode(String name) {
        String url = QR_CODE_ADDRESS + name;
        View v = getActivity().getLayoutInflater().inflate(R.layout.view_qr_code, null);
        ImageView qrCodeImageView = (ImageView) v.findViewById(R.id.iv_qr_code);
        Glide.with(this).load(url).into(qrCodeImageView);
        qrCodeImageView.setImageResource(R.drawable.erm);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        AlertDialog dialog = builder
                .setView(v)
                .setCancelable(true)
                .create();
        dialog.show();
        //设置窗口的大小
        dialog.getWindow().setLayout(DensityUtil.dp2px(getActivity(), 300),
                DensityUtil.dp2px(getActivity(), 340));

    }

    @OnClick(R.id.rl_event_report)
    void onEventReport(View view) {
        Intent intent = HtmlActivity.getIntent(getActivity(), EVENT_REPORT_URL, null);
        startActivity(intent);
    }

    @OnClick(R.id.rl_credit)
    void onCredit(View view) {

    }

    @OnClick(R.id.rl_collection)
    void onCollection(View view) {

    }

    @OnClick(R.id.rl_schedule)
    void onSchedule(View view) {

    }

    @OnClick(R.id.rl_exam)
    void onExam(View view) {

    }

    @OnClick(R.id.rl_setting)
    void onSetting(View view) {
        Intent intent = SettingActivity.getIntent(getActivity());
        startActivity(intent);
    }


}
