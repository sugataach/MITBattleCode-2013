package team303;

import java.util.Arrays;
import java.util.HashSet;

import battlecode.common.*;
public class GatherPlayer3 extends BasePlayer {

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

			if (rc.senseNearbyGameObjects(Robot.class,50,rc.getTeam().opponent()).length>1){
				rc.broadcast(9977, rc.readBroadcast(9977) - 1);
				BasePlayer br;
				br = new HunterPlayer(rc);
				System.out.println("transform");
				br.loop();
			}

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

			MapLocation[] closestNeturalEncampments = rc.senseEncampmentSquares(rc.getLocation(), 1000000, Team.valueOf("NEUTRAL"));
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
				allies = rc.senseNearbyGameObjects(Robot.class,10000000,rc.getTeam());
				alliedEncampments = new HashSet<MapLocation>(Arrays.asList(rc.senseAlliedEncampmentSquares()));
				if (rc.senseEncampmentSquare(rc.getLocation())) {
					rc.broadcast(9977, rc.readBroadcast(9977) - 1);
					if (rc.getTeamPower() + alliedEncampments.size()*5 + 40*GameConstants.CAPTURE_ROUND_DELAY< 2*allies.length*GameConstants.CAPTURE_ROUND_DELAY) {
						rc.captureEncampment(RobotType.valueOf("GENERATOR"));
					}
					//else if (rc.readBroadcast(9998) < 1 & rc.senseHQLocation().distanceSquaredTo(enemyHQ) >  rc.getLocation().distanceSquaredTo(enemyHQ)){
					//rc.broadcast(9998, rc.readBroadcast(9998) + 1);
					//rc.captureEncampment(RobotType.valueOf("ARTILLERY"));
					//}
					else if (rc.readBroadcast(9989) == 10 & encampments.length - alliedEncampments.size() > 0){
						rc.captureEncampment(RobotType.valueOf("SUPPLIER"));
					}
					else if (rc.readBroadcast(9989) == 9 & encampments.length - alliedEncampments.size() > 0){
						rc.captureEncampment(RobotType.valueOf("SUPPLIER"));
					}
					else if (rc.readBroadcast(9989) == 8 & encampments.length - alliedEncampments.size() > 1){
						rc.captureEncampment(RobotType.valueOf("SUPPLIER"));
					}
					else if (rc.readBroadcast(9989) == 7 & encampments.length - alliedEncampments.size() > 1){
						rc.captureEncampment(RobotType.valueOf("SUPPLIER"));
					}
					else if (rc.readBroadcast(9989) == 6 & encampments.length - alliedEncampments.size() > 2){
						rc.captureEncampment(RobotType.valueOf("SUPPLIER"));
					}
					else if (rc.readBroadcast(9989) == 5 & encampments.length - alliedEncampments.size() > 3){
						rc.captureEncampment(RobotType.valueOf("SUPPLIER"));
					}
					else if (rc.readBroadcast(9989) == 4 & encampments.length - alliedEncampments.size() > 5){
						rc.captureEncampment(RobotType.valueOf("SUPPLIER"));
					}
					else if (rc.readBroadcast(9989) == 3 & encampments.length - alliedEncampments.size() > 10){
						rc.captureEncampment(RobotType.valueOf("SUPPLIER"));
					}
					else{
						System.out.println("boom");
						BasePlayer br;
						br = new HunterPlayer(rc);
						br.loop();
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
			/*
		else if(closestEnemyEncampments.length > 1){

			System.out.println("What now?");

			// Find the closest non-allied Encampment Square
			alliedEncampments = new HashSet<MapLocation>(Arrays.asList(rc.senseAlliedEncampmentSquares()));
			MapLocation closestEncampment = closestNeturalEncampments[0];
			int closestEncampmentDist = rc.getLocation().distanceSquaredTo(closestEnemyEncampments[0]);

			for(int near = 1; near<closestEnemyEncampments.length;near++){
				if(!alliedEncampments.contains(closestEnemyEncampments[near])){
					if(closestEncampmentDist>rc.getLocation().distanceSquaredTo(closestEnemyEncampments[near])){
						closestEncampmentDist = rc.getLocation().distanceSquaredTo(closestEnemyEncampments[near]);
						closestEncampment = closestEnemyEncampments[near];
					}
				}
			}

			// Now that you have the closest non-allied Encampment Square, move towards it
			dir = rc.getLocation().directionTo(closestEncampment);
			int status = 0;
			// Defuse mines while moving toward objective

			if (rc.senseEncampmentSquare(rc.getLocation())) {
				if (rc.senseCaptureCost() < rc.getTeamPower()){
					if(rc.getLocation().distanceSquaredTo(enemyHQ)>100){
						rc.captureEncampment(RobotType.valueOf("MEDBAY"));
					}
					else{
						rc.captureEncampment(RobotType.valueOf("ARTILLERY"));
					}
				}
			}
			else if (dir !=null){
				if ((rc.senseMine(rc.getLocation().add(dir)) != null) & (rc.getTeam() != rc.senseMine(rc.getLocation().add(dir)))){
					rc.defuseMine(rc.getLocation().add(dir));
					System.out.println("Working YAY!");
				}
				else if ((rc.canMove(dir))){
					rc.move(dir);
				}
			}
		}
			 */


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

	public GatherPlayer3(RobotController rc) throws GameActionException{
		super(rc);
		rc.broadcast(9977, rc.readBroadcast(9977) + 1);
		// TODO Auto-generated constructor stub
	}
}