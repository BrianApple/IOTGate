package gate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import gate.base.cache.ClientChannelCache;
import gate.base.chachequeue.CacheQueue;
import gate.client.Client2Master;
import gate.cluster.ZKFramework;
import gate.rpc.rpcProcessor.RPCProcessor;
import gate.rpc.rpcProcessor.RPCProcessorImpl;
import gate.server.Server4Terminal;
import gate.threadWorkers.TServer2MClient;
import gate.util.CommonUtil;
/**
 * 入口
 * @Description: 
 * @author  yangcheng
 * @date:   2019年3月18日
 */
public class Entrance {
	
	public static CommandLine commandLine = null;
	public static int gatePort = 9811;
	public static String zkAddr = null;
	public static List<String> masterAddrs = new ArrayList<>(1);
	public static CountDownLatch locks = new CountDownLatch(1);
	private static RPCProcessor processor = new RPCProcessorImpl();
	public static void main(String[] args) {
		
		boolean isCluster = suitCommonLine(args);
		initEnvriment();
		System.setProperty("org.jboss.netty.epollBugWorkaround", "true");
		if(isCluster){
			try {
				locks.await();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		//启动与终端对接的服务端  因为是阻塞运行 需要开线程启动---后续版本中会变动
		new Thread(new Runnable() {
			
			public void run() {
				// TODO Auto-generated method stub
				System.out.println(String.format("网关开始提供终端连接服务，端口号为：%s", gatePort));
				Server4Terminal.bindAddress(Server4Terminal.config(),gatePort);
			}
		},"gateS2tmnlThread").start();
		//启动与前置对接的客户端  因为是阻塞运行 需要开线程启动
		
		for (String masterAddr : masterAddrs) {
			new Thread(new Runnable() {
				
				public void run() {
					try {
						Client2Master.bindAddress2Client(Client2Master.configClient(),masterAddr,8888);
						System.out.println(String.format("连接前置服务%s成功,前置端口必须为8888", masterAddr));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			},"gate2masterThread").start();
		}
		
		try {
			processor.exportService();
		} catch (Exception e) {
			e.printStackTrace();
		}
		addHook();
	}
	
	
	/**
	 * 命令行
	 */
	public static boolean suitCommonLine(String[] args){
		
		commandLine =
				 CommonUtil.parseCmdLine("iotGateServer", args, CommonUtil.buildCommandlineOptions(new Options()),
                    new PosixParser());
        if (null == commandLine) {
            System.exit(-1);
        }
		boolean isCluster = false;
        if(commandLine.hasOption("c") && commandLine.hasOption("z")){
        	isCluster = true;
        	zkAddr = commandLine.getOptionValue("z");
        	new ZKFramework().start(zkAddr);
        }else if (commandLine.hasOption("m")) {
        	String[] vals =  commandLine.getOptionValue("m").split("\\,");
        	for (String string : vals) {
        		masterAddrs.add(string);
			}
       	 
        }else{
        	System.err.println("启动参数有误，请重新启动");
        	System.exit(-1);
        }
        
        CommonUtil.gateNum = Integer.parseInt(commandLine.getOptionValue("n"));
        System.out.println(String.format("网关编号为：%s", CommonUtil.gateNum));
        if(commandLine.hasOption("p")){
        	gatePort = Integer.parseInt(commandLine.getOptionValue("p"));
   	 	}
        return isCluster;
	}
	/**
	 * 环境初始化  ---目前最还先不用spring管理
	 */
	public static  void initEnvriment(){
		
		ExecutorService exService = Executors.newFixedThreadPool(1);
		
		//初始化  网关终端端 --->  网关前置端   搬运数据线程
		exService.execute(new TServer2MClient(CacheQueue.up2MasterQueue));
	}
	/**
	 * JVM的关闭钩子--JVM正常关闭才会执行
	 */
	public static void addHook(){
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			
			public void run() {
				//清空缓存信息
				System.out.println("网关正常关闭前执行  清空所有缓存信息...............................");
				ClientChannelCache.clearAll();
				CacheQueue.clearIpCountRelationCache();
				CacheQueue.clearMasterChannelCache();
			}
		}));
	}

}
