package com.jerry.nurse.activity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.jerry.nurse.R;
import com.jerry.nurse.constant.ServiceConstant;
import com.jerry.nurse.listener.PhotoSelectListener;
import com.jerry.nurse.model.UserBasicInfo;
import com.jerry.nurse.model.UserCertificateInfo;
import com.jerry.nurse.model.UserHospitalInfo;
import com.jerry.nurse.model.UserRegisterInfo;
import com.jerry.nurse.net.FilterStringCallback;
import com.jerry.nurse.util.DateUtil;
import com.jerry.nurse.util.L;
import com.jerry.nurse.util.SPUtil;
import com.jerry.nurse.util.StringUtil;
import com.jerry.nurse.util.T;
import com.jerry.nurse.util.UserUtil;
import com.jerry.nurse.view.CircleImageView;
import com.zhy.http.okhttp.OkHttpUtils;

import org.litepal.crud.DataSupport;

import java.util.Calendar;
import java.util.Date;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.MediaType;

import static com.jerry.nurse.constant.ServiceConstant.REQUEST_SUCCESS;

public class PersonalInfoActivity extends BaseActivity {

    public static final int REQUEST_REGISTER_INFO = 0x00000101;
    public static final int REQUEST_BASIC_INFO = 0x00000102;
    public static final int REQUEST_CERTIFICATE_INFO = 0x00000103;
    public static final int REQUEST_HOSPITAL_INFO = 0x00000104;

    @Bind(R.id.civ_avatar)
    CircleImageView mAvatarView;

    @Bind(R.id.rl_avatar)
    RelativeLayout mAvatarLayout;

    @Bind(R.id.tv_name)
    TextView mNameTextView;

    @Bind(R.id.tv_nickname)
    TextView mNicknameTextView;

    @Bind(R.id.tv_sex)
    TextView mSexTextView;

    @Bind(R.id.tv_birthday)
    TextView mBirthdayTextView;

    @Bind(R.id.tv_certificate)
    TextView mCertificateTextView;

    @Bind(R.id.tv_practising_certificate)
    TextView mPractisingCertificateTextView;

    @Bind(R.id.tv_nursing_age)
    TextView mNursingAgeTextView;

    @Bind(R.id.tv_hospital)
    TextView mHospitalTextView;

    @Bind(R.id.tv_office)
    TextView mOfficeTextView;

    @Bind(R.id.tv_job_number)
    TextView mJobNumberTextView;

    @BindString(R.string.change_avatar)
    String mStringChangeAvatar;

    @BindString(R.string.male)
    String mStringMale;

    @BindString(R.string.female)
    String mStringFemale;

    private UserRegisterInfo mRegisterInfo;
    private UserBasicInfo mBasicInfo;
    private UserCertificateInfo mCertificateInfo;
    private UserHospitalInfo mHospitalInfo;

    private String mRegisterId;

    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context, PersonalInfoActivity.class);
        return intent;
    }

    @Override
    public int getContentViewResId() {
        return R.layout.activity_personal_info;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        // 设置图片
        setPhotoSelectListener(mAvatarLayout, new PhotoSelectListener() {
            @Override
            public void onPhotoSelected(Bitmap bitmap) {
                mAvatarView.setImageBitmap(bitmap);
            }
        });
        mRegisterId = (String) SPUtil.get(this, SPUtil.REGISTER_ID, "");

        // 读取用户注册信息
        updateRegisterInfo();

        // 获取用户各类信息
        getBasicInfo(mRegisterId);
        getCertificateInfo(mRegisterId);
        getHospitalInfo(mRegisterId);
    }

    /**
     * 获取用户基本信息
     */
    private void getBasicInfo(final String registerId) {
        mProgressDialog.show();
        OkHttpUtils.get().url(ServiceConstant.GET_USER_BASIC_INFO)
                .addParams("RegisterId", registerId)
                .build()
                .execute(new FilterStringCallback() {

                    @Override
                    public void onFilterError(Call call, Exception e, int id) {
                        mProgressDialog.dismiss();
                    }

                    @Override
                    public void onFilterResponse(String response, int id) {
                        mProgressDialog.dismiss();
                        try {
                            mBasicInfo = new Gson().fromJson(response, UserBasicInfo.class);
                            if (mBasicInfo != null) {
                                // 更新个人基本信息
                                UserUtil.saveBasicInfo(mBasicInfo);
                                updateBasicInfo();
                            }
                        } catch (JsonSyntaxException e) {
                            L.i("获取基本信息失败");
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * 获取用户执业证信息
     */
    private void getCertificateInfo(final String registerId) {
        mProgressDialog.show();
        OkHttpUtils.get().url(ServiceConstant.GET_USER_CERTIFICATE_INFO)
                .addParams("RegisterId", registerId)
                .build()
                .execute(new FilterStringCallback() {

                    @Override
                    public void onFilterError(Call call, Exception e, int id) {
                        mProgressDialog.dismiss();
                    }

                    @Override
                    public void onFilterResponse(String response, int id) {
                        mProgressDialog.dismiss();
                        try {
                            mCertificateInfo = new Gson().fromJson(response, UserCertificateInfo.class);
                            if (mCertificateInfo != null) {
                                // 更新个人执业证信息
                                UserUtil.saveCertificateInfo(mCertificateInfo);
                                updateCertificateInfo();
                            }
                        } catch (JsonSyntaxException e) {
                            L.i("获取执业证信息失败");
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * 获取用户医院信息
     */
    private void getHospitalInfo(final String registerId) {
        mProgressDialog.show();
        OkHttpUtils.get().url(ServiceConstant.GET_USER_HOSPITAL_INFO)
                .addParams("RegisterId", registerId)
                .build()
                .execute(new FilterStringCallback() {

                    @Override
                    public void onFilterError(Call call, Exception e, int id) {
                        mProgressDialog.dismiss();
                    }

                    @Override
                    public void onFilterResponse(String response, int id) {
                        mProgressDialog.dismiss();
                        try {
                            mHospitalInfo = new Gson().fromJson(response, UserHospitalInfo.class);
                            if (mHospitalInfo != null) {
                                // 更新个人医院信息
                                UserUtil.saveHospitalInfo(mHospitalInfo);
                                updateHospitalInfo();
                            }
                        } catch (JsonSyntaxException e) {
                            L.i("获取医院信息失败");
                            e.printStackTrace();
                        }
                    }
                });
    }


    /**
     * 更新用户注册信息
     */
    private void updateRegisterInfo() {

        mRegisterInfo = DataSupport.findFirst(UserRegisterInfo.class);

        if (!TextUtils.isEmpty(mRegisterInfo.getAvatar())) {

        }
        if (!TextUtils.isEmpty(mRegisterInfo.getName())) {
            mNameTextView.setText(mRegisterInfo.getName());
        }
        if (!TextUtils.isEmpty(mRegisterInfo.getNickName())) {
            mNicknameTextView.setText(mRegisterInfo.getNickName());
        }
    }


    /**
     * 更新用户基础信息
     */
    private void updateBasicInfo() {

        mBasicInfo = DataSupport.findFirst(UserBasicInfo.class);

        if (!TextUtils.isEmpty(mBasicInfo.getSex())) {
            mSexTextView.setText(mBasicInfo.getSex());
        }
        if (!TextUtils.isEmpty(mBasicInfo.getBirthday())) {
            mBirthdayTextView.setText(mBasicInfo.getBirthday());
        }
    }


    /**
     * 更新用户执业证信息
     */
    private void updateCertificateInfo() {
        mCertificateInfo = DataSupport.findFirst(UserCertificateInfo.class);
    }

    /**
     * 更新用户医院信息
     */
    private void updateHospitalInfo() {
        mHospitalInfo = DataSupport.findFirst(UserHospitalInfo.class);

        if (!TextUtils.isEmpty(mHospitalInfo.getEmployeeId())) {
            mJobNumberTextView.setText(mHospitalInfo.getEmployeeId());
        }

    }


    /**
     * 修改昵称
     *
     * @param view
     */
    @OnClick(R.id.rl_nickname)
    void onNickname(View view) {
        Intent intent = InputActivity.getIntent(this, InputActivity.NICKNAME);
        startActivityForResult(intent, REQUEST_REGISTER_INFO);
    }

    /**
     * 修改性别
     *
     * @param view
     */
    @OnClick(R.id.rl_sex)
    void onSex(View view) {
        String sex = mBasicInfo.getSex();
        Intent intent = SexActivity.getIntent(this, sex);
        startActivityForResult(intent, REQUEST_BASIC_INFO);
    }

    /**
     * 修改生日
     *
     * @param view
     */
    @OnClick(R.id.rl_birthday)
    void onBirthday(View view) {
        if (!TextUtils.isEmpty(mBasicInfo.getBirthday())) {
            Date birthDate = DateUtil.parseStringToDate(mBasicInfo.getBirthday());

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(birthDate);

            final DatePickerDialog datePickerDialog = new DatePickerDialog(this, null,
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));

            //手动设置按钮
            datePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, "完成",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //通过mDialog.getDatePicker()获得dialog上的DatePicker组件，然后可以获取日期信息
                            DatePicker datePicker = datePickerDialog.getDatePicker();
                            int year = datePicker.getYear();
                            int month = datePicker.getMonth();
                            int day = datePicker.getDayOfMonth();
                            String date = year + "-" + (month + 1) + "-" + day;
                            L.i("设置的生日是：" + date);
                            mBasicInfo.setBirthday(DateUtil.parseStringToMysqlDate(date));
                            datePickerDialog.dismiss();
                            postUserBasicInfo();
                        }
                    });

            //取消按钮，如果不需要直接不设置即可
            datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
            datePickerDialog.show();
        } else {
            T.showShort(this, R.string.date_invalid);
            L.i("生日格式有误");
        }
    }

    /**
     * 更新用户基础信息
     */
    private void postUserBasicInfo() {
        OkHttpUtils.postString()
                .url(ServiceConstant.UPDATE_BASIC_INFO)
                .content(StringUtil.addModelWithJson(mBasicInfo))
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .build()
                .execute(new FilterStringCallback() {
                    @Override
                    public void onFilterError(Call call, Exception e, int id) {
                    }

                    @Override
                    public void onFilterResponse(String response, int id) {
                        if (response.equals(REQUEST_SUCCESS)) {
                            L.i("设置生日成功");
                            // 设置成功后更新数据库
                            UserUtil.saveBasicInfo(mBasicInfo);
                            updateBasicInfo();
                        } else {
                            L.i("设置生日失败");
                        }
                    }
                });
    }


    @OnClick(R.id.rl_certificate)
    void onNurseCertificate(View view) {
        Intent intent = CertificateActivity.getIntent(this, R.string.nurse_certificate);
        startActivity(intent);
    }

    @OnClick(R.id.rl_practising_certificate)
    void onNursePractisingCertificate(View view) {
        Intent intent = CertificateActivity.getIntent(this, R.string.nurse_practising_certificate);
        startActivity(intent);
    }

    @OnClick(R.id.rl_nursing_age)
    void onNursingAge(View view) {

    }

    @OnClick(R.id.rl_hospital)
    void onHospital(View view) {
        Intent intent = HospitalActivity.getIntent(this);
        startActivity(intent);
    }

    @OnClick(R.id.rl_office)
    void onOffice(View view) {
        Intent intent = OfficeActivity.getIntent(this);
        startActivity(intent);
    }

    @OnClick(R.id.rl_job_number)
    void onJobNumber(View view) {
        Intent intent = InputActivity.getIntent(this, "工号");
        startActivityForResult(intent, REQUEST_HOSPITAL_INFO);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            // 通过回调类型更新界面
            if (requestCode == REQUEST_REGISTER_INFO) {
                updateRegisterInfo();
            } else if (requestCode == REQUEST_BASIC_INFO) {
                updateBasicInfo();
            } else if (requestCode == REQUEST_HOSPITAL_INFO) {
                updateHospitalInfo();
            }
        }
    }
}
