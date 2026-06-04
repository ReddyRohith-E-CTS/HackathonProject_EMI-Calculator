package com.hackathon.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public final class ExcelUtils {

    private ExcelUtils() {
    }

    // Writes a 2D string grid to an .xlsx file (row 0 is the bold header row).
    // synchronized: Chrome and Edge run TC04/TC06 in parallel and both write to the
    // same output file; the lock ensures only one thread writes at a time.
    public static synchronized void writeSheet(String filePath, String sheetName, List<List<String>> data) {
        if (data == null || data.isEmpty())
            throw new IllegalArgumentException("No data to write to " + filePath);
        File f = new File(filePath);
        File parent = f.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs())
            throw new IllegalStateException("Cannot create dir " + parent);

        try (XSSFWorkbook wb = new XSSFWorkbook();
                FileOutputStream out = new FileOutputStream(f)) {

            XSSFSheet sheet = wb.createSheet(sheetName);
            XSSFCellStyle headerStyle = wb.createCellStyle();
            XSSFFont bold = wb.createFont();
            bold.setBold(true);
            headerStyle.setFont(bold);

            for (int r = 0; r < data.size(); r++) {
                Row row = sheet.createRow(r);
                List<String> rowData = data.get(r);
                for (int c = 0; c < rowData.size(); c++) {
                    Cell cell = row.createCell(c);
                    cell.setCellValue(rowData.get(c));
                    if (r == 0)
                        cell.setCellStyle(headerStyle);
                }
            }
            for (int c = 0; c < data.get(0).size(); c++)
                sheet.autoSizeColumn(c);
            wb.write(out);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write Excel " + filePath, e);
        }
    }

    // Returns the number of populated rows in the given sheet (header + data).
    public static int rowCount(String filePath, String sheetName) {
        try (XSSFWorkbook wb = new XSSFWorkbook(filePath)) {
            Sheet sh = wb.getSheet(sheetName);
            return sh == null ? 0 : (sh.getLastRowNum() + 1);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel " + filePath, e);
        }
    }
}
