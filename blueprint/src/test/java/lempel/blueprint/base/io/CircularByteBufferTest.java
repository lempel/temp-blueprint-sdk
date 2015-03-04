package lempel.blueprint.base.io;

import blueprint.sdk.logger.Logger;

/**
 * CircularByteBuffer Test
 *
 * @author Sangmin Lee
 * @create 2008. 11. 25.
 * @see
 * @since 1.5
 */
public class CircularByteBufferTest {
    private static final Logger LOGGER = Logger.getInstance();

    /**
     * Entry Point
     *
     * @param args
     * @throws InterruptedException
     */
    public static void main(final String[] args) throws InterruptedException {
        CircularByteBuffer cbb = new CircularByteBuffer(10, false, false);

        cbb.push("12345".getBytes());
        cbb.push("67890".getBytes());
        LOGGER.println(new String(cbb.pop()));

        cbb.push("1234567890".getBytes());
        cbb.push("_____".getBytes());
        LOGGER.println(new String(cbb.pop()));

        cbb.push("1234567890".getBytes());
        cbb.resize(15);
        cbb.push("_____".getBytes());
        LOGGER.println(new String(cbb.pop()));

        cbb.push("1234567890abcdefghijklmnopqrstuvwxyz".getBytes());
        LOGGER.println(new String(cbb.pop()));
    }
}
