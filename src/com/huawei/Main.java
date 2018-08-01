package com.huawei;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class Main {
	public static int serverCpu;//cpu
	public static int serverMEM;//内存
    public static int serverHD;//硬盘
    public static int pvmNum;//需要预测的虚拟机个数pridict vm number
    public static String pSource;//资源维度
    public static Date startP;//预测第一天
    public static Date endP;//预测末期
    public static Date trainStart;//训练第一天
    public static Date trainEnd;//训练末期
    public static List<Flavor> pridictVm = new ArrayList<Flavor>();//需要预测的虚拟机规格
    public static DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static int days;//训练天数
    public static int diffDays;//训练最后一天和预测第一天的天数差
    public static int preDays;//预测天数
    
    
	public static void main(String[] args) {//主程序入口
		//读取训练集和输入文件数据
		
		 String[]d = FileUtil.read("E:/myEclipse_workspaces/Huawei初赛/input_5flavors_cpu_7days.txt", null);//输入文件
		 String[]b = FileUtil.read("E:/myEclipse_workspaces/Huawei初赛/TrainData_2015.1.1_2015.2.19.txt", null);//训练集
		
		 // String[]b = FileUtil.read("E:/myEclipse_workspaces/Huawei/data_2015_1~2.txt", null);
		//String[]b = FileUtil.read("E:/myEclipse_workspaces/Huawei/data_2015_2.txt", null);
		//String[]b = FileUtil.read("E:/myEclipse_workspaces/Huawei/data_2015_3.txt", null);
		//String[]b = FileUtil.read("E:/myEclipse_workspaces/Huawei/data_2015_4.txt", null);
		//String[]b = FileUtil.read("E:/myEclipse_workspaces/Huawei/data_2015_5.txt", null);
		//String[]b = FileUtil.read("E:/myEclipse_workspaces/Huawei/data_2015_12.txt", null);
		//String[]b = FileUtil.read("E:/myEclipse_workspaces/Huawei/data_2016_1.txt", null);
		 
		 long t1 = System.currentTimeMillis();//程序起始时间
	     String []s =  predictVm(b, d);//s[]为输出预测结果
	     FileUtil.write("C:/Users/zyf/Desktop/output.txt", s, false);//将结果写出文件
	     for (int i = 0; i < s.length; i++) {
	    	 System.out.println(s[i]);//在控制台输出预测结果
		 }
	   	 long t2 = System.currentTimeMillis();
	   	 System.out.println("time:"+(t2-t1)+"ms");//跑程序花费的时间
	}
/**
 * 预测
 * @param ecsContent
 * @param inputContent
 * @return
 */
	public static String[] predictVm(String[] ecsContent, String[] inputContent) {
		int data[][] = dataProcess(ecsContent, inputContent);//接收并处理数据--》data是行为天数，列为虚拟机规格，对应位置元素为某种虚拟机该天的用量
		List<Integer> pridictNum = new ArrayList<Integer>();//存放预测数量
		double alpha = 0.05;//二次平滑系数
		int type = 2;//平滑类型
		
		for (int i = 0; i < pvmNum; i++) {
			double real[] = new double [data.length];//real用于每次迭代存放没一种虚拟机的每天的用量（即data的每一列取出来）
			for(int j = 0;j < data.length;j++){
				real[j] = data[j][i];
			}
			real = Filter.diffFilter(real);//平滑异常点：是对于高于三倍标准差的数据用三倍标准差代替；
			double sum = 0;//用于叠加计算预测日期内虚拟机个数总和
			for (int k = diffDays; k < (diffDays + preDays); k++) {//开始预测
			   sum += getPredict(real, k,alpha,type);
			}		
			pridictNum.add((int)sum);//将虚拟机每一种的预测总量存入集合 ；  这里的9是随机加一的个数字，会张分数，应该是后面的服务器装的更满了
		}	
		return  putFlavors1(pridictNum);//将放置后的结果返回
	}
	
	
	
	/**
	 * 数据处理
	 * @param ecsContent
	 * @param inputContent
	 * @return
	 */
    public static int[][] dataProcess(String[] ecsContent, String[] inputContent){
    	List<String[]> ecs = new LinkedList<String[]>();//存放训练集元素
        List<String[]> input = new ArrayList<String[]>();//存放输入文件
    	String sp1[];//将训练集文件的每个元素存放到字符串数组里
		String sp2[];//将输入文件的每个元素存放到字符串数组里
		int data[][] = null;//最终返回处理后的data，行：为天数，列：为虚拟机规格，对应位置元素为某种虚拟机该天的用量

		for (int i = 0; i < ecsContent.length; i++) {
			sp1 = ecsContent[i].split("	");
	    	ecs.add(sp1);
		}
	
		for (int i = 0; i < inputContent.length; i++) {
			if(inputContent[i].equals(""))  continue;
			sp2 = inputContent[i].split(" ");
		  	input.add(sp2);
		}	
		
		sp1 = null;//置空以便于jvm回收
		sp2 = null;
		/**
		 * 读取文件的对应位置对变量赋值（针对初赛格式赋值的，复赛不同，需要修改）
		 */
		serverCpu = Integer.parseInt(input.get(0)[0]);
		serverMEM = Integer.parseInt(input.get(0)[1]);
		serverHD = Integer.parseInt(input.get(0)[2]);
		pvmNum = Integer.parseInt(input.get(1)[0]);
		int size = input.size();
		pSource = input.get(size-3)[0];
		//存放需要预测的虚拟机规格到 pridictVm集合
		for(int i = 0;i< pvmNum;i++){
			Flavor f = new Flavor(input.get(2+i)[0], Integer.parseInt(input.get(2+i)[1]),Integer.parseInt(input.get(2+i)[2]),pSource);
		    pridictVm.add(f);
		}
		
		try {
			startP = df.parse(input.get(size-2)[0]+" "+input.get(size-2)[1]);//预测起始天数
			endP = df.parse(input.get(size-1)[0]+" "+input.get(size-1)[1]);//预测终止天数
			trainStart = df.parse(ecs.get(0)[2].substring(0,11)+"00:00:00");//训练集起始天数
			trainEnd = df.parse(ecs.get(ecs.size()-1)[2].substring(0,11)+"00:00:00");//训练集起始天数
		    long diff = trainEnd.getTime() - trainStart.getTime();//训练跨度   ：这样得到的差值是微秒级别 
		    long diff2 = startP.getTime() - trainEnd.getTime();//预测起始日期与训练集最后一天的天数差
		    long diff3 = endP.getTime() - startP.getTime();//预测的天数和
		    days = (int)(diff/(1000 * 60 * 60 * 24))+1;// 转为天数，需要加1
		    diffDays = (int)(diff2/(1000 * 60 * 60 * 24));
		    preDays = (int)(diff3/(1000 * 60 * 60 * 24))+1;
		/**
		 *统计训练数据中对应的虚拟机个数
		 */
		   Calendar date = Calendar.getInstance();
	       date.setTime(trainStart);
	       Date t =  date.getTime();//用来递增天数
	       Date t1 = df.parse(ecs.get(0)[2]);//取数据集第一天数据
	       int num = 0;
	       data = new int[days][pvmNum];//行为天数，列为虚拟机规格
		   for (int i = 0; i < days; i++) {
               date.add(Calendar.DAY_OF_YEAR,1);//递增1天
               t = date.getTime();
               while(t.getTime()>t1.getTime()){//判断是否属于上一天
            	   String s1 = ecs.get(num)[1];//取出虚拟机名字
            	   for (int j = 0; j < pvmNum; j++) {
					  if(s1.equals(pridictVm.get(j).getFlavorName())){//判断相同则对应虚拟机个数加一
						 data[i][j] += 1;
					 }
				   }
            	   if(num <ecs.size()-1){//判断是否数据去到末尾
            		   t1 = df.parse(ecs.get(++num)[2]);//取出下一列时间
            	   }else//取完退出
            	   {
            		   break;
            	   }
			   }
	       }
		}catch (ParseException e) {
			e.printStackTrace();
		}
		return data;
}
	
	/**
	 * 
	 * @param data:某一种虚拟机的每天的用量
	 * @param days:预测的天数
	 * @param alpha:平滑指数
	 * @param type：平滑类型
	 * @return 某一天的预测值
	 */
	  private static double getPredict(double data[], int days,double alpha,int type) {//type{1,2,3}表示几次平滑
	       double result = 0;  //存放最终预测值
	       if(type == 1){//一次平滑（写的有点问题，不过没有用到）
	    	   double firstSmooth = (data[0]+data[1]+data[2])/3;
	    	   for (int i = 3; i < data.length; i++) {
	    		   firstSmooth = alpha * data[i] + (1-alpha) * firstSmooth;
			   }
	    	   
	    	   return firstSmooth;
	       
	       }else if(type == 2){//二次平滑
		        double firstSmooth = data[0];
		        double secSmooth = data[0];
		        for (int i = 0; i < data.length; i++) {
		        	firstSmooth = alpha * data[i] + (1-alpha) * firstSmooth;
		        	secSmooth = alpha * firstSmooth + (1-alpha) * secSmooth;
		        }
		        double a = 2 * firstSmooth - secSmooth;
		        double b = (alpha /(1-alpha)) * (firstSmooth - secSmooth);
		        
		        result = a + b * days;//计算预测值
	       
	       }else if(type == 3){//三次平滑
	        	double firstSmooth = (data[0]+data[1]+data[2])/3;
		        double secSmooth = firstSmooth;
		        double thirdSmooth = firstSmooth;
		        for (int i = 0; i < data.length; i++) {
		        	firstSmooth = alpha * data[i] + (1-alpha) * firstSmooth;
		        	secSmooth = alpha * firstSmooth + (1-alpha) * secSmooth;
		        	thirdSmooth = alpha * secSmooth + (1-alpha) * thirdSmooth;
		        }
		        double a = 3 * firstSmooth - 3 * secSmooth + thirdSmooth;
		        double b = (alpha /(2*Math.pow((1-alpha), 2))) * ((6 - 5 * alpha) * 
		        		firstSmooth - 2*(5 - 4 * alpha) * secSmooth +(4 - 3 * alpha) * thirdSmooth);
		        double c = (Math.pow(alpha, 2)/(2*Math.pow((1-alpha), 2))) * (firstSmooth - 2 * secSmooth + thirdSmooth);
		        result = a + b * days + c * Math.pow(days, 2);
	        }else{
	        	System.out.println("类型输入错误");
	        }
	        
	        if(result < 0) result = 0;//若预测个数小于零，则赋值为0
	       
	        return result;
	    }
	  /**
	   * 将预测的虚拟机放置
	   */
	  public static String[] putFlavors1(List<Integer> preFlavors) {
		  
		  String results[] = null; //存放一次退火+填补服务器后的最佳结果
		  String bestResult[] = null;  //存放多次退火+填补服务器后的最佳结果：即最终返回结果
		  List <Server> bestServers = new ArrayList<Server> ();//存放一次退火过程中利用率最佳的服务器组合
		  List <Server> bestServerL = new ArrayList<Server> ();//存放多次退火过程中利用率最佳的服务器组合
		  List<Flavor> flavors = new ArrayList<Flavor>();
		  List<Flavor> newFlavors = new ArrayList<Flavor>();
		  List <Integer> tempPreFlavor = new ArrayList<Integer>();//用于存放改变前的 preFlavor，因为退火的每次循环都会改变preFlavor(填补服务器的时候)虚拟机个数，但是每次循环都必须在原来的 基础上进行改变
		  List<Integer> bestPreFlavor = new ArrayList<Integer>();//用于存放多次循环退火后改变的利用率最高的 preFlavors
		  
		  for (int i = 0; i < preFlavors.size(); i++) {//因为List是引用传递，所以采取循环赋值，不能直接tempPreFlavor = preFlaours;
			  tempPreFlavor.add(preFlavors.get(i));//赋值
			  bestPreFlavor.add(preFlavors.get(i));
		  }
		
		 for (int i = 0; i < pvmNum; i++) {
			 int numP = preFlavors.get(i);//某种虚拟机预测个数
			 for(int j = 0;j < numP ;j++){
			 	newFlavors.add(pridictVm.get(i));//将预测的虚拟机个数，按照各自的型号放入newFlavors集合中，用于后面放置
			 }
		 }
		 
		/**
		 * 多次循环退火找最优放置方案
		 * 退火：多次随机交换newFlavors中虚拟机位置后，按照FF(首次适应法)放置
		 */
		double bestScore = flavors.size()+1;//一次退火（不包含填补）的最佳分数（利用率高）
		double t = 100.0; //退火初始温度 
		double tMin = 1;  //最终温度
		double r = 0.999; //下降率
		List<Integer> swap = new ArrayList<Integer>();//用于随机交换位置
		
		for (int i = 0; i < newFlavors.size(); i++) {
			swap.add(i);//存入位置：假设newFlavor中有5个虚拟机，则swap={1,2,3,4,5};
		}
		
	    int cpuRR = Integer.MAX_VALUE;//用于比较cpu剩余，先赋值一个大数
		int memRR = Integer.MAX_VALUE;//Mem
		int nnn=0;//计算迭代次数（用于测试跑程序的时间）
		int iterator = 150;//退火循环次数
		for (int q = 0; q < iterator; q++) {
			while(tMin<t){//退火开始
					nnn++;//计循环次数
					Collections.shuffle(swap);//随机打乱swap
				    Collections.swap(newFlavors, swap.get(0), swap.get(1));//取出swap打乱后的前两个数字交换newFlavors中的相应位置的虚拟机顺序
				    List <Server> servers = new ArrayList<Server> ();
		            servers.add(new Server(serverCpu, serverMEM, serverHD));//开启第一台服务器准备放置
					
		            int numServer = 0;//作为Server集合的下标（从零开始为一台服务器）
					for (int i = 0; i < newFlavors.size(); i++) {
						
						int cpuRest = servers.get(numServer).getCpuRest();//取出服务器剩余cpu和mem
						int memRest = servers.get(numServer).getMemRest();
						int cpu = newFlavors.get(i).getCPU();//取出虚拟机的规格（cpu和MEM）
						int mem = newFlavors.get(i).getMEM()/1024;
						
						if(cpuRest >= cpu && memRest >= mem){//比较是否服务器还是否用空间放置,可以就放置
							servers.get(numServer).setCpuRest(cpuRest - cpu);//更新资源余量
							servers.get(numServer).setMemRest(memRest - mem);
							servers.get(numServer).getFlavors().add(newFlavors.get(i));//放置
						}else{//如果本台服务器不能放置本次取出的虚拟机，那么就检测之前开启的虚拟机是否还有空间对本次取出的虚拟机放置
							int num = 0;//用于检测是否放进了前面开启的服务器
							
							for (int j = 0; j < servers.size(); j++) {
								if (servers.get(j).getCpuRest()>= cpu && servers.get(j).getMemRest()>= mem){
									servers.get(j).getFlavors().add(newFlavors.get(i));
									servers.get(j).setCpuRest(servers.get(j).getCpuRest() - cpu);
									servers.get(j).setMemRest(servers.get(j).getMemRest() - mem);
									num++;
									break;//必须break,否则假设前面开启的服务器有两台可以放进本次取出的虚拟机的话，同一个虚拟机就会被放置两次。
								}
						    }
							
							if(num==0){	//num==0说明之前开启的服务器也不能装进去本次取出的虚拟机，那么新开一台服务器进行放置
								numServer++;//服务器台数加一
								servers.add(new Server(serverCpu, serverMEM, serverHD));//新开一台
								servers.get(numServer).getFlavors().add(newFlavors.get(i));//放置到新开的服务器中，不需要检测，因为单个虚拟机资源不会超过服务器资源规格
								servers.get(numServer).setCpuRest(servers.get(numServer).getCpuRest() - cpu);//更新资源余量
								servers.get(numServer).setMemRest(servers.get(numServer).getMemRest() - mem);
							}
							
						}	
				   }
					/**
					 * 计算本次放置虚拟机耗费服务器评价分数score(double型)
					 * 如果使用了n个服务器，则前n-1个服务器de分数为1，第N个服务器分数为资源空闲率，空闲率越小即分数越小越好
					 * 模拟退火就是得到取得分数最小的放置方式
				     */
                   double d = servers.get(servers.size()-1).useageRate();//算最后一台服务器的资源利用率
				   double score = servers.size() - 1 + (1 - d);//得分
				   //分数低保存此方案，更新最佳分数，此种放置顺序的newFlavors，和最佳放置的servers
		           if(bestScore > score){
		        	   bestScore = score;
		        	   bestServers = servers;
		        	   flavors = newFlavors;
		           }
		           else{//分数高的话，以一定的几率保存此次放置，来跳出局部最优。当前分数离最有分数差距越小，(Math.exp((bestScore-score)/t)越大
		        	   if((Math.exp((bestScore-score)/t))> Math.random()){
		        		   bestScore = score;
			        	   bestServers = servers;
			        	   flavors = newFlavors;
		        	   }
		           }
		           t = r * t;//温度降低
				}
			   
	            t = 100;//一次退火结束后将温度必须重置回100，否则再次循环没有意义
	            
	            /**
	             * 1.
	             * 2.进行填补空闲多的服务器
	             */
	            int length = pvmNum + bestServers.size() + 3;//输出文件的长度
				results = new String[length];//用于记录此次循环后的输出文件
				results[pvmNum+1] = "";//对应位置为空格
				results[pvmNum+2] = String.valueOf(bestServers.size());//对应位置为虚拟机个数
				results[pvmNum+3] = "";//对应位置为空格
				for (int i = 0; i < bestServers.size(); i++) {//取出每一个服务器对应放置情况，对输出文件进行输出
					int fNum = bestServers.get(i).getFlavors().size();//某个服务器中虚拟机的个数
					for (int j = 0; j < fNum; j++) {//取出每一个虚拟机
						
						if(j==0){//第一个虚拟机直接添加虚拟机名字和计数，因为不需要检测之前是否有这个虚拟机
							results[pvmNum +3+i] =(i+1)+" "+bestServers.get(i).getFlavors().get(j).getFlavorName()+" "+ 1;
						    continue;//返回循环
						}
						//不是第一个虚拟机的话，需要检测之前是否取出过同类型的虚拟机
						String flavorName = bestServers.get(i).getFlavors().get(j).getFlavorName();//取出将要放置的虚拟机名字
						//如果包括这个虚拟机名字的话，取出这个虚拟机后面的数字加一
						if(results[pvmNum +3+i].contains(flavorName+" ")){//注：flavorName+" ",加一个空格为了区分 flavor1 和flavor11，flavor12.。。，查找flavor1字符串可能查到了flavor11在后面的数字加了一导致错误），之前一直输出报错就是这种问题
							 int fromIndex = results[pvmNum +3+i].indexOf(flavorName+" ")+flavorName.length() + 1;//取出匹配字符串后面的计数字符的起始下标
	     					 int endIndex = results[pvmNum +3+i].indexOf(" ", fromIndex);//因为计数可能超过一位数比如某种虚拟机计数到了11，所以需要取出计数字符的结尾下标，即从计数字符的起始下标起到下一个空格之前。
	     					 
	     					 if(endIndex < 0){//如果这个虚拟机名字在字符串最后一个，计数字符后面没有空格的话，会返回一个负数
	     						endIndex = results[pvmNum +3+i].length();//此时结尾下标即这一行字符串最后一位
	     					 }
	     					 
	     					 String s1 = results[pvmNum +3+i].substring(fromIndex,endIndex);//取出这个计数字符
	     					 String s2 = String.valueOf(Integer.parseInt(s1)+1);//计数字符转为int后加一，再转为字符  ：即 "3“->”4“
	     					 StringBuffer sb = new StringBuffer(results[pvmNum +3+i]);//Stringbuffer中才有替换方法，所以先把字符串转StringBuffer
	     					 sb.replace(fromIndex,(fromIndex+s1.length()),s2);//将加了一后的计数字符s2替换到s1的位置
	     					 results[pvmNum +3+i] = sb.toString();//StringBuffer再转为String格式
	     					 
	     				}else{
	     					//如果不包括这个虚拟机，直接在后面扩展新的虚拟机名字，并计数为1
	     						results[pvmNum +3+i] += " "+ flavorName+" "+ 1;
	     				}
					
					}
				}
			     System.out.println("第"+(q+1)+"次退火后各服务器剩余：");
				 for (int i = 0; i < bestServers.size(); i++) {
					   System.out.println("cpu:"+bestServers.get(i).getCpuRest()+", mem:"+bestServers.get(i).getMemRest());
				   }
				 	
				/**
				 * 将不满的服务器尽量填满
				 */
			   for (int i = 0; i < bestServers.size(); i++) {//取出退完火后的服务器集合
				   for (int j = pridictVm.size()-1; j >=0 ; j--) {//取出每一个（倒着取本来是想先放资源占比大的，再放小的，后来试过以后没什么用）
	                     int cpuR = bestServers.get(i).getCpuRest();//剩余
	                     int memR = bestServers.get(i).getMemRest();
	                     int cpuF = pridictVm.get(j).getCPU();
	                     int memF = pridictVm.get(j).getMEM()/1024;
	                     if(cpuR>=cpuF && memR>=memF){//判断在要求预测的虚拟机类型中有没有能放进去类型进行填补空余
	                    	 preFlavors.set(j, preFlavors.get(j)+1);//能填补的话对应虚拟机的预测数加一
							 String flavorName = pridictVm.get(j).getFlavorName();//取出将要放置的虚拟机名字
		     				//如果包括这个虚拟机名字的话，取出这个虚拟机后面的数字加一（
		     					
							 if(results[pvmNum +3+i].contains(flavorName+" ")){//注：flavorName+" ",加一个空格为了区分 flavor1 和flavor11，flavor2.。。，查找flavor1字符串可能查到了flavor11在后面的数字加了一导致错误），之前一直输出报错就是这种问题
								 int fromIndex = results[pvmNum +3+i].indexOf(flavorName+" ")+flavorName.length() + 1;//取出匹配字符串后面的计数字符的起始下标
		     					 int endIndex = results[pvmNum +3+i].indexOf(" ", fromIndex);//因为计数可能超过一位数，所以需要取出计数字符的结尾下标，即从计数字符的起始下标起到下一个空格之前。
		     					 
		     					 if(endIndex < 0){//如果这个虚拟机名字在字符串最后一个，计数字符后面没有空格的话，会返回一个负数
		     						endIndex = results[pvmNum +3+i].length();//此时结尾下标即字符串最后一位
		     					 }
		     					 
		     					 String s1 = results[pvmNum +3+i].substring(fromIndex,endIndex);//取出这个计数字符
		     					 String s2 = String.valueOf(Integer.parseInt(s1)+1);//计数字符转为int后加一，再转为字符  "3->4“
		     					 StringBuffer sb = new StringBuffer(results[pvmNum +3+i]);//Stringbuffer中才有替换方法，所以先转StringBuffer
		     					 sb.replace(fromIndex,(fromIndex+s1.length()),s2);//将变化后的计数字符s2替换到s1的位置
		     					 results[pvmNum +3+i] = sb.toString();//StringBuffer再转为String格式
		     					 
		     				}else{
		     					//如果不包括这个虚拟机，直接在后面扩展新的虚拟机名字，并计数为1
		     						results[pvmNum +3+i] += " "+ flavorName+" "+ 1;
		     				}
		     					//更新资源余量
	                    	    bestServers.get(i).setCpuRest(cpuR - cpuF);
	                    	    bestServers.get(i).setMemRest(memR - memF);
	                            bestServers.get(i).getFlavors().add(pridictVm.get(j));
	                     }
				   }
			   } 
			   
			   //检查	输出填补后各服务器剩余
			   System.out.println("第"+(q+1)+"次循环填补后各服务器剩余：");
			   for (int i = 0; i < bestServers.size(); i++) {
				   System.out.println("cpu:"+bestServers.get(i).getCpuRest()+", mem:"+bestServers.get(i).getMemRest());
			   }
			
			   //比较每次填补完后的剩余情况取最佳保存：即取每个服务器剩余的总和最小的进行比较保存
			   if(pSource.equals("CPU")){
				   int cpuS = 0;
				   for (int i = 0; i < bestServers.size(); i++) {
					   cpuS += bestServers.get(i).getCpuRest();//剩余cpu资源和
				   } 
				   if(cpuS < cpuRR){//剩余少的方案保存为最佳方案
					   cpuRR = cpuS;
					   bestResult = results;
					   bestServerL = bestServers;
					   for (int i = 0; i < preFlavors.size(); i++) {
						   bestPreFlavor.set(i, preFlavors.get(i));//保存最佳方案的虚拟机预测
					   }
				   }
			   }else {//优化MEM同上
				   int memS = 0;
				   for (int i = 0; i < bestServers.size(); i++) {
					   memS += bestServers.get(i).getMemRest();
				   } 
				   if(memS < memRR){
					   memRR = memS;
					   bestServerL = bestServers;
					   bestResult = results;
					   for (int i = 0; i < preFlavors.size(); i++) {
							  bestPreFlavor.set(i, preFlavors.get(i));
					   }
				   }
			   }
			   /**
			    * 刷新preFlavor;最后一次保留
			    */
			   if(q != iterator-1){//
				   for (int i = 0; i < preFlavors.size(); i++) {
					   preFlavors.set(i, tempPreFlavor.get(i));
				   }
			  }
			   
	       }//加在退火外部的for循环到此结束（即262行的for循环）
		
		   //输出最佳方案
		   System.out.println("----------------------------------");
		   System.out.println("最佳方案服务器剩余情况：");
		   for (int i = 0; i < bestServerL.size(); i++) {
				System.out.println("cpu:"+bestServerL.get(i).getCpuRest()+", mem:"+bestServerL.get(i).getMemRest());
			}
		   //将结果的虚拟机情况加入，即输出文件的前pvmNum+1行输出。
			int flavorSum = 0;
			for (int i = 0; i < pvmNum; i++) {
				flavorSum += bestPreFlavor.get(i);
				bestResult[i+1] = pridictVm.get(i).getFlavorName()+" "+bestPreFlavor.get(i);
			}
			bestResult[0] = String.valueOf(flavorSum);
			
			System.out.println(nnn);//输出循环次数
			
			return bestResult;//返回结果
  }	
}