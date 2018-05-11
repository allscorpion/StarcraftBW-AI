package models;

import bwapi.TilePosition;
import bwapi.UnitType;
import helpers.BuildingsManager;

public class ReservedTile {
	public ReservedTile(TilePosition tp, UnitType ut) {
		Init(tp, ut, BuildingsManager.paddingAroundBuildings);
	}
	public ReservedTile(TilePosition tp, UnitType ut, int paddingAmount) {
		Init(tp, ut, paddingAmount);
	}
	private void Init(TilePosition tp, UnitType ut, int paddingAmount) {
		tilePositionTopLeft = tp;
		tilePositionTopLeftWithPadding = new TilePosition(tp.getX() - paddingAmount, tp.getY() - paddingAmount);
		tilePositionBottomRight = new TilePosition(tp.getX() + ut.tileWidth(), tp.getY() + ut.tileHeight());
		tilePositionBottomRightWithPadding = new TilePosition(tp.getX() + ut.tileWidth() + paddingAmount, tp.getY() + ut.tileHeight() + paddingAmount);
		isTemp = true;
		buildingType = ut;
		addSpaceForAddon(ut, tp);
	}
	public TilePosition tilePositionTopLeft;
	public TilePosition tilePositionTopLeftWithPadding;
	public TilePosition tilePositionBottomRight;
	public TilePosition tilePositionBottomRightWithPadding;
	public boolean isTemp;
	public UnitType buildingType;
	public ReservedTile addOn;
	
	public boolean isOverlappingTile(ReservedTile testPosition) {
		if (testPosition.tilePositionBottomRightWithPadding.getX() - BuildingsManager.paddingAroundBuildings < tilePositionTopLeftWithPadding.getX() + BuildingsManager.paddingAroundBuildings) {
			return false;
		}
		else if (testPosition.tilePositionTopLeftWithPadding.getX() - BuildingsManager.paddingAroundBuildings > tilePositionBottomRightWithPadding.getX() + BuildingsManager.paddingAroundBuildings 
				&& (this.addOn != null && testPosition.tilePositionTopLeftWithPadding.getX() - BuildingsManager.paddingAroundBuildings > this.addOn.tilePositionBottomRight.getX())) {
			return false;
		}
		else if (testPosition.tilePositionBottomRightWithPadding.getY() - BuildingsManager.paddingAroundBuildings < tilePositionTopLeftWithPadding.getY() + BuildingsManager.paddingAroundBuildings) {
			return false;
		}
		else if (testPosition.tilePositionTopLeftWithPadding.getY() - BuildingsManager.paddingAroundBuildings > tilePositionBottomRightWithPadding.getY() + BuildingsManager.paddingAroundBuildings) {
			return false;
		}
		return true;
	}
	@Override
	public boolean equals(Object obj) {
	    if (obj == null) {
	        return false;
	    }
	    if (!ReservedTile.class.isAssignableFrom(obj.getClass())) {
	        return false;
	    }
	    final ReservedTile other = (ReservedTile) obj;
	    if ((this.tilePositionTopLeft == null) ? (other.tilePositionTopLeft != null) : this.tilePositionTopLeft.getDistance(other.tilePositionTopLeft) != 0) {
	        return false;
	    }
	    if ((this.tilePositionTopLeftWithPadding == null) ? (other.tilePositionTopLeftWithPadding != null) : this.tilePositionTopLeftWithPadding.getDistance(other.tilePositionTopLeftWithPadding) != 0) {
	        return false;
	    }
	    if ((this.tilePositionBottomRight == null) ? (other.tilePositionBottomRight != null) : this.tilePositionBottomRight.getDistance(other.tilePositionBottomRight) != 0) {
	        return false;
	    }
	    if ((this.tilePositionBottomRightWithPadding == null) ? (other.tilePositionBottomRightWithPadding != null) : this.tilePositionBottomRightWithPadding.getDistance(other.tilePositionBottomRightWithPadding) != 0) {
	        return false;
	    }
	    return true;
	}
	
	private boolean addSpaceForAddon(UnitType building, TilePosition position) {
        boolean canThisBuildingHaveAddon = building.canBuildAddon();

        if (canThisBuildingHaveAddon) {
        	TilePosition pos = new TilePosition(position.getX() + buildingType.tileWidth(), position.getY() + (buildingType.tileHeight() / 2));
        	this.addOn = new ReservedTile(pos, UnitType.Terran_Comsat_Station, 0);
            return true;
        }

        return false;
    }
}
