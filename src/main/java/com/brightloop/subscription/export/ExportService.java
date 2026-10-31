package com.brightloop.subscription.export;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brightloop.subscription.billing.Invoice;
import com.brightloop.subscription.billing.InvoiceLine;
import com.brightloop.subscription.billing.InvoiceRepository;
import com.brightloop.subscription.billing.InvoiceStatus;
import com.brightloop.subscription.currency.CurrencyService;
import com.brightloop.subscription.payment.Payment;
import com.brightloop.subscription.payment.PaymentRepository;
import com.brightloop.subscription.profit.ProfitService;
import com.brightloop.subscription.profit.dto.ProfitGroupRow;
import com.brightloop.subscription.profit.dto.ProfitResponse;
import com.brightloop.subscription.profit.dto.ProfitSubscriptionRow;
import com.brightloop.subscription.profit.dto.ProfitSummary;
import com.brightloop.subscription.subscription.Subscription;
import com.brightloop.subscription.subscription.SubscriptionRepository;
import com.brightloop.subscription.subscription.SubscriptionStatus;

@Service
public class ExportService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ProfitService profitService;

    public ExportService(InvoiceRepository invoiceRepository, PaymentRepository paymentRepository,
            SubscriptionRepository subscriptionRepository, ProfitService profitService) {
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.profitService = profitService;
    }

    @Transactional(readOnly = true)
    public byte[] invoices(InvoiceStatus status, Long customerId, LocalDate from, LocalDate to, String currency) {
        String effectiveCurrency = currency == null ? null : CurrencyService.normalize(currency);
        List<Invoice> invoices = invoiceRepository.findForExport(
                status, customerId, from, to, effectiveCurrency);
        Map<Long, BigDecimal> paidByInvoice = paidByInvoice(
                status, customerId, from, to, effectiveCurrency);

        try (Workbook workbook = invoices.size() > 1000 ? new SXSSFWorkbook(100) : new XSSFWorkbook()) {
            CellStyle header = headerStyle(workbook);
            Sheet summary = workbook.createSheet("Summary");
            Sheet data = workbook.createSheet("Invoices");
            Sheet gst = workbook.createSheet("GST Report");

            BigDecimal subtotal = BigDecimal.ZERO;
            BigDecimal discount = BigDecimal.ZERO;
            BigDecimal cgst = BigDecimal.ZERO;
            BigDecimal sgst = BigDecimal.ZERO;
            BigDecimal igst = BigDecimal.ZERO;
            BigDecimal total = BigDecimal.ZERO;
            BigDecimal totalInBase = BigDecimal.ZERO;
            BigDecimal paidTotal = BigDecimal.ZERO;

            Row dataHeader = data.createRow(0);
            header(dataHeader, "Invoice", "Issue Date", "Due Date", "Customer", "Customer State", "GSTIN",
                    "Currency", "Exchange Rate", "Subtotal", "Discount", "CGST", "SGST", "IGST", "Total",
                    "Total INR", "Paid", "Outstanding", "Status");
            applyHeaderStyle(dataHeader, header);

            Row gstHeader = gst.createRow(0);
            header(gstHeader, "Invoice", "Date", "Customer", "Customer State", "GSTIN", "HSN/SAC",
                    "Taxable (INR)", "CGST (INR)", "SGST (INR)", "IGST (INR)", "Total (INR)", "Place of Supply");
            applyHeaderStyle(gstHeader, header);

            int dataRow = 1;
            int gstRow = 1;
            for (Invoice invoice : invoices) {
                BigDecimal paid = paidByInvoice.getOrDefault(invoice.getId(), BigDecimal.ZERO);
                BigDecimal outstanding = invoice.getTotal().subtract(paid).max(BigDecimal.ZERO);
                BigDecimal invoiceCgst = sum(invoice, "cgst");
                BigDecimal invoiceSgst = sum(invoice, "sgst");
                BigDecimal invoiceIgst = sum(invoice, "igst");

                Row row = data.createRow(dataRow++);
                row.createCell(0).setCellValue(invoice.getNumber());
                row.createCell(1).setCellValue(invoice.getIssueDate().toString());
                row.createCell(2).setCellValue(invoice.getDueDate().toString());
                row.createCell(3).setCellValue(invoice.getCustomer().getFullName());
                row.createCell(4).setCellValue(invoice.getCustomer().getState() == null
                        ? "" : invoice.getCustomer().getState().label());
                row.createCell(5).setCellValue(nullSafe(invoice.getCustomerGstin()));
                row.createCell(6).setCellValue(invoice.getCurrency());
                row.createCell(7).setCellValue(invoice.getExchangeRate().doubleValue());
                row.createCell(8).setCellValue(invoice.getSubtotal().doubleValue());
                row.createCell(9).setCellValue(invoice.getDiscountTotal().doubleValue());
                row.createCell(10).setCellValue(invoiceCgst.doubleValue());
                row.createCell(11).setCellValue(invoiceSgst.doubleValue());
                row.createCell(12).setCellValue(invoiceIgst.doubleValue());
                row.createCell(13).setCellValue(invoice.getTotal().doubleValue());
                row.createCell(14).setCellValue(invoice.getTotalInBase().doubleValue());
                row.createCell(15).setCellValue(paid.doubleValue());
                row.createCell(16).setCellValue(outstanding.doubleValue());
                row.createCell(17).setCellValue(invoice.getStatus().name());

                subtotal = subtotal.add(invoice.getSubtotal());
                discount = discount.add(invoice.getDiscountTotal());
                cgst = cgst.add(invoiceCgst);
                sgst = sgst.add(invoiceSgst);
                igst = igst.add(invoiceIgst);
                total = total.add(invoice.getTotal());
                totalInBase = totalInBase.add(invoice.getTotalInBase());
                paidTotal = paidTotal.add(paid);

                if (invoice.getStatus() != InvoiceStatus.CANCELLED) {
                    for (InvoiceLine line : invoice.getLines()) {
                        BigDecimal rate = invoice.getExchangeRate();
                        Row gstLine = gst.createRow(gstRow++);
                        gstLine.createCell(0).setCellValue(invoice.getNumber());
                        gstLine.createCell(1).setCellValue(invoice.getIssueDate().toString());
                        gstLine.createCell(2).setCellValue(invoice.getCustomer().getFullName());
                        gstLine.createCell(3).setCellValue(invoice.getCustomer().getState() == null
                                ? "" : invoice.getCustomer().getState().label());
                        gstLine.createCell(4).setCellValue(nullSafe(invoice.getCustomerGstin()));
                        gstLine.createCell(5).setCellValue(nullSafe(line.getHsnSacCode()));
                        gstLine.createCell(6).setCellValue(scale(line.getLineNet().multiply(rate)));
                        gstLine.createCell(7).setCellValue(scale(line.getCgstAmount().multiply(rate)));
                        gstLine.createCell(8).setCellValue(scale(line.getSgstAmount().multiply(rate)));
                        gstLine.createCell(9).setCellValue(scale(line.getIgstAmount().multiply(rate)));
                        gstLine.createCell(10).setCellValue(scale(line.getTotal().multiply(rate)));
                        gstLine.createCell(11).setCellValue(nullSafe(invoice.getPlaceOfSupply()));
                    }
                }
            }

            writeSummary(summary, "Invoices", invoices.size(), subtotal, discount, cgst, sgst, igst,
                    total, totalInBase, paidTotal);
            autoSize(data);
            autoSize(gst);
            return write(workbook);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to build invoice export", ex);
        }
    }

    @Transactional(readOnly = true)
    public byte[] payments(LocalDate from, LocalDate to, Long customerId, String currency) {
        String effectiveCurrency = currency == null ? null : CurrencyService.normalize(currency);
        List<Payment> payments = paymentRepository.findForExport(from, to, customerId, effectiveCurrency);

        try (Workbook workbook = payments.size() > 1000 ? new SXSSFWorkbook(100) : new XSSFWorkbook()) {
            CellStyle header = headerStyle(workbook);
            Sheet summary = workbook.createSheet("Summary");
            Sheet data = workbook.createSheet("Payments");

            Row headerRow = data.createRow(0);
            header(headerRow, "Payment ID", "Paid On", "Invoice", "Customer", "Method", "Amount",
                    "Currency", "Amount INR", "Note", "Gateway Ref");
            applyHeaderStyle(headerRow, header);

            BigDecimal totalBase = BigDecimal.ZERO;
            int rowIndex = 1;
            for (Payment payment : payments) {
                BigDecimal base = payment.getAmount().multiply(payment.getInvoice().getExchangeRate());
                Row row = data.createRow(rowIndex++);
                row.createCell(0).setCellValue(payment.getId());
                row.createCell(1).setCellValue(payment.getPaidOn().toString());
                row.createCell(2).setCellValue(payment.getInvoice().getNumber());
                row.createCell(3).setCellValue(payment.getInvoice().getCustomer().getFullName());
                row.createCell(4).setCellValue(payment.getMethod().name());
                row.createCell(5).setCellValue(payment.getAmount().doubleValue());
                row.createCell(6).setCellValue(payment.getInvoice().getCurrency());
                row.createCell(7).setCellValue(scale(base));
                row.createCell(8).setCellValue(nullSafe(payment.getNote()));
                row.createCell(9).setCellValue(nullSafe(payment.getGatewayRef()));
                totalBase = totalBase.add(base);
            }

            summary.createRow(0).createCell(0).setCellValue("Brightloop payment export");
            summary.createRow(2).createCell(0).setCellValue("Payments");
            summary.getRow(2).createCell(1).setCellValue(payments.size());
            summary.createRow(3).createCell(0).setCellValue("Total received (INR)");
            summary.getRow(3).createCell(1).setCellValue(scale(totalBase));
            autoSize(data);
            return write(workbook);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to build payment export", ex);
        }
    }

    @Transactional(readOnly = true)
    public byte[] subscriptions(SubscriptionStatus status, Long customerId, Long planId) {
        List<Subscription> subscriptions = subscriptionRepository.findForExport(status, customerId, planId);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            CellStyle header = headerStyle(workbook);
            Sheet summary = workbook.createSheet("Summary");
            Sheet data = workbook.createSheet("Subscriptions");

            Row headerRow = data.createRow(0);
            header(headerRow, "Subscription", "Customer", "Plan", "Tier", "Status", "Start Date", "Expiry Date",
                    "Next Billing", "Payment Terms (days)", "Currency", "Lines", "Total Amount");
            applyHeaderStyle(headerRow, header);

            Map<SubscriptionStatus, Long> counts = new HashMap<>();
            BigDecimal total = BigDecimal.ZERO;
            int rowIndex = 1;
            for (Subscription subscription : subscriptions) {
                BigDecimal amount = subscription.getLines().stream()
                        .map(line -> line.getAmount())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                Row row = data.createRow(rowIndex++);
                row.createCell(0).setCellValue(subscription.getNumber());
                row.createCell(1).setCellValue(subscription.getCustomer().getFullName());
                row.createCell(2).setCellValue(subscription.getPlan().getName());
                row.createCell(3).setCellValue(subscription.getPlan().getTier().name());
                row.createCell(4).setCellValue(subscription.getStatus().name());
                row.createCell(5).setCellValue(subscription.getStartDate().toString());
                row.createCell(6).setCellValue(subscription.getExpiryDate() == null
                        ? "" : subscription.getExpiryDate().toString());
                row.createCell(7).setCellValue(subscription.getNextBillingDate() == null
                        ? "" : subscription.getNextBillingDate().toString());
                row.createCell(8).setCellValue(subscription.getPaymentTermsDays());
                row.createCell(9).setCellValue(subscription.getCurrency());
                row.createCell(10).setCellValue(subscription.getLines().size());
                row.createCell(11).setCellValue(amount.doubleValue());
                counts.merge(subscription.getStatus(), 1L, Long::sum);
                total = total.add(amount);
            }

            summary.createRow(0).createCell(0).setCellValue("Brightloop subscription export");
            summary.createRow(2).createCell(0).setCellValue("Subscriptions");
            summary.getRow(2).createCell(1).setCellValue(subscriptions.size());
            summary.createRow(3).createCell(0).setCellValue("Total line amount");
            summary.getRow(3).createCell(1).setCellValue(total.doubleValue());
            int row = 5;
            for (Map.Entry<SubscriptionStatus, Long> entry : counts.entrySet()) {
                Row statusRow = summary.createRow(row++);
                statusRow.createCell(0).setCellValue(entry.getKey().name());
                statusRow.createCell(1).setCellValue(entry.getValue());
            }
            autoSize(data);
            return write(workbook);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to build subscription export", ex);
        }
    }

    @Transactional(readOnly = true)
    public byte[] profit(LocalDate from, LocalDate to, Long planId) {
        ProfitResponse report = profitService.report(from, to, planId);
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            CellStyle header = headerStyle(workbook);
            Sheet summary = workbook.createSheet("Summary");
            Sheet subscriptions = workbook.createSheet("Subscriptions");
            Sheet plans = workbook.createSheet("Plans");
            Sheet products = workbook.createSheet("Products");
            Sheet monthly = workbook.createSheet("Monthly");

            ProfitSummary totals = report.summary();
            summary.createRow(0).createCell(0).setCellValue("Brightloop profit report");
            summary.createRow(2).createCell(0).setCellValue("Revenue (ex-GST, INR)");
            summary.getRow(2).createCell(1).setCellValue(scale(totals.totalRevenueInBase()));
            summary.createRow(3).createCell(0).setCellValue("Cost (INR)");
            summary.getRow(3).createCell(1).setCellValue(scale(totals.totalCostInBase()));
            summary.createRow(4).createCell(0).setCellValue("Gross profit (INR)");
            summary.getRow(4).createCell(1).setCellValue(scale(totals.grossProfitInBase()));
            summary.createRow(5).createCell(0).setCellValue("Margin %");
            summary.getRow(5).createCell(1).setCellValue(totals.marginPercent().doubleValue());
            summary.createRow(6).createCell(0).setCellValue("MRR (INR)");
            summary.getRow(6).createCell(1).setCellValue(scale(totals.mrrInBase()));
            summary.createRow(7).createCell(0).setCellValue("Outstanding (INR)");
            summary.getRow(7).createCell(1).setCellValue(scale(totals.outstandingInBase()));
            summary.createRow(8).createCell(0).setCellValue("GST collected (INR)");
            summary.getRow(8).createCell(1).setCellValue(scale(totals.gstCollectedInBase()));
            summary.autoSizeColumn(0);
            summary.autoSizeColumn(1);

            Row subscriptionHeader = subscriptions.createRow(0);
            header(subscriptionHeader, "Subscription", "Customer", "Plan", "Status", "Revenue (INR)",
                    "Cost (INR)", "Profit (INR)", "Margin %");
            applyHeaderStyle(subscriptionHeader, header);
            int rowIndex = 1;
            for (ProfitSubscriptionRow row : report.subscriptions()) {
                Row sheetRow = subscriptions.createRow(rowIndex++);
                sheetRow.createCell(0).setCellValue(row.number());
                sheetRow.createCell(1).setCellValue(row.customerName());
                sheetRow.createCell(2).setCellValue(row.planName());
                sheetRow.createCell(3).setCellValue(row.status());
                sheetRow.createCell(4).setCellValue(scale(row.revenueInBase()));
                sheetRow.createCell(5).setCellValue(scale(row.costInBase()));
                sheetRow.createCell(6).setCellValue(scale(row.profitInBase()));
                sheetRow.createCell(7).setCellValue(row.marginPercent().doubleValue());
            }

            writeGroupSheet(plans, header, "Plan", report.plans());
            writeGroupSheet(products, header, "Product", report.products());
            writeGroupSheet(monthly, header, "Month", report.monthly());

            autoSize(subscriptions);
            autoSize(plans);
            autoSize(products);
            autoSize(monthly);
            return write(workbook);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to build profit export", ex);
        }
    }

    private void writeGroupSheet(Sheet sheet, CellStyle header, String label, List<ProfitGroupRow> rows) {
        Row headerRow = sheet.createRow(0);
        header(headerRow, label, "Revenue (INR)", "Cost (INR)", "Profit (INR)", "Margin %");
        applyHeaderStyle(headerRow, header);
        int rowIndex = 1;
        for (ProfitGroupRow row : rows) {
            Row sheetRow = sheet.createRow(rowIndex++);
            sheetRow.createCell(0).setCellValue(row.name());
            sheetRow.createCell(1).setCellValue(scale(row.revenueInBase()));
            sheetRow.createCell(2).setCellValue(scale(row.costInBase()));
            sheetRow.createCell(3).setCellValue(scale(row.profitInBase()));
            sheetRow.createCell(4).setCellValue(row.marginPercent().doubleValue());
        }
    }

    private Map<Long, BigDecimal> paidByInvoice(InvoiceStatus status, Long customerId, LocalDate from,
            LocalDate to, String currency) {
        Map<Long, BigDecimal> paid = new HashMap<>();
        for (Object[] row : paymentRepository.sumByInvoiceFilters(status, customerId, from, to, currency)) {
            paid.put((Long) row[0], (BigDecimal) row[1]);
        }
        return paid;
    }

    private BigDecimal sum(Invoice invoice, String type) {
        return invoice.getLines().stream()
                .map(line -> switch (type) {
                    case "cgst" -> line.getCgstAmount();
                    case "sgst" -> line.getSgstAmount();
                    default -> line.getIgstAmount();
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void writeSummary(Sheet summary, String label, int count, BigDecimal subtotal, BigDecimal discount,
            BigDecimal cgst, BigDecimal sgst, BigDecimal igst, BigDecimal total, BigDecimal totalInBase,
            BigDecimal paid) {
        summary.createRow(0).createCell(0).setCellValue("Brightloop invoice export");
        summary.createRow(2).createCell(0).setCellValue(label);
        summary.getRow(2).createCell(1).setCellValue(count);
        summary.createRow(3).createCell(0).setCellValue("Subtotal (invoice currency)");
        summary.getRow(3).createCell(1).setCellValue(scale(subtotal));
        summary.createRow(4).createCell(0).setCellValue("Discount (invoice currency)");
        summary.getRow(4).createCell(1).setCellValue(scale(discount));
        summary.createRow(5).createCell(0).setCellValue("CGST (invoice currency)");
        summary.getRow(5).createCell(1).setCellValue(scale(cgst));
        summary.createRow(6).createCell(0).setCellValue("SGST (invoice currency)");
        summary.getRow(6).createCell(1).setCellValue(scale(sgst));
        summary.createRow(7).createCell(0).setCellValue("IGST (invoice currency)");
        summary.getRow(7).createCell(1).setCellValue(scale(igst));
        summary.createRow(8).createCell(0).setCellValue("Total (invoice currency)");
        summary.getRow(8).createCell(1).setCellValue(scale(total));
        summary.createRow(9).createCell(0).setCellValue("Total (INR)");
        summary.getRow(9).createCell(1).setCellValue(scale(totalInBase));
        summary.createRow(10).createCell(0).setCellValue("Payments received (invoice currency)");
        summary.getRow(10).createCell(1).setCellValue(scale(paid));
        summary.createRow(11).createCell(0).setCellValue("Outstanding (invoice currency)");
        summary.getRow(11).createCell(1).setCellValue(scale(total.subtract(paid)));
        summary.setColumnWidth(0, 40 * 256);
        summary.setColumnWidth(1, 18 * 256);
    }

    private CellStyle headerStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private void header(Row row, String... names) {
        for (int i = 0; i < names.length; i++) {
            row.createCell(i).setCellValue(names[i]);
        }
    }

    private void applyHeaderStyle(Row row, CellStyle style) {
        row.forEach(cell -> cell.setCellStyle(style));
    }

    private void autoSize(Sheet sheet) {
        if (sheet.getRow(0) == null) {
            return;
        }
        int columns = sheet.getRow(0).getLastCellNum();
        if (sheet.getLastRowNum() > 1000) {
            for (int i = 0; i < columns; i++) {
                sheet.setColumnWidth(i, 18 * 256);
            }
            return;
        }
        for (int i = 0; i < columns; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private byte[] write(Workbook workbook) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private double scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
