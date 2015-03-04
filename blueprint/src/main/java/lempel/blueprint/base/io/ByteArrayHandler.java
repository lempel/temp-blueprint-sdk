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
package lempel.blueprint.base.io;

/**
 * Wraps a byte[] and handles with offset and length.<br>
 * Some methods can change offset while others are not.<br>
 * <b>This class is not thread safe.</b><br>
 *
 * @author Sangmin Lee
 * @since 2008. 02. 05
 */
public class ByteArrayHandler {
    private final byte[] data;
    private int offset = 0;

    public ByteArrayHandler(final byte[] arr) {
        data = arr.clone();
    }

    /**
     * @param src
     * @param tar
     * @return true: identical, false: different
     */
    public static boolean compareByteArray(final byte[] src, final byte[] tar) {
        if (src.length != tar.length) {
            return false;
        }

        for (int i = 0; i < src.length; i++) {
            if (src[i] != tar[i]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns a byte form current offset.<br>
     * <b>Offset will be increased by 1.</b><br>
     *
     * @return
     */
    public byte getByte() {
        return data[offset++];
    }

    /**
     * Returns a byte form current offset
     *
     * @param offset
     * @return
     */
    public byte getByte(final int offset) {
        return data[offset];
    }

    /**
     * Returns length bytes from current offset.<br>
     * <b>Offset will be increased by length.</b><br>
     *
     * @param length
     * @return
     */
    public byte[] getBytes(final int length) {
        byte[] result = getBytes(offset, offset + length);
        offset += length;
        return result;
    }

    /**
     * Returns a byte[] from start to end
     *
     * @param startIndex
     * @param endIndex
     * @return
     */
    public byte[] getBytes(final int startIndex, final int endIndex) {
        byte[] result = new byte[endIndex - startIndex];
        System.arraycopy(data, startIndex, result, 0, result.length);
        return result;
    }

    /**
     * Returns a byte[] from current offset to delimiter or end.<br>
     * <b>Offset will be increased by length.</b><br>
     *
     * @param delimeter
     * @return
     */
    public byte[] getBytesUntil(final byte delimeter) {
        byte[] result = getBytesUntil(offset, delimeter);
        offset += result.length;
        return result;
    }

    /**
     * Return a byte[] from start to delimiter or end
     *
     * @param startIndex
     * @param delimeter
     * @return
     */
    public byte[] getBytesUntil(final int startIndex, final byte delimeter) {
        return getBytesUntil(startIndex, new byte[]{delimeter});
    }

    /**
     * Returns a byte[] from current offset to one of delimiters or end.<br>
     * <b>Offset will be increased by length.</b><br>
     *
     * @param delimeters
     * @return
     */
    public byte[] getBytesUntil(final byte[] delimeters) {
        byte[] result = getBytesUntil(offset, delimeters);
        offset += result.length;
        return result;
    }

    /**
     * Returns a byte[] from start to one of delimiters or end
     *
     * @param startIndex
     * @param delimeters
     * @return
     */
    public byte[] getBytesUntil(final int startIndex, final byte[] delimeters) {
        int pos = find(startIndex, delimeters);
        if (pos < 0) {
            pos = data.length;
        }

        byte[] result = new byte[pos - startIndex];
        System.arraycopy(data, startIndex, result, 0, result.length);
        return result;
    }

    /**
     * @param delimeter
     * @return index or -1 (not found)
     */
    public int find(final byte delimeter) {
        return find(offset, delimeter);
    }

    /**
     * @param delimeters
     * @return index or -1 (not found)
     */
    public int find(final byte[] delimeters) {
        return find(offset, delimeters);
    }

    /**
     * @param startIndex
     * @param delimeter
     * @return index or -1 (not found)
     */
    public int find(final int startIndex, final byte delimeter) {
        return find(startIndex, new byte[]{delimeter});
    }

    /**
     * @param startIndex
     * @param delimeters
     * @return index or -1 (not found)
     */
    public int find(final int startIndex, final byte[] delimeters) {
        return find(startIndex, -1, delimeters);
    }

    /**
     * @param startIndex
     * @param delimeters
     * @return index or -1 (not found)
     */
    public int find(final int startIndex, final int endIndex, final byte[] delimeters) {
        int last = (endIndex == -1 ? data.length : endIndex);
        for (int i = startIndex; i < last; i++) {
            for (int j = 0; j < delimeters.length; j++) {
                if (data[i] == delimeters[j]) {
                    return i;
                }
            }
        }

        return endIndex;
    }

    /**
     * Returns index of all sequence matches
     *
     * @param sequence
     * @return index or -1 (not found)
     */
    public int findAll(final byte[] sequence) {
        return findAll(offset, sequence);
    }

    /**
     * Returns index of all sequence matches
     *
     * @param startIndex
     * @param sequence
     * @return index or -1 (not found)
     */
    public int findAll(final int startIndex, final byte[] sequence) {
        return findAll(startIndex, -1, sequence);
    }

    /**
     * Returns index of all sequence matches
     *
     * @param startIndex
     * @param sequence
     * @return index or -1 (not found)
     */
    public int findAll(final int startIndex, final int endIndex, final byte[] sequence) {
        int cnt = 0;
        int last = (endIndex == -1 ? data.length : endIndex);
        for (int i = startIndex; i < last; i++) {
            if (data[i] == sequence[cnt]) {
                cnt++;
                if (cnt == sequence.length) {
                    return i - cnt + 1;
                }
            } else {
                cnt = 0;
            }
        }

        return endIndex;
    }

    public int getLength() {
        return data.length;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(final int offset) {
        this.offset = offset;
    }

    /**
     * Increase offset by 1
     *
     * @return offset
     */
    public int forward() {
        return forward(1);
    }

    public int forward(final int count) {
        offset += count;
        return offset;
    }

    /**
     * Decrease offset by 1
     *
     * @return offset
     */
    public int backward() {
        return backward(1);
    }

    public int backward(final int count) {
        offset -= count;
        if (offset < 0) {
            offset = 0;
        }

        return offset;
    }

    /**
     * Resets offset to 0
     */
    public void reset() {
        offset = 0;
    }
}
