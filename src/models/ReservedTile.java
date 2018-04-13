package models;

import bwapi.TilePosition;
import bwapi.UnitType;

public class ReservedTile {
	public ReservedTile(TilePosition tp, UnitType ut) {
		tilePositionTopLeft = tp;
		tilePositionBottomRight = new TilePosition(tp.getX() + ut.tileWidth(), tp.getY() + ut.tileHeight());
	}
	public TilePosition tilePositionTopLeft;
	public TilePosition tilePositionBottomRight;
	
	public boolean isOverlappingTile(ReservedTile testPosition) {
		if (testPosition.tilePositionBottomRight.getX() < tilePositionTopLeft.getX()) {
			return false;
		}
		else if (testPosition.tilePositionTopLeft.getX() > tilePositionBottomRight.getX()) {
			return false;
		}
		else if (testPosition.tilePositionBottomRight.getY() < tilePositionTopLeft.getY()) {
			return false;
		}
		else if (testPosition.tilePositionTopLeft.getY() > tilePositionBottomRight.getY()) {
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
	    if ((this.tilePositionBottomRight == null) ? (other.tilePositionBottomRight != null) : this.tilePositionBottomRight.getDistance(other.tilePositionBottomRight) != 0) {
	        return false;
	    }
	    return true;
	}
}
