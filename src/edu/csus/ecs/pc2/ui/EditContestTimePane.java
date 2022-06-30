// Copyright (C) 1989-2021 PC2 Development Team: John Clevenger, Douglas Lane, Samir Ashoo, and Troy Boudreau.
package edu.csus.ecs.pc2.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import edu.csus.ecs.pc2.core.IInternalController;
import edu.csus.ecs.pc2.core.log.Log;
import edu.csus.ecs.pc2.core.log.StaticLog;
import edu.csus.ecs.pc2.core.model.ContestInformation;
import edu.csus.ecs.pc2.core.model.ContestInformationEvent;
import edu.csus.ecs.pc2.core.model.ContestTime;
import edu.csus.ecs.pc2.core.model.IContestInformationListener;
import edu.csus.ecs.pc2.core.model.IInternalContest;

/**
 * Add/Edit ContestTime Pane.
 * 
 * @author pc2@ecs.csus.edu
 */

public class EditContestTimePane extends JPanePlugin {

    private static final long serialVersionUID = -1060536964672397704L;

    private JPanel messagePane = null;

    private JPanel buttonPane = null;

    private JButton updateButton = null;

    private JButton cancelButton = null;

    private JLabel messageLabel = null;

    private ContestTime contestTime = null;
    
    private boolean populatingGUI = true;

    private JPanel centerPane = null;

    private JLabel remaingingTimeLabel = null;

    private JTextField remainingTimeTextBox = null;

    private JLabel elapsedTimeLabel = null;

    private JTextField elapsedTimeTextBox = null;

    private JLabel contestLengthLabel = null;

    private JTextField contestLengthTextBox = null;

    private JCheckBox autoStopAtEndofContestCheckBox = null;

    /**
     * 
     * @author pc2@ecs.csus.edu
     * 
     */
    class ContestInformationListenerImplementation implements IContestInformationListener {

        public void contestInformationAdded(ContestInformationEvent event) {
            updateGUI(event.getContestInformation());
        }

        public void contestInformationChanged(ContestInformationEvent event) {
            updateGUI(event.getContestInformation());
        }

        public void contestInformationRemoved(ContestInformationEvent event) {
            // TODO Auto-generated method stub

        }

        public void contestInformationRefreshAll(ContestInformationEvent contestInformationEvent) {
            updateGUI(contestInformationEvent.getContestInformation());
        }
        
        public void finalizeDataChanged(ContestInformationEvent contestInformationEvent) {
            // Not used
        }

        private void updateGUI(final ContestInformation ci) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    getAutoStopAtEndofContestCheckBox().setSelected(ci.isAutoStopContest());
                    if (contestTime != null) {
                        contestTime.setHaltContestAtTimeZero(ci.isAutoStopContest());
                    }
                }
            });
        }
        
    }



    /**
     * This method initializes
     * 
     */
    public EditContestTimePane() {
        super();
        setPreferredSize(new Dimension(530, 350));
        initialize();
    }

    /**
     * This method initializes this
     * 
     */
    private void initialize() {
        this.setLayout(new BorderLayout());
        this.setSize(new Dimension(530, 350));

        this.add(getMessagePane(), java.awt.BorderLayout.NORTH);
        this.add(getButtonPane(), java.awt.BorderLayout.SOUTH);
        this.add(getCenterPane(), java.awt.BorderLayout.CENTER);
    }

    public void setContestAndController(IInternalContest inContest, IInternalController inController) {
        super.setContestAndController(inContest, inController);

        addWindowCloserListener();
        getContest().addContestInformationListener(new ContestInformationListenerImplementation());

    }

    private void addWindowCloserListener() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (getParentFrame() != null) {
                    getParentFrame().addWindowListener(new java.awt.event.WindowAdapter() {
                        public void windowClosing(java.awt.event.WindowEvent e) {
                            handleCancelButton();
                        }
                    });
                } 
            }
        });
    }

    public String getPluginTitle() {
        return "Edit ContestTime Pane";
    }

    /**
     * This method initializes messagePane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getMessagePane() {
        if (messagePane == null) {
            messageLabel = new JLabel();
            messageLabel.setText("");
            messageLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            messagePane = new JPanel();
            messagePane.setMinimumSize(new Dimension(10, 30));
            messagePane.setLayout(new BorderLayout());
            messagePane.setPreferredSize(new Dimension(25, 30));
            messagePane.add(messageLabel, java.awt.BorderLayout.CENTER);
        }
        return messagePane;
    }

    /**
     * This method initializes buttonPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getButtonPane() {
        if (buttonPane == null) {
            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setHgap(45);
            buttonPane = new JPanel();
            buttonPane.setLayout(flowLayout);
            buttonPane.add(getUpdateButton(), null);
            buttonPane.add(getCancelButton(), null);
        }
        return buttonPane;
    }

    /**
     * Enable or disable Update button based on comparison of current time values with
     * values in the GUI.
     * 
     */
    private void enableUpdateButton() {

        if (populatingGUI) {
            return;
        }

        //assume the "Update" button should NOT be enabled, then see if something changed such that it SHOULD be
        boolean updateButtonShouldBeEnabled = false;

        if (contestTime != null) {

            try {
                ContestTime changedContestTime = getContestTimeFromFields(null);
                
                if (contestTime.isHaltContestAtTimeZero() == changedContestTime.isHaltContestAtTimeZero() && contestTime.getElapsedTimeStr().equals(changedContestTime.getElapsedTimeStr())
                        && contestTime.getContestLengthStr().equals(changedContestTime.getContestLengthStr()) && contestTime.getRemainingTimeStr().equals(changedContestTime.getRemainingTimeStr())) {
                    updateButtonShouldBeEnabled = false;
                } else {
                    updateButtonShouldBeEnabled = true;
                }

            } catch (InvalidFieldValue e) {
                // invalid field, but that is ok as they are entering data
                // will be caught and reported when they hit update or add.
                StaticLog.getLog().log(Log.DEBUG, "Input ContestTime (but not saving) ", e);
                updateButtonShouldBeEnabled = true;
            }
        }

        setButtonStatesAndLabels(updateButtonShouldBeEnabled);
    }

    /**
     * Create a ContestTime from the fields.
     * 
     * This also populates newContestTimeDataFiles for the data files.  (Huh?  This makes no sense.... jlc)
     * 
     * @param checkContestTime
     *            will update this ContestTime if supplied, if null creates a new ContestTime
     * @return ContestTime based on fields
     * @throws InvalidFieldValue
     */
    public ContestTime getContestTimeFromFields(ContestTime checkContestTime) {

        long secs = stringToLongSecs(getElapsedTimeTextBox().getText());
        if (secs == -1) {
            throw new InvalidFieldValue("Invalid elapsed time");
        }

        long elapsedTime = secs;

        secs = stringToLongSecs(getRemainingTimeTextBox().getText());
        if (secs == -1) {
            throw new InvalidFieldValue("Invalid remaining time");
        }

        long remainingTime = secs;

        secs = stringToLongSecs(getContestLengthTextBox().getText());
        if (secs == -1) {
            throw new InvalidFieldValue("Invalid contest length");
        }

        long contestLength = secs;

        long actualRemaining = contestLength - elapsedTime;

        if (actualRemaining != remainingTime) {
            throw new InvalidFieldValue("Invalid contest times: Elapsed+Remaining must equal Length\n (set Remaining to " + ContestTime.formatTime(actualRemaining) + "?)");
        }

        if (checkContestTime == null) {
            checkContestTime = new ContestTime(0);
        }
        checkContestTime.setContestLengthSecs(contestLength);
        checkContestTime.setRemainingSecs(remainingTime);
        // elapsed is calculate in setRemainingSecs
        checkContestTime.setHaltContestAtTimeZero(getAutoStopAtEndofContestCheckBox().isSelected());
        return checkContestTime;
    }

    /**
     * This method initializes updateButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getUpdateButton() {
        if (updateButton == null) {
            updateButton = new JButton();
            updateButton.setText("Update");
            updateButton.setEnabled(false);
            updateButton.setMnemonic(java.awt.event.KeyEvent.VK_U);
            updateButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    handleUpdate();
                }
            });
        }
        return updateButton;
    }

    protected void handleUpdate() {
        
        if (getContest().getContestTime().isContestRunning()) {
            showMessage("Updating of time while contest clock is running is not allowed");
            return;
        }

        //check contest length, elapsed, and remaining times in GUI textboxes
        if (!validateContestTimeFields()) {
            // new contestTime is invalid, just return (message issued by validateContestTimeFields())
            return;
        }

        // all fields are valid
        
        //extract a new ContestTime from the GUI fields and double-check it is valid
        ContestTime newContestTime = null;
        try {
            newContestTime = getContestTimeFromFields(contestTime);
        } catch (InvalidFieldValue e) {
            showMessage(e.getMessage());
            return;
        }
        

        //get the existing ContestInfo from the contest, insert new contest info data into it
        ContestInformation contestInfo = getContest().getContestInformation();
        contestInfo.setAutoStopContest(getAutoStopAtEndofContestCheckBox().isSelected());

        //put the updated ContestTime and ContestInfo back into the Controller
        getController().updateContestTime(newContestTime);
        getController().updateContestInformation(contestInfo);

        cancelButton.setText("Close");
        updateButton.setEnabled(false);

        if (getParentFrame() != null) {
            getParentFrame().setVisible(false);
        }
    }


    /**
     * Validate that all contestTime fields are ok.
     * 
     * @return
     */
    private boolean validateContestTimeFields() {

        long secs = stringToLongSecs(getElapsedTimeTextBox().getText());
        if (secs == -1) {
            showMessage("Invalid elapsed time");
            return false;
        }

        secs = stringToLongSecs(getRemainingTimeTextBox().getText());
        if (secs == -1) {
            showMessage("Invalid remaining time");
            return false;
        }

        secs = stringToLongSecs(getContestLengthTextBox().getText());
        if (secs == -1) {
            showMessage("Invalid contest length");
            return false;
        }

        return true;
    }
    
    

    /**
     * This method initializes cancelButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getCancelButton() {
        if (cancelButton == null) {
            cancelButton = new JButton();
            cancelButton.setText("Cancel");
            cancelButton.setMnemonic(java.awt.event.KeyEvent.VK_C);
            cancelButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    handleCancelButton();
                }
            });
        }
        return cancelButton;
    }

    protected void handleCancelButton() {

        if (getUpdateButton().isEnabled()) {
            // Something changed, are they sure ?

            int result = FrameUtilities.yesNoCancelDialog(getParentFrame(), "Contest Time data has been modified;"
                    + "\n do you want to save the changes?\n", "Confirm Choice");

            if (result == JOptionPane.YES_OPTION) {
                handleUpdate();

            } else if (result == JOptionPane.NO_OPTION) {
                if (getParentFrame() != null) {
                    getParentFrame().setVisible(false);
                }
//            } else {
                //must have been Cancel; do nothing -- leave the parent frame visible for the user to contemplate
            }
        } else {
            // Close
            if (getParentFrame() != null) {
                getParentFrame().setVisible(false);
            }
        }
    }

    public ContestTime getContestTime() {
        return contestTime;
    }

    public void setContestTime(final ContestTime inputContestTime, final ContestInformation inputContestInfo) {

        this.contestTime = inputContestTime;

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                populateGUI(inputContestTime, inputContestInfo);
                setButtonStatesAndLabels(false);    //false = "Update" button should NOT be enabled
                showMessage("");
            }
        });
    }

    private void populateGUI(ContestTime inContestTime, ContestInformation inContestInfo) {

        populatingGUI = true;

        //put the fields from the ContestTime object into the GUI
        getRemainingTimeTextBox().setText(inContestTime.getRemainingTimeStr());
        getElapsedTimeTextBox().setText(inContestTime.getElapsedTimeStr());
        getContestLengthTextBox().setText(inContestTime.getContestLengthStr());
                
        //put the state of the auto-stop checkbox into the GUI
        getAutoStopAtEndofContestCheckBox().setSelected(inContestInfo.isAutoStopContest());
        inContestTime.setHaltContestAtTimeZero(inContestInfo.isAutoStopContest());
        getUpdateButton().setVisible(true);
        setButtonStatesAndLabels(false);

        populatingGUI = false;
    }

    protected void setButtonStatesAndLabels(boolean fieldsChanged) {
        if (fieldsChanged) {
            cancelButton.setText("Cancel");
        } else {
            cancelButton.setText("Close");
        }
        updateButton.setEnabled(fieldsChanged);
    }

    public void showMessage(final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                messageLabel.setForeground(Color.RED);
                messageLabel.setText(message);
            }
        });
    }

    /**
     * This method initializes centerPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getCenterPane() {
        if (centerPane == null) {
            centerPane = new JPanel();
            centerPane.setMinimumSize(new Dimension(10, 500));
            GridBagLayout gbl_centerPane = new GridBagLayout();
            gbl_centerPane.columnWidths = new int[] {30, 110, 150, 80};
            gbl_centerPane.rowHeights = new int[] {30, 30, 30, 30, 30, 30, 30};
            gbl_centerPane.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
            gbl_centerPane.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
            centerPane.setLayout(gbl_centerPane);
            contestLengthLabel = new JLabel();
            contestLengthLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
            contestLengthLabel.setText("Contest Length");
            GridBagConstraints gbc_contestLengthLabel = new GridBagConstraints();
            gbc_contestLengthLabel.fill = GridBagConstraints.BOTH;
            gbc_contestLengthLabel.insets = new Insets(0, 0, 5, 10);
            gbc_contestLengthLabel.gridx = 1;
            gbc_contestLengthLabel.gridy = 2;
            centerPane.add(contestLengthLabel, gbc_contestLengthLabel);
            GridBagConstraints gbc_contestLengthTextBox = new GridBagConstraints();
            gbc_contestLengthTextBox.fill = GridBagConstraints.BOTH;
            gbc_contestLengthTextBox.insets = new Insets(0, 0, 5, 0);
            gbc_contestLengthTextBox.gridx = 2;
            gbc_contestLengthTextBox.gridy = 2;
            centerPane.add(getContestLengthTextBox(), gbc_contestLengthTextBox);
            elapsedTimeLabel = new JLabel();
            elapsedTimeLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
            elapsedTimeLabel.setText("Current Elapsed Time");
            GridBagConstraints gbc_elapsedTimeLabel = new GridBagConstraints();
            gbc_elapsedTimeLabel.fill = GridBagConstraints.BOTH;
            gbc_elapsedTimeLabel.insets = new Insets(0, 0, 5, 10);
            gbc_elapsedTimeLabel.gridx = 1;
            gbc_elapsedTimeLabel.gridy = 3;
            centerPane.add(elapsedTimeLabel, gbc_elapsedTimeLabel);
            GridBagConstraints gbc_elapsedTimeTextBox = new GridBagConstraints();
            gbc_elapsedTimeTextBox.fill = GridBagConstraints.BOTH;
            gbc_elapsedTimeTextBox.insets = new Insets(0, 0, 5, 0);
            gbc_elapsedTimeTextBox.gridx = 2;
            gbc_elapsedTimeTextBox.gridy = 3;
            centerPane.add(getElapsedTimeTextBox(), gbc_elapsedTimeTextBox);
            remaingingTimeLabel = new JLabel();
            remaingingTimeLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
            remaingingTimeLabel.setText("Current Remaining Time");
            GridBagConstraints gbc_remaingingTimeLabel = new GridBagConstraints();
            gbc_remaingingTimeLabel.fill = GridBagConstraints.BOTH;
            gbc_remaingingTimeLabel.insets = new Insets(0, 0, 5, 10);
            gbc_remaingingTimeLabel.gridx = 1;
            gbc_remaingingTimeLabel.gridy = 4;
            centerPane.add(remaingingTimeLabel, gbc_remaingingTimeLabel);
            GridBagConstraints gbc_remainingTimeTextBox = new GridBagConstraints();
            gbc_remainingTimeTextBox.fill = GridBagConstraints.BOTH;
            gbc_remainingTimeTextBox.insets = new Insets(0, 0, 5, 0);
            gbc_remainingTimeTextBox.gridx = 2;
            gbc_remainingTimeTextBox.gridy = 4;
            centerPane.add(getRemainingTimeTextBox(), gbc_remainingTimeTextBox);
            GridBagConstraints gbc_stopAtEndofContestCheckBox = new GridBagConstraints();
            gbc_stopAtEndofContestCheckBox.insets = new Insets(0, 0, 5, 0);
            gbc_stopAtEndofContestCheckBox.fill = GridBagConstraints.VERTICAL;
            gbc_stopAtEndofContestCheckBox.gridx = 2;
            gbc_stopAtEndofContestCheckBox.gridy = 6;
            centerPane.add(getAutoStopAtEndofContestCheckBox(), gbc_stopAtEndofContestCheckBox);
        }
        return centerPane;
    }

    /**
     * This method initializes remainingTimeTextBox
     * 
     * @return javax.swing.JTextField
     */
    private JTextField getRemainingTimeTextBox() {
        if (remainingTimeTextBox == null) {
            remainingTimeTextBox = new JTextField();
            remainingTimeTextBox.addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyTyped(java.awt.event.KeyEvent e) {
                    enableUpdateButton();
                }
            });
        }
        return remainingTimeTextBox;
    }

    /**
     * This method initializes elapsedTimeTextBox
     * 
     * @return javax.swing.JTextField
     */
    private JTextField getElapsedTimeTextBox() {
        if (elapsedTimeTextBox == null) {
            elapsedTimeTextBox = new JTextField();
            elapsedTimeTextBox.addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyTyped(java.awt.event.KeyEvent e) {
                    enableUpdateButton();
                }
            });
        }
        return elapsedTimeTextBox;
    }

    /**
     * This method initializes contestLengthTextBox
     * 
     * @return javax.swing.JTextField
     */
    private JTextField getContestLengthTextBox() {
        if (contestLengthTextBox == null) {
            contestLengthTextBox = new JTextField();
            contestLengthTextBox.addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyTyped(java.awt.event.KeyEvent e) {
                    enableUpdateButton();
                }
            });
        }
        return contestLengthTextBox;
    }

    /**
     * This method initializes the "Automatically Stop at End of Contest" checkbox.
     * 
     * @return the checkbox indicating whether PC2 should automatically stop the contest when the remaining time reaches zero
     */
    private JCheckBox getAutoStopAtEndofContestCheckBox() {
        if (autoStopAtEndofContestCheckBox == null) {
            autoStopAtEndofContestCheckBox = new JCheckBox();
            autoStopAtEndofContestCheckBox.setToolTipText("Check to cause PC2 to automatically stop the contest (cease accepting submissions) when the contest clock (remaining time) reaches zero");
            autoStopAtEndofContestCheckBox.setText("Stop contest automatically");
            autoStopAtEndofContestCheckBox.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    enableUpdateButton();
                }
            });
        }
        return autoStopAtEndofContestCheckBox;
    }
    
    /**
     * Convert String to second. Expects input in form: ss or mm:ss or hh:mm:ss
     * 
     * @param s
     *            string to be converted to seconds
     * @return -1 if invalid time string, 0 or greater if valid
     */
    public long stringToLongSecs(String s) {

        if (s == null || s.trim().length() == 0) {
            return -1;
        }

        String[] fields = s.split(":");
        long hh = 0;
        long mm = 0;
        long ss = 0;
        
        switch (fields.length ) {
            case 3:
                hh = stringToLong(fields[0]);
                mm = stringToLong(fields[1]);
                ss = stringToLong(fields[2]);
                break;
            case 2:
                mm = stringToLong(fields[0]);
                ss = stringToLong(fields[1]);
                break;
            case 1:
                ss = stringToLong(fields[0]);
                break;

            default:
                break;
        }

        // System.out.println(" values "+hh+":"+mm+":"+ss);

        long totsecs = 0;
        if (hh != -1) {
            totsecs = hh;
        }
        if (mm != -1) {
            totsecs = (totsecs * 60) + mm;
        }
        if (ss != -1) {
            totsecs = (totsecs * 60) + ss;
        }

        // System.out.println(" values "+hh+":"+mm+":"+ss+" secs="+totsecs);

        if (hh == -1 || mm == -1 || ss == -1) {
            return -1;
        }

        return totsecs;
    }

    /**
     * Parse and return positive long.
     * 
     * @param s1
     * @return -1 if non-long string, else long value
     */
    private long stringToLong(String s1) {
        if (s1 == null) {
            return -1;
        }
        try {
            return Long.parseLong(s1);
        } catch (Exception e) {
            return -1;
        }
    }
} // @jve:decl-index=0:visual-constraint="10,10"
