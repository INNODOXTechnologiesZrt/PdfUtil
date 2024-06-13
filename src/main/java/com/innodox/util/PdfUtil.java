package com.innodox.util;

import com.innodox.exception.ResourceNotFoundException;
import com.innodox.model.folding.FoldingLine;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;

import com.itextpdf.text.pdf.*;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;


public class PdfUtil {

    private static final BaseColor BASE_COLOR = new BaseColor(127, 127, 127);
    private static final Logger log
            = LoggerFactory.getLogger(PdfUtil.class);
    /**
     * Retrieves the attachments with PDF files from the given PDF byte array.
     *
     * @param pdf The PDF byte array from which to retrieve the attachments.
     * @param attachmentExtensions The allowed attachment file extensions. If not provided, all attachments will be retrieved.
     * @return A map containing the names and byte arrays of the PDF attachments. The key is the attachment name, and the value
     *         is the byte array representing the attachment.
     * @throws IOException If an error occurs while reading the PDF byte array.
     */

    public static Map<String, byte[]> getPdfAttachments(byte[] pdf, String... attachmentExtensions) throws IOException {
        Map<String, byte[]> files = new HashMap<>();

        PdfReader reader = new PdfReader(pdf);
        PdfDictionary root = reader.getCatalog();
        PdfDictionary names = root.getAsDict(PdfName.NAMES); // may be null
        PdfDictionary embeddedFilesDict = names.getAsDict(PdfName.EMBEDDEDFILES); //may be null
        PdfArray embeddedFiles = embeddedFilesDict.getAsArray(PdfName.NAMES); // may be null

        int len = embeddedFiles.size();
        for (int i = 0; i < len; i += 2) {
            PdfString name = embeddedFiles.getAsString(i); // should always be present
            log.debug("PdfString name: {}", name);

            if (name != null) {
                if (attachmentExtensions != null &&
                        !Arrays.asList(attachmentExtensions).contains(FilenameUtils.getExtension(name.toUnicodeString()))) {
                    log.debug(
                            "Extension is not part of required attachment extensions. Returning emptyMap. Extension: {}",
                            FilenameUtils.getExtension(name.toUnicodeString()));
                    return Collections.emptyMap();
                }

                PdfDictionary fileSpec = embeddedFiles.getAsDict(i + 1);

                PdfDictionary streams = fileSpec.getAsDict(PdfName.EF);
                PRStream stream;
                if (streams.contains(PdfName.UF)) {
                    log.debug("streams contains PdfName.UF");
                    stream = (PRStream) streams.getAsStream(PdfName.UF);
                } else {
                    log.debug("streams does not contain PdfName.UF");
                    stream = (PRStream) streams.getAsStream(PdfName.F); // Default stream for backwards compatibility
                }

                if (stream != null) {
                    files.put(name.toUnicodeString(), PdfReader.getStreamBytes(stream));
                }
            }
        }
        return files;
    }

    /**
     * Draws a line on a PDF document.
     *
     * @param pdfContentByte the PdfContentByte object that represents the content of the PDF document
     * @param foldingLine    the FoldingLine object representing the start and end coordinates of the line
     */

    private static void drawLine(PdfContentByte pdfContentByte, FoldingLine foldingLine) {
        float startX = foldingLine.getStart().getXCoordinate();
        float startY = foldingLine.getStart().getYCoordinate();
        float endX = foldingLine.getEnd().getXCoordinate();
        float endY = foldingLine.getEnd().getYCoordinate();

        log.trace("Drawing line from x: {} y: {} to x: {} y: {}", startX, startY, endX, endY);
        pdfContentByte.moveTo(startX, startY);
        pdfContentByte.lineTo(endX, endY);
    }

    /**
     * Adds folding lines to a given PDF document.
     *
     * @param document      the original PDF document as a byte array
     * @param foldingLines  a list of FoldingLine objects representing the lines to be added
     * @return the modified PDF document as a byte array
     * @throws RuntimeException if there is an error while drawing the lines on the PDF document
     */

    public static byte[] addFoldingLines(byte[] document, List<FoldingLine> foldingLines) {
        // TODO return meta information so that it can be logged in the service layer
        try {
            PdfReader reader = new PdfReader(document);
            int numberOfPages = reader.getNumberOfPages();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            PdfStamper stamper = new PdfStamper(reader, bos);

            PdfContentByte pdfContentByte;
            for (int i = 1; i <= numberOfPages; i++) {
                log.debug("Adding lines to page: {}", i);
                pdfContentByte = stamper.getOverContent(i);
                pdfContentByte.saveState();
                pdfContentByte.setColorStroke(BASE_COLOR);
                for (FoldingLine foldingLine : foldingLines) {
                    drawLine(pdfContentByte, foldingLine);
                }
                pdfContentByte.stroke();
                pdfContentByte.restoreState();
            }
            stamper.close();
            reader.close();
            log.debug("Line drawing completed.");
            return bos.toByteArray();
        } catch (IOException | DocumentException e) {
            throw new RuntimeException("Could not draw lines on pdf document", e);
        }
    }

    /**
     * Returns the number of pages in a PDF document.
     *
     * @param document the byte array representing the PDF document
     * @return the number of pages in the PDF document
     * @throws RuntimeException if the document cannot be read
     */

    public static int getPageCount(byte[] document) {
        log.debug("Getting pages count of document. Data length: {}", document.length);
        try {
            PdfReader reader = new PdfReader(document);
            int numberOfPages = reader.getNumberOfPages();
            reader.close();

            log.debug("Page count: {}", numberOfPages);

            return numberOfPages;
        } catch (IOException e) {
            throw new RuntimeException("Could not read document.");
        }
    }

    /**
     * Retrieves a specific page from a PDF document.
     *
     * @param document    The byte array representation of the PDF document.
     * @param pageNumber The page number to retrieve (1-based index).
     * @return The byte array representation of the specified page.
     * @throws RuntimeException If an error occurs while reading the document.
     */

    public static byte[] getPage(byte[] document, int pageNumber) {
        log.debug("Getting page from document. Page: {}, document data length: {}", pageNumber, document.length);
        try {
            PdfReader reader = new PdfReader(document);
            int numberOfPages = reader.getNumberOfPages();

            validatePageNumber(pageNumber, numberOfPages);

            reader.selectPages(String.valueOf(pageNumber));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfStamper stamper = new PdfStamper(reader, outputStream);

            stamper.close();
            reader.close();

            log.debug("Pdf created from page: {}", pageNumber);
            return outputStream.toByteArray();
        } catch (IOException | DocumentException e) {
            throw new RuntimeException("Could not read document.");
        }
    }

    /**
     * Validates the page number.
     *
     * @param pageNumber   the requested page number to validate
     * @param numberOfPages   the total number of pages
     * @throws ResourceNotFoundException   if the requested page number is invalid (less than or equal to 0, or greater than the number of pages)
     */

    private static void validatePageNumber(int pageNumber, int numberOfPages) {
        log.debug("Validating page number. Requested page: {}, number of pages: {}", pageNumber, numberOfPages);
        if (pageNumber <= 0 || pageNumber > numberOfPages) {
            throw new ResourceNotFoundException(
                    String.format(
                            "Page not found: %d",
                            pageNumber
                    )
            );
        }
        log.debug("Page number is valid. Requested page: {}, number of pages: {}", pageNumber, numberOfPages);
    }
}
