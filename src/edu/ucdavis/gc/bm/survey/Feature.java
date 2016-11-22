package edu.ucdavis.gc.bm.survey;

import edu.ucdavis.gc.bm.descriptorGroup.Group;
import edu.ucdavis.gm.bm.assignments.Assignment;

public interface Feature {
	
	public Double calcScore(Group group, Assignment assign);
	
	public String getFeatureName();
	
}
