package service;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Vector;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.apache.commons.lang.StringEscapeUtils;

import dmdata.DataManager;
import dmdata.xArrayList;
import net.sf.json.JSONObject;
import util.HttpMethod;

//����Ҫ������WebService�����ϼ���ע��@WebService   
@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class WMSNotify {
	
	/**
	 * �ṩ��һ��˵Hello�ķ���
	 * 
	 * @return
	 */
	@WebResult(name = "return_sayHello")
	public String sayHello(@WebParam(name = "name", partName = "name") String name) {
		return "Hello " + name+ "\nyou are welcom ~_~";
	}
	
	/**
	 * ���� ���ݺ���ɾ�����봦��������
	 * 
	 * @return
	 */
	@WebResult(name = "return_sendStockDeleteInfo")
	public String sendStockDeleteInfo(@WebParam(name = "arg0", partName = "arg0") String arg0) {
		LogInfo.appendLog("wmsnotify",arg0);
		return "success";
	}
	
	/**
	 * ���� ���ݺ��ع鲢��ϵ��������
	 * 
	 * @return
	 */
	@WebResult(name = "return_sendMergerInfo")
	public String sendMergerInfo(@WebParam(name = "content", partName = "content") String content) {
		LogInfo.appendLog("wmsnotify",content);
		return "success";
	}

}
