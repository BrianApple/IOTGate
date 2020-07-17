package gate.util;


import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.util.DeserializeBeanInfo;
/**
 * 
 * @Description: 
 * @author  yangcheng
 * @date:   2019年3月30日
 */
public class MixAll {
	private static final String CLASS_SUFFIX = ".class";
	
	private static final String CLASS_FILE_PREFIX = File.separator + "classes"  + File.separator;
	
	private static final String PACKAGE_SEPARATOR = ".";
	
	private MixAll(){
		throw new AssertionError();
	}
	public static List<String> inetAddressList ;
	static{
		inetAddressList = getLocalInetAddress();
	}
	
	public static List<String> getLocalInetAddress() {
        List<String> inetAddressList = new ArrayList<String>();
        try {
            Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
            while (enumeration.hasMoreElements()) {
                NetworkInterface networkInterface = enumeration.nextElement();
                Enumeration<InetAddress> addrs = networkInterface.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    inetAddressList.add(addrs.nextElement().getHostAddress());
                }
            }
        }
        catch (SocketException e) {
            throw new RuntimeException("get local inet address fail", e);
        }

        return inetAddressList;
    }
	/**
	 * 获取程序进程号
	 * @return
	 */
	public static long getPID() {
	    String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
	    if (processName != null && processName.length() > 0) {
	        try {
	            return Long.parseLong(processName.split("@")[0]);
	        }
	        catch (Exception e) {
	            return 0;
	        }
	    }
	
	    return 0;
	}
	/**
	 * 获取本机的ip--linux平台上获取的ip会存在问题，拿到的是127.0.0.1
	 * @return
	 */
	public static String localhostName() {
	    try {
	        return InetAddress.getLocalHost().getHostAddress();
	    }
	    catch (Throwable e) {
	        throw new RuntimeException(
	            "InetAddress java.net.InetAddress.getLocalHost() throws UnknownHostException"
	        		, e);
	    }
	}
	/**
	 * 内网IP
	 * @return
	 * @throws SocketException
	 */
	
	public static String linuxLocalIP() throws SocketException{
		Enumeration allNetInterfaces = NetworkInterface.getNetworkInterfaces();
		InetAddress ip = null;
		String localHostIP = null;
		List<InetAddress> cache = new ArrayList<>();
		while (allNetInterfaces.hasMoreElements()){
			NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
			Enumeration addresses = netInterface.getInetAddresses();
			while (addresses.hasMoreElements()){
				ip = (InetAddress) addresses.nextElement();
//				System.out.println("：：：：：：：：：：："+ip.getHostAddress());
				if (ip != null && ip instanceof Inet4Address){
					if(!ip.isLoopbackAddress() && ip.isSiteLocalAddress()){
						localHostIP = ip.getHostAddress();
						System.out.println("本机的IPV4 = " + ip.getHostAddress());
					}else{
						cache.add(ip);
					}
					
				} else if(ip instanceof Inet6Address){
					if(!ip.isLoopbackAddress() && ip.isSiteLocalAddress()){
						localHostIP = ip.getHostAddress();
//						System.out.println("本机的IPV6 = " + ip.getHostAddress());
					}else{
						cache.add(ip);
					}
				}
			}
		}
		
		if(localHostIP != null){
			return localHostIP;
		}else{
			for (InetAddress inetAddress : cache) {
				if(inetAddress.isSiteLocalAddress()){
					System.out.println("去本地地址ip="+inetAddress.getHostAddress());
					return inetAddress.getHostAddress();
				}
			}
		}
		return null;
	}
	
	
	
	
	
	
	
	
	
	
	/**
	 * 获取指定包下所有的class名称
	 * reference from  https://blog.csdn.net/cckevincyh/article/details/81176155
	 * @param packageName
	 * @param showChildPackageFlag
	 * @return
	 */
	public static List<String> getClazzName(String packageName, boolean showChildPackageFlag ) {

	    List<String> result = new ArrayList<>();

	    String suffixPath = packageName.replaceAll("\\.", "\\/");

	    ClassLoader loader = Thread.currentThread().getContextClassLoader();

	    try {

	        Enumeration<URL> urls = loader.getResources(suffixPath);

	        while(urls.hasMoreElements()) {

	            URL url = urls.nextElement();

	            if(url != null) {

	                String protocol = url.getProtocol();

	                if("file".equals(protocol)) {

	                    String path = url.getPath();

	                    System.out.println(path);

	                    result.addAll(getAllClassNameByFile(new File(path), showChildPackageFlag));

	                }

	            }

	        }

	    } catch (IOException e) {

	        e.printStackTrace();

	    }

	    
	    return result;

	}
	

	/**
	 * 递归获取所有class文件的名字
	 * @param file 
	 * @param flag  是否需要迭代遍历
	 * @return List
	 */
	private static List<String> getAllClassNameByFile(File file, boolean flag) {

	    List<String> result =  new ArrayList<>();

	    if(!file.exists()) {

	        return result;

	    }

	    if(file.isFile()) {

	        String path = file.getPath();

	        if(path.endsWith(CLASS_SUFFIX)) {

	            path = path.replace(CLASS_SUFFIX, "");

	            String clazzName = path.substring(path.indexOf(CLASS_FILE_PREFIX) + CLASS_FILE_PREFIX.length())

	                    .replace(File.separator, PACKAGE_SEPARATOR);

	            if(-1 == clazzName.indexOf("$")) {

	                result.add(clazzName);

	            }

	        }

	        return result;

	        

	    } else {

	        File[] listFiles = file.listFiles();

	        if(listFiles != null && listFiles.length > 0) {

	            for (File f : listFiles) {

	                if(flag) {

	                    result.addAll(getAllClassNameByFile(f, flag));

	                } else {

	                    if(f.isFile()){

	                        String path = f.getPath();

	                        if(path.endsWith(CLASS_SUFFIX)) {

	                            path = path.replace(CLASS_SUFFIX, "");

	                            // 从"/classes/"后面开始截取

	                            String clazzName = path.substring(path.indexOf(CLASS_FILE_PREFIX) + CLASS_FILE_PREFIX.length())

	                                    .replace(File.separator, PACKAGE_SEPARATOR);

	                            if(-1 == clazzName.indexOf("$")) {

	                                result.add(clazzName);

	                            }

	                        }

	                    }

	                }

	            }

	        } 

	        return result;

	    }

	}
	/**
	 * 反序列化指定对象
	 * @param data
	 * @param classOfT
	 * @return
	 * 
	 * {
	 * "args":["你好rpc"],
	 * "className":"IOTGateConsole.rpc.service.RPCExportService",
	 * "methodName":"test",
	 * "paramTyps":["java.lang.String"],
	 * "requestNum":"540bbd93-06a6-4b77-a067-fbd8d0d38f2d"
	 * }
	 */
	public static <T> T decode(final byte[] data, Class<T> classOfT) {
        final String json = new String(data, Charset.forName("UTF-8"));
        try {
        	 T  t= JSON.parseObject(json, classOfT);
        	 return t;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
        return null;
	}
	
	/**
	 * 序列化指定对象
	 * @param obj
	 * @return
	 */
	public static byte[] encode(final Object obj) {
        final String json = JSON.toJSONString(obj, false);;
        if (json != null) {
            return json.getBytes(Charset.forName("UTF-8"));
        }
        return null;
    }
	
	
	
	
}