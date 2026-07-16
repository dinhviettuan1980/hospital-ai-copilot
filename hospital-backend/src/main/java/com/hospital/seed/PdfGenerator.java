package com.hospital.seed;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates a genuinely valid, single-page PDF containing a title, so seeded
 * Knowledge Center sample documents are real, openable files rather than
 * opaque placeholder bytes. No external library needed for something this
 * small.
 */
final class PdfGenerator {

    private PdfGenerator() {
    }

    static byte[] generate(String title) {
        String safeTitle = title.replace("(", "\\(").replace(")", "\\)");
        String content = "BT /F1 18 Tf 50 720 Td (%s) Tj ET".formatted(safeTitle);

        List<String> objects = new ArrayList<>();
        objects.add("<</Type/Catalog/Pages 2 0 R>>");
        objects.add("<</Type/Pages/Kids[3 0 R]/Count 1>>");
        objects.add("<</Type/Page/Parent 2 0 R/MediaBox[0 0 612 792]/Contents 4 0 R"
                + "/Resources<</Font<</F1 5 0 R>>>>>>");
        objects.add("<</Length %d>>stream\n%s\nendstream".formatted(content.getBytes(StandardCharsets.US_ASCII).length,
                content));
        objects.add("<</Type/Font/Subtype/Type1/BaseFont/Helvetica>>");

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            List<Integer> offsets = new ArrayList<>();
            write(out, "%PDF-1.4\n");

            for (int i = 0; i < objects.size(); i++) {
                offsets.add(out.size());
                write(out, (i + 1) + " 0 obj" + objects.get(i) + "endobj\n");
            }

            int xrefStart = out.size();
            write(out, "xref\n0 %d\n".formatted(objects.size() + 1));
            write(out, "0000000000 65535 f \n");
            for (int offset : offsets) {
                write(out, "%010d 00000 n \n".formatted(offset));
            }
            write(out, "trailer<</Size %d/Root 1 0 R>>\nstartxref\n%d\n%%%%EOF"
                    .formatted(objects.size() + 1, xrefStart));

            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void write(ByteArrayOutputStream out, String text) throws IOException {
        out.write(text.getBytes(StandardCharsets.US_ASCII));
    }
}
