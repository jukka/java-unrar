/**
 * 
 */
package de.innosystec.unrar.protocols.rar;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * @author Luca Santarelli hrk@users.sourceforge.net
 */
public class Handler extends URLStreamHandler {
	@Override
	protected URLConnection openConnection(URL url) throws IOException {
		return new RarURLConnection(url);
	}
}
