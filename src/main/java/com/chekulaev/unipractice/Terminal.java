package com.chekulaev.unipractice;

import java.io.IOException;
import java.util.Scanner;
import static com.chekulaev.unipractice.Main.*;

public class Terminal {
    public static ConnectionSettings requestConnectionSettings() {
        ConnectionSettings cons = new ConnectionSettings();
        try (Scanner file_input = new Scanner(CONNECTION_SETTINGS_PATH)) {
            cons.url = file_input.nextLine();
            cons.user = file_input.nextLine();
            cons.password = file_input.nextLine();
            cons.tableName = file_input.nextLine();
        } catch (IOException e) {
            System.out.println("Error opening connection settings file!");
        } catch (Exception e) {
            System.out.println("Incorrect syntax in connection settings file!");
        }

        System.out.println("Current connection settings are: ");
        System.out.println("url = '" + cons.url + "'");
        System.out.println("user = '" + cons.user + "'");
        System.out.println("password = '" + cons.password + "'");
        System.out.println("table name = '" + cons.tableName + "'");
        System.out.println("(To change these settings edit 'connection_settings.txt')");
        System.out.print("Continue with these settings? y/n: " );
        if (terminalScanner.nextLine().charAt(0) != 'y') {System.exit(0); }
        System.out.println();

        return cons;
    }

    /**
     * Returns string for sql query part 'ORDER BY'
     * Changes contents of fieldsToAnalyze to represent fields for analysis
     */
    public static String requestFieldsToAnalyze(boolean[] fieldsToAnalyze) {
        StringBuilder fieldsToSort = new StringBuilder();

        System.out.println("Select fields for duplicate analysis (Enter the field numbers, separated by space" +
                " character: ");
        for (int i = 1; i < FIELDS.length -1 ; i++) {
            if (i == 5) {System.out.println(); }
            System.out.print(i + " - " + FIELDS[i] + ", ");
        }
        System.out.print(FIELDS.length-1 + " - " + FIELDS[FIELDS.length-1] + "): ");
        while (true) {
            String[] inputStrings = terminalScanner.nextLine().split(" ");
            try {
                for (String inputStr : inputStrings) {
                    fieldsToAnalyze[Integer.parseInt(inputStr)] = true;
                }
            } catch (Exception e) {
                System.out.print("Wrong input! Try again: ");
                continue;
            }
            break;
        }

        System.out.println("These fields will be analyzed: ");
        int i = 0;
        while (!fieldsToAnalyze[i]) {i++; }
        fieldsToSort.append(FIELDS[i]);
        i++;
        for (; i < FIELDS.length; i++) {
            if (fieldsToAnalyze[i]) {
                fieldsToSort.append(", ");
                fieldsToSort.append(FIELDS[i]);
            }
        }

        System.out.println(fieldsToSort.toString());
        System.out.println();

        return fieldsToSort.toString();
    }
}
