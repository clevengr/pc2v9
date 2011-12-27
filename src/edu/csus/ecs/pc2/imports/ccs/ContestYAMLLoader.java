package edu.csus.ecs.pc2.imports.ccs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.Vector;

import edu.csus.ecs.pc2.core.Utilities;
import edu.csus.ecs.pc2.core.exception.YamlLoadException;
import edu.csus.ecs.pc2.core.list.AccountList;
import edu.csus.ecs.pc2.core.list.AccountList.PasswordType;
import edu.csus.ecs.pc2.core.model.Account;
import edu.csus.ecs.pc2.core.model.AutoJudgeSetting;
import edu.csus.ecs.pc2.core.model.Category;
import edu.csus.ecs.pc2.core.model.ClientId;
import edu.csus.ecs.pc2.core.model.ClientType;
import edu.csus.ecs.pc2.core.model.ContestInformation;
import edu.csus.ecs.pc2.core.model.Filter;
import edu.csus.ecs.pc2.core.model.IInternalContest;
import edu.csus.ecs.pc2.core.model.InternalContest;
import edu.csus.ecs.pc2.core.model.Language;
import edu.csus.ecs.pc2.core.model.LanguageAutoFill;
import edu.csus.ecs.pc2.core.model.Problem;
import edu.csus.ecs.pc2.core.model.ProblemDataFiles;
import edu.csus.ecs.pc2.core.model.SerializedFile;
import edu.csus.ecs.pc2.core.model.Site;

/**
 * Create contest from YAML.
 * 
 * @author pc2@ecs.csus.edu
 * @version $Id: ContestYAMLLoader.java 225 2011-09-02 05:22:43Z laned $
 */

// TODO CCS REALLY IMPORTANT USER INTERFACE WORK - MUST SHOW SYNTAX ERRORS AND INPUT FILE PROBLEMS TO USER!!!

// $HeadURL: http://pc2.ecs.csus.edu/repos/v9sandbox/trunk/src/edu/csus/ecs/pc2/imports/ccs/ContestYAMLLoader.java $
public class ContestYAMLLoader {

    public static final String CONTEST_NAME_KEY = "name";

    public static final String CONTEST_SHORT_NAME = "short-name";

    public static final String CONTEST_START_TIME = "start-time";

    public static final String CONTEST_DURATION = "duration";

    public static final String SCOREBOARD_FREEZE = "scoreboard-freeze";

    public static final String LANGUAGE_KEY = "languages";

    public static final String DEFAULT_CLARS_KEY = "default-clars";

    public static final String CLAR_CATEGORIES_KEY = "clar-categories";

    public static final String PROBLEMS_KEY = "problemset";

    public static final String ACCOUNTS_KEY = "accounts";

    public static final String SITES_KEY = "sites";
    
    /**
     * Run execution time limit, in seconds.
     */
    public static final String TIMEOUT_KEY = "timeout";

    public static final String LIMITS_KEY = "limits";

    private static final String DELIMIT = ":";

    private static final String DEFAULT_CONTEST_YAML_FILENAME = "contest.yaml";

    private static final String DEFAULT_PROBLEM_YAML_FILENAME = "problem.yaml";

    /**
     * problemName key in problem.yaml
     */
    private static final String PROBLEM_NAME_KEY = "name";

    public static final String DEFAULT_INTERNATIONAL_VALIDATOR_COMMAND = "{:validator} {:infile} {:outfile} {:ansfile} {:resfile} ";

    /**
     * Problem input (data files) key.
     */
    private static final String PROBLEM_INPUT_KEY = "input";

    /**
     * Default time out for run execution.
     */
    public static final int DEFAULT_TIME_OUT = 30;

    public static final String AUTO_JUDGE_KEY = "auto-judging";

    /**
     * Load contest.yaml from directory.
     * 
     * @see #fromYaml(IInternalContest, String[], String)
     * 
     * @param contest
     * @param diretoryName
     *            directory to load files from.
     * @return contest
     * @throws Exception
     */
    public IInternalContest fromYaml(IInternalContest contest, String diretoryName) throws Exception {
        String[] contents = Utilities.loadFile(diretoryName + File.separator + DEFAULT_CONTEST_YAML_FILENAME);
        return fromYaml(contest, contents, diretoryName);
    }
    
    /**
     * Get title from YAML file.
     * 
     * @param contestYamlFilename
     * @return
     * @throws IOException
     */
    public String getContestTitle(String contestYamlFilename ) throws IOException {
        String[] contents = Utilities.loadFile(contestYamlFilename);
        String contestTitle = getSequenceValue(contents, CONTEST_NAME_KEY);
        return contestTitle;
    }

    /**
     * Load/Create contest from YAML lines.
     * 
     * @param contest
     *            update/overwrite contest, if null creates new contest.
     * @param yamlLines
     *            lines from YAML file
     * @return
     */
    public IInternalContest fromYaml(IInternalContest contest, String[] yamlLines, String diretoryName) throws Exception {

        if (contest == null) {
            contest = new InternalContest();
            contest.setSiteNumber(1);
        }

        // name: ACM-ICPC World Finals 2011
        
        String contestTitle = getSequenceValue(yamlLines, CONTEST_NAME_KEY);
        if (contestTitle != null) {
            setTitle(contest, contestTitle);
        }
        
        int defaultTimeout =  getIntegerValue(getSequenceValue(yamlLines, TIMEOUT_KEY), DEFAULT_TIME_OUT);

        for (String line : yamlLines) {
            if (line.startsWith(CONTEST_NAME_KEY + DELIMIT)) {
                setTitle(contest, line.substring(line.indexOf(DELIMIT) + 1).trim());
                
            }
        }

        // TODO CCS add settings
        // short-name: ICPC WF 2011
        // start-time: 2011-02-04 01:23Z
        // duration: 5:00:00
        // scoreboard-freeze: 4:00:00

        Language[] languages = getLanguages(yamlLines);
        for (Language language : languages) {
            contest.addLanguage(language);
        }

        Problem[] problems = getProblems(yamlLines, defaultTimeout);
        for (Problem problem : problems) {
            addProblemDefAndFiles(contest, diretoryName, problem);
            
            // TODO CCS add validator(s)
            assignDefaultValidator(problem);
            problem.setComputerJudged(true);
        }
        
        if ( getSectionLines(AUTO_JUDGE_KEY, yamlLines).length == 0){
            System.err.println("No "+AUTO_JUDGE_KEY+" section in "+diretoryName);
        }
 
        Site[] sites = getSites(yamlLines);
        for (Site site : sites) {
            contest.addSite(site);
        }

        String[] categories = loadGeneralClarificationAnswers(yamlLines);
        for (String name : categories) {
            contest.addCategory(new Category(name));
        }
         
        // String[] answers = getGeneralAnswers(yamlLines);
        // TODO CCS load answers into contest

        Account[] accounts = getAccounts(yamlLines);
        contest.addAccounts(accounts);
        
        AutoJudgeSetting[] autoJudgeSettings = getAutoJudgeSettings(yamlLines, problems);

        for (AutoJudgeSetting auto : autoJudgeSettings) {
            addAutoJudgeSetting(contest,auto);
        }
        
        return contest;
    }

    private void addAutoJudgeSetting(IInternalContest contest, AutoJudgeSetting auto) throws YamlLoadException {
        
//        ClientId clientId = auto.getClientId();
        
        Account account = contest.getAccount(auto.getClientId());
        if (account == null) {
            throw new YamlLoadException("No such account for auto judge setting, undefined account is "+auto.getClientId());
        }
        
        // TODO 669 code  contest.addAutoJudgeSetting (auto);
    }

    public Site[] getSites(String[] yamlLines) {

        String[] sectionLines = getSectionLines(SITES_KEY, yamlLines);

        Vector<Site> sitesVector = new Vector<Site>();

        int idx = 1;
        String[] sequenceLines = getNextSequence(sectionLines, idx);

        while (sequenceLines.length > 0) {

            /*
             * <pre> sites: - number: 1 name: Site 1 IP: localhost port: 50002 </pre>
             */

            String siteNumberString = getSequenceValue(sequenceLines, "- number");
            String siteTitle = getSequenceValue(sequenceLines, "name").trim();
            
            int siteNumber = getIntegerValue(siteNumberString.trim(), 0);

            Site site = new Site(siteTitle, siteNumber);

            String hostName = getSequenceValue(sequenceLines, "IP").trim();
            String portString = getSequenceValue(sequenceLines, "port").trim();

            String password = getSequenceValue(sequenceLines, "password");
            if (password == null) {
                password = "site" + siteNumberString;
            }
            site.setPassword(password.trim());

            Properties props = new Properties();
            props.put(Site.IP_KEY, hostName);
            props.put(Site.PORT_KEY, portString);
            site.setConnectionInfo(props);

            sitesVector.addElement(site);

            idx += sequenceLines.length;
            sequenceLines = getNextSequence(sectionLines, idx);
        }

        return (Site[]) sitesVector.toArray(new Site[sitesVector.size()]);

    }

    private Account[] getAccounts(String[] yamlLines) {

        String[] sectionLines = getSectionLines(ACCOUNTS_KEY, yamlLines);

        Vector<Account> accountVector = new Vector<Account>();

        AccountList accountList = new AccountList();

        int idx = 1;
        String[] sequenceLines = getNextSequence(sectionLines, idx);

        while (sequenceLines.length > 0) {

            // * - account: TEAM

            String accountType = getSequenceValue(sequenceLines, "- account");
            ClientType.Type type = ClientType.Type.valueOf(accountType.trim());

            String countString = getSequenceValue(sequenceLines, "count").trim();
            int count = getIntegerValue(countString, 1);
            String siteString = getSequenceValue(sequenceLines, "site");
            int siteNumber = getIntegerValue(siteString, 1);

            /**
             * <pre>
             * 
             * accounts:
             *   -account: TEAM
             *       site: 1
             *      count: 14
             * 
             *   -account: JUDGE
             *       site: 1
             *      count: 12
             * </pre>
             */

            Vector<Account> newAccounts = accountList.generateNewAccounts(type, count, PasswordType.JOE, siteNumber, true);
            accountVector.addAll(newAccounts);

            idx += sequenceLines.length;
            sequenceLines = getNextSequence(sectionLines, idx);
        }

        return (Account[]) accountVector.toArray(new Account[accountVector.size()]);
    }

    /**
     * Add data contents into problem.
     * @param contest
     * @param directoryName
     * @param problem
     * @param defaultTimeout 
     * @throws Exception
     * @throws Exception
     */
    private void addProblemDefAndFiles(IInternalContest contest, String directoryName, Problem problem) throws Exception {

        String problemYamlFilename = directoryName + File.separator + problem.getShortName() + File.separator + DEFAULT_PROBLEM_YAML_FILENAME;
        String[] contents = Utilities.loadFile(problemYamlFilename);

        if (contents.length == 0) {
            throw new Exception("Can not load problem.yaml: " + problemYamlFilename);
        }

        String problemName = getSequenceValue(contents, PROBLEM_NAME_KEY);
        if (problemName != null) {
            problem.setDisplayName(problemName);
        }

        String[] sectionLines = getSectionLines(PROBLEM_INPUT_KEY, contents);

        String dataFileBaseDirectory = directoryName + File.separator + problem.getShortName() + File.separator + "data" + File.separator + "secret";

        if (sectionLines.length > 1) {
            loadPc2Problem(contest, dataFileBaseDirectory, problem, sectionLines);
        } else {
            loadCCSProblem(contest, dataFileBaseDirectory, problem);
        }
        
        sectionLines = getSectionLines(LIMITS_KEY, contents);
        if (sectionLines.length > 1) {
            String timeOut = getSequenceValue(sectionLines, TIMEOUT_KEY);
            if (timeOut != null) {
                problem.setTimeOutInSeconds(Integer.parseInt(timeOut.trim()));
            }
        }

    }

    private void loadCCSProblem(IInternalContest contest, String dataFileBaseDirectory, Problem problem) throws Exception {

        ProblemDataFiles problemDataFiles = new ProblemDataFiles(problem);

        String[] inputFileNames = getFileNames(dataFileBaseDirectory, ".in");

        String[] answerFileNames = getFileNames(dataFileBaseDirectory, ".ans");

        if (inputFileNames.length == 0) {
            throw new Exception("No input file names found for " + problem.getDisplayName());
        }

        if (answerFileNames.length == 0) {
            throw new Exception("No input file names found for " + problem.getDisplayName());
        }

        if (inputFileNames.length == answerFileNames.length) {

            SerializedFile[] serializedFileDataFiles = new SerializedFile[inputFileNames.length];
            SerializedFile[] serializedFileAnswerFiles = new SerializedFile[inputFileNames.length];

            for (int idx = 0; idx < inputFileNames.length; idx++) {

                problem.addTestCaseFilenames(inputFileNames[idx], answerFileNames[idx]);

                String dataFileName = dataFileBaseDirectory + File.separator + inputFileNames[idx];

                String answerFileName = dataFileName.replaceAll(".in$", ".ans");
                String answerShortFileName = inputFileNames[idx].replaceAll(".in$", ".ans");

                checkForFile(dataFileName, "Missing "+inputFileNames[idx]+" file for " + problem.getShortName());
                checkForFile(answerFileName, "Missing "+answerShortFileName+" file for " + problem.getShortName());

                serializedFileDataFiles[idx] = new SerializedFile(dataFileName);
                serializedFileAnswerFiles[idx] = new SerializedFile(answerFileName);
            }

            problemDataFiles.setJudgesDataFiles(serializedFileDataFiles);
            problemDataFiles.setJudgesAnswerFiles(serializedFileAnswerFiles);

        } else {
            throw new Exception("  For " + problem.getShortName() + " Missing files -  there are " + inputFileNames.length + " .in files and " + //
                    answerFileNames.length + " .ans files ");
        }
        
        contest.addProblem(problem, problemDataFiles);
    }

    /**
     * Check for existence of file, if does not exist throw exception with message.
     * 
     * @param filename
     * @param message
     * @throws Exception 
     */
    private void checkForFile(String filename, String message) throws Exception {
        
        if (! (new File(filename).isFile())) {
            throw new Exception (message);
        }
    }

    /**
     * Get list of filenames with extension in directory, return in sorted order.
     * 
     * @param directoryName
     * @param extension
     * @return
     */
    protected String[] getFileNames(String directoryName, String extension) {
        
        Vector<String> list = new Vector<String>();
        File dir = new File (directoryName);
        
        String [] entries = dir.list();
        Arrays.sort(entries);
        
        for (String name : entries){
            if (name.endsWith(extension)){
                list.addElement(name);
            }
        }
        
        return (String[]) list.toArray(new String[list.size()]);
    }

    private void loadPc2Problem (IInternalContest contest, String dataFileBaseDirectory, Problem problem, String[] sectionLines) throws Exception {
        
        String dataFileName = getSequenceValue(sectionLines, "datafile");
        String answerFileName = getSequenceValue(sectionLines, "answerfile");

        ProblemDataFiles problemDataFiles = new ProblemDataFiles(problem);
    
        if (dataFileName != null || answerFileName != null) {
            
            addDataFiles (problem, problemDataFiles, dataFileBaseDirectory, dataFileName, answerFileName);

            contest.addProblem(problem, problemDataFiles);

        } else {
            contest.addProblem(problem);
        }
        
    }

    private void addDataFiles(Problem problem, ProblemDataFiles problemDataFiles, String dataFileBaseDirectory, String dataFileName, String answerFileName) throws Exception {

        // load judge data file
        if (dataFileName != null) {
            String dataFilePath = dataFileBaseDirectory + File.separator + dataFileName;
            if (fileNotThere(dataFilePath)) {
                throw new Exception("Missing data file " + dataFilePath);
            }

            problem.setDataFileName(dataFileName);
            problem.setReadInputDataFromSTDIN(false);

            SerializedFile serializedFile = new SerializedFile(dataFilePath);
            problemDataFiles.setJudgesDataFile(serializedFile);
        }

        // load judge answer file
        if (answerFileName != null) {
            String answerFilePath = dataFileBaseDirectory + File.separator + answerFileName;
            if (fileNotThere(answerFilePath)) {
                throw new Exception("Missing data file " + answerFilePath);
            }

            problem.setAnswerFileName(answerFileName);

            SerializedFile serializedFile = new SerializedFile(answerFilePath);
            problemDataFiles.setJudgesAnswerFile(serializedFile);
        }
        
    }

    private boolean fileNotThere(String name) {
        return !new File(name).isFile();
    }

    private void assignDefaultValidator(Problem problem) {
        addInternalValidator(problem, 1);

    }

    public Problem addInternalValidator(Problem problem, int optionNumber) {

        problem.setValidatedProblem(true);
        problem.setUsingPC2Validator(true);
        problem.setWhichPC2Validator(optionNumber);
        problem.setIgnoreSpacesOnValidation(true);

        problem.setValidatorCommandLine(DEFAULT_INTERNATIONAL_VALIDATOR_COMMAND + " -pc2 " + problem.getWhichPC2Validator() + " " + problem.isIgnoreSpacesOnValidation());
        problem.setValidatorProgramName(Problem.INTERNAL_VALIDATOR_NAME);

        return problem;

    }

    /**
     * Load list of strings.
     * 
     * @param key
     * @param yamlLines
     * @return
     */
    public String[] loadStringList(String key, String[] yamlLines) {
        String[] sectionLines = getSectionLines(key, yamlLines);

        int idx = 1;
        String[] sequenceLines = getNextSequence(sectionLines, idx);

        Vector<String> outArrays = new Vector<String>();

        while (sequenceLines.length > 0) {
            String line = sequenceLines[0].trim();
            if (line.startsWith("-")) {
                line = line.substring(1);
            }
            outArrays.addElement(line.trim());
            idx += sequenceLines.length;
            sequenceLines = getNextSequence(sectionLines, idx);
        }
        return (String[]) outArrays.toArray(new String[outArrays.size()]);

    }

    public String[] loadGeneralClarificationAnswers(String[] yamlLines) {
        return loadStringList(CLAR_CATEGORIES_KEY, yamlLines);
    }

    public String[] getGeneralAnswers(String[] yamlLines) {
        return loadStringList(DEFAULT_CLARS_KEY, yamlLines);
    }

    /**
     * Get list of Clarification Categories from YAML.
     * 
     * @param yamlLines
     * @return
     */
    public String[] getClarificationCategories(String[] yamlLines) {
        String[] sectionLines = getSectionLines(CLAR_CATEGORIES_KEY, yamlLines);

        int idx = 1;
        String[] sequenceLines = getNextSequence(sectionLines, idx);

        Vector<String> outArrays = new Vector<String>();

        while (sequenceLines.length > 0) {
            String line = sequenceLines[0].trim();
            if (line.startsWith("-")) {
                line = line.substring(1);
            }
            outArrays.addElement(line.trim());
            idx += sequenceLines.length;
            sequenceLines = getNextSequence(sectionLines, idx);
        }
        return (String[]) outArrays.toArray(new String[outArrays.size()]);
    }

    /**
     * Get/create {@link Language}s from YAML lines.
     * 
     * @param yamlLines
     * @return list of {@link Language}s
     * @throws YamlLoadException 
     */
    public Language[] getLanguages(String[] yamlLines) throws YamlLoadException {

        String[] sectionLines = getSectionLines(LANGUAGE_KEY, yamlLines);

        Vector<Language> languageList = new Vector<Language>();

        int idx = 1;
        String[] sequenceLines = getNextSequence(sectionLines, idx);

        while (sequenceLines.length > 0) {
            String name = getSequenceValue(sequenceLines, "name");

            if (name == null) {
                syntaxError("Language name field missing in languages section");
            } else {
                Language language = new Language(name);
                
                Language lookedupLanguage = LanguageAutoFill.languageLookup (name);
                String compilerName = getSequenceValue(sequenceLines, "compilerCmd");
                
                if (compilerName == null && lookedupLanguage != null){
                    language = lookedupLanguage;
                    language.setDisplayName(name);
                 } else if (compilerName == null) {
                     throw new YamlLoadException("Language \""+name+"\" missing compiler command line");
                 } else {
                
                    String compilerArgs = getSequenceValue(sequenceLines, "compiler-args");
                    String interpreter = getSequenceValue(sequenceLines, "runner");
                    String interpreterArgs = getSequenceValue(sequenceLines, "runner-args");
                    String exeMask = getSequenceValue(sequenceLines, "exemask");
                    // runner + runner-args, so what is execCmd for ?
//                    String execCmd = getSequenceValue(sequenceLines, "execCmd");

                    if (compilerArgs == null) {
                        language.setCompileCommandLine(compilerName);
                    } else {
                        language.setCompileCommandLine(compilerName + " " + compilerArgs);
                    }
                    language.setExecutableIdentifierMask(exeMask);

                    String programExecuteCommandLine = null;
                    if (interpreter == null) {
                        programExecuteCommandLine = "a.out";
                    } else {
                        if (interpreterArgs == null) {
                            programExecuteCommandLine = interpreter;
                        } else {
                            programExecuteCommandLine = interpreter + " " + interpreterArgs;
                        }
                    }
                    language.setProgramExecuteCommandLine(programExecuteCommandLine);
                }
                
                String activeStr = getSequenceValue(sequenceLines, "active");
                boolean active = getBooleanValue(activeStr, true);
                language.setActive(active);

                // TODO handle interpreted languages, seems it should be in the export
                
                if (valid(language, name)){
                    languageList.addElement(language);
                }

            }

            idx += sequenceLines.length;
            sequenceLines = getNextSequence(sectionLines, idx);
        }

        return (Language[]) languageList.toArray(new Language[languageList.size()]);
    }

    private boolean valid(Language language, String prefix) throws YamlLoadException {
        checkField(language.getDisplayName(), prefix + " Compiler Display name");
        checkField(language.getCompileCommandLine(), prefix + " Compile Command line");
        return true;
    }

    private void checkField(String field, String fieldName) throws YamlLoadException {
        if (field == null) {
            throw new YamlLoadException("Missing " + fieldName);
        } else if (field.trim().length() == 0) {
            throw new YamlLoadException("Missing " + fieldName);
        }
    }

    /**
     * Get {@link Problem}s from YAML file.
     * 
     * @param yamlLines
     * @param seconds timeout for run execution in seconds
     * @return list of {@link Problem}
     * @throws Exception 
     */
    public Problem[] getProblems(String[] yamlLines, int seconds) throws Exception {

        String[] sectionLines = getSectionLines(PROBLEMS_KEY, yamlLines);

        Vector<Problem> problemList = new Vector<Problem>();

        int idx = 1;
        String[] sequenceLines = getNextSequence(sectionLines, idx);

        while (sequenceLines.length > 0) {

            String problemKeyName = getSequenceValue(sequenceLines, "short-name");
            String problemTitle = problemKeyName;
            /**
             * <pre>
             *  problemset:
             *    - letter:     A
             *      short-name: apl
             *      color:      yellow
             *      rgb:        #ffff00
             * </pre>
             */

            Problem problem = new Problem(problemTitle);
            
            problem.setTimeOutInSeconds(seconds);
            
            problem.setShortName(problemKeyName);

            // String problemLetter = getSequenceValue(sequenceLines, "letter");
            // String colorName = getSequenceValue(sequenceLines, "color");
            // String colorRGB = getSequenceValue(sequenceLines, "rgb");

            // TODO CCS add Problem set
            // problem.setLetter(problemLetter);
            // problem.setColorName(colorName);
            // problem.setColorRGB(colorRGB);

            // debug code
            // System.out.println("Problem   : " + problemKeyName);
            // System.out.println(" letter   : " + problemLetter);
            // System.out.println(" color    : " + colorName);
            // System.out.println(" RGB      : " + colorRGB);
            
            problemList.addElement(problem);

            idx += sequenceLines.length;
            sequenceLines = getNextSequence(sectionLines, idx);
        }

        return (Problem[]) problemList.toArray(new Problem[problemList.size()]);
    }

    private void syntaxError(String string) throws YamlLoadException {
        YamlLoadException exception = new YamlLoadException("Syntax error: " + string);
        exception.printStackTrace();
        throw exception;
    }

    private String getSequenceValue(String[] lines, String key) {
        for (String line : lines) {
            String keyString = key + DELIMIT;
            if (line.trim().startsWith(keyString)) {
                return line.trim().substring(keyString.length()).trim();
            }

            keyString = "- " + key + DELIMIT;
            if (line.trim().startsWith(keyString)) {
                return line.trim().substring(keyString.length()).trim();
            }
        }
        return null;
    }

    public String[] getNextSequence(String[] sectionLines, int idx) {

        Vector<String> lines = new Vector<String>();

        for (int i = idx; i < sectionLines.length; i++) {
            String line = sectionLines[i];
            if (i > idx && line.trim().startsWith("-")) {
                break;
            }
            lines.addElement(line);
        }

        return (String[]) lines.toArray(new String[lines.size()]);
    }

    public boolean isNewSection(String line) {
        if (line == null) {
            return false;
        }
        if (line.indexOf(DELIMIT) > 1) {
            char firstChar = line.charAt(0);
            return !((firstChar == ' ') || (firstChar == '-') || (firstChar == '#'));
        }
        return false;
    }

    /**
     * get all section lines including section name line.
     * 
     * @param key
     * @param yamlLines
     * @return all lines in a section.
     */
    public String[] getSectionLines(String key, String[] yamlLines) {

        Vector<String> lines = new Vector<String>();

        boolean inSection = false;

        for (String line : yamlLines) {

            if (isNewSection(line)) {
                inSection = false;
                if (line.startsWith(key + DELIMIT)) {
                    inSection = true;
                }
            } // no else

            if (inSection) {
                lines.addElement(line);
            }
        }

        return (String[]) lines.toArray(new String[lines.size()]);
    }

    private void setTitle(IInternalContest contest, String title) {
        ContestInformation contestInformation = contest.getContestInformation();
        contestInformation.setContestTitle(title);
    }

    public AutoJudgeSetting[] getAutoJudgeSettings (String[] yamlLines, Problem [] problems) throws YamlLoadException {

        String[] sectionLines = getSectionLines(AUTO_JUDGE_KEY, yamlLines);
        
        ArrayList<AutoJudgeSetting> ajList = new ArrayList<AutoJudgeSetting>();
        
        int idx = 1;
        String[] sequenceLines = getNextSequence(sectionLines, idx);
        
        while (sequenceLines.length > 0) {
            
            String accountType = getSequenceValue(sequenceLines, "- account");
            ClientType.Type type = ClientType.Type.valueOf(accountType.trim());

            String siteString = getSequenceValue(sequenceLines, "site");
            
            int siteNumber = getIntegerValue (siteString, 1);
            
            // TODO 669 check for syntax errors
//            syntaxError(AUTO_JUDGE_KEY + " name field missing in languages section");
            
            String numberString = getSequenceValue(sequenceLines, "number");
            String problemLettersString = getSequenceValue(sequenceLines, "letters");
            
            String activeStr = getSequenceValue(sequenceLines, "active");
            boolean active = getBooleanValue(activeStr, true);
            
            // TODO 669 code load method
            int [] numbers = null;
            if ("all".equalsIgnoreCase(numberString)){
                throw new YamlLoadException("'all' not allowed for judge number");
            } else {
                numbers = getNumberList (numberString.trim());
            }
            
            for (int i = 0; i < numbers.length; i++) {
                int clientNumber = i + 1;

                String name = accountType.toUpperCase() + clientNumber;
                
                AutoJudgeSetting autoJudgeSetting = new AutoJudgeSetting(name);
                ClientId id = new ClientId(siteNumber, type, clientNumber);
                autoJudgeSetting.setClientId(id);
                autoJudgeSetting.setActive(active);
                
                Filter filter = new Filter();
                
                if ("all".equalsIgnoreCase(problemLettersString.trim())){
                    for (Problem problem : problems) {
                        filter.addProblem(problem);
                    }
                } else {
                    for (Problem problem : getProblemsFromLetters(problems, problemLettersString)) {
                        filter.addProblem(problem);
                    }
                }

                autoJudgeSetting.setProblemFilter(filter);
                ajList.add(autoJudgeSetting);
            }
            
            idx += sequenceLines.length;
            sequenceLines = getNextSequence(sectionLines, idx);
        }
        
        return (AutoJudgeSetting[]) ajList.toArray(new AutoJudgeSetting[ajList.size()]);
    }

    protected Problem[] getProblemsFromLetters(Problem[] problems, String problemLettersString) throws YamlLoadException {

        String[] list = problemLettersString.split(",");
        Problem[] out = new Problem[list.length];

        for (int i = 0; i < list.length; i++) {

            char letter = list[i].trim().toUpperCase().charAt(0);
            int offset = letter - 'A';

            if (offset < 0 || offset >= problems.length) {
                throw new YamlLoadException("No problem defined for letter " + letter + " (" + list[i].trim().toUpperCase() + ")");
            }
            out[i] = problems[offset];
        }
        return out;
    }

    protected int[] getNumberList(String numberString) {

        String[] list = numberString.split(",");
        if (list.length == 1) {
            int[] out = new int[1];
            out[0] = getIntegerValue(list[0], 0);
//            if (out[0] < 1) {
//                // TODO 669 throw invalid number in list exception
//            }
            return out;
        } else {
            int[] out = new int[list.length];
            int i = 0;
            for (String n : list) {
                out[i] = getIntegerValue(list[0], 0);
//                if (out[i] < 1) {
//                    // TODO 669 throw invalid number in list exception
//                }
                i++;
            }
            return out;
        }
    }

    private int getIntegerValue(String string, int defaultNumber) {
        
        int number = defaultNumber;
        
        if (string != null && string.length() != 0) {
            number = Integer.parseInt(string);
        }
        
        return number;
    }
    
    private boolean getBooleanValue(String string, boolean defaultBoolean) {
        
        boolean value = defaultBoolean;

        if (string != null && string.length() != 0) {
            string = string.trim();
            if (string.equalsIgnoreCase("yes")) {
                value = true;
            } else if (string.equalsIgnoreCase("no")) {
                value = false;
            } else if (string.equalsIgnoreCase("true")) {
                value = true;
            } else if (string.equalsIgnoreCase("false")) {
                value = false;
            }
        }
        
        return value;
    }
}
