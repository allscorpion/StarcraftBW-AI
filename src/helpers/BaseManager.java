package helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
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
import models.SpawnLocation;
import models.Worker;

public class BaseManager {
	public static void Init() {
		mySpawn = BWTA.getStartLocation(StarCraftInstance.self);
		baseLocations = new ArrayList<CustomBaseLocation>();
		spawnLocations = new ArrayList<SpawnLocation>();
		for (BaseLocation bl : BWTA.getBaseLocations()) {
			baseLocations.add(new CustomBaseLocation(bl));	
			if (bl.isStartLocation()) {
				spawnLocations.add(new SpawnLocation(bl, bl.getTilePosition().getDistance(StarCraftInstance.self.getStartLocation()) <= 0));
			}
		}
	}
	
	public static BaseLocation mySpawn;
	
	public static List<CustomBaseLocation> baseLocations;
	
	public static List<SpawnLocation> spawnLocations;
	
	private static double amountOfWorkersPerMineral = 2.5;
	
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
			int totalMiningWorkers = SelectAllMineralWorkersAssignedToCommandCenter(cbl).size();
			int totalGasMiningWorkers = SelectAllGasWorkersAssignedToCommandCenter(cbl).size();
			//StarCraftInstance.game.drawCircleMap(cbl.commandCenter.unit.getPosition().getX(), cbl.commandCenter.unit.getPosition().getY(), 300,  Color.Green);
			
			StringBuilder commandCenterText = new StringBuilder(totalMiningWorkers + "/" + ((int) (cbl.baseLocation.getMinerals().size() * amountOfWorkersPerMineral)) + "\n");
			commandCenterText.append(totalGasMiningWorkers + "/" + (cbl.baseLocation.getGeysers().size() * 3) + "\n");
			DrawingHelper.drawTextOnUnit(cbl.commandCenter.unit, commandCenterText.toString());
			return totalMiningWorkers + totalGasMiningWorkers;
		}
		return 0;
	}
	
	public static int GetCommandCenterMaxWorkers(CustomBaseLocation cbl) {
		int mineralWorkers = (int) (cbl.baseLocation.getMinerals().size() * amountOfWorkersPerMineral);
		int gasWorkers = 0;
		if (cbl.baseLocation.getGeysers().size() > 0) {
			gasWorkers = cbl.baseLocation.getGeysers().size() * 3;
		}
		return mineralWorkers + gasWorkers;
	}
	
	public static void SetBaseStartedToMineOut(Unit mineral) {
		if (!mineral.getType().isMineralField()) return;
		for (CustomBaseLocation cbl : baseLocations) {
			if (cbl.commandCenter != null && cbl.baseLocation.getStaticMinerals().contains(mineral)) {
				cbl.commandCenter.isMineralFieldStartedToDeplete = true;
				break;
			}
		}
	}
	
	public static int GetTotalAmountOfCommandCenters() {
		int total = 0;
		for (CustomBaseLocation cbl : baseLocations) {
			if (cbl.commandCenter != null && !cbl.commandCenter.isMineralFieldStartedToDeplete) {
				total++;
			}
		}
		return total;
	}
	
	public static void AllowOversaturationForAllCommandCenters() {
		for (CustomBaseLocation cbl : baseLocations) {
			if (cbl.commandCenter != null && !cbl.commandCenter.isMineralFieldStartedToDeplete) {
				cbl.commandCenter.allowOversaturation = true;
			}
		}
	}
	
	public static void DisallowOversaturationForAllCommandCenters() {
		for (CustomBaseLocation cbl : baseLocations) {
			if (cbl.commandCenter != null && !cbl.commandCenter.isMineralFieldStartedToDeplete) {
				cbl.commandCenter.allowOversaturation = false;
			}
		}
	}
	
	public static int TotalWorkersAllCommandCenters() {
		int totalWorkers = 0;
		for (CustomBaseLocation cbl : baseLocations) {
			if (cbl.commandCenter != null) {
				totalWorkers += cbl.baseLocation.getMinerals().size() * amountOfWorkersPerMineral;
				if (cbl.baseLocation.getGeysers().size() > 0) {
					totalWorkers +=	cbl.baseLocation.getGeysers().size() * 3;
				}
			}
		}
		return totalWorkers;
	}
	
	public static int ReserveCommandCenterAddonSpace() {
		int totalWorkers = 0;
		for (CustomBaseLocation cbl : baseLocations) {
			if (cbl.commandCenter != null) {
				//BuildingsManager.ReservedTiles.add(new TilePosition(cbl.commandCenter.unit.getTilePosition().getX() + UnitType.Terran_Comsat_Station.tileSize().getX(), cbl.commandCenter.unit.getTilePosition().getY()));
			}
		}
		return totalWorkers;
	}
	
	public static List<CustomBaseLocation> SortBasesByDistanceToUnit(final Unit myUnit) {
		List<CustomBaseLocation> cbls = BaseManager.baseLocations;
		Collections.sort(cbls, new Comparator<CustomBaseLocation>() {
            @Override
            public int compare(CustomBaseLocation u1, CustomBaseLocation u2) {
				return myUnit.getDistance(u1.baseLocation) < myUnit.getDistance(u2.baseLocation) 
						? -1 : 1;
            }
        });
		return cbls;
	}
	
	public static CustomBaseLocation GetBaseThatHasFreeSpaceForWorkers(final Unit myUnit) {
		List<CustomBaseLocation> cbls = SortBasesByDistanceToUnit(myUnit);
		CustomBaseLocation freeBase = null;
		boolean aBaseAllowedOverSaturation = false;
		for (CustomBaseLocation cbl : cbls) {
			if (cbl.commandCenter != null) {
				if (!aBaseAllowedOverSaturation && cbl.commandCenter.allowOversaturation) aBaseAllowedOverSaturation = true;
				if (cbl.commandCenter.allowOversaturation || !IsBaseFullySaturated(cbl)) {
					return cbl;
				}
			}
		}
		if (freeBase == null && !aBaseAllowedOverSaturation) {
			AllowOversaturationForAllCommandCenters();
			freeBase = GetBaseThatHasFreeSpaceForWorkers(myUnit);
		}
		return freeBase;
	}
	
	public static void TransferAdditionalWorkersToFreeBase() {
		List<Worker> extraWorkers = new ArrayList<Worker>();
		for (CustomBaseLocation cbl : baseLocations) {
			if (cbl.commandCenter != null) {
				int amountOfWorkersOverSaturated = GetAmountOfWorkersAssignedToCommandCenter(cbl) - GetCommandCenterMaxWorkers(cbl);
				if (amountOfWorkersOverSaturated > 0) {
					for (Worker w : SelectAllMineralWorkersAssignedToCommandCenter(cbl)) {
						extraWorkers.add(w);
						amountOfWorkersOverSaturated--;
						if (amountOfWorkersOverSaturated == 0) {
							break;
						}
					}
				}
			}
		}
		if (extraWorkers.size() > 0) {
			int workersSent = 0;
			for (CustomBaseLocation cbl : baseLocations) {
				if (cbl.commandCenter != null) {
					if (!IsBaseFullySaturated(cbl)) {
						// command center has free room, transfer workers
						int amountOfFreeSpace = GetCommandCenterMaxWorkers(cbl) - GetAmountOfWorkersAssignedToCommandCenter(cbl);
						for (int i = workersSent; i < extraWorkers.size(); i++) {
							WorkersManager.SendWorkerToClosestMineral(cbl, extraWorkers.get(i));
							amountOfFreeSpace--;
							workersSent++;
							if (amountOfFreeSpace == 0) {
								break;
							}
						}
					}
				}
			}
			// we still have more workers allow oversaturation
			if (workersSent != extraWorkers.size()) {
				for (CustomBaseLocation cbl : baseLocations) {
					if (cbl.commandCenter != null) {
						for (int i = workersSent; i < extraWorkers.size(); i++) {
							WorkersManager.SendWorkerToClosestMineral(cbl, extraWorkers.get(i));
						}
					}
				}
			}
		}
	}
	
	public static boolean IsBaseFullySaturated(CustomBaseLocation cbl) {
		if (cbl.commandCenter != null) {
			return !(GetAmountOfWorkersAssignedToCommandCenter(cbl) < GetCommandCenterMaxWorkers(cbl));	
		}
		return true;
	}
	
	public static List<Worker> SelectAllMineralWorkersAssignedToCommandCenter(CustomBaseLocation cbl){
		List<Worker> workers = new ArrayList<Worker>();
		if (cbl.commandCenter != null) {
			for (Worker w : WorkersManager.Workers) {
				if (w.miningFrom != null) {
					if (cbl.baseLocation.getMinerals().contains(w.miningFrom)) {
						workers.add(w);
					}	
				}
			}	
		}
		return workers;
	}
	
	public static List<Worker> SelectAllGasWorkersAssignedToCommandCenter(CustomBaseLocation cbl){
		List<Worker> workers = new ArrayList<Worker>();
		if (cbl.commandCenter != null) {
			for (Worker w : WorkersManager.Workers) {
				for (Unit geyser : cbl.baseLocation.getGeysers()) {
					if (w.miningFrom != null) {
						if (geyser.getID() == w.miningFrom.getID()) {
							workers.add(w);	
						}	
					}
				}
			}	
		}
		return workers;
	}
	
	public static void AssignGasToCommandCenter(Unit geyser){
		for (CustomBaseLocation cbl : baseLocations) {
			if (cbl.commandCenter != null) {
				for (Unit baseGeyser : cbl.baseLocation.getGeysers()) {
					if (geyser.getID() == baseGeyser.getID()) {
						cbl.commandCenter.gasStructure = geyser;
						return;
					}	
				}
			}
		}
	}
	
	public static void RemoveGasFromCommandCenter(Unit geyser){
		for (CustomBaseLocation cbl : baseLocations) {
			if (cbl.commandCenter != null) {
				for (Unit baseGeyser : cbl.baseLocation.getGeysers()) {
					if (geyser.getID() == baseGeyser.getID()) {
						cbl.commandCenter.gasStructure = null;
						for (Worker w : WorkersManager.Workers) {
							if (w.miningFrom.getID() == geyser.getID()) {
								w.miningFrom = null;
								w.unit.stop();
							}
						}
						return;
					}	
				}
			}
		}
	}
	
	public static void TransferWorkersToRefinery(){
		for (CustomBaseLocation cbl : baseLocations) {
			if (cbl.commandCenter != null && cbl.commandCenter.gasStructure != null && SelectAllGasWorkersAssignedToCommandCenter(cbl).size() < 3) {
				List<Worker> refineryWorkers = new ArrayList<Worker>();
				List<Worker> ccWorkers = SelectAllMineralWorkersAssignedToCommandCenter(cbl);
				if (ccWorkers.size() > 0) {
					for (Worker w : ccWorkers) {
						refineryWorkers.add(w);
						if (refineryWorkers.size() == 3) {
							 for (Worker refineryWorker : refineryWorkers) {
								 refineryWorker.miningFrom = cbl.commandCenter.gasStructure;
								 refineryWorker.unit.gather(cbl.commandCenter.gasStructure);
							}
							return;
						}
					}
				}
			}
		}
	}
	
	public static int TotalActiveRefinerys(){
		int totalRefinerys = 0;
		for (CustomBaseLocation cbl : baseLocations) {
			if (cbl.commandCenter != null && cbl.commandCenter.hasGasStructure) {
				totalRefinerys++;
			}
		}
		return totalRefinerys;
	}
	
	
	
}
