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
import java.util.ArrayList;
import java.util.List;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.SaltedPasswordEncryptor;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import simpleftpserver.util.Constants;
import simpleftpserver.util.MiscUtils;

/**
 *
 * @author Georgios Migdos <cyberpython@gmail.com>
 */
public class UserManager {

    private org.apache.ftpserver.ftplet.UserManager um;

    public UserManager(File usersFile) {
        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        userManagerFactory.setFile(usersFile);
        userManagerFactory.setPasswordEncryptor(new SaltedPasswordEncryptor());
        this.um = userManagerFactory.createUserManager();
    }

    /**
     * TODO: Javadoc
     */
    public boolean createUser(String username, String password, String userRootDir, boolean hasWriteAccess) throws FtpException {
        if (!um.doesExist(username)) {
            BaseUser user = new BaseUser();
            user.setName(username);
            user.setPassword(password);
            List<Authority> auths = new ArrayList<Authority>();
            if (hasWriteAccess) {
                auths.add(new WritePermission());
            }
            user.setAuthorities(auths);
            user.setHomeDirectory(userRootDir);
            um.save(user);
            return true;
        }
        return false;
    }

    /**
     * TODO: Javadoc
     */
    public boolean setUserWritePermission(String username, boolean hasWriteAccess) throws FtpException {
        if (um.doesExist(username)) {
            List<Authority> auths = new ArrayList<Authority>();
            if (hasWriteAccess) {
                auths.add(new WritePermission());
            }
            BaseUser user = (BaseUser) um.getUserByName(username);
            user.setAuthorities(auths);
            um.save(user);
            return true;
        }
        return false;
    }

    /**
     * TODO: Javadoc
     */
    public boolean setUserRootDir(String username, String rootDir) throws FtpException {
        if (um.doesExist(username)) {
            BaseUser user = (BaseUser) um.getUserByName(username);
            user.setHomeDirectory(rootDir);
            um.save(user);
            return true;
        }
        return false;

    }

    /**
     * TODO: Javadoc
     */
    public boolean setUserPassword(String username, String password) throws FtpException {
        if (um.doesExist(username)) {
            BaseUser user = (BaseUser) um.getUserByName(username);
            user.setPassword(password);
            um.save(user);
            return true;
        }
        return false;
    }

    /**
     * TODO: Javadoc
     */
    public boolean deleteUser(String username) throws FtpException {
        if (um.doesExist(username)) {
            um.delete(username);
            return true;
        }
        return false;
    }
    
    /**
     * TODO: Javadoc
     */
    public String[] listUsers() throws FtpException {
        return um.getAllUserNames();
    }

    public static void main(String[] args) {
        try {
            OptionParser parser = new OptionParser();
            //TODO: add list-users option
            parser.accepts("create-user");
            parser.accepts("delete-user");
            parser.accepts("set-password");
            parser.accepts("set-home");
            parser.accepts("set-permissions");
            parser.accepts("list-users");
            parser.accepts("username").requiredIf("create-user", "delete-user", "set-password", "set-home", "set-permissions").withRequiredArg();
            parser.accepts("password").requiredIf("create-user", "set-password").withRequiredArg();
            parser.accepts("home").requiredIf("create-user", "set-home").withRequiredArg();
            parser.accepts("write").requiredIf("create-user", "set-permissions").withRequiredArg();
            parser.accepts("usersfile").withRequiredArg();
            OptionSet options = parser.parse(args);
            if (options.has("create-user") || options.has("delete-user") || options.has("set-password") || options.has("set-home") || options.has("set-permissions") || options.has("list-users")) {
                try {
                    MiscUtils.configureLog4J(new PrintWriter(System.out));
                    File usersFile;
                    if (options.has("usersfile")) {
                        String filepath = (String) options.valueOf("usersfile");
                        usersFile = new File(filepath);
                    } else {
                        usersFile = new File(new File(System.getProperty("user.home")), Constants.DEFAULT_USERS_FILE_NAME);
                    }
                    if (!usersFile.isFile()) {
                        System.err.println(usersFile.getAbsolutePath() + " does not point to a valid file.");
                        System.exit(1);
                    }
                    UserManager um = new UserManager(usersFile);
                    String username = (String) options.valueOf("username");
                    if (options.has("create-user")) {
                        String password = (String) options.valueOf("password");
                        String home = (String) options.valueOf("home");
                        boolean canWrite = ((String) options.valueOf("write")).toLowerCase().equals("true");
                        um.createUser(username, password, home, canWrite);
                    } else if (options.has("delete-user")) {
                        um.deleteUser(username);
                    } else if (options.has("set-password")) {
                        String password = (String) options.valueOf("password");
                        um.setUserPassword(username, password);
                    } else if (options.has("set-home")) {
                        String home = (String) options.valueOf("home");
                        um.setUserRootDir(username, home);
                    } else if (options.has("set-permissions")) {
                        boolean canWrite = ((String) options.valueOf("write")).toLowerCase().equals("true");
                        um.setUserWritePermission(username, canWrite);
                    }
                    if(options.has("list-users")){
                        String[] usernames = um.listUsers();
                        for (String usrname : usernames) {
                            System.out.println(usrname);
                        }
                    }
                } catch (FtpException ftpe) {
                    System.err.println(ftpe.getMessage());
                }
            } else {
                printUsage(System.out);
            }
        } catch (OptionException oe) {
            printUsage(System.out);
        }

    }

    private static void printUsage(PrintStream out) {
        out.println("Wrong usage! Correct is:");
        out.println("    java -jar simpleftpd-manager.jar --<action> <--<parameter> <value>> [--usersfile <properties file path>]");
        out.println("where <action> and the associated parameters may be one of the following:");
        out.println("    create-user --username <username> --password <password> --home <user's home directory> --write <true/false>");
        out.println("    delete-user --username <username>");
        out.println("    list-users");
        out.println("    set-password --username <username> --password <password>");
        out.println("    set-home --username <username> --home <user's home directory>");
        out.println("    set-permissions --username <username> --write <true/false>");
        out.println();
    }
}
