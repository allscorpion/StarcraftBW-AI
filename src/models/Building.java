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
	public void Init(Worker w, UnitType buildingType, TilePosition position) {
    	w.isBuilding = true;
		_builder = w;
		_lastBuilderPosition = w.unit.getTilePosition();
		_buildingType = buildingType;
		_isBuilderMoving = false;
		if (_buildingType == UnitType.Terran_Supply_Depot) {
			ResourcesManager.PotentialSupply += _buildingType.supplyProvided();	
		}
		ResourcesManager.MineralsInReserve += _buildingType.mineralPrice();
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
			_builder = WorkersManager.GetWorker();
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
	public void ConstructBuilding() {
		if (_buildingReservedPosition != null) {			
			if (ResourcesManager.MineralsPerMinute > 0) {
				long travelTimeInSeconds = (Math.round(_builder.unit.getPosition().getDistance(_buildingReservedPosition.tilePositionTopLeft.toPosition())) / Math.round(_builder.unit.getType().topSpeed() * 10));
				DrawingHelper.drawTextOnScreen("Seconds taken to get to building position - " + travelTimeInSeconds);
				int secondLeftUntilWeCanAffordBuilding = (_buildingType.mineralPrice() - (ResourcesManager.getCurrentMinerals() + _buildingType.mineralPrice())) / (ResourcesManager.MineralsPerMinute / 60);
				DrawingHelper.drawTextOnScreen("Seconds until we can afford building - " + secondLeftUntilWeCanAffordBuilding);	
				if (secondLeftUntilWeCanAffordBuilding - travelTimeInSeconds <= 0) {
					// start construction of building early to account for travel time
					if (_builder.unit.canBuild(_buildingType, _buildingReservedPosition.tilePositionTopLeft)) {
		    			if (_builder.unit.build(_buildingType, _buildingReservedPosition.tilePositionTopLeft)) {
		    				_buildingReservedPosition.isTemp = false;
		    				_isBuilderMoving = false;		
		    			}
					}else {
						_isBuilderMoving = true;	
						_builder.unit.move(_buildingReservedPosition.tilePositionTopLeft.toPosition());
					}
				}
			}
		}
	}
}