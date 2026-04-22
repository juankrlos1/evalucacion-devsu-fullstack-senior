package com.banking.account.infrastructure.report;

import com.banking.account.application.dto.AccountStatementResponse;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;

/**
 * Infrastructure component responsible for rendering account statements as PDF.
 * Separates PDF rendering concerns from the business logic in ReportUseCase (SRP).
 *
 * @author Juan Carlos De La Cruz Perez - Devsu
 */
@Slf4j
@Component
public class PdfReportGenerator {

    private static final String[] TABLE_HEADERS = {
            "Date", "Customer", "Account", "Type",
            "Initial Balance", "Status", "Movement", "Available Balance"
    };

    /**
     * Generates a base64-encoded PDF document from the given statement rows.
     *
     * @param rows           the statement data rows
     * @param customerId     the customer identifier for the header
     * @param dateRangeLabel human-readable date range for the header
     * @return base64-encoded PDF bytes
     */
    public String generate(List<AccountStatementResponse> rows, String customerId, String dateRangeLabel) {
        var baos = new ByteArrayOutputStream();
        var document = new Document();
        PdfWriter.getInstance(document, baos);
        document.open();

        addHeader(document, customerId, dateRangeLabel);
        addStatementTable(document, rows);

        document.close();
        log.debug("PDF generated: customerId={}, rows={}, bytes={}", customerId, rows.size(), baos.size());
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    private void addHeader(Document document, String customerId, String dateRangeLabel) {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        var title = new Paragraph("Account Statement", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("Customer: " + customerId));
        document.add(new Paragraph("Date Range: " + dateRangeLabel));
        document.add(new Paragraph(" "));
    }

    private void addStatementTable(Document document, List<AccountStatementResponse> rows) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9);

        var table = new PdfPTable(TABLE_HEADERS.length);
        table.setWidthPercentage(100);

        for (String header : TABLE_HEADERS) {
            var cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        for (AccountStatementResponse row : rows) {
            table.addCell(new Phrase(row.date().toString(), cellFont));
            table.addCell(new Phrase(row.customer(), cellFont));
            table.addCell(new Phrase(row.accountNumber(), cellFont));
            table.addCell(new Phrase(row.accountType(), cellFont));
            table.addCell(new Phrase(row.initialBalance().toPlainString(), cellFont));
            table.addCell(new Phrase(row.active() ? "Active" : "Inactive", cellFont));
            table.addCell(new Phrase(row.movement().toPlainString(), cellFont));
            table.addCell(new Phrase(row.availableBalance().toPlainString(), cellFont));
        }

        document.add(table);
    }
}
