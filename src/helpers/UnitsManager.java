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
import enums.PlayStyles;
import models.CustomBaseLocation;
import models.MilitaryUnit;

public class UnitsManager{
	
	public static void Init() {
		MilitaryUnits = new ArrayList<MilitaryUnit>();
		Medics = new ArrayList<MilitaryUnit>();
		attackedBaseLocations = new ArrayList<CustomBaseLocation>();
	}
	
    public static List<MilitaryUnit> MilitaryUnits;
    public static List<MilitaryUnit> Medics;
    public static List<CustomBaseLocation> attackedBaseLocations;
    public static void onUnitDestroy(Unit unit) {
    	if (unit.getType() == UnitType.Terran_Marine || unit.getType() == UnitType.Terran_Medic) {
    		if (MilitaryUnits.size() > 0) {
    			for (MilitaryUnit mu : MilitaryUnits) {
    				if (mu.unit.getID() == unit.getID()) {
    					MilitaryUnits.remove(mu);
    					break;
    				}
    			}
    		}
    	}
    }
    
    public static void onUnitComplete(Unit unit) {
    	if (unit.getType() == UnitType.Terran_Marine || unit.getType() == UnitType.Terran_Medic) {
    		MilitaryUnits.add(new MilitaryUnit(unit));
    		//unit.attack(new Position(GetClosestEmptyBase(unit).getPosition().getX(), GetClosestEmptyBase(unit).getPosition().getY() + 20));
    	}
    }
    
    public static void KiteMeleeUnits(Unit myUnit, Unit enemyUnit) {
    	// myUnit.isAttacking() && (myUnit.isAttackFrame() || myUnit.isStartingAttack())
    	//  || myUnit.getDistance(enemyUnit) < myUnit.getType().groundWeapon().maxRange() * 0.66
    	// myUnit.getGroundWeaponCooldown() > 0
    	//if ((myUnit.isAttackFrame() || myUnit.isStartingAttack())) return;
    	if (myUnit.isMoving()) return;
    	int distanceFromEnemy = myUnit.getDistance(enemyUnit);
    	double requiredDistance = myUnit.getType().groundWeapon().maxRange() * 0.66;
    	if (myUnit.getGroundWeaponCooldown() > 0 && distanceFromEnemy < requiredDistance) {
    		int xDifferential = (int) (requiredDistance - (myUnit.getPosition().getX() - enemyUnit.getPosition().getX()));
    		int newX = xDifferential > 0 ? -(xDifferential) : xDifferential;
    		int yDifferential = (int) (requiredDistance - (myUnit.getPosition().getY() - enemyUnit.getPosition().getY()));
    		int newY = yDifferential > 0 ? -(yDifferential) : yDifferential;
    		myUnit.move(
				new Position(
					myUnit.getPosition().getX() + newX, 
					myUnit.getPosition().getY() + newY
				)
			);
    	}
    	else {
			if (myUnit.isIdle()) {
				myUnit.attack(enemyUnit.getPosition());	
			}
    	}
    }
    
    public static void AttackAndMoveFoward(Unit myUnit, Unit enemyUnit) {
    	if (myUnit.getGroundWeaponCooldown() > 0) {
    		myUnit.move(
				enemyUnit.getPosition()
			);
    	}
    	else {
    		MarineUseStim(myUnit, enemyUnit);
			if (myUnit.isIdle()) {
				myUnit.attack(enemyUnit.getPosition());	
			}
    	}
    }
    
    private static void MarineUseStim(Unit myUnit, Unit enemyUnit) {
    	if (
				myUnit.canUseTech(TechType.Stim_Packs) && 
				myUnit.getHitPoints() >= myUnit.getType().maxHitPoints() * 0.6 && 
				myUnit.isInWeaponRange(enemyUnit) && 
				!myUnit.isStimmed()
			) {
				myUnit.useTech(TechType.Stim_Packs);
			}
    }
    
    private static boolean CanUnitAttack(Unit enemyUnit) {
    	return enemyUnit.getType().canAttack() || 
    			enemyUnit.getType().isSpellcaster() || 
    			enemyUnit.getType() == UnitType.Terran_Dropship || 
    			enemyUnit.getType() == UnitType.Protoss_Shuttle;
    }
    
    private static Unit SelectEnemyUnit(Unit myUnit, List<Unit> enemyUnits, boolean allowUnitsThatCannotAttack) {
    	int distanceFromClosestEnemy = Integer.MAX_VALUE;
    	Unit selectedEnemyUnit = null;
    	for (Unit enemyUnit : enemyUnits) {
    		if (enemyUnit.isDetected() && enemyUnit.isCloaked()) {
    			// we just scanned it so focus it
    			return enemyUnit;
    		}
			if (!enemyUnit.isDetected() && myUnit.isInWeaponRange(enemyUnit)) {
				for (CustomBaseLocation cbl : BaseManager.baseLocations) {
					if (cbl.commandCenter != null) {
						if (cbl.commandCenter.unit.getAddon() != null && cbl.commandCenter.unit.getAddon().canUseTech(TechType.Scanner_Sweep, enemyUnit.getPosition())) {
							cbl.commandCenter.unit.getAddon().useTech(TechType.Scanner_Sweep, enemyUnit.getPosition());
							// if we used a scan to see a cloaked unit, make sure we prioritize it
							return enemyUnit;
						}
					}
				}
			}
			if (!allowUnitsThatCannotAttack && !CanUnitAttack(enemyUnit)) continue;
			int distance = enemyUnit.getDistance(myUnit);
			if(distance < distanceFromClosestEnemy) {
				distanceFromClosestEnemy = distance;
				selectedEnemyUnit = enemyUnit;
			}
    	}
    	if (selectedEnemyUnit == null && allowUnitsThatCannotAttack == false) {
    		selectedEnemyUnit = SelectEnemyUnit(myUnit, enemyUnits, true);
    	}
    	return selectedEnemyUnit; 
    }
    
    private static void moveAwayFrom(Unit myUnit, Position position, double moveDistance) {
        int dx = position.getX() - myUnit.getX();
        int dy = position.getY() - myUnit.getY();
        double vectorLength = Math.sqrt(dx * dx + dy * dy);
        double modifier = (moveDistance * 32) / vectorLength;
        dx = (int) (dx * modifier);
        dy = (int) (dy * modifier);

        Position newPosition = new Position(myUnit.getX() - dx, myUnit.getY() - dy).makeValid();

        if (myUnit.isIdle() || myUnit.getOrder() == null || myUnit.getOrderTargetPosition().getDistance(newPosition) > 0) {
        	myUnit.move(newPosition);	
        }
    }
    
    private static boolean handleMoveAwayIfCloserThan(Unit unit, Position avoidCenter, double minDist) {
        if (unit.getDistance(avoidCenter) < 3.2) {
            moveAwayFrom(unit, avoidCenter, 2);
            return true;
        }
        else {
            return false;
        }
    }
    
    private static boolean AvoidStorm(Unit myUnit) {
		if (myUnit.isUnderStorm()) {
			for (Bullet bullet : StarCraftInstance.game.getBullets()) {
				if (bullet.getType().equals(BulletType.Psionic_Storm)) {
					handleMoveAwayIfCloserThan(myUnit, bullet.getPosition(), 3.2);
					break;
				}
			}
			return true;
		}
		return false;
    }
    
    public static void attackUnits() {
    	
    	List<Unit> closestEnemyUnits = StarCraftInstance.game.enemy().getUnits();
    	if (closestEnemyUnits.size() > 0) {
    		for (final MilitaryUnit mu : MilitaryUnits) {
    			if (mu.LastOrderFrame + 2 > StarCraftInstance.game.getFrameCount()) {
    				continue;
    			}
    			if (AvoidStorm(mu.unit)) {
    				// unit is currently avoiding storm
    				continue;
    			}
    			//if (myUnit.isIdle()) {
//    					Collections.sort(closestEnemyUnits, new Comparator<Unit>() {
//    			            @Override
//    			            public int compare(Unit u1, Unit u2) {
//    							return myUnit.getPosition().getDistance(u1.getPosition()) < myUnit.getPosition().getDistance(u2.getPosition())
//    			                        ? -1 : 1;
//    			            }
//    			        });
    				Unit closestEnemy = SelectEnemyUnit(mu.unit, closestEnemyUnits, false);
    				if(closestEnemy == null) continue;
    				if (closestEnemy.getDistance(mu.unit) > 1000) continue;
    				MarineUseStim(mu.unit, closestEnemy);
//    				Position closestEnemyPosition = closestEnemy.getPosition();
//    				Position myCurrentUnitPosition = mu.unit.getPosition();
//    				mu.unit.move(new Position(
//    						myCurrentUnitPosition.getX() + 
//    							(
//    								(int)((closestEnemyPosition.getX() - myCurrentUnitPosition.getX()) * 0.05)
//    							),
//    						myCurrentUnitPosition.getY() + 
//    							(
//    								(int)((closestEnemyPosition.getY() - myCurrentUnitPosition.getY()) * 0.05)
//    							)
//    					)
//    				);
    				if (mu.unit.isIdle() || (mu.unit.getOrderTarget() == null || (mu.unit.getOrderTarget().getID() != closestEnemy.getID()))) {
    					CameraHelper.moveCamera(closestEnemy.getPosition());
    					mu.AttackUnit(closestEnemy);	
//    					if (closestEnemy.getType() == UnitType.Protoss_Zealot) {
//        					mu.LastOrderFrame = StarCraftInstance.game.getFrameCount();
//        					KiteMeleeUnits(mu.unit, closestEnemy);
//    					}else{
//    						mu.AttackUnit(closestEnemy);	
//        				}
    				}
//    				if (closestEnemy.getType() == UnitType.Protoss_Zealot) {
//    					mu.LastOrderFrame = StarCraftInstance.game.getFrameCount();
//    					KiteMeleeUnits(mu.unit, closestEnemy);
//    				}else if (mu.unit.isIdle()) {
//    					mu.LastOrderFrame = StarCraftInstance.game.getFrameCount();
//    					AttackAndMoveFoward(mu.unit, closestEnemy);
//    					//mu.unit.attack(closestEnemy.getPosition());
//    				}
    			//}
    		} 
    	}
    	//if (StarCraftInstance.game.elapsedTime() / 60 > 25) {
		if (UnitsManager.MilitaryUnits.size() >= 50) {
			if (Commander.currentPlayStyle == PlayStyles.Military) Commander.currentPlayStyle = PlayStyles.Greedy;
			if (EnemyManager.enemyBuildingMemory.size() > 0) {
				for (MilitaryUnit mu : MilitaryUnits) {
	    			if (mu.unit.isIdle()) {
    					for (Position enemyBuildingPosition : EnemyManager.enemyBuildingMemory) {
    						mu.AttackPosition(enemyBuildingPosition.makeValid());
    					}
	    			}
	    		}
			}else {
				int totalAttackableBases = 0;
				for (CustomBaseLocation cbl : BaseManager.baseLocations) {
        			// If this is a possible start location,
        			if (cbl.commandCenter == null && !StarCraftInstance.game.isVisible(cbl.baseLocation.getTilePosition())) {
        				totalAttackableBases++;
        			}
        		}
				if (attackedBaseLocations.size() >= totalAttackableBases) {
					// reset attacked base locations as we've attacked them all
					attackedBaseLocations = new ArrayList<CustomBaseLocation>();
				}
				for (MilitaryUnit mu : MilitaryUnits) {
	    			if (mu.unit.isIdle()) {
	    				for (CustomBaseLocation cbl : BaseManager.baseLocations) {
	            			// If this is a possible start location,
	            			if (!attackedBaseLocations.contains(cbl) && cbl.commandCenter == null && !StarCraftInstance.game.isVisible(cbl.baseLocation.getTilePosition())) {
	        					mu.AttackPosition(cbl.baseLocation.getPosition().makeValid());
	        					attackedBaseLocations.add(cbl);
	        					break;
	            			}
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