package com.innodox.document.init;

public interface PdfPostProcessor {
    byte[] postProcess(byte[] pdfData);
}
