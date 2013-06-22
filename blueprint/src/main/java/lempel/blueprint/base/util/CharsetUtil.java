/*
 License:

 blueprint-sdk is licensed under the terms of Eclipse Public License(EPL) v1.0
 (http://www.eclipse.org/legal/epl-v10.html)


 Distribution:

 International - http://code.google.com/p/blueprint-sdk
 South Korea - http://lempel.egloos.com


 Background:

 blueprint-sdk is a java software development kit to protect other open source
 softwares' licenses. It's intended to provide light weight APIs for blueprints.
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


 To commiters:

 License terms of the other software used by your source code should not be
 violated by using your source code. That's why blueprint-sdk is made for.
 Without that, all your contributions are welcomed and appreciated.
 */
package lempel.blueprint.base.util;

import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

/**
 * Character Set related methods for KSC5601 (EUC-KR) & ISO-8859_1 (Unicode)
 * 
 * @author Simon Lee
 * @version $Revision$
 * @since 2002. 07. 30
 * @last $Date$
 */
public class CharsetUtil {
	private static final String KO_KR = "ko";
	private static final String ENC_KSC5601 = "KSC5601";
	private static final String ENC_8859_1 = "8859_1";

	/** System.out's encoding type */
	private static String defaultEncoding;

	/** System Language */
	protected static String systemLang = null;

	static {
		systemLang = Locale.getDefault().getLanguage();
		defaultEncoding = new OutputStreamWriter(System.out).getEncoding();
	}

	/**
	 * 8859_1 -> KSC5601 (Unicode to EUC-KR)
	 * 
	 * @param str
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String from8859to5601(final String str) throws UnsupportedEncodingException {
		return new String(str.getBytes(ENC_8859_1), ENC_KSC5601);
	}

	/**
	 * KSC5601 -> 8859_1 (EUC-KR to Unicode)
	 * 
	 * @param str
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String from5601to8859(final String str) throws UnsupportedEncodingException {
		return new String(str.getBytes(ENC_KSC5601), ENC_8859_1);
	}

	public static String to8859(final String src) {
		String res;
		try {
			if (KO_KR.equals(systemLang)) {
				res = new String(src.getBytes(), ENC_8859_1);
			} else {
				res = src;
			}
		} catch (UnsupportedEncodingException exUE) {
			res = src;
		}

		return res;
	}

	public static String to5601(final String src) {
		String res;
		try {
			if (KO_KR.equals(systemLang)) {
				res = src;
			} else {
				res = new String(src.getBytes(), ENC_KSC5601);
			}
		} catch (UnsupportedEncodingException exUE) {
			res = src;
		}

		return res;
	}

	public static String from8859(final String src) {
		String res;
		try {
			if (KO_KR.equals(systemLang)) {
				res = new String(src.getBytes(ENC_8859_1));
			} else {
				res = src;
			}
		} catch (UnsupportedEncodingException exUE) {
			res = src;
		}

		return res;
	}

	public static String from5601(final String src) {
		String res;
		try {
			if (KO_KR.equals(systemLang)) {
				res = src;
			} else {
				res = new String(src.getBytes(ENC_KSC5601));
			}
		} catch (UnsupportedEncodingException exUE) {
			res = src;
		}

		return res;
	}

	public static String getDefaultEncoding() {
		return defaultEncoding;
	}
}