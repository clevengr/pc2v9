// Copyright (C) 1989-2024 PC2 Development Team: John Clevenger, Douglas Lane, Samir Ashoo, and Troy Boudreau.
package edu.csus.ecs.pc2.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import edu.csus.ecs.pc2.core.IInternalController;
import edu.csus.ecs.pc2.core.list.AccountComparator;
import edu.csus.ecs.pc2.core.list.GroupComparator;
import edu.csus.ecs.pc2.core.list.PermissionByDescriptionComparator;
import edu.csus.ecs.pc2.core.list.SiteComparatorBySiteNumber;
import edu.csus.ecs.pc2.core.model.Account;
import edu.csus.ecs.pc2.core.model.Category;
import edu.csus.ecs.pc2.core.model.Clarification.ClarificationStates;
import edu.csus.ecs.pc2.core.model.ClientId;
import edu.csus.ecs.pc2.core.model.ClientType;
import edu.csus.ecs.pc2.core.model.ClientType.Type;
import edu.csus.ecs.pc2.core.model.ContestInformation;
import edu.csus.ecs.pc2.core.model.ContestInformationEvent;
import edu.csus.ecs.pc2.core.model.DisplayTeamName;
import edu.csus.ecs.pc2.core.model.ElementId;
import edu.csus.ecs.pc2.core.model.Filter;
import edu.csus.ecs.pc2.core.model.FilterFormatter;
import edu.csus.ecs.pc2.core.model.Group;
import edu.csus.ecs.pc2.core.model.IContestInformationListener;
import edu.csus.ecs.pc2.core.model.IInternalContest;
import edu.csus.ecs.pc2.core.model.Judgement;
import edu.csus.ecs.pc2.core.model.Language;
import edu.csus.ecs.pc2.core.model.Problem;
import edu.csus.ecs.pc2.core.model.Run.RunStates;
import edu.csus.ecs.pc2.core.model.Site;
import edu.csus.ecs.pc2.core.security.Permission;

/**
 * Edit Filter GUI.
 *
 * @author pc2@ecs.csus.edu
 * @version $Id$
 */

// $HeadURL$
public class EditFilterPane extends JPanePlugin {

    /**
     *
     */
    private static final long serialVersionUID = 1866852944568248601L;

    private JCheckBox filterOnCheckBox = null;

    private JPanel problemsPane = null;  //  @jve:decl-index=0:visual-constraint="288,362"

    private JPanel bottomPanel = null;

    private JPanel languagesPane = null;  //  @jve:decl-index=0:visual-constraint="533,156"

    private JPanel teamsPane = null;  //  @jve:decl-index=0:visual-constraint="589,115"

    private JPanel groupsPane = null;  //  @jve:decl-index=0:visual-constraint="589,115"

    private JPanel judgementsPane = null;  //  @jve:decl-index=0:visual-constraint="349,385"

    private JPanel listsPanel = null;

    private JPanel mainPane = null;

    private JScrollPane judgementsScroll = null;

    private JScrollPane teamsScroll = null;

    private JScrollPane groupsScroll = null;

    private JScrollPane problemsScroll = null;

    private JScrollPane languagesScroll = null;

    private JCheckBoxJList judgementListBox = null;

    private DefaultListModel<WrapperJCheckBox> judgementListModel = new DefaultListModel<WrapperJCheckBox>();

    private JCheckBoxJList teamListBox = null;

    private DefaultListModel<WrapperJCheckBox> teamListModel = new DefaultListModel<WrapperJCheckBox>();

    private JCheckBoxJList groupListBox = null;

    private DefaultListModel<WrapperJCheckBox> groupListModel = new DefaultListModel<WrapperJCheckBox>();

    private JCheckBoxJList problemsListBox = null;

    private DefaultListModel<WrapperJCheckBox> problemListModel = new DefaultListModel<WrapperJCheckBox>();

    private JCheckBoxJList languagesListBox = null;

    private DefaultListModel<WrapperJCheckBox> languageListModel = new DefaultListModel<WrapperJCheckBox>();

    private Filter filter = new Filter();

    private JScrollPane jScrollPane = null;

    private JPanel runStatesPane = null;  //  @jve:decl-index=0:visual-constraint="464,193"

    private JCheckBoxJList runStatesListBox = null;

    private DefaultListModel<WrapperJCheckBox> runStatesListModel = new DefaultListModel<WrapperJCheckBox>();

    private DefaultListModel<WrapperJCheckBox> clarificationStatesListModel = new DefaultListModel<WrapperJCheckBox>();

    private DefaultListModel<WrapperJCheckBox> sitesListModel = new DefaultListModel<WrapperJCheckBox>();

    private DefaultListModel<WrapperJCheckBox> permissionsListModel = new DefaultListModel<WrapperJCheckBox>();

    private JPanel timeRangePane = null;

    private JLabel fromTimeLabel = null;

    private JTextField fromTimeTextField = null;

    private JLabel toTimeLabel = null;

    private JTextField toTimeTextField = null;

    private DisplayTeamName displayTeamName = null;  //  @jve:decl-index=0:

    private boolean isJudgeModule = false;

    private boolean filteringClarifications = false;

    private JPanel clarificationStatesPane = null;  //  @jve:decl-index=0:visual-constraint="642,361"

    private JScrollPane clarificationStateScrollPane = null;

    private JList<?> clarificationStatesListBox = null;

    private JPanel sitesPane = null;  //  @jve:decl-index=0:visual-constraint="649,76"

    private JPanel permissionsPane = null;

    private JScrollPane sitesScroll = null;

    private JScrollPane permissionScroll = null;

    private JCheckBoxJList siteListBox = null;

    private JCheckBoxJList permissionListBox = null;

    private JPanel accountsPane = null;

    private DefaultListModel<WrapperJCheckBox> accountListModel = new DefaultListModel<WrapperJCheckBox>();

    private JCheckBoxJList accountListBox = null;

    private JScrollPane accountScroll = null;

    private JPanel clientTypePane = null;

    private JScrollPane clientTypeScroll = null;

    private DefaultListModel<WrapperJCheckBox> clientTypeListModel = new DefaultListModel<WrapperJCheckBox>();

    private JCheckBoxJList clientTypesListBox;  //  @jve:decl-index=0:

    private Permission permission = new Permission();

    /**
     * JList names in EditFilterPane.
     *
     * @author pc2@ecs.csus.edu
     * @version $Id$
     */

    public enum ListNames {
        /**
         * Language Jlist.
         */
        LANGUAGES,
        /**
         * Problem Jlist.
         */
        PROBLEMS,
        /**
         * Judgements Jlist.
         */
        JUDGEMENTS,
        /**
         * Run States JList.
         */
        RUN_STATES,
        /**
         * Clarification States JList.
         */
        CLARIFICATION_STATES,
        /**
         * Accounts JList.
         */
        ALL_ACCOUNTS,
        /**
         *
         */
        TEAM_ACCOUNTS,

        /**
         *
         */
        GROUPS,

        /**
         * Elapsed Time (both From and To)
         */
        TIME_RANGE,
        /**
         * Sites.
         */
        SITES,
        /**
         *
         */
        PERMISSIONS,
        /**
         *
         */
        CLIENT_TYPES,
    }

    public EditFilterPane() {
        super();
        initialize();
    }



    public EditFilterPane(Filter filter) {
        super();
        initialize();
        this.filter = filter;
    }

    private void initialize() {
        this.setLayout(new BorderLayout());
        this.setSize(new java.awt.Dimension(493, 337));
        this.add(getMainPane(), java.awt.BorderLayout.CENTER);
    }

    @Override
    public String getPluginTitle() {
        return "Edit Filter";
    }

    /**
     * This method initializes filterOnCheckBox
     *
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getFilterOnCheckBox() {
        if (filterOnCheckBox == null) {
            filterOnCheckBox = new JCheckBox();
            filterOnCheckBox.setText("Filter On");
            filterOnCheckBox.setMnemonic(java.awt.event.KeyEvent.VK_F);
        }
        return filterOnCheckBox;
    }

    /**
     * This method initializes problemFrame
     *
     * @return javax.swing.JPanel
     */
    private JPanel getProblemsPane() {
        if (problemsPane == null) {
            problemsPane = new JPanel();
            problemsPane.setLayout(new BorderLayout());
            problemsPane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Problems", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                    javax.swing.border.TitledBorder.DEFAULT_POSITION, null, null));
            problemsPane.setName("problemFrame");
            problemsPane.add(getProblemsScroll(), java.awt.BorderLayout.CENTER);
        }
        return problemsPane;
    }

    /**
     * This method initializes bottomPanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getBottomPanel() {
        if (bottomPanel == null) {
            bottomPanel = new JPanel();
            bottomPanel.add(getFilterOnCheckBox(), null);
        }
        return bottomPanel;
    }

    /**
     * This method initializes languagePane
     *
     * @return javax.swing.JPanel
     */
    private JPanel getLanguagesPane() {
        if (languagesPane == null) {
            languagesPane = new JPanel();
            languagesPane.setLayout(new BorderLayout());
            languagesPane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Languages", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                    javax.swing.border.TitledBorder.DEFAULT_POSITION, null, null));
            languagesPane.setName("languagePane");
            languagesPane.add(getLanguagesScroll(), java.awt.BorderLayout.CENTER);
        }
        return languagesPane;
    }

    /**
     * This method initializes teamsFrame
     *
     * @return javax.swing.JPanel
     */
    private JPanel getTeamsPane() {
        if (teamsPane == null) {
            teamsPane = new JPanel();
            teamsPane.setLayout(new BorderLayout());
            teamsPane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Teams", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION,
                    null, null));
            teamsPane.setName("teamFrame");
            teamsPane.add(getTeamsScroll(), java.awt.BorderLayout.CENTER);
        }
        return teamsPane;
    }

    /**
     * This method initializes groupsFrame
     *
     * @return javax.swing.JPanel
     */
    private JPanel getGroupsPane() {
        if (groupsPane == null) {
            groupsPane = new JPanel();
            groupsPane.setLayout(new BorderLayout());
            groupsPane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Groups", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION,
                    null, null));
            groupsPane.setName("groupFrame");
            groupsPane.add(getGroupsScroll(), java.awt.BorderLayout.CENTER);
        }
        return groupsPane;
    }

    /**
     * This method initializes accountsFrame
     *
     * @return javax.swing.JPanel
     */
    private JPanel getAccountsPane() {
        if (accountsPane == null) {
            accountsPane = new JPanel();
            accountsPane.setLayout(new BorderLayout());
            accountsPane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Accounts", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                    javax.swing.border.TitledBorder.DEFAULT_POSITION, null, null));
            accountsPane.setName("accountFrame");
            accountsPane.add(getAccountScroll(), java.awt.BorderLayout.CENTER);
        }
        return accountsPane;
    }

    /**
     * This method initializes judgementFrame
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJudgementsPane() {
        if (judgementsPane == null) {
            judgementsPane = new JPanel();
            judgementsPane.setLayout(new BorderLayout());
            judgementsPane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Judgements", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                    javax.swing.border.TitledBorder.DEFAULT_POSITION, null, null));
            judgementsPane.setName("judgementFrame");
            judgementsPane.add(getJudgementsScroll(), java.awt.BorderLayout.CENTER);
        }
        return judgementsPane;
    }

    /**
     * This method initializes otherPanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getListsPanel() {
        if (listsPanel == null) {
            GridLayout gridLayout = new GridLayout();
            gridLayout.setRows(1);
            listsPanel = new JPanel();
            listsPanel.setLayout(gridLayout);

            // TODO remove this
//            listsPanel.add(getTimeRangePane(), null);
        }
        return listsPanel;
    }

    /**
     * This method initializes mainPane
     *
     * @return javax.swing.JPanel
     */
    private JPanel getMainPane() {
        if (mainPane == null) {
            mainPane = new JPanel();
            mainPane.setLayout(new BorderLayout());
            mainPane.add(getBottomPanel(), java.awt.BorderLayout.SOUTH);
            mainPane.add(getListsPanel(), java.awt.BorderLayout.CENTER);
        }
        return mainPane;
    }

    /**
     * This method initializes judgementsScroll
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJudgementsScroll() {
        if (judgementsScroll == null) {
            judgementsScroll = new JScrollPane();
            judgementsScroll.setViewportView(getJudgementListBox());
        }
        return judgementsScroll;
    }

    /**
     * This method initializes teamsScroll
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getTeamsScroll() {
        if (teamsScroll == null) {
            teamsScroll = new JScrollPane();
            teamsScroll.setViewportView(getTeamListBox());
        }
        return teamsScroll;
    }

    /**
     * This method initializes groupsScroll
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getGroupsScroll() {
        if (groupsScroll == null) {
            groupsScroll = new JScrollPane();
            groupsScroll.setViewportView(getGroupListBox());
        }
        return groupsScroll;
    }

    /**
     * This method initializes accountsScroll
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getAccountScroll() {
        if (accountScroll == null) {
            accountScroll = new JScrollPane();
            accountScroll.setViewportView(getAccountListBox());
        }
        return accountScroll;
    }
    /**
     * This method initializes problemsScroll
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getProblemsScroll() {
        if (problemsScroll == null) {
            problemsScroll = new JScrollPane();
            problemsScroll.setViewportView(getProblemsListBox());
        }
        return problemsScroll;
    }

    /**
     * This method initializes languagesScroll
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getLanguagesScroll() {
        if (languagesScroll == null) {
            languagesScroll = new JScrollPane();
            languagesScroll.setViewportView(getLanguagesListBox());
        }
        return languagesScroll;
    }

    /**
     * This method initializes judgementListBox
     *
     * @return javax.swing.JList
     */
    private JCheckBoxJList getJudgementListBox() {
        if (judgementListBox == null) {
            judgementListBox = new JCheckBoxJList(judgementListModel);
        }
        return judgementListBox;
    }

    /**
     * This method initializes teamListBox
     *
     * @return javax.swing.JTextArea
     */
    private JCheckBoxJList getTeamListBox() {
        if (teamListBox == null) {
            teamListBox = new JCheckBoxJList(teamListModel);
        }
        return teamListBox;
    }

    /**
     * This method initializes groupListBox
     *
     * @return javax.swing.JTextArea
     */
    private JCheckBoxJList getGroupListBox() {
        if (groupListBox == null) {
            groupListBox = new JCheckBoxJList(groupListModel);
        }
        return groupListBox;
    }

    /**
     * This method initializes teamListBox
     *
     * @return javax.swing.JTextArea
     */
    private JCheckBoxJList getAccountListBox() {
        if (accountListBox == null) {
            accountListBox = new JCheckBoxJList(accountListModel);
        }
        return accountListBox;
    }


    /**
     * This method initializes problemsListBox
     *
     * @return javax.swing.JList
     */
    private JCheckBoxJList getProblemsListBox() {
        if (problemsListBox == null) {
            problemsListBox = new JCheckBoxJList(problemListModel);
        }
        return problemsListBox;
    }

    /**
     * This method initializes languagesListBox
     *
     * @return javax.swing.JList
     */
    private JCheckBoxJList getLanguagesListBox() {
        if (languagesListBox == null) {
            languagesListBox = new JCheckBoxJList(languageListModel);
        }
        return languagesListBox;
    }

    /**
     * Populate the values for all JLists.
     *
     */
    public void populateFields() {

        getFilterOnCheckBox().setSelected(filter.isFilterOn());

        problemListModel.removeAllElements();
        if (isFilteringClarifications()){
            Category[] categories = getContest().getCategories();
            for (int i = 0; i < categories.length; i++) {
                Category category = categories[i];
                WrapperJCheckBox wrapperJCheckBox = new WrapperJCheckBox(category);
                if (filter.isFilteringProblems()) {
                    wrapperJCheckBox.setSelected(filter.matches(category));
                }
                problemListModel.addElement(wrapperJCheckBox);

            }
        }

        for (Problem problem : getContest().getProblems()) {
            WrapperJCheckBox wrapperJCheckBox = new WrapperJCheckBox(problem);
            if (filter.isFilteringProblems()) {
                wrapperJCheckBox.setSelected(filter.matches(problem));
            }
            problemListModel.addElement(wrapperJCheckBox);
        }

        languageListModel.removeAllElements();
        for (Language language : getContest().getLanguages()) {
            WrapperJCheckBox wrapperJCheckBox = new WrapperJCheckBox(language);
            if (filter.isFilteringLanguages()) {
                wrapperJCheckBox.setSelected(filter.matches(language));
            }
            languageListModel.addElement(wrapperJCheckBox);
        }

        sitesListModel.removeAllElements();
        Site [] sites = getContest().getSites();
        Arrays.sort (sites, new SiteComparatorBySiteNumber());
        for (Site site : sites) {
            WrapperJCheckBox wrapperJCheckBox = new WrapperJCheckBox(site, site.getSiteNumber()+" "+site.getDisplayName());
            if (filter.isFilteringSites()) {
                wrapperJCheckBox.setSelected(filter.matches(site));
            }
            sitesListModel.addElement(wrapperJCheckBox);
        }


        permissionsListModel.removeAllElements();
        Permission.Type[] types = Permission.Type.values();
        Arrays.sort(types, new PermissionByDescriptionComparator());
        for (Permission.Type type : types) {
            WrapperJCheckBox wrapperJCheckBox = new WrapperJCheckBox(type, permission.getDescription(type));
            if (filter.isFilteringPermissions()) {
                wrapperJCheckBox.setSelected(filter.matches(type));
            }
            permissionsListModel.addElement(wrapperJCheckBox);
        }

        clientTypeListModel.removeAllElements();
        for (Type type : Type.values()) {
            WrapperJCheckBox wrapperJCheckBox = new WrapperJCheckBox(type);
            if (filter.isFilteringAccounts()) {
                wrapperJCheckBox.setSelected(filter.matches(type));
            }
            clientTypeListModel.addElement(wrapperJCheckBox);
        }

        judgementListModel.removeAllElements();
        for (Judgement judgement : getContest().getJudgements()) {
            WrapperJCheckBox wrapperJCheckBox = new WrapperJCheckBox(judgement);
            if (filter.isFilteringJudgements()) {
                wrapperJCheckBox.setSelected(filter.matches(judgement));
            }
            judgementListModel.addElement(wrapperJCheckBox);
        }

        loadTeamNames (filter);

        loadGroupNames (filter);

        loadAccountNames(filter);

        runStatesListModel.removeAllElements();
        RunStates[] runStates = RunStates.values();
        for (RunStates runState : runStates) {
            WrapperJCheckBox wrapperJCheckBox = new WrapperJCheckBox(runState);
            if (filter.isFilteringRunStates()) {
                wrapperJCheckBox.setSelected(filter.matches(runState));
            }
            runStatesListModel.addElement(wrapperJCheckBox);
        }

        clarificationStatesListModel.removeAllElements();
        ClarificationStates[] clarificationStates = ClarificationStates.values();
        for (ClarificationStates clarificationState : clarificationStates) {
            WrapperJCheckBox wrapperJCheckBox = new WrapperJCheckBox(clarificationState);
            if (filter.isFilteringClarificationStates()) {
                wrapperJCheckBox.setSelected(filter.matches(clarificationState));
            }
            clarificationStatesListModel.addElement(wrapperJCheckBox);
        }

        getFromTimeTextField().setText("");
        getToTimeTextField().setText("");
        if (filter.isFilteringElapsedTime()) {
            if (filter.getStartElapsedTime() >= 0) {
                getFromTimeTextField().setText("" + filter.getStartElapsedTime());
            }
            if (filter.getEndElapsedTime() >= 0) {
                getToTimeTextField().setText("" + filter.getEndElapsedTime());
            }
        }
    }

    /**
     * Populate the team names when with display mask.
     *
     * This method also retains and re-populates the teams selected
     * not based on the input filter, but based on what the user has
     * selected.
     */
    protected void populateTeamNamesWithDisplayMask(){

        if (isJudgeModule) {
            ContestInformation contestInformation = getContest().getContestInformation();

            if (displayTeamName == null) {
                displayTeamName = new DisplayTeamName();
            }

            displayTeamName.setTeamDisplayMask(contestInformation.getTeamDisplayMode());

            // Save off selected teams into a filter.

            Filter teamsFilter = new Filter();

            teamsFilter.clearAccountList();
            Enumeration<?> enumeration = teamListModel.elements();
            while (enumeration.hasMoreElements()) {
                WrapperJCheckBox element = (WrapperJCheckBox) enumeration.nextElement();
                if (element.isSelected()) {
                    Object object = element.getContents();
                    teamsFilter.addAccount((ClientId) object);
                }
            }

            // load selected teams and set checkbox based on filter
            loadTeamNames(teamsFilter);

        }
    }

    private void loadTeamNames(Filter inFilter) {
        Vector<Account> vector = getContest().getAccounts(ClientType.Type.TEAM);
        Account[] accounts = vector.toArray(new Account[vector.size()]);
        Arrays.sort(accounts, new AccountComparator());

        teamListModel.removeAllElements();
        WrapperJCheckBox wrapperJCheckBox = null;
        for (Account account : accounts) {
            if (displayTeamName != null) {
                wrapperJCheckBox = new WrapperJCheckBox(account.getClientId(), displayTeamName);
            } else {
                wrapperJCheckBox = new WrapperJCheckBox(account.getClientId());
            }
            if (inFilter.isFilteringAccounts()) {
                wrapperJCheckBox.setSelected(inFilter.matches(account));
            }
            teamListModel.addElement(wrapperJCheckBox);
        }
    }

    private void loadGroupNames(Filter inFilter) {
        Group [] groups = getContest().getGroups();
        Arrays.sort(groups, new GroupComparator());

        groupListModel.removeAllElements();
        WrapperJCheckBox wrapperJCheckBox = null;
        for (Group group : groups) {
            ElementId groupElementId = group.getElementId();
            wrapperJCheckBox = new WrapperJCheckBox(groupElementId, group.getDisplayName());
            if (inFilter.isFilteringGroups()) {
                wrapperJCheckBox.setSelected(inFilter.matches(group));
            }
            groupListModel.addElement(wrapperJCheckBox);
        }
    }

    private void loadAccountNames(Filter inFilter) {
        Account[] accounts = getContest().getAccounts();
        Arrays.sort(accounts, new AccountComparator());

        accountListModel.removeAllElements();
        WrapperJCheckBox wrapperJCheckBox = null;

        for (Account account : accounts) {
            if (displayTeamName != null) {
                wrapperJCheckBox = new WrapperJCheckBox(account.getClientId(), displayTeamName);
            } else {
                wrapperJCheckBox = new WrapperJCheckBox(account.getClientId());
            }
            if (inFilter.isFilteringAccounts()) {
                wrapperJCheckBox.setSelected(inFilter.matches(account));
            }
            accountListModel.addElement(wrapperJCheckBox);
        }
    }



    @Override
    public void setContestAndController(IInternalContest inContest, IInternalController inController) {

        super.setContestAndController(inContest, inController);

        getContest().addContestInformationListener(new ContestInformationListenerImplementation());

        isJudgeModule = getContest().getClientId().getClientType().equals(Type.JUDGE);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                populateFields();
            }
        });
    }

    public Filter getFilter() {

        // Derive filter based on settings

        filter.setFilter(getFilterOnCheckBox().isSelected());

        filter.clearProblemList();
        Enumeration<?> enumeration = problemListModel.elements();
        while (enumeration.hasMoreElements()) {
            WrapperJCheckBox element = (WrapperJCheckBox) enumeration.nextElement();
            if (element.isSelected()) {
                Object object = element.getContents();
                filter.addProblem((Problem) object);
            }
        }

        filter.clearLanguageList();
        enumeration = languageListModel.elements();
        while (enumeration.hasMoreElements()) {
            WrapperJCheckBox element = (WrapperJCheckBox) enumeration.nextElement();
            if (element.isSelected()) {
                Object object = element.getContents();
                filter.addLanguage((Language) object);
            }
        }

        filter.clearAccountList();
        enumeration = teamListModel.elements();
        while (enumeration.hasMoreElements()) {
            WrapperJCheckBox element = (WrapperJCheckBox) enumeration.nextElement();
            if (element.isSelected()) {
                Object object = element.getContents();
                filter.addAccount((ClientId) object);
            }
        }

        enumeration = accountListModel.elements();
        while (enumeration.hasMoreElements()) {
            WrapperJCheckBox element = (WrapperJCheckBox) enumeration.nextElement();
            if (element.isSelected()) {
                Object object = element.getContents();
                filter.addAccount((ClientId) object);
            }
        }


        filter.clearRunStatesList();
        enumeration = runStatesListModel.elements();
        while (enumeration.hasMoreElements()) {
            WrapperJCheckBox element = (WrapperJCheckBox) enumeration.nextElement();
            if (element.isSelected()) {
                Object object = element.getContents();
                filter.addRunState((RunStates) object);
            }
        }

        filter.clearClarificationStateList();
        enumeration = clarificationStatesListModel.elements();
        while (enumeration.hasMoreElements()) {
            WrapperJCheckBox element = (WrapperJCheckBox) enumeration.nextElement();
            if (element.isSelected()) {
                Object object = element.getContents();
                filter.addClarificationState((ClarificationStates) object);
            }
        }

        filter.clearJudgementList();
        enumeration = judgementListModel.elements();
        while (enumeration.hasMoreElements()) {
            WrapperJCheckBox element = (WrapperJCheckBox) enumeration.nextElement();
            if (element.isSelected()) {
                Object object = element.getContents();
                filter.addJudgement((Judgement) object);
            }
        }

        filter.clearSiteList();
        enumeration = sitesListModel.elements();
        while (enumeration.hasMoreElements()) {
            WrapperJCheckBox element = (WrapperJCheckBox) enumeration.nextElement();
            if (element.isSelected()) {
                Object object = element.getContents();
                filter.addSite((Site) object);
            }
        }

        filter.clearPermissionsList();
        enumeration = permissionsListModel.elements();
        while (enumeration.hasMoreElements()) {
            WrapperJCheckBox element = (WrapperJCheckBox) enumeration.nextElement();
            if (element.isSelected()) {
                Object object = element.getContents();
                filter.addPermission((Permission.Type) object);
            }
        }

        filter.clearGroupsList();
        enumeration = groupListModel.elements();
        while (enumeration.hasMoreElements()) {
            WrapperJCheckBox element = (WrapperJCheckBox) enumeration.nextElement();
            if (element.isSelected()) {
                Object object = element.getContents();
                filter.addGroup((ElementId)object);
            }
        }


        filter.clearClientTypesList();
        enumeration = clientTypeListModel.elements();
        while (enumeration.hasMoreElements()) {
            WrapperJCheckBox element = (WrapperJCheckBox) enumeration.nextElement();
            if (element.isSelected()) {
                Object object = element.getContents();
                filter.addClientType((Type) object);
            }
        }

        filter.clearElapsedTimeRange();
        if (getFromTimeTextField().getText().length() > 0){
            filter.setStartElapsedTime(Long.parseLong(getFromTimeTextField().getText()));
        }

        if (getToTimeTextField().getText().length() > 0){
            filter.setEndElapsedTime(Long.parseLong(getToTimeTextField().getText()));
        }

        return filter;
    }

    /**
     * Assigns filter and repopulates fields.
     *
     * @param filter
     */
    public void setFilter(Filter filter) {
        this.filter = filter;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                populateFields();
            }
        });
    }

    protected void printAllSpecifiers(String prefix, IInternalContest contest, Filter inFilter) {
        String[] names = { FilterFormatter.ACCOUNT_SPECIFIER, FilterFormatter.JUDGMENTS_SPECIFIER, FilterFormatter.LANGUAGES_SPECIFIER, FilterFormatter.NUMBER_ACCOUNTS_SPECIFIER,
                FilterFormatter.NUMBER_JUDGEMENTS_SPECIFIER, FilterFormatter.NUMBER_LANGUAGES_SPECIFIER, FilterFormatter.NUMBER_PROBLEMS_SPECIFIER, FilterFormatter.PROBLEMS_SPECIFIER,
                FilterFormatter.SHORT_ACCOUNT_NAMES_SPECIFIER, FilterFormatter.TEAM_LIST_SPECIFIER, FilterFormatter.TEAM_LONG_LIST_SPECIFIER, FilterFormatter.START_TIME_RANGE_SPECIFIER,
                FilterFormatter.END_TIME_RANGE_SPECIFIER };

        Arrays.sort(names);

        FilterFormatter filterFormatter = new FilterFormatter();
        for (String string : names) {
            System.out.println(prefix + " " + string + " '" + filterFormatter.format(string, contest, inFilter) + "'");
        }

    }

    /**
     * This method initializes jScrollPane
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView(getRunStatesListBox());
        }
        return jScrollPane;
    }

    /**
     * This method initializes runStatesPane
     *
     * @return javax.swing.JPanel
     */
    private JPanel getRunStatesPane() {
        if (runStatesPane == null) {
            runStatesPane = new JPanel();
            runStatesPane.setLayout(new BorderLayout());
            runStatesPane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Run States", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                    javax.swing.border.TitledBorder.DEFAULT_POSITION, null, null));
            runStatesPane.add(getJScrollPane(), java.awt.BorderLayout.CENTER);
        }
        return runStatesPane;
    }

    /**
     * This method initializes runStatesListBox
     *
     * @return edu.csus.ecs.pc2.ui.JCheckBoxJList
     */
    private JCheckBoxJList getRunStatesListBox() {
        if (runStatesListBox == null) {
            runStatesListBox = new JCheckBoxJList(runStatesListModel);
        }
        return runStatesListBox;
    }

    /**
     * Add a list to the criteria.
     *
     * @param listName
     */
    public void addList (ListNames listName){
        switch (listName) {
            case ALL_ACCOUNTS:
                listsPanel.add(getAccountsPane(), 0);
                break;
            case TEAM_ACCOUNTS:
                listsPanel.add(getTeamsPane(), 0);
                break;
            case GROUPS:
                listsPanel.add(getGroupsPane(), 0);
                break;
            case LANGUAGES:
              listsPanel.add(getLanguagesPane(), 0);
                break;
            case PROBLEMS:
              listsPanel.add(getProblemsPane(), 0);
                break;
            case JUDGEMENTS:
              listsPanel.add(getJudgementsPane(), 0);
                break;
            case RUN_STATES:
              listsPanel.add(getRunStatesPane(), 0);
                break;
            case CLARIFICATION_STATES:
              listsPanel.add(getClarificationStatesPane(), 0);
                break;
            case TIME_RANGE:
                listsPanel.add(getTimeRangePane(), 0);
                break;
            case SITES:
                listsPanel.add(getSitesPane(), 0);
                break;
            case PERMISSIONS:
                listsPanel.add(getPermissionsPane(), 0);
                break;
            case CLIENT_TYPES:
                listsPanel.add(getClientTypePane(), 0);
                break;
            default:
                throw new InvalidParameterException("Invalid listNames: " + listName);
        }
    }

    /**
     * This method initializes timeRangePane
     *
     * @return javax.swing.JPanel
     */
    private JPanel getTimeRangePane() {
        if (timeRangePane == null) {
            toTimeLabel = new JLabel();
            toTimeLabel.setText("To");
            fromTimeLabel = new JLabel();
            fromTimeLabel.setText("From");
            timeRangePane = new JPanel();
            timeRangePane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Time Range", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                    javax.swing.border.TitledBorder.DEFAULT_POSITION, null, null));
            timeRangePane.add(fromTimeLabel, null);
            timeRangePane.add(getFromTimeTextField(), null);
            timeRangePane.add(toTimeLabel, null);
            timeRangePane.add(getToTimeTextField(), null);
        }
        return timeRangePane;
    }

    /**
     * This method initializes fromTimeTextField
     *
     * @return javax.swing.JTextField
     */
    private JTextField getFromTimeTextField() {
        if (fromTimeTextField == null) {
            fromTimeTextField = new JTextField();
            fromTimeTextField.setDocument(new IntegerDocument());
            fromTimeTextField.setPreferredSize(new java.awt.Dimension(60, 20));
        }
        return fromTimeTextField;
    }

    /**
     * This method initializes toTimeTextField
     *
     * @return javax.swing.JTextField
     */
    private JTextField getToTimeTextField() {
        if (toTimeTextField == null) {
            toTimeTextField = new JTextField();
            toTimeTextField.setDocument(new IntegerDocument());
            toTimeTextField.setPreferredSize(new java.awt.Dimension(60, 20));
        }
        return toTimeTextField;
    }

    public DisplayTeamName getDisplayTeamName() {
        return displayTeamName;
    }

    public void setDisplayTeamName(DisplayTeamName displayTeamName) {
        this.displayTeamName = displayTeamName;
    }

    /**
     * Contest Listener for Edit Filter Pane.
     *
     * This listens for changes in the way the team display is to
     * displayed aka the  Team Information Displayed to Judges setting
     *
     * @author pc2@ecs.csus.edu
     * @version $Id$
     */

    public class ContestInformationListenerImplementation implements IContestInformationListener {

        @Override
        public void contestInformationAdded(ContestInformationEvent event) {
            populateTeamNamesWithDisplayMask();
        }

        @Override
        public void contestInformationChanged(ContestInformationEvent event) {
            populateTeamNamesWithDisplayMask();
        }

        @Override
        public void contestInformationRemoved(ContestInformationEvent event) {
            populateTeamNamesWithDisplayMask();
        }

        @Override
        public void contestInformationRefreshAll(ContestInformationEvent contestInformationEvent) {
            populateTeamNamesWithDisplayMask();
        }

        @Override
        public void finalizeDataChanged(ContestInformationEvent contestInformationEvent) {
            // Not used
        }
    }

    /**
     * This method initializes clarificationsPane
     *
     * @return javax.swing.JPanel
     */
    private JPanel getClarificationStatesPane() {
        if (clarificationStatesPane == null) {
            clarificationStatesPane = new JPanel();
            clarificationStatesPane.setLayout(new BorderLayout());
            clarificationStatesPane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Clar States", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                    javax.swing.border.TitledBorder.DEFAULT_POSITION, null, null));
            clarificationStatesPane.setVisible(true);
            clarificationStatesPane.add(getClarificationStateScrollPane(), java.awt.BorderLayout.CENTER);
        }
        return clarificationStatesPane;
    }

    /**
     * This method initializes clarificationStateScrollPane
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getClarificationStateScrollPane() {
        if (clarificationStateScrollPane == null) {
            clarificationStateScrollPane = new JScrollPane();
            clarificationStateScrollPane.setViewportView(getClarificationStatesListBox());
        }
        return clarificationStateScrollPane;
    }

    /**
     * This method initializes clarificationStatesListBox
     *
     * @return javax.swing.JList
     */
    private JList<?> getClarificationStatesListBox() {
        if (clarificationStatesListBox == null) {
            clarificationStatesListBox = new JCheckBoxJList(clarificationStatesListModel);
        }
        return clarificationStatesListBox;
    }

    public boolean isFilteringClarifications() {
        return filteringClarifications;
    }

    public void setFilteringClarifications(boolean filteringClarifications) {
        this.filteringClarifications = filteringClarifications;
    }

    /**
     * This method initializes jPanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getSitesPane() {
        if (sitesPane == null) {
            sitesPane = new JPanel();
            sitesPane.setLayout(new BorderLayout());
            sitesPane.setName("sitesPane");
            sitesPane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Sites", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION,
                    new java.awt.Font("Dialog", java.awt.Font.BOLD, 12), new java.awt.Color(51, 51, 51)));
            sitesPane.setSize(new java.awt.Dimension(251, 151));
            sitesPane.add(getSitesScroll(), java.awt.BorderLayout.CENTER);
        }
        return sitesPane;
    }


    public JPanel getClientTypePane() {
        if (clientTypePane == null) {
            clientTypePane = new JPanel();
            clientTypePane.setLayout(new BorderLayout());
            clientTypePane.setName("clientTypePane");
            clientTypePane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "ClientType", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                    javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", java.awt.Font.BOLD, 12), new java.awt.Color(51, 51, 51)));
            clientTypePane.setSize(new java.awt.Dimension(251, 151));
            clientTypePane.add(getClientTypeScroll(), java.awt.BorderLayout.CENTER);
        }

        return clientTypePane;
    }

    private JScrollPane getClientTypeScroll() {
        if (clientTypeScroll == null) {
            clientTypeScroll = new JScrollPane();
            clientTypeScroll.setViewportView(getClientTypesListBox());
        }
        return clientTypeScroll;
    }

    private JCheckBoxJList getClientTypesListBox () {
        if (clientTypesListBox == null) {
            clientTypesListBox = new JCheckBoxJList(clientTypeListModel);
        }
        return clientTypesListBox;
    }



    public JPanel getPermissionsPane() {
        if (permissionsPane == null) {
            permissionsPane = new JPanel();
            permissionsPane.setLayout(new BorderLayout());
            permissionsPane.setName("permissionsPane");
            permissionsPane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Permissions", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                    javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", java.awt.Font.BOLD, 12), new java.awt.Color(51, 51, 51)));
            permissionsPane.setSize(new java.awt.Dimension(251, 151));
            permissionsPane.add(getPermissionsScroll(), java.awt.BorderLayout.CENTER);
        }

        return permissionsPane;
    }

    /**
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getPermissionsScroll() {
        if (permissionScroll == null) {
            permissionScroll = new JScrollPane();
            permissionScroll.setViewportView(getPermissionsListBox());
        }
        return permissionScroll;
    }

    /**
     * This method initializes permissionListBox
     *
     * @return edu.csus.ecs.pc2.ui.JCheckBoxJList
     */
    private JCheckBoxJList getPermissionsListBox() {
        if (permissionListBox == null) {
            permissionListBox = new JCheckBoxJList(permissionsListModel);
        }
        return permissionListBox;
    }


    /**
     * This method initializes siteSCroll
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getSitesScroll() {
        if (sitesScroll == null) {
            sitesScroll = new JScrollPane();
            sitesScroll.setViewportView(getSiteListBox());
        }
        return sitesScroll;
    }

    /**
     * This method initializes siteListBox
     *
     * @return edu.csus.ecs.pc2.ui.JCheckBoxJList
     */
    private JCheckBoxJList getSiteListBox() {
        if (siteListBox == null) {
            siteListBox = new JCheckBoxJList(sitesListModel);
        }
        return siteListBox;
    }


} // @jve:decl-index=0:visual-constraint="10,10"
