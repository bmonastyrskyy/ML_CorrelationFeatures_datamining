package edu.ucdavis.gc.bm.visualisation;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import edu.ucdavis.gc.bm.descriptorGroup.Group;
import edu.ucdavis.gc.bm.properties.Props;
import edu.ucdavis.gc.bm.survey.SubgroupsPerPosition;

public class SummaryMatrix {

	/**
	 * three-dimensional matrix with indexes i, j, k:<br>
	 * i - index of descriptor<br>
	 * j - index of segment<br>
	 * k - index of position<br>
	 */
	private final double[][][] matrix;
	/**
	 * two-dimensional matrix with indexes i, j:<br>
	 * i - index of descriptor<br>
	 * j - index of compound feature<br>
	 */
	private  List<TreeSet<Integer>> listCF; 
	/**
	 * list of CF names - order is synchronized with the order of listCF
	 */
	private List<String> listCFNames = null;
	
	private List<SubgroupsPerPosition> listSubGroups;

	public SummaryMatrix(Group group, List<SubgroupsPerPosition> listSubGroups) {
		matrix = new double[group.getDescriptors().size()][group
				.getRootDescriptor().getNumberSegments()][group
				.getRootDescriptor().getNumberResidues()];
		this.listSubGroups = listSubGroups;
		calcMatrix();
	}

	private void calcMatrix() {
		double max = 0;
		for (SubgroupsPerPosition sggp : listSubGroups) {
			for (int index = 0; index < sggp.getSubGroups().size(); index++) {
				if (!sggp.passedFilters(index, 
						Props.getInt("noSegmentsCutoff",2), 
						Props.getInt("noResidueCutoff",5), 
						Props.getInt("noEntriesProximityCutoff",7))){
					continue;
				}
				for (int i : sggp.getSubGroups().get(index)) {
					for (String shortIDs_inconsensus : sggp
							.getOthersInConsensus().get(index)) {
						String[] tokens = shortIDs_inconsensus.split(",");
						int j = Integer.valueOf(tokens[0]);
						int k = Integer.valueOf(tokens[1]);
						matrix[i][j][k] = matrix[i][j][k] + 1;
						if (max < matrix[i][j][k]) {
							max = matrix[i][j][k];
						}
					}
				}
			}
		}
		if (max > 0) {
			for (int i = 0; i < matrix.length; i++) {
				for (int j = 0; j < matrix[0].length; j++) {
					for (int k = 0; k < matrix[0][0].length; k++) {
						matrix[i][j][k] = matrix[i][j][k] / max;
					}
				}
			}
		}
	}

	public double[][][] getMatrix() {
		return matrix;
	}

	private void calcListCF(){
		listCF = new ArrayList<TreeSet<Integer>>();
		listCFNames = new ArrayList<String>();
		for (SubgroupsPerPosition sggp : listSubGroups) {
			for (int index = 0; index < sggp.getSubGroups().size(); index++) {
				if (!sggp.passedFilters(index, 
						Props.getInt("noSegmentsCutoff",2), 
						Props.getInt("noResidueCutoff",5), 
						Props.getInt("noEntriesProximityCutoff",7))){
					continue;
				}
				listCF.add(sggp.getSubGroups().get(index));
				listCFNames.add("cf_" + sggp.getShortID(index).replaceAll(",", ""));
			}
		}
	}
	
	public List<TreeSet<Integer>> getListCF(){
		if (listCF == null){
			calcListCF();
		}
		return listCF;
	}
	
	public String listCFToString(Group group){
		StringBuilder sb = new StringBuilder("");
		if (listCF == null || listCFNames == null){
			calcListCF();
		}
		// print header
		for (int j = 0; j < listCFNames.size(); j++){
			sb.append(listCFNames.get(j) + ",");
		}
		sb.append("fold\n");
		for(int i = 0; i < group.getDescriptors().size(); i++){
			for (int j = 0; j < listCF.size(); j++){
				if (listCF.get(j).contains(i)){
					sb.append("1,");					
				} else {
					sb.append("0,");
				}
			}
			sb.append(group.getDescriptors().get(i).getFoldAstral() + "_" + i + "\n");
		}
		return sb.toString();
	}
	
}
