package models;

import bwapi.*;
import helpers.BuildingsManager;
import helpers.ResourcesManager;


public class Building {
	public Building(Unit builder, UnitType buildingType) {
		Init(builder, buildingType, null);
		
	}
	public Building(Unit builder, UnitType buildingType, TilePosition position) {
		Init(builder, buildingType, position);
	}
	public Unit _builder;
	public Unit _structure;
	public UnitType _buildingType;
	public TilePosition _buildingReservedPosition;
	public boolean _isConstructing;
	public boolean _isBuilderMoving;
	public boolean _isPositionOverriden;
	public void Init(Unit builder, UnitType buildingType, TilePosition position) {
		_builder = builder;
		_buildingType = buildingType;
		_isConstructing = false;
		_isBuilderMoving = false;
		ResourcesManager.PotentialSupply += _buildingType.supplyProvided();
		ResourcesManager.MineralsInReserve += _buildingType.mineralPrice();
		if (position == null) {
			_isPositionOverriden = false;
			SetBuildingPosition();			
		}else {
			_isPositionOverriden = true;
			SetBuildingPosition(position);
		}
		ConstructBuilding();
	}
	public void GetNewBuilder() {
		if (_builder == null) {
			// temporary code fix later
			BuildingsManager.BuildingFinishedConstruction(this);
			// get new worker
			//game.printf("Unable to find worker for " + _buildingType);
//			for (Unit worker : Workers) {
//				if (worker.canBuild()) {
//					_builder = worker;
//				}
//			}
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
		GetNewBuilder();
		if (_structure != null) {
			_builder.repair(_structure);
		}else {
			if (!_isBuilderMoving && !_isPositionOverriden) {
				BuildingsManager.RemoveBuildingReservedPosition(this);
    			SetBuildingPosition();	
    		}
			if (!_isBuilderMoving && _isPositionOverriden) {
				BuildingsManager.BuildingFinishedConstruction(this);
    		} else {
    			ConstructBuilding();	
    		}
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