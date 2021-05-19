package edu.csus.ecs.pc2.shadow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.csus.ecs.pc2.core.log.Log;
import edu.csus.ecs.pc2.core.standings.json.ScoreboardJsonModel;
import edu.csus.ecs.pc2.core.standings.json.TeamScoreRow;

public class ShadowScoreboardComparator {
    
    private Log log ;
    
    /**
     * Constructs a ShadowScoreboardComparator which can be used to generate comparison information for two
     * scoreboards -- typically, a PC2 Shadow scoreboard and a Remote CCS scoreboard.
     * 
     * @param log the Log used by methods of this class; must not be null.
     */
    public ShadowScoreboardComparator (Log log) {
        if (log==null) {
            throw new RuntimeException ("ShadowScoreboardComparator constructor called with null log");
        }
        this.log = log;
    }

    /**
     * Returns an array of {@link ShadowScoreboardRowComparison}s, where each array element contains comparison 
     * information between corresponding rows of the specified scoreboard JSON strings.
     * 
     *  The scoreboard JSON strings are expected to be in the format defined by the CLICS Contest API at
     *  https://ccs-specs.icpc.io/contest_api#scoreboard.
     *  
     *  The returned array contains one {@link ShadowScoreboardRowComparison} object for each corresponding 
     *  {@link TeamScoreRow} (that is, for each element of the "rows" array in the CLICS scoreboard JSON strings)
     *  which is present in both scoreboards. If there is an element which is present in one of the JSON strings
     *  but not in the other, the corresponding element in the returned array will contain a ScoreboardRowComparison 
     *  object in which one of the TeamScoreRow entries is null.
     *  
     *  If an error occurs while converting either input JSON string to a collection of corresponding TeamScoreRow
     *  objects, an array of size zero is returned.
     *  
     * @param pc2ScoreboardJson a CLICS Scoreboard JSON string for the current PC2 scoreboard.
     * @param remoteScoreboardJson a CLICS Scoreboard JSON string for the remote CCS being shadowed.
     * 
     * @return an array of comparisons between rows of the PC2 and Remote CCS scoreboards; the returned array
     *          will be empty (size=0) if an error occurs while processing the input JSON strings.
     */
    public ShadowScoreboardRowComparison [] compare (String pc2ScoreboardJson, String remoteScoreboardJson) {
        
        log.info("Generating ShadowScoreboardRowComparison info...");

        List<ShadowScoreboardRowComparison> comparedRows = new ArrayList<ShadowScoreboardRowComparison>();
        
        //create maps for both scoreboards.  The key in each map is the scoreboard rank; the value is
        // a PriorityQueue of TeamScoreRow objects (each containing a rank, teamId, and StandingScore (which in turn contains
        // the number solved and the total time)). The ordering in the PriorityQueue is by increasing team number.
        Map<Integer,PriorityQueue<TeamScoreRow>> pc2RowMap = createRowMap(pc2ScoreboardJson);
        Map<Integer,PriorityQueue<TeamScoreRow>> remoteRowMap = createRowMap(remoteScoreboardJson);
        
        if (pc2RowMap==null || pc2RowMap.keySet().isEmpty() || remoteRowMap==null || remoteRowMap.keySet().isEmpty()) {
            //something went wrong creating the necessary maps from JSON
            log.warning("Error processing JSON input resulted in null or empty row map");
            return new ShadowScoreboardRowComparison[0];
        }
        
        //find the highest rank in either map
        Integer highestPC2MapRankKey = Collections.max(pc2RowMap.keySet());
        Integer highestRemoteMapRankKey = Collections.max(remoteRowMap.keySet());
        int maxRank = Math.max(highestPC2MapRankKey,highestRemoteMapRankKey);

        //process every rank list
        for (int rank = 1; rank <= maxRank; rank++) {

            // get the priority queues for the current rank out of the two maps
            PriorityQueue<TeamScoreRow> pc2RankQueue = pc2RowMap.get(rank);
            PriorityQueue<TeamScoreRow> remoteRankQueue = remoteRowMap.get(rank);
            
            //skip this rank if there's no entries for it in either queue
            if (pc2RankQueue==null && remoteRankQueue==null) {
                continue;
            }

            // determine which list, if either, is larger
            int pc2QueueSize = 0;
            if (pc2RankQueue!=null) {
                pc2QueueSize = pc2RankQueue.size();
            }
            int remoteQueueSize = 0;
            if (remoteRankQueue!=null) {
                remoteQueueSize = remoteRankQueue.size();
            }

            int larger = pc2QueueSize;
            if (pc2QueueSize < remoteQueueSize) {
                larger = remoteQueueSize;
            }

            // process as many TeamScoreRows as exist in the larger list
            int current = 1;
            while (current++ <= larger) {

                // get the next row, if any, from both the larger and smaller lists
                // Note that poll() will return null once the corresponding list is exhausted.
                TeamScoreRow nextPC2Row = pc2RankQueue==null ? null : pc2RankQueue.poll();
                TeamScoreRow nextRemoteRow = remoteRankQueue ==null ? null : remoteRankQueue.poll();

                // add a comparison between the corresponding rows. Note that if either row reference
                // is null then getComparison() will include a null reference and will set "matches" to false.
                comparedRows.add(getComparison(nextPC2Row, nextRemoteRow));
            }

        }
        
        log.info("Returning ShadowScoreboardRowComparison info");
        
        ShadowScoreboardRowComparison [] retArray = new ShadowScoreboardRowComparison [comparedRows.size()];
        
        retArray = comparedRows.toArray(retArray);
        
        return retArray;
    }


    /**
     * Returns a {@link ShadowScoreboardRowComarison} object containing comparison information for the
     * two specified {@link TeamScoreRow}s.   
     * 
     * If either of the specified TeamScoreRows is null then the returned ShadowScoreboardRowComparison object
     * will contain null for that entry, along with a "match" value of false.
     * 
     * @param teamScoreRow1 the first TeamScoreRow to be compared.
     * @param teamScoreRow2 the second TeamScoreRow to be compared.
     * 
     * @return a ShadowScoreboardRowComparison object containing references to the two specified TeamScoreRows 
     *          (unless one or the other is null), along with a "matches" flag which will be true if and only if 
     *          the two TeamScoreRow objects match.
     */
    private ShadowScoreboardRowComparison getComparison(TeamScoreRow teamScoreRow1, TeamScoreRow teamScoreRow2) {

        ShadowScoreboardRowComparison comparison = new ShadowScoreboardRowComparison();
        
        //note that setting score rows into the comparison object automatically updates the "match" status in that object
        comparison.setSb1Row(teamScoreRow1);
        comparison.setSb2Row(teamScoreRow2);
        
        return comparison;
    }

    /**
     * Returns a Map whose keys are scoreboard rank numbers from the specified scoreboard JSON string (expected to 
     * be a legitimate {@link ScoreboardJsonModel} representation), and whose values are {@link PriorityQueue}s
     * linking all {@link TeamScoreRow}s for teams holding that rank.  The PriorityQueue for a given rank is ordered by
     * {@link TeamScoreRow#getTeam_id()}, with lower teamIds appearing first in the queue.  
     * 
     * Note that ideally the PriorityQueues would be order alphabetically by Team Display Name (since that most closely
     * matches the CLICS criterion for scoreboard ordering of teams tied for a given rank; however, this would require
     * a completely separate API access to obtain the Team Display Name (since the display name is not contained in
     * a TeamScoreRow -- only the TeamId).   It is thus the responsibility of separate code which wants to actually 
     * display a scoreboard (and further wants to display it by ordering teams with the same rank alphabetically)
     * to separately obtain the Team Display Names for tied teams and use those to adjust the PriorityQueue ordering
     * if necessary. 
     * 
     * @param scoreboardJson a JSON representation of a CLICS API Scoreboard.
     * 
     * @return a Map which maps Integer rank numbers to a list of TeamScoreRows for teams with that rank.
     * 
     */
    private Map<Integer, PriorityQueue<TeamScoreRow>> createRowMap(String scoreboardJson) {
        
        log.info("Creating scoreboard rank map from JSON scoreboard string");
        
        //debug:
//        System.out.println ("Creating scoreboard rank map from JSON scoreboard string");
        
        //initialize the map we're going to return
        Map<Integer, PriorityQueue<TeamScoreRow>> retMap = new HashMap<Integer, PriorityQueue<TeamScoreRow>>() ;
        
        try {
            
            //deserialize the input JSON to a ScoreboardJsonModel object
            ObjectMapper mapper = new ObjectMapper();
            ScoreboardJsonModel jsonModel = mapper.readValue(scoreboardJson, ScoreboardJsonModel.class);
            
            //get the list of TeamScoreRows out of the JSON model
            List<TeamScoreRow> rows = jsonModel.getRows();
            
            //add each TeamScoreRow to the appropriate PriorityQueue in the return map, using the rank as the queue selection key
            for (TeamScoreRow row : rows) {
                
                //determine the rank for the team represented by this row (note that more than one team can have the same rank, due to ties...)
                Integer rank = row.getRank();  //uses autoboxing
                
                //get the current entry for this rank (if any) from the map
                PriorityQueue<TeamScoreRow> rankQueue = retMap.get(rank);
                
                //add a Priority Queue for this rank if there wasn't already one there
                if (rankQueue==null) {
                    rankQueue = new PriorityQueue<TeamScoreRow>(new TeamScoreRowByTeamIdComparator());
                    retMap.put(rank, rankQueue);
                }
                
                rankQueue.add(row);
            }
            
            //debug:
//            System.out.println ("Scoreboard rank map:");
//            for (Integer rank : retMap.keySet()) {
//                PriorityQueue<TeamScoreRow> queue = retMap.get(rank);
//                System.out.print("   Rank " + rank + ": ");
//                for (TeamScoreRow row : queue) {
//                    System.out.print ("  Team " + row.getTeam_id() + " Solved " + row.getScore().getNum_solved() + " Time " + row.getScore().getTotal_time()
//                            + ";  ");
//                }
//                System.out.println();
//            }
            
            
        } catch (IOException e) {
            log.severe("Exception converting scoreboard JSON string to ScoreboardJsonModel object: " + e.getMessage());
            //debug
//            System.out.println("Exception converting scoreboard JSON string to ScoreboardJsonModel object: " + e.getMessage());
        }

        return retMap;
    }
    

    /**
     * This class provides a {@link Comparator} which returns an indication of the relative ordering of two 
     * {@link TeamScoreRow}s taking into account only the teamId.  That is, this method ranks TeamScoreRows by the numeric TeamId.
     * 
     * This is useful, for example, when other tests (e.g. a call to {@link TeamScoreRow#compareTo(TeamScoreRow)}
     * has determined that two TeamScoreRows are "equal" (that is, have the same rank, number solved, and total
     * points).  This method provides a way to order different teams in the absence of having the Team Display Name
     * available for alphabetic ordering.
     * 
     * @author John Clevenger, PC2 Development Team (pc2@ecs.csus.edu)
     */
    private class TeamScoreRowByTeamIdComparator implements Comparator<TeamScoreRow> {

        /**
         * This method returns an indication of whether the TeamId in the first specified TeamScoreRow is less than,
         * equal to, or greater than the TeamId in the second specified TeamScoreRow.
         * 
         * @param row1, row2: the two TeamScoreRows to be compared.
         *
         * @return -1, 0, or 1, depending on whether the TeamId in the first TeamScoreRow is less than, equal to, or 
         *          greater than the TeamId in the specified other TeamScoreRow respectively.
         */      
       @Override
        public int compare(TeamScoreRow row1, TeamScoreRow row2) {

            int firstTeamNum = row1.getTeam_id();
            int secondTeamNum = row2.getTeam_id();
            if (firstTeamNum == secondTeamNum) {
                return 0;
            } else if (firstTeamNum < secondTeamNum) {
                return -1;
            } else {
                return 1;
            }

        }
    }
}
