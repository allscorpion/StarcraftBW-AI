package models;

import bwapi.*;
import helpers.BuildingsManager;
import helpers.ResourcesManager;


public class Building {
	public Building(Game _game, Player _self, BuildingsManager bm, ResourcesManager rm, Unit builder, UnitType buildingType) {
		Init(_game, _self, bm, rm, builder, buildingType, null);
		
	}
	public Building(Game _game, Player _self, BuildingsManager bm, ResourcesManager rm, 
			Unit builder, UnitType buildingType, TilePosition position) {
		Init(_game, _self, bm, rm, builder, buildingType, position);
	}
	public BuildingsManager _buildingsManager;
	public ResourcesManager _resourcesManager;
	public Unit _builder;
	public Unit _structure;
	public UnitType _buildingType;
	public TilePosition _buildingReservedPosition;
	public boolean _isConstructing;
	public boolean _isBuilderMoving;
	
	public void Init(Game _game, Player _self, BuildingsManager bm, ResourcesManager rm, 
			Unit builder, UnitType buildingType, TilePosition position) {
		_buildingsManager = bm;
		_resourcesManager = rm;
		_builder = builder;
		_buildingType = buildingType;
		_isConstructing = false;
		_isBuilderMoving = false;
		_resourcesManager.PotentialSupply += _buildingType.supplyProvided();
		_resourcesManager.MineralsInReserve += _buildingType.mineralPrice();
		if (position == null) {
			SetBuildingPosition();			
		}else {
			SetBuildingPosition(position);
		}
		ConstructBuilding();
	}
	public void GetNewBuilder() {
		if (_builder == null) {
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
		SetBuildingPosition(_buildingsManager.getBuildTile(_builder, _buildingType, _builder.getTilePosition()));
	}
	public void SetBuildingPosition(TilePosition position) {
		_buildingsManager.ReservedTiles.add(position);
		_buildingReservedPosition = position;	
	}
	
	
	
	public void RestartBuild() {
		GetNewBuilder();
		if (_structure != null) {
			_builder.repair(_structure);
		}else {
			if (!_isBuilderMoving) {
				_buildingsManager.RemoveBuildingReservedPosition(this);
    			SetBuildingPosition();	
    		}
			ConstructBuilding();
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