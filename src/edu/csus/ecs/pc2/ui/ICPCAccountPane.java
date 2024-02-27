// Copyright (C) 1989-2024 PC2 Development Team: John Clevenger, Douglas Lane, Samir Ashoo, and Troy Boudreau.
/**
 *
 */
package edu.csus.ecs.pc2.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.ibm.webrunner.j2mclb.util.HeapSorter;
import com.ibm.webrunner.j2mclb.util.NumericStringComparator;

import edu.csus.ecs.pc2.core.IInternalController;
import edu.csus.ecs.pc2.core.imports.ICPCAccount;
import edu.csus.ecs.pc2.core.log.Log;
import edu.csus.ecs.pc2.core.model.Account;
import edu.csus.ecs.pc2.core.model.AccountEvent;
import edu.csus.ecs.pc2.core.model.ClientId;
import edu.csus.ecs.pc2.core.model.ClientType;
import edu.csus.ecs.pc2.core.model.ElementId;
import edu.csus.ecs.pc2.core.model.IAccountListener;
import edu.csus.ecs.pc2.core.model.IInternalContest;

/**
 * @author pc2@ecs.csus.edu
 * @version $Id$
 */

// $HeadURL$
public class ICPCAccountPane extends JPanePlugin {

    /**
     *
     */
    private static final long serialVersionUID = -94109299290020550L;

    private DisplayNameFormatterPane displayNameFormatterPane = null;

    private MCLB accountListBox = null;

    private JPanel buttonPanel = null;

    private JButton updateButton = null;

    private JButton cancelButton = null;

    private ICPCAccount[] icpcAccounts;

    private String displayChoice = "SHORTSCHOOLNAME";

    private Vector<Account> updatedAccountVector = new Vector<Account>();  //  @jve:decl-index=0:

    private JCheckBox showAllCheckBox = null;
    /**
     * @author pc2@ecs.csus.edu
     *
     */
    public class AccountListenerImplementation implements IAccountListener {

        @Override
        public void accountAdded(AccountEvent accountEvent) {
            changeDisplayName(displayChoice);
        }

        @Override
        public void accountModified(AccountEvent event) {
            changeDisplayName(displayChoice);
        }

        @Override
        public void accountsAdded(AccountEvent accountEvent) {
            changeDisplayName(displayChoice);
        }

        @Override
        public void accountsModified(AccountEvent accountEvent) {
            changeDisplayName(displayChoice);
        }

        @Override
        public void accountsRefreshAll(AccountEvent accountEvent) {
            changeDisplayName(displayChoice);
        }
    }

    /**
     *
     * @author pc2@ecs.csus.edu
     *
     */
    public class PropertyChangeListenerImplementation implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getNewValue() != null && !evt.getNewValue().equals(evt.getOldValue())) {
                // display choice has changed
                displayChoice=(String) evt.getNewValue();
                changeDisplayName(displayChoice);
                // TODO now reload the grid?
            }
        }
    }

    /**
     *
     */
    public ICPCAccountPane() {
        super();
        initialize();

    }

    private String getSiteTitle(String string) {
        return "Site " + string;
    }

    private String getTeamDisplayName(Account account) {
        if (account != null) {
            return account.getDisplayName();
        }

        return "Invalid Account";
    }

    private Object[] buildAccountRow(Account account, String newDisplayName) {
        // Object[] cols = {"Site", "Type", "Account Id", "Current Display Name", "New Display Name"};
        try {
            int cols = accountListBox.getColumnCount();
            Object[] s = new String[cols];

            ClientId clientId = account.getClientId();
            s[0] = getSiteTitle("" + account.getSiteNumber());
            s[1] = clientId.getClientType().toString().toLowerCase();
            s[2] = "" + clientId.getClientNumber();

            s[3] = getTeamDisplayName(account);
            s[4] = newDisplayName;
            return s;
        } catch (Exception exception) {
            getController().getLog().log(Log.INFO, "Exception in buildAccountRow()", exception);
        }
        return null;
    }

    protected void changeDisplayName(String string) {
        if (string != null && string.length() > 0) {
            updatedAccountVector.clear();
            getAccountListBox().removeAllRows();
            String newDisplayName;

            Hashtable<ClientId,ICPCAccount> icpcAccountsHash = new Hashtable<ClientId,ICPCAccount>();
            for (ICPCAccount account : icpcAccounts) {
                if (account.getClientId() != null) {
                    icpcAccountsHash.put(account.getClientId(), account);
                }
            }

            // update all the accounts, if they have the icpc data
            // note, the icpcAccount (if available) will be merged in updateDisplayName
            for(Account account : getContest().getAccounts(ClientType.Type.TEAM)) {
                newDisplayName = "";
                ICPCAccount icpcAccount = icpcAccountsHash.get(account.getClientId());
                if (string.equalsIgnoreCase(DisplayNameFormatterPane.DisplayNameChoice.TEAMNAME.toString())) {
                    if (icpcAccount == null) {
                        newDisplayName=account.getExternalName();
                    } else {
                        newDisplayName=icpcAccount.getExternalName();
                    }
                } else if (string.equalsIgnoreCase(DisplayNameFormatterPane.DisplayNameChoice.SCHOOLNAME.toString())) {
                    if (icpcAccount == null) {
                        newDisplayName=account.getLongSchoolName();
                    } else {
                        newDisplayName=icpcAccount.getLongSchoolName();
                    }
                } else if (string.equalsIgnoreCase(DisplayNameFormatterPane.DisplayNameChoice.SHORTSCHOOLNAME.toString())) {
                    if (icpcAccount == null) {
                        newDisplayName=account.getShortSchoolName();
                    } else {
                        newDisplayName=icpcAccount.getShortSchoolName();
                    }
                } else if (string.equalsIgnoreCase(DisplayNameFormatterPane.DisplayNameChoice.TEAMANDSHORTSCHOOLNAME.toString())) {
                    String externalName = "";
                    String shortSchoolName = "";
                    if (icpcAccount == null) {
                        externalName = account.getExternalName();
                        shortSchoolName = account.getShortSchoolName();
                    } else {
                        externalName = icpcAccount.getExternalName();
                        shortSchoolName = icpcAccount.getShortSchoolName();
                    }
                    if (externalName.equals("") && shortSchoolName.equals("")) {
                        // skip this account, no icpc data
                        continue;
                    } else {
                        newDisplayName=externalName+" ("+shortSchoolName+")";
                    }
                } else {
                    getController().getLog().info("Unknown display name choice " + string);
                    break;
                }
                if (!newDisplayName.equals("")) {
                    updateDisplayName(account, icpcAccount, newDisplayName);
                } // else skip this account, no icpc data
            }

            if (updatedAccountVector.size() > 0) {
                cancelButton.setText("Cancel");
            } else {
                cancelButton.setText("Close");
            }
            getUpdateButton().setEnabled(updatedAccountVector.size() > 0);
            getAccountListBox().autoSizeAllColumns();
        }
    }

    /**
     * This method will update the Account if the newDisplayName does not
     * equal the account2.getDisplayName().  If an update is required
     * the Account will be added to the updateAccountVector.
     *
     * @param account2
     * @param newDisplayName
     */
    private void updateDisplayName(Account account2, ICPCAccount icpcAccount, String newDisplayName) {
        if (showAllCheckBox.isSelected() || !account2.getDisplayName().equals(newDisplayName)) {
            // TODO would be nice if Account had a deep clone, actually we just need
            // a deep clone of the string fields
            Account account = new Account(account2.getClientId(), account2.getPassword(), account2.getClientId().getSiteNumber());
            account.clearListAndLoadPermissions(account2.getPermissionList());

            // Copy groups to new account
            HashSet<ElementId> groupsClean = account2.getGroupIds();
            if(groupsClean != null) {
                for(ElementId elementId : groupsClean) {
                    account.addGroupId(elementId, account2.getPrimaryGroupId() == elementId);
                }
            }
            account.setAliasName(new String(account2.getAliasName()));
            account.setDisplayName(new String(account2.getDisplayName()));
            account.setExternalId(new String(account2.getExternalId()));
            account.setExternalName(new String(account2.getExternalName()));
            account.setLongSchoolName(new String(account2.getLongSchoolName()));
            account.setShortSchoolName(new String(account2.getShortSchoolName()));
            if (account.isSameAs(account2)) {
                if (icpcAccount != null) {
                    account.setExternalId(icpcAccount.getExternalId());
                    account.setExternalName(icpcAccount.getExternalName());
                    account.setLongSchoolName(icpcAccount.getLongSchoolName());
                    account.setShortSchoolName(icpcAccount.getShortSchoolName());

                    HashMap<ElementId, String> groups = icpcAccount.getGroups();
                    if(groups != null) {
                        for(ElementId groupElementId : groups.keySet()) {
                            // TODO: when/if CMS supports multiple group ID's,
                            // then 'true' should only be used for the primary group (main)
                            account.addGroupId(groupElementId, true);
                        }
                    }
                }
                String oldDisplayName = account.getDisplayName();
                account.setDisplayName(newDisplayName);
                if (!newDisplayName.equals(oldDisplayName)) {
                    updatedAccountVector.add(account);
                }
                getAccountListBox().addRow(buildAccountRow(account2, newDisplayName));
            } else {
                getController().getLog().info("clone failed isSameAs test "+account+" vs "+account2);
            }
        }
    }

    /**
     * This method initializes this
     *
     */
    private void initialize() {
        this.setLayout(new BorderLayout());
        this.setSize(new java.awt.Dimension(600, 1500));
        this.setPreferredSize(new java.awt.Dimension(600, 1500));
        this.add(getDisplayNameFormatterPane(), java.awt.BorderLayout.NORTH);

        this.add(getAccountListBox(), java.awt.BorderLayout.CENTER);
    }

    @Override
    public String getPluginTitle() {
        return "ICPC Account Pane";
    }

    /**
     * This method initializes displayNameFormatterPane
     *
     * @return edu.csus.ecs.pc2.ui.DisplayNameFormatterPane
     */
    private DisplayNameFormatterPane getDisplayNameFormatterPane() {
        if (displayNameFormatterPane == null) {
            displayNameFormatterPane = new DisplayNameFormatterPane();
            displayNameFormatterPane.addPropertyChangeListener(DisplayNameFormatterPane.CHANGE_PROPERTY, new PropertyChangeListenerImplementation());
            displayChoice = displayNameFormatterPane.getCurrentSelection().toString();
        }
        return displayNameFormatterPane;
    }

    /**
     * This method initializes accountListbox
     *
     * @return com.ibm.webrunner.j2mclb.MultiColumnListbox
     */
    private MCLB getAccountListBox() {
        if (accountListBox == null) {
            accountListBox = new MCLB();
            accountListBox.add(getButtonPanel(), java.awt.BorderLayout.SOUTH);
            Object[] cols = {"Site", "Type", "Account Id", "Old Display Name", "New Display Name"};
            accountListBox.addColumns(cols);
            // Sorters
            HeapSorter sorter = new HeapSorter();
            HeapSorter numericStringSorter = new HeapSorter();
            numericStringSorter.setComparator(new NumericStringComparator());

            // Site
            accountListBox.setColumnSorter(0, sorter, 3);

            // Type
            accountListBox.setColumnSorter(1, sorter, 2);

            // Account Id
            accountListBox.setColumnSorter(2, numericStringSorter, 1);

            // Display Name
            accountListBox.setColumnSorter(3, sorter, 4);

            // new Display Name
            accountListBox.setColumnSorter(4, sorter, 5);

            cols = null;

            accountListBox.autoSizeAllColumns();

        }
        return accountListBox;
    }

    /**
     * This method initializes buttonPanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getButtonPanel() {
        if (buttonPanel == null) {
            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setHgap(25);
            buttonPanel = new JPanel();
            buttonPanel.setLayout(flowLayout);
            buttonPanel.add(getUpdateButton(), null);
            buttonPanel.add(getCancelButton(), null);
            buttonPanel.add(getShowAllCheckBox(), null);
        }
        return buttonPanel;
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
            // let changeDisplayName() changes to enabled as needed
            updateButton.setEnabled(false);
            updateButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    handleUpdateButton();
                }
            });
        }
        return updateButton;
    }

    protected void handleUpdateButton() {
        if (updatedAccountVector.size() > 0) {
            Account[] accounts = updatedAccountVector.toArray(new Account[updatedAccountVector.size()]);
            if (accounts != null) {
                getController().updateAccounts(accounts);
            }
        }
        if (getParentFrame() != null) {
            getParentFrame().setVisible(false);
        }
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
            cancelButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    handleCancelButton();
                }
            });
        }
        return cancelButton;
    }

    private void addWindowCloserListener() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (getParentFrame() != null) {
                    getParentFrame().addWindowListener(new java.awt.event.WindowAdapter() {
                        @Override
                        public void windowClosing(java.awt.event.WindowEvent e) {
                            handleCancelButton();
                        }
                    });
                }
            }
        });
    }

    @Override
    public void setContestAndController(IInternalContest inContest, IInternalController inController) {
        super.setContestAndController(inContest, inController);
        addWindowCloserListener();
        getContest().addAccountListener(new AccountListenerImplementation());
        changeDisplayName(displayChoice);
    }

    protected void handleCancelButton() {
        if (updatedAccountVector.size() > 0) {
            // Something changed, are they sure ?

            int result = FrameUtilities.yesNoCancelDialog(getParentFrame(), "Accounts modified, save changes?", "Confirm Choice");

            if (result == JOptionPane.YES_OPTION) {
                // pretend that hit update in that case
                handleUpdateButton();
            } else if (result == JOptionPane.NO_OPTION) {
                if (getParentFrame() != null) {
                    getParentFrame().setVisible(false);
                }
            } // cancel is a no-op
        } else {
            if (getParentFrame() != null) {
                getParentFrame().setVisible(false);
            }
        }
    }

    /**
     * @param icpcAccounts The icpcAccounts to set.
     */
    public void setIcpcAccounts(ICPCAccount[] icpcAccounts) {
        this.icpcAccounts = icpcAccounts;
    }

    /**
     * This method initializes showAllCheckBox
     *
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getShowAllCheckBox() {
        if (showAllCheckBox == null) {
            showAllCheckBox = new JCheckBox();
            showAllCheckBox.setText("Included unchanged accounts");
            showAllCheckBox.setPreferredSize(new Dimension(250, 24));
            showAllCheckBox.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    changeDisplayName(displayChoice);
                }
            });

        }
        return showAllCheckBox;
    }

} // @jve:decl-index=0:visual-constraint="10,10"
