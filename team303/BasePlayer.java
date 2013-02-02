package team303;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.Direction;
import java.util.HashSet;
import java.util.Arrays;
import battlecode.common.Robot;

public abstract class BasePlayer {
	RobotController rc;
	int width;
	public static MapLocation[] encampments;
	public static HashSet<MapLocation> encampmentsSet;
	public static MapLocation enemyHQ;
	public HashSet<MapLocation> alliedEncampments;
	public Robot[] allies;

	public BasePlayer(RobotController rc) {
		this.rc = rc;
		width = rc.getMapWidth();
		encampments = rc.senseAllEncampmentSquares();
		enemyHQ = rc.senseEnemyHQLocation();
	}

	public Direction goTo(MapLocation destination){
		int dist = rc.getLocation().distanceSquaredTo(destination);
		if (dist>0&&rc.isActive()){
			Direction dir = rc.getLocation().directionTo(destination);
			int[] directionOffsets = {0,1,-1,2,-2};
			Direction lookingAtCurrently = null;
			lookAround: for (int d:directionOffsets){
				lookingAtCurrently = Direction.values()[(dir.ordinal()+d+8)%8];
				if(rc.canMove(lookingAtCurrently)){
					return lookingAtCurrently;
				}
			}
		}
		return null;
	}
	
	public static int MapLocationToInt(MapLocation loc){
		/** Convert MapLocation to Int.
		 * 
		 */

		return loc.x*1000+loc.y;
	}

	public static MapLocation IntToMaplocation(int mint){
		/** Convert Int to MapLocation.
		 * 
		 */

		int y = mint%1000;
		int x = (mint-y)/1000;

		if(x==0 && y==0){
			return null;
		}
		else{
			return new MapLocation(x,y);
		}
	}

	public abstract void run() throws GameActionException;

	public void loop() {
		while(true) {
			try {
				// Execute turn
				run();	
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
			// End turn
			rc.yield();
		}
	}
}
