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
				System.out.println("�Ѿ�����WebService����[http://0.0.0.0:7758/ws/aggwms]");
				return;
			}
			System.out.println("׼������WebService����[http://0.0.0.0:7758/ws/aggwms]");
			// ����һ��WebService
//			Endpoint endpoint =  Endpoint.publish("http://172.20.100.5:8419/ws/luolaiapp", new WSAPP());
//			Endpoint endpoint_ELT =  Endpoint.publish("http://172.20.100.5:8419/ws/luolaiapp_elt", new WSELTAPP());
//			endpoint.getBinding();
//			endpoint_ELT.getBinding();
			Endpoint endpoint =  Endpoint.publish("http://0.0.0.0:7758/ws/aggwms", new webserviceIml());
			endpoint.getBinding();
			
			System.out.println("�ѳɹ�����WebService����[http://0.0.0.0:7758/ws/aggwms]");
		} catch (ServletException e) {
			System.out.println("����WebServiceʧ��[http://0.0.0.0:7758/ws/aggwms]");
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
		System.out.println("��Servlet�������κ�ҵ���߼�����������һ��Web����");
	}
}
