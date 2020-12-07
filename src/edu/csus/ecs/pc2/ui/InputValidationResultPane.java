// Copyright (C) 1989-2019 PC2 Development Team: John Clevenger, Douglas Lane, Samir Ashoo, and Troy Boudreau.
package edu.csus.ecs.pc2.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

import edu.csus.ecs.pc2.core.Utilities;
import edu.csus.ecs.pc2.core.log.Log;
import edu.csus.ecs.pc2.core.model.Problem.INPUT_VALIDATOR_TYPE;
import edu.csus.ecs.pc2.core.model.Problem.InputValidationStatus;
import edu.csus.ecs.pc2.core.model.SerializedFile;
import edu.csus.ecs.pc2.core.model.inputValidation.InputValidationResult;
import edu.csus.ecs.pc2.core.model.inputValidation.InputValidationResultsTableModel;
import edu.csus.ecs.pc2.ui.cellRenderer.LinkCellRenderer;
import edu.csus.ecs.pc2.ui.cellRenderer.TestCaseResultCellRenderer;

/**
 * This class defines a JPanel for displaying the results of running an Input Validator on a set of Input Data files (Judge's input data).
 * 
 * @author John
 *
 */
public class InputValidationResultPane extends JPanePlugin {

    private static final long serialVersionUID = 1L;

    private JPanel inputValidationResultSummaryPanel;

    private JLabel inputValidationResultsSummaryLabel;

    private JLabel inputValidationResultSummaryTextLabel; //the label containing the (variable) result summary text

    private Component verticalStrut_1;

    private Component verticalStrut_2;

    private JPanel inputValidationResultDetailsPanel;

    private JScrollPane resultsScrollPane;

    private JTable resultsTable;

    private InputValidationResultsTableModel inputValidationResultsTableModel = new InputValidationResultsTableModel();

    /**
     * list of columns
     */
    enum COLUMN {
//        FILE_NAME, RESULT, VALIDATOR_OUTPUT, VALIDATOR_ERR
        FILE_NAME, RESULT, SHOW_DETAILS
    };

    // define the column headers for the table of results
    //TODO: this list of column names is also defined in class InputValidatorResultsTableModel; the defintion should only
    // appear in a single source location...
    private String[] columnNames = { "File", "Result", "Details" };

    private JPanePlugin parentPane;

    private JCheckBox showOnlyFailedRunsCheckbox;

    private Component horizontalStrut_1;

    private JLabel inputValidationResultSourceLabel;

    private JLabel inputValidationResultSourceTextLabel;    //the label containing the (variable) result source identifier

    private Component rigidArea1;

    public InputValidationResultPane() {

        this.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(getVerticalStrut_1());
        this.add(getInputValidationResultSummaryPanel());
        this.add(getVerticalStrut_2());
        this.add(getInputValidationResultDetailsPanel());

    }

    private JPanel getInputValidationResultSummaryPanel() {
        if (inputValidationResultSummaryPanel == null) {
            inputValidationResultSummaryPanel = new JPanel();
            inputValidationResultSummaryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            FlowLayout flowLayout = (FlowLayout) inputValidationResultSummaryPanel.getLayout();
            flowLayout.setHgap(10);
            flowLayout.setAlignment(FlowLayout.LEFT);
            inputValidationResultSummaryPanel.add(getInputValidationResultsSummaryLabel());
            inputValidationResultSummaryPanel.add(getInputValidationResultSummaryTextLabel());
            inputValidationResultSummaryPanel.add(getHorizontalStrut_1());
            inputValidationResultSummaryPanel.add(getInputValidationResultSourceLabel());
            inputValidationResultSummaryPanel.add(getInputValidationResultSourceTextLabel());
            inputValidationResultSummaryPanel.add(getRigidArea1());
            inputValidationResultSummaryPanel.add(getShowOnlyFailedFilesCheckbox());
        }
        return inputValidationResultSummaryPanel;
    }

    private JLabel getInputValidationResultsSummaryLabel() {
        if (inputValidationResultsSummaryLabel == null) {
            inputValidationResultsSummaryLabel = new JLabel("Most Recent Status: ");
        }
        return inputValidationResultsSummaryLabel;
    }

    private JLabel getInputValidationResultSummaryTextLabel() {
        if (inputValidationResultSummaryTextLabel == null) {
            inputValidationResultSummaryTextLabel = new JLabel("<No Input Validation test run yet>");
            inputValidationResultSummaryTextLabel.setForeground(Color.black);
        }
        return inputValidationResultSummaryTextLabel;
    }
    
    private JLabel getInputValidationResultSourceLabel() {
        if (inputValidationResultSourceLabel==null) {
            inputValidationResultSourceLabel = new JLabel("Validator: ");
        }
        return inputValidationResultSourceLabel;
    }

    private JLabel getInputValidationResultSourceTextLabel() {
        if (inputValidationResultSourceTextLabel == null) {
            inputValidationResultSourceTextLabel = new JLabel("Unknown");
            inputValidationResultSourceTextLabel.setForeground(Color.black);
        }
        return inputValidationResultSourceTextLabel;
    }
    
    private JPanel getInputValidationResultDetailsPanel() {
        if (inputValidationResultDetailsPanel == null) {
            inputValidationResultDetailsPanel = new JPanel();
            inputValidationResultDetailsPanel.setLayout(new BorderLayout(0, 0));
            inputValidationResultDetailsPanel.add(getInputValidationResultsScrollPane(), BorderLayout.CENTER);
        }
        return inputValidationResultDetailsPanel;
    }

    private JScrollPane getInputValidationResultsScrollPane() {
        if (resultsScrollPane == null) {
            resultsScrollPane = new JScrollPane();
            resultsScrollPane.setViewportView(getInputValidationResultsTable());
        }
        return resultsScrollPane;
    }

    protected JTable getInputValidationResultsTable() {
        if (resultsTable == null) {
            resultsTable = new JTable(inputValidationResultsTableModel);

            // set the desired options on the table
            resultsTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            resultsTable.setFillsViewportHeight(true);
            resultsTable.setRowSelectionAllowed(true);
            resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            resultsTable.getTableHeader().setReorderingAllowed(false);

            // code from MultipleDataSetPane:
            // insert a renderer that will center cell contents
            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

            for (int i = 0; i < resultsTable.getColumnCount(); i++) {
                resultsTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
            resultsTable.setDefaultRenderer(String.class, centerRenderer);
            //
            // // also center column headers (which use a different CellRenderer)
            // (this code came from MultipleDataSetPane, but the JTable here already has centered headers...
            // ((DefaultTableCellRenderer) testDataSetsListBox.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

            // render file-name, std-out, and std-err file names as clickable links
            resultsTable.getColumn(columnNames[COLUMN.FILE_NAME.ordinal()]).setCellRenderer(new LinkCellRenderer());
//            resultsTable.getColumn(columnNames[COLUMN.VALIDATOR_OUTPUT.ordinal()]).setCellRenderer(new LinkCellRenderer());
//            resultsTable.getColumn(columnNames[COLUMN.VALIDATOR_ERR.ordinal()]).setCellRenderer(new LinkCellRenderer());
            resultsTable.getColumn(columnNames[COLUMN.SHOW_DETAILS.ordinal()]).setCellRenderer(new LinkCellRenderer());

            // add a listener to allow users to click an output or data file name and display it
            resultsTable.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    JTable targetTable = (JTable) e.getSource();
                    // int row = targetTable.getSelectedRow();
                    // int column = targetTable.getSelectedColumn();
                    int row = targetTable.rowAtPoint(e.getPoint());
                    int column = targetTable.columnAtPoint(e.getPoint());
                    if (row >= 0 && row < targetTable.getRowCount() && e.getClickCount() < 2) {
                        viewFiles(targetTable, row, column);
                    }
                }
            });

            // change the header font
            JTableHeader header = resultsTable.getTableHeader();
            header.setFont(new Font("Dialog", Font.BOLD, 12));

            // render Result column as Pass/Fail on Green/Red background
            resultsTable.getColumn("Result").setCellRenderer(new TestCaseResultCellRenderer());

        }
        return resultsTable;
    }

    /**
     * Displays the files listed in the Input Validation Results table File, StdOut, and StdErr columns in a single MultiFileViewer frame.
     * 
     * 
     * @param table
     *            the JTable from which the data is obtained
     * @param row
     *            the table row whose data is to be displayed
     */
    private void viewFiles(JTable table, int row, int selectedColumn) {

        // get the data file from the table
        int dataFileCol = table.getColumn(columnNames[COLUMN.FILE_NAME.ordinal()]).getModelIndex();
        SerializedFile dataFile = getFileForTableCell(table, row, dataFileCol);
        if (dataFile == null) {
            System.err.println("Got a null SerializedFile for Input Validator Results data file (table cell (" + row + "," + dataFileCol + "))");
            getController().getLog().warning("Got a null SerializedFile for InputValidatorResults data file (table cell (" + row + "," + dataFileCol + "))");
        }

        // get the stdout file from the table
//        int stdoutFileCol = table.getColumn(columnNames[COLUMN.VALIDATOR_OUTPUT.ordinal()]).getModelIndex();
//        SerializedFile stdOutFile = getFileForTableCell(table, row, stdoutFileCol);
        InputValidationResult res = ((InputValidationResultsTableModel) table.getModel()).getResultAt(row);
        SerializedFile stdOutFile = res.getValidatorStdOut();
        if (stdOutFile == null) {
            System.err.println("Got a null SerializedFile for Input Validator Results stdout file for table row " + row);
            getController().getLog().warning("Got a null SerializedFile for InputValidatorResults stdout file for table row " + row);
        }

        // get the stderr file from the table
//        int stdErrFileCol = table.getColumn(columnNames[COLUMN.VALIDATOR_ERR.ordinal()]).getModelIndex();
//        SerializedFile stdErrFile = getFileForTableCell(table, row, stdErrFileCol);
        res = ((InputValidationResultsTableModel) table.getModel()).getResultAt(row);
        SerializedFile stdErrFile = res.getValidatorStdErr();
        if (stdErrFile == null) {
            System.err.println("Got a null SerializedFile for Input Validator Results stderr file for table row " + row);
            getController().getLog().warning("Got a null SerializedFile for InputValidatorResults stderr file for table row " + row);
        }

        // get the execution directory being used by the EditProblemPane
        String executeDir;
        JFrame parentFrame = getParentFrame();
        if (parentFrame instanceof InputValidationResultFrame) {

            JPanePlugin grandParent = ((InputValidationResultFrame) parentFrame).getParentPane();
            
            if (grandParent instanceof InputValidatorPane) {
                
                JPanePlugin greatGrandParent = ((InputValidatorPane) grandParent).getParentPane();
                
                if (greatGrandParent instanceof EditProblemPane) {

                    JPanePlugin epp = greatGrandParent;
                    executeDir = ((EditProblemPane) epp).getExecuteDirectoryName();

                    Utilities.insureDir(executeDir);
                    MultipleFileViewer viewer = new MultipleFileViewer(getController().getLog(), "Input Validation Results " + "(row " + (row + 1) + ")");
                    String title;

                    boolean outputPaneAdded = false;

                    if (stdErrFile != null) {
                        // display the stderr file in a viewer pane
                        String stdErrFileName = executeDir + File.separator + stdErrFile.getName();
                        try {
                            // write the stderr file to the execute directory
                            stdErrFile.writeFile(stdErrFileName);

                            // add the stderr file to the viewer frame
                            if (new File(stdErrFileName).isFile()) {
                                title = stdErrFile.getName();
                                viewer.addFilePane(title, stdErrFileName);
                            } else {
                                title = "Error accessing file";
                                viewer.addTextPane(title, "Could not access file ' " + stdErrFile.getName() + " '");
                            }
                        } catch (IOException e) {
                            title = "Error during file access";
                            viewer.addTextPane(title, "Could not create file " + stdErrFileName + "Exception " + e.getMessage());
                        }
                        outputPaneAdded = true;
                    }

                    if (stdOutFile != null) {
                        // display the stdout file in a viewer pane
                        String stdOutFileName = executeDir + File.separator + stdOutFile.getName();
                        try {
                            // write the stdout file to the execute directory
                            stdOutFile.writeFile(stdOutFileName);

                            // add the stdout file to the viewer frame
                            if (new File(stdOutFileName).isFile()) {
                                title = stdOutFile.getName();
                                viewer.addFilePane(title, stdOutFileName);
                            } else {
                                title = "Error accessing file";
                                viewer.addTextPane(title, "Could not access file ' " + stdOutFile.getName() + " '");
                            }
                        } catch (IOException e) {
                            title = "Error during file access";
                            viewer.addTextPane(title, "Could not create file " + stdOutFileName + "Exception " + e.getMessage());
                        }
                        outputPaneAdded = true;
                    }

                    if (dataFile != null) {
                        // display the data file in a viewer pane
                        String dataFileName = executeDir + File.separator + dataFile.getName();
                        try {
                            // write the data file to the execute directory
                            dataFile.writeFile(dataFileName);

                            // add the data file to the viewer frame
                            if (new File(dataFileName).isFile()) {
                                title = dataFile.getName();
                                viewer.addFilePane(title, dataFileName);
                            } else {
                                title = "Error accessing file";
                                viewer.addTextPane(title, "Could not access file ' " + dataFile.getName() + " '");
                            }
                        } catch (IOException e) {
                            title = "Error during file access";
                            viewer.addTextPane(title, "Could not create file " + dataFileName + "Exception " + e.getMessage());
                        }
                        outputPaneAdded = true;
                    }

                    // check if we actually added anything
                    if (outputPaneAdded) {
                        // yes we added something; decide which tab should be active

                        int activeTab = 0;  //default to the input data file tab being the active tab
                        if (selectedColumn == COLUMN.SHOW_DETAILS.ordinal()) {
                            //the "Show Details" link was clicked; make the stdout tab the active tab
                            activeTab = 1;
                        }
                        viewer.setSelectedIndex(activeTab);

                        // show the viewer containing the files
                        viewer.setVisible(true);

                    } else {
                        getController().getLog().warning("Found no Input Validation Result files to add to MultiFileViewer");
                        System.err.println("Request to display results files but found no Input Validation Result files to add to MultiFileViewer");
                    }

                } else {
                    getController().getLog().severe("GreatGrandParent of InputValidationResultPane is not an EditProblemPane; not supported");
                } 
            } else {
                getController().getLog().severe("Grandparent of InputValidationResultPane is not an InputValidatorPane; not supported");
            }
        } else {
            getController().getLog().severe("Parent of InputValidationResultPane is not an InputValidationResultFrame; not supported");
        }
    }

    private SerializedFile getFileForTableCell(JTable table, int row, int col) {
        InputValidationResult res = ((InputValidationResultsTableModel) table.getModel()).getResultAt(row);
        SerializedFile file = null;
        switch (col) {
            case 0:
                file = new SerializedFile(res.getFullPathFilename());
                try {
                    if (Utilities.serializedFileError(file)) {
                        getController().getLog().warning("Error obtaining SerializedFile for file ' " + res.getFullPathFilename() + " '");
                        System.err.println("Error constructing SerializedFile (see log)");
                        file = null;
                    }
                } catch (Exception e) {
                    getController().getLog().getLogger().log(Log.SEVERE, "Error obtaining SerializedFile for file ' " + res.getFullPathFilename() + " '", e);
                    System.err.println("Exception constructing SerializedFile (see log): " + e.getMessage());
                    file = null;
                }
                break;
            case 1:
                getController().getLog().getLogger().log(Log.SEVERE, "Got a mouse click on an unclickable table cell!");
                System.err.println("Internal error: got a mouse click on a cell that shouldn't be clickable");
                System.err.println("Please report this error to the PC2 Development Team (pc2@ecs.csus.edu)");
                file = null;
                break;
// columns 2 and 3 used to be clickable file links; they were replaced by a single "Show Details" button
//            case 2:
//                file = res.getValidatorStdOut();
//                break;
//            case 3:
//                file = res.getValidatorStdErr();
//                break;
            case 2:
                //case 2 (col #2, the third column) now has a button with an actionPerformed() method; do nothing here
                break;
            default:
                getController().getLog().severe("Undefined JTable column");
                System.err.println("Internal error: InputValidationResultPane.getFileForTableCell() received an undefined JTable column.");
                System.err.println("Please report this error to the PC2 Development Team (pc2@ecs.csus.edu");
                return null;
        }
        return file;

    }

    private Component getVerticalStrut_1() {
        if (verticalStrut_1 == null) {
            verticalStrut_1 = Box.createVerticalStrut(20);
        }
        return verticalStrut_1;
    }

    private Component getVerticalStrut_2() {
        if (verticalStrut_2 == null) {
            verticalStrut_2 = Box.createVerticalStrut(20);
        }
        return verticalStrut_2;
    }

    @Override
    public String getPluginTitle() {
        return "Input Validation Result Pane";
    }

    public void setParentPane(JPanePlugin parentPane) {
        this.parentPane = parentPane;
    }

    public JPanePlugin getParentPane() {
        return this.parentPane;
    }

    public void setInputValidationSummaryMessageText(String msg) {
        getInputValidationResultSummaryTextLabel().setText(msg);
    }

    public void setInputValidationResultSourceText(String msg) {
        getInputValidationResultSourceTextLabel().setText(msg);
    }

    public void setInputValidationSummaryMessageColor(Color color) {
        getInputValidationResultSummaryTextLabel().setForeground(color);
    }

    /**
     * Examines the provided array of {@link InputValidationResult} values and sets the Status Message label text correspondingly.
     * 
     * @param runResults
     *            an array of InputValidationStatus values
     */
    public void updateInputValidationStatusMessage(InputValidationResult[] runResults) {

        Color color = Color.BLACK;
        String msg = "<No Input Validation test run yet>";

        if (runResults != null && runResults.length > 0) {

            // there are some results; see if there were any failures or errors
            boolean foundFailure = false;
            boolean foundError = false;
            for (InputValidationResult res : runResults) {
                if (res == null) {
                    // ignore null results, but log them
                    getController().getLog().warning("InputValidationPane SwingWorker thread returned null InputValidationResult");
                } else {
                    if (res.getStatus() == InputValidationStatus.ERROR) {
                        foundError = true;
                        break;
                    } else {
                        if (!res.isPassed()) {
                            foundFailure = true;
                            break;
                        }
                    }
                }
            }

            InputValidationStatus overallStatus;
            if (foundError) {
                overallStatus = InputValidationStatus.ERROR;
            } else if (foundFailure) {
                overallStatus = InputValidationStatus.FAILED;
            } else {
                overallStatus = InputValidationStatus.PASSED;
            }

            switch (overallStatus) {

                case PASSED:
                    int count = runResults.length;
                    msg = "" + count + " of " + count + " input data files PASSED validation";
                    color = new Color(0x00, 0xC0, 0x00); // green, but with some shading
                    break;

                case FAILED:
                    int totalCount = 0;
                    int failCount = 0;
                    for (InputValidationResult res : runResults) {
                        if (res != null) {
                            if (!res.isPassed()) {
                                failCount++;
                            }
                            totalCount++;
                        }
                    }
                    msg = "" + failCount + " of " + totalCount + " input data files FAILED validation";
                    color = Color.red;
                    break;

                case ERROR:
                    msg = "" + "Error occured during Input Validator execution";
                    color = Color.red;
                    break;
                    
                case NOT_TESTED:
                    msg = "Error occurred during input validation result display; check logs";
                    color = Color.YELLOW;
                    getController().getLog()
                            .severe("Unexpected error in computing Input Validation Status: found status '" + overallStatus + "' when " + "only 'PASSED', 'FAILED' or 'ERROR' should be possible");
                    break;
                default:
                    msg = "This message should never be displayed; please notify PC2 Developers: pc2@ecs.csus.edu";
                    color = Color.ORANGE;
            }
        }

        getInputValidationResultSummaryTextLabel().setText(msg);
        getInputValidationResultSummaryTextLabel().setForeground(color);

    }

    protected JCheckBox getShowOnlyFailedFilesCheckbox() {
        if (showOnlyFailedRunsCheckbox == null) {
            showOnlyFailedRunsCheckbox = new JCheckBox("Show only failed input files");
            showOnlyFailedRunsCheckbox.setSelected(true);

            showOnlyFailedRunsCheckbox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            
                            //get the frame which holds this result pane
                            JFrame  resultFrame = getParentFrame();
                            if (resultFrame instanceof InputValidationResultFrame) {

                                //get the pane pointed to by the frame holding this pane which holds the Input Validation results 
                                JPanePlugin resultHolder = ((InputValidationResultFrame)resultFrame).getParentPane();
                                
                                if (resultHolder instanceof InputValidatorPane) {
                                    
                                    // get the results of the latest run from the result-holding InputValidatorPane
                                    InputValidationResult[] results = null;
                                    if (((InputValidatorPane) resultHolder).getMostRecentlyRunInputValidatorType()==INPUT_VALIDATOR_TYPE.CUSTOM) {
                                        results = ((InputValidatorPane) resultHolder).getCustomInputValidatorResults();
                                    } else if (((InputValidatorPane) resultHolder).getMostRecentlyRunInputValidatorType()==INPUT_VALIDATOR_TYPE.VIVA) {
                                        results = ((InputValidatorPane) resultHolder).getVivaInputValidatorResults();
                                    }
                                    
                                    //check to make sure we got some results
                                    if (results != null && results.length > 0) {

                                        // make a copy so we don't wipe out the parent's results
                                        InputValidationResult[] updatedResults = Arrays.copyOf(results, results.length);

                                        // if we are only going to show failed results, make a new array containing only failed results
                                        if (getShowOnlyFailedFilesCheckbox().isSelected()) {
                                            ArrayList<InputValidationResult> failedResultsList = new ArrayList<InputValidationResult>();
                                            for (int i = 0; i < updatedResults.length; i++) {
                                                if (!updatedResults[i].isPassed()) {
                                                    failedResultsList.add(updatedResults[i]);
                                                }
                                            }
                                            updatedResults = new InputValidationResult[failedResultsList.size()];
                                            for (int i = 0; i < updatedResults.length; i++) {
                                                updatedResults[i] = failedResultsList.get(i);
                                            }
                                        }

                                        // put the updated results in the table model and redraw the table
                                        ((InputValidationResultsTableModel) getInputValidationResultsTable().getModel()).setResults(updatedResults);
                                        ((InputValidationResultsTableModel) getInputValidationResultsTable().getModel()).fireTableDataChanged();

                                    } else {
                                        getController().getLog().info("ShowOnlyFailedFiles checkbox selected but found no run results to display");
                                    } 
                                } else {
                                    getController().getLog().warning("InputValidationResultFrame does not link to an InputValidatorPane; cannot fetch Input Validation results");
                                }

                            } else {
                                getController().getLog().warning("InputValidationResultPane parent not an InputValidationResultFrame; cannot obtain results to update table");
                            }
                        }
                    });
                }
            });
        }
        return showOnlyFailedRunsCheckbox;
    }

    private Component getHorizontalStrut_1() {
        if (horizontalStrut_1 == null) {
            horizontalStrut_1 = Box.createHorizontalStrut(20);
        }
        return horizontalStrut_1;
    }

    private Component getRigidArea1() {
        if (rigidArea1 == null) {
            rigidArea1 = Box.createRigidArea(new Dimension(40, 20));
        }
        return rigidArea1;
    }


    /**
     * This method uses the specified {@link InputValidationResult} array to update the Table Model in the 
     * Input Validation Results JTable contained in this InputValidationResultPane, and then fires TableDataChanged
     * on the Table Model.
     * 
     * @param runResults the new InputValidationResults to be displayed in the table.
     */
    protected void updateResultsTable(InputValidationResult[] runResults) {
        // put the results in the table model and redraw the table
        ((InputValidationResultsTableModel) getInputValidationResultsTable().getModel()).setResults(runResults);
        ((InputValidationResultsTableModel) getInputValidationResultsTable().getModel()).fireTableDataChanged();
        updateInputValidationStatusMessage(runResults);
    }
}
