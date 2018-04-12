package helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import bwapi.*;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;
import models.CustomBaseLocation;

public class UnitsManager{
	
    public static List<Unit> MilitaryUnits = new ArrayList<Unit>();
    
    public static void onUnitDestroy(Unit unit) {
    	if (unit.getType() == UnitType.Terran_Marine) {
    		MilitaryUnits.remove(unit);
    	}
    }
    
    public static void onUnitComplete(Unit unit) {
    	if (unit.getType() == UnitType.Terran_Marine) {
    		MilitaryUnits.add(unit);
    		//unit.attack(new Position(GetClosestEmptyBase(unit).getPosition().getX(), GetClosestEmptyBase(unit).getPosition().getY() + 20));
    	}
    }
    
    public static void attackUnits() {
    	List<Unit> closestEnemyUnits = StarCraftInstance.game.enemy().getUnits();
    	if (StarCraftInstance.self.supplyUsed() / 2 == 200) {
    		for (Unit myUnit : MilitaryUnits) {
    			if (myUnit.isIdle()) {
    				for (CustomBaseLocation cbl : BaseManager.baseLocations) {
            			// If this is a possible start location,
            			if (cbl.baseLocation.isStartLocation() && cbl.baseLocation.getTilePosition().getDistance(StarCraftInstance.self.getStartLocation()) > 0) {
            				// do something. For example send some unit to attack that position:
            				myUnit.attack(cbl.baseLocation.getPosition());
            			}
            		}	
    			}
    		}
    	}
    	if (closestEnemyUnits.size() > 0) {
			for (final Unit myUnit : MilitaryUnits) {
				if (myUnit.isIdle()) {
//					Collections.sort(closestEnemyUnits, new Comparator<Unit>() {
//			            @Override
//			            public int compare(Unit u1, Unit u2) {
//							return myUnit.getPosition().getDistance(u1.getPosition()) < myUnit.getPosition().getDistance(u2.getPosition())
//			                        ? -1 : 1;
//			            }
//			        });
					for (Unit enemyUnit : closestEnemyUnits) {
						if (enemyUnit.isVisible()) {
							myUnit.attack(enemyUnit.getPosition());	
						}
			    	}
				}
			}
		}
    	// else {
//			if (StarCraftInstance.self.allUnitCount(UnitType.Terran_Marine) < 50) {
////				for (Unit attackUnit : MilitaryUnits) {
////					Position rallyLocation = new Position(GetClosestEmptyBase().getPosition().getX(), GetClosestEmptyBase().getPosition().getY() + 20);
////					if (attackUnit.getPosition().getDistance(rallyLocation) > 10) {
////						attackUnit.attack(rallyLocation);
////					}
////				} 
//			} else {
//	    		for (Unit myUnit : MilitaryUnits) {
//	    			if (myUnit.isIdle()) {
//	    				for (BaseLocation b : StarCraftInstance.baseLocations) {
//	            			// If this is a possible start location,
//	            			if (b.isStartLocation() && b.getTilePosition().getDistance(StarCraftInstance.self.getStartLocation()) > 0) {
//	            				// do something. For example send some unit to attack that position:
//	            				myUnit.attack(b.getPosition());
//	            			}
//	            		}	
//	    			}
//				}
//			}
//	    	
//		}
    }
}