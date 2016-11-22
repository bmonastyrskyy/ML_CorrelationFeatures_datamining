package edu.ucdavis.gc.bm.survey;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import edu.ucdavis.gc.aa.utils.AAUtils;
import edu.ucdavis.gc.bm.descriptorGroup.Descriptor;
import edu.ucdavis.gc.bm.descriptorGroup.Group;
import edu.ucdavis.gm.bm.assignments.Assignment;

/**
 * Class deals with profile feature:<br>
 * all positions and all descriptors in a group are taking into account.<br>
 * In the current version "AA" feature is implemented only.
 * 
 * @author bohdan
 *
 */
public class ProfileFeature implements Serializable, Feature {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * weights : List of HashMaps: every HashMap corresponds to segment: key -
	 * position, value -
	 */
	private HashMap<Integer, HashMap<Integer, Double>> weights;

	private String featureID = "A";

	public ProfileFeature(){
		
	}
	
	public ProfileFeature(HashMap<Integer, HashMap<Integer, Double>> weights,
			String featureID) {
		this.weights = weights;
	}

	public ProfileFeature(HashMap<Integer, HashMap<Integer, Double>> weights) {
		this.weights = weights;
	}

	/**
	 * The method calculates the weights per position using KM-entropy formula.
	 * @param group
	 * @param listSubGroups
	 * @param featureID
	 */
	public void calcWeights(Group group,
			List<SubgroupsPerPosition> listSubGroups, String featureID) {
		weights = new HashMap<Integer, HashMap<Integer, Double>>();
		for (SubgroupsPerPosition sgpp : listSubGroups) {
			String shortID = sgpp.getShortID();
			String [] tokens = shortID.split(",");
			int segmI = Integer.valueOf(tokens[0]);
			int resI = Integer.valueOf(tokens[1]);
			// filter for the same feature type
			if (!tokens[2].equalsIgnoreCase(featureID)){
				continue;
			}
			for (Integer segmIndex = 0; segmIndex < group.getRootDescriptor()
					.getSegments().size(); segmIndex++) {
				if (segmI != segmIndex) {
					continue;
				}
				weights.put(segmIndex, new HashMap<Integer, Double>());
				for (Integer resIndex = 0; resIndex < group.getRootDescriptor()
						.getSegments().get(segmIndex).getSeq().length(); resIndex++) {
					if (resI != resIndex) {
						continue;
					}
					weights.get(segmIndex).put(resIndex, sgpp.calcFMentropy());
				}
			}
		}
	}

	/**
	 * The methods calculates the weights per residue positions setting all of them to 1.0.<br>
	 * @param group
	 */
	public void calcWeights(Group group) {
		weights = new HashMap<Integer, HashMap<Integer, Double>>();
		for (Integer segmIndex = 0; segmIndex < group.getRootDescriptor()
				.getSegments().size(); segmIndex++) {
			weights.put(segmIndex, new HashMap<Integer, Double>());
			for (Integer resIndex = 0; resIndex < group.getRootDescriptor()
					.getSegments().get(segmIndex).getSeq().length(); resIndex++) {
				weights.get(segmIndex).put(resIndex, 1.0);
			}
		}
	}

	@Override
	public Double calcScore(Group group, Assignment assign) {
		Double result = 0.0;
		int countPositions = 0;
		// loop over segments
		for (int segmIndex : weights.keySet()) {
			// loop over positions
			for (int resIndex : weights.get(segmIndex).keySet()) {
				Character targetAA;
				try{
					targetAA = assign.getSegmAssigns().get(segmIndex)
						.getSeqArray()[resIndex];
				} catch (ArrayIndexOutOfBoundsException e){
					targetAA = 'X';
				}
				if (featureID.contains("A")) {
					double avrg = 0.0;
					int count = 0;
					// loop over descriptors
					for (Descriptor desc : group.getDescriptors()) {
						Character curAA = desc.getSegments().get(segmIndex)
								.getSeq().toUpperCase().charAt(resIndex);
						if (curAA == '.'){
							curAA = 'X';
						}
						avrg += AAUtils.getInstance().Blosum62[AAUtils
								.getInstance().indexAA.get(curAA)][AAUtils
								.getInstance().indexAA.get(targetAA)] + 4;
						count++;
					}
					avrg /= count;
					//result += (weights.get(segmIndex).get(resIndex) * avrg);
					result += avrg;
				}
				countPositions++;
			}
		}
		return result/countPositions;
	}

	@Override
	public String getFeatureName() {
		// TODO Auto-generated method stub
		return "cf_" + featureID + "prof";
	}
}
