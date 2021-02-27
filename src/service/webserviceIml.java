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
public class webserviceIml {
	
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
	 * ��ȡ�汾��Ϣ
	 * 
	 * @param
	 * @return
	 */
	@WebResult(name = "return_RFVersion")
	public String getRFVersion(@WebParam(name = "SYS_VERSION_CLIENT", partName = "SYS_VERSION_CLIENT") String SYS_VERSION_CLIENT,
			@WebParam(name = "SYS_VERSION_CODE", partName = "SYS_VERSION_CODE") String SYS_VERSION_CODE) {
		String sql = "select SYS_VERSION_CODE,SYS_VERSION_NAME,SYS_VERSION_CLIENT,SYS_UPDATE_URL,SYS_UPDATE_FILES "
				+" from sys_version "
				+" where SYS_VERSION_CLIENT='"+SYS_VERSION_CLIENT+"' and SYS_VERSION_CODE > '"+SYS_VERSION_CODE+"' limit 1 ";
		if (sqlValidate(sql)) {
			DataManager dm = DBOperator.DoSelect2DM(sql);
			String ret = DBOperator.DataManager2JSONString(dm, "");
			return ret;
		}
		return "";
	}
	
	/**
	 * ��¼��Ϣ
	 * 
	 * @param
	 * @return
	 */
	@WebResult(name = "return_getLogin")
	public String getLogin(@WebParam(name = "userCode", partName = "userCode") String userCode,
			@WebParam(name = "password", partName = "password") String password) {
		String passwordstr = MD5.GetMD5Code(password);
		String sql = "select WAREHOUSE_CODE,USER_CODE,LOGIN_CODE,USER_NAME,ROLE_NAME,ACTIVE,CREATED_DTM_LOC,password "
				+ "from sys_user "
				+ "where (user_code= '" + userCode + "' or login_code = '" + userCode + "') ";
		if (sqlValidate(sql)) {
			DataManager dm = DBOperator.DoSelect2DM(sql);
			SimpleDateFormat sdf =   new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			long timeInMillis = 0;
			try {
				timeInMillis = sdf.parse(dm.getString("CREATED_DTM_LOC", 0)).getTime();
			} catch (ParseException e1) {
				timeInMillis = 0;
			}
			// input_pwd_md5Ϊ�û����������������js����MD5���ܺ��������
			String reslut_pwd_md5 = MD5.GetMD5Code(dm.getString("USER_CODE", 0) + timeInMillis) 
					+ MD5.GetMD5Code(password) + 
					MD5.GetMD5Code(dm.getString("LOGIN_CODE", 0));
			LogInfo.appendLog("sql",reslut_pwd_md5);
			if(dm.getString("password", 0).equalsIgnoreCase(reslut_pwd_md5)){
				String ret = DBOperator.DataManager2JSONString(dm, "getLogin");
				return ret;
			}else{
				return "";
			}
		}
		return "";
	}
	
	/**
	 * ���⸴��
	 * 
	 * @param
	 * @return
	 */
	@WebResult(name = "return_getShipmentOutboundCheck")
	public String getShipmentOutboundCheck(@WebParam(name = "transportNo", partName = "transportNo") String trackingNo,
			@WebParam(name = "warehouseCode", partName = "warehouseCode") String warehouseCode,
			@WebParam(name = "userCode", partName = "userCode") String userCode) {
		String sql = "";
		String shipmentNo = "";
		String status = "";
		String checkUser = "";
		String checkTime = "";
		String outputString = "";
		if(!trackingNo.equals("")){
			sql = "select osh.SHIPMENT_NO,osh.status,osh.CHECK_BY_USER,concat(osh.CHECK_BY_USER,' ',ifnull(user.USER_NAME,'')) USER_NAME,osh.CHECK_DTM_LOC "
				+ "from oub_shipment_header osh "
				+ "left join sys_user user on osh.CHECK_BY_USER=user.USER_CODE and user.WAREHOUSE_CODE=osh.WAREHOUSE_CODE "
				+ "where osh.TRANSFER_ORDER_NO='"+trackingNo+"' and osh.WAREHOUSE_CODE='"+warehouseCode+"' ";
			DataManager dm = DBOperator.DoSelect2DM(sql);
			if(dm==null || dm.getCurrentCount()==0){
				outputString ="ERR=�˵��Ų���ȷ�����������룡";
			}else{
				status = dm.getString("status", 0);
				shipmentNo = dm.getString("SHIPMENT_NO", 0);
				checkUser = dm.getString("USER_NAME", 0);
				checkTime = dm.getString("CHECK_DTM_LOC", 0);
				if(status.equals("800")){
					if(checkUser.equals(userCode)){
						outputString = "ERR=�˵��Ѿ����⸴�����";
						return outputString;
					}else{
						outputString = "ERR=���˵��������û������⸴�ˡ�"+checkUser+"  ʱ�䣺"+checkTime+"��";
						return outputString;
					}
				}
				if(status.equals("700")){
					if(checkUser.equals(userCode)){
						outputString = "ERR=�ظ�ɨ��";
						return outputString;
					}else{
						outputString = "ERR=���˵��������û������⸴�ˡ�"+checkUser+"  ʱ�䣺"+checkTime+"��";
						return outputString;
					}
				}
				if(!status.equals("500")){
					outputString = "ERR=�˵�δ���������";
					return outputString;
				}
				sql = "update oub_shipment_header set status='800',CHECK_BY_USER='"+userCode+"'"
						+",CHECK_DTM_LOC=now(),UPDATED_BY_USER='"+userCode+"',UPDATED_DTM_LOC=now() "
						+" where SHIPMENT_NO='"+shipmentNo+"' and WAREHOUSE_CODE='"+warehouseCode+"' ";
				int t = DBOperator.DoUpdate(sql);
				if(t!=1){
					outputString = "ERR=��̨���ݸ���ʧ�ܣ�������ɨ�裡";
					return outputString;
				}else{
					//���¶�����ϸ״̬
					sql = "update oub_shipment_detail set `STATUS`='800' "
							+" where SHIPMENT_NO='"+shipmentNo+"' "
							+" and WAREHOUSE_CODE='"+warehouseCode+"' ";
					t = DBOperator.DoUpdate(sql);
					if(t==0){
						return "ERR-�˵��ţ�"+trackingNo+" ��ϸ״̬����ʧ��";
					}
					//���¿����������ͼ������
					sql = "update inv_inventory ii "
						+"inner join (select t.STORER_CODE,t.WAREHOUSE_CODE,t.SHIPMENT_NO,t.ITEM_CODE,t.INV_INVENTORY_ID,sum(t.PICKED_QTY) PICKED_QTY "
						+ "  from oub_pick_detail t where t.SHIPMENT_NO="
						+ "  (select SHIPMENT_NO from oub_shipment_header where SHIPMENT_NO='"+shipmentNo+"' "
						+ "  and WAREHOUSE_CODE='"+warehouseCode+"' limit 1) and t.`STATUS`<>'999' "
						+ "  group by t.STORER_CODE,t.WAREHOUSE_CODE,t.SHIPMENT_NO,t.ITEM_CODE,t.INV_INVENTORY_ID) opd on ii.INV_INVENTORY_ID=opd.INV_INVENTORY_ID and ii.ITEM_CODE=opd.ITEM_CODE "
						//+"inner join oub_shipment_header osh on osh.STORER_CODE=opd.STORER_CODE and osh.WAREHOUSE_CODE=opd.WAREHOUSE_CODE and osh.SHIPMENT_NO=opd.SHIPMENT_NO "
						+" set ii.ON_HAND_QTY=ii.ON_HAND_QTY-(opd.PICKED_QTY),"
						+ "ii.PICKED_QTY=ii.PICKED_QTY-(opd.PICKED_QTY),"
						+ "ii.OUB_TOTAL_QTY=ii.OUB_TOTAL_QTY+opd.PICKED_QTY "
						+",ii.UPDATED_DTM_LOC=now(),ii.UPDATED_BY_USER='"+userCode+"'"
						+"where opd.SHIPMENT_NO='"+shipmentNo+"' and opd.WAREHOUSE_CODE='"+warehouseCode+"' "
						+"";
					t = DBOperator.DoUpdate(sql);
					if(t==0){
						//�۳����ʧ��    ����״̬����Ϊ700
						sql = "update oub_shipment_header set status='700',CHECK_BY_USER='"+userCode+"'"
								+",CHECK_DTM_LOC=now(),UPDATED_BY_USER='"+userCode+"',UPDATED_DTM_LOC=now() "
								+" where TRANSFER_ORDER_NO='"+trackingNo+"' ";
						t = DBOperator.DoUpdate(sql);
						return "ERR-�˵��ţ�"+trackingNo+" �ۼ����ʧ��";
					}else{
						//���ݲֿ���Ϣ���ж��Ƿ���Ҫ���ýӿڣ��ۼ������˲���
//						sql = "select * from bas_warehouse where WAREHOUSE_CODE='"+warehouseCode+"'";
//						dm = DBOperator.DoSelect2DM(sql);
//						if(dm.getString("IS_PUSH_CUSTOM", 0).equalsIgnoreCase("Y")){
//							//�ӿ����ͺ��ؿۼ�    �����˲���
//							sql = "select IS_PUSH_CUSTOM from oub_shipment_header "
//									+ "where shipment_no='"+shipmentNo+"' and WAREHOUSE_CODE='"+warehouseCode+"'";
//							dm = DBOperator.DoSelect2DM(sql);
//							//��ֹ�ظ����ͺ��ؿۼ��˲���
//							if(dm.getString("IS_PUSH_CUSTOM", 0).equalsIgnoreCase("N")){
//								sql = "select a.EXTERNAL_ORDER_NO orderSn,a.TRANSFER_ORDER_NO,b.ITEM_CODE sourceNo,b.OQC_QTY inOutAmount "
//										+ "from oub_shipment_header a "
//										+ "inner join oub_shipment_detail b on a.shipment_no=b.shipment_no and a.warehouse_code=b.warehouse_code "
//										+ "where a.shipment_no='"+shipmentNo+"' "
//										+ "and a.WAREHOUSE_CODE='"+warehouseCode+"' ";
//								dm = DBOperator.DoSelect2DM(sql);
//								String jsonData = DBOperator.DataManager2JSONString(dm, "productDeatil");
//								JSONObject dataJson = JSONObject.fromObject(jsonData);
//								try{
//								String postResult =  new HttpMethod().httpPost_manInOutStock(dm.getString("TRANSFER_ORDER_NO", 0), dataJson.get("productDeatil").toString());
//								dataJson = JSONObject.fromObject(postResult);
//								if(dataJson.get("code").equals("0")){
//									sql = "update oub_shipment_header set IS_PUSH_CUSTOM='Y' "
//										+ "where shipment_no='"+shipmentNo+"' and WAREHOUSE_CODE='"+warehouseCode+"' ";
//									t = DBOperator.DoUpdate(sql);
//								}else{
//									LogInfo.appendLog("error","�ۼ������˲���ʧ��\n"+postResult+"\n"+jsonData);
//								}
//								}catch(Exception e){
//									LogInfo.appendLog("error","�ۼ������˲���ʧ��"+"\n"+jsonData);
//								}
//							}
//						}
						
						
						sql = "select CONFIG_VALUE1,CONFIG_VALUE2 from sys_config_detail where CONFIG_CODE='IS_REDUCE_MATERIAL' and CONFIG_VALUE1='1' ";
						dm = DBOperator.DoSelect2DM(sql);
						if(dm==null || dm.getCurrentCount()==0){
							//���عرգ�����ۼ����Ŀ������
						}else{
							//�ۼ����Ŀ������
							sql = "update inv_inventory ii "
									+ "inner join ( "
									+ "select osd.STORER_CODE,osd.ITEM_CODE,osd.OQC_QTY,bim.ITEM_CODE_MATERIAL,bim.MATCH_QTY,(bim.MATCH_QTY*osd.OQC_QTY) MATERIAL_QTY "
									+ "from oub_shipment_detail osd "
									+ "left join bas_item_material bim on osd.STORER_CODE=bim.STORER_CODE and osd.ITEM_CODE=bim.ITEM_CODE "
									+ "where osd.SHIPMENT_NO='"+shipmentNo+"') tmp on tmp.ITEM_CODE_MATERIAL=ii.ITEM_CODE "
									+ "set ii.ON_HAND_QTY=ii.ON_HAND_QTY-(tmp.MATERIAL_QTY),"
									+ "ii.OUB_TOTAL_QTY=ii.OUB_TOTAL_QTY+(tmp.MATERIAL_QTY) "
									+ "";
							t = DBOperator.DoUpdate(sql);
							if(t==0){
								//��¼������־
								DataManager dmProcess = comData.getSysProcessHistoryDataModel("sys_process_history");
								if (dmProcess != null) {
									dmdata.xArrayList list = (xArrayList) dmProcess.getRow(0);
									list.set(dmProcess.getCol("SYS_PROCESS_HISTORY_ID"), "null");
									list.set(dmProcess.getCol("PROCESS_CODE"), "OutboundCheck");
									list.set(dmProcess.getCol("PROCESS_NAME"), "���⸴��");
									list.set(dmProcess.getCol("STORER_CODE"), "");
									list.set(dmProcess.getCol("WAREHOUSE_CODE"),warehouseCode);
									list.set(dmProcess.getCol("FROM_LOCATION_CODE"), "");
									list.set(dmProcess.getCol("FROM_CONTAINER_CODE"), "");
									list.set(dmProcess.getCol("QTY"), "");
									list.set(dmProcess.getCol("REFERENCE_NO"), trackingNo);
									list.set(dmProcess.getCol("REFERENCE_LINE_NO"), "");
									list.set(dmProcess.getCol("REFERENCE_TYPE"), "");
									list.set(dmProcess.getCol("LOT_NO"), "");
									list.set(dmProcess.getCol("MESSAGE"), "�۳����Ŀ��ʧ��:"+StringEscapeUtils.escapeSql(sql));
									list.set(dmProcess.getCol("PROCESS_TIME"), "now()");
									list.set(dmProcess.getCol("CREATED_BY_USER"),userCode);
									list.set(dmProcess.getCol("CREATED_DTM_LOC"), "now()");
									list.set(dmProcess.getCol("UPDATED_DTM_LOC"), "now()");
									dmProcess.RemoveRow(0);
									dmProcess.AddNewRow(list);
									boolean bool = comData.addSysProcessHistory("sys_process_history", dmProcess);
									System.out.println("д�������־��" + bool);
								}
							}
						}
						//��¼������־
						DataManager dmProcess = comData.getSysProcessHistoryDataModel("sys_process_history");
						if (dmProcess != null) {
							dmdata.xArrayList list = (xArrayList) dmProcess.getRow(0);
							list.set(dmProcess.getCol("SYS_PROCESS_HISTORY_ID"), "null");
							list.set(dmProcess.getCol("PROCESS_CODE"), "OutboundCheck");
							list.set(dmProcess.getCol("PROCESS_NAME"), "���⸴��");
							list.set(dmProcess.getCol("STORER_CODE"), "");
							list.set(dmProcess.getCol("WAREHOUSE_CODE"),warehouseCode);
							list.set(dmProcess.getCol("FROM_LOCATION_CODE"), "");
							list.set(dmProcess.getCol("FROM_CONTAINER_CODE"), "");
							list.set(dmProcess.getCol("QTY"), "");
							list.set(dmProcess.getCol("REFERENCE_NO"), trackingNo);
							list.set(dmProcess.getCol("REFERENCE_LINE_NO"), "");
							list.set(dmProcess.getCol("REFERENCE_TYPE"), "");
							list.set(dmProcess.getCol("LOT_NO"), "");
							list.set(dmProcess.getCol("MESSAGE"), "");
							list.set(dmProcess.getCol("PROCESS_TIME"), "now()");
							list.set(dmProcess.getCol("CREATED_BY_USER"),userCode);
							list.set(dmProcess.getCol("CREATED_DTM_LOC"), "now()");
							list.set(dmProcess.getCol("UPDATED_DTM_LOC"), "now()");
							dmProcess.RemoveRow(0);
							dmProcess.AddNewRow(list);
							boolean bool = comData.addSysProcessHistory("sys_process_history", dmProcess);
							System.out.println("д�������־��" + bool);
						}
						sql = "select sum(REQ_QTY) REQ_QTY,sum(OQC_QTY) OQC_QTY from oub_shipment_detail "
								+" where SHIPMENT_NO='"+shipmentNo+"' "
								+" and WAREHOUSE_CODE='"+warehouseCode+"' ";
						DataManager dmReturn = DBOperator.DoSelect2DM(sql);
						outputString = "OK=�˵����⸴�˳ɹ�";
						if(dmReturn!=null || dmReturn.getCurrentCount()>0){
							outputString = outputString + "\n������"+dm.getString("REQ_QTY", 0)+"/"+dm.getString("OQC_QTY", 0);
						}
						return outputString;
					}
					
				}
			}
		}
		return outputString;
	}
	
	/**
	 * ����̵㵥��
	 * 
	 * @param
	 * @return
	 */
	@WebResult(name = "return_checkStockTakeNo")
	public String checkStockTakeNo(@WebParam(name = "STOCKTAKE_NO", partName = "STOCKTAKE_NO") String STOCKTAKE_NO) {
		String sql = "select STORER_CODE,WAREHOUSE_CODE,STOCKTAKE_NO,STOCKTAKE_NAME,STATUS from inv_stocktake_header "
				+ "where STATUS in ('100','200') and STOCKTAKE_NO like '%"+STOCKTAKE_NO+"%' order by STOCKTAKE_NO desc limit 1 ";
		if (sqlValidate(sql)) {
			DataManager dm = DBOperator.DoSelect2DM(sql);
			if(dm==null || dm.getCurrentCount()==0){
				
			}else{
				sql = "update inv_stocktake_header set STATUS='200' where STOCKTAKE_NO='"+dm.getString("STOCKTAKE_NO", 0)+"' and STATUS='100' ";
				int t = DBOperator.DoUpdate(sql);
			}
			String ret = DBOperator.DataManager2JSONString(dm, "");
			return ret;
		}
		return "";
	}
	
	/**
	 * ����λ
	 * 
	 * @param
	 * @return
	 */
	@WebResult(name = "return_checkLocationCode")
	public String checkLocationCode(@WebParam(name = "WAREHOUSE_CODE", partName = "WAREHOUSE_CODE") String WAREHOUSE_CODE,
			@WebParam(name = "LOCATION_CODE", partName = "LOCATION_CODE") String LOCATION_CODE) {
		String sql = "select location_code from bas_location "
				+ "where WAREHOUSE_CODE='"+WAREHOUSE_CODE+"' and LOCATION_CODE='"+LOCATION_CODE+"' ";
		if (sqlValidate(sql)) {
			DataManager dm = DBOperator.DoSelect2DM(sql);
			String ret = DBOperator.DataManager2JSONString(dm, "checkLocationCode");
			return ret;
		}
		return "";
	}
	
	/**
	 * ������
	 * 
	 * @param
	 * @return
	 */
	@WebResult(name = "return_checkContainerNo")
	public String checkContainerNo(@WebParam(name = "WAREHOUSE_CODE", partName = "WAREHOUSE_CODE") String WAREHOUSE_CODE,
			@WebParam(name = "CONTAINER_CODE", partName = "CONTAINER_CODE") String CONTAINER_CODE) {
		String sql = "select WAREHOUSE_CODE,CONTAINER_CODE,STATUS,USE_TYPE from bas_container "
				+ "where CONTAINER_CODE='"+CONTAINER_CODE+"' and WAREHOUSE_CODE='"+WAREHOUSE_CODE+"'";
		if (sqlValidate(sql)) {
			DataManager dm = DBOperator.DoSelect2DM(sql);
			String ret = DBOperator.DataManager2JSONString(dm, "checkContainerNo");
			return ret;
		}
		return "";
	}
	
	/**
	 * ���������Ʒ��Ϣ
	 * 
	 * @param
	 * @return
	 */
	@WebResult(name = "return_ItemInfo")
	public String getItemInfoByBarcode(@WebParam(name = "ITEM_BAR_CODE", partName = "ITEM_BAR_CODE") String ITEM_BAR_CODE,
			@WebParam(name = "STORER_CODE", partName = "STORER_CODE") String STORER_CODE) {
		String sql = "select bi.STORER_CODE,bs.STORER_NAME,bi.ITEM_CODE,bi.ITEM_NAME,bi.ITEM_BAR_CODE,"
				+ "if(ifnull(bi.PORT_CODE,'0')='','0',ifnull(bi.PORT_CODE,'0')) PORT_CODE,"
				+ "if(ifnull(biu.unit_name,'��')='','��',ifnull(biu.unit_name,'��')) unit_name "
				+"from bas_item bi "
				+"inner join bas_storer bs on bi.STORER_CODE=bs.STORER_CODE "
				+"left join bas_item_unit biu on bi.UNIT_CODE=biu.unit_code "
				+"where bi.STORER_CODE='"+STORER_CODE+"' and bi.ITEM_BAR_CODE='"+ITEM_BAR_CODE+"' "
				+ "order by CREATED_DTM_LOC desc limit 1 ";
		if (sqlValidate(sql)) {
			DataManager dm = DBOperator.DoSelect2DM(sql);
			String ret = DBOperator.DataManager2JSONString(dm, "");
			return ret;
		}
		return "";
	}
	
	/**
	 * ���������Ʒ��Ϣ
	 * ֻ���������ѯ
	 * 
	 * @param
	 * @return
	 */
	@WebResult(name = "return_ItemInfo")
	public String getItemInfoByBarcodeTMP(@WebParam(name = "ITEM_BAR_CODE", partName = "ITEM_BAR_CODE") String ITEM_BAR_CODE) {
		String sql = "select bi.STORER_CODE,bs.STORER_NAME,bi.ITEM_CODE,bi.ITEM_NAME,"
				+ "bi.ITEM_BAR_CODE,if(ifnull(bi.PORT_CODE,'0')='','0',ifnull(bi.PORT_CODE,'0')) PORT_CODE,"
				+ "if(ifnull(biu.unit_name,'��')='','��',ifnull(biu.unit_name,'��')) unit_name "
				+"from bas_item bi "
				+"inner join bas_storer bs on bi.STORER_CODE=bs.STORER_CODE "
				+"left join bas_item_unit biu on bi.UNIT_CODE=biu.unit_code "
				+"where bi.ITEM_BAR_CODE='"+ITEM_BAR_CODE+"' limit 1 ";
		if (sqlValidate(sql)) {
			DataManager dm = DBOperator.DoSelect2DM(sql);
			String ret = DBOperator.DataManager2JSONString(dm, "");
			return ret;
		}
		return "";
	}
	
	/**
	 * ���������Ʒ��Ϣ
	 * ֻ��������+�ֿ�Ų�ѯ
	 * 
	 * @param
	 * @return
	 */
	@WebResult(name = "return_ItemInfo")
	public String getItemInfoByBarcodeByWarehouse(@WebParam(name = "WAREHOUSE_CODE", partName = "WAREHOUSE_CODE") String WAREHOUSE_CODE,
			@WebParam(name = "ITEM_BAR_CODE", partName = "ITEM_BAR_CODE") String ITEM_BAR_CODE) {
		//���Ȳ��ұ��ֿ���Ʒ���룬����鲻��������չ�������ֿ�
		String sql = "select bi.STORER_CODE,bs.STORER_NAME,bi.ITEM_CODE,bi.ITEM_NAME,"
				+ "bi.ITEM_BAR_CODE,if(ifnull(bi.PORT_CODE,'0')='','0',ifnull(bi.PORT_CODE,'0')) PORT_CODE,"
				+ "if(ifnull(biu.unit_name,'��')='','��',ifnull(biu.unit_name,'��')) unit_name "
				+"from bas_item bi "
				+"inner join bas_storer bs on bi.STORER_CODE=bs.STORER_CODE "
				+"left join bas_item_unit biu on bi.UNIT_CODE=biu.unit_code "
				+"where bi.ITEM_BAR_CODE='"+ITEM_BAR_CODE+"' "
				+ "and bi.ITEM_CODE in (select ITEM_CODE from inv_inventory where WAREHOUSE_CODE='"+WAREHOUSE_CODE+"') "
				+ "limit 1 ";
		if (sqlValidate(sql)) {
			DataManager dm = DBOperator.DoSelect2DM(sql);
			if(dm.getCurrentCount()==0){
				sql = "select bi.STORER_CODE,bs.STORER_NAME,bi.ITEM_CODE,bi.ITEM_NAME,"
						+ "bi.ITEM_BAR_CODE,if(ifnull(bi.PORT_CODE,'0')='','0',ifnull(bi.PORT_CODE,'0')) PORT_CODE,"
						+ "if(ifnull(biu.unit_name,'��')='','��',ifnull(biu.unit_name,'��')) unit_name "
						+"from bas_item bi "
						+"inner join bas_storer bs on bi.STORER_CODE=bs.STORER_CODE "
						+"left join bas_item_unit biu on bi.UNIT_CODE=biu.unit_code "
						+"where bi.ITEM_BAR_CODE='"+ITEM_BAR_CODE+"' "
						+ "limit 1 ";
				dm = DBOperator.DoSelect2DM(sql);
			}
			String ret = DBOperator.DataManager2JSONString(dm, "");
			return ret;
		}
		return "";
	}
	
	/**
	 * �̵�ɨ������
	 * 
	 * @param
	 * @return
	 */
	@WebResult(name = "return_stockTakeScanBarCode")
	public String stockTakeScanBarCode(@WebParam(name = "STOCKTAKE_NO", partName = "STOCKTAKE_NO") String STOCKTAKE_NO,
			@WebParam(name = "LOCATION_CODE", partName = "LOCATION_CODE") String LOCATION_CODE,
			@WebParam(name = "CONTAINER_CODE", partName = "CONTAINER_CODE") String CONTAINER_CODE,
			@WebParam(name = "ITEM_BAR_CODE", partName = "ITEM_BAR_CODE") String ITEM_BAR_CODE,
			@WebParam(name = "itemQty", partName = "itemQty") String itemQty,
			@WebParam(name = "userCode", partName = "userCode") String userCode) {
		String itemCode = "";
		String unitCode = "";
		String itemName = "";
		String itemBarCode = ITEM_BAR_CODE;
		String storerCode = "";
		String warehouseCode = "";
		String locationCode = LOCATION_CODE;
		String containerCode = CONTAINER_CODE;
		String stockTakeNo = STOCKTAKE_NO;
		String sql ="select WAREHOUSE_CODE,STORER_CODE,STOCKTAKE_NO,STOCKTAKE_NAME,STATUS from inv_stocktake_header where STOCKTAKE_NO='"+STOCKTAKE_NO+"' ";
		DataManager dm = DBOperator.DoSelect2DM(sql);
		storerCode = dm.getString("STORER_CODE", 0);
		warehouseCode = dm.getString("WAREHOUSE_CODE", 0);
		
		sql = "select item_code,unit_code,item_name from bas_item "
			+ "where storer_code='"+storerCode+"' and item_bar_code='"+itemBarCode+"'";
		if (!itemBarCode.equals("")) {
			dm = DBOperator.DoSelect2DM(sql);
			if (dm == null || dm.getCurrentCount() == 0) {
				return "ERR-��Ʒ�������벻��ȷ!";
			}else{
				//�ж�һ�������Ӧ������Ϻţ���ô����Ҫ���ݲֿ����+����+���� ������ѡ�� ��ȷ�����Ϻ�
				if(dm.getCurrentCount()>1){
					sql = "select item_code,unit_code,item_name from bas_item "
						+ "where storer_code='"+storerCode+"' "
						+ "and item_bar_code='"+itemBarCode+"' "
						+ "and item_code in (select distinct inv.item_code from inv_inventory inv where inv.storer_code='"+storerCode+"' and inv.item_code=item_code and inv.warehouse_code='"+warehouseCode+"')";
						dm = DBOperator.DoSelect2DM(sql);
						if(dm.getCurrentCount()==0){
							return "ERR-��Ʒ������ѡ��ֿⲻƥ��!";
						}else{
							itemCode = dm.getString("item_code", 0);
							unitCode = dm.getString("unit_code", 0);
							itemName = dm.getString("item_name", 0);
						}
				}
				itemCode = dm.getString("item_code", 0);
				unitCode = dm.getString("unit_code", 0);
				itemName = dm.getString("item_name", 0);
				sql = "select STOCKTAKE_NO,STORER_CODE,WAREHOUSE_CODE,LOCATION_CODE,CONTAINER_CODE,ITEM_CODE "
						+"from inv_stocktake_detail isd "
						+" where isd.STOCKTAKE_NO='"+stockTakeNo+"' and isd.STORER_CODE='"+storerCode+"' "
						+" and isd.WAREHOUSE_CODE = '"+warehouseCode+"' and isd.LOCATION_CODE='"+locationCode+"' "
						+" and isd.CONTAINER_CODE='"+containerCode+"' and isd.ITEM_CODE='"+itemCode+"' ";
				dm = DBOperator.DoSelect2DM(sql);
				if(dm == null || dm.getCurrentCount() == 0){
					//�����̵��¼�¼
					sql = "insert into inv_stocktake_detail(STOCKTAKE_NO,STORER_CODE,WAREHOUSE_CODE,LOCATION_CODE,CONTAINER_CODE,"
							+"ITEM_CODE,GUIDE_QTY,GUIDE_UOM,CONF_QTY,CONF_UOM,FIRST_STOCKTAKE_QTY,FIRST_STOCKTAKE_UOM,CREATED_BY_USER,CREATED_DTM_LOC) "
							+"select '"+stockTakeNo+"','"+storerCode+"','"+warehouseCode+"','"+locationCode+"','"+containerCode+"', "
							+"'"+itemCode+"',"
							+"ifnull((select sum(ii.ON_HAND_QTY+IN_TRANSIT_QTY-(ALLOCATED_QTY)-(PICKED_QTY)-(INACTIVE_QTY)) qty from inv_inventory ii where ii.STORER_CODE='"+storerCode+"' and ii.WAREHOUSE_CODE='"+warehouseCode+"' and ii.ITEM_CODE='"+itemCode+"' and ii.LOCATION_CODE='"+locationCode+"' and ii.CONTAINER_CODE='"+containerCode+"'),0),"
							+"'"+unitCode+"',"+itemQty+",'"+unitCode+"',"+itemQty+",'"+unitCode+"','"+userCode+"',now() "
							+"";
					int t = DBOperator.DoUpdate(sql);
					if(t==0){
						return "ERR-�����̵���ϸʧ��\n"+itemBarCode+"��"+itemName+" �̵�������"+itemQty;
					}else{
						//��¼������־
						DataManager dmProcess = comData.getSysProcessHistoryDataModel("sys_process_history");
						if (dmProcess!=null) {
							dmdata.xArrayList list = (xArrayList) dmProcess.getRow(0);
							list.set(dmProcess.getCol("SYS_PROCESS_HISTORY_ID"), "null");
							list.set(dmProcess.getCol("WAREHOUSE_CODE"), warehouseCode);
							list.set(dmProcess.getCol("PROCESS_CODE"), "stockTakeScanBarCode");
							list.set(dmProcess.getCol("PROCESS_NAME"), "�̵�ɨ������");
							list.set(dmProcess.getCol("ITEM_CODE"), itemCode);
							list.set(dmProcess.getCol("FROM_LOCATION_CODE"), LOCATION_CODE);
							list.set(dmProcess.getCol("FROM_CONTAINER_CODE"), CONTAINER_CODE);
							list.set(dmProcess.getCol("QTY"), itemQty);
							list.set(dmProcess.getCol("REFERENCE_NO"), STOCKTAKE_NO);
							list.set(dmProcess.getCol("REFERENCE_LINE_NO"), "");
							list.set(dmProcess.getCol("CREATED_BY_USER"), userCode);
							list.set(dmProcess.getCol("PROCESS_TIME"), "now()");
							list.set(dmProcess.getCol("CREATED_DTM_LOC"), "now()");
							list.set(dmProcess.getCol("UPDATED_DTM_LOC"), "now()");
							dmProcess.RemoveRow(0);
							dmProcess.AddNewRow(list);
							boolean bool = comData.addSysProcessHistory("sys_process_history", dmProcess);
						}
						return "OK-"+itemBarCode+" "+itemQty;
					}
				}else{
					//�����̵�����
					sql = "update inv_stocktake_detail isd set CONF_QTY=CONF_QTY+("+itemQty+"),FIRST_STOCKTAKE_QTY=FIRST_STOCKTAKE_QTY+("+itemQty+") "
							+" where isd.STOCKTAKE_NO='"+stockTakeNo+"' and isd.STORER_CODE='"+storerCode+"' "
							+" and isd.WAREHOUSE_CODE = '"+warehouseCode+"' and isd.LOCATION_CODE='"+locationCode+"' "
							+" and isd.CONTAINER_CODE='"+containerCode+"' and isd.ITEM_CODE='"+itemCode+"' ";
					int t = DBOperator.DoUpdate(sql);
					if(t==0){;
						return "ERR-�����̵���ϸʧ��\n"+itemBarCode+"��"+itemName+" �̵�������"+itemQty;
					}else{
						//��¼������־
						DataManager dmProcess = comData.getSysProcessHistoryDataModel("sys_process_history");
						if (dmProcess!=null) {
							dmdata.xArrayList list = (xArrayList) dmProcess.getRow(0);
							list.set(dmProcess.getCol("SYS_PROCESS_HISTORY_ID"), "null");
							list.set(dmProcess.getCol("WAREHOUSE_CODE"), warehouseCode);
							list.set(dmProcess.getCol("PROCESS_CODE"), "stockTakeScanBarCode");
							list.set(dmProcess.getCol("PROCESS_NAME"), "�̵�ɨ������");
							list.set(dmProcess.getCol("ITEM_CODE"), itemCode);
							list.set(dmProcess.getCol("FROM_LOCATION_CODE"), LOCATION_CODE);
							list.set(dmProcess.getCol("FROM_CONTAINER_CODE"), CONTAINER_CODE);
							list.set(dmProcess.getCol("QTY"), itemQty);
							list.set(dmProcess.getCol("REFERENCE_NO"), STOCKTAKE_NO);
							list.set(dmProcess.getCol("REFERENCE_LINE_NO"), "");
							list.set(dmProcess.getCol("CREATED_BY_USER"), userCode);
							list.set(dmProcess.getCol("PROCESS_TIME"), "now()");
							list.set(dmProcess.getCol("CREATED_DTM_LOC"), "now()");
							list.set(dmProcess.getCol("UPDATED_DTM_LOC"), "now()");
							dmProcess.RemoveRow(0);
							dmProcess.AddNewRow(list);
							boolean bool = comData.addSysProcessHistory("sys_process_history", dmProcess);
						}
						return "OK-"+itemBarCode+" "+itemQty;
					}
				}
			}
		}else{
			return "ERR-��Ʒ�������벻��ȷ!";
		}
	}
	
	/**
	 * �̵�ɨ������
	 * ��ʱ�̵㣬�����жϿ��Բ����ڣ�ϵͳ����д������
	 * 
	 * @param
	 * @return
	 */
	@WebResult(name = "return_TMPStockTakeScanBarCode")
	public String TMPStockTakeScanBarCode(@WebParam(name = "STOCKTAKE_NO", partName = "STOCKTAKE_NO") String STOCKTAKE_NO,
			@WebParam(name = "LOCATION_CODE", partName = "LOCATION_CODE") String LOCATION_CODE,
			@WebParam(name = "CONTAINER_CODE", partName = "CONTAINER_CODE") String CONTAINER_CODE,
			@WebParam(name = "ITEM_BAR_CODE", partName = "ITEM_BAR_CODE") String ITEM_BAR_CODE,
			@WebParam(name = "itemQty", partName = "itemQty") String itemQty,
			@WebParam(name = "userCode", partName = "userCode") String userCode,
			@WebParam(name = "itemDate", partName = "itemDate") String itemDate) {
		String itemCode = "";
		String itemBarCode = ITEM_BAR_CODE;
		String storerCode = "";
		String warehouseCode = "";
		String locationCode = LOCATION_CODE;
		String containerCode = CONTAINER_CODE;
		String stockTakeNo = STOCKTAKE_NO;
		String unitCode = "";
		String itemName = "";
		String sql ="select WAREHOUSE_CODE,STORER_CODE,STOCKTAKE_NO,STOCKTAKE_NAME,STATUS from inv_stocktake_header where STOCKTAKE_NO='"+STOCKTAKE_NO+"' ";
		DataManager dm = DBOperator.DoSelect2DM(sql);
		storerCode = dm.getString("STORER_CODE", 0);
		warehouseCode = dm.getString("WAREHOUSE_CODE", 0);
		
		sql = "select item_code,unit_code,item_name from bas_item "
			+ "where STORER_CODE='"+storerCode+"' and warehouse_code='"+warehouseCode+"' and item_bar_code='"+itemBarCode+"' "
			+ " order by BAS_ITEM_ID desc";
		if (!itemBarCode.equals("")) {
			dm = DBOperator.DoSelect2DM(sql);
			if(dm.getCurrentCount()>=1){
				itemCode = dm.getString("item_code", 0);
				unitCode = dm.getString("unit_code", 0);
				itemName = dm.getString("item_name", 0);
				storerCode = dm.getString("STORER_CODE", 0);
			}else{
				sql = "select item_code,unit_code,item_name from bas_item "
					+ "where STORER_CODE='"+storerCode+"' and item_bar_code='"+itemBarCode+"' order by BAS_ITEM_ID desc";
				dm = DBOperator.DoSelect2DM(sql);
				if(dm.getCurrentCount()>=1){
					itemCode = dm.getString("item_code", 0);
					unitCode = dm.getString("unit_code", 0);
					itemName = dm.getString("item_name", 0);
					storerCode = dm.getString("STORER_CODE", 0);
				}else{
					sql = "select STORER_CODE,item_code,unit_code,item_name from bas_item "
						+ "where item_bar_code='"+itemBarCode+"' order by BAS_ITEM_ID desc";
					dm = DBOperator.DoSelect2DM(sql);
					if (dm == null || dm.getCurrentCount() == 0) {
						itemCode = itemBarCode;
						unitCode = "007";
						itemName = itemBarCode;
					}else{
						itemCode = dm.getString("item_code", 0);
						unitCode = dm.getString("unit_code", 0);
						itemName = dm.getString("item_name", 0);
						storerCode = dm.getString("STORER_CODE", 0);
					}
				}
			}
			
			sql = "select STOCKTAKE_NO,STORER_CODE,WAREHOUSE_CODE,LOCATION_CODE,CONTAINER_CODE,ITEM_CODE "
				+"from inv_stocktake_detail isd "
				+" where isd.STOCKTAKE_NO='"+stockTakeNo+"' and isd.STORER_CODE='"+storerCode+"' "
				+" and isd.WAREHOUSE_CODE = '"+warehouseCode+"' and isd.LOCATION_CODE='"+locationCode+"' "
				+" and isd.CONTAINER_CODE='"+containerCode+"' and isd.ITEM_CODE='"+itemCode+"' "
				+ "and ifnull(isd.USER_DEF1,'')='"+itemDate+"' ";
			dm = DBOperator.DoSelect2DM(sql);
			if(dm == null || dm.getCurrentCount() == 0){
				//�����̵��¼�¼
				sql = "insert into inv_stocktake_detail(STOCKTAKE_NO,STORER_CODE,WAREHOUSE_CODE,LOCATION_CODE,CONTAINER_CODE,"
						+"ITEM_CODE,GUIDE_QTY,GUIDE_UOM,CONF_QTY,CONF_UOM,FIRST_STOCKTAKE_QTY,FIRST_STOCKTAKE_UOM,"
						+ "USER_DEF1,CREATED_BY_USER,CREATED_DTM_LOC,UPDATED_BY_USER,UPDATED_DTM_LOC) "
						+"select '"+stockTakeNo+"','"+storerCode+"','"+warehouseCode+"','"+locationCode+"','"+containerCode+"', "
						+"'"+itemCode+"',"
						+"ifnull((select sum(ii.ON_HAND_QTY+IN_TRANSIT_QTY-(ALLOCATED_QTY)-(PICKED_QTY)-(INACTIVE_QTY)) qty from inv_inventory ii where ii.STORER_CODE='"+storerCode+"' and ii.WAREHOUSE_CODE='"+warehouseCode+"' and ii.ITEM_CODE='"+itemCode+"' and ii.LOCATION_CODE='"+locationCode+"' and ii.CONTAINER_CODE='"+containerCode+"'),0),"
						+"'"+unitCode+"',"+itemQty+",'"+unitCode+"',"+itemQty+",'"+unitCode+"',"
						+ "'"+itemDate+"','"+userCode+"',now(),'"+userCode+"',now() "
						+"";
				int t = DBOperator.DoUpdate(sql);
				if(t==0){
					return "ERR-�����̵���ϸʧ��\n"+itemBarCode+"��"+itemName+" �̵�������"+itemQty;
				}else{
					//��¼������־
					DataManager dmProcess = comData.getSysProcessHistoryDataModel("sys_process_history");
					if (dmProcess!=null) {
						dmdata.xArrayList list = (xArrayList) dmProcess.getRow(0);
						list.set(dmProcess.getCol("SYS_PROCESS_HISTORY_ID"), "null");
						list.set(dmProcess.getCol("WAREHOUSE_CODE"), warehouseCode);
						list.set(dmProcess.getCol("PROCESS_CODE"), "stockTakeScanBarCode");
						list.set(dmProcess.getCol("PROCESS_NAME"), "�̵�ɨ������");
						list.set(dmProcess.getCol("ITEM_CODE"), itemCode);
						list.set(dmProcess.getCol("FROM_LOCATION_CODE"), LOCATION_CODE);
						list.set(dmProcess.getCol("FROM_CONTAINER_CODE"), CONTAINER_CODE);
						list.set(dmProcess.getCol("QTY"), itemQty);
						list.set(dmProcess.getCol("REFERENCE_NO"), STOCKTAKE_NO);
						list.set(dmProcess.getCol("REFERENCE_LINE_NO"), "");
						list.set(dmProcess.getCol("CREATED_BY_USER"), userCode);
						list.set(dmProcess.getCol("PROCESS_TIME"), "now()");
						list.set(dmProcess.getCol("CREATED_DTM_LOC"), "now()");
						list.set(dmProcess.getCol("UPDATED_DTM_LOC"), "now()");
						dmProcess.RemoveRow(0);
						dmProcess.AddNewRow(list);
						boolean bool = comData.addSysProcessHistory("sys_process_history", dmProcess);
					}
					return "OK-"+itemBarCode+" "+itemQty;
				}
			}else{
				//�����̵�����
				sql = "update inv_stocktake_detail isd set CONF_QTY=CONF_QTY+("+itemQty+"),"
					+ "FIRST_STOCKTAKE_QTY=FIRST_STOCKTAKE_QTY+("+itemQty+"),"
					+ "USER_DEF1='"+itemDate+"',"
					+ "UPDATED_BY_USER='"+userCode+"',UPDATED_DTM_LOC=now() "
					+" where isd.STOCKTAKE_NO='"+stockTakeNo+"' and isd.STORER_CODE='"+storerCode+"' "
					+" and isd.WAREHOUSE_CODE = '"+warehouseCode+"' and isd.LOCATION_CODE='"+locationCode+"' "
					+" and isd.CONTAINER_CODE='"+containerCode+"' and isd.ITEM_CODE='"+itemCode+"' ";
				int t = DBOperator.DoUpdate(sql);
				if(t==0){;
					return "ERR-�����̵���ϸʧ��\n"+itemBarCode+"��"+itemName+" �̵�������"+itemQty;
				}else{
					//��¼������־
					DataManager dmProcess = comData.getSysProcessHistoryDataModel("sys_process_history");
					if (dmProcess!=null) {
						dmdata.xArrayList list = (xArrayList) dmProcess.getRow(0);
						list.set(dmProcess.getCol("SYS_PROCESS_HISTORY_ID"), "null");
						list.set(dmProcess.getCol("WAREHOUSE_CODE"), warehouseCode);
						list.set(dmProcess.getCol("PROCESS_CODE"), "stockTakeScanBarCode");
						list.set(dmProcess.getCol("PROCESS_NAME"), "�̵�ɨ������");
						list.set(dmProcess.getCol("ITEM_CODE"), itemCode);
						list.set(dmProcess.getCol("FROM_LOCATION_CODE"), LOCATION_CODE);
						list.set(dmProcess.getCol("FROM_CONTAINER_CODE"), CONTAINER_CODE);
						list.set(dmProcess.getCol("QTY"), itemQty);
						list.set(dmProcess.getCol("REFERENCE_NO"), STOCKTAKE_NO);
						list.set(dmProcess.getCol("REFERENCE_LINE_NO"), "");
						list.set(dmProcess.getCol("CREATED_BY_USER"), userCode);
						list.set(dmProcess.getCol("PROCESS_TIME"), "now()");
						list.set(dmProcess.getCol("CREATED_DTM_LOC"), "now()");
						list.set(dmProcess.getCol("UPDATED_DTM_LOC"), "now()");
						dmProcess.RemoveRow(0);
						dmProcess.AddNewRow(list);
						boolean bool = comData.addSysProcessHistory("sys_process_history", dmProcess);
					}
					return "OK-"+itemBarCode+" "+itemQty;
				}
			}
		}else{
			return "ERR-��Ʒ���벻��Ϊ��!";
		}
	}
	
	/**
	 * ��ȡ�����Ϣby_storer_code_itemcode
	 * 
	 * @param
	 * @return
	 */
	@WebResult(name = "return_getInventoryDetailByStorer")
	public String getInventoryDetailByStorer(@WebParam(name = "storercode", partName = "storercode") String storercode,
			@WebParam(name = "itemcode", partName = "itemcode") String itemcode) {
		String sql = "select ii.item_code,bi.item_bar_code,bi.item_name,sum(ON_HAND_QTY-ALLOCATED_QTY-PICKED_QTY) open_qty,biu.unit_name "
				+ "from inv_inventory ii "
				+ "inner join bas_item bi on ii.storer_code=bi.storer_code and ii.item_code=bi.item_code "
				+ "left join bas_item_unit biu on bi.UNIT_CODE=biu.unit_code "
				+ "left outer join inv_lot il on ii.LOT_NO=il.LOT_NO " 
				+ "where ii.STORER_CODE='" + storercode + "' and ii.item_code='"+itemcode+"' "
				+ "group by ii.item_code,bi.item_bar_code,bi.item_name ";
		DataManager dm = DBOperator.DoSelect2DM(sql);
		String ret = DBOperator.DataManager2JSONString(dm,"inventory_list");
		return ret;
	}
	
	/**
	 * ���PO����
	 * 
	 * @param
	 * @return
	 */
	@WebResult(name = "return_checkPONo")
	public String checkPONo(@WebParam(name = "PO_NO", partName = "PO_NO") String PO_NO) {
		String sql = "select iph.STORER_CODE,bs.STORER_NAME,iph.WAREHOUSE_CODE,bw.WAREHOUSE_NAME,iph.PO_NO,iph.ERP_PO_NO,STATUS from inb_po_header iph  "
				+"inner join bas_storer bs on iph.STORER_CODE=bs.STORER_CODE "
				+"inner join bas_warehouse bw on iph.WAREHOUSE_CODE=bw.WAREHOUSE_CODE "
				+ "where iph.PO_NO like '%"+PO_NO+"%' order by iph.PO_NO desc limit 1 ";
		if (sqlValidate(sql)) {
			DataManager dm = DBOperator.DoSelect2DM(sql);
			String ret = DBOperator.DataManager2JSONString(dm, "");
			return ret;
		}
		return "";
	}
	
	/**
	 * ��ȡ�ֿ��б�
	 * 
	 * @param
	 * @return
	 */
	@WebResult(name = "return_getWarehouseList")
	public String getWarehouseList() {
		String sql = "select WAREHOUSE_CODE,WAREHOUSE_NAME,WAREHOUSE_TYPE,PORT_NO from bas_warehouse order by WAREHOUSE_CODE ";
		if (sqlValidate(sql)) {
			DataManager dm = DBOperator.DoSelect2DM(sql);
			String ret = DBOperator.DataManager2JSONString(dm, "");
			return ret;
		}
		return "";
	}
	
	/**
	 * д��PO��ϸ����
	 * 
	 * @param
	 * @return
	 */
	@WebResult(name = "return_savePODetail")
	public String savePODetail(@WebParam(name = "PO_NO", partName = "PO_NO") String PO_NO,
			@WebParam(name = "ERP_PO_NO", partName = "ERP_PO_NO") String ERP_PO_NO,
			@WebParam(name = "STORER_CODE", partName = "STORER_CODE") String STORER_CODE,
			@WebParam(name = "WAREHOUSE_CODE", partName = "WAREHOUSE_CODE") String WAREHOUSE_CODE,
			@WebParam(name = "ITEM_CODE", partName = "ITEM_CODE") String ITEM_CODE,
			@WebParam(name = "TOTAL_QTY", partName = "TOTAL_QTY") String TOTAL_QTY,
			@WebParam(name = "UOM", partName = "UOM") String UOM,
			@WebParam(name = "LOTTABLE01", partName = "LOTTABLE01") String LOTTABLE01,
			@WebParam(name = "LOTTABLE02", partName = "LOTTABLE02") String LOTTABLE02,
			@WebParam(name = "LOTTABLE03", partName = "LOTTABLE03") String LOTTABLE03,
			@WebParam(name = "LOTTABLE04", partName = "LOTTABLE04") String LOTTABLE04,
			@WebParam(name = "LOTTABLE05", partName = "LOTTABLE05") String LOTTABLE05,
			@WebParam(name = "LOTTABLE06", partName = "LOTTABLE06") String LOTTABLE06,
			@WebParam(name = "LOTTABLE07", partName = "LOTTABLE07") String LOTTABLE07,
			@WebParam(name = "LOTTABLE08", partName = "LOTTABLE08") String LOTTABLE08,
			@WebParam(name = "LOTTABLE09", partName = "LOTTABLE09") String LOTTABLE09,
			@WebParam(name = "LOTTABLE10", partName = "LOTTABLE10") String LOTTABLE10,
			@WebParam(name = "userCode", partName = "userCode") String userCode) {
		String //������ϸ
		sql = "insert into inb_po_detail(inb_po_header_id,line_number,po_no,erp_po_no,warehouse_code,storer_code,item_code,total_qty,uom,"
				+ "LOTTABLE01,LOTTABLE02,LOTTABLE03,LOTTABLE04,LOTTABLE05,LOTTABLE06,LOTTABLE07,LOTTABLE08,LOTTABLE09,LOTTABLE10,"
				+ "created_dtm_loc,created_by_user,updated_dtm_loc,updated_by_user) "
				+ "select (select inb_po_header_id from inb_po_header where  storer_code='"+STORER_CODE+"' and ERP_PO_NO='"
				+ ERP_PO_NO + "')," + "(select ifnull((select max(LINE_NUMBER)+1 from inb_po_detail where PO_NO='"+PO_NO+"' and STORER_CODE='"+STORER_CODE+"' and WAREHOUSE_CODE='"+WAREHOUSE_CODE+"'),1)),'" + PO_NO + "','" + ERP_PO_NO + "','"
				+ WAREHOUSE_CODE +"','"+STORER_CODE+"','"+ITEM_CODE+"','"+TOTAL_QTY+"','"+UOM+"'," 
				+ "'"+LOTTABLE01+"','"+LOTTABLE02+"','"+LOTTABLE03+"','"+LOTTABLE04+"','"+LOTTABLE05+"'," 
				+ "'"+LOTTABLE06+"','"+LOTTABLE07+"','"+LOTTABLE08+"','"+LOTTABLE09+"','"+LOTTABLE10+"'"  
				+ ",now(),'"+userCode+"',now(),'"+userCode+"' " ;
		if (sqlValidate(sql)) {
			int t = DBOperator.DoUpdate(sql);
			if(t==0){
				return "ERR-PO��ϸ����ʧ��";
			}else{
				//��¼������־
				DataManager dmProcess = comData.getSysProcessHistoryDataModel("sys_process_history");
				if (dmProcess!=null) {
					dmdata.xArrayList list = (xArrayList) dmProcess.getRow(0);
					list.set(dmProcess.getCol("SYS_PROCESS_HISTORY_ID"), "null");
					list.set(dmProcess.getCol("WAREHOUSE_CODE"), WAREHOUSE_CODE);
					list.set(dmProcess.getCol("PROCESS_CODE"), "savePODetail");
					list.set(dmProcess.getCol("PROCESS_NAME"), "д��PO��ϸ����");
					list.set(dmProcess.getCol("ITEM_CODE"), STORER_CODE);
					list.set(dmProcess.getCol("FROM_LOCATION_CODE"), "");
					list.set(dmProcess.getCol("FROM_CONTAINER_CODE"), "");
					list.set(dmProcess.getCol("QTY"), "0");
					list.set(dmProcess.getCol("CREATED_BY_USER"), userCode);
					list.set(dmProcess.getCol("PROCESS_TIME"), "now()");
					list.set(dmProcess.getCol("CREATED_DTM_LOC"), "now()");
					list.set(dmProcess.getCol("UPDATED_DTM_LOC"), "now()");
					dmProcess.RemoveRow(0);
					dmProcess.AddNewRow(list);
					boolean bool = comData.addSysProcessHistory("sys_process_history", dmProcess);
				}
				return "OK-PO��ϸ����ɹ�";
			}
		}
		return "ERR-PO��ϸ����ʧ��";
	}
	
	/**
	 * �����ջ�����ͷ
	 * 
	 * @param
	 * @return
	 */
	@WebResult(name = "return_createReceiptHeader")
	public String createReceiptHeader(@WebParam(name = "PO_NO", partName = "PO_NO") String PO_NO,
			@WebParam(name = "userCode", partName = "userCode") String userCode) {
		String sql = "select iph.INB_PO_HEADER_ID,iph.WAREHOUSE_CODE,bw.WAREHOUSE_NAME,iph.PO_NO,iph.ERP_PO_NO,iph.STORER_CODE,bs.STORER_NAME,iph.VENDOR_CODE,bv.VENDOR_NAME,iph.CREATED_DTM_LOC,iph.CREATED_BY_USER "
				+" ,sum(ifnull(ipd.TOTAL_QTY,0)-ifnull(ipd.RECEIVED_QTY,0)) openQty "
				+ " from inb_po_header iph "
				+ " inner join inb_po_detail ipd on iph.po_no=ipd.po_no and iph.storer_code=ipd.storer_code "
				+ " inner join bas_warehouse bw on iph.WAREHOUSE_CODE=bw.WAREHOUSE_CODE"
				+ " inner join bas_storer bs on iph.STORER_CODE=bs.STORER_CODE"
				+ " inner join bas_vendor bv on bv.VENDOR_CODE=iph.VENDOR_CODE" + " where iph.po_no ='"+PO_NO+"' ";
		DataManager dm = DBOperator.DoSelect2DM(sql);
		if(dm==null || dm.getCurrentCount()<=0){
			return "";
		}else{
			if(comData.str2Double(dm.getString("openQty", 0).toString())==0){
				return "ERR=PO��ȫ���ջ���ɣ���ѡ������PO";
			}
			sql = "select receipt_no from inb_receipt_header where po_no='"+PO_NO+"' and status='100' and CREATED_BY_USER='"+userCode+"' ";
			DataManager dm2 = DBOperator.DoSelect2DM(sql);
			if(dm2==null || dm2.getCurrentCount()<=0){
				String receiptNo = comData.getValueFromBasNumRule("inb_receipt_header", "RECEIPT_NO");
				sql = "insert into inb_receipt_header(RECEIPT_NO,INB_PO_HEADER_ID,PO_NO,ERP_PO_NO,WAREHOUSE_CODE,VENDOR_CODE,STORER_CODE,CREATED_DTM_LOC,CREATED_BY_USER )"
						+" select '"+receiptNo+"',INB_PO_HEADER_ID,PO_NO,ERP_PO_NO,WAREHOUSE_CODE,VENDOR_CODE,STORER_CODE,now(),'"+userCode+"' "
						+"from inb_po_header where po_no='"+PO_NO+"' ";
				int t = DBOperator.DoUpdate(sql);
				if(t>0){
					//��¼������־
					DataManager dmProcess = comData.getSysProcessHistoryDataModel("sys_process_history");
					if (dmProcess!=null) {
						dmdata.xArrayList list = (xArrayList) dmProcess.getRow(0);
						list.set(dmProcess.getCol("SYS_PROCESS_HISTORY_ID"), "null");
						list.set(dmProcess.getCol("WAREHOUSE_CODE"), "");
						list.set(dmProcess.getCol("PROCESS_CODE"), "createReceiptHeader");
						list.set(dmProcess.getCol("PROCESS_NAME"), "�����ջ�����ͷ");
						list.set(dmProcess.getCol("ITEM_CODE"), "");
						list.set(dmProcess.getCol("FROM_LOCATION_CODE"), "");
						list.set(dmProcess.getCol("FROM_CONTAINER_CODE"), "");
						list.set(dmProcess.getCol("QTY"), "0");
						list.set(dmProcess.getCol("REFERENCE_NO"), receiptNo);
						list.set(dmProcess.getCol("CREATED_BY_USER"), userCode);
						list.set(dmProcess.getCol("PROCESS_TIME"), "now()");
						list.set(dmProcess.getCol("CREATED_DTM_LOC"), "now()");
						list.set(dmProcess.getCol("UPDATED_DTM_LOC"), "now()");
						dmProcess.RemoveRow(0);
						dmProcess.AddNewRow(list);
						boolean bool = comData.addSysProcessHistory("sys_process_history", dmProcess);
					}
					return receiptNo;
				}else{
					return "";
				}
			}else{
				return dm2.getString("receipt_no", 0);
			}
			
		}
	}
	
	/**
	 * �����ջ�����ϸ
	 * 
	 * @param
	 * @return
	 */
	@WebResult(name = "return_createReceiptDetail")
	public String createReceiptDetail(@WebParam(name = "RECEIPT_NO", partName = "RECEIPT_NO") String RECEIPT_NO,
			@WebParam(name = "PO_NO", partName = "PO_NO") String PO_NO,
			@WebParam(name = "ITEM_BAR_CODE", partName = "ITEM_BAR_CODE") String ITEM_BAR_CODE,
			@WebParam(name = "CONTAINER_CODE", partName = "CONTAINER_CODE") String CONTAINER_CODE,
			@WebParam(name = "SCAN_QTY", partName = "SCAN_QTY") String SCAN_QTY,
			@WebParam(name = "userCode", partName = "userCode") String userCode) {
		String receiptNo = RECEIPT_NO;
		String poNo = PO_NO;
		String itemBarCode = ITEM_BAR_CODE;
		String containerCode = CONTAINER_CODE;
		if(SCAN_QTY.equals("")){
			SCAN_QTY = "0";
		}
		
		String sql = "select iph.INB_PO_HEADER_ID,ipd.INB_PO_DETAIL_ID,"
				+"iph.WAREHOUSE_CODE,iph.STORER_CODE,iph.PO_NO,ipd.LINE_NUMBER,ipd.ITEM_CODE,'"+CONTAINER_CODE.trim()+"'"
				+" from inb_po_header iph "
				+"inner join inb_po_detail ipd on iph.PO_NO=ipd.PO_NO and iph.INB_PO_HEADER_ID=ipd.INB_PO_HEADER_ID "
				+"inner join bas_item bi on ipd.ITEM_CODE=bi.ITEM_CODE "
				+"left join bas_item_unit biu on bi.UNIT_CODE=biu.unit_code "
				+"where ipd.PO_NO='"+poNo+"' and bi.ITEM_BAR_CODE='"+itemBarCode+"' "
				+" and ifnull(ipd.TOTAL_QTY,0)-ifnull(ipd.RECEIVED_QTY,0)>0 "
				+"order by ifnull(ipd.TOTAL_QTY,0)-ifnull(ipd.RECEIVED_QTY,0) "
				+"limit 1 ";
		DataManager dmtmp = DBOperator.DoSelect2DM(sql);
		if(dmtmp==null || dmtmp.getCurrentCount()<=0){
			return "ERR=PO����ȷ����ѡ������PO";
		}else{
			String warehouseCode = dmtmp.getString("WAREHOUSE_CODE", 0);
			String itemCode = dmtmp.getString("ITEM_CODE", 0);
			String poLineNumber = dmtmp.getString("LINE_NUMBER", 0);
			sql = "select ird.receipt_no from inb_receipt_detail ird "
			+"where ird.receipt_no='"+receiptNo+"' and ird.item_code='"+itemCode+"' and ird.PO_LINE_NO='"+poLineNumber+"' and ird.container_code='"+containerCode+"' ";
			DataManager dmtmp2 = DBOperator.DoSelect2DM(sql);
			if(dmtmp2==null || dmtmp2.getCurrentCount()<=0){
				//�����ջ�����ϸ��
				sql = "insert into inb_receipt_detail(INB_RECEIPT_HEADER_ID,RECEIPT_NO,INB_PO_HEADER_ID,INB_PO_DETAIL_ID,"
						+"WAREHOUSE_CODE,STORER_CODE,PO_NO,PO_LINE_NO,RECEIPT_LINE_NO,ITEM_CODE,CONTAINER_CODE,LOCATION_CODE"
						+",LOTTABLE01,LOTTABLE02,LOTTABLE03,LOTTABLE04,LOTTABLE05,LOTTABLE06,LOTTABLE07,LOTTABLE08,LOTTABLE09,LOTTABLE10"
						+",TOTAL_QTY,TOTAL_UOM,RECEIVED_QTY,RECEIVED_UOM,CREATED_DTM_LOC,CREATED_BY_USER) "
						
						+"select (select INB_RECEIPT_HEADER_ID from inb_receipt_header where RECEIPT_NO='"+receiptNo+"' limit 1),'"+receiptNo+"',iph.INB_PO_HEADER_ID,ipd.INB_PO_DETAIL_ID,"
						+"iph.WAREHOUSE_CODE,iph.STORER_CODE,iph.PO_NO,ipd.LINE_NUMBER,ifnull((select count(1) from inb_receipt_detail where RECEIPT_NO='"+receiptNo+"'),0)+1,ipd.ITEM_CODE,'"+CONTAINER_CODE+"',"
						+"(select location_code from bas_location where warehouse_code='"+warehouseCode+"' and location_type_code='Dock' order by location_code limit 1 )"
						+",ipd.LOTTABLE01,ipd.LOTTABLE02,ipd.LOTTABLE03,ipd.LOTTABLE04,ipd.LOTTABLE05,ipd.LOTTABLE06,ipd.LOTTABLE07,ipd.LOTTABLE08,ipd.LOTTABLE09,ipd.LOTTABLE10"
						+",ipd.TOTAL_QTY,ipd.UOM,"+SCAN_QTY+",biu.unit_name,now(),'"+userCode+"' "
						+"from inb_po_header iph "
						+"inner join inb_po_detail ipd on iph.PO_NO=ipd.PO_NO and iph.INB_PO_HEADER_ID=ipd.INB_PO_HEADER_ID "
						+"inner join bas_item bi on ipd.ITEM_CODE=bi.ITEM_CODE "
						+"left join bas_item_unit biu on bi.UNIT_CODE=biu.unit_code "
						+"where ipd.PO_NO='"+poNo+"' and bi.ITEM_BAR_CODE='"+itemBarCode+"' "
						+" and ifnull(ipd.TOTAL_QTY,0)-ifnull(ipd.RECEIVED_QTY,0)>0 "
						+"order by ifnull(ipd.TOTAL_QTY,0)-ifnull(ipd.RECEIVED_QTY,0) "
						+"limit 1 ";
			}else{
				//�����ջ�����ϸ��
				Double scanQty = 0.0;
				try{
					scanQty = Double.parseDouble(SCAN_QTY);
				}catch(Exception e){
					scanQty = 1.0;
				}
				
				if(Double.parseDouble(comData.getPODetailLineNumberAvailableQty(poNo,itemCode,poLineNumber))-scanQty<0){
					return "ERR=���ܳ���PO��ϸ����";
				}
				//�����ջ�����ϸ����
				sql = "update inb_receipt_detail set RECEIVED_QTY=RECEIVED_QTY+("+SCAN_QTY+") "
						+"where receipt_no='"+receiptNo+"' and item_code='"+itemCode+"' and PO_LINE_NO='"+poLineNumber+"' "
						+"and container_code='"+containerCode+"' ";
			}
			int t = DBOperator.DoUpdate(sql);
			if(t>0){
				//�ջ�����ϸ�����ɹ��󣬸���PO��ϸʵ���ջ�����
				sql = "update inb_po_detail set status='300',RECEIVED_QTY=RECEIVED_QTY+("+SCAN_QTY+") "
						+" where PO_NO='"+poNo+"' and LINE_NUMBER="+poLineNumber+" and ITEM_CODE='"+itemCode+"' ";
				int poUpdate = DBOperator.DoUpdate(sql);
				if(poUpdate==1){
					//����PO��ͷ״̬=300 ����ջ���
					sql = "update inb_po_header set status='300' where PO_NO='"+poNo+"' and status='100' ";
					DBOperator.DoUpdate(sql);
					sql = "update bas_container set status='1' where container_code='"+containerCode+"' ";
					DBOperator.DoUpdate(sql);
					//��¼������־
					DataManager dmProcess = comData.getSysProcessHistoryDataModel("sys_process_history");
					if (dmProcess!=null) {
						dmdata.xArrayList list = (xArrayList) dmProcess.getRow(0);
						list.set(dmProcess.getCol("SYS_PROCESS_HISTORY_ID"), "null");
						list.set(dmProcess.getCol("WAREHOUSE_CODE"), warehouseCode);
						list.set(dmProcess.getCol("PROCESS_CODE"), "createReceiptDetail");
						list.set(dmProcess.getCol("PROCESS_NAME"), "�����ջ�����ϸ");
						list.set(dmProcess.getCol("ITEM_CODE"), itemCode);
						list.set(dmProcess.getCol("FROM_LOCATION_CODE"), "");
						list.set(dmProcess.getCol("FROM_CONTAINER_CODE"), CONTAINER_CODE);
						list.set(dmProcess.getCol("QTY"), SCAN_QTY);
						list.set(dmProcess.getCol("REFERENCE_NO"), receiptNo);
						list.set(dmProcess.getCol("REFERENCE_LINE_NO"), "");
						list.set(dmProcess.getCol("CREATED_BY_USER"), userCode);
						list.set(dmProcess.getCol("PROCESS_TIME"), "now()");
						list.set(dmProcess.getCol("CREATED_DTM_LOC"), "now()");
						list.set(dmProcess.getCol("UPDATED_DTM_LOC"), "now()");
						dmProcess.RemoveRow(0);
						dmProcess.AddNewRow(list);
						boolean bool = comData.addSysProcessHistory("sys_process_history", dmProcess);
					}
					return "OK-SUCCESS";
				}else{
					return "ERR-����PO��ϸʵ���ջ�����ʧ��";
				}
			}else{
				return "ERR-������ⵥ��ϸʧ��";
			}
		}
	}
	
	/**
	 * �ջ����
	 * 
	 * @param
	 * @return
	 */
	@WebResult(name = "return_generateInventory")
	public String generateInventoryByReceived(@WebParam(name = "receiptNo", partName = "receiptNo") String receiptNo,
			@WebParam(name = "userCode", partName = "userCode") String userCode) {
		boolean bool = comData.generateInventory(receiptNo,userCode);
		if(bool){
			String sql = "select RECEIPT_NO from inb_receipt_detail where receipt_no='"+receiptNo+"' ";
			DataManager dm = DBOperator.DoSelect2DM(sql);
			if(dm==null || dm.getCurrentCount()==0){
				return "ERR-������������Ҫ�Ȱ��س������ύ���ݺ���ܵ�����ջ���ɡ�";
			}
			sql = "update inb_receipt_header set status='900',UPDATED_DTM_LOC=now(),UPDATED_BY_USER='"+userCode+"' where receipt_no='"+receiptNo+"'";
			int updateRow = DBOperator.DoUpdate(sql);
			if(updateRow==1){
				//������װ��״̬=2
				sql = "update bas_container set status='2',UPDATED_DTM_LOC=now(),UPDATED_BY_USER='"+userCode+"' where status in('0','1') and container_code in (select distinct container_code from inb_receipt_detail where receipt_no='"+receiptNo+"') ";
				DBOperator.DoUpdate(sql);
			}
			return "OK-success";
		}else{
			return "ERR-�ջ���ϸ���浽�ݴ��λʧ��";
		}
	}
	
	/**
	 * ����ջ���װ����Ƿ���ȷ
	 * 
	 * @param
	 * @return
	 */
	@WebResult(name = "return_checkReceivedContainerCode")
	public String checkReceivedContainerCode(@WebParam(name = "CONTAINER_CODE", partName = "CONTAINER_CODE") String CONTAINER_CODE) {
		String sql = "select distinct ii.container_code,ii.item_code,bi.item_bar_code,bi.item_name,ii.on_hand_qty "
				+"from inv_inventory ii inner join bas_item bi on ii.storer_code=bi.storer_code and ii.item_code=bi.item_code "
				+"inner join bas_location bl on ii.warehouse_code=bl.warehouse_code and ii.location_code=bl.location_code "
				+"inner join bas_container bc on ii.warehouse_code=bc.warehouse_code and ii.container_code=bc.container_code "
				+" where bl.location_type_code='Dock' and bc.status='2' and ii.container_code='"+CONTAINER_CODE+"' limit 1 ";
		if (sqlValidate(sql)) {
			DataManager dm = DBOperator.DoSelect2DM(sql);
			String ret = DBOperator.DataManager2JSONString(dm, "");
			return ret;
		}
		return "";
	}
	
	/**
	 * ��ȡϵͳ�Ƽ��ϼܿ�λ
	 * 
	 * @param
	 * @return
	 */
	@WebResult(name = "return_getSysSuggestLocation")
	public String getSysSuggestLocation(@WebParam(name = "CONTAINER_CODE", partName = "CONTAINER_CODE") String CONTAINER_CODE) {
		String sql = "select ii.LOCATION_CODE from inb_receipt_detail ird "
				+"left outer join inv_inventory ii on ird.WAREHOUSE_CODE=ii.WAREHOUSE_CODE and ii.STORER_CODE=ird.STORER_CODE and ii.ITEM_CODE=ird.ITEM_CODE "
				+"left outer join bas_location bl on bl.location_code=ii.location_code and bl.warehouse_code=ii.warehouse_code "
				+"where bl.location_type_code='Normal' and ird.CONTAINER_CODE='"+CONTAINER_CODE+"' order by ii.ON_HAND_QTY limit 1 ";
		if (sqlValidate(sql)) {
			DataManager dm = DBOperator.DoSelect2DM(sql);
			String ret = DBOperator.DataManager2JSONString(dm, "");
			return ret;
		}
		return "";
	}
	
	/**
	 * ����ϼ�Ŀ���λ�Ƿ���ȷ
	 * 
	 * @param
	 * @return
	 */
	@WebResult(name = "return_checkPutawayDestLocationCode")
	public String checkPutawayDestLocationCode(@WebParam(name = "LOCATION_CODE", partName = "LOCATION_CODE") String LOCATION_CODE) {
		String sql = "select location_code from bas_location where location_type_code='Normal' and location_code = '"+LOCATION_CODE+"' ";
		if (sqlValidate(sql)) {
			DataManager dm = DBOperator.DoSelect2DM(sql);
			String ret = DBOperator.DataManager2JSONString(dm, "");
			return ret;
		}
		return "";
	}
	
	/**
	 * ����ϼ��ƿ�
	 * 
	 * @param
	 * @return
	 */
	@WebResult(name = "return_putawayConfirm")
	public String putawayConfirm(@WebParam(name = "LOCATION_CODE", partName = "LOCATION_CODE") String LOCATION_CODE,
			@WebParam(name = "CONTAINER_CODE", partName = "CONTAINER_CODE") String CONTAINER_CODE,
			@WebParam(name = "userCode", partName = "userCode") String userCode) {
		String sql = "select WAREHOUSE_CODE,container_code from inv_inventory where container_code='"+CONTAINER_CODE+"' ";
		DataManager tmpDM = DBOperator.DoSelect2DM(sql);
		if(tmpDM==null || tmpDM.getCurrentCount()==0){
			return "ERR-���δ�ջ��������ϼ�";
		}
//		if(tmpDM.getCurrentCount()>1){
//			return "ERR-�����֮ǰ���ջ����δ�ϼܵ����񣬲����ظ�ʹ�ø�����ϼ�";
//		}
		String WAREHOUSE_CODE = tmpDM.getString("WAREHOUSE_CODE", 0);
		if(!comData.checkPOCanPutaway(WAREHOUSE_CODE,CONTAINER_CODE)){
			return "ERR-PO��Ҫ��AOS��ʵ����˺�������ϼܲ���";
		}
		sql = "update inv_inventory set location_code='"+LOCATION_CODE+"' "
			+ "where WAREHOUSE_CODE='"+WAREHOUSE_CODE+"' and container_code='"+CONTAINER_CODE+"' ";
		int t = DBOperator.DoUpdate(sql);
		if(t==0){
			return "ERR-�ϼ�ʧ�ܣ�δ�ҵ�����,��λ��"+LOCATION_CODE+"����ţ�"+CONTAINER_CODE+"������ϵϵͳ����Ա";
		}else{
			//�����װ�� use_type=temp (��ʱ)���ϼܺ���¿��Container_code = '*'��ͬʱ�ջ���װ��  status='0'
			sql = "select ii.INV_INVENTORY_ID,ii.WAREHOUSE_CODE,ii.STORER_CODE,ii.ITEM_CODE,ii.LOT_NO,ii.LOCATION_CODE,bc.USE_TYPE,ii.CONTAINER_CODE,ii.ON_HAND_QTY "
				+"from inv_inventory ii inner join bas_container bc on ii.WAREHOUSE_CODE=bc.WAREHOUSE_CODE and ii.CONTAINER_CODE=bc.CONTAINER_CODE "
				+"where ii.WAREHOUSE_CODE='"+WAREHOUSE_CODE+"' and ii.LOCATION_CODE='"+LOCATION_CODE+"' and ii.CONTAINER_CODE='"+CONTAINER_CODE+"' ";
			DataManager dm = DBOperator.DoSelect2DM(sql);
			if(dm==null || dm.getCurrentCount()==0){
				return "ERR-�ϼ�ʧ�ܣ����µ��ϼܿ�λʧ�ܣ�����ϵϵͳ����Ա";
			}
			for(int i=0;i<dm.getCurrentCount();i++){
				WAREHOUSE_CODE = dm.getString("WAREHOUSE_CODE", i);
				String INV_INVENTORY_ID = dm.getString("INV_INVENTORY_ID", i);
				String USE_TYPE = dm.getString("USE_TYPE", i);
				String STORER_CODE = dm.getString("STORER_CODE", i);
				String ITEM_CODE = dm.getString("ITEM_CODE", i);
				String LOT_NO = dm.getString("LOT_NO", i);
				String ON_HAND_QTY = dm.getString("ON_HAND_QTY", i);
				
				String tmp = "";
				//�ж���ת���Ƿ�����ʱ��ת�䣬�������Ҫ���ӿ�棬����ֱ�Ӱ���ת��������
				if(USE_TYPE.equalsIgnoreCase("temp")){
					//���жϵ�ǰ����container_code=* �Ƿ��п�棬����У��������ۼӣ������ֱ�Ӹ��¿���container_code=*
					tmp = comData.getInventoryID(WAREHOUSE_CODE,STORER_CODE,ITEM_CODE,LOT_NO,LOCATION_CODE,"*",ON_HAND_QTY,userCode);
					if(!tmp.equals("")){
						//�ͷ����״̬
						sql = "update bas_container set status='0',UPDATED_BY_USER='"+userCode+"',UPDATED_DTM_LOC=now(),USER_DEF1='����ϼܳɹ�' where container_code='"+CONTAINER_CODE+"' ";
						t = DBOperator.DoUpdate(sql);
						if(t>=0){
							//�ϼܳɹ���ԭ�ȿ���м�¼(container_code <> *)
							sql = "delete from inv_inventory "
								+ "where INV_INVENTORY_ID='"+INV_INVENTORY_ID+"' and LOCATION_CODE='"+LOCATION_CODE+"' and CONTAINER_CODE='"+CONTAINER_CODE+"' ";
							t = DBOperator.DoUpdate(sql);
							//���Ŀ�λΪ��ʹ��״̬
							sql = "update bas_location set STATUS='storage',UPDATED_BY_USER='"+userCode+"',UPDATED_DTM_LOC=now(),USER_DEF1='����ϼ�' "
								+"where status='empty' and LOCATION_CODE='"+LOCATION_CODE+"' and WAREHOUSE_CODE in "
								+"(select WAREHOUSE_CODE from inv_inventory where container_code='"+CONTAINER_CODE+"') ";
							DBOperator.DoUpdate(sql);
						}else{
							return "ERR-�ϼ�ʧ�ܣ��ͷ���ţ�"+CONTAINER_CODE+" ʧ�ܣ�����ϵϵͳ����Ա";
						}
					}else{
						return "ERR-�ϼ�ʧ�ܣ�����ϵϵͳ����Ա";
					}
				}else{
					//�������
					sql = "update bas_container set UPDATED_DTM_LOC=now(),USER_DEF1='����ϼܳɹ�' where container_code='"+CONTAINER_CODE+"' ";
					t = DBOperator.DoUpdate(sql);
					if(t>=0){
						//���Ŀ�λΪ��ʹ��״̬
						sql = "update bas_location set STATUS='storage',UPDATED_BY_USER='"+userCode+"',UPDATED_DTM_LOC=now(),USER_DEF1='����ϼ�' "
							+"where status='empty' and LOCATION_CODE='"+LOCATION_CODE+"' and WAREHOUSE_CODE in "
							+"(select WAREHOUSE_CODE from inv_inventory where container_code='"+CONTAINER_CODE+"') ";
						DBOperator.DoUpdate(sql);
						//��¼������־
						DataManager dmProcess = comData.getSysProcessHistoryDataModel("sys_process_history");
						if (dmProcess!=null) {
							dmdata.xArrayList list = (xArrayList) dmProcess.getRow(0);
							list.set(dmProcess.getCol("SYS_PROCESS_HISTORY_ID"), "null");
							list.set(dmProcess.getCol("WAREHOUSE_CODE"), WAREHOUSE_CODE);
							list.set(dmProcess.getCol("PROCESS_CODE"), "PUTAWAY");
							list.set(dmProcess.getCol("PROCESS_NAME"), "�ϼ�");
							list.set(dmProcess.getCol("ITEM_CODE"), STORER_CODE);
							list.set(dmProcess.getCol("FROM_LOCATION_CODE"), LOCATION_CODE);
							list.set(dmProcess.getCol("FROM_CONTAINER_CODE"), CONTAINER_CODE);
							list.set(dmProcess.getCol("QTY"), "0");
							list.set(dmProcess.getCol("CREATED_BY_USER"), userCode);
							list.set(dmProcess.getCol("PROCESS_TIME"), "now()");
							list.set(dmProcess.getCol("CREATED_DTM_LOC"), "now()");
							list.set(dmProcess.getCol("UPDATED_DTM_LOC"), "now()");
							dmProcess.RemoveRow(0);
							dmProcess.AddNewRow(list);
							boolean bool = comData.addSysProcessHistory("sys_process_history", dmProcess);
						}
					}else{
						return "ERR-�ϼ�ʧ�ܣ��ͷ���ţ�"+CONTAINER_CODE+" ʧ�ܣ�����ϵϵͳ����Ա";
					}
				}
			}
			return "OK-�ϼܳɹ�";//ѭ��ȫ��������������쳣�������ϼܳɹ�
//			else{
//				return "ERR-�ϼ�ʧ��\n��ǰ��λ��"+LOCATION_CODE+"����ţ�"+CONTAINER_CODE+" \n֮ǰ��δ��ɵ��ϼ���������ϵϵͳ����Ա";
//			}

		}
	}
	
	/**
	 * ����ѯ
	 * 
	 * @param
	 * @return
	 */
	@WebResult(name = "return_getInvList")
	public String getInvList(@WebParam(name = "LOCATION_CODE", partName = "LOCATION_CODE") String LOCATION_CODE,
			@WebParam(name = "ITEM_BAR_CODE", partName = "ITEM_BAR_CODE") String ITEM_BAR_CODE) {
		String sql = "select case when ifnull(ii.LOCATION_CODE,'')='' then '.' else ii.LOCATION_CODE end LOCATION_CODE"
				+ ",bi.ITEM_BAR_CODE,bi.ITEM_NAME,sum(ii.ON_HAND_QTY+ii.IN_TRANSIT_QTY-(ii.PICKED_QTY)-(ii.ALLOCATED_QTY)) QTY,biu.unit_name "
				+"from inv_inventory ii "
				+"inner join bas_item bi on ii.STORER_CODE=bi.STORER_CODE and ii.ITEM_CODE=bi.ITEM_CODE "
				+"left join bas_item_unit biu on bi.UNIT_CODE=biu.unit_code "
				+"where ii.STATUS='ACTIVE' and ii.ON_HAND_QTY>0 and "
				+ "ii.LOCATION_CODE like '%"+LOCATION_CODE+"%' and bi.ITEM_BAR_CODE like '%"+ITEM_BAR_CODE+"%' "
				+"GROUP BY ii.LOCATION_CODE,bi.ITEM_BAR_CODE,bi.ITEM_NAME ";
		if (sqlValidate(sql)) {
			DataManager dm = DBOperator.DoSelect2DM(sql);
			String ret = DBOperator.DataManager2JSONString(dm, "");
			return ret;
		}
		return "";
	}
	
	/**
	 * ����ѯ-���ֿ��ѯ
	 * 
	 * @param
	 * @return
	 */
	@WebResult(name = "return_getInvList")
	public String getInvListByWarehouse(@WebParam(name = "WAREHOUSE_CODE", partName = "WAREHOUSE_CODE") String WAREHOUSE_CODE,
			@WebParam(name = "LOCATION_CODE", partName = "LOCATION_CODE") String LOCATION_CODE,
			@WebParam(name = "ITEM_BAR_CODE", partName = "ITEM_BAR_CODE") String ITEM_BAR_CODE) {
		String sql = "select case when ifnull(ii.LOCATION_CODE,'')='' then '.' else ii.LOCATION_CODE end LOCATION_CODE"
				+ ",bi.ITEM_BAR_CODE,bi.ITEM_NAME,sum(ii.ON_HAND_QTY+ii.IN_TRANSIT_QTY-(ii.PICKED_QTY)-(ii.ALLOCATED_QTY)) QTY,biu.unit_name "
				+"from inv_inventory ii "
				+"inner join bas_item bi on ii.STORER_CODE=bi.STORER_CODE and ii.ITEM_CODE=bi.ITEM_CODE "
				+"left join bas_item_unit biu on bi.UNIT_CODE=biu.unit_code "
				+"where ii.STATUS='ACTIVE' and ii.ON_HAND_QTY>0 and (ii.ON_HAND_QTY-(ii.ALLOCATED_QTY)-(ii.PICKED_QTY))>0 and  "
				+ "ii.LOCATION_CODE like '%"+LOCATION_CODE+"%' and bi.ITEM_BAR_CODE like '%"+ITEM_BAR_CODE+"%' "
				+ "and ii.WAREHOUSE_CODE like '%"+WAREHOUSE_CODE+"%' "
				+"GROUP BY ii.LOCATION_CODE,bi.ITEM_BAR_CODE,bi.ITEM_NAME ";
		if (sqlValidate(sql)) {
			DataManager dm = DBOperator.DoSelect2DM(sql);
			String ret = DBOperator.DataManager2JSONString(dm, "");
			return ret;
		}
		return "";
	}
	
	/**
	 * �ƿ�
	 * 
	 * @param
	 * @return
	 */
	@WebResult(name = "return_getInvList")
	public String invMove(@WebParam(name = "WAREHOUSE_CODE", partName = "WAREHOUSE_CODE") String WAREHOUSE_CODE,
			@WebParam(name = "FROM_LOCATION_CODE", partName = "FROM_LOCATION_CODE") String FROM_LOCATION_CODE,
			@WebParam(name = "FROM_CONTAINER_CODE", partName = "FROM_CONTAINER_CODE") String FROM_CONTAINER_CODE,
			@WebParam(name = "ITEM_BAR_CODE", partName = "ITEM_BAR_CODE") String ITEM_BAR_CODE,
			@WebParam(name = "QTY", partName = "QTY") String QTY,
			@WebParam(name = "DEST_LOCATION_CODE", partName = "DEST_LOCATION_CODE") String DEST_LOCATION_CODE,
			@WebParam(name = "DEST_CONTAINER_CODE", partName = "DEST_CONTAINER_CODE") String DEST_CONTAINER_CODE,
			@WebParam(name = "userCode", partName = "userCode") String userCode) {
		boolean isDamage = false;
		// ����Ƿ�Ŀ�Ŀ�λΪ�дο�λ
		String sql = "select location_code,location_type_code from bas_location " + "where warehouse_code='"
				+ WAREHOUSE_CODE + "' and location_code='" + DEST_LOCATION_CODE + "' and location_type_code='Damage' ";
		DataManager dm = DBOperator.DoSelect2DM(sql);
		if (dm == null || dm.getCurrentCount() == 0) {
			//�����κδ���
		} else {
			isDamage = true;
		}
		sql = "select ii.STORER_CODE,ii.ITEM_CODE,ii.lot_no,ii.INV_INVENTORY_ID,"
				+"ii.ON_HAND_QTY+ii.IN_TRANSIT_QTY-(ii.ALLOCATED_QTY)-(ii.PICKED_QTY) availableQty "
				+"from inv_inventory ii "
				+"inner join bas_location bl on ii.LOCATION_CODE=bl.LOCATION_CODE and ii.WAREHOUSE_CODE=bl.WAREHOUSE_CODE "
				+"inner join bas_item bi on ii.ITEM_CODE=bi.ITEM_CODE and ii.STORER_CODE=bi.STORER_CODE "
				+"inner join bas_storer bs on ii.STORER_CODE=bs.STORER_CODE "
				+"inner join bas_warehouse bw on ii.WAREHOUSE_CODE=bw.WAREHOUSE_CODE "
				+"where ii.WAREHOUSE_CODE='"+WAREHOUSE_CODE+"' and ii.LOCATION_CODE='"+FROM_LOCATION_CODE+"' "
				+" and ii.CONTAINER_CODE='"+FROM_CONTAINER_CODE+"' and bi.ITEM_BAR_CODE='"+ITEM_BAR_CODE+"'";
		dm = DBOperator.DoSelect2DM(sql);
		if (dm == null || dm.getCurrentCount() == 0) {
			return "ERR-δ�ҵ�ԭ��λ�Ŀ����Ϣ��ϵͳ�޷��ƿ�";
		}else if(dm.getCurrentCount()>1){
			return "ERR-�������������λ�ж��п���¼\n��ʹ�õ��Կͻ��˽����ƿ�";
		}
		String itemCodeFrom = dm.getString("ITEM_CODE", 0);
		String lotNo = dm.getString("lot_no", 0);
		String fromInvID = dm.getString("INV_INVENTORY_ID", 0);
		String availableQty = dm.getString("availableQty", 0);
		if(isDamage){
			//�Ȳ�ѯĿ���λ�Ƿ���ڼ�¼
			sql = "select INV_INVENTORY_ID from inv_inventory "
					+ "where warehouse_code='"+WAREHOUSE_CODE+"' and location_code='" + DEST_LOCATION_CODE + "' and container_code='" + DEST_CONTAINER_CODE
					+ "' and item_code='" + itemCodeFrom.toString().trim() + "' and LOT_NO = '"+lotNo+"' ";
			DataManager dm2 = DBOperator.DoSelect2DM(sql);
			if(comData.str2Double(QTY)>comData.str2Double(availableQty)){
				return "ERR-��������ԭ������������ܲ���";
			}
			//�����ھͲ���Ŀ���λ�¼�¼   ��������
			if(dm2==null || dm2.getCurrentCount()==0){
				sql = "insert into inv_inventory(WAREHOUSE_CODE,STORER_CODE,ITEM_CODE,ITEM_NAME,INV_LOT_ID,LOT_NO,LOCATION_CODE"
						+ ",CONTAINER_CODE,INACTIVE_QTY,INB_TOTAL_QTY,CREATED_BY_USER,CREATED_DTM_LOC) "
						+"select WAREHOUSE_CODE,STORER_CODE,ITEM_CODE,ITEM_NAME,INV_LOT_ID,LOT_NO,'"+DEST_LOCATION_CODE+"',"
						+"'"+DEST_CONTAINER_CODE+"',"+QTY+","+QTY+",'"+userCode+"',now() "
						+" from inv_inventory where warehouse_code='"+WAREHOUSE_CODE+"' and location_code='" + FROM_LOCATION_CODE + "' and container_code='" + FROM_CONTAINER_CODE
						+ "' and item_code='" + itemCodeFrom.toString().trim() + "' and LOT_NO = '"+lotNo+"' ";
				int insertCount = DBOperator.DoUpdate(sql);
				if(insertCount==0){
					return "ERR-���ݲ���Ŀ���λʧ��";
				}else{
					//���ھͿۼ�ԭ��λ����
					sql = "update inv_inventory set ON_HAND_QTY=ON_HAND_QTY-("+QTY+"),"
							+ "OUB_TOTAL_QTY=OUB_TOTAL_QTY+("+QTY+") "
							+ "where INV_INVENTORY_ID='"+fromInvID+"' and warehouse_code='"+WAREHOUSE_CODE+"' and location_code='" + FROM_LOCATION_CODE + "' and container_code='" + FROM_CONTAINER_CODE
							+ "' and item_code='" + itemCodeFrom.toString().trim() + "' and LOT_NO = '"+lotNo+"' ";
					//ִ�п�ԭ��λ���
					int t = DBOperator.DoUpdate(sql);
					if(t>0){
						sql = "update bas_container set status='2' where container_code='"+DEST_CONTAINER_CODE+"' ";
						DBOperator.DoUpdate(sql);
						//��¼������־
						DataManager dmProcess = comData.getSysProcessHistoryDataModel("sys_process_history");
						if (dmProcess!=null) {
							dmdata.xArrayList list = (xArrayList) dmProcess.getRow(0);
							list.set(dmProcess.getCol("SYS_PROCESS_HISTORY_ID"), "null");
							list.set(dmProcess.getCol("WAREHOUSE_CODE"), WAREHOUSE_CODE);
							list.set(dmProcess.getCol("PROCESS_CODE"), "INV_MOVE");
							list.set(dmProcess.getCol("PROCESS_NAME"), "�ƿ�");
							list.set(dmProcess.getCol("ITEM_CODE"), itemCodeFrom);
							list.set(dmProcess.getCol("FROM_LOCATION_CODE"), FROM_LOCATION_CODE);
							list.set(dmProcess.getCol("TO_LOCATION_CODE"), DEST_LOCATION_CODE);
							list.set(dmProcess.getCol("FROM_CONTAINER_CODE"), FROM_CONTAINER_CODE);
							list.set(dmProcess.getCol("TO_CONTAINER_CODE"), DEST_CONTAINER_CODE);
							list.set(dmProcess.getCol("QTY"), QTY);
							list.set(dmProcess.getCol("CREATED_BY_USER"), userCode);
							list.set(dmProcess.getCol("PROCESS_TIME"), "now()");
							list.set(dmProcess.getCol("CREATED_DTM_LOC"), "now()");
							list.set(dmProcess.getCol("UPDATED_DTM_LOC"), "now()");
							dmProcess.RemoveRow(0);
							dmProcess.AddNewRow(list);
							boolean bool = comData.addSysProcessHistory("sys_process_history", dmProcess);
						}
						return "OK-�ƿ�����ɹ�";
					}
				}
			}else{
				//���� ����Ŀ���λ�������    ��������
				String toInvID = dm2.getString("INV_INVENTORY_ID", 0);
				sql = "update inv_inventory set INACTIVE_QTY=INACTIVE_QTY+("+QTY+"),"
						+ "INB_TOTAL_QTY=INB_TOTAL_QTY+("+QTY+") "
						+ "where INV_INVENTORY_ID='"+toInvID+"' and warehouse_code='"+WAREHOUSE_CODE+"' and location_code='" + DEST_LOCATION_CODE + "' and container_code='" + DEST_CONTAINER_CODE
						+ "' and item_code='" + itemCodeFrom.toString().trim() + "' and LOT_NO = '"+lotNo+"' ";
				int insertCount = DBOperator.DoUpdate(sql);
				if(insertCount==0){
					return "ERR-���ݸ��µ�Ŀ���λʧ��";
				}else{
					//���ھͿۼ�ԭ��λ����
					sql = "update inv_inventory set ON_HAND_QTY=ON_HAND_QTY-("+QTY+"),"
							+ "OUB_TOTAL_QTY=OUB_TOTAL_QTY+("+QTY+") "
							+ "where INV_INVENTORY_ID='"+fromInvID+"' and warehouse_code='"+WAREHOUSE_CODE+"' and location_code='" + FROM_LOCATION_CODE + "' and container_code='" + FROM_CONTAINER_CODE
							+ "' and item_code='" + itemCodeFrom.toString().trim() + "' and LOT_NO = '"+lotNo+"' ";
					//ִ�п�ԭ��λ���
					int t = DBOperator.DoUpdate(sql);
					if(t>0){
						sql = "update bas_container set status='2' where container_code='"+DEST_CONTAINER_CODE+"' ";
						DBOperator.DoUpdate(sql);
						//��¼������־
						DataManager dmProcess = comData.getSysProcessHistoryDataModel("sys_process_history");
						if (dmProcess!=null) {
							dmdata.xArrayList list = (xArrayList) dmProcess.getRow(0);
							list.set(dmProcess.getCol("SYS_PROCESS_HISTORY_ID"), "null");
							list.set(dmProcess.getCol("WAREHOUSE_CODE"), WAREHOUSE_CODE);
							list.set(dmProcess.getCol("PROCESS_CODE"), "INV_MOVE");
							list.set(dmProcess.getCol("PROCESS_NAME"), "�ƿ�");
							list.set(dmProcess.getCol("ITEM_CODE"), itemCodeFrom);
							list.set(dmProcess.getCol("FROM_LOCATION_CODE"), FROM_LOCATION_CODE);
							list.set(dmProcess.getCol("TO_LOCATION_CODE"), DEST_LOCATION_CODE);
							list.set(dmProcess.getCol("FROM_CONTAINER_CODE"), FROM_CONTAINER_CODE);
							list.set(dmProcess.getCol("TO_CONTAINER_CODE"), DEST_CONTAINER_CODE);
							list.set(dmProcess.getCol("QTY"), QTY);
							list.set(dmProcess.getCol("CREATED_BY_USER"), userCode);
							list.set(dmProcess.getCol("PROCESS_TIME"), "now()");
							list.set(dmProcess.getCol("CREATED_DTM_LOC"), "now()");
							list.set(dmProcess.getCol("UPDATED_DTM_LOC"), "now()");
							dmProcess.RemoveRow(0);
							dmProcess.AddNewRow(list);
							boolean bool = comData.addSysProcessHistory("sys_process_history", dmProcess);
						}
						return "OK-�ƿ�����ɹ�";
					}
				}
			}
		}else{
			//�Ȳ�ѯĿ���λ�Ƿ���ڼ�¼
			sql = "select INV_INVENTORY_ID from inv_inventory "
					+ "where warehouse_code='"+WAREHOUSE_CODE+"' and location_code='" + DEST_LOCATION_CODE + "' and container_code='" + DEST_CONTAINER_CODE
					+ "' and item_code='" + itemCodeFrom.toString().trim() + "' and LOT_NO = '"+lotNo+"' ";
			DataManager dm2 = DBOperator.DoSelect2DM(sql);
			if(comData.str2Double(QTY)>comData.str2Double(availableQty)){
				return "ERR-��������ԭ������������ܲ���";
			}
			//�����ھͲ���Ŀ���λ�¼�¼
			if(dm2==null || dm2.getCurrentCount()==0){
				sql = "insert into inv_inventory(WAREHOUSE_CODE,STORER_CODE,ITEM_CODE,ITEM_NAME,INV_LOT_ID,LOT_NO,LOCATION_CODE"
						+ ",CONTAINER_CODE,ON_HAND_QTY,INB_TOTAL_QTY,CREATED_BY_USER,CREATED_DTM_LOC) "
						+"select WAREHOUSE_CODE,STORER_CODE,ITEM_CODE,ITEM_NAME,INV_LOT_ID,LOT_NO,'"+DEST_LOCATION_CODE+"',"
						+"'"+DEST_CONTAINER_CODE+"',"+QTY+","+QTY+",'"+userCode+"',now() "
						+" from inv_inventory where warehouse_code='"+WAREHOUSE_CODE+"' and location_code='" + FROM_LOCATION_CODE + "' and container_code='" + FROM_CONTAINER_CODE
						+ "' and item_code='" + itemCodeFrom.toString().trim() + "' and LOT_NO = '"+lotNo+"' ";
				int insertCount = DBOperator.DoUpdate(sql);
				if(insertCount==0){
					return "ERR-����д��Ŀ���λʧ��";
				}else{
					//���ھͿۼ�ԭ��λ����
					sql = "update inv_inventory set ON_HAND_QTY=ON_HAND_QTY-("+QTY+"),"
							+ "OUB_TOTAL_QTY=OUB_TOTAL_QTY+("+QTY+") "
							+ "where INV_INVENTORY_ID='"+fromInvID+"' and warehouse_code='"+WAREHOUSE_CODE+"' and location_code='" + FROM_LOCATION_CODE + "' and container_code='" + FROM_CONTAINER_CODE
							+ "' and item_code='" + itemCodeFrom.toString().trim() + "' and LOT_NO = '"+lotNo+"' ";
					//ִ�п�ԭ��λ���
					int t = DBOperator.DoUpdate(sql);
					if(t>0){
						sql = "update bas_container set status='2' where container_code='"+DEST_CONTAINER_CODE+"' ";
						DBOperator.DoUpdate(sql);
						//��¼������־
						DataManager dmProcess = comData.getSysProcessHistoryDataModel("sys_process_history");
						if (dmProcess!=null) {
							dmdata.xArrayList list = (xArrayList) dmProcess.getRow(0);
							list.set(dmProcess.getCol("SYS_PROCESS_HISTORY_ID"), "null");
							list.set(dmProcess.getCol("WAREHOUSE_CODE"), WAREHOUSE_CODE);
							list.set(dmProcess.getCol("PROCESS_CODE"), "INV_MOVE");
							list.set(dmProcess.getCol("PROCESS_NAME"), "�ƿ�");
							list.set(dmProcess.getCol("ITEM_CODE"), itemCodeFrom);
							list.set(dmProcess.getCol("FROM_LOCATION_CODE"), FROM_LOCATION_CODE);
							list.set(dmProcess.getCol("TO_LOCATION_CODE"), DEST_LOCATION_CODE);
							list.set(dmProcess.getCol("FROM_CONTAINER_CODE"), FROM_CONTAINER_CODE);
							list.set(dmProcess.getCol("TO_CONTAINER_CODE"), DEST_CONTAINER_CODE);
							list.set(dmProcess.getCol("QTY"), QTY);
							list.set(dmProcess.getCol("CREATED_BY_USER"), userCode);
							list.set(dmProcess.getCol("PROCESS_TIME"), "now()");
							list.set(dmProcess.getCol("CREATED_DTM_LOC"), "now()");
							list.set(dmProcess.getCol("UPDATED_DTM_LOC"), "now()");
							dmProcess.RemoveRow(0);
							dmProcess.AddNewRow(list);
							boolean bool = comData.addSysProcessHistory("sys_process_history", dmProcess);
						}
						return "OK-�ƿ�����ɹ�";
					}
				}
			}else{
				//���� ����Ŀ���λ�������
				String toInvID = dm2.getString("INV_INVENTORY_ID", 0);
				sql = "update inv_inventory set ON_HAND_QTY=ON_HAND_QTY+("+QTY+"),"
						+ "INB_TOTAL_QTY=INB_TOTAL_QTY+("+QTY+") "
						+ "where INV_INVENTORY_ID='"+toInvID+"' and warehouse_code='"+WAREHOUSE_CODE+"' and location_code='" + DEST_LOCATION_CODE + "' and container_code='" + DEST_CONTAINER_CODE
						+ "' and item_code='" + itemCodeFrom.toString().trim() + "' and LOT_NO = '"+lotNo+"' ";
				int insertCount = DBOperator.DoUpdate(sql);
				if(insertCount==0){
					return "ERR-���ݸ��µ�Ŀ���λʧ��";
				}else{
					//���ھͿۼ�ԭ��λ����
					sql = "update inv_inventory set ON_HAND_QTY=ON_HAND_QTY-("+QTY+"),"
							+ "OUB_TOTAL_QTY=OUB_TOTAL_QTY+("+QTY+") "
							+ "where INV_INVENTORY_ID='"+fromInvID+"' and warehouse_code='"+WAREHOUSE_CODE+"' and location_code='" + FROM_LOCATION_CODE + "' and container_code='" + FROM_CONTAINER_CODE
							+ "' and item_code='" + itemCodeFrom.toString().trim() + "' and LOT_NO = '"+lotNo+"' ";
					//ִ�п�ԭ��λ���
					int t = DBOperator.DoUpdate(sql);
					if(t>0){
						sql = "update bas_container set status='2' where container_code='"+DEST_CONTAINER_CODE+"' ";
						DBOperator.DoUpdate(sql);
						//��¼������־
						DataManager dmProcess = comData.getSysProcessHistoryDataModel("sys_process_history");
						if (dmProcess!=null) {
							dmdata.xArrayList list = (xArrayList) dmProcess.getRow(0);
							list.set(dmProcess.getCol("SYS_PROCESS_HISTORY_ID"), "null");
							list.set(dmProcess.getCol("WAREHOUSE_CODE"), WAREHOUSE_CODE);
							list.set(dmProcess.getCol("PROCESS_CODE"), "INV_MOVE");
							list.set(dmProcess.getCol("PROCESS_NAME"), "�ƿ�");
							list.set(dmProcess.getCol("ITEM_CODE"), itemCodeFrom);
							list.set(dmProcess.getCol("FROM_LOCATION_CODE"), FROM_LOCATION_CODE);
							list.set(dmProcess.getCol("TO_LOCATION_CODE"), DEST_LOCATION_CODE);
							list.set(dmProcess.getCol("FROM_CONTAINER_CODE"), FROM_CONTAINER_CODE);
							list.set(dmProcess.getCol("TO_CONTAINER_CODE"), DEST_CONTAINER_CODE);
							list.set(dmProcess.getCol("QTY"), QTY);
							list.set(dmProcess.getCol("CREATED_BY_USER"), userCode);
							list.set(dmProcess.getCol("PROCESS_TIME"), "now()");
							list.set(dmProcess.getCol("CREATED_DTM_LOC"), "now()");
							list.set(dmProcess.getCol("UPDATED_DTM_LOC"), "now()");
							dmProcess.RemoveRow(0);
							dmProcess.AddNewRow(list);
							boolean bool = comData.addSysProcessHistory("sys_process_history", dmProcess);
						}
						return "OK-�ƿ�����ɹ�";
					}
				}
			}
		}
		return "";
	}
	
	/**
	 * ���ݲֿ��+��λ��+��� ��ȡ��������
	 * 
	 * @param
	 * @return
	 */
	@WebResult(name = "return_getStorerInfoByWarehouseLocationContainer")
	public String getStorerInfoByWarehouseLocationContainer(@WebParam(name = "WAREHOUSE_CODE", partName = "WAREHOUSE_CODE") String WAREHOUSE_CODE,
			@WebParam(name = "LOCATION_CODE", partName = "LOCATION_CODE") String LOCATION_CODE,
			@WebParam(name = "CONTAINER_CODE", partName = "CONTAINER_CODE") String CONTAINER_CODE) {
		String sql = "select distinct ii.STORER_CODE,bs.STORER_NAME from inv_inventory ii  "
				+"inner join bas_storer bs on ii.STORER_CODE=bs.STORER_CODE "
				+"where ii.WAREHOUSE_CODE='"+WAREHOUSE_CODE+"' and ii.LOCATION_CODE='"+LOCATION_CODE+"' and ii.CONTAINER_CODE='"+CONTAINER_CODE+"' "
				+"and (ii.ON_HAND_QTY-(ii.ALLOCATED_QTY)-(ii.PICKED_QTY))>0  "
				+"order by (ii.ON_HAND_QTY-(ii.ALLOCATED_QTY)-(ii.PICKED_QTY)) desc limit 1 ";
		if (sqlValidate(sql)) {
			DataManager dm = DBOperator.DoSelect2DM(sql);
			String ret = DBOperator.DataManager2JSONString(dm, "");
			return ret;
		}
		return "";
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
	
	//Ч��
  protected static boolean sqlValidate(String str) {
      str = str.toLowerCase();//ͳһתΪСд
      String badStr = "'|and|exec|execute|insert|select|delete|update|count|drop|*|%|chr|mid|master|truncate|" +
              "char|declare|sitename|net user|xp_cmdshell|;|or|-|+|,|like'|and|exec|execute|insert|create|drop|" +
              "table|from|grant|use|group_concat|column_name|" +
              "information_schema.columns|table_schema|union|where|select|delete|update|order|by|count|*|" +
              "chr|mid|master|truncate|char|declare|or|;|-|--|+|,|like|//|/|%|#";//���˵���sql�ؼ��֣������ֶ����
      String[] badStrs = badStr.split("\\|");
      for (int i = 0; i < badStrs.length; i++) {
          if (str.indexOf(badStrs[i]) >= 0) {
              return true;
          }
      }
      return false;
  }
	
	public static void main(String[] args) throws SQLException {
//		System.out.println(new webserviceIml().getInventoryDetailByStorer("1117", "0094-00031"));
//
//		System.out.println(new webserviceIml().getInventoryDetailByStorer("1117", "0094-00031"));
//		System.out.println(new webserviceIml().getLogin("777", "888").equals(""));
//		System.out.println(new webserviceIml().getShipmentOutboundCheck("1123320770606","HZ", "admin"));
//		System.out.println(new webserviceIml().checkStockTakeNo("ST00000007"));
//		System.out.println(new webserviceIml().checkLocationCode("hz", "HZ-H16-B-2"));
//		System.out.println(new webserviceIml().checkContainerNo("hz", "C000000198"));
//		System.out.println(new webserviceIml().stockTakeScanBarCode("ST00000008", "HZ-H16-B-1", "*", "4903111117710",
//				"2", "admin"));
//		System.out.println(new webserviceIml().checkStockTakeNo("ST00000008"));
//		System.out.println(new webserviceIml().getItemInfoByBarcode("4903111321438","1117"));
//		System.out.println(new webserviceIml().getRFVersion("RF","1.0"));
//		System.out.println(new webserviceIml().checkPONo("52"));
//		System.out.println(new webserviceIml().savePODetail("PO00000049","15.10.12","0001","hz","0094-00194","8","ƿ","lot1","","","","","","","","","lot10"));
//		System.out.println(new webserviceIml().createReceiptHeader("PO00000053","sys"));
//		System.out.println(new webserviceIml().createReceiptDetail("IN00000091","PO00000053","8806173556151","C000000010","100","sys"));
//		System.out.println(new webserviceIml().generateInventoryByReceived("IN00000295","sys"));
//		System.out.println(new webserviceIml().checkReceivedContainerCode("C000000001"));
//		System.out.println(new webserviceIml().getSysSuggestLocation("C000000001"));
//		System.out.println(new webserviceIml().checkPutawayDestLocationCode("HZ-H16-B-3"));
		System.out.println(new webserviceIml().putawayConfirm("HZGR-01-01-04","C000001009","sys"));
//		System.out.println(new webserviceIml().getInvList("HZ-H0","8"));
//		System.out.println(new webserviceIml().getWarehouseList());
//		System.out.println(new webserviceIml().invMove("hz","HZ-H1-B-2","*","4518216400300","1","HZ-H2-A-3","C000000033","sys"));
	}

}
