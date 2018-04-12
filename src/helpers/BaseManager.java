package helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import models.Building;
import models.CommandCenter;
import models.CustomBaseLocation;
import models.Worker;

public class BaseManager {
	public static void Init() {
		mySpawn = BWTA.getStartLocation(StarCraftInstance.self);
		for (BaseLocation bl : BWTA.getBaseLocations()) {
			baseLocations.add(new CustomBaseLocation(bl));	
		}
	}
	
	public static CustomBaseLocation GetCustomBaseLocationFromPosition(Position p) {
		for (CustomBaseLocation cbl : baseLocations) {
			if (cbl.baseLocation.getPosition().getDistance(p) <= Math.max(UnitType.Terran_Command_Center.tileSize().getX(), UnitType.Terran_Command_Center.tileSize().getY())) {
				return cbl;
			}
		}
		return null;
	}
	
	public static void AssignCommandCenterToBaseLocation(BaseLocation bl, CommandCenter cc) {
		for (CustomBaseLocation cbl : baseLocations) {
			if (cbl.baseLocation.getPosition() == bl.getPosition()) {
				cbl.commandCenter = cc;
			}
		}
	}
	
	public static void RemoveCommandCenterToBaseLocation(BaseLocation bl) {
		for (CustomBaseLocation cbl : baseLocations) {
			if (cbl.baseLocation.getPosition() == bl.getPosition()) {
				cbl.commandCenter = null;
			}
		}
	}
	
	public static int GetAmountOfWorkersAssignedToCommandCenter(CustomBaseLocation cbl) {
		if (cbl.commandCenter != null) {
			// found base location 
			int totalMiningWorkers = SelectAllWorkersAssignedToCommandCenter(cbl).size();
			//StarCraftInstance.game.drawCircleMap(cbl.commandCenter.unit.getPosition().getX(), cbl.commandCenter.unit.getPosition().getY(), 300,  Color.Green);
			
			StringBuilder commandCenterText = new StringBuilder(totalMiningWorkers + "/" + GetCommandCenterMaxMineralWorkers(cbl) + "\n");
			DrawingHelper.drawTextOnUnit(cbl.commandCenter.unit, commandCenterText.toString());
			return totalMiningWorkers;
		}
		return 0;
	}
	
	public static int GetCommandCenterMaxMineralWorkers(CustomBaseLocation cbl) {
		return (int) (cbl.baseLocation.getMinerals().size() * amountOfWorkersPerMineral);
	}
	
	public static int GetTotalAmountOfCommandCenters() {
		int total = 0;
		for (CustomBaseLocation cbl : baseLocations) {
			if (cbl.commandCenter != null) {
				total++;
			}
		}
		return total;
	}
	
	public static void AllowOversaturationForAllCommandCenters() {
		for (CustomBaseLocation cbl : baseLocations) {
			if (cbl.commandCenter != null) {
				cbl.commandCenter.allowOversaturation = true;
			}
		}
	}
	
	public static void DisallowOversaturationForAllCommandCenters() {
		for (CustomBaseLocation cbl : baseLocations) {
			if (cbl.commandCenter != null) {
				cbl.commandCenter.allowOversaturation = false;
			}
		}
	}
	
	public static int TotalWorkersAllCommandCenters() {
		int totalWorkers = 0;
		for (CustomBaseLocation cbl : baseLocations) {
			if (cbl.commandCenter != null) {
				totalWorkers += cbl.baseLocation.getMinerals().size() * amountOfWorkersPerMineral;
			}
		}
		return totalWorkers;
	}
	
	public static CustomBaseLocation GetBaseThatHasFreeSpaceForWorkers(final Unit myUnit) {
		List<CustomBaseLocation> cbls = BaseManager.baseLocations;
		Collections.sort(cbls, new Comparator<CustomBaseLocation>() {
            @Override
            public int compare(CustomBaseLocation u1, CustomBaseLocation u2) {
				return myUnit.getDistance(u1.baseLocation) < myUnit.getDistance(u2.baseLocation) 
						? -1 : 1;
            }
        });
		for (CustomBaseLocation cbl : cbls) {
			if (cbl.commandCenter != null) {
				if (cbl.commandCenter.allowOversaturation || !IsBaseFullySaturated(cbl)) {
					return cbl;
				}
			}
		}
		return null;
	}
	
	public static void TransferAdditionalWorkersToFreeBase() {
		List<Worker> extraWorkers = new ArrayList<Worker>();
		for (CustomBaseLocation cbl : baseLocations) {
			if (cbl.commandCenter != null) {
				int amountOfWorkersOverSaturated = GetAmountOfWorkersAssignedToCommandCenter(cbl) - GetCommandCenterMaxMineralWorkers(cbl);
				if (amountOfWorkersOverSaturated > 0) {
					for (Worker w : SelectAllWorkersAssignedToCommandCenter(cbl)) {
						extraWorkers.add(w);
						amountOfWorkersOverSaturated--;
						if (amountOfWorkersOverSaturated == 0) {
							break;
						}
					}
				}
			}
		}
		int workersSent = 0;
		for (CustomBaseLocation cbl : baseLocations) {
			if (cbl.commandCenter != null) {
				if (!IsBaseFullySaturated(cbl)) {
					// command center has free room, transfer workers
					int amountOfFreeSpace = GetCommandCenterMaxMineralWorkers(cbl) - GetAmountOfWorkersAssignedToCommandCenter(cbl);
					for (int i = workersSent; i < extraWorkers.size(); i++) {
						WorkersManager.SendWorkerToClosestMineral(cbl, extraWorkers.get(i));
						amountOfFreeSpace--;
						if (amountOfFreeSpace == 0) {
							break;
						}
					}
				}
			}
		}
		// we still have more workers allow oversaturation
		if (extraWorkers.size() > 0) {
			for (CustomBaseLocation cbl : baseLocations) {
				if (cbl.commandCenter != null) {
					for (int i = workersSent; i < extraWorkers.size(); i++) {
						WorkersManager.SendWorkerToClosestMineral(cbl, extraWorkers.get(i));
					}
				}
			}
		}
	}
	
	public static boolean IsBaseFullySaturated(CustomBaseLocation cbl) {
		if (cbl.commandCenter != null) {
			return !(GetAmountOfWorkersAssignedToCommandCenter(cbl) < GetCommandCenterMaxMineralWorkers(cbl));	
		}
		return true;
	}
	
	public static List<Worker> SelectAllWorkersAssignedToCommandCenter(CustomBaseLocation cbl){
		List<Worker> workers = new ArrayList<Worker>();
		if (cbl.commandCenter != null) {
			for (Worker w : WorkersManager.Workers) {
				if (cbl.baseLocation.getMinerals().contains(w.miningFrom)) {
					workers.add(w);
				}
			}	
		}
		return workers;
	}
	
	public static BaseLocation mySpawn;
	
	public static List<CustomBaseLocation> baseLocations = new ArrayList<CustomBaseLocation>();
	
	private static double amountOfWorkersPerMineral = 2.5;
}
