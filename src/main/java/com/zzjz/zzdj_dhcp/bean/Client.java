package com.zzjz.zzdj_dhcp.bean;

/**
 * Created by Administrator on 19-1-16.
 */
public class Client {

    /**
     * ip
     */
    private String ip;

    /**
     * 子网掩码
     */
    private String subnetMask;

    /**
     * mac地址
     */
    private String mac;

    /**
     * 租用过期
     */
    private String expire;

    /**
     * 类型
     */
    private String type;

    /**
     * 所属scope
     */
    private String scope;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getSubnetMask() {
        return subnetMask;
    }

    public void setSubnetMask(String subnetMask) {
        this.subnetMask = subnetMask;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getExpire() {
        return expire;
    }

    public void setExpire(String expire) {
        this.expire = expire;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
