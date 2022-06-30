// Copyright (C) 1989-2019 PC2 Development Team: John Clevenger, Douglas Lane, Samir Ashoo, and Troy Boudreau.
package edu.csus.ecs.pc2.core.report;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.csus.ecs.pc2.VersionInfo;
import edu.csus.ecs.pc2.core.IInternalController;
import edu.csus.ecs.pc2.core.Utilities;
import edu.csus.ecs.pc2.core.list.JudgementSortBySiteAcronymComparator;
import edu.csus.ecs.pc2.core.log.Log;
import edu.csus.ecs.pc2.core.model.Filter;
import edu.csus.ecs.pc2.core.model.IInternalContest;
import edu.csus.ecs.pc2.core.model.Judgement;

/**
 * Print All Judgement Information.
 * 
 * @author pc2@ecs.csus.edu
 * @version $Id$
 */

// $HeadURL$

public class JudgementReport implements IReport {

    /**
     * 
     */
    private static final long serialVersionUID = -287199138291014045L;

    private IInternalContest contest;

    private IInternalController controller;

    private Log log;
    
    private Filter filter;

    public void writeReport(PrintWriter printWriter) {
        
        // Active Judgements
        printWriter.println();
        Judgement [] judgements = contest.getJudgements();
        
        printWriter.println("-- " + judgements.length + " Judgements --");
        printWriter.println("     Active Judgements");
        for (Judgement judgement : judgements) {
            if (! judgement.isActive()){
                continue;
            }
            printWriter.print("  '" + judgement + "'");
            printWriter.print(" acronym=" + judgement.getAcronym());
            printWriter.print(" id=" + judgement.getElementId());
            printWriter.print(" site=" + judgement.getSiteNumber());
            printWriter.println();
        }
        
        printWriter.println("     All Judgements");
        for (Judgement judgement : judgements) {
            String hiddenText = "";
            if (!judgement.isActive()){
                hiddenText = "[HIDDEN] ";
            }
            printWriter.print("  '" + judgement );
            printWriter.print("' ");
            printWriter.print(" acronym=" + judgement.getAcronym());
            printWriter.print(hiddenText+" id=" + judgement.getElementId());
            printWriter.print(" site=" + judgement.getSiteNumber());
            printWriter.println();
        }
        
        
        printWriter.println();
        printWriter.println();
        printWriter.println("     All Judgements for all sites, " + judgements.length + " Judgements");
        printWriter.println();
        
        Arrays.sort(judgements, new JudgementSortBySiteAcronymComparator());
        
        int lastSite = -1;
        for (Judgement judgement : judgements) {

            if (lastSite != judgement.getSiteNumber()) {
                printWriter.println();
                // print # judgements per site
                List<Judgement> judgementList = getSitesJudgements(judgements, judgement.getSiteNumber());
                int judgementPerSiteCount = judgementList.size();
                printWriter.println("-- Site " + judgement.getSiteNumber() + " has " + judgementPerSiteCount + " Judgements --");
                lastSite = judgement.getSiteNumber();
            }
        
            
            String hiddenText = "";
            if (!judgement.isActive()){
                hiddenText = "[HIDDEN] ";
            }
            printWriter.print(" site=" + judgement.getSiteNumber());
            printWriter.print("  '" + judgement );
            printWriter.print("' ");
            printWriter.print(" acronym=" + judgement.getAcronym());
            printWriter.print(hiddenText+" id=" + judgement.getElementId());
            printWriter.println();
            
        }
    }

    /**
     * Return a list of all judgements assigned for a site number
     * @param judgements
     * @param siteNumber
     * @return list of judgements with site number siteNumber.
     */
    private List<Judgement> getSitesJudgements(Judgement[] judgements, int siteNumber) {

        List<Judgement> list = new ArrayList<Judgement>();

        for (Judgement judgement : judgements) {
            if (siteNumber == judgement.getSiteNumber()) {
                list.add(judgement);
            }
        }

        return list;
    }

    public void printHeader(PrintWriter printWriter) {
        printWriter.println(new VersionInfo().getSystemName());
        printWriter.println("Date: " + Utilities.getL10nDateTime());
        printWriter.println(new VersionInfo().getSystemVersionInfo());
        printWriter.println();
        printWriter.println(getReportTitle() + " Report");
    }

    public void printFooter(PrintWriter printWriter) {
        printWriter.println();
        printWriter.println("end report");
    }

    public void createReportFile(String filename, Filter inFilter) throws IOException {

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

    public String[] createReport(Filter inFilter) {
        throw new SecurityException("Not implemented");
    }

    public String createReportXML(Filter inFilter) throws IOException {
        return Reports.notImplementedXML(this);
    }

    public String getReportTitle() {
        return "Judgements";
    }

    public void setContestAndController(IInternalContest inContest, IInternalController inController) {
        this.contest = inContest;
        this.controller = inController;
        log = controller.getLog();
    }

    public String getPluginTitle() {
        return "Judgements Report";
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

}
