package service;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class DBConnectionManager {
	static private DBConnectionManager instance;       // The single instance
    static private int clients;
    private Vector<Driver> drivers = new Vector<Driver>();
    private PrintWriter log;
    private Hashtable<String, DBConnectionPool> pools = new Hashtable<String, DBConnectionPool>();
    static java.sql.Connection dbcon = null;
    static DBConnectionPool pool = null;
    
    static int lport = 7759;//本地端口
	static String rhost = "10.10.119.161";//远程MySQL服务器
	static int rport = 3306;//远程MySQL服务端口
	
	public static void bindPortSSH() {
		String user = "root";//SSH连接用户名
		String password = "540hs0qos";//SSH连接密码
		String host = "123.59.82.190";//SSH服务器
		int port = 22;//SSH访问端口
		try {
			JSch jsch = new JSch();
			Session session = jsch.getSession(user, host, port);
			session.setPassword(password);
			session.setConfig("StrictHostKeyChecking", "no");
			session.connect();
			System.out.println(session.getServerVersion());//这里打印SSH服务器版本信息
			int assinged_port = session.setPortForwardingL(lport, rhost, rport);
			System.out.println("localhost:" + assinged_port + " -> " + rhost + ":" + rport);
		} catch (Exception e) {
//			e.printStackTrace();
			LogInfo.appendLog(e.getMessage());
		}
	}

    /**
     * Returns the single instance, creating one if it's the first time this
     * method is called.
     *
     * @return DBConnectionManager The single instance.
     */
    static synchronized public DBConnectionManager getInstance() {
        if (instance == null) {
            instance = new DBConnectionManager();
        }
        clients++;
        return instance;
    }

    /**
     * A private constructor since this is a Singleton
     */
    private DBConnectionManager() {
        init();
    }

    /**
     * Returns a connection to the named pool.
     *
     * @param name The pool name as defined in the properties file
     * @param con The Connection
     */
    public void freeConnection(String name, Connection con) {
        DBConnectionPool pool = (DBConnectionPool) pools.get(name);
        if (pool != null) {
            pool.freeConnection(con);
        }
        clients--;
    }

    /**
     * Returns an open connection. If no one is available, and the max number of
     * connections has not been reached, a new connection is created.
     *
     * @param name The pool name as defined in the properties file
     * @return Connection The connection or null
     */
    public java.sql.Connection getConnection(String name) {
//		DBConnectionPool pool = null;
		try {
			pool = (DBConnectionPool) pools.get(name);
			if (pool != null) {
				LogInfo.appendLog("connect counter:"+clients);
				System.out.println("connect counter:"+clients);
				return pool.getConnection();
			} else {
				init();
				pool = (DBConnectionPool) pools.get(name);
				LogInfo.appendLog("getConnection call init()");
				System.out.println("getConnection call init()");
			}
		} catch (Exception e) {
			init();
			pool = (DBConnectionPool) pools.get(name);
			LogInfo.appendLog("Exception occur,getConnection call init()");
			e.printStackTrace();
			LogInfo.appendLog(e.getMessage());
		}
		//LogInfo.appendLog("clients"+clients);
        return pool.getConnection();
    }

    /**
     * Returns an open connection. If no one is available, and the max number of
     * connections has not been reached, a new connection is created. If the max
     * number has been reached, waits until one is available or the specified
     * time has elapsed.
     *
     * @param name The pool name as defined in the properties file
     * @param time The number of milliseconds to wait
     * @return Connection The connection or null
     */
    public java.sql.Connection getConnection(String name, long time) {
        DBConnectionPool pool = (DBConnectionPool) pools.get(name);
        if (pool != null) {
            return pool.getConnection(time);
        }else{
        	init();
        	pool = (DBConnectionPool) pools.get(name);
        }
        return pool.getConnection();
    }

    /**
     * Closes all open connections and deregisters all drivers.
     */
    public synchronized void releaseAll() {
        // Wait until called by the last client
        if (--clients != 0) {
            return;
        }

        Enumeration<DBConnectionPool> allPools = pools.elements();
        while (allPools.hasMoreElements()) {
            DBConnectionPool pool = (DBConnectionPool) allPools.nextElement();
            pool.releaseAllConn();
        }
        Enumeration<Driver> allDrivers = drivers.elements();
        while (allDrivers.hasMoreElements()) {
            Driver driver = (Driver) allDrivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
                log("Deregistered JDBC driver " + driver.getClass().getName());
            } catch (SQLException e) {
                log(e, "Can't deregister JDBC driver: " + driver.getClass().getName());
            }
        }
    }

    /**
     * Loads properties and initializes the instance with its values.
     */
    private void init() {
        FileInputStream inputFile;
        try {
        	bindPortSSH();
        	System.out.println(System.getProperty("user.dir")+"/wsapp.properties");
        	LogInfo.appendLog(System.getProperty("user.dir")+"/wsapp.properties");
            inputFile = new FileInputStream(System.getProperty("user.dir")+"/wsapp.properties");
        } catch (FileNotFoundException ex) {
            System.out.println(ex.getMessage());
            LogInfo.appendLog(ex.getMessage());
            return;
        }
        Properties dbProps = new Properties();
        try {
            dbProps.load(inputFile);
        } catch (Exception e) {
            System.err.println("Can't read the properties file. "
                    + "Make sure db.properties is in the CLASSPATH");
            LogInfo.appendLog("Can't read the properties file. "
                    + "Make sure db.properties is in the CLASSPATH");
            LogInfo.appendLog(e.getMessage());
            return;
        }
        String logFile = dbProps.getProperty("logfile", "DBConnectionManager.log");
        try {
            log = new PrintWriter(new FileWriter(logFile, true), true);
        } catch (IOException e) {
            System.err.println("Can't open the log file: " + logFile);
            LogInfo.appendLog("Can't open the log file: " + logFile);
            LogInfo.appendLog(e.getMessage());
        }
        loadDrivers(dbProps);
        createPools(dbProps);
        //LogInfo.appendLog("clients:"+clients);
    }

    /**
     * Loads and registers all JDBC drivers. This is done by the
     * DBConnectionManager, as opposed to the DBConnectionPool, since many pools
     * may share the same driver.
     *
     * @param props The connection pool properties
     */
    private void loadDrivers(Properties props) {
        String driverClasses = props.getProperty("drivers");
        StringTokenizer st = new StringTokenizer(driverClasses);
        while (st.hasMoreElements()) {
            String driverClassName = st.nextToken().trim();
            try {
                Driver driver = (Driver) Class.forName(driverClassName).newInstance();
                DriverManager.registerDriver(driver);
                drivers.addElement(driver);
                log("Registered JDBC driver " + driverClassName);
            } catch (Exception e) {
            	e.printStackTrace();
            	LogInfo.appendLog("Can't register JDBC driver: "
                        + driverClassName + ", Exception: " + e);
            }
        }
    }
    
    /**
     * Creates instances of DBConnectionPool based on the properties. A
     * DBConnectionPool can be defined with the following properties:
     * <PRE>
     * &lt;poolname&gt;.url         The JDBC URL for the database
     * &lt;poolname&gt;.user        A database user (optional)
     * &lt;poolname&gt;.password    A database user password (if user specified)
     * &lt;poolname&gt;.maxconn     The maximal number of connections (optional)
     * </PRE>
     *
     * @param props The connection pool properties
     */
    private void createPools(Properties props) {
        Enumeration<?> propNames = props.propertyNames();
        while (propNames.hasMoreElements()) {
            String name = (String) propNames.nextElement();
            if (name.endsWith(".url")) {
                String poolName = name.substring(0, name.lastIndexOf("."));
                String url = props.getProperty(poolName + ".url");
                if (url == null) {
                    LogInfo.appendLog("No URL specified for " + poolName);
                    continue;
                }
                String user = props.getProperty(poolName + ".user");
                if(user.equals("namaxxx")){
                	user = "namacity";
                }
                String password = props.getProperty(poolName + ".password");
                if(password.equals("namaxxx")){
                	password = "namacity1524";
                }
                String maxconn = props.getProperty(poolName + ".maxconn", "0");
                int max;
                try {
                    max = Integer.valueOf(maxconn).intValue();
                } catch (NumberFormatException e) {
                	LogInfo.appendLog("Invalid maxconn value: " + maxconn + " for " + poolName);
                    max = 0;
                }
                DBConnectionPool pool =
                        new DBConnectionPool(poolName, url, user, password, max);
                pools.put(poolName, pool);
                LogInfo.appendLog("Initialized pool: " + poolName);
            }
        }
    }

    /**
     * Writes a message to the log file.
     */
    private void log(String msg) {
        log.println(new java.util.Date() + ": " + msg);
    }

    /**
     * Writes a message with an Exception to the log file.
     */
    private void log(Throwable e, String msg) {
        log.println(new java.util.Date() + ": " + msg);
        e.printStackTrace(log);
    }

    /**
     * This inner class represents a connection pool. It creates new connections
     * on demand, up to a max number if specified. It also makes sure a
     * connection is still open before it is returned to a client.
     */
    static class DBConnectionPool {

        private int checkedOut;
        static Vector<Connection> freeConnections = new Vector<Connection>();
        private int maxConn;
        private String name;
        private String password;
        private String URL;
        private String user;

        /**
         * Creates new connection pool.
         *
         * @param name The pool name
         * @param URL The JDBC URL for the database
         * @param user The database user, or null
         * @param password The database user password, or null
         * @param maxConn The maximal number of connections, or 0 for no limit
         */
        public DBConnectionPool(String name, String URL, String user, String password,
                int maxConn) {
            this.name = name;
            this.URL = URL;
            this.user = user;
            this.password = password;
            this.maxConn = maxConn;
        }

        /**
         * Checks in a connection to the pool. Notify other Threads that may be
         * waiting for a connection.
         *
         * @param con The connection to check in
         */
        public synchronized void freeConnection(Connection con) {
            // Put the connection at the end of the Vector
            freeConnections.addElement(con);
            if(checkedOut>0) {
            	checkedOut--;
            	LogInfo.appendLog("freeConnection clients:"+checkedOut);
                notifyAll();
            }
            
        }

        /**
         * Checks out a connection from the pool. If no free connection is
         * available, a new connection is created unless the max number of
         * connections has been reached. If a free connection has been closed by
         * the database, it's removed from the pool and this method is called
         * again recursively.
         */
        public synchronized java.sql.Connection getConnection() {
//            java.sql.Connection con = null;
            
            if(checkedOut >= maxConn){
            	dbcon =null;
            	releaseAllConn();
            	LogInfo.appendLog("checkedOut >= maxConn call releaseAllConn()");
            }
            try {
				if(dbcon==null || dbcon.isClosed()){
					dbcon = newConnection();
					return dbcon;
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
				dbcon = newConnection();
			}
            
            if (freeConnections.size() > 0) {
                // Pick the first Connection in the Vector
                // to get round-robin usage
            	dbcon = (java.sql.Connection) freeConnections.firstElement();
                if (dbcon != null) {
	                checkedOut++;
	            }else{
	            	dbcon = newConnection();
	            	return dbcon;
	            }
                freeConnections.removeElementAt(0);
                try {
                    if (dbcon==null || dbcon.isClosed()) {
                        //LogInfo.appendLog("Removed bad connection from " + name);
                        // Try again recursively
                    	dbcon = getConnection();
                        if (dbcon != null) {
    		                checkedOut++;
    		            }
                        LogInfo.appendLog("1.getConnection " + checkedOut);
                    }
                } catch (SQLException e) {
                	//LogInfo.appendLog("Removed bad connection from " + name);
                    // Try again recursively
                	dbcon = getConnection();
                    LogInfo.appendLog(e.getMessage());
                }
            } else if (maxConn == 0 || checkedOut < maxConn) {
				if (dbcon == null) {
					dbcon = newConnection();
					if (dbcon != null) {
		                checkedOut++;
		            }
					LogInfo.appendLog("2.newConnection " + checkedOut);
				}
            } 
            
            return dbcon;
        }

        /**
         * Checks out a connection from the pool. If no free connection is
         * available, a new connection is created unless the max number of
         * connections has been reached. If a free connection has been closed by
         * the database, it's removed from the pool and this method is called
         * again recursively. <P> If no connection is available and the max
         * number has been reached, this method waits the specified time for one
         * to be checked in.
         *
         * @param timeout The timeout value in milliseconds
         */
        public synchronized java.sql.Connection getConnection(long timeout) {
            long startTime = new java.util.Date().getTime();
            java.sql.Connection con;
            while ((con = getConnection()) == null) {
                try {
                    wait(timeout);
                } catch (InterruptedException e) {
                }
                if ((new java.util.Date().getTime() - startTime) >= timeout) {
                    // Timeout has expired
                    return null;
                }
            }
            return con;
        }

        /**
         * Closes all available connections.
         */
        public synchronized void releaseAllConn() {
        	checkedOut=0;
            Enumeration<Connection> allConnections = freeConnections.elements();
            while (allConnections.hasMoreElements()) {
                java.sql.Connection con = (java.sql.Connection) allConnections.nextElement();
                try {
                    con.close();
                    LogInfo.appendLog("Closed connection for pool " + name+" now connection clients:"+checkedOut);
                } catch (SQLException e) {
                	LogInfo.appendLog("Can't close connection for pool " + name);
                }
            }
            freeConnections.removeAllElements();
        }

        /**
         * Creates a new connection, using a userid and password if specified.
         */
        private synchronized java.sql.Connection newConnection() {
//            java.sql.Connection con = null;
            try {
            	String bindURL = URL.replaceAll("port", String.valueOf(lport));
                if (user == null) {
                	try{
                		dbcon = DriverManager.getConnection(bindURL);
                		freeConnections.add(dbcon);
                	}catch(Exception e){
                		lport = lport + 1;
                		bindPortSSH();//if error  create other port
                		dbcon = DriverManager.getConnection(bindURL);
                	}
                } else {
                	try{
                		dbcon = DriverManager.getConnection(bindURL, user, password);
                		freeConnections.add(dbcon);
                	}catch(Exception e){
                		lport = lport + 1;
                		bindPortSSH();//if error  create other port
                		dbcon = DriverManager.getConnection(bindURL, user, password);
                		freeConnections.add(dbcon);
                	}
                }
                //LogInfo.appendLog("Created a new connection in pool " + name);
            } catch (SQLException e) {
            	LogInfo.appendLog("Can't create a new connection for " + URL);
                return null;
            }
            return dbcon;
        }
    }

    public static void main(String[] args) {
        Connection con = DBConnectionManager.getInstance().getConnection("wms");
        try {
            Statement stmt = con.createStatement();
            String sql = "select now() ";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String str = rs.getString(1);
                System.out.println(str);
            }
            DBConnectionManager.instance.freeConnection("wms", con);
        } catch (SQLException ex) {
            Logger.getLogger(DBConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

