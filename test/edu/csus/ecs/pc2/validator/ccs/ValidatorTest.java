package edu.csus.ecs.pc2.validator.ccs;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

import edu.csus.ecs.pc2.ccs.CCSConstants;
import edu.csus.ecs.pc2.core.IInternalController;
import edu.csus.ecs.pc2.core.execute.Executable;
import edu.csus.ecs.pc2.core.log.NullController;
import edu.csus.ecs.pc2.core.model.ClientId;
import edu.csus.ecs.pc2.core.model.ClientType;
import edu.csus.ecs.pc2.core.model.IInternalContest;
import edu.csus.ecs.pc2.core.model.Problem;
import edu.csus.ecs.pc2.core.model.Run;
import edu.csus.ecs.pc2.core.model.RunFiles;
import edu.csus.ecs.pc2.core.model.SampleContest;
import edu.csus.ecs.pc2.core.util.AbstractTestCase;

/**
 * Unit tests for CCS Internal Validator
 * 
 * @author pc2@ecs.csus.edu
 * @version $Id$
 */

// $HeadURL$
public class ValidatorTest extends AbstractTestCase {

    private static final String PC2_JARNAME = "pc2.jar";

    public void testYesArgument() throws Exception {

        Validator ccsVal = new Validator();
        String[] args = { "--testYes" };
        int code = ccsVal.runValidator(args);
        assertEquals(CCSConstants.VALIDATOR_JUDGED_SUCCESS_EXIT_CODE, code);

    }
    
    public void testMissingInfilename() throws Exception {
        Validator ccsVal = new Validator();
        String[] args = { "" };
        int code = ccsVal.runValidator(args);
        assertEquals(CCSConstants.VALIDATOR_CCS_ERROR_EXIT_CODE, code);
    }
    
    public void testMissingAnswerFile() throws Exception {
        Validator ccsVal = new Validator();
        String[] args = { "inputfile" };
        int code = ccsVal.runValidator(args);
        assertEquals(CCSConstants.VALIDATOR_CCS_ERROR_EXIT_CODE, code);
    }
    
    public void testMissingFeedbackDir() {
        Validator ccsVal = new Validator();
        String[] args = { "inputfile", "answerfile" };
        int code = ccsVal.runValidator(args);
        assertEquals(CCSConstants.VALIDATOR_CCS_ERROR_EXIT_CODE, code);
    }
    
    protected String findPC2JarPath() {
        
        String jarDir = ".." + File.separator + ".classes" + File.pathSeparator; // default to ..\.classes (eclipse) directory

        try {
            String name = "dist";
            File dir = new File(name);
            if (dir.exists()) {
                jarDir = dir.getCanonicalPath();
            }
        } catch (IOException e) {
            System.err.println("Trouble locating pc2home: " + e.getMessage());
        }

        try {
            String cp = System.getProperty("java.class.path");
            StringTokenizer st = new StringTokenizer(cp, File.pathSeparator);
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                File dir = new File(token);
                if (dir.exists() && dir.isFile() && dir.toString().endsWith(PC2_JARNAME)) {
                    jarDir = new File(dir.getParent()).getCanonicalPath();
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Trouble locating pc2home: " + e.getMessage());
        }
        
        return jarDir + File.separator;
    }
    
    /**
     * Test run the validator.
     * 
     * @throws Exception
     */
    public void testRunValidator() throws Exception {

        SampleContest sample = new SampleContest();
        IInternalContest contest = sample.createContest(1, 1, 12, 12, true);

        IInternalController controller = new NullController(this.getName());

        Problem problem = contest.getProblems()[0];

        sample.setCCSValidation(contest, "--testYes", problem);

        ClientId clientId = contest.getAccounts(ClientType.Type.TEAM).firstElement().getClientId();
        Run run = sample.createRun(contest, clientId, problem);

        String sourcefilename = sample.createSampleJavaSource(getTestFilename("Sumit.java"));
        
        assertFileExists(sourcefilename, "Missing source file");
        
        RunFiles runFiles = new RunFiles(run, sourcefilename);
        contest.acceptRun(run, runFiles);
        
        Executable executable = new Executable(contest, controller, run, runFiles);

        // create execute directory
        String executeDirectoryName = getDataDirectory() + File.separator + "execute";
        new File(executeDirectoryName).mkdirs();
        
        assertTrue("Execute dir does not exist " + executeDirectoryName, new File(executeDirectoryName).isDirectory());

        executable.setExecuteDirectoryName(executeDirectoryName);

        String pathToPC2Jar = findPC2JarPath();
        
        assertFileExists (pathToPC2Jar + PC2_JARNAME, "system jar");
        
        String commandPattern = "java -cp " + pathToPC2Jar + problem.getValidatorCommandLine();

        Process process = executable.runProgram(commandPattern, null, false);

        int exitVal = process.waitFor();
        exitVal = process.exitValue();
        
        String message = executable.getRunProgramErrorMessage();

        assertNotNull("Expecting process to be created: " + message, process);
   
        assertEquals(CCSConstants.VALIDATOR_JUDGED_SUCCESS_EXIT_CODE, process.exitValue());
        assertEquals(CCSConstants.VALIDATOR_JUDGED_SUCCESS_EXIT_CODE, exitVal);
    }
    
//    public Test suite() {
//
//        TestSuite s = new TestSuite();
//
//        s.addTest(new ValidatorTest("testYesArgument"));
//        s.addTest(new ValidatorTest("testMissingInfilename"));
//        s.addTest(new ValidatorTest("testMissingAnswerFile"));
//        s.addTest(new ValidatorTest("testMissingFeedbackDir"));
//        s.addTest(new ValidatorTest("testRunValidator"));
//
//        return s;
//    }

}