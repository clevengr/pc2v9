// Copyright (C) 1989-2024 PC2 Development Team: John Clevenger, Douglas Lane, Samir Ashoo, and Troy Boudreau.
package edu.csus.ecs.pc2.api;

import java.util.HashMap;

import edu.csus.ecs.pc2.api.exceptions.LoginFailureException;
import edu.csus.ecs.pc2.api.exceptions.NotLoggedInException;
import edu.csus.ecs.pc2.core.model.ElementId;

/**
 * Sample Code for API.
 *
 * This class is not intended as a JUnit test, it is a syntax check for the API samples in the Java doc in the API classes.
 *
 * @author pc2@ecs.csus.edu
 * @version $Id$
 */

// $HeadURL$
public class APISampleCode {

    /**
     * Sample code for ServerConnection
     */
    @SuppressWarnings("unused")
    public void serverConnectionSample() {

        String login = "team4";
        String password = "team4";
        try {
            ServerConnection serverConnection = new ServerConnection();
            IContest contest = serverConnection.login(login, password);
            // ... code here to invoke methods in "contest";
            serverConnection.logoff();
        } catch (LoginFailureException e) {
            System.out.println("Could not login because " + e.getMessage());
        } catch (NotLoggedInException e) {
            System.out.println("Unable to execute API method");
            e.printStackTrace();
        }

    }

    // IContest Samples

    /**
     * getTeams() sample.
     *
     * @param contest The contest from which team samples are to be drawn
     */
    public void getTeamsSample(IContest contest) {

        for (ITeam team : contest.getTeams()) {
            String teamName = team.getDisplayName();
            int siteNumber = team.getSiteNumber();

            HashMap<ElementId, IGroup> groups = team.getGroups();
            String groupName = "";
            boolean first = true;
            for(ElementId groupElementId : groups.keySet()) {
                IGroup group = groups.get(groupElementId);
                if(group != null) {
                    if(first) {
                        first = false;
                    } else {
                        groupName = groupName + ",";
                    }
                    groupName = groupName + group.getName();
                }
            }
            if(groupName.isEmpty()) {
                groupName = "(no groups assigned)";
            }
            System.out.println(teamName + " Site: " + siteNumber + " Groups: " + groupName);
        }

    }

    /**
     * getLanguages() sample.
     *
     * @param contest The contest from which language samples are to be drawn
     */
    public void getLanguagesSample(IContest contest) {

        for (ILanguage language : contest.getLanguages()) {
            System.out.println(language.getName());
        }

    }

    /**
     * getProblems() sample.
     *
     * @param contest The contest from which problem samples are to be drawn
     */
    public void getProblemSample(IContest contest) {

        for (IProblem problem : contest.getProblems()) {
            System.out.println(problem.getName());
        }

    }

    /**
     * getJudgements() sample.
     *
     * @param contest The contest from which judgement samples are to be drawn
     */
    public void getJudgmentsSample(IContest contest) {

        for (IJudgement judgement : contest.getJudgements()) {
            System.out.println(judgement.getName());
        }

    }

    /**
     * getRuns() sample.
     *
     * @param contest The contest from which run samples are to be drawn
     */
    public void getRunsSample(IContest contest) {

        for (IRun run : contest.getRuns()) {

            System.out.println("Run " + run.getNumber() + " from site " + run.getSiteNumber());
            System.out.println("    submitted at " + run.getSubmissionTime() + " minutes by " + run.getTeam().getDisplayName());
            System.out.println("    For problem " + run.getProblem().getName());
            System.out.println("    Written in " + run.getLanguage().getName());

            if (run.isFinalJudged()) {
                System.out.println("    Judgement: " + run.getJudgementName());
            } else {
                System.out.println("    Judgement: not judged yet ");
            }
        }
    }

    /**
     * getStandings() samples.
     *
     * @param contest The contest from which Standings samples are to be drawn
     */
    public void getStandingsSample(IContest contest) {

        for (IStanding standingRank : contest.getStandings()) {
            String displayName = standingRank.getClient().getDisplayName();
            System.out.printf(" %3d %-35s %2d %4d", standingRank.getRank(), displayName, standingRank.getNumProblemsSolved(), standingRank.getPenaltyPoints());
        }
    }
}
