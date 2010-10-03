/*
 * Copyright (c) 2010 Jukka Zitting <jukka@zitting.name>
 *
 * the unrar licence applies to all junrar source and binary distributions
 * you are not allowed to use this source to re-create the RAR compression
 * algorithm
 */
package de.innosystec.unrar.tika;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;

import org.apache.tika.exception.TikaException;
import org.apache.tika.extractor.EmbeddedDocumentExtractor;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.XHTMLContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import de.innosystec.unrar.Archive;
import de.innosystec.unrar.exception.RarException;
import de.innosystec.unrar.rarfile.FileHeader;

public class RARParser implements Parser {

    private static final MediaType TYPE =
        MediaType.application("x-rar-compressed");

    public Set<MediaType> getSupportedTypes(ParseContext context) {
        return Collections.singleton(TYPE);
    }

    public void parse(
            InputStream stream, ContentHandler handler,
            Metadata metadata, ParseContext context)
            throws SAXException, IOException, TikaException {
        EmbeddedDocumentExtractor extractor =
            new EmbeddedDocumentExtractor(context);

        try {
            File file = TikaInputStream.get(stream).getFile();
            Archive archive = new Archive(file);

            metadata.set(Metadata.CONTENT_TYPE, TYPE.toString());
            XHTMLContentHandler xhtml =
                new XHTMLContentHandler(handler, metadata);
            xhtml.startDocument();
            for (FileHeader header : archive.getFileHeaders()) {
                Metadata entrydata = new Metadata();
                entrydata.set(
                        Metadata.RESOURCE_NAME_KEY,
                        header.getFileNameString());
                if (extractor.shouldParseEmbedded(entrydata)) {
                    extractor.parseEmbedded(stream, xhtml, entrydata, true);
                }
            }
            xhtml.endDocument();
        } catch (RarException e) {
            throw new TikaException("Unable to parse a RAR archive", e);
        }
    }

    public void parse(
            InputStream stream, ContentHandler handler, Metadata metadata)
            throws IOException, SAXException, TikaException {
        parse(stream, handler, metadata, new ParseContext());
    }

}
