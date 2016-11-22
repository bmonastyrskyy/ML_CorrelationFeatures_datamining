package edu.ucdavis.gc.bm.survey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import edu.ucdavis.gc.aa.utils.AAUtils;
import edu.ucdavis.gc.bm.descriptorGroup.Descriptor;
import edu.ucdavis.gc.bm.descriptorGroup.Group;

public class CompoundFeaturePerPosition implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Integer segmIndex;

	private Integer resIndex;

	private String featureID;

	private Double mean;

	private Double std;

	public CompoundFeaturePerPosition() {

	}

	public CompoundFeaturePerPosition(String shortID) {
		setParameteres(shortID);
	}

	private void setParameteres(String shortID) {
		String[] tokens = shortID.split(",");
		segmIndex = Integer.valueOf(tokens[0]);
		resIndex = Integer.valueOf(tokens[1]);
		featureID = tokens[2];
	}

	public void calcStatParam(String shortFeatureID, Group group,
			TreeSet<Integer> descrIndexes) {
		setParameteres(shortFeatureID);
		List<Double> scores = new ArrayList<Double>();
		for (Integer i : descrIndexes) {
			Descriptor desc = group.getDescriptors().get(i);
			Character curAA = desc.getSegments().get(segmIndex).getSeq()
					.toUpperCase().charAt(resIndex);
			if (curAA == '.') {
				curAA = 'X';
			}
			if (featureID.equals("H")) {
				scores.add(AAUtils.getInstance().hydroPhobicScore.get(curAA));
			} else if (featureID.equals("V")) {
				scores.add(AAUtils.getInstance().volume.get(curAA));
			} else if (featureID.equals("A")) {
				double avrg = 0.0;
				for (Integer j : descrIndexes) {

					Descriptor desc2 = group.getDescriptors().get(j);

					Character aa = desc2.getSegments().get(segmIndex).getSeq()
							.toUpperCase().charAt(resIndex);
					if (aa == '.') {
						aa = 'X';
					}
					avrg += (double) AAUtils.getInstance().Blosum62[AAUtils
							.getInstance().indexAA.get(curAA)][AAUtils
							.getInstance().indexAA.get(aa)] + 4;
				}
				avrg /=  descrIndexes.size();
				scores.add(avrg);
			} else if (featureID.equals("B")) {
				scores.add(AAUtils.getInstance().betaBranchness.get(curAA));
			} else if (featureID.equals("P")) {
				scores.add(AAUtils.getInstance().polarity.get(curAA));
			} else if (featureID.equals("E")) {
				scores.add(Math.abs(AAUtils.getInstance().electricCharge
						.get(curAA)));
			}
			Double[] res = calcMeanStd(scores);
			this.mean = res[0];
			this.std = res[1];
			if (featureID.equals("A")) {
				this.std = 0.5 * 2.941 ; // half of score range time number
													// of residues
			} else if (featureID.equals("V")) {
				this.std = 0.5 * 174.0 ; // half of score range time number
													// of residues
			} else if (featureID.equals("A")) {
				this.std = 0.5 * 15 ; // half of score range time number of
												// residues
			}
		}
	}

	private Double[] calcMeanStd(List<Double> list) {
		double eps = 0.01;
		double sum = 0.0;
		int count = 0;
		for (Double el : list) {
			try {
				sum += el;
				count++;
			} catch (NullPointerException e) {
				throw e;
			}
		}
		Double mean = sum / count;
		sum = 0.0;
		for (Double el : list) {
			sum += (el - mean) * (el - mean);
		}
		Double std = 1.0;
		if (count != 0 && count != 1) {
			std = Math.sqrt(sum / (count - 1));
		} 
		if (Math.abs(std) < eps ){
			std = 1.0;
		}
		Double[] result = new Double[2];
		result[0] = mean;
		result[1] = std;
		return result;
	}

	public int getSegmIndex() {
		return segmIndex;
	}

	public int getResIndex() {
		return resIndex;
	}

	public String getFeatureID() {
		return featureID;
	}

	// the methods below this line are related to calculating scorer for target
	// sequence

	public Double calcScore(Character targetAA, Group group,
			TreeSet<Integer> descrIndexes) {
		if (featureID.equals("H")) {
			return Math.abs((AAUtils.getInstance().hydroPhobicScore.get(targetAA) - mean)
					/ std);
		} else if (featureID.equals("V")) {
			return Math.abs((AAUtils.getInstance().volume.get(targetAA) - mean) / std);
		} else if (featureID.equals("A")) {
			double avrg = 0.0;
			for (Integer j : descrIndexes) {
				Descriptor desc2 = group.getDescriptors().get(j);
				Character aa = desc2.getSegments().get(segmIndex).getSeq()
						.toUpperCase().charAt(resIndex);
				avrg += (double) AAUtils.getInstance().Blosum62[AAUtils
						.getInstance().indexAA.get(targetAA)][AAUtils
						.getInstance().indexAA.get(aa)] + 4;

			}
			avrg /= descrIndexes.size();
			
			return Math.abs((avrg - mean) / std);
		} else if (featureID.equals("B")) {
			return Math.abs((AAUtils.getInstance().betaBranchness.get(targetAA) - mean)
					/ std);
		} else if (featureID.equals("P")) {
			return Math.abs((AAUtils.getInstance().polarity.get(targetAA) - mean) / std);
		} else if (featureID.equals("E")) {
			return Math.abs((Math
					.abs(AAUtils.getInstance().electricCharge.get(targetAA)) - mean)
					/ std);
		}
		return null;
	}

	public Double calcScoreSimple(Character targetAA, Group group,
			TreeSet<Integer> descrIndexes) {
		int result = 0;
		if (targetAA == 'X' || targetAA == '.') {
			return 0.0;
		}
		for (int descrIndex : descrIndexes) {
			try{
			Character aa = group.getDescriptors().get(descrIndex).getSegments()
					.get(segmIndex).getSeq().toUpperCase().charAt(resIndex);
			if (aa == targetAA) {
				return 1.0;
			}
			} catch (IndexOutOfBoundsException e){
				System.err.println(group.getName());
				System.err.println(group.getNumberMembers());
				System.err.println(descrIndexes);
				throw e;
			}
		}
		return 0.0;
	}

}
