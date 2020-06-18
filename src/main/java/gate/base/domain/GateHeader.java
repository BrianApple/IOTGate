package gate.base.domain;

import java.util.Arrays;

/**
 * 网关头对象  A8开头的
 * @author BriansPC
 *
 */
public class GateHeader {
	private boolean inBigEndian = false;//默认小端
	private byte[] dataBuffer;//真实存放数据的地方
	private int pos = 0;//自定义指针即对应数组下标
	
	private final static int INCREASE_DATA_SIZE = 30;
	
	/**
	 * 默认构造
	 */
	public GateHeader() {
		this(INCREASE_DATA_SIZE);
	}
	/**
	 * 指定初始化字节数组大小
	 * @param size
	 */
	public GateHeader(int size) {
		this.dataBuffer = new byte[size];
	}
	/**
	 * byte
	 * @param data
	 * @throws Exception
	 */
	public void writeInt8(byte data) throws Exception {
		ensureCapacity(this.pos + 1);
		this.dataBuffer[this.pos] = data;
		this.pos += 1;
	}
	
	/**
	 * short
	 * @param i
	 * @throws Exception
	 */
	public void writeInt16(int i) throws Exception {
		ensureCapacity(this.pos + 2);
		if (this.inBigEndian) {
			this.dataBuffer[this.pos] = (byte) (i >>> 8 & 0xFF);
			this.dataBuffer[(this.pos + 1)] = (byte) (i & 0xFF);
		} else {
			this.dataBuffer[this.pos] = (byte) (i & 0xFF);
			this.dataBuffer[(this.pos + 1)] = (byte) (i >>> 8 & 0xFF);
		}
		this.pos += 2;
	}
	/**
	 * int
	 * @param i
	 * @throws Exception
	 */
	public void writeInt32(int i) throws Exception {
		ensureCapacity(this.pos + 4);
		if (this.inBigEndian) {
			this.dataBuffer[this.pos] = (byte) (i >>> 24 & 0xFF);
			this.dataBuffer[(this.pos + 1)] = (byte) (i >>> 16 & 0xFF);
			this.dataBuffer[(this.pos + 2)] = (byte) (i >>> 8 & 0xFF);
			this.dataBuffer[(this.pos + 3)] = (byte) (i & 0xFF);
		} else {
			this.dataBuffer[this.pos] = (byte) (i & 0xFF);
			this.dataBuffer[(this.pos + 1)] = (byte) (i >>> 8 & 0xFF);
			this.dataBuffer[(this.pos + 2)] = (byte) (i >>> 16 & 0xFF);
			this.dataBuffer[(this.pos + 3)] = (byte) (i >>> 24 & 0xFF);
		}
		this.pos += 4;
	}
	/**
	 * long
	 * @param l
	 * @throws Exception
	 */
	public void writeInt64(long l) throws Exception {
		ensureCapacity(this.pos + 8);
		if (this.inBigEndian) {
			this.dataBuffer[this.pos] = (byte) (int) (l >>> 56 & 0xFF);
			this.dataBuffer[(this.pos + 1)] = (byte) (int) (l >>> 48 & 0xFF);
			this.dataBuffer[(this.pos + 2)] = (byte) (int) (l >>> 40 & 0xFF);
			this.dataBuffer[(this.pos + 3)] = (byte) (int) (l >>> 32 & 0xFF);
			this.dataBuffer[(this.pos + 4)] = (byte) (int) (l >>> 24 & 0xFF);
			this.dataBuffer[(this.pos + 5)] = (byte) (int) (l >>> 16 & 0xFF);
			this.dataBuffer[(this.pos + 6)] = (byte) (int) (l >>> 8 & 0xFF);
			this.dataBuffer[(this.pos + 7)] = (byte) (int) (l & 0xFF);
		} else {
			this.dataBuffer[this.pos] = (byte) (int) (l & 0xFF);
			this.dataBuffer[(this.pos + 1)] = (byte) (int) (l >>> 8 & 0xFF);
			this.dataBuffer[(this.pos + 2)] = (byte) (int) (l >>> 16 & 0xFF);
			this.dataBuffer[(this.pos + 3)] = (byte) (int) (l >>> 24 & 0xFF);
			this.dataBuffer[(this.pos + 4)] = (byte) (int) (l >>> 32 & 0xFF);
			this.dataBuffer[(this.pos + 5)] = (byte) (int) (l >>> 40 & 0xFF);
			this.dataBuffer[(this.pos + 6)] = (byte) (int) (l >>> 48 & 0xFF);
			this.dataBuffer[(this.pos + 7)] = (byte) (int) (l >>> 56 & 0xFF);
		}
		this.pos += 8;
	}

	/**
	 * 当字节数组不够用时以指定单位扩容
	 * @param minCapacity
	 * @throws Exception
	 */
	public void ensureCapacity(int minCapacity) throws Exception {
		if (this.dataBuffer.length < minCapacity) {
			int nextBufSize = INCREASE_DATA_SIZE
					* (minCapacity / INCREASE_DATA_SIZE + 1);
			byte[] data = new byte[nextBufSize];
			System.arraycopy(this.dataBuffer, 0, data, 0,
					this.dataBuffer.length);
			this.dataBuffer = data;
		}
	}
	public boolean isInBigEndian() {
		return inBigEndian;
	}
	public void setInBigEndian(boolean inBigEndian) {
		this.inBigEndian = inBigEndian;
	}
	public byte[] getDataBuffer() {
		//pos的值等于dataBuffer中真实值的个数
		byte[] data = new byte[pos];
		
		System.arraycopy(this.dataBuffer, 0, data, 0, data.length);
		return data;
	}
	public void setDataBuffer(byte[] dataBuffer) {
		this.dataBuffer = dataBuffer;
	}
	public int getPos() {
		return pos;
	}
	public void setPos(int pos) {
		this.pos = pos;
	}
	
	
	
}
