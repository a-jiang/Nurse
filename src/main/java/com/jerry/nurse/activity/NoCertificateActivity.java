package com.jerry.nurse.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.jerry.nurse.R;
import com.jerry.nurse.model.UserPractisingCertificateInfo;
import com.jerry.nurse.model.UserProfessionalCertificateInfo;
import com.jerry.nurse.view.TitleBar;

import org.litepal.crud.DataSupport;

import butterknife.Bind;
import butterknife.OnClick;

import static com.jerry.nurse.constant.ServiceConstant.AUDIT_EMPTY;

public class NoCertificateActivity extends BaseActivity {

    public static final String EXTRA_TITLE = "title";

    public static final String PROFESSIONAL_CERTIFICATE = "专业技术资格证书";
    public static final String PRACTISING_CERTIFICATE = "护士执业证书";

    @Bind(R.id.tb_certificate)
    TitleBar mTitleBar;

    private String mTitle;

    public static Intent getIntent(Context context, String title) {
        Intent intent = new Intent(context, NoCertificateActivity.class);
        intent.putExtra(EXTRA_TITLE, title);
        return intent;
    }

    @Override
    public int getContentViewResId() {
        return R.layout.activity_no_certificate;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        mTitle = getIntent().getStringExtra(EXTRA_TITLE);
        if (PROFESSIONAL_CERTIFICATE.equals(mTitle)) {
            UserProfessionalCertificateInfo professionalCertificateInfo =
                    DataSupport.findFirst(UserProfessionalCertificateInfo.class);
            if (professionalCertificateInfo != null &&
                    professionalCertificateInfo.getVerifyStatus() != AUDIT_EMPTY) {
                Intent intent = ProfessionalCertificateActivity.getIntent(this);
                startActivity(intent);
                finish();
            }
        } else if (PRACTISING_CERTIFICATE.equals(mTitle)) {
            UserPractisingCertificateInfo practisingCertificateInfo =
                    DataSupport.findFirst(UserPractisingCertificateInfo.class);
            if (practisingCertificateInfo != null &&
                    practisingCertificateInfo.getVerifyStatus() != AUDIT_EMPTY) {
                Intent intent = PractisingCertificateActivity.getIntent(this);
                startActivity(intent);
                finish();
            }
        }

        mTitleBar.setTitle(mTitle);
    }

    @OnClick(R.id.acb_go_certificate)
    void onGoCertificateButton(View view) {
        if (PROFESSIONAL_CERTIFICATE.equals(mTitle)) {
            Intent intent = ProfessionalCertificateActivity.getIntent(this);
            startActivity(intent);
            finish();
        } else if (PRACTISING_CERTIFICATE.equals(mTitle)) {
            Intent intent = PractisingCertificateActivity.getIntent(this);
            startActivity(intent);
            finish();
        }
    }
}
