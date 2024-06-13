package com.innodox.manipulate;


import com.innodox.exception.BarcodeGenerationFailedException;
import com.innodox.exception.InvalidFileContentException;
import com.innodox.model.PdfAttachment;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.parser.PdfImageObject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Usage example:
 * <p>
 * 1. Merge list of files, the result contains the merged file.
 * <pre>
 *   PdfTransform.get(srcByte1) //have to init
 *                  .add(srcByte2) //add one byte[]
 *                  .addAll(srcByteList) //add list of byte[]
 *                  .merge(); //merge
 *  </pre>
 * <p>
 * 2. Merge list of files, the result contains the merged file with watermark.
 * <pre>
 *   PdfTransform.get(srcByte1) //have to init
 *                  .add(srcByte2) //add one byte[]
 *                  .addAll(srcByteList) //add list of byte[]
 *                  .mergeAndWatermark(); //merge and mark
 * </pre>
 * <p>
 * 3. Mark list of files
 * <pre>
 *   PdfTransform.get(srcByte1) //have to init
 *                  .watermark(); //mark
 * </pre>
 */

public class PdfTransform {

    private PdfTransform(){

    }

    /**
     * Create a new {@link PdfTransformBuilder} instance. For safe working, you have to instantiate the builder with this way.
     * Don't use this code: <pre>new PdfTransform.PdfTransformBuilder()</pre>
     *
     * @param pdfByte
     * @return
     */

    public static PdfTransformBuilder get(byte[] pdfByte) {
        PdfTransformBuilder builder = new PdfTransformBuilder();
        builder.add(pdfByte);
        return builder;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class PdfTransformBuilder {

        private List<byte[]> listOfDocs = new ArrayList<>();

        /**
         * Add an element for working list.
         *
         * @param pdf a new pdf
         * @return current builder instance
         */
        public PdfTransformBuilder add(byte[] pdf) {
            if (pdf != null) this.listOfDocs.add(pdf);
            return this;
        }

        /**
         * Add list of elements for working list.
         *
         * @param pdfList list of pdfs
         * @return current builder instance
         */
        public PdfTransformBuilder addAll(List<byte[]> pdfList) {
            if (pdfList != null && !pdfList.isEmpty()) this.listOfDocs.addAll(pdfList);
            return this;
        }

        /**
         *
         * @param document
         * @param attachments
         * @return document with the required attachments embedded
         * @throws IOException
         * @throws DocumentException
         */
        public byte[] addAttachments(
            byte[] document,
            List<PdfAttachment> attachments
        ) throws IOException, DocumentException {
            PdfReader reader = new PdfReader(document);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfStamper stamper = new PdfStamper(reader, outputStream);

            for (PdfAttachment attachment : attachments) {
                PdfFileSpecification fs = PdfFileSpecification.fileEmbedded(
                    stamper.getWriter(),
                    null,
                    attachment.getFilename(),
                    attachment.getData()
                );
                stamper.addFileAttachment(attachment.getDescription(), fs);
            }

            stamper.close();
            return outputStream.toByteArray();
        }

        /**
         * Need to add more than one byte array for merging.
         *
         * @return <ul>
         *      <li>byte[] if the list of documents contains more than one document</li>
         *      <li>empty byte array otherwise</li>
         *  </ul>
         */
        public byte[] merge() {
            if (this.listOfDocs.size() < 2) return new byte[0];
            return mergePdf(this.listOfDocs);
        }

        /**
         * Need to add more than one byte array for merging.
         *
         * @return <ul>
         *      <li>byte[] if the list of documents contains more than one document</li>
         *      <li>empty byte array otherwise</li>
         *  </ul>
         */
        public byte[] mergeAndWatermark() {
            if (this.listOfDocs.size() < 2) return new byte[0];
            byte[] mergedFile = mergePdf(this.listOfDocs);
            return watermark(mergedFile);
        }

        /**
         * Have to call if you have more than one docs in the list.
         *
         * @return list of marked docs
         */
        public List<byte[]> watermarkAll() {
            return watermark(this.listOfDocs);
        }

        /**
         * Mark the init document.
         *
         * @return marked document. if the list contains more than one document, it mark the first element od the list.
         */
        public byte[] watermark() {
            return watermark(this.listOfDocs.get(0));
        }

        private List<byte[]> watermark(List<byte[]> files) {
            return files.stream()
                .map(file -> watermark(file))
                .collect(Collectors.toList());
        }

        public List<byte[]> getBarcodeFromPdf() {
            if (this.listOfDocs.size() < 1) return Collections.emptyList();

            try {
                PdfReader reader = new PdfReader(listOfDocs.get(0));
                PdfDictionary pageResources = reader.getPageResources(1);
                PdfDictionary xObjects = pageResources.getAsDict(PdfName.XOBJECT);

                if (xObjects == null) {
                    throw new BarcodeGenerationFailedException();
                }

                return xObjects.getKeys().stream()
                    .filter(key -> {
                        try {
                            PRStream imgStream = (PRStream) xObjects.getAsStream(key);
                            PdfImageObject imgObject = new PdfImageObject(imgStream);

                            return imgObject.getImageAsBytes() != null && imgObject.getImageAsBytes().length > 0;
                        } catch (IOException e) {
                            throw new InvalidFileContentException(e);
                        }
                    })
                    .map(key -> {
                        try {
                            return new PdfImageObject((PRStream) xObjects.getAsStream(key)).getImageAsBytes();
                        } catch (IOException e) {
                            throw new InvalidFileContentException(e);
                        }
                    })
                    .collect(Collectors.toList());
            } catch (IOException ioe) {
                throw new InvalidFileContentException(ioe);
            }
        }

        /**
         * Applies a watermark to a PDF file.
         *
         * @param mergedFile The PDF file to watermark, represented as a byte array.
         * @return The watermarked PDF file, represented as a byte array.
         * @throws InvalidFileContentException If an error occurs while processing the PDF file.
         */

        private byte[] watermark(byte[] mergedFile) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            try {
                PdfReader reader = new PdfReader(mergedFile);
                int n = reader.getNumberOfPages();
                PdfStamper stamper = new PdfStamper(reader, outputStream);
                stamper.setRotateContents(false);

                // text watermark
                Font f = new Font(Font.FontFamily.COURIER, 84);
                Phrase p = new Phrase("PISZKOZAT", f);

                // transparency
                PdfGState gs1 = new PdfGState();
                gs1.setFillOpacity(0.2f);

                // properties
                PdfContentByte over;
                Rectangle pagesize;
                float x, y;

                // loop over every page
                for (int i = 1; i <= n; i++) {
                    pagesize = reader.getPageSize(i);
                    x = (pagesize.getLeft() + pagesize.getRight()) / 2;
                    y = (pagesize.getTop() + pagesize.getBottom()) / 2;
                    over = stamper.getOverContent(i);
                    over.saveState();
                    over.setGState(gs1);
                    ColumnText.showTextAligned(over, Element.ALIGN_CENTER, p, x, y, 45);
                    over.restoreState();
                }

                stamper.close();
                reader.close();

            } catch (IOException | DocumentException e) {
                throw new InvalidFileContentException(e);
            }

            return outputStream.toByteArray();
        }

        /**
         * Merge multiple PDF files into a single PDF file.
         *
         * @param files A list of byte arrays representing the PDF files to be merged.
         * @return A byte array representing the merged PDF file.
         */

        private byte[] mergePdf(List<byte[]> files) {
            List<PdfReader> readers =
                files.stream()
                    .map(file -> {
                        PdfReader pdfReader = null;

                        try {
                            pdfReader = new PdfReader(file);
                        } catch (IOException e) {
                            throw new InvalidFileContentException(e);
                        }

                        return pdfReader;
                    })
                    .collect(Collectors.toList());

            return mergePdfReaders(readers);
        }

        /**
         * Merges multiple PDF files represented by PdfReader objects into a single PDF file.
         *
         * @param readers A list of PdfReader objects representing the PDF files to be merged.
         * @return A byte array representing the merged PDF file.
         * @throws InvalidFileContentException If an error occurs while processing the PDF files.
         */

        private byte[] mergePdfReaders(List<PdfReader> readers) {
            Document document = new Document();

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                PdfCopy copy = new PdfCopy(document, outputStream);
                document.open();

                readers.forEach(reader -> {
                    try {

                        for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                            copy.addPage(copy.getImportedPage(reader, i));
                        }

                    } catch (DocumentException | IOException e) {
                        throw new InvalidFileContentException(e);
                    }
                });

                document.close();

                return outputStream.toByteArray();
            } catch (IOException | DocumentException e) {
                throw new InvalidFileContentException(e);
            }
        }

    }

}
