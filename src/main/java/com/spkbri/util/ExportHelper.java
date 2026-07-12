package com.spkbri.util;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.spkbri.model.RankingResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

public class ExportHelper {

    public static void exportToCSV(List<RankingResult> results, String divisi, File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            // Write BOM for Excel UTF-8 compatibility
            writer.write('\ufeff');
            writer.write("Rank,NIK,Nama,Divisi,Score (Yi)\n");
            DecimalFormat df = new DecimalFormat("0.0000");
            for (RankingResult r : results) {
                writer.write(r.getRank() + ","
                        + escapeCSV(r.getKaryawan().getNik()) + ","
                        + escapeCSV(r.getKaryawan().getNama()) + ","
                        + escapeCSV(r.getKaryawan().getDivisi()) + ","
                        + df.format(r.getScore()) + "\n");
            }
        }
    }

    private static String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    public static void exportToPDF(List<RankingResult> results, String divisi, File file) throws Exception {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
        Font subtitleFont = new Font(Font.HELVETICA, 12, Font.NORMAL);
        Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD);
        Font bodyFont = new Font(Font.HELVETICA, 10, Font.NORMAL);

        Paragraph title = new Paragraph("LAPORAN PERANKINGAN KARYAWAN TERBAIK", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph subtitle = new Paragraph("Bank BRI KCP Arundina - Divisi " + divisi, subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        document.add(subtitle);
        document.add(new Paragraph("\n"));

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{10, 20, 40, 15, 15});

        // Add headers
        String[] headers = {"Rank", "NIK", "Nama Karyawan", "Divisi", "Score (Yi)"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Paragraph(header, headerFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(6);
            table.addCell(cell);
        }

        // Add data
        DecimalFormat df = new DecimalFormat("0.0000");
        for (RankingResult r : results) {
            table.addCell(createCell(String.valueOf(r.getRank()), Element.ALIGN_CENTER, bodyFont));
            table.addCell(createCell(r.getKaryawan().getNik(), Element.ALIGN_CENTER, bodyFont));
            table.addCell(createCell(r.getKaryawan().getNama(), Element.ALIGN_LEFT, bodyFont));
            table.addCell(createCell(r.getKaryawan().getDivisi(), Element.ALIGN_CENTER, bodyFont));
            table.addCell(createCell(df.format(r.getScore()), Element.ALIGN_RIGHT, bodyFont));
        }

        document.add(table);
        document.close();
    }

    private static PdfPCell createCell(String text, int alignment, Font font) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(5);
        return cell;
    }
}
