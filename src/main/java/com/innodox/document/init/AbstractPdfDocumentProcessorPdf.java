package com.innodox.document.init;


import com.innodox.exception.InvalidFileContentException;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;

import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfDocument;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import org.apache.commons.io.IOUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public abstract class AbstractPdfDocumentProcessorPdf implements PdfPreProcessor, PdfPostProcessor  {

    private static final Logger log=LoggerFactory.getLogger(AbstractPdfDocumentProcessorPdf.class);
    protected abstract InputStream getHeaderStream();

    protected abstract InputStream getFooterStream();

    protected abstract boolean isHeaderEnabled();

    protected abstract boolean isFooterEnabled();

    @Override
    public byte[] preProcess(byte[] docxData) {
        return docxData;
    }

    @Override
    public byte[] postProcess(byte[] pdf) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            PdfStamper pdfStamper = new PdfStamper(new PdfReader(pdf), byteArrayOutputStream);
            PdfContentByte overContent = pdfStamper.getOverContent(1);
            PdfDocument pdfDocument = overContent.getPdfDocument();

            if(isHeaderEnabled()) {
                buildHeader(overContent, pdfDocument);
            }
            if(isFooterEnabled()) {
                buildFooter(overContent, pdfDocument);
            }

            pdfStamper.close();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException | DocumentException e) {
            throw new InvalidFileContentException(e);
        }
    }

    protected void buildHeader(PdfContentByte overContent, PdfDocument pdfDocument) throws IOException, DocumentException {
        buildStaticImages(
                overContent,
                getHeaderStream(),
                pdfDocument.left(),
                pdfDocument.top(40f),
                100f,
                250f);
    }

    protected void buildFooter(PdfContentByte overContent, PdfDocument pdfDocument) throws IOException, DocumentException {
        buildStaticImages(
                overContent,
                getFooterStream(),
                pdfDocument.left(),
                pdfDocument.bottom(5f),
                100f,
                pdfDocument.right() - pdfDocument.left()
        );
    }

    private void buildStaticImages( PdfContentByte overContent, InputStream imageDataStream, float absoluteX,
                                    float absoluteY, float heightScale, float widthScale) throws IOException, DocumentException {

        try (InputStream dataStream = imageDataStream /* cause of redeclaration: "Resource references are not supported at language level 8 */) {

            byte[] imageBytes = IOUtils.toByteArray(dataStream);

            Image image = Image.getInstance(imageBytes);
            image.setAbsolutePosition(absoluteX, absoluteY);
            image.scaleToFit(widthScale, heightScale);

            overContent.addImage(image);
        }
    }

    public InputStream getFileStream(boolean useEditableOrBundled, String filePathEditable, String filePathBundled) {
        if(useEditableOrBundled) {
            try {
                return new FileInputStream(filePathEditable);
            } catch (FileNotFoundException ex) {
                String errMsg = "The editable file not found by the given path: " + filePathEditable;
                log.error(errMsg);
                throw new IllegalStateException(errMsg, ex);
            }
        }
        InputStream bundledStream = this.getClass().getResourceAsStream(filePathBundled);
        if(bundledStream == null) {
            String errMsg = "The bundled file not found by the given path: " + filePathBundled;
            log.error(errMsg);
            throw new IllegalStateException(errMsg);
        }
        return bundledStream;
    }
}
