package service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import util.Coder;

//����Ҫ������WebService�����ϼ���ע��@WebService   
@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class WSELTAPP {
	
	/**
	 * ��֤��¼ ELT
	 * 
	 * @param
	 * @return
	 */
	@WebResult(name = "return_getLogin")
	public String getLogin(@WebParam(name = "username", partName = "username") String username,
			@WebParam(name = "password", partName = "password") String password) {
		String pass=null;
		try {
			 //pass = new BigInteger(Coder.encryptSHA(password.getBytes()));  
	         //System.err.println("SHA:\n" + pass.toString(16));
			pass=Coder.bytes2Hex(Coder.encryptSHA(password.getBytes()));
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		StringBuffer brands = new StringBuffer();
		String sql = "select username from bas_ts_user where username='"+username+"' and password='"+pass+"'";
		Connection con = DBConnectionManager.getInstance().getConnection("elt");
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			if (rs.next()) {
				brands.append(rs.getString("username").toString().trim());
			}else{
				brands.append("");
			}
			rs.close();
			stmt.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
			LogInfo.appendLog(sql+"\n"+e.getMessage());
			DBConnectionManager.getInstance().freeConnection("elt", con);
		} finally {
			DBConnectionManager.getInstance().freeConnection("elt", con);
		}
		return brands.toString();
	}
	
	/**
	 * �õ�ELT����Ʒ���б�
	 * 
	 * @param 
	 * @return
	 */
	@WebResult(name = "return_getELTBrands")
	public String getELTBrands() {
		JSONObject jsonObject = new JSONObject();
		JSONArray jsonAllArray = new JSONArray();
		Connection con = DBConnectionManager.getInstance().getConnection("elt");
		String brandSql="select distinct brand,cl.name_zh from sc_tm_dealer_info t "
				+"inner join bas_tc_code_list cl on t.brand=cl.code_value "
				+"where cl.tc_code_type_id = 62000 order by t.brand ";
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(brandSql);
			while (rs.next()) {
				JSONObject jsonArray = new JSONObject();
				jsonArray.put("brand", rs.getString("brand"));
				jsonArray.put("brandname", rs.getString("name_zh"));
				jsonAllArray.add(jsonArray);
			}
			rs.close();
			stmt.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
			LogInfo.appendLog(e.getMessage());
			DBConnectionManager.getInstance().freeConnection("elt", con);
		} finally {
			DBConnectionManager.getInstance().freeConnection("elt", con);
		}
		jsonObject.put("brandList", jsonAllArray);
		
		return jsonObject.toString();
	}
	
	/**
	 * �õ�Ʒ���������е�Ƭ��
	 * 
	 * @param 
	 * @return
	 */
	@WebResult(name = "return_getELTBrandAreas")
	public String getELTBrandAreas(@WebParam(name = "brand", partName = "brand") String brand) {
		System.out.println("begin init json object");
		JSONObject jsonObject = new JSONObject();
		JSONArray jsonAllArray = new JSONArray();
		System.out.println("init json object");
		Connection con = DBConnectionManager.getInstance().getConnection("elt");
		String brandSql="select distinct cl.name_zh,t.sc_tt_bak8 from sc_tm_dealer_info t  "
				+"inner join bas_tc_code_list cl on t.brand=cl.code_value  "
				+"where cl.tc_code_type_id = 62000 and t.sc_tt_bak8 is not null and cl.name_zh='"+brand+"' "
				+"order by t.sc_tt_bak8 ";
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(brandSql);
			while (rs.next()) {
				JSONObject jsonArray = new JSONObject();
				jsonArray.put("brandname", rs.getString("name_zh"));
				jsonArray.put("area", rs.getString("sc_tt_bak8"));
				jsonAllArray.add(jsonArray);
			}
			rs.close();
			stmt.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
			LogInfo.appendLog(e.getMessage());
			DBConnectionManager.getInstance().freeConnection("elt", con);
		} finally {
			DBConnectionManager.getInstance().freeConnection("elt", con);
		}
		jsonObject.put("brandAreaList", jsonAllArray);
		
		return jsonObject.toString();
	}
	
	/**
	 * �õ�Ʒ�� Ƭ�� �������пͻ�
	 * 
	 * @param 
	 * @return
	 */
	@WebResult(name = "return_getELTBrandAreaCustomers")
	public String getELTBrandAreaCustomers(@WebParam(name = "brand", partName = "brand") String brand,
			@WebParam(name = "area", partName = "area") String area) {
		JSONObject jsonObject = new JSONObject();
		JSONArray jsonAllArray = new JSONArray();
		Connection con = DBConnectionManager.getInstance().getConnection("elt");
		String brandSql="select distinct cl.name_zh,t.sc_tt_bak8,t.dealer_no,t.dealer_name_attr from sc_tm_dealer_info t  "
				+"inner join bas_tc_code_list cl on t.brand=cl.code_value  "
				+"where cl.tc_code_type_id = 62000 and t.sc_tt_bak8 is not null and cl.name_zh='"+brand+"' and t.sc_tt_bak8='"+area+"' "
				+"order by t.dealer_no ";
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(brandSql);
			while (rs.next()) {
				JSONObject jsonArray = new JSONObject();
				jsonArray.put("dealer_no", rs.getString("dealer_no"));
				jsonArray.put("dealer_name_attr", rs.getString("dealer_name_attr"));
				jsonAllArray.add(jsonArray);
			}
			rs.close();
			stmt.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
			LogInfo.appendLog(e.getMessage());
			DBConnectionManager.getInstance().freeConnection("elt", con);
		} finally {
			DBConnectionManager.getInstance().freeConnection("elt", con);
		}
		jsonObject.put("brandAreaCustomerList", jsonAllArray);
		
		return jsonObject.toString();
	}
	
	/**
	 * �����û��� �õ� �ͻ��б�   ELT
	 * 
	 * @param username�û���
	 * @return
	 */
	@WebResult(name = "return_getDealerNo")
	public String getDealerNo(
			@WebParam(name = "username", partName = "username") String username) {
		StringBuffer dealerNos = new StringBuffer();
		Connection con = DBConnectionManager.getInstance().getConnection("elt");
		String codeSql="select tree_code from BAS_TS_DEPARTMENT  where  bas_ts_department_id in (select ts_department_id from bas_tr_user_department where ts_user_id=(select bas_ts_user_id from bas_ts_user where username='"+username+"'))";
		String brandSql="select brand_join from SC_TM_USER where sc_tm_user_id = (select bas_ts_user_id from bas_ts_user where username='"+username+"')";
		String brand=null;
		StringBuffer treeCodeSql = new StringBuffer();
		treeCodeSql.append(" select t.reference_no from BAS_TS_DEPARTMENT t where (");
		try {
			Statement stmt = con.createStatement();
			ResultSet coders = stmt.executeQuery(codeSql);
			while (coders.next()) {
				
					treeCodeSql.append("t.tree_code = '").append(coders.getString("tree_code"))
					.append("' or t.tree_code like '").append(coders.getString("tree_code")).append(".%' or ");
			}
			treeCodeSql.delete(treeCodeSql.length() - 4, treeCodeSql.length());
			treeCodeSql.append(") and (t.reference_type = 'DL' or t.reference_type = 'DR') ");
			coders.close();
			ResultSet brandrs = stmt.executeQuery(brandSql);
			while (brandrs.next()) {
				brand=brandrs.getString("brand_join").replace("|", ",");
			}
			brandrs.close();
			String sql = "select dl.dealer_no as dealerNo,dl.dealer_name_attr as dealerNameAttr from SC_TM_DEALER_INFO dl where " 
					+ "dl.dealer_no in (" + treeCodeSql.toString() 
					+ ") and dl.brand in ("+brand+")";	
			LogInfo.appendLog(sql);
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				dealerNos.append(rs.getString("dealerNameAttr").toString().trim()+"@"+
						rs.getString("dealerNo").toString().trim()+ ";");
			}
			rs.close();
			stmt.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
			LogInfo.appendLog(codeSql+"\n"+brandSql+"\n"+e.getMessage());
			DBConnectionManager.getInstance().freeConnection("elt", con);
		} finally {
			DBConnectionManager.getInstance().freeConnection("elt", con);
		}
		return dealerNos.toString();
	}
	
	/**
	 * 
	 * 
	 * @param dealer_no�����̱��
	 * @return
	 */
	@WebResult(name = "return_getDealerNoAmt")
	public String getDealerNoAmt(
			@WebParam(name = "dealer_no", partName = "dealer_no") String dealer_no) {
		java.text.SimpleDateFormat df = new java.text.SimpleDateFormat(
				"yyyy-MM-dd");
		Calendar now = Calendar.getInstance();
		now.roll(Calendar.DAY_OF_YEAR, -1);// ����
		String yesterday = df.format(now.getTime());
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date date = null;
		try {
			date = format.parse(yesterday);
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		}
		StringBuffer dealerNoAmt = new StringBuffer();
		Connection con = DBConnectionManager.getInstance().getConnection("elt");
		//�˻����
		String currentBalanceSql="select nvl((select balance from ( select * from SC_TT_DEALER_BALANCE b where b.sc_tm_dealer_info_id=( select di.sc_tm_dealer_info_id from sc_tm_dealer_info di where di.dealer_no='"+dealer_no+"' ) order by charge_date desc, b.sc_tt_dealer_balance_id desc) where rownum = 1),0) balance from dual";
		//���տ���ܶ�������ܶ�������
		String codeSql="select tree_code from BAS_TS_DEPARTMENT where REFERENCE_NO = '"+dealer_no+"'";
		//��������ܶ�
		String stockOutTotalAmtSql=" select dealerinfo0_.STOCKOUT_TOTAL_AMOUNT as stockOutTotalAmt from SC_TT_DEALERINFO_STOCKOUT dealerinfo0_ where dealerinfo0_.DEALER_NO = '"+dealer_no+"' ";
		//�����ŵ������ܶ�
		String storeSaleAmtOfMonthSql=" select nvl(sum(TOTAL_AMOUNT),0) as totalAmount from SC_TT_STORE_STAT where SC_TM_STORE_INFO_ID in (select SC_TM_STORE_INFO_ID from SC_TM_STORE_INFO where DEALER_NO = '"+dealer_no+"') ";
		//�����ŵ������ܶ�
		String retailAmountSql=" select nvl(sum(sp.RECEIVABLE_TOTAL_AMOUNT),0) as ztotalAmount from SC_TT_POS_INFO_REPORT sp where sp.SC_TM_DEALER_INFO_ID =  (select SC_TM_DEALER_INFO_ID from sc_tm_dealer_info  where dealer_no = '"+dealer_no+"')  and sp.STATUS = 1  and sp.BUSINESS_DATE = to_timestamp('"+yesterday+"', 'yyyy-mm-dd hh24:mi:ss.ff')";
		//���տ���ܶ�
		StringBuffer stockAmtSql = new StringBuffer();
		stockAmtSql.append(" select sum(stt.TOTAL_AMOUNT) as stockAmt from SC_TT_STOCK_STAT stt ")
				.append(" where stt.SC_TM_WAREHOUSE_ID in (select wh.SC_TM_WAREHOUSE_ID from SC_TM_WAREHOUSE wh left outer join SC_TM_STORE_INFO st on wh.SC_TM_STORE_INFO_ID = ")
						.append(" st.SC_TM_STORE_INFO_ID left outer join SC_TM_DEALER_INFO sd on wh.SC_TM_DEALER_INFO_ID = ")
								.append(" sd.SC_TM_DEALER_INFO_ID,BAS_TS_DEPARTMENT ts where (st.APPROVED_STATUS = 'Y' or sd.APPROVED_STATUS = 'Y') and ( ");
		try {
			Statement stmt = con.createStatement();
			
			ResultSet coders = stmt.executeQuery(codeSql);
			while (coders.next()) {
				stockAmtSql.append("ts.tree_code = '").append(coders.getString("tree_code"))
					.append("' or ts.tree_code like '").append(coders.getString("tree_code")).append(".%' or ");
			}
			LogInfo.appendLog(codeSql);
			System.out.println(dealer_no);
			System.out.println((stockAmtSql.lastIndexOf("or ")));
			System.out.println(stockAmtSql.length());
			if(stockAmtSql.lastIndexOf("or ")==stockAmtSql.length()-3){
				stockAmtSql.delete(stockAmtSql.lastIndexOf("or "), stockAmtSql.length());
			}
			if(stockAmtSql.lastIndexOf("and ( ")==stockAmtSql.length()-6){
				stockAmtSql.append(" 1=1 ");
			}
			stockAmtSql.append(") and ts.REFERENCE_NO = wh.WARE_ID ) ");
			coders.close();
			//�˻����
			ResultSet currentBalances = stmt.executeQuery(currentBalanceSql.toString());
			while (currentBalances.next()) {
				dealerNoAmt.append(currentBalances.getString("balance").toString().trim() + ";");
			}
			LogInfo.appendLog("1="+currentBalanceSql);
			currentBalances.close();
			//���տ���ܶ�������ܶ�������
			ResultSet stockAmts = stmt.executeQuery(stockAmtSql.toString());
			while (stockAmts.next()) {
				dealerNoAmt.append(stockAmts.getString("stockAmt").toString().trim() + ";");
			}
			LogInfo.appendLog("2="+stockAmtSql.toString());
			stockAmts.close();
			//��������ܶ�
			ResultSet stockOutTotalAmts = stmt.executeQuery(stockOutTotalAmtSql.toString());
			while (stockOutTotalAmts.next()) {
				dealerNoAmt.append(stockOutTotalAmts.getString("stockOutTotalAmt").toString().trim() + ";");
			}
			LogInfo.appendLog("3="+stockOutTotalAmtSql);
			stockOutTotalAmts.close();
			//�����ŵ������ܶ�
			ResultSet storeSaleAmtOfMonths = stmt.executeQuery(storeSaleAmtOfMonthSql.toString());
			while (storeSaleAmtOfMonths.next()) {
				dealerNoAmt.append(storeSaleAmtOfMonths.getString("totalAmount").toString().trim() + ";");
			}
			LogInfo.appendLog("4="+storeSaleAmtOfMonthSql);
			storeSaleAmtOfMonths.close();
			//�����ŵ������ܶ�
			ResultSet retailAmounts = stmt.executeQuery(retailAmountSql.toString());
			while (retailAmounts.next()) {
				dealerNoAmt.append(retailAmounts.getString("ztotalAmount").toString().trim() + ";");
			}
			LogInfo.appendLog("5="+retailAmountSql);
			retailAmounts.close();
			stmt.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
			LogInfo.appendLog(currentBalanceSql+"\n"+stockAmtSql+"\n"+stockOutTotalAmtSql+"\n"+storeSaleAmtOfMonthSql+"\n"+retailAmountSql+"\n"+e.getMessage());
			DBConnectionManager.getInstance().freeConnection("elt", con);
		} finally {
			DBConnectionManager.getInstance().freeConnection("elt", con);
		}
		return dealerNoAmt.toString();
	}
	
	public static void main(String[] args){
//		System.out.println(new WSELTAPP().getELTBrands());
//		System.out.println(new WSELTAPP().getELTBrandAreas("����"));
//		System.out.println(new WSELTAPP().getELTBrandAreaCustomers("����","�㶫Ƭ��"));
		
//		System.out.println(new WSELTAPP().getLogin("851403", "1234"));
//		System.out.println(new WSELTAPP().getDealerNo("851403"));
//		System.out.println(new WSELTAPP().getDealerNoAmt("851403"));
		System.out.println(new WSELTAPP().getDealerNoAmt("855201"));
	}
}
