package com.jerry.nurse.model;

/**
 * Created by Jerry on 2017/8/25.
 */

public class WeiXin {

    private String Id;
    private String OpenId;
    private String NickName;
    private int Sex;
    private String Language;
    private String City;
    private String Province;
    private String Country;
    private String HeadImgurl;


    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getOpenId() {
        return OpenId;
    }

    public void setOpenId(String openId) {
        OpenId = openId;
    }

    public String getNickName() {
        return NickName;
    }

    public void setNickName(String nickName) {
        NickName = nickName;
    }

    public int getSex() {
        return Sex;
    }

    public void setSex(int sex) {
        Sex = sex;
    }

    public String getLanguage() {
        return Language;
    }

    public void setLanguage(String language) {
        Language = language;
    }

    public String getCity() {
        return City;
    }

    public void setCity(String city) {
        City = city;
    }

    public String getProvince() {
        return Province;
    }

    public void setProvince(String province) {
        Province = province;
    }

    public String getCountry() {
        return Country;
    }

    public void setCountry(String country) {
        Country = country;
    }

    public String getHeadImgurl() {
        return HeadImgurl;
    }

    public void setHeadImgurl(String headImgurl) {
        HeadImgurl = headImgurl;
    }
}
