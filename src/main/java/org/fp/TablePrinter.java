package org.fp;
import java.util.List;


/**
 * A class for printing data in a table format.
 */
public class TablePrinter {

    /**
     * Prints a dynamic table with a title and a list of rows.
     *
     * @param tableTitle the title of the table
     * @param rows       a list of rows, where each row is a list of strings representing cell values
     */
    public static void printDynamicTable(String tableTitle, List<List<String>> rows) {
        if (rows == null || rows.isEmpty()) {
            System.out.println("No data to display.");
            return;
        }
        System.out.println("===================================================");
        System.out.println("           🎉 " + tableTitle + " 🎉              ");
        System.out.println("===================================================");
        int colCount = rows.get(0).size();
        int[] cw = new int[colCount];
        for (List<String> row : rows) {
            if (row.size() == 1 && "###SEPARATOR###".equals(row.get(0))) {
                continue;
            }
            for (int c = 0; c < colCount; c++) {
                String cell;
                if (row.get(c) == null) {
                    cell = "";
                } else {
                    cell = row.get(c);
                }
                cw[c] = Math.max(cw[c], cell.length());
            }
        }
        String separator = buildSeparatorLine(cw);
        System.out.println(separator);
        printRow(rows.get(0), cw);
        System.out.println(separator);
        for (int r = 1; r < rows.size(); r++) {
            List<String> row = rows.get(r);
//            add a SEPARATOR for each column and set the row
            if (row.size() == 1 && "###SEPARATOR###".equals(row.get(0))) {
                System.out.println(separator);
            } else {
                printRow(row, cw);
            }
        }
        System.out.println(separator);
    }

    private static String buildSeparatorLine(int[] cw) {
        StringBuilder sb = new StringBuilder();
        for (int width : cw) {
            sb.append("+-");
            for (int i = 0; i < width; i++) {
                sb.append("-");
            }
            sb.append("-");
        }
        sb.append("+");
        return sb.toString();
    }

    /**
     * Prints a formatted row where each cell is padded to match its corresponding column width.
     *
     * @param row the list of cell values for the row
     * @param cw an array containing the width of each column
     */
    private static void printRow(List<String> row, int[] cw) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i<cw.length; i++) {
            String cell = row.get(i);
            if (cell == null) {
                cell = "";
            }
            // a separator, a space, and the cell value padded to the required width.
            sb.append("| ").append(padRight(cell, cw[i])).append(" ");
        }
        sb.append("|");
        System.out.println(sb.toString());
    }

//. helper method, put the text on the right
    private static String padRight(String text,int width) {
        if (text.length()>=width) {
            return text;
        }
        StringBuilder sb = new StringBuilder(text);
        while (sb.length()<width) {
            sb.append(" ");
        }
        return sb.toString();
    }
}
