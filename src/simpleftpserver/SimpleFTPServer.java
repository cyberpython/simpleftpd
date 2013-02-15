/*
 * Copyright 2013 Georgios Migdos <cyberpython@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package simpleftpserver;

import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.logging.Logger;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.SaltedPasswordEncryptor;
import simpleftpserver.util.Constants;
import simpleftpserver.util.MiscUtils;

/**
 *
 * @author Georgios Migdos <cyberpython@gmail.com>
 */
public class SimpleFTPServer {

    private FtpServer server;
    private int port;
    private String usersFilePath;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        String usersFilePath = null;
        int port = Constants.DEFAULT_PORT;
        try{
            OptionParser parser = new OptionParser("p:u:");
            OptionSet options = parser.parse(args);
            if(options.has("p")){
                port = Integer.parseInt((String)options.valueOf("p"));
            }
            if(options.has("u")){
            usersFilePath = (String)options.valueOf("u");
            }

            SimpleFTPServer srv = new SimpleFTPServer(usersFilePath, port, new PrintWriter(System.out));
            srv.startServer();
        }catch(OptionException oe){
            printUsage(System.out);
        }
    }
    
    private static void printUsage(PrintStream out){
        out.println("Wrong usage! Correct is:");
        out.println("    java -jar simpleftpd.jar [-p <port>] [-u <properties file path>]");
        out.println();
    }

    public SimpleFTPServer(String usersFilePath, int port, Writer loggerOut) {
        this.usersFilePath = usersFilePath;
        this.port = port;
        MiscUtils.configureLog4J(loggerOut);
    }

    public SimpleFTPServer() {
        this(null, Constants.DEFAULT_PORT, new PrintWriter(System.out));
    }
    
    

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void setUsersFilePath(String usersFilePath) {
        this.usersFilePath = usersFilePath;
    }

    public String getUsersFilePath() {
        return usersFilePath;
    }
    
    /**
     * TODO: Documentation
     */
    public void startServer(){
        
//      Stop the server if it is running
        stopServer(); 
        
        FtpServerFactory serverFactory = new FtpServerFactory();
        ListenerFactory factory = new ListenerFactory();
//      Set the port of the listener
        factory.setPort(port);
        serverFactory.setUserManager(loadUsers(getUsersFile(usersFilePath)));
//      Replace the default listener
        serverFactory.addListener("default", factory.createListener());
//      Start the server
        server = serverFactory.createServer();
        try {
            server.start();
        } catch (FtpException ftpe) {
            Logger.getLogger(SimpleFTPServer.class.getName()).severe(ftpe.getMessage());
            stopServer();
        }
    }
    
    /**
     * TODO: Documentation
     */
    public void stopServer(){
        if(server != null){
            if(!server.isStopped()){
                server.stop();
            }
            while(!server.isStopped()){
                try{
                    Thread.sleep(200);
                }catch(InterruptedException ie){
                    //Ignore
                }
            }
        }
        server = null;
    }

    /**
     * Returns a File object pointing to the users
     * properties file. If the path is null or does not
     * point to a file, it checks for the default users
     * properties file in the current user's home directory.
     * If it does not exist as well, it returns null.
     * 
     * @param path A String pointing to the users properties file.
     */
    private File getUsersFile(String path) {
        if (path != null) {
            File f = new File(path);
            if (f.isFile()) {
                return f;
            }
        }
        path = System.getProperty("user.home");
        File f = new File(path);
        f = new File(f, Constants.DEFAULT_USERS_FILE_NAME);
        if (f.isFile()) {
            return f;
        }
        return null;
    }

    /**
     * TODO: documentation
     */
    private UserManager loadUsers(File usersFile) {
        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        userManagerFactory.setFile(usersFile);
        userManagerFactory.setPasswordEncryptor(new SaltedPasswordEncryptor());
        return userManagerFactory.createUserManager();
    }

    
}
