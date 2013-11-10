package de.innosystec.unrar.protocols.rar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownServiceException;

import de.innosystec.unrar.Archive;
import de.innosystec.unrar.exception.RarException;
import de.innosystec.unrar.rarfile.FileHeader;

/**
 * @author Luca Santarelli luca.santarelli@gmail.com
 */
public class RarURLConnection extends URLConnection {

	/**
	 * Will contain file data after a successful connection.
	 */
	private InputStream is = null;
	private int contentLength = -1;
	private String contentType;

	private URL rarURL = null;
	private String entry = null;

	protected RarURLConnection(URL url) throws MalformedURLException {
		super(url);

		String[] aux = url.getPath().split("!/");
		if (aux.length > 2) {
			throw new MalformedURLException();
		} else {
			rarURL = new URL(aux[0]);
			if (aux.length == 2) {
				entry = aux[1];
			}
		}
	}

	@Override
	public void connect() throws IOException {
		// System.out.println("Requested to connect; url is " + url);

		try {
			File rarFile;
			if (rarURL.getProtocol().compareTo("file") == 0) {
				rarFile = new File(rarURL.toURI());
			} else {
				throw new IOException("Cannot open rar files on protocol " + rarURL.getProtocol() + " (yet)");
			}

			if (!rarFile.exists()) {
				throw new FileNotFoundException("File not found: " + rarFile.toURI());
			}

			/*
			 * The following line raises a RarException if the file is not a true rar
			 * file.
			 */
			Archive rarArchive = new Archive(rarFile);
			if (entry == null) {
				contentLength = (int) rarFile.length();
				contentType = "application/x-rar-compressed";
				is = new FileInputStream(rarFile);
			} else {
				FileHeader fileHeader = null;
				while ((fileHeader = rarArchive.nextFileHeader()) != null) {
					if (!fileHeader.isDirectory()) {
						if (fileHeader.getFileNameString().compareTo(entry) == 0) {
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							rarArchive.extractFile(fileHeader, baos);
							baos.flush();
							contentLength = baos.size();
							contentType = "unknown";
							is = new RarEntryInputStream(rarArchive, baos.toByteArray());
							break;
						}
					}
				}
				if (is == null) {
					throw new FileNotFoundException("Entry " + entry + " not found in archive " + rarFile.toURI());
				}
			}
			connected = true;
		} catch (URISyntaxException usex) {
			usex.printStackTrace();
			IOException ioex = new IOException(usex.getMessage());
			ioex.setStackTrace(usex.getStackTrace());
			throw ioex;
		} catch (RarException rex) {
			rex.printStackTrace();
			IOException ioex = new IOException(rex.getMessage());
			ioex.setStackTrace(rex.getStackTrace());
			throw ioex;
		}
	}

	public InputStream getInputStream() throws IOException {
		if (!connected) {
			connect();
		}
		if (is == null) {
			throw new IOException("File " + entry + " does not exist in archive " + rarURL);
		}
		return is;
	}

	public int getContentLength() {
		return contentLength;
	}

	public String getContentType() {
		return contentType;
	}

	/*
	 * Methods with no meaning.
	 */
	public OutputStream getOutputStream() throws UnknownServiceException {
		throw new UnknownServiceException();
	}
}
