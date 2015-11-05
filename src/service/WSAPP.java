package service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.Endpoint;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import util.Coder;
import net.sf.json.JSONException;

//在想要发布成WebService的类上加上注解@WebService   
@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class WSAPP {

	/**
	 * 提供了一个说Hello的服务
	 * 
	 * @return
	 */
	@WebResult(name = "return_sayHello")
	public String sayHello(
			@WebParam(name = "name", partName = "name") String name) {
		return "Hello " + name;
	}

	/**
	 * 品牌列表
	 * 
	 * @param
	 * @return
	 */
	@WebResult(name = "return_getBrands")
	public String getBrands() {
		StringBuffer brands = new StringBuffer();
		String sql = "select distinct brand_nm from vbi_organ_list where brand_nm is not null order by brand_nm";
		Connection con = DBConnectionManager.getInstance().getConnection("vbi");
		if(con==null){
			DBConnectionManager.getInstance().freeConnection("vbi", con);
			con = DBConnectionManager.getInstance().getConnection("vbi");
			LogInfo.appendLog("getConnection again!");
		}
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				brands.append(rs.getString("brand_nm").toString().trim() + ";");
			}
			rs.close();
			stmt.close();
			//con.close();
		} catch (Exception e) {
			e.printStackTrace();
			LogInfo.appendLog(sql+"\n"+e.getMessage());
			DBConnectionManager.getInstance().freeConnection("vbi", con);
		} finally {
			DBConnectionManager.getInstance().freeConnection("vbi", con);
		}
		return brands.toString();
	}
	
	
	/**
	 * 品牌列表 by_business_date
	 * 
	 * @param
	 * @return
	 */
	@WebResult(name = "return_getBrands_by_business_date")
	public String getBrands_by_business_date(@WebParam(name = "business_date", partName = "business_date") String business_date) {
		StringBuffer brands = new StringBuffer();
		String sql = "select brand_nm,sum(total_amount) from vbi_store_sale_amount "
				+ "where brand_nm is not null and business_date >='"
				+ business_date.split("至")[0].toString().trim()
				+ "' and business_date<='"
				+ business_date.split("至")[1].toString().trim()
				+ "' group by brand_nm order by 2 desc ";
		Connection con = DBConnectionManager.getInstance().getConnection("vbi");
		if(con==null){
			DBConnectionManager.getInstance().freeConnection("vbi", con);
			con = DBConnectionManager.getInstance().getConnection("vbi");
			LogInfo.appendLog("getConnection again!");
		}
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				brands.append(rs.getString("brand_nm").toString().trim() + ";");
			}
			rs.close();
			stmt.close();
			//con.close();
		} catch (Exception e) {
			e.printStackTrace();
			LogInfo.appendLog(sql+"\n"+e.getMessage());;
			DBConnectionManager.getInstance().freeConnection("vbi", con);
		} finally {
			DBConnectionManager.getInstance().freeConnection("vbi", con);
		}
		return brands.toString();
	}
	
	/**
	 * 渠道列表
	 * 
	 * @param
	 * @return
	 */
	@WebResult(name = "return_getFiliale")
	public String getFiliale() {
		StringBuffer brands = new StringBuffer();
		String sql = "select distinct filiale_nm from vbi_store_sale_amount where filiale_nm is not null order by filiale_nm";
		Connection con = DBConnectionManager.getInstance().getConnection("vbi");
		if(con==null){
			DBConnectionManager.getInstance().freeConnection("vbi", con);
			con = DBConnectionManager.getInstance().getConnection("vbi");
			LogInfo.appendLog("getConnection again!");
		}
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				brands.append(rs.getString("filiale_nm").toString().trim() + ";");
			}
			rs.close();
			stmt.close();
			//con.close();
		} catch (Exception e) {
			e.printStackTrace();
			LogInfo.appendLog(sql+"\n"+e.getMessage());;
			DBConnectionManager.getInstance().freeConnection("vbi", con);
		} finally {
			DBConnectionManager.getInstance().freeConnection("vbi", con);
		}
		return brands.toString();
	}
	
	/**
	 * 渠道列表
	 * 
	 * @param brand_nm
	 * @return
	 */
	@WebResult(name = "return_getFiliales")
	public String getFiliales(@WebParam(name = "brand_nm", partName = "brand_nm") String brand_nm) {
		StringBuffer brands = new StringBuffer();
		String sql = "select distinct filiale_nm from vbi_organ_list t where brand_nm='"+brand_nm+"'";
		Connection con = DBConnectionManager.getInstance().getConnection("vbi");
		if(con==null){
			DBConnectionManager.getInstance().freeConnection("vbi", con);
			con = DBConnectionManager.getInstance().getConnection("vbi");
			LogInfo.appendLog(brand_nm+ " getConnection again!");
		}
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				brands.append(rs.getString("filiale_nm").toString().trim() + ";");
			}
			rs.close();
			stmt.close();
			//con.close();
		} catch (SQLException e) {
			e.printStackTrace();
			LogInfo.appendLog(sql+"\n"+e.getMessage());;
			DBConnectionManager.getInstance().freeConnection("vbi", con);
		} finally {
			DBConnectionManager.getInstance().freeConnection("vbi", con);
		}
		return brands.toString();
	}
	
	/**
	 * 渠道列表 by_brand
	 * 
	 * @param
	 * @return
	 */
	@WebResult(name = "return_getFiliale_by_brand")
	public String getFiliale_by_brand(
			@WebParam(name = "business_date", partName = "business_date") String business_date,
			@WebParam(name = "brand", partName = "brand") String brand) {
		StringBuffer brands = new StringBuffer();
		String sql = "select filiale_nm,sum(total_amount) from vbi_store_sale_amount where brand_nm like '%"
				+ brand
				+ "%' and business_date >='"
				+ business_date.split("至")[0].toString().trim()
				+ "' and business_date<='"
				+ business_date.split("至")[1].toString().trim()
				+ "' and filiale_nm is not null group by brand_nm,filiale_nm order by 2 desc ";
		Connection con = DBConnectionManager.getInstance().getConnection("vbi");
		if(con==null){
			DBConnectionManager.getInstance().freeConnection("vbi", con);
			con = DBConnectionManager.getInstance().getConnection("vbi");
			LogInfo.appendLog("getConnection again!");
		}
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				brands.append(rs.getString("filiale_nm").toString().trim() + ";");
			}
			rs.close();
			stmt.close();
			//con.close();
		} catch (Exception e) {
			e.printStackTrace();
			LogInfo.appendLog(sql+"\n"+e.getMessage());;
			DBConnectionManager.getInstance().freeConnection("vbi", con);
		} finally {
			DBConnectionManager.getInstance().freeConnection("vbi", con);
		}
		return brands.toString();
	}
	
	/**
	 * 片区列表
	 * 
	 * @param
	 * @return
	 */
	@WebResult(name = "return_getArea")
	public String getArea() {
		StringBuffer brands = new StringBuffer();
		String sql = "select distinct area_nm from vbi_store_sale_amount where area_nm is not null order by area_nm";
		Connection con = DBConnectionManager.getInstance().getConnection("vbi");
		if(con==null){
			DBConnectionManager.getInstance().freeConnection("vbi", con);
			con = DBConnectionManager.getInstance().getConnection("vbi");
			LogInfo.appendLog("getConnection again!");
		}
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				brands.append(rs.getString("area_nm").toString().trim() + ";");
			}
			rs.close();
			stmt.close();
			//con.close();
		} catch (Exception e) {
			e.printStackTrace();
			LogInfo.appendLog(sql+"\n"+e.getMessage());;
			DBConnectionManager.getInstance().freeConnection("vbi", con);
		} finally {
			DBConnectionManager.getInstance().freeConnection("vbi", con);
		}
		return brands.toString();
	}
	
	/**
	 * 片区列表
	 * 
	 * @param brand_nm,filiale_nm
	 * @return
	 */
	@WebResult(name = "return_getAreas")
	public String getAreas(
			@WebParam(name = "brand_nm", partName = "brand_nm") String brand_nm,
			@WebParam(name = "filiale_nm", partName = "filiale_nm") String filiale_nm) {
		StringBuffer brands = new StringBuffer();
		String sql = "select distinct area_nm from vbi_organ_list t where brand_nm='"+brand_nm+"' and filiale_nm='"+filiale_nm+"'";
		Connection con = DBConnectionManager.getInstance().getConnection("vbi");
		if(con==null){
			DBConnectionManager.getInstance().freeConnection("vbi", con);
			con = DBConnectionManager.getInstance().getConnection("vbi");
			LogInfo.appendLog("getConnection again!");
		}
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				brands.append(rs.getString("area_nm").toString().trim() + ";");
			}
			rs.close();
			stmt.close();
			//con.close();
		} catch (Exception e) {
			e.printStackTrace();
			LogInfo.appendLog(sql+"\n"+e.getMessage());;
			DBConnectionManager.getInstance().freeConnection("vbi", con);
		} finally {
			DBConnectionManager.getInstance().freeConnection("vbi", con);
		}
		return brands.toString();
	}
	
	/**
	 * 门店
	 * 
	 * @param brand_nm,filiale_nm
	 * @return
	 */
	@WebResult(name = "return_getStores")
	public String getStores(
			@WebParam(name = "brand_nm", partName = "brand_nm") String brand_nm,
			@WebParam(name = "filiale_nm", partName = "filiale_nm") String filiale_nm,
			@WebParam(name = "area_nm", partName = "area_nm") String area_nm) {
		StringBuffer brands = new StringBuffer();
		String sql = "select distinct store_nm,dealer_nm from vbi_organ_list t "
				+" where brand_nm='"+brand_nm+"' and filiale_nm='"+filiale_nm+"' and area_nm='"+area_nm+"'";
		Connection con = DBConnectionManager.getInstance().getConnection("vbi");
		if(con==null){
			DBConnectionManager.getInstance().freeConnection("vbi", con);
			con = DBConnectionManager.getInstance().getConnection("vbi");
			LogInfo.appendLog("getConnection again!");
		}
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				brands.append(rs.getString("store_nm").toString().trim()+"@"+
						rs.getString("dealer_nm").toString().trim()+ ";");
			}
			rs.close();
			stmt.close();
			//con.close();
		} catch (Exception e) {
			e.printStackTrace();
			LogInfo.appendLog(sql+"\n"+e.getMessage());;
			DBConnectionManager.getInstance().freeConnection("vbi", con);
		} finally {
			DBConnectionManager.getInstance().freeConnection("vbi", con);
		}
		return brands.toString();
	}
	
	/**
	 * store_sale_amount
	 * 
	 * @param get_store_sale_amount_by_area
	 * @return
	 */
	@WebResult(name = "return_get_store_sale_amount_by_area")
	public String[][] get_store_sale_amount_by_area(
			@WebParam(name = "business_date", partName = "business_date") String business_date
			,@WebParam(name = "brand", partName = "brand") String brand
			,@WebParam(name = "filiale", partName = "filiale") String filiale) {
		String[][] data = new String[1][1];
		String strSQL = "select area_nm,sum(total_amount) total_amount  "
				+ "from vbi_store_sale_amount t "
				+ "where area_nm is not null and business_date >='"
				+ business_date.split("至")[0].toString().trim()
				+ "' and business_date<='"
				+ business_date.split("至")[1].toString().trim() + "' "
				+ " and brand_nm like '%" + brand + "%' and filiale_nm like '%"+filiale+"%' "
				+"group by brand_nm,filiale_nm,area_nm "
				+"order by 2 desc";
		Connection con = DBConnectionManager.getInstance().getConnection("vbi");
		if(con==null){
			DBConnectionManager.getInstance().freeConnection("vbi", con);
			con = DBConnectionManager.getInstance().getConnection("vbi");
			LogInfo.appendLog("getConnection again!");
		}
		Statement stmt;
		ResultSet rs;
		int i = 0;
		try {
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			rs = stmt.executeQuery(strSQL);
			while (rs.next()) {
				i++;
			}
		} catch (SQLException e1) {
			data[0][0] = e1.getMessage();
			e1.printStackTrace();
			LogInfo.appendLog(e1.getMessage());
			DBConnectionManager.getInstance().freeConnection("vbi", con);
			return data;
		}

		if (i == 0) {
			data[0][0] = "没有找到数据";
			return data;// 没有数据直接返回
		}

		data = new String[i + 1][2];

		data[0][0] = "area_nm";
		data[0][1] = "total_amount";

		i = 0;
		try {
			rs.first();
		} catch (SQLException e1) {
			data[0][0] = e1.getMessage();
			e1.printStackTrace();
			LogInfo.appendLog(e1.getMessage());
			DBConnectionManager.getInstance().freeConnection("vbi", con);
			return data;
		}
		try {
			do {
				i++;
				data[i][0] = rs.getString("area_nm");
				data[i][1] = rs.getString("total_amount");
			} while (rs.next());
			rs.close();
			stmt.close();
			//con.close();
		} catch (Exception e) {
			data[0][0] = e.getMessage();
			e.printStackTrace();
			LogInfo.appendLog(strSQL+"\n"+e.getMessage());;
			DBConnectionManager.getInstance().freeConnection("vbi", con);
			return data;
		} finally {
			DBConnectionManager.getInstance().freeConnection("vbi", con);
		}

		return data;
	}
	
	/**
	 * store_sale_amount
	 * 
	 * @param get_store_sale_amount_by_filiale
	 * @return
	 */
	@WebResult(name = "return_get_store_sale_amount_by_filiale")
	public String[][] get_store_sale_amount_by_filiale(
			@WebParam(name = "business_date", partName = "business_date") String business_date
			,@WebParam(name = "brand", partName = "brand") String brand) {
		String[][] data = new String[1][1];
		String strSQL = "select filiale_nm,sum(total_amount) total_amount  "
				+ "from vbi_store_sale_amount t "
				+ "where filiale_nm is not null and business_date >='"
				+ business_date.split("至")[0].toString().trim()
				+ "' and business_date<='"
				+ business_date.split("至")[1].toString().trim() + "' "
				+ " and brand_nm like '%" + brand + "%' "
				+"group by brand_nm,filiale_nm "
				+"order by 2 desc";
		Connection con = DBConnectionManager.getInstance().getConnection("vbi");
		if(con==null){
			DBConnectionManager.getInstance().freeConnection("vbi", con);
			con = DBConnectionManager.getInstance().getConnection("vbi");
			LogInfo.appendLog("getConnection again!");
		}
		Statement stmt;
		ResultSet rs;
		int i = 0;
		try {
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			rs = stmt.executeQuery(strSQL);
			while (rs.next()) {
				i++;
			}
		} catch (SQLException e1) {
			data[0][0] = e1.getMessage();
			e1.printStackTrace();
			LogInfo.appendLog(e1.getMessage());
			DBConnectionManager.getInstance().freeConnection("vbi", con);
			return data;
		}

		if (i == 0) {
			data[0][0] = "没有找到数据";
			return data;// 没有数据直接返回
		}

		data = new String[i + 1][2];

		data[0][0] = "filiale_nm";
		data[0][1] = "total_amount";

		i = 0;
		try {
			rs.first();
		} catch (SQLException e1) {
			data[0][0] = e1.getMessage();
			e1.printStackTrace();
			LogInfo.appendLog(e1.getMessage());
			DBConnectionManager.getInstance().freeConnection("vbi", con);
			return data;
		}
		try {
			do {
				i++;
				data[i][0] = rs.getString("filiale_nm");
				data[i][1] = rs.getString("total_amount");
			} while (rs.next());
			rs.close();
			stmt.close();
			//con.close();
		} catch (Exception e) {
			data[0][0] = e.getMessage();
			e.printStackTrace();
			LogInfo.appendLog(strSQL+"\n"+e.getMessage());;
			DBConnectionManager.getInstance().freeConnection("vbi", con);
			return data;
		} finally {
			DBConnectionManager.getInstance().freeConnection("vbi", con);
		}

		return data;
	}
	
	/**
	 * store_sale_amount
	 * 
	 * @param get_store_sale_amount_by_brand
	 * @return
	 */
	@WebResult(name = "return_get_store_sale_amount_by_brand")
	public String[][] get_store_sale_amount_by_brand(
			@WebParam(name = "business_date", partName = "business_date") String business_date) {
		String[][] data = new String[1][1];
		String strSQL = "select brand_nm,sum(total_amount) total_amount  "
				+ "from vbi_store_sale_amount t "
				+ "where brand_nm is not null and business_date >='"
				+ business_date.split("至")[0].toString().trim()
				+ "' and business_date<='"
				+ business_date.split("至")[1].toString().trim() + "' "
				+ "group by brand_nm "
				+"order by 2 desc ";
		Connection con = DBConnectionManager.getInstance().getConnection("vbi");
		if(con==null){
			DBConnectionManager.getInstance().freeConnection("vbi", con);
			con = DBConnectionManager.getInstance().getConnection("vbi");
			LogInfo.appendLog("getConnection again!");
		}
		Statement stmt;
		ResultSet rs;
		int i = 0;
		try {
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			rs = stmt.executeQuery(strSQL);
			while (rs.next()) {
				i++;
			}
		} catch (SQLException e1) {
			data[0][0] = e1.getMessage();
			e1.printStackTrace();
			LogInfo.appendLog(e1.getMessage());
			DBConnectionManager.getInstance().freeConnection("vbi", con);
			return data;
		}

		if (i == 0) {
			data[0][0] = "没有找到数据";
			return data;// 没有数据直接返回
		}

		data = new String[i + 1][2];

		data[0][0] = "brand_nm";
		data[0][1] = "total_amount";

		i = 0;
		try {
			rs.first();
		} catch (SQLException e1) {
			data[0][0] = e1.getMessage();
			e1.printStackTrace();
			LogInfo.appendLog(e1.getMessage());
			DBConnectionManager.getInstance().freeConnection("vbi", con);
			return data;
		}
		try {
			do {
				i++;
				data[i][0] = rs.getString("brand_nm");
				data[i][1] = rs.getString("total_amount");
			} while (rs.next());
			rs.close();
			stmt.close();
			//con.close();
		} catch (Exception e) {
			data[0][0] = e.getMessage();
			e.printStackTrace();
			LogInfo.appendLog(strSQL+"\n"+e.getMessage());;
			DBConnectionManager.getInstance().freeConnection("vbi", con);
			return data;
		} finally {
			DBConnectionManager.getInstance().freeConnection("vbi", con);
		}

		return data;
	}
	
	/**
	 * get_warrehouse_stock_by_brand
	 * 
	 * @param 
	 * @return
	 */
	@WebResult(name = "return_get_warrehouse_stock_by_brand")
	public String[][] get_warrehouse_stock_by_brand() {
		String[][] data = new String[1][1];
		String strSQL = "select brand_name,sum(sell_price*stockcount) sell_price_total,sum(stockcount) stockcount "
				+"from vbi_prt_warehouse_dist_dl "
				+"where brand_name is not null group by brand_name order by 2 desc ";
		Connection con = DBConnectionManager.getInstance().getConnection("vbi");
		if(con==null){
			DBConnectionManager.getInstance().freeConnection("vbi", con);
			con = DBConnectionManager.getInstance().getConnection("vbi");
			LogInfo.appendLog("getConnection again!");
		}
		Statement stmt;
		ResultSet rs;
		int i = 0;
		try {
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			rs = stmt.executeQuery(strSQL);
			while (rs.next()) {
				i++;
			}
		} catch (SQLException e1) {
			data[0][0] = e1.getMessage();
			e1.printStackTrace();
			LogInfo.appendLog(e1.getMessage());
			DBConnectionManager.getInstance().freeConnection("vbi", con);
			return data;
		}

		if (i == 0) {
			data[0][0] = "没有找到数据";
			return data;// 没有数据直接返回
		}

		data = new String[i + 1][3];

		data[0][0] = "brand_name";
		data[0][1] = "sell_price_total";
		data[0][2] = "stockcount";

		i = 0;
		try {
			rs.first();
		} catch (SQLException e1) {
			data[0][0] = e1.getMessage();
			e1.printStackTrace();
			LogInfo.appendLog(e1.getMessage());
			DBConnectionManager.getInstance().freeConnection("vbi", con);
			return data;
		}
		try {
			do {
				i++;
				data[i][0] = rs.getString("brand_name");
				data[i][1] = rs.getString("sell_price_total");
				data[i][2] = rs.getString("stockcount");
			} while (rs.next());
			rs.close();
			stmt.close();
			//con.close();
		} catch (Exception e) {
			data[0][0] = e.getMessage();
			e.printStackTrace();
			LogInfo.appendLog(strSQL+"\n"+e.getMessage());;
			DBConnectionManager.getInstance().freeConnection("vbi", con);
			return data;
		} finally {
			DBConnectionManager.getInstance().freeConnection("vbi", con);
		}

		return data;
	}
	
	/**
	 * get_warrehouse_stock_by_filiale_nm
	 * 
	 * @param brand_name
	 * @return
	 */
	@WebResult(name = "return_get_warrehouse_stock_by_filiale_nm")
	public String[][] get_warrehouse_stock_by_filiale_nm(
			@WebParam(name = "brand_name", partName = "brand_name") String brand_name) {
		String[][] data = new String[1][1];
		String strSQL = "select nvl(filiale_nm,'N/A') filiale_nm,sum(sell_price*stockcount) sell_price_total,sum(stockcount) stockcount "
				+"from vbi_prt_warehouse_dist_dl "
				+"where nvl(brand_name,'N/A') ='"+brand_name+"' "
				+"group by brand_name,filiale_nm order by 2 desc ";
		Connection con = DBConnectionManager.getInstance().getConnection("vbi");
		if(con==null){
			DBConnectionManager.getInstance().freeConnection("vbi", con);
			con = DBConnectionManager.getInstance().getConnection("vbi");
			LogInfo.appendLog("getConnection again!");
		}
		Statement stmt;
		ResultSet rs;
		int i = 0;
		try {
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			rs = stmt.executeQuery(strSQL);
			while (rs.next()) {
				i++;
			}
		} catch (SQLException e1) {
			data[0][0] = e1.getMessage();
			e1.printStackTrace();
			LogInfo.appendLog(e1.getMessage());
			DBConnectionManager.getInstance().freeConnection("vbi", con);
			return data;
		}

		if (i == 0) {
			data[0][0] = "没有找到数据";
			return data;// 没有数据直接返回
		}

		data = new String[i + 1][3];

		data[0][0] = "filiale_nm";
		data[0][1] = "sell_price_total";
		data[0][2] = "stockcount";

		i = 0;
		try {
			rs.first();
		} catch (SQLException e1) {
			data[0][0] = e1.getMessage();
			e1.printStackTrace();
			LogInfo.appendLog(e1.getMessage());
			DBConnectionManager.getInstance().freeConnection("vbi", con);
			return data;
		}
		try {
			do {
				i++;
				data[i][0] = rs.getString("filiale_nm");
				data[i][1] = rs.getString("sell_price_total");
				data[i][2] = rs.getString("stockcount");
			} while (rs.next());
			rs.close();
			stmt.close();
			//con.close();
		} catch (Exception e) {
			data[0][0] = e.getMessage();
			e.printStackTrace();
			LogInfo.appendLog(strSQL+"\n"+e.getMessage());;
			DBConnectionManager.getInstance().freeConnection("vbi", con);
			return data;
		} finally {
			DBConnectionManager.getInstance().freeConnection("vbi", con);
		}

		return data;
	}
	
	/**
	 * get_warrehouse_stock_by_area_nm
	 * 
	 * @param brand_name
	 * @return
	 */
	@WebResult(name = "return_get_warrehouse_stock_by_area_nm")
	public String[][] get_warrehouse_stock_by_area_nm(
			@WebParam(name = "brand_name", partName = "brand_name") String brand_name,
			@WebParam(name = "filiale_nm", partName = "filiale_nm") String filiale_nm) {
		String[][] data = new String[1][1];
		String strSQL = "select nvl(area_nm,'N/A') area_nm,sum(sell_price*stockcount) sell_price_total,sum(stockcount) stockcount "
				+"from vbi_prt_warehouse_dist_dl "
				+"where nvl(brand_name,'N/A') ='"+brand_name+"' and nvl(filiale_nm,'N/A')='"+filiale_nm+"' "
				+"group by brand_name,filiale_nm,area_nm order by 2 desc ";
		Connection con = DBConnectionManager.getInstance().getConnection("vbi");
		if(con==null){
			DBConnectionManager.getInstance().freeConnection("vbi", con);
			con = DBConnectionManager.getInstance().getConnection("vbi");
			LogInfo.appendLog("getConnection again!");
		}
		Statement stmt;
		ResultSet rs;
		int i = 0;
		try {
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			rs = stmt.executeQuery(strSQL);
			while (rs.next()) {
				i++;
			}
		} catch (SQLException e1) {
			data[0][0] = e1.getMessage();
			e1.printStackTrace();
			LogInfo.appendLog(e1.getMessage());
			DBConnectionManager.getInstance().freeConnection("vbi", con);
			return data;
		}

		if (i == 0) {
			data[0][0] = "没有找到数据";
			return data;// 没有数据直接返回
		}

		data = new String[i + 1][3];

		data[0][0] = "area_nm";
		data[0][1] = "sell_price_total";
		data[0][2] = "stockcount";

		i = 0;
		try {
			rs.first();
		} catch (SQLException e1) {
			data[0][0] = e1.getMessage();
			e1.printStackTrace();
			LogInfo.appendLog(e1.getMessage());
			DBConnectionManager.getInstance().freeConnection("vbi", con);
			return data;
		}
		try {
			do {
				i++;
				data[i][0] = rs.getString("area_nm");
				data[i][1] = rs.getString("sell_price_total");
				data[i][2] = rs.getString("stockcount");
			} while (rs.next());
			rs.close();
			stmt.close();
			//con.close();
		} catch (Exception e) {
			data[0][0] = e.getMessage();
			e.printStackTrace();
			LogInfo.appendLog(strSQL+"\n"+e.getMessage());;
			DBConnectionManager.getInstance().freeConnection("vbi", con);
			return data;
		} finally {
			DBConnectionManager.getInstance().freeConnection("vbi", con);
		}

		return data;
	}
	
	/**
	 * get_warrehouse_stock_by_dealer_name_attr
	 * 
	 * @param brand_name
	 * @return
	 */
	@WebResult(name = "return_get_warrehouse_stock_by_dealer_name_attr")
	public String[][] get_warrehouse_stock_by_dealer_name_attr(
			@WebParam(name = "brand_name", partName = "brand_name") String brand_name,
			@WebParam(name = "filiale_nm", partName = "filiale_nm") String filiale_nm,
			@WebParam(name = "area_nm", partName = "area_nm") String area_nm) {
		String[][] data = new String[1][1];
		String strSQL = "select nvl(dealer_name_attr,'N/A') dealer_name_attr,sum(sell_price*stockcount) sell_price_total,sum(stockcount) stockcount "
				+"from vbi_prt_warehouse_dist_dl "
				+"where nvl(brand_name,'N/A') ='"+brand_name+"' and nvl(filiale_nm,'N/A')='"+filiale_nm+"' "
				+"and nvl(area_nm,'N/A')='"+area_nm+"' "
				+"group by brand_name,filiale_nm,area_nm,dealer_name_attr order by 2 desc ";
		Connection con = DBConnectionManager.getInstance().getConnection("vbi");
		if(con==null){
			DBConnectionManager.getInstance().freeConnection("vbi", con);
			con = DBConnectionManager.getInstance().getConnection("vbi");
			LogInfo.appendLog("getConnection again!");
		}
		Statement stmt;
		ResultSet rs;
		int i = 0;
		try {
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			rs = stmt.executeQuery(strSQL);
			while (rs.next()) {
				i++;
			}
		} catch (SQLException e1) {
			data[0][0] = e1.getMessage();
			e1.printStackTrace();
			LogInfo.appendLog(e1.getMessage());
			DBConnectionManager.getInstance().freeConnection("vbi", con);
			return data;
		}

		if (i == 0) {
			data[0][0] = "没有找到数据";
			return data;// 没有数据直接返回
		}

		data = new String[i + 1][3];

		data[0][0] = "dealer_name_attr";
		data[0][1] = "sell_price_total";
		data[0][2] = "stockcount";

		i = 0;
		try {
			rs.first();
		} catch (SQLException e1) {
			data[0][0] = e1.getMessage();
			e1.printStackTrace();
			LogInfo.appendLog(e1.getMessage());
			DBConnectionManager.getInstance().freeConnection("vbi", con);
			return data;
		}
		try {
			do {
				i++;
				data[i][0] = rs.getString("dealer_name_attr");
				data[i][1] = rs.getString("sell_price_total");
				data[i][2] = rs.getString("stockcount");
			} while (rs.next());
			rs.close();
			stmt.close();
			//con.close();
		} catch (Exception e) {
			data[0][0] = e.getMessage();
			e.printStackTrace();
			LogInfo.appendLog(strSQL+"\n"+e.getMessage());;
			DBConnectionManager.getInstance().freeConnection("vbi", con);
			return data;
		} finally {
			DBConnectionManager.getInstance().freeConnection("vbi", con);
		}

		return data;
	}

	public static void main(String[] args) throws SQLException {
		System.out.println(new WSAPP().getBrands());
//		for(int i=0;i<1000;i++){
//		new WSAPP().getFiliales("罗莱");
//		}
		//System.out.println(new WSAPP().getBrands_by_business_date("2013-01-02 至 2013-05-02"));
		//System.out.println(new WSAPP().getFiliale_by_brand("2013-01-02 至 2013-05-02","罗莱"));//getBrands_by_business_date("2013-01-02 至 2013-05-02"));
//		try{
//		System.out.println("准备启动Luolai app WebService服务");
//		Endpoint.publish("http://172.20.100.5:8419/ws/luolaiapp", new WSAPP());
//		System.out.println("已成功启动Luolai app WebService服务");
//		}catch(Exception e){
//			System.out.println("启动Luolai app WebService失败");
//			e.printStackTrace();
//		}
	}
}
