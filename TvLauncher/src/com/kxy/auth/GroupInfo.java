package com.kxy.auth;

/**
 * Created by zhuxi on 2016/10/19.
 */
public class GroupInfo {


    /**
     * userToken : 06228843262127592787131019144531
     * epgDomain : http://10.27.40.138:8082/EDS/jsp/index.jsp     
     * epgDomainBackup : http://10.27.40.138:8082/EDS/jsp/index.jsp
     * userGroupNmb : 2a000137
     * platFlag : 2
     * ntPDomain : 202.96.168.10
     * code : 200
     * message : 成功
     */
	
	/**
	 *  用户临时身份 验证码
	 */
    private String userToken;
    /**
     * EPG 域名
     */
    private String epgDomain; 
    /**
     * EPG 备份域名
     */
    private String epgDomainBackup;
    /**
     *  用户分组信息标识
     */
    private String userGroupNmb;
    
    private String platFlag;
    /**
     * 时间同步域名
     */
    private String ntPDomain;
    
    private String code;
    private String message;

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public String getEpgDomain() {
        return epgDomain;
    }

    public void setEpgDomain(String epgDomain) {
        this.epgDomain = epgDomain;
    }

    public String getEpgDomainBackup() {
        return epgDomainBackup;
    }

    public void setEpgDomainBackup(String epgDomainBackup) {
        this.epgDomainBackup = epgDomainBackup;
    }

    public String getUserGroupNmb() {
        return userGroupNmb;
    }

    public void setUserGroupNmb(String userGroupNmb) {
        this.userGroupNmb = userGroupNmb;
    }

    public String getPlatFlag() {
        return platFlag;
    }

    public void setPlatFlag(String platFlag) {
        this.platFlag = platFlag;
    }

    public String getNtPDomain() {
        return ntPDomain;
    }

    public void setNtPDomain(String ntPDomain) {
        this.ntPDomain = ntPDomain;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

	@Override
	public String toString() {
		return "GroupInfo [userToken=" + userToken + ", epgDomain=" + epgDomain
				+ ", epgDomainBackup=" + epgDomainBackup + ", userGroupNmb="
				+ userGroupNmb + ", platFlag=" + platFlag + ", ntPDomain="
				+ ntPDomain + ", code=" + code + ", message=" + message + "]";
	}
    
}
