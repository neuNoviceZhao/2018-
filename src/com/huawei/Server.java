package com.huawei;

import java.util.ArrayList;
import java.util.List;

public class Server {
	private int CPU;
	private int MEM;
	private int HD;//硬盘
	private String serverName;//服务器规格（复赛新加）
	private int cpuRest;//cpu剩余
	private int memRest;//mem剩余
	private List <Flavor> flavors = new ArrayList<Flavor>();//用于保存此服务器中装进的虚拟机

	public Server(int cPU, int mEM, int hD) {
		this.CPU = cPU;
		this.MEM = mEM;
		this.HD = hD;
		this.cpuRest = cPU;
		this.memRest = mEM;
	}
	
	public int getCpuRest() {
		return cpuRest;
	}

	public void setCpuRest(int cpuRest) {
		this.cpuRest = cpuRest;
	}

	public int getMemRest() {
		return memRest;
	}

	public void setMemRest(int memRest) {
		this.memRest = memRest;
	}

	public List<Flavor> getFlavors() {
		return flavors;
	}
    /**
     * 
     * @return  返回某种资源的空闲率
     */
	public double useageRate(){
		if(this.getFlavors().get(0).isCpuCompare()){//比较的是cpu的利用率的话
			double a = this.getCpuRest();
			double b = this.CPU;
			return (a/b);//   剩余/总量
		}
		else{//比较的是MEM利用率的话
			double a = this.getMemRest();
			double b = this.MEM;
			return (a/b);
		}
	}
}
