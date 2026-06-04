package com.hackathon.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Reads test input rows from src/test/resources/testdata/TestData.xlsx.
// Sheets are cached in memory after the first load; lookup is by sheet name + TC ID column.
public final class TestDataReader {

    private static final String RESOURCE = "testdata/TestData.xlsx";
    private static final Map<String, Map<String, String>> CACHE = new ConcurrentHashMap<>();

    private TestDataReader() {
    }

    // Loads (and caches) every row of the given sheet keyed by TestCaseID; returns the row for tcId.
    public static Map<String, String> get(String sheetName, String tcId) {
        Map<String, String> rowMap = CACHE.get(key(sheetName, tcId));
        if (rowMap == null) {
            loadSheet(sheetName);
            rowMap = CACHE.get(key(sheetName, tcId));
        }
        if (rowMap == null) {
            throw new IllegalArgumentException(
                    "No row for TestCaseID '" + tcId + "' in sheet '" + sheetName + "'");
        }
        return rowMap;
    }

    // Pulls a string column value from the row for the given TC id and column.
    public static String getString(String sheetName, String tcId, String column) {
        return get(sheetName, tcId).getOrDefault(column, "");
    }

    // Pulls an int column value from the row for the given TC id and column.
    public static int getInt(String sheetName, String tcId, String column) {
        return Integer.parseInt(getString(sheetName, tcId, column).trim());
    }

    // Loads every data row of a sheet into the cache (key = sheet|tcId).
    private static void loadSheet(String sheetName) {
        try (InputStream is = TestDataReader.class.getClassLoader().getResourceAsStream(RESOURCE);
                Workbook wb = new XSSFWorkbook(is)) {
            Sheet sh = wb.getSheet(sheetName);
            if (sh == null)
                throw new IllegalStateException("Sheet not found: " + sheetName);
            Row header = sh.getRow(0);
            DataFormatter fmt = new DataFormatter();
            for (int r = 1; r <= sh.getLastRowNum(); r++) {
                Row row = sh.getRow(r);
                if (row == null)
                    continue;
                Map<String, String> rowMap = new LinkedHashMap<>();
                String tcId = null;
                for (int c = 0; c < header.getLastCellNum(); c++) {
                    Cell hCell = header.getCell(c);
                    Cell vCell = row.getCell(c);
                    if (hCell == null)
                        continue;
                    String col = fmt.formatCellValue(hCell).trim();
                    String val = vCell == null ? "" : fmt.formatCellValue(vCell).trim();
                    rowMap.put(col, val);
                    if ("TestCaseID".equalsIgnoreCase(col))
                        tcId = val;
                }
                if (tcId != null && !tcId.isEmpty())
                    CACHE.put(key(sheetName, tcId), rowMap);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load " + RESOURCE + " sheet=" + sheetName, e);
        }
    }

    // Builds the composite cache key from sheet name and TestCaseID.
    private static String key(String sheetName, String tcId) {
        return sheetName + "|" + tcId;
    }
}
