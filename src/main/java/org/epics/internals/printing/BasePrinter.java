package org.epics.internals.printing;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AbstractElement;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.BlockElement;
import com.itextpdf.layout.element.Image;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public abstract class BasePrinter {

    protected OutputStream fileOutputStream;

    protected PdfWriter pdfWriter;

    protected PdfDocument pdfDocument;

    private List<AbstractElement<?>> elementList;

    protected Document document;

    public BasePrinter(String destination) throws FileNotFoundException {

        fileOutputStream = new FileOutputStream(destination);

        pdfWriter = new PdfWriter(fileOutputStream);

        pdfDocument = new PdfDocument(pdfWriter);

        elementList = new ArrayList<>();

    }

    abstract public void initDocument(int margins, PageSize pageSize);

    public void addAreaBreak(AreaBreak areaBreak) {
        elementList.add(areaBreak);
    }

    public void addBlockElement(BlockElement<?> blockElement) {
        elementList.add(blockElement);
    }

    public void addImage(Image image) {
        elementList.add(image);
    }

    public void print() {

        elementList.forEach(element -> {
            if (element instanceof AreaBreak) document.add((AreaBreak) element);
            if (element instanceof Image) document.add((Image) element);
            if (element instanceof BlockElement<?>) document.add((BlockElement<?>) element);
        });

        document.close();

    }
}
