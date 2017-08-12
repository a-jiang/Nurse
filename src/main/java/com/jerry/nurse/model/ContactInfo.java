package com.jerry.nurse.model;

import org.litepal.crud.DataSupport;

/**
 * Created by Jerry on 2017/8/11.
 */

public class ContactInfo extends DataSupport {

    private String mAvatar;
    private String mName;
    private String mNickName;
    private String mCellphone;
    private String mRemark;
    private String mRegisterId;

    public String getAvatar() {
        return mAvatar;
    }

    public void setAvatar(String avatar) {
        mAvatar = avatar;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getNickName() {
        return mNickName;
    }

    public void setNickName(String nickName) {
        mNickName = nickName;
    }

    public String getCellphone() {
        return mCellphone;
    }

    public void setCellphone(String cellphone) {
        mCellphone = cellphone;
    }

    public String getRemark() {
        return mRemark;
    }

    public void setRemark(String remark) {
        mRemark = remark;
    }

    public String getRegisterId() {
        return mRegisterId;
    }

    public void setRegisterId(String registerId) {
        mRegisterId = registerId;
    }
}