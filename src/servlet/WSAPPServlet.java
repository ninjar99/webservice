package servlet;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.xml.ws.Endpoint;
import service.webserviceIml;

public class WSAPPServlet extends HttpServlet{
	private static final long serialVersionUID = 1L;

	public void init() throws ServletException {
		super.init();
	}

	public void init(ServletConfig config) {
		try {
			super.init(config);
			if(isLoclePortUsing(7758)){
				System.out.println("已经启动WebService服务[http://0.0.0.0:7758/ws/aggwms]");
				return;
			}
			System.out.println("准备启动WebService服务[http://0.0.0.0:7758/ws/aggwms]");
			// 发布一个WebService
//			Endpoint endpoint =  Endpoint.publish("http://172.20.100.5:8419/ws/luolaiapp", new WSAPP());
//			Endpoint endpoint_ELT =  Endpoint.publish("http://172.20.100.5:8419/ws/luolaiapp_elt", new WSELTAPP());
//			endpoint.getBinding();
//			endpoint_ELT.getBinding();
			Endpoint endpoint =  Endpoint.publish("http://0.0.0.0:7758/ws/aggwms", new webserviceIml());
			endpoint.getBinding();
			
			System.out.println("已成功启动WebService服务[http://0.0.0.0:7758/ws/aggwms]");
		} catch (ServletException e) {
			System.out.println("启动WebService失败[http://0.0.0.0:7758/ws/aggwms]");
			e.printStackTrace();
		} finally {
			System.out.println("init finish");
		}
	}
	
	/*** 
     *  true:already in using  false:not using  
     * @param port 
     */  
    public static boolean isLoclePortUsing(int port){  
        boolean flag = true;  
        try {  
            flag = isPortUsing("127.0.0.1", port);  
        } catch (Exception e) {  
        }  
        return flag;  
    }  
    /*** 
     *  true:already in using  false:not using  
     * @param host 
     * @param port 
     * @throws UnknownHostException  
     */  
    public static boolean isPortUsing(String host,int port) throws UnknownHostException{  
        boolean flag = false;  
        InetAddress theAddress = InetAddress.getByName(host);  
        try {  
            Socket socket = new Socket(theAddress,port);  
            flag = true;  
        } catch (IOException e) {  
              
        }  
        return flag;  
    }

	public void service(ServletRequest arg0, ServletResponse arg1)
			throws ServletException, IOException {
		super.service(arg0, arg1);
		System.out.println("此Servlet不处理任何业务逻辑，仅仅发布一个Web服务");
	}
}
