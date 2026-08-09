package gate;

import java.awt.image.Kernel;
import java.io.BufferedReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import gate.base.cache.ClientChannelCache;
import gate.base.cache.ProtocalStrategyCache;
import gate.base.chachequeue.CacheQueue;
import gate.client.Client2Master;
import gate.concurrent.ThreadFactoryImpl;
import gate.register.HttpRegisterClient;
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
	public static List<String> masterAddrs = new ArrayList<>(1);
	private static RPCProcessor processor = new RPCProcessorImpl();
	private static String[] protocolType;
	/**
	 * 前端管理服务(Console)地址：-c 参数配合 -r 指定，网关启动后主动注册
	 */
	public static String consoleAddr = null;
	/**
	 * 网关注册客户端：-c 模式启动，JVM关闭钩子中反注册
	 */
	public static HttpRegisterClient registerClient = null;

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		boolean registerToConsole = suitCommonLine(args);
		BannerUtil.info();
		System.setProperty("org.jboss.netty.epollBugWorkaround", "true");
		initEnvriment();
		if(registerToConsole){
			try {
				//-c 参数语义：主动注册到前端管理服务(Console)；
				//master 数据通道仍通过 -m 直连前置，发布rpc服务供Console管理
				startCli();
				processor.exportService();
				registerClient = new HttpRegisterClient(consoleAddr, CommonUtil.gateNum);
				registerClient.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			startCli();
		}
		startSev( protocolType);	
		/**
		 * kill pid时 该方法会自动执行
		 */
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
		boolean registerToConsole = false;
        //这里我就不搞一堆设计模式了，特此声明！
        //-c 参数语义：主动注册到前端管理服务(Console)，配合 -r 指定Console地址(支持 ip / ip:port，默认端口8686)
        if(commandLine.hasOption("c")){
        	String rArg = commandLine.getOptionValue("r");
        	if (null == rArg || rArg.trim().isEmpty()) {
        		System.err.println("启动参数有误：-c 需配合 -r 指定前端管理服务(Console)地址，如 -c -r 192.168.1.10:8686");
        		System.exit(-1);
        	}
        	consoleAddr = rArg.trim();
        	registerToConsole = true;
        }
        //master 数据通道直连前置(8888)，逻辑零改动
        if(commandLine.hasOption("m")) {
			String  mArg = commandLine.getOptionValue("m");
			if (null != mArg){
				String[] vals =  mArg.split("\\,");
				for (String string : vals) {
					masterAddrs.add(string);
				}
			}

        } else if (commandLine.hasOption("k")) {
            CommonUtil.kernelPort = 10915;
        } else if (!registerToConsole) {
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
        return registerToConsole;
	}
	/**
	 * 环境初始化
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
				//主动向Console反注册(尽力而为，失败由心跳超时判定兜底)
				if(registerClient != null){
					registerClient.unregister();
				}
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
	
	public static void startCli(){
        if (-1 != CommonUtil.kernelPort ){
            //  当kernel模式启动服务时，client访问前置方式自动关闭
            return;
        }
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
			},"gate2masterThread_client_ip_"+addr).start();
		}
	}
	
	public static void startSev(String... protocolType){
		int size = protocolType.length;
		if (-1 != CommonUtil.kernelPort ){
			new Thread(new Runnable() {
				public void run() {
					System.out.println(String.format("！！！网关kernel模式开启，服务端口号为：%s，心跳周期为：%sH", CommonUtil.kernelPort,1));
					Server4Terminal server4Terminal = new Server4Terminal(String.valueOf(CommonUtil.kernelPort));
					server4Terminal.bindAddress(server4Terminal.config());
				}
			},"gate2masterThread_kernel_0").start();
		}
		for(int i = 0 ; i <size  ; i++){
			//启动与终端对接的服务端  因为是阻塞运行 需要开线程启动---后续版本中会变动
			String pts =  protocolType[i];
			String pid = pts.split("\\,")[0];//pId

			new Thread(new Runnable() {
				public void run() {
					// TODO Auto-generated method stub
					
					String[] pt = pts.split("\\,");
					boolean isBigEndian = "0".equals(pt[1]) ? false : true;
					boolean isDataLenthIncludeLenthFieldLenth = "0".equals(pt[5]) ? false : true;
					System.out.println(String.format("！！！网关开始提供规约类型为%s的终端连接服务，开启端口号为：%s，心跳周期为：%sS", Integer.parseInt(pt[0]),Integer.parseInt(pt[7]),Integer.parseInt(pt[8])));
					Server4Terminal server4Terminal = new Server4Terminal(pt[0],pt[7]);
					server4Terminal.bindAddress(server4Terminal.config(Integer.parseInt(pt[0]),isBigEndian,Integer.parseInt(pt[2]),
							Integer.parseInt(pt[3]),Integer.parseInt(pt[4]),isDataLenthIncludeLenthFieldLenth,Integer.parseInt(pt[6]),Integer.parseInt(pt[8])));//1, false, -1, 1, 2, true, 1
					
				}
			},"gate2tmnlThread_pid_"+pid).start();
			ProtocalStrategyCache.protocalStrategyCache.put(pid, pts);
		}		
	}
}
