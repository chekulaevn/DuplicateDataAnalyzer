package com.chekulaev.unipractice;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.sql.*;

public class Main {

    public static final String[] FIELDS = {"id", "Фамилия", "Имя", "Отчество", "Возраст", "Адрес", "Паспорт", "ИНН"};
    public static final Path CONNECTION_SETTINGS_PATH = Paths.get("connection_settings.txt");
    public static final Scanner terminalScanner = new Scanner(System.in);

    private JdbcTemplate jdbcTemplate;

    public Main(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Checks if table rows are equal
     * Equivalent arrays length guarantied
     */
    public static boolean rowsEqual(String[] row1, String[] row2, boolean[] fieldsToAnalyze) {
        for (int i = 0; i < FIELDS.length; i++) {
            if (fieldsToAnalyze[i]) {
                if (!row1[i].equals(row2[i])) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext("spring.xml");
        Main main = ctx.getBean("main", Main.class);
        ctx.close();

        char flag = 'y';

        while (flag == 'y') {

            System.out.println("--------------------------------------------------");
            ConnectionSettings cons = Terminal.requestConnectionSettings();

            try (Connection con = DriverManager.getConnection(cons.url + "?useUnicode=yes&characterEncoding=UTF-8",
                    cons.user, cons.password);
                 Statement stat = con.createStatement()) {
                System.out.println();
                System.out.println("Database opened successfully");

                DatabaseMetaData dbm = con.getMetaData();
                ResultSet tables = dbm.getTables(null, null, cons.tableName, null);
                if (tables.next()) {
                    boolean[] fieldsToAnalyze = new boolean[FIELDS.length];
                    String fieldsToSort = Terminal.requestFieldsToAnalyze(fieldsToAnalyze);

                    long startTime = System.currentTimeMillis();

                    ResultSet rs = stat.executeQuery("SELECT * FROM " + cons.tableName + " ORDER BY " + fieldsToSort);
                    String[] row = new String[FIELDS.length];
                    String[] row2 = new String[FIELDS.length];
                    boolean endFlag, firstDuplicatePrinted = false;
                    int i, duplicateCount = 0, rowsCount = 0;
                    if (rs.next()) {
                        for (i = 0; i < FIELDS.length; i++) {
                            row[i] = rs.getString(FIELDS[i]);
                        }
                        rowsCount++;
                    }
                    while (true) {
                        endFlag = true;
                        while (rs.next()) {
                            endFlag = false;
                            rowsCount++;
                            for (i = 0; i < FIELDS.length; i++) {
                                row2[i] = rs.getString(FIELDS[i]);
                            }
                            if (!rowsEqual(row,row2,fieldsToAnalyze)) {
                                break;
                            } else {
                                if (!firstDuplicatePrinted) {
                                    for (i = 0; i < FIELDS.length; i++) {
                                        System.out.print(" - " + row[i]);
                                    }
                                    System.out.println();
                                    firstDuplicatePrinted = true;
                                }
                                for (i = 0; i < FIELDS.length; i++) {
                                    System.out.print(" - " + row2[i]);
                                }
                                System.out.println();
                                duplicateCount++;
                                endFlag = true;
                            }
                        }
                        if (endFlag) {
                            break;
                        } else {
                            row = row2.clone();
                            firstDuplicatePrinted = false;
                        }

                    }

                    System.out.println();
                    System.out.println("Records processed: " + rowsCount);
                    System.out.printf("Duplicates found: %d (%.2f%%)\n", duplicateCount,
                            (float)duplicateCount / rowsCount * 100);
                    System.out.println("Time spent: " + (System.currentTimeMillis() - startTime)/1000.0 + " seconds");

                } else {
                    System.out.println("Table '" + cons.tableName + "' doesn't exist in the database!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.print("Repeat? y/n: ");
            flag = terminalScanner.nextLine().charAt(0);
            System.out.println();
        }

        terminalScanner.close();
    }
}