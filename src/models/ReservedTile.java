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
		tilePositionBottomRight = new TilePosition(tp.getX() + ut.tileWidth() + paddingAmount, tp.getY() + ut.tileHeight() + paddingAmount);
		isTemp = true;
		buildingType = ut;
	}
	public TilePosition tilePositionTopLeft;
	public TilePosition tilePositionTopLeftWithPadding;
	public TilePosition tilePositionBottomRight;
	public boolean isTemp;
	public UnitType buildingType;
	
	public boolean isOverlappingTile(ReservedTile testPosition) {
		if (testPosition.tilePositionBottomRight.getX() < tilePositionTopLeft.getX()) {
			return false;
		}
		else if (testPosition.tilePositionTopLeftWithPadding.getX() > tilePositionBottomRight.getX()) {
			return false;
		}
		else if (testPosition.tilePositionBottomRight.getY() < tilePositionTopLeft.getY()) {
			return false;
		}
		else if (testPosition.tilePositionTopLeftWithPadding.getY() > tilePositionBottomRight.getY()) {
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
	    return true;
	}
}
