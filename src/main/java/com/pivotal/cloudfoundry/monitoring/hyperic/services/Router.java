package com.pivotal.cloudfoundry.monitoring.hyperic.services;


public class Router implements CFService{

	private int index;
	private String ip;
	
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String job) {
		this.ip = job;
	}
	
	
	
}
