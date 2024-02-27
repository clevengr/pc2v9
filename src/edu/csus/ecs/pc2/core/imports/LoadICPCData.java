// Copyright (C) 1989-2023 PC2 Development Team: John Clevenger, Douglas Lane, Samir Ashoo, and Troy Boudreau.
package edu.csus.ecs.pc2.core.imports;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import edu.csus.ecs.pc2.core.exception.IllegalTSVFormatException;
import edu.csus.ecs.pc2.core.list.SiteComparatorBySiteNumber;
import edu.csus.ecs.pc2.core.model.Account;
import edu.csus.ecs.pc2.core.model.ClientId;
import edu.csus.ecs.pc2.core.model.ClientType;
import edu.csus.ecs.pc2.core.model.Group;
import edu.csus.ecs.pc2.core.model.Site;
import edu.csus.ecs.pc2.core.util.TabSeparatedValueParser;

/**
 * Load ICPC CSV Data.
 *
 * @author pc2@ecs.csus.edu
 * @version $Id$
 */

// $HeadURL$
public final class LoadICPCData {
    private static final int TEAM_TAB = 11;

    private static final int SITE_TAB = 22;

    private static final int CONTEST_TAB = 33;

    /**
     * Caller is responsible for merging groups from ICPCImportData with the model grouplist and updating the ContestTitle.
     *
     * @param directory
     * @param sites
     * @return ICPCImportData populated with contestTitle and groups
     * @throws Exception
     */
    public static ICPCImportData loadSites(String directory, Site[] sites) throws Exception {
        if (sites != null) {
            Arrays.sort(sites, new SiteComparatorBySiteNumber());
        }
        ICPCImportData siteData = new ICPCImportData();

        String fs = File.separator;
        String path = directory + fs;
        String file = "PC2_Contest.tab";
        if (new File(path + file).exists()) {
            readFile(path + file, CONTEST_TAB, sites, siteData);
        } else {
            throw new FileNotFoundException(path + file);
        }

        file = "_PC2_Site.tab";
        if (!new File(path + file).exists()) {
            file = "PC2_Site.tab";
            if (!new File(path + file).exists()) {
                throw new FileNotFoundException(path + file);
            }
        }
        readFile(path + file, SITE_TAB, sites, siteData);

        return siteData;
    }

    /**
     * This will load the PC2_Team.tab, including populating ICPCAccount group and client (if sufficient data is provided).
     *
     * @param directory
     * @param groups
     * @param existingAccounts
     * @return ICPCImportdata with ICPCAccounts
     * @throws Exception
     */
    public static ICPCImportData loadAccounts(String directory, Group[] groups, Account[] existingAccounts) throws Exception {
        ICPCImportData accountData = new ICPCImportData();
        // Map of CMS Group ID's to PC2 Groups
        HashMap<String, Group> groupMap = new HashMap<String, Group>();
        if (groups != null) {
            for (Group group : groups) {
                if (group.getGroupId() != 0) {
                    groupMap.put(Integer.toString(group.getGroupId()), group);
                }
            }
        }

        HashMap<ClientId, Account> existingAccountsMap = new HashMap<ClientId, Account>();
        if (existingAccounts != null) {
            for (Account account : existingAccounts) {
                existingAccountsMap.put(account.getClientId(), account);
            }
        }

        String fs = File.separator;
        String path = directory + fs;
        String file = "_PC2_Team.tab";
        if (!new File(path + file).exists()) {
            file = "PC2_Team.tab";
            if (!new File(path + file).exists()) {
                throw new FileNotFoundException(path + file);
            }
        }
        readFile(path + file, TEAM_TAB, null, accountData);
        ICPCAccount[] accounts = accountData.getAccounts();
        if (accounts != null && accounts.length > 0) {
            for (ICPCAccount account : accounts) {
                // the external CMS group is was squirreled away when we processed the tab file in readFile above
                // It needs to be converted to an ElementId and added to the groups map for the account
                String extGroupId = account.getExternalGroupId();
                if (extGroupId != null && extGroupId.length() > 0) {
                    if (groupMap.containsKey(extGroupId)) {
                        Group group = groupMap.get(account.getExternalGroupId());
                        account.addGroupId(group.getElementId(), extGroupId);
                        // external group id string is no longer valid since we only support the hashmap
                        account.setExternalGroupId(null);
                        if (group.getSite() != null) {
                            int siteNum = group.getSite().getSiteNumber();
                            int accountNum = account.getAccountNumber();
                            ClientId clientId = new ClientId(siteNum, ClientType.Type.TEAM, accountNum);
                            if (existingAccountsMap.containsKey(clientId)) {
                                account.setClientId(clientId);
                            }
                        }
                    }
                }
            }
        }

        return accountData;
    }

    static void readFile(String filename, int fileType, Site[] sites, ICPCImportData importedData) throws Exception {
        Vector<ICPCAccount> accounts = new Vector<ICPCAccount>();
        Vector<Group> groups = new Vector<Group>();
        String line;
        int lineCount = 0;
        FileReader fileReader = new FileReader(filename);
        BufferedReader in = new BufferedReader(fileReader);
        line = in.readLine();
        lineCount++;
        while (line != null) {
            try {
                // skip comments and blank lines
                if (line.startsWith("#") || line.equals("")) {
                    line = in.readLine();
                    lineCount++;
                    continue;
                }
                String[] values = TabSeparatedValueParser.parseLine(line);
                // if 2nd field is empty, must be a empty line....
                if (values[1].equals("")) {
                    line = in.readLine();
                    lineCount++;
                    continue;
                }
                switch (fileType) {
                    case 11:
                        accounts.add(processTeam(values));
                        break;

                    case 22:
                        groups.add(processSite(values, sites));
                        break;

                    case 33:
                        importedData.setContestTitle(processContest(values));
                        break;

                    default:
                        new Exception("Unknown file type");
                        break;
                }
            } catch (IllegalTSVFormatException e2) {
                // already a properly formatted exception
                in.close();
                throw e2;
            } catch (Exception e) {
                String msg = "Error " + filename + ":" + lineCount + ": " + e.getMessage();
                Exception sendException = new Exception(msg);
                sendException.setStackTrace(e.getStackTrace());
                in.close();
                throw sendException;
            }
            line = in.readLine();
            lineCount++;
        }
        in.close();
        fileReader.close();
        in = null;
        fileReader = null;
        if (fileType == TEAM_TAB) {
            importedData.setAccounts(accounts.toArray(new ICPCAccount[accounts.size()]));
        } else if (fileType == SITE_TAB) {
            importedData.setGroups(groups.toArray(new Group[groups.size()]));
        }
    }

    /**
     * This processes a PC2_Team.tab entry.
     *
     * @param values
     * @return file populated ICPCAccount
     */
    private static ICPCAccount processTeam(String[] values) {
        int offset = 0;
        int clientNumber = 0;

        switch (values.length) {
            case 10:
                if (values[0].length() > 0) {
                    clientNumber = Integer.parseInt(values[0]);
                }
                offset = 0;
                break;
            case 9:
                // clientNumber++;
                offset = -1;
                break;
            default:
                // TODO consider throwing exception here
                break;
        }
        // TODO convert icpc site to pc2 site
        // String siteString = values[siteColumn];
        ICPCAccount account = new ICPCAccount();

        if (clientNumber != 0) {
            account.setAccountNumber(clientNumber);
        }
        account.setExternalId(values[1 + offset]);

        String groupId = values[2 + offset];

        if (groupId != null && groupId.trim().length() > 0) {
            account.setExternalGroupId(groupId.trim());
        }
        account.setExternalName(values[4 + offset]);
        account.setLongSchoolName(values[5 + offset]);
        account.setShortSchoolName(values[6 + offset]);
        return account;
    }

    /**
     * This processes a PC2_Site.tab entry, if their are 9 fields it will associate the Group to a Site based on the referenced pc2 site number.
     *
     * @param values
     * @param sites
     * @return populated (pc2) Group
     */
    private static Group processSite(String[] values, Site[] sites) {
        int offset;

        if (values.length == 8) {
            // we have no pc2 site info
            offset = -1;
        } else {
            offset = 0;
            // we have the pc2 site info
        }
        Group group = new Group(values[3 + offset]);
        // 0 is the pc2 site id
        if (values.length == 9 && sites != null) {
            int siteNum = Integer.parseInt(values[0]);
            if (siteNum <= sites.length) {
                group.setSite(sites[siteNum - 1].getElementId());
            }
        }
        group.setGroupId(Integer.parseInt(values[1 + offset]));
        // 2 is the contest Id
        return group;
    }

    /**
     * This will process the PC2_Contest.tab line to pull out the title.
     *
     * @param values
     * @return InternalContest Title
     */
    private static String processContest(String[] values) {
        String title = "";
        if (values.length > 0) {
            title = values[1];
        }
        return title;
    }

    /**
     *
     */
    private LoadICPCData() {
        super();
        // TODO Auto-generated constructor stub
    }
}
