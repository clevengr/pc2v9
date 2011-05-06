package edu.csus.ecs.pc2.core.model;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Random;
import java.util.Vector;

import edu.csus.ecs.pc2.core.IInternalController;
import edu.csus.ecs.pc2.core.InternalController;
import edu.csus.ecs.pc2.core.list.AccountComparator;
import edu.csus.ecs.pc2.core.model.ClientType.Type;
import edu.csus.ecs.pc2.core.model.Run.RunStates;
import edu.csus.ecs.pc2.core.report.IReport;
import edu.csus.ecs.pc2.core.security.FileSecurityException;
import edu.csus.ecs.pc2.core.security.FileStorage;
import edu.csus.ecs.pc2.core.util.JUnitUtilities;

/**
 * Create Sample InternalContest and InternalController.
 * 
 * Also has a method to create a Site class instance.
 * 
 * @see #createContest(int, int, int, int)
 * @see #createController(IInternalContest, boolean, boolean)
 * 
 * @author pc2@ecs.csus.edu
 * @version $Id$
 */

// $HeadURL$
public class SampleContest {

    private boolean debugMode = false;

    private Random random = new Random();

    public static final int DEFAULT_PORT_NUMBER = 50002;

    private int defaultPortNumber = DEFAULT_PORT_NUMBER;
    
    private static final String[] W3C_COLORS = { "Alice Blue;#F0F8FF", "Antique White;#FAEBD7", "Aqua;#00FFFF", "Aquamarine;#7FFFD4", "Azure;#F0FFFF", "Beige;#F5F5DC", "Bisque;#FFE4C4",
            "Black;#000000", "Blanched Almond;#FFEBCD", "Blue;#0000FF", "Blue Violet;#8A2BE2", "Brown;#A52A2A", "Burlywood;#DEB887", "Cadet Blue;#5F9EA0", "Chartreuse;#7FFF00", "Chocolate;#D2691E",
            "Coral;#FF7F50", "Cornflower;#6495ED", "Cornsilk;#FFF8DC", "Crimson;#DC143C", "Cyan;#00FFFF", "Dark Blue;#00008B", "Dark Cyan;#008B8B", "Dark Goldenrod;#B8860B", "Dark Gray;#A9A9A9",
            "Dark Green;#006400", "Dark Khaki;#BDB76B", "Dark Magenta;#8B008B", "Dark Olive Green;#556B2F", "Dark Orange;#FF8C00", "Dark Orchid;#9932CC", "Dark Red;#8B0000", "Dark Salmon;#E9967A",
            "Dark Sea Green;#8FBC8F", "Dark Slate Blue;#483D8B", "Dark Slate Gray;#2F4F4F", "Dark Turquoise;#00CED1", "Dark Violet;#9400D3", "Deep Pink;#FF1493", "Deep Sky Blue;#00BFFF",
            "Dim Gray;#696969", "Dodger Blue;#1E90FF", "Firebrick;#B22222", "Floral White;#FFFAF0", "Forest Green;#228B22", "Fuchsia;#FF00FF", "Gainsboro;#DCDCDC", "Ghost White;#F8F8FF",
            "Gold;#FFD700", "Goldenrod;#DAA520", "Gray;#7F7F7F", "Green;#007F00", "Green Yellow;#ADFF2F", "Honeydew;#F0FFF0", "Hot Pink;#FF69B4", "Indian Red;#CD5C5C", "Indigo;#4B0082",
            "Ivory;#FFFFF0", "Khaki;#F0E68C", "Lavender;#E6E6FA", "Lavender Blush;#FFF0F5", "Lawn Green;#7CFC00", "Lemon Chiffon;#FFFACD", "Light Blue;#ADD8E6", "Light Coral;#F08080",
            "Light Cyan;#E0FFFF", "Light Goldenrod;#FAFAD2", "Light Gray;#D3D3D3", "Light Green;#90EE90", "Light Pink;#FFB6C1", "Light Salmon;#FFA07A", "Light Sea Green;#20B2AA",
            "Light Sky Blue;#87CEFA", "Light Slate Gray;#778899", "Light Steel Blue;#B0C4DE", "Light Yellow;#FFFFE0", "Lime;#00FF00", "Lime Green;#32CD32", "Linen;#FAF0E6", "Magenta;#FF00FF",
            "Maroon;#7F0000", "Medium Aquamarine;#66CDAA", "Medium Blue;#0000CD", "Medium Orchid;#BA55D3", "Medium Purple;#9370DB", "Medium Sea Green;#3CB371", "Medium Slate Blue;#7B68EE",
            "Medium Spring Green;#00FA9A", "Medium Turquoise;#48D1CC", "Medium Violet Red;#C71585", "Midnight Blue;#191970", "Mint Cream;#F5FFFA", "Misty Rose;#FFE4E1", "Moccasin;#FFE4B5",
            "Navajo White;#FFDEAD", "Navy;#000080", "Old Lace;#FDF5E6", "Olive;#808000", "Olive Drab;#6B8E23", "Orange;#FFA500", "Orange Red;#FF4500", "Orchid;#DA70D6", "Pale Goldenrod;#EEE8AA",
            "Pale Green;#98FB98", "Pale Turquoise;#AFEEEE", "Pale Violet Red;#DB7093", "Papaya Whip;#FFEFD5", "Peach Puff;#FFDAB9", "Peru;#CD853F", "Pink;#FFC0CB", "Plum;#DDA0DD",
            "Powder Blue;#B0E0E6", "Purple;#7F007F", "Red;#FF0000", "Rosy Brown;#BC8F8F", "Royal Blue;#4169E1", "Saddle Brown;#8B4513", "Salmon;#FA8072", "Sandy Brown;#F4A460", "Sea Green;#2E8B57",
            "Seashell;#FFF5EE", "Sienna;#A0522D", "Silver;#C0C0C0", "Sky Blue;#87CEEB", "Slate Blue;#6A5ACD", "Slate Gray;#708090", "Snow;#FFFAFA", "Spring Green;#00FF7F", "Steel Blue;#4682B4",
            "Tan;#D2B48C", "Teal;#008080", "Thistle;#D8BFD8", "Tomato;#FF6347", "Turquoise;#40E0D0", "Violet;#EE82EE", "Wheat;#F5DEB3", "White;#FFFFFF", "White Smoke;#F5F5F5", "Yellow;#FFFF00",
            "Yellow Green;#9ACD32" };

    /**
     * Create a new Site class instance.
     * 
     * @param siteNumber
     *            site number
     * @param siteName
     *            title for site
     * @param hostName
     *            if null, assigned to localhost
     * @param portNumber
     *            if 0 assigned 50002 + (siteNumber-1)* 1000
     * @return
     */
    public Site createSite(int siteNumber, String siteName, String hostName, int portNumber) {
        Site site = new Site(siteName, siteNumber);

        Properties props = new Properties();
        if (hostName == null) {
            props.put(Site.IP_KEY, "localhost");
        }

        if (portNumber == 0) {
            portNumber = defaultPortNumber + (siteNumber - 1) * 1000;
        }
        props.put(Site.PORT_KEY, "" + portNumber);

        site.setConnectionInfo(props);
        site.setPassword("site" + siteNumber);

        return site;
    }

    public IInternalContest createContest(int siteNumber, int numSites, int numTeams, int numJudges, boolean initAsServer) {

        String contestPassword = "Password 101";
        Profile profile = new Profile("Default.");
        return createContest(siteNumber, numSites, numTeams, numJudges, initAsServer, profile, contestPassword);
    }

    /**
     * Create an instance of contest with languages, problems, teams and judges.
     * 
     * @param numSites
     *            number of sites to create
     * @param numTeams
     *            number of teams to create
     * @param numJudges
     *            number of judges to create
     * @param setAsServer
     * @return
     */
    public IInternalContest createContest(int siteNumber, int numSites, int numTeams, int numJudges, boolean initAsServer, Profile profile, String contestPassword) {

        String[] languages = { LanguageAutoFill.JAVATITLE, LanguageAutoFill.DEFAULTTITLE, LanguageAutoFill.GNUCPPTITLE, LanguageAutoFill.PERLTITLE, LanguageAutoFill.MSCTITLE, "APL" };
        String[] problems = { "Sumit", "Quadrangles", "Routing", "Faulty Towers", "London Bridge", "Finnigans Bluff" };
        String[] judgements = { "Stupid programming error", "Misread problem statement", "Almost there", "You have no clue", "Give up and go home", "Consider switching to another major",
                "How did you get into this place ?", "Contact Staff - you have no hope" };

        InternalContest contest = new InternalContest();

        contest.setSiteNumber(siteNumber);

        for (int i = 0; i < numSites; i++) {
            Site site = createSite(i + 1, "Site " + (i + 1), null, 0);
            contest.addSite(site);
        }

        for (String langName : languages) {
            Language language = new Language(langName);
            String[] values = LanguageAutoFill.getAutoFillValues(langName);
            if (values[0].trim().length() != 0) {
                fillLanguage(language, values);
            }
            language.setSiteNumber(siteNumber);
            contest.addLanguage(language);
        }

        for (String probName : problems) {
            Problem problem = new Problem(probName);
            problem.setSiteNumber(siteNumber);
            contest.addProblem(problem);
        }

        Problem generalProblem = new Problem("General.");
        contest.setGeneralProblem(generalProblem);

        Judgement judgementYes = new Judgement("Yes.");
        contest.addJudgement(judgementYes);

        for (String judgementName : judgements) {
            Judgement judgement = new Judgement(judgementName);
            contest.addJudgement(judgement);
        }

        if (numTeams > 0) {
            contest.generateNewAccounts(Type.TEAM.toString(), numTeams, true);
        }

        if (numJudges > 0) {
            contest.generateNewAccounts(Type.JUDGE.toString(), numJudges, true);
        }

        ContestTime contestTime = new ContestTime(siteNumber);
        contest.addContestTime(contestTime);

        if (initAsServer) {
            ClientId serverId = new ClientId(siteNumber, Type.SERVER, 0);
            contest.setClientId(serverId);
        }

        contest.setProfile(profile);
        contest.setContestPassword(contestPassword);
        
        assignColors(contest);
        
        return contest;
    }
    
    private void assignColors (IInternalContest contest) {

        getNotificationSettings(contest);
        
        BalloonSettings balloonSettings = contest.getBalloonSettings(contest.getSiteNumber());
        
        int problemNumber = 0;
        
        for (Problem problem : contest.getProblems()) {
            String [] fields = W3C_COLORS[problemNumber].split(";");
            balloonSettings.addColor(problem, fields[0], fields[1]);
            problemNumber ++;
        }
        
    }

    private void fillLanguage(Language language, String[] values) {
        // values array
        // 0 Title for Language
        // 1 Compiler Command Line
        // 2 Executable Identifier Mask
        // 3 Execute command line

        language.setCompileCommandLine(values[1]);
        language.setExecutableIdentifierMask(values[2]);
        language.setProgramExecuteCommandLine(values[3]);
    }

    public IInternalController createController(IInternalContest contest, boolean isServer, boolean isRemote) {
        return createController(contest, null, isServer, isRemote);
    }

    /**
     * Create a InternalController.
     * 
     * @param contest
     *            model for controller
     * @param isServer
     *            is this a server controller ?
     * @param isRemote
     *            is this a remote site ?
     * @param storageDirectory
     *            where this controller would save its packets.
     * @return
     */
    public IInternalController createController(IInternalContest contest, String storageDirectory, boolean isServer, boolean isRemote) {

        // Start site 1
        InternalController controller = new InternalController(contest);
        controller.setUsingMainUI(false);

        if (isServer) {
            controller.setContactingRemoteServer(isRemote);
            String[] argsSiteOne = { "--server", "--skipini" };
            int siteNumber = contest.getSiteNumber();

            // As of 2008-01-20 start sets site number to zero.
            controller.start(argsSiteOne);

            // set InternalContest back to original site number
            contest.setSiteNumber(siteNumber);

            if (storageDirectory != null) {
                FileStorage storage = new FileStorage(storageDirectory);
                contest.setStorage(storage);
                controller.initializeStorage(storage);
            }
        } else {
            controller.start(null);

        }

        return controller;
    }

    public static String getTestDirectoryName() {
        String testDir = "testing";

        if (!new File(testDir).isDirectory()) {
            new File(testDir).mkdirs();
        }

        return testDir;
    }

    public static String getTestDirectoryName(String subDirName) {
        String testDir = getTestDirectoryName() + File.separator + subDirName;

        if (!new File(testDir).isDirectory()) {
            new File(testDir).mkdirs();
        }

        return testDir;
    }

    /**
     * Populate Language, Problems and Judgements.
     * 
     * @param contest
     */
    public void populateContest(IInternalContest contest) {

        String[] languages = { "Java", LanguageAutoFill.JAVATITLE, LanguageAutoFill.DEFAULTTITLE, LanguageAutoFill.GNUCPPTITLE, LanguageAutoFill.PERLTITLE, LanguageAutoFill.MSCTITLE, "APL" };
        String[] problems = { "Sumit", "Quadrangles", "Routing" };
        String[] judgements = { "No no", "No no no", "No - judges are confused" };

        for (String langName : languages) {
            Language language = new Language(langName);
            String[] values = LanguageAutoFill.getAutoFillValues(langName);
            if (values[0].trim().length() != 0) {
                fillLanguage(language, values);
            }
            contest.addLanguage(language);
        }

        for (String probName : problems) {
            Problem problem = new Problem(probName);
            contest.addProblem(problem);
        }

        // Only put judgements in if there are no judgements
        if (contest.getJudgements().length == 0) {

            Judgement judgementYes = new Judgement("Yes");
            contest.addJudgement(judgementYes);

            for (String judgementName : judgements) {
                contest.addJudgement(new Judgement(judgementName));
            }
        }

    }

    /**
     * Get all sites' teams.
     * 
     * @param contest
     * @return
     */
    protected Account[] getTeamAccounts(IInternalContest contest) {
        Vector<Account> accountVector = contest.getAccounts(ClientType.Type.TEAM);
        Account[] accounts = (Account[]) accountVector.toArray(new Account[accountVector.size()]);
        Arrays.sort(accounts, new AccountComparator());

        return accounts;
    }

    /**
     * Get site's teams.
     * 
     * @param contest
     * @param siteNumber
     * @return
     */
    protected Account[] getTeamAccounts(IInternalContest contest, int siteNumber) {
        Vector<Account> accountVector = contest.getAccounts(ClientType.Type.TEAM, siteNumber);
        Account[] accounts = (Account[]) accountVector.toArray(new Account[accountVector.size()]);
        Arrays.sort(accounts, new AccountComparator());

        return accounts;
    }

    /**
     * Create N runs identical to input run, add elapsed time.
     * 
     * @param contest
     * @param numberRuns
     * @param run
     * @return
     */
    public Run[] cloneRun(IInternalContest contest, int numberRuns, Run run) {

        Run[] runs = new Run[numberRuns];
        Problem problem = contest.getProblem(run.getProblemId());
        Language language = contest.getLanguage(run.getLanguageId());
        ClientId teamId = run.getSubmitter();

        System.out.println("debug22Z " + run + " " + run.getProblemId() + " " + run.getLanguageId());

        for (int i = 0; i < numberRuns; i++) {
            Run newRun = new Run(teamId, language, problem);
            newRun.setElapsedMins(run.getElapsedMins() + 9 + i);
            newRun.setNumber(contest.getRuns().length);
            System.out.println("debug22Z " + run + " " + run.getProblemId() + " " + run.getLanguageId());
            runs[i] = newRun;
        }

        return runs;
    }

    public Run[] createRandomRuns(IInternalContest contest, int numberRuns, boolean randomTeam, boolean randomProblem, boolean randomLanguage, int siteNumber) {

        Run[] runs = new Run[numberRuns];

        Account[] accounts = getTeamAccounts(contest);
        Language[] languages = contest.getLanguages();
        Problem[] problems = contest.getProblems();

        int numRuns = contest.getRuns().length;

        if (siteNumber != 0) {
            accounts = getTeamAccounts(contest, siteNumber);
        }

        if (accounts.length == 0) {
            new Exception("No accounts for site " + siteNumber).printStackTrace();
            return new Run[0];
        }

        for (int i = 0; i < numberRuns; i++) {
            Problem problem = problems[0];
            Language language = languages[0];
            ClientId teamId = accounts[0].getClientId();

            if (randomTeam) {
                int randomLangIndex = random.nextInt(languages.length);
                language = (Language) languages[randomLangIndex];
            }

            if (randomTeam) {
                int randomProblemIndex = random.nextInt(problems.length);
                problem = (Problem) problems[randomProblemIndex];
            }

            if (randomTeam) {
                int randomTeamIndex = random.nextInt(accounts.length);
                teamId = accounts[randomTeamIndex].getClientId();
            }

            Run run = new Run(teamId, language, problem);
            run.setElapsedMins(9 + i);
            run.setNumber(++numRuns);
            runs[i] = run;
        }
        return runs;

    }

    /**
     * Generate a number of new runs.
     * 
     * @param contest
     * @param numberRuns
     * @param randomTeam
     * @param randomProblem
     * @param randomLanguage
     * @return
     */
    public Run[] createRandomRuns(IInternalContest contest, int numberRuns, boolean randomTeam, boolean randomProblem, boolean randomLanguage) {
        return createRandomRuns(contest, numberRuns, randomTeam, randomProblem, randomLanguage, 0);
    }

    /**
     * Create new run and add to contest run list.
     * 
     * @param contest
     * @param clientId
     * @param language
     * @param problem
     * @param elapsed
     * @return
     */
    public Run createRun(IInternalContest contest, ClientId clientId, Language language, Problem problem, long elapsed) {
        int numRuns = contest.getRuns().length;
        Run run = new Run(clientId, language, problem);
        run.setElapsedMins(elapsed);
        run.setNumber(++numRuns);
        try {
            contest.addRun(run);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FileSecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return run;
    }

    /**
     * Create new run and add to contest run list.
     * 
     * @param contest
     * @param clientId
     * @param problem
     * @return
     * @throws FileSecurityException
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public Run createRun(IInternalContest contest, ClientId clientId, Problem problem) throws IOException, ClassNotFoundException, FileSecurityException {
        int numRuns = contest.getRuns().length;
        Run run = new Run(clientId, contest.getLanguages()[0], problem);
        run.setElapsedMins(9 + numRuns);
        run.setNumber(++numRuns);
        contest.addRun(run);
        return run;
    }

    /**
     * Create a copy of the run (not a clone, but close) and add to contest run list.
     * 
     * This references the input run's JugementRecords instead of cloning them. This run will have a different getElementId() and a getNumber() which represents the next run number.
     * 
     * @param contest
     * @param run
     * @param cloneJudgements
     * @return
     * @throws FileSecurityException
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public Run copyRun(IInternalContest contest, Run run, boolean cloneJudgements) throws IOException, ClassNotFoundException, FileSecurityException {
        Run newRun = new Run(run.getSubmitter(), contest.getLanguage(run.getLanguageId()), contest.getProblem(run.getProblemId()));
        newRun.setElapsedMS(run.getElapsedMS());
        newRun.setDeleted(run.isDeleted());
        newRun.setNumber(contest.getRuns().length);
        newRun.setStatus(RunStates.NEW);

        if (cloneJudgements) {
            for (JudgementRecord judgementRecord : newRun.getAllJudgementRecords()) {
                newRun.addJudgement(judgementRecord);
            }
            newRun.setStatus(run.getStatus());
        }
        contest.addRun(newRun);
        return newRun;
    }

    public Site createSite(int nextSiteNumber, String hostName, int port) {
        return createSite(nextSiteNumber, hostName, port, null);
    }

    public Site createSite(int nextSiteNumber, String hostName, int port, String siteName) {
        if (siteName == null) {
            siteName = new String("Site " + nextSiteNumber);
        }
        Site site = new Site("Site " + nextSiteNumber, nextSiteNumber);
        Properties props = new Properties();
        props.put(Site.IP_KEY, hostName);
        props.put(Site.PORT_KEY, "" + port);
        site.setConnectionInfo(props);
        site.setPassword("site" + nextSiteNumber);
        return site;
    }

    public Site createSite(IInternalContest contest, String siteName) {
        int nextSiteNumber = contest.getSites().length + 1;
        int newPortNumber = DEFAULT_PORT_NUMBER + (nextSiteNumber - 1) * 1000;
        Site site = createSite(nextSiteNumber, "localhost", newPortNumber, siteName);
        return site;
    }

    public Site[] createSites(IInternalContest contest, int count) {
        Site[] sites = new Site[count];
        for (int i = 0; i < count; i++) {
            int nextSiteNumber = contest.getSites().length + i + 1;
            int newPortNumber = DEFAULT_PORT_NUMBER + (nextSiteNumber - 1) * 1000;
            Site site = createSite(nextSiteNumber, "localhost", newPortNumber, null);
            sites[i] = site;
        }
        return sites;
    }

    /**
     * Create profiles but do not add profiles to contest.
     * 
     * @param contest
     * @param count
     * @return
     */
    public Profile[] createProfiles(IInternalContest contest, int count) {
        Profile[] profiles = new Profile[count];
        for (int i = 0; i < count; i++) {
            int nextProfileNumber = contest.getProfiles().length + i + 1;
            Profile profile = new Profile("Profile " + nextProfileNumber);
            profile.setDescription("Created Profile " + nextProfileNumber);
            profiles[i] = profile;
        }
        return profiles;
    }

    /**
     * add a judged run to list of runs in a contest.
     * 
     * Fields in runInfoLine:
     * 
     * <pre>
     * 0 - run id, int
     * 1 - team id, int
     * 2 - problem letter, char
     * 3 - elapsed, int
     * 4 - solved, String &quot;Yes&quot; or No (or full No judgement text)
     * 5 - send to teams, Yes or No
     * 
     * Example:
     * &quot;6,5,A,12,Yes&quot;
     * &quot;6,5,A,12,Yes,Yes&quot;
     * 
     * </pre>
     * 
     * @param contest
     * @param runInfoLine
     * @throws FileSecurityException
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public Run addARun(InternalContest contest, String runInfoLine) throws IOException, ClassNotFoundException, FileSecurityException {

        // get last judge
        Account[] accounts = (Account[]) contest.getAccounts(Type.JUDGE).toArray(new Account[contest.getAccounts(Type.JUDGE).size()]);
        ClientId judgeId = accounts[accounts.length - 1].getClientId();

        Problem[] problemList = contest.getProblems();
        Language languageId = contest.getLanguages()[0];

        Judgement yesJudgement = contest.getJudgements()[0];
        Judgement noJudgement = contest.getJudgements()[1];

        String[] data = runInfoLine.split(",");

        // Line is: runId,teamId,problemLetter,elapsed,solved[,sendToTeamsYN]

        int runId = getIntegerValue(data[0]);
        int teamId = getIntegerValue(data[1]);
        String probLet = data[2];
        int elapsed = getIntegerValue(data[3]);

        boolean solved = data[4].equals("Yes");

        boolean sendToTeams = true;
        if (data.length > 5) {
            sendToTeams = data[5].equals("Yes");
        }

        int problemIndex = probLet.charAt(0) - 'A';
        Problem problem = problemList[problemIndex];
        ClientId clientId = new ClientId(contest.getSiteNumber(), Type.TEAM, teamId);

        Run run = new Run(clientId, languageId, problem);
        run.setNumber(runId);
        run.setElapsedMins(elapsed);

        // Use a default No entry
        ElementId judgementId = noJudgement.getElementId();

        if (solved) {
            judgementId = yesJudgement.getElementId();
        } else {

            // Try to find No judgement

            for (Judgement judgement : contest.getJudgements()) {
                if (judgement.toString().equalsIgnoreCase(data[5])) {
                    judgementId = judgement.getElementId();
                }
            }
        }
        JudgementRecord judgementRecord = new JudgementRecord(judgementId, judgeId, solved, false);
        judgementRecord.setSendToTeam(sendToTeams);
        contest.addRun(run);

        checkOutRun(contest, run, judgeId);
        contest.addRunJudgement(run, judgementRecord, null, judgeId);

        if (debugMode) {
            System.out.print("Send to teams " + run.getJudgementRecord().isSendToTeam() + " ");
            System.out.println("Added run " + run);
        }

        return contest.getRun(run.getElementId());

    }

    private boolean isSolved(IInternalContest contest, ElementId judgementId) {
        return contest.getJudgements()[0].getElementId().equals(judgementId);
    }

    /**
     * Add a judgement to a run.
     * 
     * @param contest
     * @param run
     * @param judgement
     * @param judgeId
     * @return run with judgement.
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws FileSecurityException
     */
    public Run addJudgement(IInternalContest contest, Run run, Judgement judgement, ClientId judgeId) throws IOException, ClassNotFoundException, FileSecurityException {

        ElementId judgementId = judgement.getElementId();
        boolean solved = isSolved(contest, judgementId);

        JudgementRecord judgementRecord = new JudgementRecord(judgementId, judgeId, solved, false);

        checkOutRun(contest, run, judgeId);
        contest.addRunJudgement(run, judgementRecord, null, judgeId);

        return contest.getRun(run.getElementId());
    }

    private void checkOutRun(IInternalContest contest, Run run, ClientId judgeId) {
        try {
            contest.checkoutRun(run, judgeId, false, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Assign a color to a problem.
     * 
     * @param contest
     * @param problem
     * @param color
     * @param rgbColor
     */
    public void assignColor(IInternalContest contest, Problem problem, String color, String rgbColor) {

        BalloonSettings balloonSettings = contest.getBalloonSettings(contest.getSiteNumber());

        if (balloonSettings == null) {
            balloonSettings = new BalloonSettings("BalloonSettingsSite" + contest.getSiteNumber(), contest.getSiteNumber());
        }

        balloonSettings.addColor(problem, color, rgbColor);
        contest.updateBalloonSettings(balloonSettings);
    }


    /**
     * 
     * Creates scoreboard client is missing.
     *  
     * @param contest
     * @return
     */
    protected ClientSettings getNotificationSettings(IInternalContest contest) {

        BalloonSettings balloonSettings = contest.getBalloonSettings(contest.getSiteNumber());
        if (balloonSettings == null) {
            balloonSettings = new BalloonSettings("ballonSet1", contest.getSiteNumber());
            Account account = insureAndGetClient(contest, Type.SCOREBOARD);
            balloonSettings.setBalloonClient(account.getClientId());
            contest.addBalloonSettings(balloonSettings);
        }
        
        ClientId id = balloonSettings.getBalloonClient();

        ClientSettings clientSettings = contest.getClientSettings(id);
        if (clientSettings == null) {
            clientSettings = new ClientSettings(id);
            contest.updateClientSettings(clientSettings);
        }

        return clientSettings;
    }

    /**
     * Insures at least one client and returns a client.
     * 
     * @param contest
     * @param type
     * @return account for type
     */
    private Account insureAndGetClient(IInternalContest contest, Type type) {
        Vector<Account> accounts = contest.getAccounts(type, contest.getSiteNumber());
        Account account = null;
        if (accounts.size() == 0) {
            contest.generateNewAccounts(type.toString(), 1, true);
            account = contest.getAccounts(type, contest.getSiteNumber()).firstElement();
        } else {
            account = accounts.firstElement();
        }

        return account;
    }

    /**
     * Returns key for balloon.
     * 
     * <pre>
     * from BalloonHandler.getBalloonKey(who, problemId)
     * </pre>
     * 
     * TODO this should be in a utility class or a static method somewhere dal
     * 
     * @param who
     * @param problemId
     * @return
     */
    public String getBalloonKey(ClientId who, ElementId problemId) {
        return who.getTripletKey() + " " + problemId.toString();
    }

    /**
     * Add a ballon notification.
     * 
     * @param contest
     * @param scoreboardClientId
     * @param run
     */
    public void addBalloonNotification(IInternalContest contest, Run run) {

        ClientSettings settings = getNotificationSettings(contest);

        BalloonDeliveryInfo deliveryInfo = new BalloonDeliveryInfo(run.getSubmitter(), run.getProblemId(), Calendar.getInstance().getTime().getTime());

        String balloonKey = getBalloonKey(run.getSubmitter(), run.getProblemId());

        Hashtable<String, BalloonDeliveryInfo> hashtable = settings.getBalloonList();
        hashtable.put(balloonKey, deliveryInfo);
        settings.setBalloonList(hashtable);
    }

    private int getIntegerValue(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Add runs to contest (via acceptRun)
     * 
     * @param contest
     * @param runs
     * @param filename
     *            name of file to submit
     * @throws FileSecurityException
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public void addRuns(IInternalContest contest, Run[] runs, String filename) throws IOException, ClassNotFoundException, FileSecurityException {

        if (!new File(filename).exists()) {
            throw new IllegalArgumentException("filename is null");
        }

        for (Run run : runs) {
            RunFiles runFiles = new RunFiles(run, filename);
            contest.acceptRun(run, runFiles);
        }
    }

    /**
     * Print the report to the filename.
     * 
     * @param filename
     * @param selectedReport
     * @param filter
     * @param inContest
     * @param inController
     */
    public void printReport(String filename, IReport selectedReport, Filter filter, IInternalContest inContest, IInternalController inController) {

        if (filter == null) {
            filter = new Filter();
        }

        try {
            selectedReport.setContestAndController(inContest, inController);
            selectedReport.setFilter(filter);
            selectedReport.createReportFile(filename, filter);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Judgement getYesJudgement(IInternalContest contest) {
        return contest.getJudgements()[0];
    }

    public int getDefaultPortNumber() {
        return defaultPortNumber;
    }

    public void setDefaultPortNumber(int defaultPortNumber) {
        this.defaultPortNumber = defaultPortNumber;
    }

    public Judgement getRandomJudgement(IInternalContest contest, boolean solved) {

        Judgement[] judgements = contest.getJudgements();

        if (solved) {
            return judgements[0];
        } else {
            int randomJudgement = random.nextInt(judgements.length - 1);
            return judgements[randomJudgement + 1];
        }
    }

    /**
     * Return Sumit source file in test area.
     * 
     * @see #getSampleFile(String)
     * @return
     */
    public String getSampleFile() {
        return getSampleFile("Sumit.java");
    }

    /**
     * Return full filename in file test directory.
     * 
     * Will print Exceptions if test directory is not present or if no such filename found.
     * 
     * @param filename
     * @return filename with path to test data.
     */
    public String getSampleFile(String filename) {
        String testDir = "testdata";
        String projectPath = JUnitUtilities.locate(testDir);
        if (projectPath == null) {
            new Exception("Unable to locate " + testDir).printStackTrace(System.err);
        }
        testDir = projectPath + File.separator + testDir + File.separator;
        String testfilename = testDir + filename;

        if (!new File(testfilename).isFile()) {
            new Exception("No sample file found " + testfilename).printStackTrace(System.err);
        }
        return testfilename;
    }

}
