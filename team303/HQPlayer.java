package team303;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.Team;
import battlecode.common.Upgrade;
import java.util.HashSet;
import java.util.Arrays;
import battlecode.common.GameConstants;
import battlecode.common.Direction;

public class HQPlayer extends BasePlayer {
	//declare local variables
	int i;
	public static int condition;
	public static MapLocation rallyPt;
	int nowheretogo;
	public HQPlayer(RobotController rc) throws GameActionException{
		//code to execute one time
		super(rc);
		i = -1;
		condition = 0;
		rc.broadcast(9989, 10);
		//rallyPt = rc.getLocation().add(rc.getLocation().directionTo(enemyHQ),5);
		rallyPt = rc.getLocation();
		nowheretogo = 1;
	}

	private boolean among(MapLocation[] alliedEncampments, MapLocation rallyPt) {
		/**Check if the rally point is among the allied encampements
		 * 
		 */

		// For the encampments in the allied 
		for(MapLocation enc:alliedEncampments){
			if(enc.equals(rallyPt))
				return true;
		}
		return false;
	}

	public MapLocation captureEncampments(MapLocation[] alliedEncampments) throws GameActionException{
		/** Make a decision on which encampments to capture
		 * 
		 */


		// Locate uncaptured encampments within a certain radius
		MapLocation[] neutralEncampments = new MapLocation[encampments.length];
		int neInd = 0;

		// Compute nearest encampment (counting the enemy HQ)
		outer: for(MapLocation enc: encampments) {
			for(MapLocation aenc: alliedEncampments) 
				if(aenc.equals(enc))
					continue outer;
			if(rc.getLocation().distanceSquaredTo(enc)<= Math.pow(Clock.getRoundNum()/10, 2)){
				// Add to neutral encampments list
				neutralEncampments[neInd]=enc;
				neInd=neInd+1;
			}
		}
		rc.setIndicatorString(2, "neutral enc det "+neInd+" round "+Clock.getRoundNum());

		if (neInd>0){
			// Proceed to an encampment and capture it
			int which = (int) ((Math.random()*100)%neInd);
			MapLocation campLoc = neutralEncampments[which];
			return campLoc;
		}
		// No encampments to capture; change state
		else{
			return null;
		}
	}
	public MapLocation newRally(MapLocation oldRally) throws GameActionException{
		/** Make a decision on which encampments to rally to
		 * 
		 */

		int closest = rc.getLocation().distanceSquaredTo(enemyHQ);
		MapLocation next = enemyHQ;
		// Compute nearest encampment (counting the enemy HQ)
		for(MapLocation enc: alliedEncampments) {
			if (enc != oldRally & rc.getLocation().distanceSquaredTo(enc) < closest & (rc.senseHQLocation().distanceSquaredTo(enemyHQ) > enc.distanceSquaredTo(enemyHQ))){
				closest = rc.getLocation().distanceSquaredTo(enc);
				next = enc;
			}
		}
		return next;
	}

	public void run() throws GameActionException {
		//code to execute for the whole match


		if (condition == 1){//just spawned
			if (rc.readBroadcast(9989) != rc.roundsUntilActive() + 1){
				System.out.println("just spawned");
				System.out.println(rc.roundsUntilActive() + 1);
				rc.broadcast(9989, rc.roundsUntilActive() + 1);
			}
			condition = 2;
		}
		if(rc.senseEnemyNukeHalfDone()){
			rc.broadcast(15, 1);
		}

		if(rc.senseNearbyGameObjects(Robot.class, 14, rc.getTeam().opponent()).length > 1){
			rc.broadcast(14, 1);
		}



		if (rc.isActive()){

			/*
			if (alliedEncampments.size()>0 && alliedEncampments.contains(rallyPt)){
				MapLocation closestEncampment = captureEncampments(rc.senseAlliedEncampmentSquares());
				if (closestEncampment != null){
					rallyPt = closestEncampment;
				}
			}
			 */
			allies = rc.senseNearbyGameObjects(Robot.class,10000000,rc.getTeam());
			alliedEncampments = new HashSet<MapLocation>(Arrays.asList(rc.senseAlliedEncampmentSquares()));

			if (rc.senseMineLocations(rallyPt, 100000, rc.getTeam()).length > 500){
				rallyPt = rallyPt.add(rc.getLocation().directionTo(enemyHQ));
			}
			if(Clock.getRoundNum()>1000||rc.senseEnemyNukeHalfDone()){
				rallyPt = enemyHQ;
			}

			int msg = MapLocationToInt(rallyPt);
			if (msg != rc.readBroadcast(9995)){
				rc.broadcast(9995, msg);
			}

			if (rc.senseNearbyGameObjects(Robot.class,100000,rc.getTeam().opponent()).length<3){
				if (rc.readBroadcast(9994) != 2){
					rc.broadcast(9994, 2);
				}
			}
			else{
				if (rc.readBroadcast(9994) != 1){
					rc.broadcast(9994, 1);
				}
			}
		}




		if (rc.isActive()){

			i++;
			Direction dir = goTo(enemyHQ);
			String[] directions = {"EAST", "NORTH", "NORTH_EAST", "NORTH_WEST", "SOUTH", "SOUTH_EAST", "WEST"};
			int index = 0;
			if (dir != null & (rc.senseMine(rc.getLocation().add(dir)) != null) & (rc.getTeam() != rc.senseMine(rc.getLocation().add(dir)))){
				while (index < directions.length & ((rc.senseMine(rc.getLocation().add(dir)) != null) & (rc.getTeam() != rc.senseMine(rc.getLocation().add(dir))))){
					if (rc.canMove(Direction.valueOf(directions[index]))){
						dir = Direction.valueOf(directions[index]);
					}
					index++;
				} 
			}

			if ((rc.senseMine(rc.getLocation().add(dir)) != null) & (rc.getTeam() != rc.senseMine(rc.getLocation().add(dir)))){
				nowheretogo = 2;
				if (!rc.hasUpgrade(Upgrade.valueOf("FUSION"))){
					condition = 2;
					rc.researchUpgrade(Upgrade.valueOf("FUSION"));
				}
				else if (!rc.hasUpgrade(Upgrade.valueOf("DEFUSION"))){
					condition = 2;
					rc.researchUpgrade(Upgrade.valueOf("DEFUSION"));
				}
				//else if (!rc.hasUpgrade(Upgrade.valueOf("PICKAXE"))){
				//rc.researchUpgrade(Upgrade.valueOf("PICKAXE"));
				//}
				else{
					condition = 2;
					rc.researchUpgrade(Upgrade.valueOf("NUKE"));
				}
			}
			else{
				nowheretogo = 1;
			}

			if (rc.isActive() & nowheretogo == 1){
				if (rc.getTeamPower() + 40 > allies.length*GameConstants.HQ_SPAWN_DELAY & (dir != null)){
					if (rc.readBroadcast(9997) < 1){
						System.out.println("hi");
						condition = 1;
						rc.spawn(dir);
					}
					else{
						if (!rc.hasUpgrade(Upgrade.valueOf("DEFUSION"))){
							condition = 2;
							rc.researchUpgrade(Upgrade.valueOf("DEFUSION"));
						}
						//else if (!rc.hasUpgrade(Upgrade.valueOf("PICKAXE"))){
						//rc.researchUpgrade(Upgrade.valueOf("PICKAXE"));
						//}
						else{
							System.out.println("bye");
							condition = 1;
							rc.spawn(dir);
						}
					}
				}
			
			else if (!rc.hasUpgrade(Upgrade.valueOf("FUSION"))){
				condition = 2;
				rc.researchUpgrade(Upgrade.valueOf("FUSION"));
			}
			else if (!rc.hasUpgrade(Upgrade.valueOf("DEFUSION"))){
				condition = 2;
				rc.researchUpgrade(Upgrade.valueOf("DEFUSION"));
			}
			//else if (!rc.hasUpgrade(Upgrade.valueOf("PICKAXE"))){
			//rc.researchUpgrade(Upgrade.valueOf("PICKAXE"));
			//}
			else{
				condition = 2;
				rc.researchUpgrade(Upgrade.valueOf("NUKE"));
			}
		}
	}
}
}



