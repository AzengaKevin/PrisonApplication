package org.epics.internals.printing;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import java.io.FileNotFoundException;
import java.io.IOException;

public class TablePrinter extends BasePrinter {

    public TablePrinter(String destination) throws FileNotFoundException {
        super(destination);
    }

    public void addTitle(String title) throws IOException {

        addBlockElement(new Paragraph(title).setFontSize(24.0F));

    }

    @Override
    public void initDocument(int margins, PageSize pageSize) {


        document = new Document(pdfDocument, pageSize);
    }
}
