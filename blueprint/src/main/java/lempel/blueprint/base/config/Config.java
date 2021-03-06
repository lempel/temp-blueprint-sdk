/*
 License:

 blueprint-sdk is licensed under the terms of Eclipse Public License(EPL) v1.0
 (http://www.eclipse.org/legal/epl-v10.html)


 Distribution:

 International - http://code.google.com/p/blueprint-sdk
 South Korea - http://lempel.egloos.com


 Background:

 blueprint-sdk is a java software development kit to protect other open source
 software licenses. It's intended to provide light weight APIs for blueprints.
 Well... at least trying to.

 There are so many great open source projects now. Back in year 2000, there
 were not much to use. Even JDBC drivers were rare back then. Naturally, I have
 to implement many things by myself. Especially dynamic class loading, networking,
 scripting, logging and database interactions. It was time consuming. Now I can
 take my picks from open source projects.

 But I still need my own APIs. Most of my clients just don't understand open
 source licenses. They always want to have their own versions of open source
 projects but don't want to publish derivative works. They shouldn't use open
 source projects in the first place. So I need to have my own open source project
 to be free from derivation terms and also as a mediator between other open
 source projects and my client's requirements.

 Primary purpose of blueprint-sdk is not to violate other open source project's
 license terms.


 To committers:

 License terms of the other software used by your source code should not be
 violated by using your source code. That's why blueprint-sdk is made for.
 Without that, all your contributions are welcomed and appreciated.
 */
package lempel.blueprint.base.config;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * Common methods for Configuration Classes
 *
 * @author Sangmin Lee
 * @since 2007. 10. 22
 */
public interface Config {
    boolean getBoolean(String path);

    double getDouble(String path);

    float getFloat(String path);

    int getInt(String path);

    long getLong(String path);

    String getString(String path);

    void load(String fileName) throws IOException, ParserConfigurationException, SAXException;

    void save(String fileName) throws IOException;

    String toString();
}
