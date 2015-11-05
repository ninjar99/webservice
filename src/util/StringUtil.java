package util;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * String Utility Class including String related manipulation. <br/>
 * And all methods that return a String class comply with the rule that, if
 * parameter String is null, the result will be null.<br/>
 * But if the return type is other than String, the return may be varied.
 * 
 * @author sherlockq
 * 
 */
public final class StringUtil {

	/**
	 * The separator for split and join.
	 */
	public static final char SEPARATOR = '|';

	/**
	 * Returns a blank string while given string is null.
	 * 
	 * @param value
	 *            string value to be avoid.
	 * @return blank string while null, other original string value.
	 */
	public static String avoidNull(String value) {
		return (value == null) ? "" : value;
	}

	/**
	 * Parse exception's stack trace into a String.
	 * 
	 * @param e
	 *            the exception
	 * @return Stack trace in a String.
	 */
	public static String exceptionStackTrace(Throwable e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		String returnStr = "";
		if (null != e) {
			e.printStackTrace(pw);
			pw.flush();
			pw.close();
			returnStr = sw.toString();
		}
		return returnStr;
	}

	/**
	 * Returns the byte length in given charset of the String.
	 * 
	 * @param value
	 * @param charset
	 *            Charset under which to measure.
	 * @return Returns 0 if String is null, or the charset is not supported.
	 * @see CharsetConstant
	 */
	public static int getByteLength(String value, String charset) {
		if (value == null) {
			return 0;
		} else {
			try {
				return value.getBytes(charset).length;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return 0;
			}
		}

	}

	/**
	 * Returns object's toString() result if it's not null. Return an empty
	 * String if it's null.
	 * 
	 * @param obj
	 *            object.
	 * @return string
	 */
	public static String getObjString(Object obj) {
		if (null == obj)
			return "";
		else
			return obj.toString();
	}

	/**
	 * Returns the byte length in UTF8 of the String.<br/>
	 * Generally, Chinese characters are three bytes each, ASCII characters are
	 * one byte each.
	 * 
	 * @param value
	 * @return Returns 0 if String is null.
	 * @see CharsetConstant
	 */
	public static int getUTF8Length(String value) {

		return getByteLength(value, CharsetConstant.UTF_8);

	}

	/**
	 * Check whether given string value is null or a blank string (trimmed).
	 * 
	 * @param value
	 *            string value to be checked.
	 * @return true if string value is null or blank, otherwise false.
	 */
	public static boolean isNullOrBlank(String value) {
		return value == null || "".equals(value.trim());
	}

	/**
	 * Join collection's toString() into one String split with "\|". <br/>
	 * A null element of the collection will leave a empty string in the result. <br/>
	 * Process has been taken to be compatible to separator included String: If
	 * there is \ in the String, it'll be replaced with double \.
	 * 
	 * @param objects
	 * @return If collection has no elements return null. The separator should
	 *         appear the times same to collection's count.
	 * @see URLUtil#split(String)
	 */
	public static String join(Object[] objects) {

		if (objects == null || objects.length == 0) {
			return null;
		}

		StringBuffer sb = new StringBuffer();
		for (Object object : objects) {
			if (object != null) {

				// 首先对所有\进行规避处理，即修改为两个\
				String objString = object.toString();
				if (objString != null) {
					objString = objString.replaceAll("\\\\", // 即\�?
							Matcher.quoteReplacement("\\\\"));
					objString = objString.replaceAll("\\|", Matcher.quoteReplacement("\\|"));
					sb.append(objString);
				}
			}

			// 之后用\和分隔符作为分割
			sb.append(SEPARATOR);
		}

		// 去除�?后一个多余的分隔符，不用考虑空的情况
		return sb.toString().substring(0, sb.length() - 1);
	}

	/**
	 * Left the string by given byte-length in specified charset, the
	 * byte-length of result string should not exceed parameter
	 * <code>bytelen</code> and meanwhile should be closest to it.<br/>
	 * For example, generally Chinese character in UTF-8 occupies 3 bytes, so <br/>
	 * <br/>
	 * <code>
	 * leftByCharsetByte("中文","UTF-8",3) = "�?" <br/>
	 * leftByCharsetByte("中文","UTF-8",4) = "�?" <br/>
	 * leftByCharsetByte("中文","UTF-8",6) = "中文"<br/>
	 * </code><br/>
	 * If not even a character can be returned, the result will be "" as <br/>
	 * <br/>
	 * <code>
	 * leftByCharsetByte("中文","UTF-8",1) = ""
	 * </code>
	 * 
	 * @param value
	 *            original String to left
	 * @param charset
	 *            Charset Name
	 * @param bytelen
	 *            the max length of byte to return in specified charset
	 * @return If value is null, return null, otherwise return not null String.
	 */
	public static String leftByCharsetByte(String value, String charset,
			int bytelen) {

		// 如果为NULL，则直接返回
		if (null == value) {
			return null;
		}

		try {

			Charset cs = Charset.forName(charset);
			if (cs.encode(value).limit() <= bytelen) { // 直接返回
				return value;
			}

			int byteCount = 0, stringIndex = 0;
			while (true) { // 不用考虑value总长�?
				int currentByteLen = cs.encode(
						value.substring(stringIndex, stringIndex + 1)).limit();
				if ((byteCount + currentByteLen) > bytelen) {
					break; // 达到�?大字节数，中�?

				} else {
					byteCount += currentByteLen; // 继续
					stringIndex++;

				}
			}

			return value.substring(0, stringIndex);

		} catch (UnsupportedCharsetException e) {
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * A convenience method for UTF8 charset. See reference in leftByCharsetByte
	 * method.
	 * 
	 * @param value
	 *            original String to left
	 * @param bytelen
	 *            the max length of byte to return in UTF8
	 * @return If value is null, return null, otherwise return not null String.
	 * @see URLUtil#leftByCharsetByte(String, String, int)
	 */
	public static String leftByUTF8Byte(String value, int bytelen) {
		return leftByCharsetByte(value, CharsetConstant.UTF_8, bytelen);
	}

	private static final Pattern pattern = Pattern.compile("(?<!\\\\)(\\\\\\\\)*\\Q"
			+ SEPARATOR + "\\E"); // 匹配前方有偶数个�?0个\的分隔符

	/**
	 * Split a joined String which separated by specified separator back to a
	 * String array. Should be used with StringUitl.join because there is some
	 * mechanism to avoid special character.
	 * 
	 * @param ori
	 * @return
	 * @see URLUtil#join(Collection)
	 */
	public static String[] split(String ori) {
		if (ori == null) {
			return null;
		} else {

			List<String> strings = new ArrayList<String>();
			// 自己写解析器

			Matcher matcher = pattern.matcher(ori);
			int currentStart = 0;
			while (matcher.find()) {
				// 匹配返回的end即分割符后位�?
				int pos = matcher.end();
				strings.add(ori.substring(currentStart, pos - 1)); // 截取本次�?始到分隔符前�?个位�?(去掉\�?)
				currentStart = pos;
			}
			// �?后必然还有剩余，就算�?后一个字符是分隔�?
			strings.add(ori.substring(currentStart, ori.length()));

			String[] strs = strings.toArray(new String[0]);
			// 依次将规避的普�?�字符恢�?
			for (int i = 0; i < strs.length; i++) {
				strs[i] = strs[i].replaceAll("\\Q\\\\\\E", Matcher
						.quoteReplacement("\\"));
				strs[i] = strs[i].replaceAll("\\Q\\|\\E", Matcher
						.quoteReplacement("|"));
			}
			return strs;
		}
	}

	/**
	 * 将字符串替换为HTML兼容的字串，并且将换行替换为<br/>
	 * @param ori
	 * @return
	 */
	public static String escapeHtmlAndBreak(String ori){
		if(ori==null){
			return null;
		}else{
			return StringEscapeUtils.escapeHtml(ori).replaceAll("\n", "<br/>");
		}
	}
	
	public static void main(String[] args) {
		String[] strs = new String[]{"\\|","\\","\\"};
		String str = 
		join(strs);
		System.out.println(str);
		System.out.println(Arrays.toString(split(str)));
		
	}
}
