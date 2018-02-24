package com.qjy;

import javax.swing.JLabel;

public class LinkItem extends JLabel{
	private String name;
	private String hostName;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	@Override
	public String toString() {
		return getName();
	} 
}
