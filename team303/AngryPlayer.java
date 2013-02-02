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
import java.util.Random;

public class AngryPlayer extends BasePlayer {

	public static int turn;
	public static int encampmentTurn;
	public int num;

	public AngryPlayer(RobotController rc) throws GameActionException{
		super(rc);
		turn = 0;
	}

	public MapLocation findClosest(Robot[] robots) throws GameActionException {
		/** The method for finding the closest robot.
		 * 
		 * Input: 
		 * 			robots - List of robots.
		 * Output: 
		 * 			closest - MapLocation of the closest robot.
		 */

		int closestDist = rc.getLocation().distanceSquaredTo(enemyHQ);
		MapLocation closest = enemyHQ;

		for (int i=0;i<robots.length;i++){
			Robot arobot = robots[i];
			if (rc.canSenseObject(arobot)){
				RobotInfo arobotInfo = rc.senseRobotInfo(arobot);
				int dist = rc.getLocation().distanceSquaredTo(arobotInfo.location);
				if (dist<closestDist & dist > 5){
					closestDist = dist;
					closest = arobotInfo.location;
				}
			}
		}
		return closest;
	}

	public void run() throws GameActionException{

		Direction dir;
		MapLocation next = enemyHQ;
		/*
		int i_min = -1;
		int i = 0;
		int distanceHQ = rc.getLocation().distanceSquaredTo(enemyHQ);
		int shortest = distanceHQ;
		Robot[] nearbyAllies = rc.senseNearbyGameObjects(Robot.class,30,rc.getTeam()); // Get a list of all your nearby allies
		Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class,100,rc.getTeam().opponent()); // Get a list of all your nearby enemies
		Robot[] closeEnemies = rc.senseNearbyGameObjects(Robot.class,4,rc.getTeam().opponent());	
			if (nearbyAllies.length<nearbyEnemies.length + 5 & nearbyEnemies.length > 0){
				allies = rc.senseNearbyGameObjects(Robot.class,10000000,rc.getTeam());
				if (allies.length < 3){
					next = rc.senseHQLocation();
					turn ++;
				}
				else if(nearbyAllies.length>0){
					next = findClosest(nearbyAllies);
					turn++;
				}

				else{
					next = findClosest(allies);
					turn++;
				}
			}
			else if (nearbyEnemies.length < 1){
				if(nearbyAllies.length<10 & nearbyAllies.length>3){
					next = findClosest(nearbyAllies);
					turn++;
				}
				else if (nearbyAllies.length>10){
				}
				else{
					next = rc.senseHQLocation();
					turn++;
				}
			}
			else if (nearbyAllies.length>nearbyEnemies.length){		
				next = findClosest(nearbyEnemies);
				turn++;
			}
		if (next == null){
			MapLocation HQ = rc.senseHQLocation();
			MapLocation enemyLoc = rc.senseEnemyHQLocation();
			MapLocation rallyPt = HQ.add(HQ.directionTo(enemyLoc),5);
			next = rallyPt;
			turn -= 1;
		}
		 */
		dir = goTo(next);	
		if (rc.isActive()){
			if (rc.isActive()){
				if (dir != null & rc.senseMine(rc.getLocation()) != rc.getTeam().opponent()){
					if ((rc.senseMine(rc.getLocation().add(dir)) != null) & (rc.getTeam() != rc.senseMine(rc.getLocation().add(dir)))){
						rc.defuseMine(rc.getLocation().add(dir));	
					}
					else if ((rc.canMove(dir))){
						rc.move(dir);
					}
				}
				else if (rc.senseMine(rc.getLocation()) == rc.getTeam().opponent()){
					System.out.println("hello world");
					rc.broadcast(9997, rc.readBroadcast(9997) + 1);
					next = rc.senseHQLocation();
					dir = goTo(next);
					if (dir != null & rc.senseMine(rc.getLocation().add(dir)) != rc.getTeam().opponent()){
						rc.move(dir);
					}
					else{
						rc.defuseMine(rc.getLocation());
					}
				}
				turn++;
			}
		}
	}
}

