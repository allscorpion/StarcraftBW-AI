package interfaces;

import models.Worker;

public interface IStructure {
	boolean RequirementsMetToBuild(); 
	void OnSuccess(Worker worker);
}
