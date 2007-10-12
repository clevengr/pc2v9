package edu.csus.ecs.pc2.core.report;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.Vector;

import edu.csus.ecs.pc2.VersionInfo;
import edu.csus.ecs.pc2.core.IController;
import edu.csus.ecs.pc2.core.list.AccountComparator;
import edu.csus.ecs.pc2.core.list.SiteComparatorBySiteNumber;
import edu.csus.ecs.pc2.core.log.Log;
import edu.csus.ecs.pc2.core.model.Account;
import edu.csus.ecs.pc2.core.model.ClientId;
import edu.csus.ecs.pc2.core.model.ClientType;
import edu.csus.ecs.pc2.core.model.ElementId;
import edu.csus.ecs.pc2.core.model.Filter;
import edu.csus.ecs.pc2.core.model.Group;
import edu.csus.ecs.pc2.core.model.IContest;
import edu.csus.ecs.pc2.core.model.Site;
import edu.csus.ecs.pc2.core.model.ClientType.Type;
import edu.csus.ecs.pc2.core.security.Permission;

/**
 * Print all account info.
 * 
 * @author pc2@ecs.csus.edu
 * @version $Id$
 */

// $HeadURL$

public class AccountsReport implements IReport {

    /**
     * 
     */
    private static final long serialVersionUID = -8628612595677727400L;

    private IContest contest;

    private IController controller;

    private Log log;
    
    private Filter accountFilter = new Filter();

    private void writeSummaryRow(PrintWriter printWriter, int site) {

        int total = 0;
        
        ClientType.Type[] types = ClientType.Type.values();
        for (ClientType.Type type : types) {
            Vector<Account> vector = contest.getAccounts(type, site);
            total += vector.size();
            String countString = vector.size() + "";
            if (vector.size() == 0) {
                countString = "-";
            }
            printWriter.format("%7s", countString);
        }
        printWriter.format("%7s", total);
        printWriter.println();
    }
    
    private void printAccountSummaryBySite (PrintWriter printWriter){

        Site[] sites = contest.getSites();
        Arrays.sort(sites, new SiteComparatorBySiteNumber());

        // print header for summary
        printWriter.println();
        ClientType.Type[] types = ClientType.Type.values();
        printWriter.print("Site # ");
        for (ClientType.Type type : types) {
            int truncIndex = Math.min(type.toString().length(), 5);
            printWriter.format("%7s", type.toString().substring(0, truncIndex).toLowerCase());
        }
        printWriter.format("%7s", "total");
        printWriter.println();

        for (Site site : sites) {
            printWriter.print("Site " + site.getSiteNumber());
            writeSummaryRow(printWriter, site.getSiteNumber());
        }
    }

    private void writeReport(PrintWriter printWriter) {
        printAccountSummaryBySite (printWriter);
        printAccountsByGroup (printWriter);
    }

    private void printAccountsByGroup(PrintWriter printWriter) {

        printWriter.println();
        printWriter.println("-- Accounts --");
        for (ClientType.Type ctype : ClientType.Type.values()) {

            Vector<Account> accounts;

            if (accountFilter.isThisSiteOnly()) {
                accounts = contest.getAccounts(ctype, accountFilter.getSiteNumber());
            } else {
                accounts = contest.getAccounts(ctype);
            }

            if (accounts.size() > 0) {
                printWriter.println();
                printWriter.print("Accounts " + ctype.toString() + " there are " + accounts.size());
                if (accountFilter.isThisSiteOnly()) {
                    printWriter.print(" for site " + accountFilter.getSiteNumber());
                }
                printWriter.println();

                Account [] sortedAccounts = (Account[]) accounts.toArray(new Account[accounts.size()]);
                
                Arrays.sort(sortedAccounts, new AccountComparator());

                for (Account account : sortedAccounts){
                    printWriter.print("   Site " + account.getSiteNumber());
                    printWriter.format(" %-15s", account.getClientId().getName());
                    printWriter.println(" id=" + account.getElementId());
                    
                    printWriter.format("%22s"," ");
                    printWriter.print("'"+account.getDisplayName()+"' ");
                    ClientId clientId = contest.getClientId();
                    
                    if (clientId.getClientType().equals(Type.ADMINISTRATOR) || clientId.getClientType().equals(Type.SERVER)) {
                        printWriter.print("password '" + account.getPassword() + "' ");
                    }
                    
                    Permission.Type type = Permission.Type.LOGIN;
                    if (account.isAllowed(type)){
                        printWriter.print(type+" ");
                    }
                    type = Permission.Type.DISPLAY_ON_SCOREBOARD;
                    if (account.isAllowed(type)){
                        printWriter.print(type+" ");
                    }
                    printWriter.println();

                    printWriter.format("%22s"," ");
                    printWriter.print("alias '" + account.getAliasName()+"' ");
                    ElementId groupId = account.getGroupId();
                    if (groupId != null) {
                        Group group = contest.getGroup(groupId);
                        if (group != null) {
                            printWriter.print("group '"+group+"' ("+groupId+")");
                        } else {
                            printWriter.print("group invalid ("+groupId+")");
                        }
                    } else {
                        printWriter.print("group ''");
                    }
                    printWriter.println();
                }
            }
        }
    }

    private void printHeader(PrintWriter printWriter) {
        printWriter.println(new VersionInfo().getSystemName());
        printWriter.println("Date: " + new Date());
        printWriter.println(new VersionInfo().getSystemVersionInfo());
    }

    private void printFooter(PrintWriter printWriter) {
        printWriter.println();
        printWriter.println("end report");
    }

    public void createReportFile(String filename, Filter filter) throws IOException {

        PrintWriter printWriter = new PrintWriter(new FileOutputStream(filename, false), true);

        try {
            printHeader(printWriter);

            try {
                writeReport(printWriter);
            } catch (Exception e) {
                printWriter.println("Exception in report: " + e.getMessage());
                e.printStackTrace(printWriter);
            }

            printFooter(printWriter);

            printWriter.close();
            printWriter = null;

        } catch (Exception e) {
            log.log(Log.INFO, "Exception writing report", e);
            printWriter.println("Exception generating report " + e.getMessage());
        }
    }

    public String[] createReport(Filter filter) {
        throw new SecurityException("Not implemented");
    }

    public String createReportXML(Filter filter) {
        throw new SecurityException("Not implemented");
    }

    public String getReportTitle() {
        return "Accounts";
    }

    public void setContestAndController(IContest inContest, IController inController) {
        this.contest = inContest;
        this.controller = inController;
        log = controller.getLog();
    }

    public String getPluginTitle() {
        return "Accounts Report";
    }

}
