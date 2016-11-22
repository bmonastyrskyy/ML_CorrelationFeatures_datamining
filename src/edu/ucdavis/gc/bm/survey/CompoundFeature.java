package edu.ucdavis.gc.bm.survey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import edu.ucdavis.gc.bm.descriptorGroup.Group;
import edu.ucdavis.gc.bm.properties.Props;
import edu.ucdavis.gm.bm.assignments.Assignment;

public class CompoundFeature implements Serializable, Feature{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * list of 
	 */
	private List<CompoundFeaturePerPosition> cfppList;
	/**
	 * set of descriptor indexes 
	 */
	private TreeSet<Integer> descrIndexes;
	
	private String featureID; 
	
	public CompoundFeature(){
		
	}
	
	public void setCFPPlist(Group group, String featureID, TreeSet<String> otherInConsensus, TreeSet<Integer> descrIndexes){
		cfppList = new ArrayList<CompoundFeaturePerPosition>();
		this.featureID = featureID;
		this.descrIndexes = descrIndexes;
		for (String shortFeatureID : otherInConsensus) {
			CompoundFeaturePerPosition cfpp = new CompoundFeaturePerPosition();
			try{
				cfpp.calcStatParam(shortFeatureID, group, descrIndexes);
			} catch (NullPointerException e) {
				System.err.println(group.getName() + " " + shortFeatureID);
				throw e;
			}
			cfppList.add(cfpp);
		}
	}
	
	public String getFeatureID(){
		return featureID;
	}
	
	public Double calcScore(Group group, Assignment assign){
		String calcScoreMethod = Props.get("calcScoreMethod");
		if (calcScoreMethod.equalsIgnoreCase("distToMean")){
			return calcScoreDistToMean(group, assign);
		} else {
			return calcScoreSimple(group, assign);
		}
	}
	
	public Double calcScoreDistToMean( Group group, Assignment assign){
		double result = 0.0;
		int count = 0;
		for(CompoundFeaturePerPosition cfpp : cfppList){
			int segmIndex = cfpp.getSegmIndex();
			int resIndex = cfpp.getResIndex();
			Character targetAA = null;
			try{
				targetAA = assign.getSegmAssigns().get(segmIndex).getSeqArray()[resIndex];
			} catch (ArrayIndexOutOfBoundsException e){
				targetAA = 'X';
				//System.err.println(assign.getTargetName() + " " + segmIndex + " " + resIndex);
				//throw e;
			}
			if (targetAA == '.'){
				targetAA = 'X';
			}
			result += cfpp.calcScore(targetAA, group, descrIndexes);
			count++;
		}
		return result/count;
	}
	
	public Double calcScoreSimple(Group group, Assignment assign){
		double result = 0.0;
		int count = 0;
		for(CompoundFeaturePerPosition cfpp : cfppList){
			int segmIndex = cfpp.getSegmIndex();
			int resIndex = cfpp.getResIndex();
			Character targetAA = null;
			try{
				targetAA = assign.getSegmAssigns().get(segmIndex).getSeqArray()[resIndex];
			} catch (ArrayIndexOutOfBoundsException e){
				targetAA = 'X';
				//System.err.println(assign.getTargetName() + " " + segmIndex + " " + resIndex);
				//throw e;
			}
			if (targetAA == '.'){
				targetAA = 'X';
			}
			result += cfpp.calcScoreSimple(targetAA, group, descrIndexes);
			count++;
		}
		return result/count;
	}

	

	@Override
	public String getFeatureName() {
		// TODO Auto-generated method stub
		return "cf_"+this.getFeatureID().replaceAll(",", "");
	}
	
	public TreeSet<Integer> getDescrIndexes(){
		return this.descrIndexes;
	}
}
