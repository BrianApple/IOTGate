package gate;

import java.io.BufferedReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import gate.base.cache.ClientChannelCache;
import gate.base.cache.ProtocalStrategyCache;
import gate.base.chachequeue.CacheQueue;
import gate.client.Client2Master;
import gate.cluster.ZKFramework;
import gate.concurrent.ThreadFactoryImpl;
import gate.rpc.rpcProcessor.RPCProcessor;
import gate.rpc.rpcProcessor.RPCProcessorImpl;
import gate.server.Server4Terminal;
import gate.threadWorkers.MClient2Tmnl;
import gate.threadWorkers.TServer2MClient;
import gate.util.BannerUtil;
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
	private static String[] protocolType;
	public static void main(String[] args) {
		
		boolean isCluster = suitCommonLine(args);
		BannerUtil.info();
		System.setProperty("org.jboss.netty.epollBugWorkaround", "true");
		initEnvriment();
		if(isCluster){
			try {
				locks.await();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}else{
			//启动与前置对接的客户端  因为是阻塞运行 需要开线程启动
			
			for(int i = 0 ; i < masterAddrs.size() ; i++){
				String addr = masterAddrs.get(i);
				new Thread(new Runnable() {
					public void run() {
						try {
							Client2Master client2Master = new Client2Master();
							client2Master.bindAddress2Client(client2Master.configClient(addr,8888,true));
							
						} catch (Exception e) {
							e.printStackTrace();
							System.exit(-1);
						}
					}
				},"gate2masterThread_ip_"+addr).start();
			}
		}
		
		/**
		 * 后面这部分有点low  将就一下吧，启动过程无所谓了.....O(∩_∩)O哈哈~
		 */
		
		for(int i = 0 ; i < protocolType.length ; i++){
			//启动与终端对接的服务端  因为是阻塞运行 需要开线程启动---后续版本中会变动
			String pts =  protocolType[i];
			String pid = pts.split("\\,")[0];//pId
			
			new Thread(new Runnable() {
				public void run() {
					// TODO Auto-generated method stub
					
					String[] pt = pts.split("\\,");
					boolean isBigEndian = "0".equals(pt[1]) ? false : true;
					boolean isDataLenthIncludeLenthFieldLenth = "0".equals(pt[5]) ? false : true;
					System.out.println(String.format("！！！网关开始提供规约类型为%s的终端连接服务，开启端口号为：%s，心跳周期为：%s S", Integer.parseInt(pt[0]),Integer.parseInt(pt[7]),Integer.parseInt(pt[8])));
					Server4Terminal server4Terminal = new Server4Terminal(pt[0],pt[7]);
					server4Terminal.bindAddress(server4Terminal.config(Integer.parseInt(pt[0]),isBigEndian,Integer.parseInt(pt[2]),
							Integer.parseInt(pt[3]),Integer.parseInt(pt[4]),isDataLenthIncludeLenthFieldLenth,Integer.parseInt(pt[6]),Integer.parseInt(pt[8])));//1, false, -1, 1, 2, true, 1
					
				}
			},"gate2tmnlThread_pid_"+pid).start();
			ProtocalStrategyCache.protocalStrategyCache.put(pid, pts);
		}		
		
		try {
			processor.exportService();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("rpc服务发布失败...............");
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
        String confFile = commandLine.getOptionValue("f");
        protocolType = getProtocolType(confFile);
        
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
		
		
		//初始化数据中转线程
		try {
			new TServer2MClient(CacheQueue.up2MasterQueue,1).start();
			new MClient2Tmnl(CacheQueue.down2TmnlQueue, 1).start();
		} catch (Exception e) {
			System.err.println("数据中转线程启动失败");
			e.printStackTrace();
			System.exit(-1);
		};
		
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

	
	@SuppressWarnings("resource")
	public static String[] getProtocolType(String filePath){
		File conf=  new File(filePath);
		System.setProperty("BasicDir",conf.getParent() );
		BufferedReader bufferedReader =null;
        try {
        	bufferedReader = new BufferedReader(new FileReader(conf));
        	String str;
        	while((str = bufferedReader.readLine()) != null){
        		if(str.startsWith("protocolType")){
        			return str.split("\\=")[1].split(";");
        		}
            }
        	
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.println("配置文件加载失败");
        	System.exit(-1);
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				bufferedReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
        return null;
        
	}
}
