package com.innodox.document.util;

import com.innodox.util.PdfUtil;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


public class PdfUtilTest {

    @Test
    void testPageCount() throws IOException {
        // Prepare a sample PDF file with attachments for testing
        Path sourceDir = Paths.get("src/test/resources/sample_with_attachments.pdf");
        byte[] pdfBytes = Files.readAllBytes(Paths.get(sourceDir.toString()));

        int pageCount = PdfUtil.getPageCount(pdfBytes);

        // Verify the results
        assertEquals(3,pageCount);
    }

    @Test
    void testPageGetNthPageContent() throws IOException {
        // Prepare a sample PDF file with attachments for testing
        Path sourceDir = Paths.get("src/test/resources/sample_with_attachments.pdf");
        byte[] pdfBytes = Files.readAllBytes(Paths.get(sourceDir.toString()));

        byte[] pageContent = PdfUtil.getPage(pdfBytes,1);

        // Verify the results
        assertTrue(pageContent.length > 0);
    }
}
