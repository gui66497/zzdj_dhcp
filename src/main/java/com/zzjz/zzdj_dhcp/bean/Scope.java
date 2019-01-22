package com.zzjz.zzdj_dhcp.bean;

/**
 * Created by Administrator on 19-1-17.
 */
public class Scope {

    /**
     * 作用域地址
     */
    private String address;

    /**
     * 子网掩码
     */
    private String subnetMask;

    /**
     * 状态
     */
    private String state;

    /**
     * 作用域名称
     */
    private String scopeName;

    /**
     * 备注
     */
    private String desc;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSubnetMask() {
        return subnetMask;
    }

    public void setSubnetMask(String subnetMask) {
        this.subnetMask = subnetMask;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getScopeName() {
        return scopeName;
    }

    public void setScopeName(String scopeName) {
        this.scopeName = scopeName;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
