package com.vunke.sharehome.model;


import android.graphics.Bitmap;

public class VideoBean {
	private String fileName;
	private String filePath;
	private String fileVideoTime;
	private String fileCreateTime;
	private String fileType;
	private Bitmap fileBitmap;
	private boolean isSelect = false;
	public Bitmap getFileBitmap() {
		return fileBitmap;
	}

	public void setFileBitmap(Bitmap fileBitmap) {
		this.fileBitmap = fileBitmap;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getFileVideoTime() {
		return fileVideoTime;
	}

	public void setFileVideoTime(String fileVideoTime) {
		this.fileVideoTime = fileVideoTime;
	}

	public String getFileCreateTime() {
		return fileCreateTime;
	}

	public void setFileCreateTime(String fileCreateTime) {
		this.fileCreateTime = fileCreateTime;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public boolean isSelect() {
		return isSelect;
	}

	public void setSelect(boolean isSelect) {
		this.isSelect = isSelect;
	}

	@Override
	public String toString() {
		return "VideoBean [fileName=" + fileName + ", filePath=" + filePath
				+ ", fileVideoTime=" + fileVideoTime + ", fileCreateTime="
				+ fileCreateTime + ", fileType=" + fileType + ", fileBitmap="
				+ fileBitmap + ", isSelect=" + isSelect + "]";
	}



}
