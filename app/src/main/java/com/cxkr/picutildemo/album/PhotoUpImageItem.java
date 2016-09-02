package com.cxkr.picutildemo.album;

import java.io.Serializable;

public class PhotoUpImageItem implements Serializable {

	//图片ID
	private String imageId;
	//原图路径
	private String imagePath;
	//是否被选择
	private boolean isSelected = false;
	
	public String getImageId() {
		return imageId;
	}
	public void setImageId(String imageId) {
		this.imageId = imageId;
	}
	public String getImagePath() {
		return imagePath;
	}
	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}
	public boolean isSelected() {
		return isSelected;
	}
	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
	
	
}
