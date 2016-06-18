package util;

import java.util.HashMap;
import java.util.Map;
import api.HttpClientUtil;
import dmdata.DataManager;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import service.DBOperator;
import service.LogInfo;
import service.comData;

public class HttpMethod {
	
	public static void main(String[] args){
		String sql = "select a.TRANSFER_ORDER_NO,b.ITEM_CODE sourceNo,b.OQC_QTY inOutAmount "
				+ "from oub_shipment_header a "
				+ "inner join oub_shipment_detail b on a.shipment_no=b.shipment_no and a.warehouse_code=b.warehouse_code "
				+ "where a.shipment_no='SO00000019'";
		DataManager dm = DBOperator.DoSelect2DM(sql);
		String jsonData = DBOperator.DataManager2JSONString(dm, "productDeatil");
		JSONObject dataJson = JSONObject.fromObject(jsonData);
		new HttpMethod().httpPost_manInOutStock(dm.getString("TRANSFER_ORDER_NO", 0), dataJson.get("productDeatil").toString());
	}
	
	public String httpPost_manInOutStock(String wayBill,String productDeatil){
		String url = "http://api.ajyaguru.com/sandwichapi/stock/manInOutStock.html";//"https://api.ajyaguru.com/stock/manInOutStock.html";
		String charset = "utf-8";
		HttpClientUtil httpClientUtil = new HttpClientUtil();
		Map<String, String> hashMap = new HashMap<String, String>();
		hashMap.put("wayBill", wayBill);
		hashMap.put("productDeatil", productDeatil);
		String salt = comData.createLinkString2(hashMap);
		System.out.println(salt);
		String httpRequestResult = httpClientUtil.doPost(url,hashMap,charset);
		System.out.println("http post返回值:"+httpRequestResult);
		LogInfo.appendLog("API","http post返回值:"+httpRequestResult);
		JSONObject dataJson = JSONObject.fromObject(httpRequestResult);
		JSONArray data = null;
		if(dataJson.containsKey("code")){
			String code = dataJson.getString("code");
			if(!code.equals("0")){
				LogInfo.appendLog("error","扣减海关账册库存失败(httpPost_manInOutStock)："+salt+" 返回值:"+httpRequestResult);
			}
//			data=dataJson.getJSONArray("Data");
//			for(int i=0;i<data.size();i++){
//				JSONObject info=data.getJSONObject(i);
//				String ProductID = info.getString("ProductID");
//				String OnlineQty = info.getString("OnlineQty");
//				String WareHouseID = info.getString("WareHouseID");
//				System.out.println(WareHouseID+" / "+ProductID +" : "+ OnlineQty);
//			}
		}
		return httpRequestResult;
	}

}
