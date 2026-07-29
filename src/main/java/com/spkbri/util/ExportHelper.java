package com.spkbri.util;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.spkbri.model.Karyawan;
import com.spkbri.model.Kriteria;
import com.spkbri.model.MooraCalculationResult;
import com.spkbri.model.RankingResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

public class ExportHelper {

    public static void exportDataKaryawanPDF(List<Karyawan> list, String divisi, File file) throws Exception {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();
        addHeader(document, "LAPORAN DATA KARYAWAN", divisi);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{10, 20, 40, 30});

        String[] headers = {"No", "Kode Karyawan", "Nama Karyawan", "Divisi"};
        Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD);
        Font bodyFont = new Font(Font.HELVETICA, 10, Font.NORMAL);

        for (String h : headers) {
            table.addCell(createCell(h, Element.ALIGN_CENTER, headerFont));
        }

        int no = 1;
        for (Karyawan k : list) {
            table.addCell(createCell(String.valueOf(no++), Element.ALIGN_CENTER, bodyFont));
            table.addCell(createCell(k.getKodeKaryawan(), Element.ALIGN_CENTER, bodyFont));
            table.addCell(createCell(k.getNama(), Element.ALIGN_LEFT, bodyFont));
            table.addCell(createCell(k.getDivisi(), Element.ALIGN_CENTER, bodyFont));
        }

        document.add(table);
        addSignature(document, divisi);
        document.close();
    }

    public static void exportPerhitunganPDF(MooraCalculationResult result, String divisi, File file) throws Exception {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();
        addHeader(document, "LAPORAN PERHITUNGAN MOORA", divisi);

        Font subtitleFont = new Font(Font.HELVETICA, 11, Font.BOLD);
        Font headerFont = new Font(Font.HELVETICA, 9, Font.BOLD);
        Font bodyFont = new Font(Font.HELVETICA, 9, Font.NORMAL);
        DecimalFormat df = new DecimalFormat("0.0000");

        List<Karyawan> kList = result.getKaryawanList();
        List<Kriteria> kritList = result.getKriteriaList();

        int cols = kritList.size() + 1;
        float[] widths = new float[cols];
        widths[0] = 30;
        for (int i = 1; i < cols; i++) widths[i] = 70.0f / kritList.size();

        document.add(new Paragraph("1. Matriks Keputusan", subtitleFont));
        document.add(new Paragraph("\n"));
        PdfPTable t1 = new PdfPTable(cols);
        t1.setWidthPercentage(100);
        t1.setWidths(widths);
        t1.addCell(createCell("Karyawan", Element.ALIGN_CENTER, headerFont));
        for (Kriteria kr : kritList) {
            t1.addCell(createCell(kr.getKodeKriteria(), Element.ALIGN_CENTER, headerFont));
        }
        for (Karyawan k : kList) {
            t1.addCell(createCell(k.getNama(), Element.ALIGN_LEFT, bodyFont));
            Map<Integer, Double> vals = result.getMatriksKeputusan().get(k.getIdKaryawan());
            for (Kriteria kr : kritList) {
                double val = vals != null ? vals.getOrDefault(kr.getIdKriteria(), 0.0) : 0.0;
                t1.addCell(createCell(df.format(val), Element.ALIGN_CENTER, bodyFont));
            }
        }
        document.add(t1);
        document.add(new Paragraph("\n"));

        document.add(new Paragraph("2. Matriks Normalisasi", subtitleFont));
        document.add(new Paragraph("\n"));
        PdfPTable t2 = new PdfPTable(cols);
        t2.setWidthPercentage(100);
        t2.setWidths(widths);
        t2.addCell(createCell("Karyawan", Element.ALIGN_CENTER, headerFont));
        for (Kriteria kr : kritList) {
            t2.addCell(createCell(kr.getKodeKriteria(), Element.ALIGN_CENTER, headerFont));
        }
        for (Karyawan k : kList) {
            t2.addCell(createCell(k.getNama(), Element.ALIGN_LEFT, bodyFont));
            Map<Integer, Double> vals = result.getMatriksNormalisasi().get(k.getIdKaryawan());
            for (Kriteria kr : kritList) {
                double val = vals != null ? vals.getOrDefault(kr.getIdKriteria(), 0.0) : 0.0;
                t2.addCell(createCell(df.format(val), Element.ALIGN_CENTER, bodyFont));
            }
        }
        document.add(t2);
        document.add(new Paragraph("\n"));

        document.add(new Paragraph("3. Matriks Normalisasi Terbobot", subtitleFont));
        document.add(new Paragraph("\n"));
        PdfPTable t3 = new PdfPTable(cols);
        t3.setWidthPercentage(100);
        t3.setWidths(widths);
        t3.addCell(createCell("Karyawan", Element.ALIGN_CENTER, headerFont));
        for (Kriteria kr : kritList) {
            t3.addCell(createCell(kr.getKodeKriteria(), Element.ALIGN_CENTER, headerFont));
        }
        for (Karyawan k : kList) {
            t3.addCell(createCell(k.getNama(), Element.ALIGN_LEFT, bodyFont));
            Map<Integer, Double> vals = result.getMatriksNormalisasiTerbobot().get(k.getIdKaryawan());
            for (Kriteria kr : kritList) {
                double val = vals != null ? vals.getOrDefault(kr.getIdKriteria(), 0.0) : 0.0;
                t3.addCell(createCell(df.format(val), Element.ALIGN_CENTER, bodyFont));
            }
        }
        document.add(t3);

        addSignature(document, divisi);
        document.close();
    }

    public static void exportRankingPDF(List<RankingResult> results, String divisi, File file) throws Exception {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();
        addHeader(document, "LAPORAN HASIL RANKING MOORA", divisi);

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{10, 20, 40, 15, 15});

        Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD);
        Font bodyFont = new Font(Font.HELVETICA, 10, Font.NORMAL);

        String[] headers = {"Rank", "Kode Karyawan", "Nama Karyawan", "Divisi", "Score (Yi)"};
        for (String header : headers) {
            table.addCell(createCell(header, Element.ALIGN_CENTER, headerFont));
        }

        DecimalFormat df = new DecimalFormat("0.0000");
        for (RankingResult r : results) {
            table.addCell(createCell(String.valueOf(r.getRank()), Element.ALIGN_CENTER, bodyFont));
            table.addCell(createCell(r.getKaryawan().getKodeKaryawan(), Element.ALIGN_CENTER, bodyFont));
            table.addCell(createCell(r.getKaryawan().getNama(), Element.ALIGN_LEFT, bodyFont));
            table.addCell(createCell(r.getKaryawan().getDivisi(), Element.ALIGN_CENTER, bodyFont));
            table.addCell(createCell(df.format(r.getScore()), Element.ALIGN_RIGHT, bodyFont));
        }

        document.add(table);
        addSignature(document, divisi);
        document.close();
    }

    public static void exportToCSV(List<RankingResult> results, String divisi, File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {

            writer.write('\ufeff');
            writer.write("Rank,Kode Karyawan,Nama,Divisi,Score (Yi)\n");
            DecimalFormat df = new DecimalFormat("0.0000");
            for (RankingResult r : results) {
                writer.write(r.getRank() + ","
                        + escapeCSV(r.getKaryawan().getKodeKaryawan()) + ","
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

    private static void addHeader(Document document, String titleStr, String divisi) throws Exception {
        PdfPTable headerTable = new PdfPTable(new float[]{1.5f, 5.5f});
        headerTable.setWidthPercentage(100);

        try {
            com.lowagie.text.Image logo = com.lowagie.text.Image.getInstance("logo-kop-surat.png");
            logo.scaleToFit(70, 70);
            PdfPCell logoCell = new PdfPCell(logo);
            logoCell.setBorder(0);
            logoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            headerTable.addCell(logoCell);
        } catch (Exception e) {
            PdfPCell empty = new PdfPCell();
            empty.setBorder(0);
            headerTable.addCell(empty);
        }

        PdfPCell textCell = new PdfPCell();
        textCell.setBorder(0);
        textCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        textCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        
        Font font1 = new Font(Font.TIMES_ROMAN, 16, Font.BOLD);
        Font font2 = new Font(Font.TIMES_ROMAN, 14, Font.BOLD);
        Font font3 = new Font(Font.TIMES_ROMAN, 10, Font.NORMAL);
        
        Paragraph p1 = new Paragraph("PT. BANK RAKYAT INDONESIA (Persero) Tbk.", font1);
        p1.setAlignment(Element.ALIGN_CENTER);
        textCell.addElement(p1);
        
        Paragraph p2 = new Paragraph("KANTOR CABANG PEMBANTU ARUNDINA (i.1170)", font2);
        p2.setAlignment(Element.ALIGN_CENTER);
        textCell.addElement(p2);
        
        Paragraph p3 = new Paragraph("Jl. Lapangan Tembak No. 14H Kelapa Dua Wetan , Ciracas ,Jakarta-Timur", font3);
        p3.setAlignment(Element.ALIGN_CENTER);
        textCell.addElement(p3);
        
        Paragraph p4 = new Paragraph("Telepon: (021) 87710767, 87710642 * Facs: (021) 8721694", font3);
        p4.setAlignment(Element.ALIGN_CENTER);
        textCell.addElement(p4);
        
        headerTable.addCell(textCell);
        document.add(headerTable);

        PdfPTable lineTable = new PdfPTable(1);
        lineTable.setWidthPercentage(100);
        PdfPCell lineCell = new PdfPCell(new Paragraph(""));
        lineCell.setBorder(0);
        lineCell.setBorderWidthBottom(2f);
        lineCell.setPaddingBottom(5f);
        lineTable.addCell(lineCell);
        document.add(lineTable);
        
        document.add(new Paragraph("\n"));

        Font titleFont = new Font(Font.HELVETICA, 14, Font.BOLD);
        Font subtitleFont = new Font(Font.HELVETICA, 11, Font.NORMAL);
        Paragraph title = new Paragraph(titleStr, titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        
        if (divisi != null && !divisi.isEmpty()) {
            Paragraph subtitle = new Paragraph("Divisi " + divisi, subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subtitle);
        }
        document.add(new Paragraph("\n"));
    }

    private static PdfPCell createCell(String text, int alignment, Font font) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(5);
        return cell;
    }

    private static String getPimpinanName(String divisi) {
        String pimpinanName = "NAMA PIMPINAN";
        String usernameTarget = (divisi != null && divisi.equalsIgnoreCase("Bisnis")) ? "pimpinan_bisnis" : "pimpinan_ops";
        
        String sql = "SELECT nama_lengkap FROM users WHERE username = ? AND role = 'pimpinan'";
        try (java.sql.Connection conn = com.spkbri.database.DatabaseHelper.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, usernameTarget);
            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    pimpinanName = rs.getString("nama_lengkap");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pimpinanName;
    }

    private static void addSignature(Document document, String divisi) throws Exception {
        document.add(new Paragraph("\n\n")); // Space before signature

        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(40);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);

        Font normalFont = new Font(Font.HELVETICA, 10, Font.NORMAL);
        Font boldUnderlineFont = new Font(Font.HELVETICA, 10, Font.BOLD | Font.UNDERLINE);
        Font boldFont = new Font(Font.HELVETICA, 10, Font.BOLD);

        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy", new java.util.Locale("id", "ID"));
        String dateStr = java.time.LocalDate.now().format(formatter);
        
        PdfPCell cellDate = new PdfPCell(new Paragraph("Jakarta, " + dateStr, normalFont));
        cellDate.setBorder(0);
        cellDate.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cellDate);

        PdfPCell cellTitle = new PdfPCell(new Paragraph("Pimpinan Cabang", boldFont));
        cellTitle.setBorder(0);
        cellTitle.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cellTitle);

        PdfPCell cellSpace = new PdfPCell(new Paragraph("\n\n\n\n", normalFont));
        cellSpace.setBorder(0);
        table.addCell(cellSpace);

        String namaPimpinan = getPimpinanName(divisi);
        PdfPCell cellName = new PdfPCell(new Paragraph(namaPimpinan, boldUnderlineFont));
        cellName.setBorder(0);
        cellName.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cellName);

        document.add(table);
    }
}
