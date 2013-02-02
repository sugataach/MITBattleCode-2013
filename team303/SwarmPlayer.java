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

public class SwarmPlayer extends BasePlayer {

	public int num;
	public static MapLocation rallyPt;
	public static int status;

	public SwarmPlayer(RobotController rc) throws GameActionException{
		super(rc);
		//rc.broadcast(9998, rc.readBroadcast(9998) + 1);
		rc.broadcast(9991, rc.readBroadcast(9991) + 1);
		rallyPt = rc.getLocation();
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
	private boolean goodPlace(MapLocation location) {
		/** A simplistic boolean which decides whether the current location is a good one. I think if you're on an even tile then it's considered "good".
		 *  Probably need to UPDATE/CHANGE to make a more defined idea of "good".
		 *  Tries to create a checkerboard of mines....too intensive
		 *  CHANGE!!!!
		 */

		//		return ((3*location.x+location.y)%8==0);//pickaxe with gaps
		//		return ((2*location.x+location.y)%5==0);//pickaxe without gaps
		return ((location.x+location.y)%2==0 & ((rc.senseHQLocation().distanceSquaredTo(enemyHQ) >  rc.getLocation().distanceSquaredTo(enemyHQ))));//checkerboard
	}

	private void freeGo(MapLocation target, Robot[] allies,Robot[] enemies,Robot[] nearbyEnemies) throws GameActionException {
		/** The BIG ONE, this is the main method that controls the movement of the robots. I think the movement is based on a set of target weights, which
		 *  are suspiciously similar to neural network decision making. Try to IMPROVE using a learning algorithm of that nature.
		 *  
		 *  Inputs:
		 * 		target - a location of the target
		 * 		allies - a list of allies
		 * 		enemies - a list of enemies
		 * 		nearbyEnemies - a list of nearby enemies  
		 * 
		 */

		// Robot will be attracted to goal, repulsed from other things. I think this means that
		if (rc.isActive()){

			MapLocation myLoc = rc.getLocation();
			Direction toTarget = myLoc.directionTo(target);
			int targetWeighting = targetWeight(myLoc.distanceSquaredTo(target));
			MapLocation goalLoc = myLoc.add(toTarget,targetWeighting);//toward target, TODO weighted by the distance?

			// If there aren't any enemies
			if (enemies.length==0){
				// Add encampments player here!

				// Get closest encampment
				int shortestDist = 0;
				MapLocation closestEncmp = enemyHQ;
				for(int x = 0;x<encampments.length;x++){
					if(myLoc.distanceSquaredTo(encampments[x])<shortestDist){
						shortestDist = myLoc.distanceSquaredTo(encampments[x]);
						closestEncmp = encampments[x];
					}
				}

				// Go to closest encampment
				Direction goEncmp = goTo(closestEncmp);
				goalLoc = goalLoc.add(myLoc.directionTo(closestEncmp));

				// and there are allies: Move away from them
				if(allies.length>0){
					MapLocation closestAlly = findClosest(allies);
					goalLoc = goalLoc.add(myLoc.directionTo(closestAlly),-3);
				}
			}
			// If there is a substantial number of enemies (in this case if there is a minimum of 3 more enemies than allies)
			else if (allies.length<nearbyEnemies.length+3){

				// If there are allies: Move toward them.
				if(allies.length>0){
					MapLocation closestAlly = findClosest(allies);
					goalLoc = goalLoc.add(myLoc.directionTo(closestAlly),5);
				}

				// If there are enemies: Move away from them.
				if(nearbyEnemies.length>0){
					MapLocation closestEnemy = findClosest(nearbyEnemies);
					goalLoc = goalLoc.add(myLoc.directionTo(closestEnemy),-10);
				}
			}
			// If you have more allies than your nearby enemies
			else if (allies.length>=nearbyEnemies.length+3){

				// If you have allies, move towards your closest ally.
				if(allies.length>0){
					MapLocation closestAlly = findClosest(allies);
					goalLoc = goalLoc.add(myLoc.directionTo(closestAlly),5);
				}
				// If there are nearby enemies, move towards them.
				if(nearbyEnemies.length>0){
					MapLocation closestEnemy = findClosest(nearbyEnemies);
					goalLoc = goalLoc.add(myLoc.directionTo(closestEnemy),10);
				}
				// Move towards the farthest enemy.
				else{
					MapLocation closestEnemy = findClosest(enemies);
					goalLoc = goalLoc.add(myLoc.directionTo(closestEnemy),10);
				}
			}

			// TODO: Add functionality to move away from allied mines

			Direction finalDir = myLoc.directionTo(goalLoc);
			
//			int retreatChannel = 14;
//			
//			if(rc.readBroadcast(retreatChannel)==1){
//				finalDir = goTo(rc.senseHQLocation());
//			}

			int blitzChannel = 15;

			if(rc.readBroadcast(blitzChannel)==1){
				finalDir = goTo(enemyHQ);
			}

			if (Math.random()<.1)
				finalDir = finalDir.rotateRight();
			simpleMove(finalDir, myLoc, true);
		}
	}
	private int targetWeight(int dSquared){
		/** The importance (i.e. weight) of the target is determined by its distance. 
		 *  Basically, the farther it is away, the "higher" the importance, but the importance scales down exponentially.
		 *  So, far targets may be treated as important but close targets are treated with "as much" importance? Weird wording...but I get the idea.
		 *  Maybe not the best way to determine the importance. Have another go at it!
		 */

		if (dSquared>100){
			return 5;
		}else if (dSquared>9){
			return 2;
		}else{
			return 1;
		}
	}

	private void simpleMove(Direction dir, MapLocation myLoc, boolean defuseMines) throws GameActionException {
		/** Alternate pathing algorithm. Very confusing, really need to sit down and understand the else block.\
		 *  REVIEW!
		 */
		if (rc.isActive()){
			int[] directionOffsets = {0,1,-1,2,-2};
			Direction lookingAtCurrently = null;
			lookAround: for (int d:directionOffsets){
				lookingAtCurrently = Direction.values()[(dir.ordinal()+d+8)%8];
				Team currentMine = rc.senseMine(myLoc.add(lookingAtCurrently));
				if(rc.canMove(lookingAtCurrently)&&(defuseMines||(!defuseMines&&(currentMine==rc.getTeam()||currentMine==null)))){
					moveOrDefuse(lookingAtCurrently);
					break lookAround;
				}
			}
		}
	}

	private void moveOrDefuse(Direction dir) throws GameActionException{
		/** Decision model of whether to move or defuse when encountering a mine
		 * 
		 */

		MapLocation ahead = rc.getLocation().add(dir);
		Team mineAhead = rc.senseMine(ahead);

		// If there is a mine ahead, and it's made by the enemy team, defuse it, or else keep moving
		if(mineAhead!=null&&mineAhead!= rc.getTeam()){
			rc.defuseMine(ahead);
		}
		else{
			rc.move(dir);			
		}
	}

	public void run() throws GameActionException{
		if (rc.isActive()){

			int i = 0;
			int distanceHQ = rc.getLocation().distanceSquaredTo(enemyHQ);

			MapLocation received = IntToMaplocation(rc.readBroadcast(9995));
			if (received!= null)
				rallyPt = received;

			int ir = rc.readBroadcast(9994);
			if (ir!=0&&ir<=2)
				status = ir;

			Direction dir;
			int shortest = distanceHQ;
			MapLocation next = enemyHQ;
			int i_min = -1;
			Robot[] allies = rc.senseNearbyGameObjects(Robot.class,14,rc.getTeam());
			Robot[] enemies = rc.senseNearbyGameObjects(Robot.class,10000000,rc.getTeam().opponent());
			Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class,14,rc.getTeam().opponent());
			Robot[] nearbyAllies = rc.senseNearbyGameObjects(Robot.class,30,rc.getTeam());
			if (rc.isActive()){
				if (nearbyAllies.length > 15 & rc.getLocation().distanceSquaredTo(rc.senseHQLocation()) < 200){
					BasePlayer br;
					br = new HunterPlayer(rc);
					rc.broadcast(9991, rc.readBroadcast(9991) - 1);
					System.out.println("yay");
					br.loop();
				}
				else if (rc.getLocation().distanceSquaredTo(rc.senseHQLocation()) > 100){
					BasePlayer br;
					br = new HunterPlayer(rc);
					rc.broadcast(9991, rc.readBroadcast(9991) - 1);
					System.out.println("yay");
					br.loop();
				}
				if (status == 1){//don't lay mines
					BasePlayer br;
					br = new HunterPlayer(rc);
					rc.broadcast(9991, rc.readBroadcast(9991) - 1);
					System.out.println("yay");
					br.loop();
					//move toward received goal, using swarm behavior
					//freeGo(rallyPt,allies,enemies,nearbyEnemies);
				}else if (status == 2){//lay mines!
					if (goodPlace(rc.getLocation())&&rc.senseMine(rc.getLocation())==null){
						rc.layMine();
					}else{
						freeGo(rallyPt,allies,enemies,nearbyEnemies);
					}
				}
			}
		}
	}	
}
