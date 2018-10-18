package gate.base.domain;

import java.io.Serializable;
/**
 * 一个客户端请求所对应的所有有用数据对想
 */
public class ChannelData implements Serializable{
	private static final long serialVersionUID = -7164069554228806843L;
	/**
	 * 通道的远程ip地址
	 */
	private String ipAddress;
	private SocketData socketData;
	/**
	 *根据SocketData创建对象 
	 */
	public ChannelData(SocketData socketData) {
		
		this(null,socketData);
	}
	
	public ChannelData(String ipAddress, SocketData socketData) {
		super();
		this.ipAddress = ipAddress;
		this.socketData = socketData;
	}
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	public SocketData getSocketData() {
		return socketData;
	}
	public void setSocketData(SocketData socketData) {
		this.socketData = socketData;
	}
	
}
