// Copyright (C) 1989-2019 PC2 Development Team: John Clevenger, Douglas Lane, Samir Ashoo, and Troy Boudreau.
package edu.csus.ecs.pc2.core.model;

import junit.framework.TestCase;
import edu.csus.ecs.pc2.core.list.JudgementNotificationsList;
import edu.csus.ecs.pc2.core.model.ClientType.Type;
import edu.csus.ecs.pc2.core.model.Run.RunStates;

/**
 * Testing for RunUtilities.
 * 
 * 
 * @author pc2@ecs.csus.edu
 * @version $Id$
 */

// $HeadURL$
public class RunUtilitiesTest extends TestCase {

    /**
     * Test Suppress Judgement.
     */
    public void testSuppressJudgement() {

        SampleContest sampleContest = new SampleContest();

        IInternalContest contest = sampleContest.createContest(1, 3, 33, 12, true);

        JudgementNotificationsList judgementNotificationsList = null;

        ContestTime contestTime = contest.getContestTime();

        ClientId firstJudgeId = contest.getAccounts(Type.JUDGE).firstElement().getClientId();

        Run[] runs = sampleContest.createRandomRuns(contest, 12, true, true, false);

        Judgement yesJudgement = sampleContest.getYesJudgement(contest);

        Run run = runs[0];

        boolean suppressed = RunUtilities.supppressJudgement(null, run, contestTime);

        assertFalse("Should be suppressed, no notifications defined", suppressed);

        /**
         * Add Preliminary Judgement Yes.
         */

        JudgementRecord record = new JudgementRecord(yesJudgement.getElementId(), firstJudgeId, true, false);
        record.setPreliminaryJudgement(true);
        run.addJudgement(record);

        suppressed = RunUtilities.supppressJudgement(null, run, contestTime);
        assertFalse("Should be suppressed, no notifications defined", suppressed);

        judgementNotificationsList = new JudgementNotificationsList();
        NotificationSetting notificationSetting = new NotificationSetting(run.getProblemId());
        judgementNotificationsList.add(notificationSetting);

        NotificationSetting notificationSetting2 = (NotificationSetting) judgementNotificationsList.get(run.getProblemId());
        assertTrue("Notification not properly inserted into list", notificationSetting.isSameAs(notificationSetting2));

        suppressed = RunUtilities.supppressJudgement(judgementNotificationsList, run, contestTime);
        assertFalse("Should be suppressed, notifications defined but turned ON", suppressed);

        JudgementNotification judgementNotification = new JudgementNotification(true, 30);
        notificationSetting.setPreliminaryNotificationYes(judgementNotification);

        JudgementNotification judgementNotificationNo = new JudgementNotification(false, 30);
        notificationSetting.setPreliminaryNotificationNo(judgementNotificationNo);

        run.setElapsedMins(minutesBeforeEnd(contest, 31));
        suppressed = RunUtilities.supppressJudgement(judgementNotificationsList, run, contestTime);
        assertFalse("Should be NOT suppressed, run.elapsed = 31", suppressed);

        run.setElapsedMins(minutesBeforeEnd(contest, 30));
        suppressed = RunUtilities.supppressJudgement(judgementNotificationsList, run, contestTime);
        assertTrue("Should be suppressed, run.elapsed = 30", suppressed);

        run.setElapsedMins(minutesBeforeEnd(contest, 29));
        suppressed = RunUtilities.supppressJudgement(judgementNotificationsList, run, contestTime);
        assertTrue("Should be suppressed, run.elapsed = 29", suppressed);

        /**
         * Add Final Judgement Yes.
         */

        record = new JudgementRecord(yesJudgement.getElementId(), firstJudgeId, true, false);
        // final judgement
        //        record.setPreliminaryJudgement(true);
        run.addJudgement(record);

        run.setElapsedMins(minutesBeforeEnd(contest, 31));
        suppressed = RunUtilities.supppressJudgement(judgementNotificationsList, run, contestTime);
        assertFalse("Should be NOT suppressed, run.elapsed = 31", suppressed);

        run.setElapsedMins(minutesBeforeEnd(contest, 30));
        suppressed = RunUtilities.supppressJudgement(judgementNotificationsList, run, contestTime);
        assertFalse("Should be NOT suppressed, run.elapsed = 30", suppressed);

        run.setElapsedMins(minutesBeforeEnd(contest, 29));
        suppressed = RunUtilities.supppressJudgement(judgementNotificationsList, run, contestTime);
        assertFalse("Should be NOT suppressed, run.elapsed = 29", suppressed);

        // Add Final suppress for yes

        judgementNotification = new JudgementNotification(true, 30);
        notificationSetting.setFinalNotificationYes(judgementNotification);

        run.setElapsedMins(minutesBeforeEnd(contest, 31));
        suppressed = RunUtilities.supppressJudgement(judgementNotificationsList, run, contestTime);
        assertFalse("Should be NOT suppressed, run.elapsed = 31", suppressed);

        run.setElapsedMins(minutesBeforeEnd(contest, 30));
        suppressed = RunUtilities.supppressJudgement(judgementNotificationsList, run, contestTime);
        assertTrue("Should be suppressed, run.elapsed = 30", suppressed);

        run.setElapsedMins(minutesBeforeEnd(contest, 29));
        suppressed = RunUtilities.supppressJudgement(judgementNotificationsList, run, contestTime);
        assertTrue("Should be suppressed, run.elapsed = 29", suppressed);

        /**
         * Loop through all runs, set Final Judgmenet Yes for each run,
         * except the first one which is ignored.
         */
        for (Run run2 : runs) {
            if (!run2.equals(run)) {
                suppressed = RunUtilities.supppressJudgement(judgementNotificationsList, run2, contestTime);
                assertFalse("Should be NOT suppressed, not judged " + run2, suppressed);

                // Add final judgement
                record = new JudgementRecord(yesJudgement.getElementId(), firstJudgeId, true, false);
                run2.addJudgement(record);

                suppressed = RunUtilities.supppressJudgement(judgementNotificationsList, run2, contestTime);
                assertFalse("Should be NOT suppressed, judged " + run2, suppressed);

                run2.setElapsedMins(minutesBeforeEnd(contest, 20));
                suppressed = RunUtilities.supppressJudgement(judgementNotificationsList, run2, contestTime);

                if (run2.getProblemId().equals(run.getProblemId())) {
                    /**
                     * Same Problem as run, so the settings are to suppress the run.
                     */
                    assertTrue("Should be suppressed, judged at 20 min " + run2, suppressed);
                } else {
                    assertFalse("Should be NOT suppressed, judged at 20 min " + run2, suppressed);
                }

            }
        }

        // TODO test all combinations of final/prelim/no/yes and cuttoff time
    }

    /**
     * Test the {@link RunUtilities#createNewRun(Run, IInternalContest)}
     */
    public void testCreateNewRun() {

        SampleContest sampleContest = new SampleContest();

        IInternalContest contest = sampleContest.createContest(3, 3, 33, 12, true);

        ClientId firstJudgeId = contest.getAccounts(Type.JUDGE).firstElement().getClientId();

        Run[] runs = sampleContest.createRandomRuns(contest, 12, true, true, false);

        Judgement yesJudgement = sampleContest.getYesJudgement(contest);

        Run run = runs[0];
        run.setElapsedMins(40);

        // prelim judgement
        JudgementRecord record = new JudgementRecord(yesJudgement.getElementId(), firstJudgeId, true, true);
        record.setPreliminaryJudgement(true);
        run.addJudgement(record);
        run.setStatus(RunStates.JUDGED);

        // final judgement
        record = new JudgementRecord(yesJudgement.getElementId(), firstJudgeId, true, false);
        run.addJudgement(record);

        Run run2 = RunUtilities.createNewRun(run, contest);

        assertEquals(run.getSiteNumber(), run2.getSiteNumber());
        assertEquals(run.getNumber(), run2.getNumber());
        assertEquals(run.getElapsedMins(), run2.getElapsedMins());

        assertEquals(run.getElementId(), run2.getElementId());

        assertEquals(run.getProblemId(), run2.getProblemId());
        assertEquals(run.getLanguageId(), run2.getLanguageId());

        assertFalse(run2.isJudged());
        assertTrue(run.isJudged());

        assertFalse(run2.isSendToTeams());
        assertTrue(run.isSendToTeams());

        assertEquals(RunStates.NEW, run2.getStatus());
        assertNotSame(RunStates.NEW, run.getStatus());

        assertFalse(run.isSameAs(run2));

        assertNull(run2.getJudgementRecord());
        assertNotNull(run.getJudgementRecord());
    }

    /**
     * Return the elapsed time for mins before end of contest.
     * @param contest
     * @param mins
     * @return
     */
    private long minutesBeforeEnd(IInternalContest contest, int mins) {
        return contest.getContestTime().getContestLengthMins() - mins;
    }

    /**
     * Test isDigits, positiv and negative tests.
     * @throws Exception
     */
    public void testisDigits() throws Exception {

        String[] data = {
                //
                "1233", //
                "0", //
                "585849393", //
        };

        for (String inNum : data) {
            assertTrue("Expecting isDigits for " + inNum, RunUtilities.isDigits(inNum));
        }

        String[] negativeData = {
                //
                "", //
                "FE123", //
                "ab", //
                "", //
                "FE123", //
        };

        for (String inNum : negativeData) {
            assertFalse("Expecting not isDigits for " + inNum, RunUtilities.isDigits(inNum));
        }
    }

    /**
     * Test isAlreadySubmitted.
     * 
     * @throws Exception
     */
    public void testisAlreadySubmitted() throws Exception {

        SampleContest sampleContest = new SampleContest();

        IInternalContest contest = sampleContest.createContest(1, 3, 33, 12, true);

        Run[] runs = sampleContest.createRandomRuns(contest, 12, true, true, false);

        Run oRun = runs[5];
        int overrideId = 55343;
        oRun.setOverRideNumber(overrideId);

        for (Run run : runs) {
            contest.addRun(run);
        }
        Run foundRun = null;

        int count = 0;
        runs = contest.getRuns();
        for (Run run : runs) {
            if (run.getOverrideNumber() != 0) {
                foundRun = run;
                count++;
            }
        }

        assertTrue("Expect already submitted ", RunUtilities.isAlreadySubmitted(contest, overrideId + ""));

        assertEquals("Expecting 1 override run ", 1, count);

        assertTrue("Missing override run ", foundRun != null);

        assertEquals("Expecting override id ", overrideId, foundRun.getOverrideNumber());

        // override id has precedence so getNumber should be the same.
        assertEquals("Expecting override id ", overrideId, foundRun.getNumber());
    }
}
