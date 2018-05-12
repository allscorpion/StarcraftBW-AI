package interfaces;

import models.Worker;

public interface IStructure {
	boolean RequirementsMetToBuild(); 
	boolean Greedy_RequirementsMetToBuild();
	boolean Military_RequirementsMetToBuild();
	boolean Balanced_RequirementsMetToBuild();
	void OnSuccess(Worker worker);
}
