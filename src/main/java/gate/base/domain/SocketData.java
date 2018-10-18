package gate.base.domain;

import java.io.Serializable;

import gate.util.StringUtils;

/**
 * 报文实体
 * @author BriansPC
 *
 */
public class SocketData implements Serializable{
	
	//报文头
	private byte header;
	//报文长度
	private byte[] lenArea;
	//报文体
	private byte[] content;
	//结尾16
	private byte end;
	
	public SocketData(byte header, byte[] lenArea, byte[] content, byte end) {
		super();
		this.header = header;
		this.lenArea = lenArea;
		this.content = content;
		this.end = end;
	}
	public byte getHeader() {
		return header;
	}
	public void setHeader(byte header) {
		this.header = header;
	}
	public byte[] getLenArea() {
		return lenArea;
	}
	public void setLenArea(byte[] lenArea) {
		this.lenArea = lenArea;
	}
	public byte[] getContent() {
		return content;
	}
	public void setContent(byte[] content) {
		this.content = content;
	}
	public byte getEnd() {
		return end;
	}
	public void setEnd(byte end) {
		this.end = end;
	}
	
	
	
	
	

}
