package com.huawei;
import java.util.List;
/**
 * 数据滤波处理：是对于高于三倍标准差的数据用三倍标准差代替
 * @author zyf
 *
 */
public  class Filter{
/**
 * @param originalData
 * @return 均值
 */
	public static double avgData(double [] originalData)//平均值
	{
		return sumData(originalData) / originalData.length;
	}
	
/**
 * @param originalData
 * @return 求和
 */
	public static  double sumData(double [] originalData)//数组求和
	{
		double sum = 0.0;
		for (int i = 0; i < originalData.length; ++i)
		{
			sum += originalData[i];
		}
		return sum;
	}
/**
 * @param list
 * @return 对于集合类List中的元素求和
 */
	public static  double sumData(List<Double>list)//集合求和
	{
		double sum = 0.0;
		for (int i = 0; i < list.size(); ++i)
		{
			sum += list.get(i);
		}
		return sum;
	}
/**
 * @param originalData
 * @return 标准差
 */
	public static double standardDiff(double [] originalData){
		double n = originalData.length;
		double sum = 0;
		double avg = avgData(originalData);
		for (int i = 0; i < originalData.length; i++) {
			sum += Math.pow((originalData[i]-avg), 2);
		}
		return Math.sqrt(sum/n);
	}
/**
 * 平滑训练集中的异常点
 * 训练集中超过三倍标准差的数据用用三倍标准差代替
 */
	public static double[] diffFilter(double [] originalData){
		double diff = 3 * standardDiff(originalData);
		for (int i = 0; i < originalData.length; i++) {
			if(originalData[i] > diff){
				originalData[i] = diff;//对于高于三倍标准差的数据用三倍标准差代替
			}
		}
		return originalData;
	}
	
}
	