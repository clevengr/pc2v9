// Copyright (C) 1989-2019 PC2 Development Team: John Clevenger, Douglas Lane, Samir Ashoo, and Troy Boudreau.
import java.util.Date;

/**
 * Sumit that will cause a TLE.
 * 
 * @author Douglas A. Lane, PC^2 Team, pc2@ecs.csus.edu
 */
public class SumitTLE {
    public static void main(String[] args)
    {
        try
        {
            // do a nice loop and print date every 30 seconds

            long pauseMS = 30 * 1000; // 30 seconds

            System.out.println("Started at " + new Date());

            while (true)
            {
                Thread.sleep(pauseMS);
                System.out.println(new Date());
            }

        } catch (Exception e)
        {
            System.out.println("Possible trouble reading stdin");
            System.out.println("Message: " + e.getMessage());
        }
    }
}

// eof SumitTLE.java
