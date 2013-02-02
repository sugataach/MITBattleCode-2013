package team303;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import java.util.HashSet;
import battlecode.common.Robot;

public class ArtilleryPlayer extends BasePlayer {

	public ArtilleryPlayer(RobotController rc) throws GameActionException{
		super(rc);
	}

	public void run() throws GameActionException { 
		MapLocation enemyLoc;
		Robot[] nearbyEnemies=rc.senseNearbyGameObjects(Robot.class, 63, rc.getTeam().opponent());
		if (nearbyEnemies.length >= 1){
			for (int i=0; i<nearbyEnemies.length; i++){
				enemyLoc = rc.senseRobotInfo(nearbyEnemies[i]).location;
				if (rc.senseNearbyGameObjects(Robot.class, enemyLoc, 2, rc.getTeam()).length <1){
					if (rc.isActive()) {
						rc.attackSquare(enemyLoc);
					}
				}
			}
		}
	}
}