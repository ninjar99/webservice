package service;

import dmdata.DataManager;

public class test {

	public static void main(String[] args){
//		String sql = "select ��Ʒ���� from wms11";
//		DataManager dm = DBOperator.DoSelect2DM(sql);
//		for(int i=0;i<dm.getCurrentCount();i++){
//			String barcode = dm.getString("��Ʒ����", i);
//			String sql2 = "select ��λ���� from wms1 where ��Ʒ����='"+barcode+"'";
//			StringBuffer sbf = new StringBuffer();
//			DataManager dm2 = DBOperator.DoSelect2DM(sql2);
//			for(int k=0;k<dm2.getCurrentCount();k++){
//				sbf.append(dm2.getString("��λ����", k));
//				if(k<dm2.getCurrentCount()-1){
//					sbf.append(",");
//				}
//			}
//			String sql3 = "update wms11 set ��λ����='"+sbf.toString()+"' where ��Ʒ����='"+barcode+"'";
//			int t = DBOperator.DoUpdate(sql3);
//		}
		
		String sql = "select ���,�Ϻ�,������ from wms2";
		DataManager dm = DBOperator.DoSelect2DM(sql);
		for(int i=0;i<dm.getCurrentCount();i++){
			String seq = dm.getString("���", i);
			String itemcode = dm.getString("�Ϻ�", i);
			String barcode = dm.getString("������", i);
			String sql2 = "select a.item_code,b.ITEM_BAR_CODE,GROUP_CONCAT(a.location_code SEPARATOR ',') location_code,sum(a.ON_HAND_QTY) ON_HAND_QTY "
					+ "from inv_inventory a  "
					+ "inner join bas_item b on a.STORER_CODE=b.STORER_CODE and a.ITEM_CODE=b.ITEM_CODE "
					+ "where a.WAREHOUSE_CODE='hzgr' "
					+ "and (a.item_code='"+itemcode+"' or b.ITEM_BAR_CODE='"+barcode+"') "
					+ "group by item_code ";
			DataManager dm2 = DBOperator.DoSelect2DM(sql2);
			String location = dm2.getString("location_code", 0);
			String qty = dm2.getString("ON_HAND_QTY", 0);
			String sql3 = "update wms2 set ��λ='"+location+"',����='"+qty+"' where ���='"+seq+"'";
			int t = DBOperator.DoUpdate(sql3);
		}
		
	}
}
