package models;

import bwapi.*;
import helpers.BuildingsManager;
import helpers.DrawingHelper;
import helpers.ResourcesManager;
import helpers.StarCraftInstance;
import helpers.WorkersManager;


public class Building {
	public Building(Worker builder, UnitType buildingType) {
		Init(builder, buildingType, null);
		
	}
	public Building(Worker builder, UnitType buildingType, TilePosition position) {
		Init(builder, buildingType, position);
	}
	public Worker _builder;
	public TilePosition _lastBuilderPosition;
	public Unit _structure;
	public UnitType _buildingType;
	public ReservedTile _buildingReservedPosition;
	public boolean _isBuilderMoving;
	public boolean _isPositionOverriden;
	public boolean _costsTaken;
	public void Init(Worker w, UnitType buildingType, TilePosition position) {
		_costsTaken = false;
    	w.isBuilding = true;
		_builder = w;
		_lastBuilderPosition = w.unit.getTilePosition();
		_buildingType = buildingType;
		_isBuilderMoving = false;
		if (position == null) {
			// auto position building
			_isPositionOverriden = false;
			SetBuildingPosition();			
		}else {
			// manually position building
			_isPositionOverriden = true;
			SetBuildingPosition(position);
		}
	}
	public void GetNewBuilderIfRequired() {
		if (_builder == null) {
			// get new worker
			//game.printf("Unable to find worker for " + _buildingType);
			_builder = WorkersManager.GetWorker(_buildingReservedPosition.tilePositionTopLeft);
			_isBuilderMoving = false;
		}
	}
	public void SetBuildingPosition() {
		SetBuildingPosition(BuildingsManager.getBuildTile(_builder.unit, _buildingType, _builder.unit.getTilePosition()));
	}
	public void SetBuildingPosition(TilePosition position) {
		if (position != null) {
			ReservedTile rt = new ReservedTile(position, _buildingType);
			BuildingsManager.ReservedTiles.add(rt);
			_buildingReservedPosition = rt;	
		}
	}
	
	
	
	public void RestartBuild() {
		GetNewBuilderIfRequired();
		if (_structure != null) {
			_builder.unit.rightClick(_structure);
		}else {
			ConstructBuilding();
//			if (!_isBuilderMoving && !_isPositionOverriden) {
//				BuildingsManager.RemoveBuildingReservedPosition(this);
//    			SetBuildingPosition();	
//    		}
//			if (!_isBuilderMoving && _isPositionOverriden) {
//				BuildingsManager.BuildingFinishedConstruction(this);
//    		} else {
//    			ConstructBuilding();	
//    		}
		}
	}
	
	public void AddBuildingCosts() {
		if (!_costsTaken) {
			_costsTaken = true;
			ResourcesManager.MineralsInReserve += _buildingType.mineralPrice();
			if (_buildingType == UnitType.Terran_Supply_Depot) {
				ResourcesManager.PotentialSupply += _buildingType.supplyProvided();	
			}	
		}
	}
	
	public void ConstructBuilding() {
		if (_buildingReservedPosition != null) {			
			if (ResourcesManager.MineralsPerMinute > 0) {
				long travelTimeInSeconds = (Math.round(_builder.unit.getPosition().getDistance(_buildingReservedPosition.tilePositionTopLeft.toPosition())) / Math.round(_builder.unit.getType().topSpeed() * 10));
				//DrawingHelper.drawTextOnScreen("Seconds taken to get to building position - " + travelTimeInSeconds);
				int currentMineralsIncludingBuildingCost = 0;
				if (_buildingType == UnitType.Terran_Supply_Depot) {
					currentMineralsIncludingBuildingCost = ResourcesManager.getCurrentMinerals();
				}else {
					currentMineralsIncludingBuildingCost = ResourcesManager.getCurrentMineralsIncludingUnitsQueue();
				}
				if (_costsTaken) {
					currentMineralsIncludingBuildingCost += _buildingType.mineralPrice();
				}
				int secondLeftUntilWeCanAffordBuilding = (_buildingType.mineralPrice() - currentMineralsIncludingBuildingCost) / (ResourcesManager.MineralsPerMinute / 60);
				//DrawingHelper.drawTextOnScreen("Seconds until we can afford building - " + secondLeftUntilWeCanAffordBuilding);	
				if (secondLeftUntilWeCanAffordBuilding - travelTimeInSeconds <= 0) {
					// start construction of building early to account for travel time
					Worker w = WorkersManager.GetWorker(_buildingReservedPosition.tilePositionTopLeft);
					if (w != null) {
						_builder = w;	
					}
					if (_builder.unit.canBuild(_buildingType, _buildingReservedPosition.tilePositionTopLeft)) {
		    			if (_builder.unit.build(_buildingType, _buildingReservedPosition.tilePositionTopLeft)) {
		    				AddBuildingCosts();
		    				_isBuilderMoving = false;		
		    			}
					}else {
						if (!_isBuilderMoving) {
							_isBuilderMoving = true;	
							AddBuildingCosts();
						}
						_builder.unit.move(_buildingReservedPosition.tilePositionTopLeft.toPosition());
					}
				}
			}
		}
	}
}