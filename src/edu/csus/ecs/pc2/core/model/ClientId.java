// Copyright (C) 1989-2019 PC2 Development Team: John Clevenger, Douglas Lane, Samir Ashoo, and Troy Boudreau.
package edu.csus.ecs.pc2.core.model;

import java.io.Serializable;

import edu.csus.ecs.pc2.core.InternalController;
import edu.csus.ecs.pc2.core.model.ClientType.Type;
import edu.csus.ecs.pc2.core.packet.Packet;
import edu.csus.ecs.pc2.core.transport.ConnectionHandlerID;

/**
 * Client Identification Information (site, type, and number).
 *
 * Contains site number, {@link ClientType.Type}, clientNumber, active.
 * 
 * Also contains support for external code to set/get the {@link ConnectionHandlerID}
 * associated with this ClientId (which is null by default).
 *
 * @author pc2@ecs.csus.edu
 */

// TODO doc complete javadoc for methods.

public class ClientId implements Serializable {

    private static final long serialVersionUID = -3481561733498755619L;

    public static final int UNSET = 0;

    private int siteNumber = UNSET;

    private int clientNumber = 0;

    private ClientType.Type clientType = ClientType.Type.UNKNOWN;
    
    private ConnectionHandlerID connectionHandlerID = null;

    public ClientId(int siteNumber, ClientType.Type type, int clientNumber) {
        this.siteNumber = siteNumber;
        this.clientNumber = clientNumber;
        clientType = type;
    }

    private boolean active = true;

    /**
     * Returns a String containing the type, number and site associated with this ClientId.
     * 
     * Example: for team 4 site 3, will return "TEAM4 @ site 3".
     * 
     */
    public String toString() {
        return clientType.toString() + clientNumber + " @ site " + siteNumber;
    }

    /**
     * Gets the client number.
     * 
     * The client number is assigned to each Account/Client, for example
     * for team 5 the client number is 5.
     * @return Returns the clientNumber.
     */
    public int getClientNumber() {
        return clientNumber;
    }

    /**
     * Get the client type.
     * 
     * The client type is one of the predefined client types
     * found in {@link ClientType.Type}.
     * 
     * @return Returns the clientType.
     */
    public ClientType.Type getClientType() {
        return clientType;
    }

    /**
     * @return Returns the siteNumber.
     */
    public int getSiteNumber() {
        return siteNumber;
    }

    /**
     * Returns short name of client in lower case.
     *
     * Note that admin 0 returns root.
     *
     * @return short version of name (team1, admin1)
     */
    public String getName() {
        if (clientNumber == 0 && clientType == Type.ADMINISTRATOR) {
            return "root";
        }

        return new String("" + clientType + clientNumber).toLowerCase();
    }

    /**
     * 
     * @return true if client is marked as active.
     */
    protected boolean isActive() {
        return active;
    }

    protected void setActive(boolean active) {
        this.active = active;
    }

    public int hashCode() {
        return toString().hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj instanceof ClientId) {
            ClientId otherId = (ClientId) obj;
            return (getClientType() == otherId.getClientType()
                    && getClientNumber() == otherId.getClientNumber() && getSiteNumber() == otherId
                    .getSiteNumber());
        } else {
            return false;
        }
    }

    /**
     * Get Triplet Key (SiteNumber and ClientType and ClientNumber).
     * 
     * For team 5 site 12 will return "12TEAM5".
     * 
     * @return a string that is composed of site number and client type name and client number.
     */
    public String getTripletKey() {
        return getSiteNumber() + getClientType().toString() + getClientNumber();
    }

    public void setSiteNumber(int siteNumber) {
        this.siteNumber = siteNumber;
    }

    /**
     * Returns the {@link ConnectionHandlerID} associated with this ClientId.
     * Note that when a ClientId is first constructed it has a ConnectionHandlerID of null;
     * it is the responsibility of external users of this class to set the ConnectionHandlerID
     * if needed.  Typically this is done when a ClientId is passed to a Server as part of
     * a PC2 Transport {@link Packet}.
     * 
     * @return the connectionHandlerID associated with this ClientId, which may be null.
     * 
     * @see {@link InternalController#receiveObject(Serializable, ConnectionHandlerID)}
     */
    public ConnectionHandlerID getConnectionHandlerID() {
        return connectionHandlerID;
    }

    /**
     * Sets the {@link ConnectionHandlerID} associated with this ClientId.
     * 
     * @param connectionHandlerID the connectionHandlerID value to set in this CientId.
     */
    public void setConnectionHandlerID(ConnectionHandlerID connectionHandlerID) {
        this.connectionHandlerID = connectionHandlerID;
    }

}
