package gate.util;


import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;
/**
 * 
 * @Description: 
 * @author  yangcheng
 * @date:   2019年3月30日
 */
public class StringUtils {
	
	private static Random randGen = new Random();

	private static char[] numbersAndLetters = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
			.toCharArray();
	
	private static final char[] base64Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
			.toCharArray();

	private static final char[] upcaseHexChar = "0123456789ABCDEF"
			.toCharArray();
	
	private static final char[] lowerHexChar = "0123456789abcdef".toCharArray();

	private static int[] hexCharCodes = new int[256];

	private static int[] base64Codes = new int[256];
	
	private static SimpleDateFormat m_ISO8601Local = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss");

	static {
		for (int i = 0; i < 256; i++) {
			byte tmp128_127 = -1;
			base64Codes[i] = tmp128_127;
			hexCharCodes[i] = tmp128_127;
		}
		for (int i = 0; i < base64Chars.length; i++) {
			base64Codes[base64Chars[i]] = (byte) i;
		}
		for (int i = 0; i < upcaseHexChar.length; i++) {
			hexCharCodes[upcaseHexChar[i]] = (byte) i;
		}
		for (int i = 0; i < lowerHexChar.length; i++)
			hexCharCodes[lowerHexChar[i]] = (byte) i;
	}

	public static String getCurrTime() {
		Date now = new Date();
		SimpleDateFormat outFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		String s = outFormat.format(now);
		return s;
	}

	public static String getCurrTimeISO8601(Date date) {
		if (date == null) {
			date = new Date();
		}

		String dateStr = m_ISO8601Local.format(date);

		return dateStr.substring(0, 22) + ":" + dateStr.substring(22);
	}

	public static int getStrIndex(String s, String[] args) {
		int length = args.length;
		for (int i = 0; i < length; i++) {
			if (args[i].equals(s)) {
				return i;
			}
		}
		return -1;
	}

	public static final String replace(String line, String oldString,
			String newString) {
		return replace(line, oldString, newString, false);
	}

	public static final String replaceIgnoreCase(String line, String oldString,
			String newString) {
		return replace(line, oldString, newString, true);
	}

	public static final String replace(String line, String oldString,
			String newString, boolean ignoreCase) {
		if ((line == null) || (oldString == null) || (newString == null)) {
			return null;
		}
		String lcLine = line;
		String lcOldString = oldString;
		if (ignoreCase) {
			lcLine = line.toLowerCase();
			lcOldString = oldString.toLowerCase();
		}

		int i = 0;
		if ((i = lcLine.indexOf(lcOldString, i)) >= 0) {
			char[] line2 = line.toCharArray();
			char[] newString2 = newString.toCharArray();
			int oLength = oldString.length();
			StringBuffer buf = new StringBuffer(line2.length);
			buf.append(line2, 0, i).append(newString2);
			i += oLength;

			for (int j = i; (i = lcLine.indexOf(lcOldString, i)) > 0; j = i) {
				buf.append(line2, j, i - j).append(newString2);
				i += oLength;
			}

			buf.append(line2, i, line2.length - i);
			return buf.toString();
		}
		return line;
	}
	/**
	 * 将字节数组转成报文
	 * @param bytes
	 * @return
	 */
	public static final String encodeHex(byte[] bytes) {
		return encodeHex(bytes, true);
	}

	public static final String encodeHex(byte[] bytes, boolean isUpper) {
		char[] hexChar;
		if (isUpper)
			hexChar = upcaseHexChar;
		else {
			hexChar = lowerHexChar;
		}

		char[] buf = new char[bytes.length * 2];

		return encodeHex(bytes, buf, hexChar, 0, bytes.length);
	}

	public static final String encodeHex(byte[] bytes, char[] buf, int offset,
			int length) {
		return encodeHex(bytes, buf, upcaseHexChar, offset, length);
	}

	public static final String encodeHex(byte[] bytes, char[] buf,
			char[] hexChar, int offset, int length) {
		for (int i = 0; i < length; i++) {
			int code = bytes[(i + offset)] & 0xFF;
			buf[(2 * i)] = hexChar[(code >> 4)];
			buf[(2 * i + 1)] = hexChar[(code & 0xF)];
		}
		return new String(buf, 0, length * 2);
	}

	public static final String dumpHex(byte[] bytes) {
		int linecount = (bytes.length + 15) / 16;
		char[] buf = new char[linecount * 56];
		byte[] bs = new byte[16];

		int bytepos = 0;
		int bufpos = 0;
		for (int i = 0; i < linecount; i++) {
			int addr = i * 16;
			buf[(bufpos++)] = upcaseHexChar[(addr >> 12 & 0xF)];
			buf[(bufpos++)] = upcaseHexChar[(addr >> 8 & 0xF)];
			buf[(bufpos++)] = upcaseHexChar[(addr >> 4 & 0xF)];
			buf[(bufpos++)] = upcaseHexChar[(addr & 0xF)];
			buf[(bufpos++)] = ' ';
			buf[(bufpos++)] = ' ';
			for (int j = 0; j < 16; bytepos++) {
				if (bytepos < bytes.length) {
					int code = bytes[bytepos] & 0xFF;
					bs[j] = bytes[bytepos];
					buf[(bufpos++)] = upcaseHexChar[(code >> 4)];
					buf[(bufpos++)] = upcaseHexChar[(code & 0xF)];
					if (j == 7)
						buf[(bufpos++)] = '-';
					else
						buf[(bufpos++)] = ' ';
				} else {
					buf[(bufpos++)] = ' ';
					buf[(bufpos++)] = ' ';
					buf[(bufpos++)] = ' ';
					bs[j] = 32;
				}
				j++;
			}

			buf[(bufpos++)] = '\r';
			buf[(bufpos++)] = '\n';
		}
		return new String(buf, 0, bufpos);
	}

	public static final byte[] decodeHex(String hex) {
		byte[] bytes = new byte[hex.length() / 2];
		decodeHex(hex, bytes);
		return bytes;
	}

	public static final int decodeHex(String hex, byte[] bytes) {
		int byteCount = 0;
		int length = hex.length();
		for (int i = 0; i < length; i += 2) {
			byte newByte = 0;
			newByte = (byte) (newByte | hexCharCodes[hex.charAt(i)]);
			newByte = (byte) (newByte << 4);
			newByte = (byte) (newByte | hexCharCodes[hex.charAt(i + 1)]);
			bytes[byteCount] = newByte;
			byteCount++;
		}

		return byteCount;
	}

	public static String encodeBase64(String data) {
		return encodeBase64(data.getBytes());
	}

	public static String encodeBase64(byte[] data, boolean lineBreak) {
		if (data == null)
			return null;
		int len = data.length;
		char[] buf = new char[(len / 3 + 1) * 4 + len / 57 + 1];
		int pos = 0;
		for (int i = 0; i < len; i++) {
			int c = data[i] >> 2 & 0x3F;
			buf[(pos++)] = base64Chars[c];
			c = data[i] << 4 & 0x3F;
			i++;
			if (i < len) {
				c |= data[i] >> 4 & 0xF;
			}
			buf[(pos++)] = base64Chars[c];
			if (i < len) {
				c = data[i] << 2 & 0x3F;
				i++;
				if (i < len) {
					c |= data[i] >> 6 & 0x3;
				}
				buf[(pos++)] = base64Chars[c];
			} else {
				i++;
				buf[(pos++)] = '=';
			}
			if (i < len) {
				c = data[i] & 0x3F;
				buf[(pos++)] = base64Chars[c];
			} else {
				buf[(pos++)] = '=';
			}

			if ((lineBreak) && (i % 57 == 56)) {
				buf[(pos++)] = '\n';
			}
		}
		if ((pos > 0) && (lineBreak) && (buf[(pos - 1)] != '\n'))
			buf[pos] = '\n';
		return new String(buf, 0, pos);
	}

	public static String encodeBase64(byte[] data) {
		return encodeBase64(data, false);
	}

	public static byte[] decodeBase64(String data) {
		return decodeBase64(data, 0);
	}

	public static byte[] decodeBase64(String data, int offset) {
		if (data == null)
			return null;
		int len = data.length();
		byte[] result = new byte[len * 3 / 4];
		int pos = 0;
		if (offset >= len) {
			return null;
		}
		for (int i = offset; i < len; i++) {
			int c = base64Codes[data.charAt(i)];
			if (c == -1) {
				continue;
			}
			i++;
			int c1 = base64Codes[data.charAt(i)];
			c = c << 2 | c1 >> 4 & 0x3;
			result[(pos++)] = (byte) c;
			i++;
			if (i < len) {
				c = data.charAt(i);
				if (61 == c) {
					break;
				}
				c = base64Codes[data.charAt(i)];
				c1 = c1 << 4 & 0xF0 | c >> 2 & 0xF;
				result[(pos++)] = (byte) c1;
			}
			i++;
			if (i >= len) {
				continue;
			}
			c1 = data.charAt(i);
			if (61 == c1) {
				break;
			}
			c1 = base64Codes[data.charAt(i)];
			c = c << 6 & 0xC0 | c1;
			result[(pos++)] = (byte) c;
		}
		if (result.length != pos) {
			byte[] result2 = new byte[pos];
			System.arraycopy(result, 0, result2, 0, pos);
			result = result2;
		}
		return result;
	}

	public static char randomChar() {
		return numbersAndLetters[randGen.nextInt(numbersAndLetters.length)];
	}

	public static final String randomString(int length) {
		if (length < 1) {
			return null;
		}
		char[] randBuffer = new char[length];
		for (int i = 0; i < randBuffer.length; i++) {
			randBuffer[i] = randomChar();
		}

		return new String(randBuffer);
	}

	public static String genEmptyString(int length) {
		char[] cs = new char[length];
		for (int i = 0; i < length; i++) {
			cs[i] = ' ';
		}
		return new String(cs);
	}

	public static boolean nullOrBlank(String param) {
		return (param == null) || (param.trim().equals(""));
	}

	public static int parseInt(String param, int defValue) {
		int i = defValue;
		try {
			i = Integer.parseInt(param);
		} catch (Exception localException) {
		}
		return i;
	}

	public static long parseLong(String param, long defValue) {
		long l = defValue;
		try {
			l = Long.parseLong(param);
		} catch (Exception localException) {
		}
		return l;
	}

	public static float parseFloat(String param, float defValue) {
		float f = defValue;
		try {
			f = Float.parseFloat(param);
		} catch (Exception localException) {
		}
		return f;
	}

	public static double parseDouble(String param, double defValue) {
		double d = defValue;
		try {
			d = Double.parseDouble(param);
		} catch (Exception localException) {
		}
		return d;
	}

	public static boolean parseBoolean(String param) {
		return parseBoolean(param, false);
	}

	public static boolean parseBoolean(String param, boolean value) {
		if (nullOrBlank(param)) {
			return value;
		}
		switch (param.charAt(0)) {
		case '1':
		case 'T':
		case 'Y':
		case 't':
		case 'y':
			return true;
		case '0':
		case 'F':
		case 'N':
		case 'f':
		case 'n':
			return false;
		}
		return value;
	}

	public static String encodeUrlString(String s) {
		return encodeUrlString(s, true);
	}

	public static String encodeUrlString(String s, boolean isUpper) {
		char[] hexChar;
		if (isUpper)
			hexChar = upcaseHexChar;
		else {
			hexChar = lowerHexChar;
		}

		StringBuffer sbuf = new StringBuffer();
		int len = s.length();
		for (int i = 0; i < len; i++) {
			int ch = s.charAt(i);
			if (ch == 32) {
				sbuf.append('+');
			} else if ((65 <= ch) && (ch <= 90)) {
				sbuf.append((char) ch);
			} else if ((97 <= ch) && (ch <= 122)) {
				sbuf.append((char) ch);
			} else if ((48 <= ch) && (ch <= 57)) {
				sbuf.append((char) ch);
			} else if ((ch == 45) || (ch == 95) || (ch == 46) || (ch == 33)
					|| (ch == 126) || (ch == 42) || (ch == 39) || (ch == 40)
					|| (ch == 41)) {
				sbuf.append((char) ch);
			} else if (ch <= 127) {
				sbuf.append('%');
				sbuf.append(hexChar[(ch >>> 4 & 0xF)]);
				sbuf.append(hexChar[(ch & 0xF)]);
			} else {
				sbuf.append('%');
				sbuf.append('u');
				sbuf.append(hexChar[(ch >>> 12 & 0xF)]);
				sbuf.append(hexChar[(ch >>> 8 & 0xF)]);
				sbuf.append(hexChar[(ch >>> 4 & 0xF)]);
				sbuf.append(hexChar[(ch & 0xF)]);
			}
		}
		return sbuf.toString();
	}

	public static String decodeUrlString(String s) {
		StringBuffer sbuf = new StringBuffer();
		int i = 0;
		int len = s.length();
		while (i < len) {
			int ch = s.charAt(i);
			if (ch == 43) {
				sbuf.append(' ');
			} else if ((65 <= ch) && (ch <= 90)) {
				sbuf.append((char) ch);
			} else if ((97 <= ch) && (ch <= 122)) {
				sbuf.append((char) ch);
			} else if ((48 <= ch) && (ch <= 57)) {
				sbuf.append((char) ch);
			} else if ((ch == 45) || (ch == 95) || (ch == 46) || (ch == 33)
					|| (ch == 126) || (ch == 42) || (ch == 39) || (ch == 40)
					|| (ch == 41)) {
				sbuf.append((char) ch);
			} else if ((ch == 37) && (i < s.length() - 1)) {
				int cint = 0;
				if (('%' == s.charAt(i + 1)) || ('\'' == s.charAt(i + 1))) {
					sbuf.append((char) ch);
				} else {
					if ('u' != s.charAt(i + 1)) {
						cint = cint << 4 | hexCharCodes[s.charAt(i + 1)];
						cint = cint << 4 | hexCharCodes[s.charAt(i + 2)];
						i += 2;
					} else {
						cint = cint << 4 | hexCharCodes[s.charAt(i + 2)];
						cint = cint << 4 | hexCharCodes[s.charAt(i + 3)];
						cint = cint << 4 | hexCharCodes[s.charAt(i + 4)];
						cint = cint << 4 | hexCharCodes[s.charAt(i + 5)];
						i += 5;
					}
					sbuf.append((char) cint);
				}
			} else {
				sbuf.append((char) ch);
			}
			i++;
		}
		return sbuf.toString();
	}

	public static String join(Object[] list, String separator) {
		return join(Arrays.asList(list).iterator(), separator);
	}

	@SuppressWarnings("rawtypes")
	public static String join(Iterator iterator, String separator) {
		StringBuffer buf = new StringBuffer();
		while (iterator.hasNext()) {
			buf.append(iterator.next());
			if (iterator.hasNext()) {
				buf.append(separator);
			}
		}
		return buf.toString();
	}

	public static String toEncoding(String strvalue, String fromEncoding,
			String toEncoding) {
		try {
			if (strvalue == null) {
				return null;
			}
			strvalue = new String(strvalue.getBytes(fromEncoding), toEncoding);
			return strvalue;
		} catch (Exception e) {
		}
		return null;
	}

	public static String toChinese(String strvalue) {
		return toEncoding(strvalue, "ISO-8859-1", "GBK");
	}

	public static String toLatin(String strvalue) {
		return toEncoding(strvalue, "GBK", "ISO-8859-1");
	}

	public static int compareString(String s, String s2, String encoding) {
		if (s2 == null) {
			if (s != null) {
				return 1;
			}
			return 0;
		}
		if (s == null) {
			return -1;
		}
		try {
			byte[] v1 = s.getBytes(encoding);
			byte[] v2 = s2.getBytes(encoding);
			int i = v1.length;
			int j = v2.length;
			int n = Math.min(i, j);
			int k = 0;
			int lim = n;
			while (k < lim) {
				int c1 = v1[k] & 0xFF;
				int c2 = v2[k] & 0xFF;
				if (c1 != c2) {
					return c1 - c2;
				}
				k++;
			}

			return i - j;
		} catch (Exception e) {
		}
		return 0;
	}

	public static String escapeString(String str) {
		if (str == null) {
			return null;
		}
		int len = str.length();
		if (len == 0) {
			return str;
		}
		StringBuffer buf = new StringBuffer(len * 2);

		for (int i = 0; i < len; i++) {
			char ch = str.charAt(i);
			switch (ch) {
			case '\b':
				buf.append("\\b");
				break;
			case '\f':
				buf.append("\\f");
				break;
			case '\\':
				buf.append("\\\\");
				break;
			case '\t':
				buf.append("\\t");
				break;
			case '\n':
				buf.append("\\n");
				break;
			case '\r':
				buf.append("\\r");
				break;
			case '\000':
				buf.append("\\0");
				break;
			default:
				if (((ch >= 'a') && (ch <= 'z'))
						|| ((ch >= 'A') && (ch <= 'Z'))
						|| ((ch >= '0') && (ch <= '9'))) {
					buf.append(ch);
				} else if (ch > 'Ā') {
					buf.append('\\');
					buf.append('u');
					buf.append(upcaseHexChar[(ch >> '\f' & 0xF)]);
					buf.append(upcaseHexChar[(ch >> '\b' & 0xF)]);
					buf.append(upcaseHexChar[(ch >> '\004' & 0xF)]);
					buf.append(upcaseHexChar[(ch >> '\000' & 0xF)]);
				} else {
					buf.append('\\');
					buf.append('x');
					buf.append(upcaseHexChar[(ch >> '\004' & 0xF)]);
					buf.append(upcaseHexChar[(ch >> '\000' & 0xF)]);
				}
			}
		}

		return buf.toString();
	}

	public static boolean strEquals(String s1, String s2) {
		if ((s1 != null) && (s2 != null)) {
			return s1.equals(s2);
		}
		return s1 == s2;
	}

	public static String trimToByteSize(String s, String encoding, int btyeSize) {
		if (s != null) {
			int pos = 0;
			int charSize = 2;
			int strlen = s.length();
			if (encoding.charAt(0) == 'U') {
				charSize = 3;
			}
			for (int i = 0; i < strlen; i++) {
				if (pos > btyeSize) {
					return s.substring(0, i - 1);
				}

				if ((s.charAt(i) & 0xFFFFFF00) != 0)
					pos += charSize;
				else {
					pos++;
				}
			}
		}
		return s;
	}

	public static boolean objectEquals(Object param0, Object pram1) {
		if ((param0 != null) && (pram1 != null)) {
			return param0.equals(pram1);
		}
		return (param0 == null) && (pram1 == null);
	}
	
	public static String formatIpAddress(String ip,String port){
		return String.format("%s|%s",ip.trim(),port.trim());
	}
}
