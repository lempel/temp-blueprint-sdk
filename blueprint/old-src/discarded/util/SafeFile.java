package lempel.blueprint.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.net.URI;
import java.util.ArrayList;

/**
 * J2SE 1.4 이하에서만 필요<br>
 * path의 길이가 255byte보다 큰 경우 mkdir()/mkdirs()에서 파일시스템 순환참조 현상을 방지<br>
 * 
 * @author Simon Lee
 * @version $Revision$
 * @create 2005. 9. 13.
 * @since 1.5
 * @last $Date$
 * @see
 * @deprecated
 */
public class SafeFile extends File {
	static final long serialVersionUID = 20050913L;

	/**
	 * Constructor
	 * 
	 * @param pathname
	 */
	public SafeFile(String pathname) {
		super(pathname);
	}

	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param child
	 */
	public SafeFile(File parent, String child) {
		super(parent, child);
	}

	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param child
	 */
	public SafeFile(String parent, String child) {
		super(parent, child);
	}

	/**
	 * Constructor
	 * 
	 * @param uri
	 */
	public SafeFile(URI uri) {
		super(uri);
	}

	/**
	 * 안전한 path 인지 확인
	 * 
	 * @return
	 */
	public boolean isSafe() {
		if (getPath().length() > 255)
			return false;
		return true;
	}

	/*
	 * (non-Javadoc, override method)
	 * 
	 * @see java.io.File#mkdir()
	 */
	public boolean mkdir() {
		if (!isSafe())
			return false;
		return super.mkdir();
	}

	/*
	 * (non-Javadoc, override method)
	 * 
	 * @see java.io.File#mkdirs()
	 */
	public boolean mkdirs() {
		if (!isSafe())
			return false;
		return super.mkdirs();
	}

	/*
	 * (non-Javadoc, override method)
	 * 
	 * @see java.io.File#listFiles()
	 */
	public File[] listFiles() {
		String[] ss = list();
		if (ss == null)
			return null;
		int n = ss.length;
		SafeFile[] fs = new SafeFile[n];
		for (int i = 0; i < n; i++) {
			fs[i] = new SafeFile(getPath(), ss[i]);
		}
		return fs;
	}

	/*
	 * (non-Javadoc, override method)
	 * 
	 * @see java.io.File#listFiles(java.io.FileFilter)
	 */
	@SuppressWarnings("unchecked")
	public File[] listFiles(FileFilter filter) {
		String ss[] = list();
		if (ss == null)
			return null;
		ArrayList v = new ArrayList(100);
		for (int i = 0; i < ss.length; i++) {
			SafeFile f = new SafeFile(getPath(), ss[i]);
			if ((filter == null) || filter.accept(f)) {
				v.add(f);
			}
		}
		return (SafeFile[]) (v.toArray(new SafeFile[0]));
	}

	/*
	 * (non-Javadoc, override method)
	 * 
	 * @see java.io.File#listFiles(java.io.FilenameFilter)
	 */
	@SuppressWarnings("unchecked")
	public File[] listFiles(FilenameFilter filter) {
		String ss[] = list();
		if (ss == null)
			return null;
		ArrayList v = new ArrayList(100);
		for (int i = 0; i < ss.length; i++) {
			if ((filter == null) || filter.accept(this, ss[i])) {
				v.add(new SafeFile(getPath(), ss[i]));
			}
		}
		return (SafeFile[]) (v.toArray(new SafeFile[0]));
	}
}