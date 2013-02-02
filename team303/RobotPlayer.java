package team303;

import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.GameObject;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.Clock;
/** Haitao's first attempt at a good macro bot.
 */
public class RobotPlayer {
	public static void run(RobotController rc) throws GameActionException{
		BasePlayer br;
		switch(rc.getType()) {
		case HQ:
			br = new HQPlayer(rc);
			break;
		case SOLDIER:
			if (Clock.getRoundNum() < 10){
				br = new AngryPlayer(rc);
				System.out.println("Angry");
			}
			else if (rc.readBroadcast(9999) + rc.readBroadcast(9978)< rc.senseAllEncampmentSquares().length/8 & Clock.getRoundNum() < 50){
				if (rc.readBroadcast(9978)*3 < rc.readBroadcast(9999)){
					br = new GatherPlayer2(rc);
					System.out.println("GatherPlayer2");
				}
				else{
					br = new GatherPlayer(rc);
					System.out.println("gather");
				}
			
			}
			else if (rc.readBroadcast(9999) + rc.readBroadcast(9978)< rc.senseAllEncampmentSquares().length/6 & 50 < Clock.getRoundNum() & Clock.getRoundNum() < 100){
				if (rc.readBroadcast(9978)*3 < rc.readBroadcast(9999)){
					br = new GatherPlayer2(rc);
					System.out.println("GatherPlayer2");
				}
				else{
					br = new GatherPlayer(rc);
					System.out.println("gather");
				}
			}
			else if (rc.readBroadcast(9999) + rc.readBroadcast(9978)< rc.senseAllEncampmentSquares().length/4 & 100 < Clock.getRoundNum()){
				if (rc.readBroadcast(9978)*3 < rc.readBroadcast(9999)){
					br = new GatherPlayer2(rc);
					System.out.println("GatherPlayer2");
				}
				else{
					br = new GatherPlayer(rc);
					System.out.println("gather");
				}
			}
			else{
				if ((rc.senseNearbyGameObjects(Robot.class,14,rc.getTeam()).length > 5) & (rc.senseNearbyGameObjects(Robot.class,100,rc.getTeam().opponent()).length < 1)){
						br = new SwarmPlayer(rc);
						System.out.println("swarm");
				}
				else{
					br = new HunterPlayer(rc);
					System.out.println("Hunter");
				}
			}
			break;
		case ARTILLERY:
			br = new ArtilleryPlayer(rc);
			break;
		default:
			br = new EncampmentPlayer(rc);
			break;
		}
		
		br.loop();
	}
	

public static RobotInfo nearestEnemy(RobotController rc, int distThreshold) throws GameActionException {
		// functinos written here will be available to each player file
		return null;
	}
}	

