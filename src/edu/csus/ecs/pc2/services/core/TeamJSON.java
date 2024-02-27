// Copyright (C) 1989-2019 PC2 Development Team: John Clevenger, Douglas Lane, Samir Ashoo, and Troy Boudreau.
package edu.csus.ecs.pc2.services.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.csus.ecs.pc2.core.model.Account;
import edu.csus.ecs.pc2.core.model.Group;
import edu.csus.ecs.pc2.core.model.IInternalContest;

/**
 * Team JSON
 *
 * TODO: Remove this class as it is never referenced -- JB
 *
 * @author Douglas A. Lane, PC^2 Team, pc2@ecs.csus.edu
 */
public class TeamJSON extends JSONUtilities  {

    private ObjectMapper mapper = new ObjectMapper();
    private ObjectNode element = mapper.createObjectNode();
    private ArrayNode childNode = mapper.createArrayNode();

    public String createJSON(IInternalContest contest, Account account) {

        element = mapper.createObjectNode();
        childNode = mapper.createArrayNode();

        // TODO multi-site with overlapping teamNumbers?

        //    id
        //    icpc_id
        //    name
        //    organization_id

        element.put("id", new Integer(account.getClientId().getClientNumber()).toString());
        if (notEmpty(account.getExternalId())) {
            element.put("icpc_id", account.getExternalId());
        }
        element.put("name", account.getTeamName());
        if (notEmpty(account.getInstitutionCode()) && !account.getInstitutionCode().equals("undefined")) {
            element.put("organization_id", account.getInstitutionCode());
        }
        if (account.getPrimaryGroupId() != null) {
            Group group = contest.getGroup(account.getPrimaryGroupId());
            element.put("group_id",  Integer.toString(group.getGroupId()));
        }

        // rest is provided by CDS, not CCS
        childNode.add(element);

        return stripOuterJSON(childNode.toString());
    }
}
