package service;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Vector;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.apache.commons.lang.StringEscapeUtils;

import cn.gov.zjport.manchester.encrypt.AESEncrypt;
import dmdata.DataManager;
import dmdata.xArrayList;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;
import util.HttpMethod;
import util.StringUtil;

//在想要发布成WebService的类上加上注解@WebService   
@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class WMSNotify {
	
	/**
	 * 提供了一个说Hello的服务
	 * 
	 * @return
	 */
	@WebResult(name = "return_sayHello")
	public String sayHello(@WebParam(name = "name", partName = "name") String name) {
		return "Hello " + name+ "\nyou are welcom ~_~";
	}
	
	/**
	 * 接收 杭州海关删单申请处理结果数据
	 * 
	 * @return
	 */
	@WebResult(name = "return_sendStockDeleteInfo")
	public String sendStockDeleteInfo(@WebParam(name = "arg0", partName = "arg0") String arg0) {
		AESEncrypt aes = new AESEncrypt();
		LogInfo.appendLog("wmsnotify","海关删单-海关发送原始数据：\n"+arg0);
		String result = "";
		try {
			result = aes.decryptor(arg0);
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException
				| UnsupportedEncodingException e) {
			e.printStackTrace();
			LogInfo.appendLog("error",e.getMessage());
		}
		LogInfo.appendLog("wmsnotify","海关删单-解密后数据：\n"+result);
		
		XMLSerializer xmlserial = new XMLSerializer();
		JSON json = xmlserial.read(result);
		LogInfo.appendLog("wmsnotify",json.toString());
		JSONObject dataJson = JSONObject.fromObject(json);
		JSONArray body = dataJson.getJSONArray("body");
//		LogInfo.appendLog("wmsnotify",body.toString());
		JSONObject manSign = body.getJSONObject(0).getJSONObject("manSign");
		String companyCode = manSign.get("companyCode").toString();
		String businessNo = StringUtil.avoidNull(manSign.get("businessNo").toString());
		StringBuffer sbf = new StringBuffer();
		sbf.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		sbf.append("<mo version=\"1.0.0\">");
		sbf.append("<head>");
		sbf.append("<businessType>RESULT</businessType>");
		sbf.append("</head>");
		sbf.append("<body>");
		sbf.append("<resultInfo>");
		sbf.append("<manResultHead>");
		sbf.append("<companyCode>"+companyCode+"</companyCode>");
		sbf.append("<businessType>STOCK_DELETE_RESULT</businessType>");
		sbf.append("<businessNo>"+businessNo+"</businessNo>");
		sbf.append("<processTime>"+LogInfo.getCurrentDate()+"</processTime>");
		sbf.append("<processResult>S</processResult>");
		sbf.append("<processComment>接收成功</processComment>");
		sbf.append("</manResultHead>");
		sbf.append("<manResultDetailList>");
		sbf.append("<manResultDetail>");
		sbf.append("<information>处理明细</information>");
		sbf.append("</manResultDetail>");
		sbf.append("</manResultDetailList>");
		sbf.append("</resultInfo>");
		sbf.append("</body>");
		sbf.append("</mo>");
		LogInfo.appendLog("wmsnotify","删单回执报文：\n"+sbf.toString());
		try {
			result = aes.encrytor(sbf.toString());
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException
				| UnsupportedEncodingException e) {
			result = "回执报文加密失败";
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 接收 杭州海关归并关系发送数据
	 * 
	 * @return
	 */
	@WebResult(name = "return_sendMergerInfo")
	public String sendMergerInfo(@WebParam(name = "content", partName = "content") String content) {
		AESEncrypt aes = new AESEncrypt();
		LogInfo.appendLog("wmsnotify","归并关系-海关发送原始数据：\n"+content);
		String result = "";
		try {
			result = aes.decryptor(content);
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException
				| UnsupportedEncodingException e) {
			e.printStackTrace();
			LogInfo.appendLog("error",e.getMessage());
		}
		LogInfo.appendLog("wmsnotify","归并关系-解密后数据：\n"+result);
		
		XMLSerializer xmlserial = new XMLSerializer();
		JSON json = xmlserial.read(result);
		LogInfo.appendLog("wmsnotify",json.toString());
		JSONObject dataJson = JSONObject.fromObject(json);
		JSONArray body = dataJson.getJSONArray("body");
//		LogInfo.appendLog("wmsnotify",body.toString());
		JSONObject manSign = body.getJSONObject(0).getJSONObject("manSign");
		String companyCode = manSign.get("companyCode").toString();
//		LogInfo.appendLog("wmsnotify",manSign.toString());
//		LogInfo.appendLog("wmsnotify",manSign.get("companyCode").toString());
		JSONArray manItemSourceList = (JSONArray) body.getJSONObject(0).get("manItemSourceList");
		for(int i=0;i<manItemSourceList.size();i++){
			JSONObject manItemSource = manItemSourceList.getJSONObject(i);
			LogInfo.appendLog("wmsnotify",manItemSource.get("manualId").toString());
			LogInfo.appendLog("wmsnotify",manItemSource.get("goodsNo").toString());
		}
		StringBuffer sbf = new StringBuffer();
		sbf.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		sbf.append("<mo version=\"1.0.0\">");
		sbf.append("<head>");
		sbf.append("<businessType>RESULT</businessType>");
		sbf.append("</head>");
		sbf.append("<body>");
		sbf.append("<resultInfo>");
		sbf.append("<manResultHead>");
		sbf.append("<companyCode>"+companyCode+"</companyCode>");
		sbf.append("<businessType>MERGER</businessType>");
		sbf.append("<businessNo>业务编号</businessNo>");
		sbf.append("<processTime>"+LogInfo.getCurrentDate()+"</processTime>");
		sbf.append("<processResult>S</processResult>");
		sbf.append("<processComment>接收成功</processComment>");
		sbf.append("</manResultHead>");
		sbf.append("<manResultDetailList>");
		sbf.append("<manResultDetail>");
		sbf.append("<information>处理明细行："+manItemSourceList.size()+"</information>");
		sbf.append("</manResultDetail>");
		sbf.append("</manResultDetailList>");
		sbf.append("</resultInfo>");
		sbf.append("</body>");
		sbf.append("</mo>");
		LogInfo.appendLog("wmsnotify","归并关系回执报文：\n"+sbf.toString());
		try {
			result = aes.encrytor(sbf.toString());
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException
				| UnsupportedEncodingException e) {
			result = "回执报文加密失败";
			e.printStackTrace();
		}
		return result;
	}

}
