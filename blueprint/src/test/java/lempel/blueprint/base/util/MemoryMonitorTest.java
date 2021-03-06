/*
 * Copyright 2009 Sangmin Lee, all rights reserved.
 */
package lempel.blueprint.base.util;

import java.util.ArrayList;

import blueprint.sdk.util.jvm.MemoryMonitor;

/**
 * Tests MemoryMonitor
 *
 * @author Sangmin Lee
 * @since 2009. 3. 2.
 */
public class MemoryMonitorTest {
    public static void main(final String[] args) {
        new MemoryMonitor().start();

        ArrayList<byte[]> list = new ArrayList<byte[]>();
        for (int i = 0; i < 1000000; i++) {
            if (MemoryMonitor.getMemoryUsage() < 80) {
                list.add(new byte[100000]);
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }
}
