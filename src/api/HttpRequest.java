package api;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.apache.http.HttpEntity;  
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;  
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import net.sf.json.JSONObject;  


public class HttpRequest {

	public static <namevaluepair> void main(String[] args) throws ClientProtocolException, IOException  
    {  
		/* 将参数值放入map */
		Map<String, String> hashMap = new HashMap<String, String>();
//		hashMap.put("appid", "seller1280");
//		hashMap.put("format", "json");
//		hashMap.put("method", "Inventory.ChannelQ4SBatchGet");
//		hashMap.put("timestamp", getDateTimeString());
//		hashMap.put("data", "{\"ProductIDs\":\"'111','3333'\";\"SaleChannelSysNo\";\"44\"}");
//		hashMap.put("nonce",(new Random().nextInt(100000000)) + "");
//		hashMap.put("version", "1.0");
		hashMap.put("accept", "*/*");
		hashMap.put("Accept-Charset", "UTF-8");
		hashMap.put("connection", "Keep-Alive");
		hashMap.put("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
		hashMap.put("user-agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
		
		post("http://api.kjt.com/open.api",new JSONObject(),hashMap);
    }
	
	@SuppressWarnings({ "unused", "deprecation" })
	public static JSONObject post(String url, JSONObject json, Map<String, String> headers) {
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(url);
		JSONObject response = null;
		if (headers != null) {
			Set<String> keys = headers.keySet();
			for (Iterator<String> i = keys.iterator(); i.hasNext();) {
				String key = (String) i.next();
				post.addHeader(key, headers.get(key));

			}
		}

		try {
			json = new JSONObject();
			json.put("appid", "seller1280");
			json.put("format", "json");
			json.put("method", "Inventory.ChannelQ4SBatchGet");
			json.put("timestamp", getDateTimeString());
			json.put("data", "{\"ProductIDs\":\"'111','3333'\";\"SaleChannelSysNo\";\"44\"}");
			json.put("nonce", (new Random().nextInt(100000000)) + "");
			json.put("version", "1.0");
			StringEntity s = new StringEntity(json.toString(), "utf-8");
			s.setContentEncoding("HTTP.UTF_8");
			// s.setContentType("application/json");
			s.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
			post.setEntity(s);

			HttpResponse httpResponse = client.execute(post);
			InputStream inStream = httpResponse.getEntity().getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "utf-8"));
			StringBuilder strber = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null)
				strber.append(line + "\n");
			inStream.close();
			System.out.println(strber.toString());
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				HttpEntity entity = httpResponse.getEntity();
				String charset = EntityUtils.getContentCharSet(entity);
				// response = new JSONObject(new JSONTokener(new
				// InputStreamReader(entity.getContent(),charset)));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return response;
	}
	
	public static  String getDateTimeString(){
        Date now = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String result = dateFormat.format(now);
        return  result;
    }
    
    public static String convertStreamToString(InputStream is) {      
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));      
        StringBuilder sb = new StringBuilder();      
       
        String line = null;      
        try {      
            while ((line = reader.readLine()) != null) {  
                sb.append(line + "\n");      
            }      
        } catch (IOException e) {      
            e.printStackTrace();      
        } finally {      
            try {      
                is.close();      
            } catch (IOException e) {      
               e.printStackTrace();      
            }      
        }      
        return sb.toString();      
    } 
}