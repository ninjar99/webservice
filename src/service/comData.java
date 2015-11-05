package service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

import dmdata.DataManager;
import dmdata.xArrayList;

public class comData {

	public synchronized static String getValueFromBasNumRule(String table,String fieldname){
		String retValue = "";
		String sql = "select current_id,rule_expr,now() strdate from bas_num_rule where table_name='"+table+"' and field_name='"+fieldname+"' ";
		System.out.println(sql);
		LogInfo.appendLog("sql",sql);
		java.sql.Connection con = DBConnectionManager.getInstance().getConnection("wms");
		try {
			java.sql.Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while(rs.next()){
				StringBuffer sb = new StringBuffer();
				int current_id = rs.getInt("current_id");
				Date date = rs.getDate("strdate");
				String rule = rs.getString("rule_expr");
				String[] rules = rule.split("@");
				for(int i=0;i<rules.length;i++){
					if(rules[i].indexOf("DATE:")>0){
						sb.append(new SimpleDateFormat(rules[i].substring(rules[i].indexOf(":")+1, rules[i].length()-1)).format(date));
					}else if(rules[i].indexOf("SEQ:")>0){
						String tmp = "00000000000000000000"+String.valueOf(current_id);
						String seq = rules[i].substring(rules[i].indexOf(":")+1, rules[i].length()-1);
						sb.append((tmp.substring(tmp.length()-Integer.parseInt(seq),tmp.length()) ) );
					}else if(rules[i].length()>0){
						sb.append(rules[i]);
					}
				}
				sql = "update bas_num_rule set current_id=current_id+1 where table_name='"+table+"' and field_name='"+fieldname+"'";
				System.out.println(sql);
				LogInfo.appendLog("sql",sql);
				java.sql.Statement stmt2 = con.createStatement();
				int t = stmt2.executeUpdate(sql);
				if(t==1){
					retValue = sb.toString();
				}else{
					retValue = "";
				}
			}
			DBConnectionManager.getInstance().freeConnection("wms", con);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			retValue = "";
		}
		
		return retValue;
	}
	
	public static double str2Double(String value){
		  try{
			  Double.parseDouble(value);
		  }catch(Exception e){
			  return 0.0;
		  }
		  return Double.parseDouble(value);
	  }
	
	public static String getPODetailLineNumberAvailableQty(String poNo,String itemCode,String poLineNumber){
		String sql = "select ifnull(ipd.TOTAL_QTY,0)-ifnull(ipd.RECEIVED_QTY,0) openQty from inb_po_detail ipd "
				+"where ipd.po_no='"+poNo+"' and ipd.item_code='"+itemCode+"' and ipd.Line_number="+poLineNumber+" ";
		Vector vec = DBOperator.DoSelect(sql);
		if(vec==null || vec.size()==0){
			return "0.0";
		}else{
			Object[] obj = (Object[]) vec.get(0);
			return obj[0].toString(); 
		}
		
	}
	
	public static boolean generateInventory(String receiptNo,String userCode){
		String warehouseCode ="";
		String storerCode = "";
		String itemCode = "";
		String lotNo = "";
		String inventoryID = "";
		String lottable01 = "";
		String lottable02 = "";
		String lottable03 = "";
		String lottable04 = "";
		String lottable05 = "";
		String lottable06 = "";
		String lottable07 = "";
		String lottable08 = "";
		String lottable09 = "";
		String lottable10 = "";
		String locationCode = "";
		String containCode = "";
		String receivedQty = "";
		String receivedUOM = "";
		String sql = "select RECEIPT_NO,WAREHOUSE_CODE,STORER_CODE,ITEM_CODE,CONTAINER_CODE,LOCATION_CODE,"
				+"LOTTABLE01,LOTTABLE02,LOTTABLE03,LOTTABLE04,LOTTABLE05,LOTTABLE06,LOTTABLE07,LOTTABLE08,LOTTABLE09,LOTTABLE10,"
				+"RECEIVED_QTY,RECEIVED_UOM "
		+"from inb_receipt_detail "
		+"where receipt_no='"+receiptNo+"' ";
		DataManager dm = DBOperator.DoSelect2DM(sql);
		for(int i=0;i<dm.getCurrentCount();i++){
			warehouseCode = dm.getString("WAREHOUSE_CODE", i);
			storerCode = dm.getString("STORER_CODE", i);
			itemCode = dm.getString("ITEM_CODE", i);
			locationCode = dm.getString("LOCATION_CODE", i);
			containCode = dm.getString("CONTAINER_CODE", i);
			receivedQty = dm.getString("RECEIVED_QTY", i);
			receivedUOM = dm.getString("RECEIVED_UOM", i);
			lottable01 = dm.getString("LOTTABLE01", i);
			lottable02 = dm.getString("LOTTABLE02", i);
			lottable03 = dm.getString("LOTTABLE03", i);
			lottable04 = dm.getString("LOTTABLE04", i);
			lottable05 = dm.getString("LOTTABLE05", i);
			lottable06 = dm.getString("LOTTABLE06", i);
			lottable07 = dm.getString("LOTTABLE07", i);
			lottable08 = dm.getString("LOTTABLE08", i);
			lottable09 = dm.getString("LOTTABLE09", i);
			lottable10 = dm.getString("LOTTABLE10", i);
			//先生成库存批次号
			lotNo = getInventoryLotNo(storerCode,itemCode,lottable01,lottable02,lottable03,lottable04,lottable05,lottable06,lottable07,lottable08,lottable09,lottable10);
			if(!lotNo.equals("")){
				//插入库存表
				inventoryID = getInventoryID(warehouseCode,storerCode,itemCode,lotNo,locationCode,containCode,receivedQty,userCode);
				if(!inventoryID.equals("")){
					//库存表写入成功
					continue;
				}else{
					return false;
				}
			}else{
				return false;
			}
		}
		return true;
	}
	
	private static String getInventoryLotNo(String storerCode,String itemCode,String lot1,String lot2,String lot3,String lot4,String lot5,String lot6,String lot7,String lot8,String lot9,String lot10){
		String lotNo = "";
		String sql = "select LOT_NO from inv_lot where STORER_CODE='"+storerCode+"' and ITEM_CODE='"+itemCode+"' "
				+" and LOTTABLE01='"+lot1+"' and LOTTABLE02='"+lot2+"' and LOTTABLE03='"+lot3+"' and LOTTABLE04='"+lot4+"' "
				+" and LOTTABLE05='"+lot5+"' and LOTTABLE06='"+lot6+"' and LOTTABLE07='"+lot7+"' and LOTTABLE08='"+lot8+"' "
				+" and LOTTABLE09='"+lot9+"' and LOTTABLE10='"+lot10+"'";
		Vector vec = DBOperator.DoSelect(sql);
		if(vec==null || vec.size()==0){
			lotNo = comData.getValueFromBasNumRule("inv_lot", "lot_no");
			sql = "insert into inv_lot(LOT_NO,STORER_CODE,ITEM_CODE,LOTTABLE01,LOTTABLE02,LOTTABLE03,LOTTABLE04,LOTTABLE05,LOTTABLE06,LOTTABLE07,LOTTABLE08,LOTTABLE09,LOTTABLE10) "
					+"select '"+lotNo+"','"+storerCode+"','"+itemCode+"','"+lot1+"','"+lot2+"','"+lot3+"','"+lot4+"','"+lot5+"' "
					+",'"+lot6+"','"+lot7+"','"+lot8+"','"+lot9+"','"+lot10+"' ";
			int t = DBOperator.DoUpdate(sql);
			if(t==1){
				return lotNo;
			}else{
				return "";
			}
		}else{
			Object[] obj = (Object[]) vec.get(0);
			lotNo = obj[0].toString();
		}
		return lotNo;
	}
	
	@SuppressWarnings("rawtypes")
	public static String getInventoryID(String warehouseCode,String storerCode,String itemCode,String lotNo,
			String locationCode,String containerCode,String onHandQty,String userCode){
		String INV_INVENTORY_ID = "";
		String sql = "select INV_INVENTORY_ID from inv_inventory "
				+"where WAREHOUSE_CODE='"+warehouseCode+"' and STORER_CODE='"+storerCode+"' and ITEM_CODE='"+itemCode+"' and LOT_NO='"+lotNo+"' "
				+" and LOCATION_CODE='"+locationCode+"' and CONTAINER_CODE='"+containerCode+"' ";
		Vector vec = DBOperator.DoSelect(sql);
		if(vec==null || vec.size()==0){
			//插入新的库存行记录
			sql = "insert into inv_inventory(WAREHOUSE_CODE,STORER_CODE,ITEM_CODE,ITEM_NAME,INV_LOT_ID,LOT_NO,LOCATION_CODE"
					+ ",CONTAINER_CODE,ON_HAND_QTY,CREATED_BY_USER,CREATED_DTM_LOC) " 
					+ "select '"+warehouseCode+"','"+storerCode+"','"+itemCode+"',(select ITEM_NAME from bas_item where storer_code='"+storerCode+"' and item_code='"+itemCode+"') "
					+",(select INV_LOT_ID from inv_lot where LOT_NO='"+lotNo+"' and STORER_CODE='"+storerCode+"' and ITEM_CODE='"+itemCode+"'),"
					+"'"+lotNo+"','"+locationCode+"','"+containerCode+"',"+onHandQty+",'"+userCode+"',now() ";
			int t = DBOperator.DoUpdate(sql);
			if(t==1){
				sql = "select INV_INVENTORY_ID from inv_inventory "
						+"where WAREHOUSE_CODE='"+warehouseCode+"' and STORER_CODE='"+storerCode+"' and ITEM_CODE='"+itemCode+"' and LOT_NO='"+lotNo+"' "
						+" and LOCATION_CODE='"+locationCode+"' and CONTAINER_CODE='"+containerCode+"' ";
				vec = DBOperator.DoSelect(sql);
				if(vec==null || vec.size()==0){
					return "";
				}else{
					Object[] obj = (Object[]) vec.get(0);
					INV_INVENTORY_ID = obj[0].toString();
					return INV_INVENTORY_ID;
				}
			}else{
				return "";
			}
		}else{
			//增加库存数量
			Object[] obj = (Object[]) vec.get(0);
			INV_INVENTORY_ID = obj[0].toString();
			sql = "update inv_inventory set ON_HAND_QTY=ON_HAND_QTY+("+onHandQty+") "
					+"where INV_INVENTORY_ID="+INV_INVENTORY_ID;
			int t = DBOperator.DoUpdate(sql);
			if(t==1){
				return INV_INVENTORY_ID;
			}else{
				return "";
			}
		}
	}
	
	public static DataManager getSysProcessHistoryDataModel(String table){
		DataManager dm = new DataManager();
		if(table.equals("")){
			return dm;
		}
		try{
		String sql = "select * from "+table+" where 1<>1";
		dm = DBOperator.DoSelect2DM(sql);
		String[] rowdata = new String[dm.getColCount()];
		dm.AddNewRow(rowdata);
		}catch(Exception e){
			return dm;
		}
		return dm;
	}
	
	public static boolean addSysProcessHistory(String table,DataManager dm){
		StringBuffer sbf = new StringBuffer();
		sbf.append("insert into "+table+"(");
		for(int i=0;i<dm.getColCount();i++){
			sbf.append(dm.getCol(i));
			if(i<dm.getColCount()-1){
				sbf.append(",");
			}
		}
		sbf.append(") ");
		for(int k=0;k<dm.getCurrentCount();k++){
			if(k==0){
				sbf.append("select ");
			}else{
				sbf.append(" union all select ");
			}
			for(int i=0;i<dm.getColCount();i++){
				if(dm.getString(i, k).equalsIgnoreCase("null") || dm.getString(i, k).equalsIgnoreCase("now()")){
					sbf.append(dm.getString(i, k));
				}else{
					sbf.append("'"+dm.getString(i, k)+"'");
				}
				if(i<dm.getColCount()-1){
					sbf.append(",");
				}
			}
		}
		int t = DBOperator.DoUpdate(sbf.toString());
		if(t>0){
			return true;
		}else{
			return false;
		}
	}
	
	public static void main(String[] args){
//		DataManager dm = getSysProcessHistoryDataModel("sys_process_history");
//		if (dm!=null) {
//			dmdata.xArrayList list = (xArrayList) dm.getRow(0);
//			list.set(dm.getCol("SYS_PROCESS_HISTORY_ID"), "null");
//			list.set(dm.getCol("QTY"), "0");
//			list.set(dm.getCol("STORER_CODE"), "1117");
//			list.set(dm.getCol("WAREHOUSE_CODE"), "HZ");
//			list.set(dm.getCol("PROCESS_TIME"), "now()");
//			list.set(dm.getCol("CREATED_DTM_LOC"), "now()");
//			list.set(dm.getCol("UPDATED_DTM_LOC"), "now()");
//			dm.RemoveRow(0);
//			dm.AddNewRow(list);
//			boolean bool = addSysProcessHistory("sys_process_history", dm);
//			System.out.println(bool);
//		}
		
//		String tmp = comData.getValueFromBasNumRule("sys_user", "user_code");
//		System.out.println(tmp);
		
//		for(int i=0;i<=1000;i++){
//			String tmp = comData.getValueFromBasNumRule("bas_container", "container_code");
//			String sql = "insert into bas_container(CONTAINER_CODE,WAREHOUSE_CODE,CONTAINER_TYPE_CODE,USE_TYPE,STATUS) "+
//			"select '"+tmp+"','hz','inb','normal','0' ";
//			DBOperator.DoUpdate(sql);
//		}
		
	}
}
