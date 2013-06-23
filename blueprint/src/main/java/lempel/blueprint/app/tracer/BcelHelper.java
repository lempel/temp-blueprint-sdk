package lempel.blueprint.app.tracer;

import java.io.IOException;
import java.io.InputStream;


import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.util.SyntheticRepository;

import blueprint.sdk.logger.Logger;

public class BcelHelper {
	private static final Logger LOGGER = Logger.getInstance();

	public static JavaClass lookupClass(InputStream is, String className) throws ClassNotFoundException {
		try {
			if (is != null) {
				ClassParser parser = new ClassParser(is, className);
				JavaClass result = parser.parse();

				// WCF
				// System.out.println("parsed class from stream: "+className);

				SyntheticRepository.getInstance().storeClass(result);

				return result;
			}
		} catch (IOException e) {
			throw new ClassNotFoundException("Exception while looking for class " + className + ": " + e.toString());
		}
		throw new ClassNotFoundException("SyntheticRepository could not load " + className);
	}
}
