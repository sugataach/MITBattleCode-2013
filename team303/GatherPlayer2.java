package team303;

import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotType;

import java.util.Arrays;
import java.util.HashSet;

import battlecode.common.*;
public class GatherPlayer2 extends BasePlayer {

	public static int turn;
	public static int encampmentTurn;
	public int num;


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

		if (rc.isActive()){

			int blitzChannel = 15;

			if(rc.readBroadcast(blitzChannel)==1){
				BasePlayer br = new AngryPlayer(rc);
				System.out.println("Angry");
				br.loop();
			}
			/*
			if (rc.senseNearbyGameObjects(Robot.class,50,rc.getTeam().opponent()).length>1){
				rc.broadcast(9978, rc.readBroadcast(9978) - 1);
				BasePlayer br;
				br = new HunterPlayer(rc);
				System.out.println("transform");
				br.loop();
			}
			 */
			int i = 0;
			int distanceHQ = rc.getLocation().distanceSquaredTo(enemyHQ);
			int distanceOurHQ = rc.getLocation().distanceSquaredTo(rc.senseHQLocation());
			Direction dir;
			int shortest = distanceHQ;
			MapLocation next = enemyHQ;
			int i_min = -1;
			Robot[] nearbyAllies = rc.senseNearbyGameObjects(Robot.class,30,rc.getTeam()); // Get a list of all your nearby allies
			Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class,100,rc.getTeam().opponent()); // Get a list of all your nearby enemies
			Robot[] closeEnemies = rc.senseNearbyGameObjects(Robot.class,4,rc.getTeam().opponent());	



			if (nearbyEnemies.length < 1){


				MapLocation[] closestNeturalEncampments = rc.senseEncampmentSquares(rc.getLocation(), 30, Team.valueOf("NEUTRAL"));
				//MapLocation[] closestEnemyEncampments = rc.senseEncampmentSquares(rc.getLocation(), 1000000, rc.getTeam().opponent());
				// If there's an encampment within a 100 units, execute:
				if(closestNeturalEncampments.length > 1){

					//System.out.println("What now?");

					// Find the closest non-allied Encampment Square
					alliedEncampments = new HashSet<MapLocation>(Arrays.asList(rc.senseAlliedEncampmentSquares()));
					MapLocation closestEncampment = closestNeturalEncampments[0];
					int closestEncampmentDist = rc.getLocation().distanceSquaredTo(closestNeturalEncampments[0]);

					for(int near = 1; near<closestNeturalEncampments.length;near++){
						if(!alliedEncampments.contains(closestNeturalEncampments[near])){
							if(closestEncampmentDist>rc.getLocation().distanceSquaredTo(closestNeturalEncampments[near])){
								closestEncampmentDist = rc.getLocation().distanceSquaredTo(closestNeturalEncampments[near]);
								closestEncampment = closestNeturalEncampments[near];
							}
						}
					}

					// Now that you have the closest non-allied Encampment Square, move towards it
					dir = rc.getLocation().directionTo(closestEncampment);
					int status = 0;
					// Defuse mines while moving toward objective

					if (rc.senseEncampmentSquare(rc.getLocation())) {
						rc.broadcast(9978, rc.readBroadcast(9978) - 1);
						if (rc.senseCaptureCost() < rc.getTeamPower()){
							if(rc.getLocation().distanceSquaredTo(enemyHQ)>500){
								rc.captureEncampment(RobotType.valueOf("MEDBAY"));
								rc.captureEncampment(RobotType.valueOf("MEDBAY"));
								MapLocation MedBayLoc = rc.getLocation();
								int medLoc = MapLocationToInt(MedBayLoc);
								rc.broadcast(24, medLoc);
							}
							else{
								rc.captureEncampment(RobotType.valueOf("ARTILLERY"));
							}
						}
					}
					else if (dir !=null){
						if ((rc.senseMine(rc.getLocation().add(dir)) != null) & (rc.getTeam() != rc.senseMine(rc.getLocation().add(dir)))){
							rc.defuseMine(rc.getLocation().add(dir));
							//System.out.println("Working YAY!");
						}
						else if ((rc.canMove(dir))){
							rc.move(dir);
						}
					}
				}
				else{

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

					dir = goTo(next);

					//				int retreatChannel = 14;
					//
					//				if(rc.readBroadcast(retreatChannel)==1){
					//					dir = goTo(rc.senseHQLocation());
					//				}

					if (rc.isActive()){
						
						if (dir != null & rc.senseMine(rc.getLocation()) != rc.getTeam().opponent()){
							if ((rc.senseMine(rc.getLocation().add(dir)) != null) & (rc.getTeam() != rc.senseMine(rc.getLocation().add(dir)))){
								if (closeEnemies.length > 0){
								}
								else{
									rc.defuseMine(rc.getLocation().add(dir));
								}
							}
							else if ((rc.canMove(dir))){
								rc.move(dir);
							}
						}
						else if (rc.senseMine(rc.getLocation()) == rc.getTeam().opponent()){
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
	}

	public GatherPlayer2(RobotController rc) throws GameActionException{
		super(rc);
		rc.broadcast(9978, rc.readBroadcast(9978) + 1);
		// TODO Auto-generated constructor stub
	}
}