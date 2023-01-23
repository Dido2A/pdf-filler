package com.example.pdffiller.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("storage")
public class StorageProperties {

	/**
	 * Folder location for storing files
	 */
	private String location = "upload-dir";

	private boolean clearStorageOnStart = false;

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public boolean getClearStorageOnStart() {
		return clearStorageOnStart;
	}

	public void setClearStorageOnStart(boolean clearStorageOnStart) {
		this.clearStorageOnStart = clearStorageOnStart;
	}

}
