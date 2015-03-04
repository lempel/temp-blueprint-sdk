/*
 * Copyright 2008 Sangmin Lee, all rights reserved.
 */
package lempel.blueprint.base.xml;

import blueprint.sdk.logger.Logger;
import org.dom4j.io.DOMReader;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;

/**
 * XP Test
 *
 * @author Sangmin Lee
 * @create 2008. 12. 8.
 * @see
 * @since 1.5
 */
public class XPTest {
    private static final Logger LOGGER = Logger.getInstance();

    // private static boolean debug = true;
    private static boolean debug = false;

    // private static String testFile = "sample_utf8.xml";
    // private static String testFile = "log4j.xml";
    // private static String testFile = "log4j_no_dtd.xml";
    private static String testFile = "D:/work/blueprint/src/test/java/lempel/blueprint/base/xml/log4j_no_dtd.xml";

    public static void main(final String[] args) throws Throwable {
        XP xp = new XP();
        Document doc = xp.parse(testFile);
        XP.print(doc, System.out);

        runJdkParser();
        runXP();
    }

    private static void runXP() throws InterruptedException {
        Thread[] threads = new Thread[20];

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread() {
                public void run() {
                    int count;
                    if (debug) {
                        count = 1;
                    } else {
                        count = 1000;
                    }

                    try {
                        XP xp = new XP();

                        for (int l = 0; l < count; l++) {
                            Document result = xp.parse(testFile);
                            if (debug) {
                                print(result);
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.trace(e);
                    }
                }
            };
        }

        long start = System.currentTimeMillis();
        for (Thread target : threads) {
            target.start();
        }
        for (Thread target : threads) {
            target.join();
        }
        long end = System.currentTimeMillis();
        Runtime rtime = Runtime.getRuntime();
        LOGGER.println("XP elapsed = " + (end - start) + " used memory = "
                + (rtime.totalMemory() - rtime.freeMemory()));
    }

    private static void runJdkParser() throws InterruptedException {
        Thread[] threads = new Thread[20];

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread() {
                public void run() {
                    int count;
                    if (debug) {
                        count = 1;
                    } else {
                        count = 1000;
                    }

                    try {
                        DocumentBuilder builder = DocumentBuilderFactory.newInstance()
                                .newDocumentBuilder();

                        for (int l = 0; l < count; l++) {
                            Document result = builder.parse(testFile);
                            // SAXReader reader = new SAXReader();
                            // org.dom4j.Document dom4jDoc = reader
                            // .read(TEST_FILE);

                            if (debug) {
                                print(result);
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.trace(e);
                    }
                }
            };
        }

        long start = System.currentTimeMillis();
        for (Thread target : threads) {
            target.start();
        }
        for (Thread target : threads) {
            target.join();
        }
        long end = System.currentTimeMillis();
        Runtime rtime = Runtime.getRuntime();
        LOGGER.println("JDK elapsed = " + (end - start) + " used memory = "
                + (rtime.totalMemory() - rtime.freeMemory()));
    }

    private static void print(final Document result) throws IOException {
        org.dom4j.Document dom4jDoc = new DOMReader().read(result);
        print(dom4jDoc);
    }

    private static void print(final org.dom4j.Document dom4jDoc) throws IOException {
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("EUC-KR");
        XMLWriter writer = new XMLWriter(System.out, format);
        writer.write(dom4jDoc);
        writer.close();
    }
}
