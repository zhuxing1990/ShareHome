/**
 *应用更新信息类
 */
package com.vunke.sharehome.updata;

import java.io.Serializable;

/**
 * @author zs
 * @since 2015-08-24
 * 应用更新所需要的信息
 */
public class AppTVStoreUpdateInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	// 应用 Id
	private int updateId;
	// 升级模式，0强制升级，1可选升级
	private int updateType;
	// 版本信息
	private int version;
	// 升级链接
	private String updateUrl;
	// 创建时间
	private String createTime;

	@Override
	public String toString() {
		return "AppTVStoreUpdateInfo [updateId=" + updateId + ", updateType="
				+ updateType + ", version=" + version + ", updateUrl="
				+ updateUrl + ", createTime=" + createTime + "]";
	}

	public int getUpdateId() {
		return updateId;
	}

	public void setUpdateId(int updateId) {
		this.updateId = updateId;
	}

	public int getUpdateType() {
		return updateType;
	}

	public void setUpdateType(int updateType) {
		this.updateType = updateType;
	}

	public String getUpdateUrl() {
		return updateUrl;
	}

	public void setUpdateUrl(String updateUrl) {
		this.updateUrl = updateUrl;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public AppTVStoreUpdateInfo() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

}
