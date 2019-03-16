package gate.util;


import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class MixAll {
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
			System.out.println("::::"+netInterface.getName());
			Enumeration addresses = netInterface.getInetAddresses();
			while (addresses.hasMoreElements()){
				ip = (InetAddress) addresses.nextElement();
				System.out.println("：：：：：：：：：：："+ip.getHostAddress());
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
						System.out.println("本机的IPV6 = " + ip.getHostAddress());
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
	
}
