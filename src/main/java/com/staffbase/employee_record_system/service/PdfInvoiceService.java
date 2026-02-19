package com.staffbase.employee_record_system.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.staffbase.employee_record_system.entity.Payment;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfInvoiceService {

    public byte[] generateInvoice(Payment payment) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);

        try {
            PdfWriter.getInstance(document, out);
            document.open();


            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, Color.BLUE);
            Font subHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);


            Paragraph title = new Paragraph("INVOICE", headerFont);
            title.setAlignment(Element.ALIGN_RIGHT);
            document.add(title);
            document.add(new Paragraph("\n"));


            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);


            PdfPCell companyCell = new PdfPCell();
            companyCell.setBorder(Rectangle.NO_BORDER);
            companyCell.addElement(new Paragraph("StaffBase Corp", subHeaderFont));
            companyCell.addElement(new Paragraph("123 Tech Avenue", normalFont));
            companyCell.addElement(new Paragraph("Silicon Valley, CA", normalFont));
            companyCell.addElement(new Paragraph("support@staffbase.com", normalFont));
            infoTable.addCell(companyCell);

            PdfPCell invoiceCell = new PdfPCell();
            invoiceCell.setBorder(Rectangle.NO_BORDER);
            invoiceCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            invoiceCell.addElement(new Paragraph("Invoice #: " + payment.getOrderId().replace("order_", ""), boldFont));
            invoiceCell.addElement(new Paragraph(
                    "Date: " + payment.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy")), normalFont));
            invoiceCell.addElement(new Paragraph("Status: " + payment.getStatus(), normalFont));
            infoTable.addCell(invoiceCell);

            document.add(infoTable);
            document.add(new Paragraph("\n\n"));

            document.add(new Paragraph("BILL TO:", boldFont));
            document.add(new Paragraph(payment.getUser().getEmail(), normalFont));
            document.add(new Paragraph("\n"));

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setWidths(new float[] { 3, 1, 1 });

            addTableHeader(table, "Plan Description");
            addTableHeader(table, "Qty");
            addTableHeader(table, "Amount");

            table.addCell(new Paragraph(payment.getPlanName() + " Subscription - 30 Days", normalFont));
            table.addCell(new Paragraph("1", normalFont));
            table.addCell(new Paragraph(payment.getCurrency() + " " + payment.getAmount(), normalFont));

            document.add(table);

            Paragraph total = new Paragraph("\nTotal: " + payment.getCurrency() + " " + payment.getAmount(), boldFont);
            total.setAlignment(Element.ALIGN_RIGHT);
            document.add(total);

            document.add(new Paragraph("\n\n\n"));
            Paragraph footer = new Paragraph("Thank you for choosing StaffBase!", normalFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }

    private void addTableHeader(PdfPTable table, String headerTitle) {
        PdfPCell header = new PdfPCell();
        header.setBackgroundColor(Color.LIGHT_GRAY);
        header.setBorderWidth(1);
        header.setPadding(5);
        header.setPhrase(new Phrase(headerTitle, FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
        table.addCell(header);
    }
}



