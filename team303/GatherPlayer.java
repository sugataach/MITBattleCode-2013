package team303;

import java.util.HashSet;

import java.util.Set;
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
import java.util.Arrays;
import battlecode.common.Upgrade;

public class GatherPlayer extends BasePlayer {

	public static int turn;
	public static int encampmentTurn;

	public GatherPlayer(RobotController rc) throws GameActionException{
		super(rc);
		turn = 0;
		rc.broadcast(9999, rc.readBroadcast(9999) + 1);
	}

	public void filter(Robot[] allies) throws GameActionException{
		int i = 0;
		while (i < allies.length){
			Robot teamMate = allies[i];
			if(rc.canSenseObject(teamMate)){
				RobotInfo teamMateInfo = rc.senseRobotInfo(teamMate);
				MapLocation loc = teamMateInfo.location;
				if (encampmentsSet.contains(loc)){
					encampmentsSet.remove(loc);
				}
			}
			i++;
		}
	}

	public MapLocation findClosest(Robot[] robots) throws GameActionException {
		/** The method for finding the closest robot.
		 * 
		 * Input: 
		 *    robots - List of robots.
		 * Output: 
		 *    closest - MapLocation of the closest robot.
		 */

		int closestDist = rc.getLocation().distanceSquaredTo(enemyHQ);
		MapLocation closest = enemyHQ;

		for (int i=0;i<robots.length;i++){
			Robot arobot = robots[i];
			if (rc.canSenseObject(arobot)){
				RobotInfo arobotInfo = rc.senseRobotInfo(arobot);
				int dist = rc.getLocation().distanceSquaredTo(arobotInfo.location);
				if (dist<closestDist){
					closestDist = dist;
					closest = arobotInfo.location;
				}
			}
		}
		return closest;
	}

	public void run() throws GameActionException{
		if(rc.isActive()){
		if (rc.isActive()){

			int blitzChannel = 15;

			if(rc.readBroadcast(blitzChannel)==1){
				BasePlayer br = new AngryPlayer(rc);
				System.out.println("Angry");
				br.loop();
			}

			if (rc.senseNearbyGameObjects(Robot.class,100,rc.getTeam().opponent()).length>0){
				rc.broadcast(9999, rc.readBroadcast(9999) - 1);
				BasePlayer br;
				br = new HunterPlayer(rc);
				System.out.println("transform");
				br.loop();
			}

			int i = 0;
			int distanceHQ = rc.getLocation().distanceSquaredTo(enemyHQ);
			Direction dir;
			int shortest = distanceHQ;
			MapLocation next = enemyHQ;
			int i_min = -1;
			encampmentsSet = new HashSet<MapLocation>(Arrays.asList(rc.senseAllEncampmentSquares()));
			allies = rc.senseNearbyGameObjects(Robot.class,10000000,rc.getTeam());
			alliedEncampments = new HashSet<MapLocation>(Arrays.asList(rc.senseAlliedEncampmentSquares()));
			filter(allies);
			Robot[] nearbyAllies = rc.senseNearbyGameObjects(Robot.class,14,rc.getTeam()); // Get a list of all your nearby allies
			Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class,14,rc.getTeam().opponent()); // Get a list of all your nearby enemies
			if (nearbyEnemies.length > 1){ 
				if (nearbyAllies.length<nearbyEnemies.length){ 
					if(nearbyAllies.length>0){
						next = findClosest(nearbyAllies);
						turn++;
					}
				}
				else if (nearbyAllies.length >nearbyEnemies.length){  
					next = findClosest(nearbyEnemies);
					if(nearbyAllies.length>0){
						MapLocation closestAlly = findClosest(nearbyAllies);
						if (rc.getLocation().distanceSquaredTo(closestAlly) < rc.getLocation().distanceSquaredTo(next)){
							next = closestAlly;
						}
					}
					turn++;
				}
			}
			if (next == null){
				next = enemyHQ;
				turn -= 1;
			}
			if (next == enemyHQ){
				while (i < encampments.length){
					if (rc.getLocation().distanceSquaredTo(encampments[i]) < shortest & (!(alliedEncampments.contains(encampments[i]))) & (encampmentsSet.contains(encampments[i]))){
						shortest = rc.getLocation().distanceSquaredTo(encampments[i]);
						next = encampments[i];
						i_min = i;
					}
					i++;
				}
			}
			dir = goTo(next);

			//   int retreatChannel = 14;
			//
			//   if(rc.readBroadcast(retreatChannel)==1){
			//    dir = goTo(rc.senseHQLocation());
			//   }

			if (rc.isActive()){
				
				if (rc.senseEncampmentSquare(rc.getLocation())){
					
					
					
					if (rc.senseCaptureCost() < rc.getTeamPower()){
						
						if (rc.getLocation().distanceSquaredTo(enemyHQ) < 500){
							rc.captureEncampment(RobotType.valueOf("ARTILLERY"));
						}
						
						rc.broadcast(9999, rc.readBroadcast(9999) - 1);
						if (rc.getTeamPower() + 40 < allies.length*GameConstants.CAPTURE_ROUND_DELAY) {
							rc.captureEncampment(RobotType.valueOf("GENERATOR"));
						}

						else{
							rc.captureEncampment(RobotType.valueOf("SUPPLIER"));
						}
						encampmentTurn++;
					}
				}
				else if (dir !=null){
					if ((rc.senseMine(rc.getLocation().add(dir)) != null) & (rc.getTeam() != rc.senseMine(rc.getLocation().add(dir)))){
						rc.defuseMine(rc.getLocation().add(dir));
					}
					else if ((rc.canMove(dir))){
						rc.move(dir);
					}
					else{
						rc.layMine();
					}
				}
				else{
					rc.layMine();
				}
				turn++;
			}

		}
	}
	}
}