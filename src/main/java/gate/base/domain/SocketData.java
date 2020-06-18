package gate.base.domain;



import io.netty.buffer.ByteBuf;

/**
 * 报文实体
 * @Description: 
 * @author  yangcheng
 * @date:   2019年3月30日
 */
public class SocketData {
	
	private ByteBuf byteBuf;
	private int pId;
	//报文头
	private byte header;
	//报文长度
	private byte[] lenArea;
	//报文体
	private byte[] content;
	//结尾
	private byte end;
	
	
	public SocketData(ByteBuf byteBuf) {
		super();
		this.byteBuf = byteBuf;
	}
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
	public ByteBuf getByteBuf() {
		return byteBuf;
	}
	public void setByteBuf(ByteBuf byteBuf) {
		this.byteBuf = byteBuf;
	}
	public int getpId() {
		return pId;
	}
	public void setpId(int pId) {
		this.pId = pId;
	}
	
	
	
	
	

}
