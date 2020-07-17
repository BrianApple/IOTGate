package gate.util;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.buffer.UnpooledHeapByteBuf;
import io.netty.channel.EventLoopGroup;
import io.netty.util.HashedWheelTimer;
import io.netty.util.ReferenceCountUtil;
/**
 * 
 * @Description: 
 * @author  yangcheng
 * @date:   2019年3月30日
 */
public class CommonUtil {
	/**
	 * 网关编号
	 */
	public static int gateNum ;
	
	/**
	 * 使用堆内存ByteBuf，减少对外内存使用，便于通过gc垃圾回收
	 */
	private static UnpooledByteBufAllocator allocator;
	/**
	 * 计数
	 */
	public static AtomicInteger recieveCount ;
	
	public static HashedWheelTimer wheelTimer = new HashedWheelTimer() ;
	
	
	public static final String OS_NAME = System.getProperty("os.name");

    private static boolean isLinuxPlatform = false;
    private static boolean isWindowsPlatform = false;
	
	static{
		recieveCount = new AtomicInteger(0);
		allocator = UnpooledByteBufAllocator.DEFAULT;
		if (OS_NAME != null && OS_NAME.toLowerCase().indexOf("linux") >= 0) {
            isLinuxPlatform = true;
        }
        if (OS_NAME != null && OS_NAME.toLowerCase().indexOf("windows") >= 0) {
            isWindowsPlatform = true;
        }
	}
	
	public static boolean isLinuxPlatform() {
        return isLinuxPlatform;
    }


    public static boolean isWindowsPlatform() {
        return isWindowsPlatform;
    }
	
	/**
	 * 从直接内存池中获取ByteBuf
	 * @return
	 */
	public static ByteBuf getByteBuf(){
				
		return allocator.heapBuffer();
	}
	
	/**
	 * 释放ByteBuf
	 * @param buf
	 */
	public static void releaseByteBuf(ByteBuf buf){
		if(buf != null ){
			ReferenceCountUtil.release(buf);
		}
		
	}
	
	/**
	 * 关闭EventLoopGroup
	 * @param group
	 */
	public static void closeEventLoop(EventLoopGroup...  group ){
		for (EventLoopGroup eventLoopGroup : group) {
			eventLoopGroup.shutdownGracefully();
		}
	}
	
	
	public static Options buildCommandlineOptions(final Options options) {
		
		Option opt = new Option("n", true, "gate num");
        opt.setRequired(true);
        options.addOption(opt);
		
//		opt = new Option("p", true, "gate port ,defualt 9811");
//        opt.setRequired(false);
//        options.addOption(opt);
        
        
		
        opt = new Option("c", false, "open Cluster");
        opt.setRequired(false);
        options.addOption(opt);

        opt =new Option("z",  true,
                    "zk address list, eg: 192.168.18.27:2181,192.168.18.27:2182,192.168.18.27:2183");
        opt.setRequired(false);
        options.addOption(opt);
        /**
         * 如果未启用zookeeper集群的话则手动传入master地址
         */
        opt = new Option("m", true, "master addr, eg 127.0.0.1,127.0.0.1");
        opt.setRequired(false);
        options.addOption(opt);
        
        opt = new Option("f", true, "cache file url: eg win:'D:\\iotGate.conf'  ; linux: '/gate/iotGate.conf'");
        opt.setRequired(true);
        options.addOption(opt);
        
        opt = new Option("h", false, "help info");
        opt.setRequired(false);
        options.addOption(opt);
        

        return options;
    }
	/**
	 * Reference from RocketMQ
	 * @param appName
	 * @param args
	 * @param options
	 * @param parser
	 * @return
	 */
	 public static CommandLine parseCmdLine(final String appName, String[] args, Options options,
	            CommandLineParser parser) {
	        HelpFormatter hf = new HelpFormatter();
	        hf.setWidth(110);
	        CommandLine commandLine = null;
	        try {
	            commandLine = parser.parse(options, args);
	            if (commandLine.hasOption('h')) {
	                hf.printHelp(appName, options, true);
	                return null;
	            }
	        }
	        catch (ParseException e) {
	            hf.printHelp(appName, options, true);
	        }

	        return commandLine;
	 }
}
