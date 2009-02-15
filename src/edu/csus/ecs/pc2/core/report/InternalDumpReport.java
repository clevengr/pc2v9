package edu.csus.ecs.pc2.core.report;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Vector;

import edu.csus.ecs.pc2.VersionInfo;
import edu.csus.ecs.pc2.core.IInternalController;
import edu.csus.ecs.pc2.core.Utilities;
import edu.csus.ecs.pc2.core.list.AccountComparator;
import edu.csus.ecs.pc2.core.list.ClarificationComparator;
import edu.csus.ecs.pc2.core.list.ClientSettingsComparator;
import edu.csus.ecs.pc2.core.list.ContestTimeComparator;
import edu.csus.ecs.pc2.core.list.RunComparator;
import edu.csus.ecs.pc2.core.list.SiteComparatorBySiteNumber;
import edu.csus.ecs.pc2.core.log.Log;
import edu.csus.ecs.pc2.core.model.Account;
import edu.csus.ecs.pc2.core.model.Clarification;
import edu.csus.ecs.pc2.core.model.ClientId;
import edu.csus.ecs.pc2.core.model.ClientSettings;
import edu.csus.ecs.pc2.core.model.ClientType;
import edu.csus.ecs.pc2.core.model.ContestInformation;
import edu.csus.ecs.pc2.core.model.ContestTime;
import edu.csus.ecs.pc2.core.model.Filter;
import edu.csus.ecs.pc2.core.model.IInternalContest;
import edu.csus.ecs.pc2.core.model.Judgement;
import edu.csus.ecs.pc2.core.model.JudgementNotification;
import edu.csus.ecs.pc2.core.model.Language;
import edu.csus.ecs.pc2.core.model.NotificationSetting;
import edu.csus.ecs.pc2.core.model.Problem;
import edu.csus.ecs.pc2.core.model.Run;
import edu.csus.ecs.pc2.core.model.Site;
import edu.csus.ecs.pc2.core.model.ContestInformation.TeamDisplayMask;
import edu.csus.ecs.pc2.core.transport.ConnectionHandlerID;

/**
 * Print/Report a number internal settings.
 * 
 * @author pc2@ecs.csus.edu
 * @version $Id$ 
 */

// $HeadURL$
public class InternalDumpReport implements IReport {

    /**
     * 
     */
    private static final long serialVersionUID = -82937493006891083L;

    private IInternalContest contest;

    private IInternalController controller;

    private Log log;

    private Filter filter = new Filter();

    private void printClientSettings(PrintWriter printWriter) {
      
        ClientSettings [] clientSettingsArray = contest.getClientSettingsList();
        
        printWriter.println();
        printWriter.println("-- Client Settings --");
        
        Arrays.sort(clientSettingsArray, new ClientSettingsComparator());
        
        for (ClientSettings clientSettings : clientSettingsArray){
            printWriter.println("    For: "+clientSettings.getClientId());
            for (String key : clientSettings.getKeys()){
                printWriter.println("      "+key+" = "+clientSettings.getProperty(key));
            }
        }
    }
    
    private void printNotificationSettings (PrintWriter printWriter){
        ClientSettings [] clientSettingsArray = contest.getClientSettingsList();
        
        printWriter.println();
        printWriter.println("-- Notification Settings --");
        
        Arrays.sort(clientSettingsArray, new ClientSettingsComparator());
        
        for (ClientSettings clientSettings : clientSettingsArray){
            NotificationSetting notificationSetting = clientSettings.getNotificationSetting();
            if (notificationSetting != null){
                printWriter.println("    For: "+clientSettings.getClientId());
                dumpNotification(printWriter, clientSettings.getNotificationSetting());
            }
        } 
    }
    

    protected void dumpNotification(PrintWriter printWriter, NotificationSetting notificationSetting2) {

        printWriter.println();

        if (notificationSetting2 == null) {
            printWriter.println("          No delivery notification settings defined.");

        } else {
            JudgementNotification judgementNotification = null;

            judgementNotification = notificationSetting2.getPreliminaryNotificationYes();
            printWriter.println("          Prelim Yes send " + judgementNotification.isNotificationSent() + " cuttoff at " + judgementNotification.getCuttoffMinutes());

            judgementNotification = notificationSetting2.getPreliminaryNotificationNo();
            printWriter.println("          Prelim No  send " + judgementNotification.isNotificationSent() + " cuttoff at " + judgementNotification.getCuttoffMinutes());

            judgementNotification = notificationSetting2.getFinalNotificationYes();
            printWriter.println("          Final  Yes send " + judgementNotification.isNotificationSent() + " cuttoff at " + judgementNotification.getCuttoffMinutes());

            judgementNotification = notificationSetting2.getFinalNotificationNo();
            printWriter.println("          Final  No  send " + judgementNotification.isNotificationSent() + " cuttoff at " + judgementNotification.getCuttoffMinutes());
        }
    }

    
    private void printContestInformation(PrintWriter printWriter) {
      
        ContestInformation contestInformation = contest.getContestInformation();
        
        printWriter.println();
        printWriter.println("-- Contest Information --");
        printWriter.println("  Title : '" + contestInformation.getContestTitle()+"'");
        printWriter.println("  URL   : '" + contestInformation.getContestURL()+"'");
        
        printWriter.println();
        printWriter.println("  Include Preliminary Judgements in Scoring Algorithm : "+Utilities.yesNoString(contestInformation.isPreliminaryJudgementsUsedByBoard()));
        printWriter.println("  Send Notifications for Preliminary Judgements       : "+Utilities.yesNoString(contestInformation.isPreliminaryJudgementsTriggerNotifications()));
        printWriter.println("  Send Additional Run Status Information              : "+Utilities.yesNoString(contestInformation.isSendAdditionalRunStatusInformation()));
        printWriter.println();
        
        printWriter.println("  Judges' Default Answer: '" + contestInformation.getJudgesDefaultAnswer()+"'");
        
        if (contestInformation.getTeamDisplayMode() != null){
            printWriter.println("  Judges see: "+contestInformation.getTeamDisplayMode());
        } else {
            printWriter.println("  Judges see: "+TeamDisplayMask.LOGIN_NAME_ONLY);
        }
    }
    
    private void printAccounts(PrintWriter printWriter, Account [] accounts) {
    
        Arrays.sort(accounts, new AccountComparator());
        
        for (Account account : accounts) {
            if (filter.matchesAccount(account)){
                printWriter.print("   Site " + account.getSiteNumber());
                printWriter.format(" %-15s", account.getClientId().getName());
                printWriter.println(" id=" + account.getElementId());
            }
        }
    }

    private void printAccounts(PrintWriter printWriter) {

        printWriter.println();
        printWriter.println("-- Accounts -- " + getFilterText());

        for (ClientType.Type ctype : ClientType.Type.values()) {

            Vector<Account> vector = contest.getAccounts(ctype);
            Account[] accounts = (Account[]) vector.toArray(new Account[vector.size()]);
            int accountCount = filter.countAccounts(accounts);
            printWriter.print("Accounts " + ctype.toString() + " there are " + accountCount);
            printWriter.println();

            printAccounts(printWriter, accounts);

        }

        printAllAccounts(printWriter);
    }
    

    private void printAllAccounts(PrintWriter printWriter) {
        
        Vector<Account> vector = contest.getAccounts(ClientType.Type.ALL);
        printWriter.println();
        
        Account[] accounts = (Account[]) vector.toArray(new Account[vector.size()]);
        int accountCount = filter.countAccounts(accounts);
        printWriter.print("There are " + accountCount);
        printWriter.println();
        printAccounts(printWriter, accounts);

    }

    private String getFilterText() {
        if (filter != null){
            return " (filtered) ";
        } else {
            return "";
        }
    }

    public void writeReport(PrintWriter printWriter) {

        printLocalContestTime(printWriter);
              
        printCurrentClientInfo(printWriter);
        
        printSites (printWriter);

        printContestTimes(printWriter);
                
        printContestInformation(printWriter);
        
        printProblems(printWriter);
        
        printLanguages(printWriter);
        
        printJudgements (printWriter);

        printAccounts(printWriter);

        printRuns(printWriter);
        
        printClarifications(printWriter);

        printLogins(printWriter);
        
        printConnections(printWriter);
        
        printClientSettings(printWriter);
        
        printNotificationSettings(printWriter);

    }

    private void printCurrentClientInfo(PrintWriter printWriter) {

        printWriter.println();
        Account account = contest.getAccount(contest.getClientId());
        String name = contest.getClientId().getName();
        if (account != null){
            name = account.getDisplayName();
        }
        printWriter.println("* Client Id = "+contest.getClientId()+" "+name);

        
    }

    private void printLocalContestTime(PrintWriter printWriter) {

        ContestTime localContestTime = contest.getContestTime();

        printWriter.println();
        if (localContestTime != null) {
            if (localContestTime.isContestRunning()) {
                printWriter.print("Contest is RUNNING");
            } else {
                printWriter.print("Contest is STOPPED");
            }

            printWriter.print(" elapsed = " + localContestTime.getElapsedTimeStr());
            printWriter.print(" remaining = " + localContestTime.getRemainingTimeStr());
            printWriter.print(" length = " + localContestTime.getContestLengthStr());
            printWriter.println();
        } else {
            printWriter.println("Contest Time is undefined (null)");

        }

        
    }

    private void printContestTimes(PrintWriter printWriter) {

        printWriter.println();
        ContestTime[] contestTimes = contest.getContestTimes();
        Arrays.sort(contestTimes, new ContestTimeComparator());
        printWriter.println("-- " + contestTimes.length + " Contest Times --");
        for (ContestTime contestTime : contestTimes) {

            if (contest.getSiteNumber() == contestTime.getSiteNumber()) {
                printWriter.print("  * ");
            } else {
                printWriter.print("    ");
            }
            String state = "STOPPED";
            if (contestTime.isContestRunning()) {
                state = "STARTED";
            }

            printWriter.println("  Site " + contestTime.getSiteNumber() + " " + state + " " + contestTime.getElapsedTimeStr() + " " + contestTime.getRemainingTimeStr() + " "
                    + contestTime.getContestLengthStr());
            
            printWriter.println("         past end " + contestTime.isPastEndOfContest() + ", halt at end " + contestTime.isHaltContestAtTimeZero() + ", offset " + contestTime.getLocalClockOffset()
                    + ", id=" + contestTime.getElementId() + " site " + contestTime.getElementId().getSiteNumber());
        }

    }

    private void printLanguages(PrintWriter printWriter) {
        printWriter.println();
        printWriter.println("-- " + contest.getLanguages().length + " languages --");
        for (Language language : contest.getLanguages()) {
            printWriter.println("  '" + language + "' id=" + language.getElementId());
        }

    }

    private void printProblems(PrintWriter printWriter) {
        
        printWriter.println();
        if (contest.getGeneralProblem() == null){
            printWriter.println(" General Problem: (not defined) ");
        } else {
            printWriter.println(" General Problem: "+contest.getGeneralProblem().getElementId());
        }

        printWriter.println();
        printWriter.println("-- " + contest.getProblems().length + " problems --");
        for (Problem problem : contest.getProblems()) {
            printWriter.println("  '" + problem + "' id=" + problem.getElementId());

        }
    }

    private void printLogins(PrintWriter printWriter) {

        printWriter.println();
        printWriter.println("-- Logins -- ");
        for (ClientType.Type ctype : ClientType.Type.values()) {

            ClientId[] clientIds = contest.getAllLoggedInClients(ctype);
            if (clientIds.length > 0) {

                printWriter.println("Logged in " + ctype.toString());

                for (ClientId clientId : clientIds) {
                    ConnectionHandlerID connectionHandlerID = contest.getConnectionHandleID(clientId);
                    printWriter.println("   " + clientId + " on " + connectionHandlerID);
                }
            }
        }

        
    }

    private void printSites(PrintWriter printWriter) {
        
        printWriter.println();
        printWriter.println("-- " + contest.getSites().length + " sites --");
        Site[] sites = contest.getSites();
        Arrays.sort(sites, new SiteComparatorBySiteNumber());
        for (Site site1 : sites) {
            String hostName = site1.getConnectionInfo().getProperty(Site.IP_KEY);
            String portStr = site1.getConnectionInfo().getProperty(Site.PORT_KEY);

            printWriter.println("Site " + site1.getSiteNumber() + " " + hostName + ":" + portStr + " '" + site1.getDisplayName() + "' " + " password='" + site1.getPassword() + "' id="
                    + site1.getElementId());
        }

    }

    private void printJudgements(PrintWriter printWriter) {
        
        // Judgements
        printWriter.println();
        Judgement [] judgements = contest.getJudgements();
        
        printWriter.println("-- " + judgements.length + " Judgements --");
        for (Judgement judgement : judgements) {
            printWriter.println("  '" + judgement + "' id=" + judgement.getElementId());
        }
    }

    public void printConnections(PrintWriter printWriter) {

        // Connections
        printWriter.println();
        ConnectionHandlerID[] connectionHandlerIDs = contest.getConnectionHandleIDs();
        // Arrays.sort(connectionHandlerIDs, new ConnectionHanlderIDComparator());
        printWriter.println("-- " + connectionHandlerIDs.length + " Connections --");
        for (ConnectionHandlerID connectionHandlerID : connectionHandlerIDs) {
            printWriter.println("  " + connectionHandlerID);
        }
    }

    private void printHeader(PrintWriter printWriter) {
        VersionInfo versionInfo = new VersionInfo();
        printWriter.println(versionInfo.getSystemName());
        printWriter.println("Date: " + Utilities.getL10nDateTime());
        printWriter.println(versionInfo.getSystemVersionInfo());
        printWriter.println("Build " + versionInfo.getBuildNumber());

    }

    private void printFooter(PrintWriter printWriter) {
        printWriter.println();
        printWriter.println("end report");
    }

    public void createReportFile(String filename, Filter inFilter) throws IOException {

        if (inFilter == null){
            throw new IllegalArgumentException("filter must not be null");
        }
        filter = inFilter;

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

        } catch (Exception e) {
            log.log(Log.INFO, "Exception writing report", e);
            printWriter.println("Exception generating report " + e.getMessage());
        }

        printWriter.close();
        printWriter = null;
    }

    public String[] createReport(Filter inFilter) {
        throw new SecurityException("Not implemented");
    }

    public String createReportXML(Filter inFilter) {
        throw new SecurityException("Not implemented");
    }

    public String getReportTitle() {
        return "Internal Dump";
    }

    public void setContestAndController(IInternalContest inContest, IInternalController inController) {
        this.contest = inContest;
        this.controller = inController;
        log = controller.getLog();
    }

    public String getPluginTitle() {
        return "Internal Dump Report";
    }

    private void printRuns(PrintWriter printWriter) {
        // Runs
        printWriter.println();
        Run[] runs = contest.getRuns();
        Arrays.sort(runs, new RunComparator());

        int count = filter.countRuns(runs);

        printWriter.print("-- " + count + " runs --"+getFilterText());
        printWriter.println();
        if (count > 0) {
            for (Run run : runs) {
                if (filter.matches(run)) {
                    printWriter.println("  " + run);
                }
            }
        }
    }

    private void printClarifications(PrintWriter printWriter) {
        // Clarifications
        printWriter.println();
        Clarification[] clarifications = contest.getClarifications();
        Arrays.sort(clarifications, new ClarificationComparator());

        int count = filter.countClarifications(clarifications);
        
        printWriter.print("-- " + count + " clarifications -- "+getFilterText());
        printWriter.println();
        for (Clarification clarification : clarifications) {
            if (filter.matches(clarification)) {
                printWriter.println("  " + clarification);
            }
        }
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }
    
    /**
     * Count the accounts that match this filter.
     * @param accounts
     * @return number of accounts matching this filter.
     */
    public int countClientSettings (ClientSettings [] clientSettings){
        int count = 0;
        for (ClientSettings clientSettings2 : clientSettings) {
            if (filter.matches(clientSettings2.getClientId())) {
                count++;
            }
        }
        return count;
    }
    

}
