package com.huawei;

public class Flavor implements Comparable<Flavor>{
	
	private String flavorName;//虚拟机名称
	private int CPU;//cpu
	private int MEM;//内存
	private boolean cpuCompare ;//是否优化cpu
	private boolean memCompare ; //是否优化mem

	public Flavor(String flavorName, int CPU, int memory ,String pSource) {
		this.flavorName = flavorName;
		this.CPU = CPU;
		this.MEM = memory;
		if(pSource.equals("CPU")){
			this.cpuCompare = true;
		}else{
			this.memCompare = true;
		}
	}

	@Override
	/**
	 *
	 * 重写Comparable接口中的compare方法，用于排序（暂时没有用到）
	 */
	public int compareTo(Flavor o) {
		double a = (double)this.CPU;
		double b = (double)this.MEM/1024;
		double c = (double)o.CPU;
		double d = (double)o.MEM/1024;
		if(isCpuCompare()){
			if((a/b)>(c/d)){
				return 1;
			}else if((a/b)<(c/d)){
				return -1;
			}else{
				if(this.CPU>o.CPU){
					return 1;
				}else if(this.CPU<o.CPU){
					return -1;
				}else{
					return 0;
				}
			}
		}else{
			if((b/a)>(d/c)){
				return 1;
			}else if((b/a)<(d/c)){
				return -1;
			}else{
				if(this.MEM>o.MEM){
					return 1;
				}else if(this.MEM<o.MEM){
					return -1;
				}else{
					return 0;
				}
			}
		}
	}
	
   
	public String getFlavorName() {
		return flavorName;
	}

	public void setFlavorName(String flavorName) {
		this.flavorName = flavorName;
	}

	public int getCPU() {
		return CPU;
	}

	public void setCPU(int cPU) {
		CPU = cPU;
	}

	public int getMEM() {
		return MEM;
	}

	public void setMEM(int mEM) {
		MEM = mEM;
	}

	public boolean isCpuCompare() {
		return cpuCompare;
	}

	public void setCpuCompare(boolean cpuCompare) {
		this.cpuCompare = cpuCompare;
	}

	public boolean isMemCompare() {
		return memCompare;
	}

	public void setMemCompare(boolean memCompare) {
		this.memCompare = memCompare;
	}
}	
