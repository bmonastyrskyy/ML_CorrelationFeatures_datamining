package edu.ucdavis.gc.bm.survey;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import edu.ucdavis.gc.aa.utils.AAUtils;
import edu.ucdavis.gc.bm.descriptorGroup.Descriptor;
import edu.ucdavis.gc.bm.descriptorGroup.Group;
import edu.ucdavis.gm.bm.assignments.Assignment;

public class CompoundBestPerPositionFeature implements Serializable, Feature {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Descriptor indexes for the best feature per position.<br>
	 * Key1 - index of the segment;<br>
	 * Key2 - index of residue;<br>
	 * value - tree set of indexes of descriptors.
	 */
	private HashMap<Integer, HashMap<Integer, TreeSet<Integer>>> descIndexPerPosition;

	/**
	 * weights : List of HashMaps: every HashMap corresponds to segment: key -
	 * position, value -
	 */
	private HashMap<Integer, HashMap<Integer, Double>> weights;

	private String featureID = "A";

	public CompoundBestPerPositionFeature(
			HashMap<Integer, HashMap<Integer, TreeSet<Integer>>> descIndexPerPosition,
			HashMap<Integer, HashMap<Integer, Double>> weights, String featureID) {
		this.descIndexPerPosition = descIndexPerPosition;
		this.weights = weights;
		this.featureID = featureID;
	}

	public CompoundBestPerPositionFeature(){
		
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
			if (null == descIndexPerPosition){
				descIndexPerPosition = new HashMap<Integer, HashMap<Integer, TreeSet<Integer>>>();
			}
			for (Integer segmIndex = 0; segmIndex < group.getRootDescriptor()
					.getSegments().size(); segmIndex++) {
				if (segmI != segmIndex) {
					continue;
				}
				descIndexPerPosition.put(segmIndex, new HashMap<Integer, TreeSet<Integer>>());
				weights.put(segmIndex, new HashMap<Integer, Double>());
				for (Integer resIndex = 0; resIndex < group.getRootDescriptor()
						.getSegments().get(segmIndex).getSeq().length(); resIndex++) {
					if (resI != resIndex) {
						continue;
					}
					descIndexPerPosition.get(segmIndex).put(resIndex,sgpp.getBiggestSubGroup()); 
					weights.get(segmIndex).put(resIndex, sgpp.calcFMentropy());
				}
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
					targetAA= assign.getSegmAssigns().get(segmIndex)
							.getSeqArray()[resIndex];
				} catch (ArrayIndexOutOfBoundsException e){
					targetAA = 'X';
				}
				double avrg = 0.0;
				int count = 0;
				// loop over descriptors
				for (int descIndex : descIndexPerPosition.get(segmIndex).get(resIndex)){
					Descriptor desc = group.getDescriptors().get(descIndex);
					Character curAA = desc.getSegments().get(segmIndex)
							.getSeq().toUpperCase().charAt(resIndex);
					if (curAA == '.'){
						curAA = 'X';
					}
					if (featureID.equals("A")) {
						avrg += AAUtils.getInstance().Blosum62[AAUtils
								.getInstance().indexAA.get(curAA)][AAUtils
								.getInstance().indexAA.get(targetAA)] + 4;
					}
					if (featureID.equals("H")) {
						avrg += Math.abs(AAUtils.getInstance().hydroPhobicScore
								.get(curAA)
								- AAUtils.getInstance().hydroPhobicScore
										.get(targetAA));
					}
					if (featureID.equals("V")) {
						avrg += Math.abs(AAUtils.getInstance().volume
								.get(curAA)
								- AAUtils.getInstance().volume
										.get(targetAA));
					}
					count++;
				}
				avrg /= count;
				//result += (weights.get(segmIndex).get(resIndex) * avrg);
				result += avrg;
				countPositions++;
			}
		}
		return result/countPositions;
	}

	@Override
	public String getFeatureName() {
		return "cf_" + featureID + "bestPP" ;
	}

}
