package models;

import bwapi.*;
import helpers.BuildingsManager;
import helpers.ResourcesManager;
import helpers.WorkersManager;


public class Building {
	public Building(Unit builder, UnitType buildingType) {
		Init(builder, buildingType, null);
		
	}
	public Building(Unit builder, UnitType buildingType, TilePosition position) {
		Init(builder, buildingType, position);
	}
	public Unit _builder;
	public TilePosition _lastBuilderPosition;
	public Unit _structure;
	public UnitType _buildingType;
	public TilePosition _buildingReservedPosition;
	public boolean _isBuilderMoving;
	public boolean _isPositionOverriden;
	public void Init(Unit builder, UnitType buildingType, TilePosition position) {
		// remove from workers so that the builder doesn't get selected again
		WorkersManager.Workers.remove(builder);
		WorkersManager.Builders.add(builder);
		_builder = builder;
		_lastBuilderPosition = builder.getTilePosition();
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
		ConstructBuilding();
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
		SetBuildingPosition(BuildingsManager.getBuildTile(_builder, _buildingType, _builder.getTilePosition()));
	}
	public void SetBuildingPosition(TilePosition position) {
		BuildingsManager.ReservedTiles.add(position);
		_buildingReservedPosition = position;	
	}
	
	
	
	public void RestartBuild() {
		GetNewBuilderIfRequired();
		if (_structure != null) {
			_builder.rightClick(_structure);
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
			if (_builder.canBuild(_buildingType, _buildingReservedPosition)) {
    			if (_builder.build(_buildingType, _buildingReservedPosition)) {
    				_isBuilderMoving = false;		
    			}
			}else if (!_isBuilderMoving){
				_isBuilderMoving = true;
				_builder.move(_buildingReservedPosition.toPosition());
			}
		}
	}
}