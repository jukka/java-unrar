package de.innosystec.unrar.protocols.rar;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import de.innosystec.unrar.Archive;

/**
 * This is an auxiliary class used in {@link RarURLConnection#getInputStream()}.
 * Its purpose is to hold a reference to the {@link Archive} file which contains
 * the requested entry, so that when the {@link #close()} method is called on
 * the stream, the corresponding {@link Archive#close()} method is called.
 * 
 * @author Luca Santarelli luca.santarelli@gmail.com
 */
public class RarEntryInputStream extends ByteArrayInputStream {

	Archive archive = null;

	public RarEntryInputStream(Archive archive, byte[] ba) {
		super(ba);
		this.archive = archive;
	}

	public void close() throws IOException {
		super.close();
		archive.close();
		archive = null;
	}
}
