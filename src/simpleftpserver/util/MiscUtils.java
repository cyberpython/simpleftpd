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
package simpleftpserver.util;

import java.io.Writer;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;

/**
 *
 * @author Georgios Migdos <cyberpython@gmail.com>
 */
public class MiscUtils {
    /**
     * Configures the Apache Log4J logger.
     */
    public static void configureLog4J(Writer writer) {
        org.apache.log4j.Logger log = org.apache.log4j.Logger.getRootLogger();
        log.setLevel(Level.INFO);
        log.removeAllAppenders();
        Layout l = new PatternLayout("%d{yyyy-MM-dd HH:mm} %-5p %m%n");
        WriterAppender a = new WriterAppender(l, writer);
        log.addAppender(a);
    }
}
