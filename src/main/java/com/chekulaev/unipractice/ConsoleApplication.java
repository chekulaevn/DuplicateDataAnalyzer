package com.chekulaev.unipractice;

import com.chekulaev.unipractice.utils.FinderReport;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.*;

public class ConsoleApplication {
    private static final Scanner terminalScanner = new Scanner(System.in);

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext("spring.xml");
        DuplicateFinder finder = ctx.getBean("duplicateFinder", DuplicateFinder.class);
        ctx.close();

        char flag = 'y';
        while (flag == 'y') {
            System.out.println("----------------------------------------");
            System.out.print("Enter table's name: ");
            String tableName = terminalScanner.nextLine();
            System.out.print("Select fields for duplicate analysis (Enter the field names, separated by space" +
                    " character): ");
            String[] inputStrings = terminalScanner.nextLine().split(" ");
            try {
                FinderReport report = finder.find(tableName, inputStrings, 4);
                System.out.println("//---//---//---//---//---//");
                System.out.println("   Time spent: " + report.getTimeSpent() + " seconds");
                System.out.println("   Records processed: " + report.getProcessedRecordsNumber());
                System.out.println("   Duplicates found: " + report.getDuplicateCount());
                System.out.println("//---//---//---//---//---//");
            } catch (Exception e) {
                //e.printStackTrace();
                System.out.println(e.getMessage());
            }
            System.out.print("Repeat? (y/n): ");
            flag = terminalScanner.nextLine().charAt(0);
        }
        terminalScanner.close();
    }
}