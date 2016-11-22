package edu.ucdavis.gc.bm.survey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import edu.ucdavis.gc.aa.utils.AAUtils;
import edu.ucdavis.gc.bm.descriptorGroup.Descriptor;
import edu.ucdavis.gc.bm.descriptorGroup.Group;

public class SubgroupsPerPosition {

	/**
	 * segmNo - segment index
	 */
	private Integer segmNo;
	/**
	 * resNo - residue index
	 */
	private Integer resNo;
	/**
	 * group - group of descriptors
	 */
	private Group group;
	/**
	 * featureID - feature name, e.g. "hydrophobicity"
	 */
	private String featureType;
	/**
	 * cutoff
	 */
	private Double cutoff;
	/**
	 * subgroups - list of sets of descriptors' indexes identifying the subgroup
	 */
	private List<TreeSet<Integer>> subGroupsIndexes = new ArrayList<TreeSet<Integer>>();
	/**
	 * short ID
	 */
	private String shortID;
	/**
	 * List of other features most probably over other positions which are in
	 * consensus with subgroups.<br>
	 * Index of the list corresponds to to index of list of subgroups.
	 */
	private List<TreeSet<String>> othersInConsensus = new ArrayList<TreeSet<String>>();

	/**
	 * Constructor of the class Clusters.
	 * 
	 * @param group
	 * @param segmNo
	 * @param resNo
	 * @param featureType
	 */
	public SubgroupsPerPosition(Group group, int segmNo, int resNo,
			String featureType, double cutoff) {
		this.group = group;
		this.segmNo = segmNo;
		this.resNo = resNo;
		this.featureType = featureType;
		this.cutoff = cutoff;
		setShortID();
		calcSubGroups();
		setOthersInConsensus();
	}

	/**
	 * getter of list of other features in consensus
	 * 
	 * @return
	 */
	public List<TreeSet<String>> getOthersInConsensus() {
		return othersInConsensus;
	}

	/**
	 * set shortID: segmNo,resNo,featureType
	 */
	private void setShortID() {
		StringBuilder sb = new StringBuilder("");
		sb.append(segmNo + ",");
		sb.append(resNo + ",");
		if (featureType.matches("(?i:aaprofile.*)")) {
			sb.append("A");
		} else if (featureType.matches("(?i:hydrophobic.*)")) {
			sb.append("H");
		} else if (featureType.matches("(?i:volume.*)")) {
			sb.append("V");
		} else if (featureType.matches("(?i:betabranch.*)")) {
			sb.append("B");
		} else if (featureType.matches("(?i:electric.*)")) {
			sb.append("E");
		} else if (featureType.matches("(?i:polar.*)")) {
			sb.append("P");
		}
		shortID = sb.toString();
	}

	private void calcSubGroups() {
		for (Character aa : AAUtils.getInstance().AA) { // loop over amino acids
			TreeSet<Integer> curIndexes = new TreeSet<Integer>();
			int index = 0;
			for (Descriptor desc : group.getDescriptors()) { // loop over
																// descriptors
																// in group
				Character curAA = desc.getSegments().get(this.segmNo).getSeq()
						.toUpperCase().charAt(this.resNo);
				if (curAA == '.') {
					curAA = 'X';
				}
				if (featureType.matches("(?i:aaprofile.*)")) {
					if (-AAUtils.getInstance().Blosum62[AAUtils.getInstance().indexAA
							.get(curAA)][AAUtils.getInstance().indexAA.get(aa)] < cutoff) {
						curIndexes.add(index);
					}
				} else if (featureType.matches("(?i:hydrophob.*)")) {
					if (Math.abs(AAUtils.getInstance().hydroPhobicScore
							.get(curAA)
							- AAUtils.getInstance().hydroPhobicScore.get(aa)) < this.cutoff) {
						curIndexes.add(index);
					}
				} else if (featureType.matches("(?i:volume.*)")) {
					if (Math.abs(AAUtils.getInstance().volume.get(curAA)
							- AAUtils.getInstance().volume.get(aa)) < this.cutoff) {
						curIndexes.add(index);
					}
				} else if (featureType.matches("(?i:betabranch.*)")) {
					if (Math.abs(AAUtils.getInstance().betaBranchness
							.get(curAA)
							- AAUtils.getInstance().betaBranchness.get(aa)) < this.cutoff) {
						curIndexes.add(index);
					}
				} else if (featureType.matches("(?i:electric.*)")) {
					if (Math.abs(AAUtils.getInstance().electricCharge
							.get(curAA)
							- AAUtils.getInstance().electricCharge.get(aa)) < this.cutoff) {
						curIndexes.add(index);
					}
				} else if (featureType.matches("(?i:polar.*)")) {
					if (Math.abs(AAUtils.getInstance().polarity.get(curAA)
							- AAUtils.getInstance().polarity.get(aa)) < this.cutoff) {
						curIndexes.add(index);
					}
				}
				index++;
			} // end loop over all descriptors

			// check if the putative subgroup has enough folds
			// redundant obsolete code: this requirement is checked in SubgroupsPerPositionPair class
			//if (!hasEnoughFolds(curIndexes, Props.getInt("minNoFoldsPerSubGroup", 7))) {
			//	continue;
			//}
			// adding and sorting
			if (subGroupsIndexes.size() == 0) {
				subGroupsIndexes.add(curIndexes);
			} else {
				int no = subGroupsIndexes.size() - 1;
				while (no >= 0
						&& subGroupsIndexes.get(no).size() <= curIndexes.size()) {
					no--;
				}
				no++;
				subGroupsIndexes.add(no, curIndexes);
			}

		} // end loop over amino acids
			// sort by folds
		Collections.sort(subGroupsIndexes, new Comparator<TreeSet<Integer>>() {

			public int compare(TreeSet<Integer> indexes1,
					TreeSet<Integer> indexes2) {
				TreeSet<String> folds1 = new TreeSet<String>();
				TreeSet<String> folds2 = new TreeSet<String>();
				for (int ind : indexes1) {
					String fold = group.getDescriptors().get(ind)
							.getFoldAstral();
					fold = fold.substring(0, fold.indexOf(".", 2));
					folds1.add(fold);
				}
				for (int ind : indexes2) {
					String fold = group.getDescriptors().get(ind)
							.getFoldAstral();
					fold = fold.substring(0, fold.indexOf(".", 2));
					folds2.add(fold);
				}
				if (folds1.size() != folds2.size()){
					return -(folds1.size() - folds2.size()); // Descending order
				} else {
					return -(indexes1.size() - indexes2.size()); // Descending order
				}
			}

		});
		;
		//filterSubGroups(Props.getDouble("newDescr_cutoff"));
		removeIdentical(); 
	}

	/**
	 * set subgroups in consensus with themselves
	 */
	private void setOthersInConsensus() {
		for (int k = 0; k < this.subGroupsIndexes.size(); k++) {
			this.othersInConsensus.add(new TreeSet<String>());
			this.othersInConsensus.get(k).add(getShortID(k));
		}
	}

	/**
	 * Method sorts subgroups and filtered them with respect of new elements
	 * 
	 * @param subGroup
	 * @param cutoff
	 *            - default: 0.5
	 */
	private void filterSubGroups(double cutoff) {
		TreeSet<Integer> done = new TreeSet<Integer>();
		List<TreeSet<Integer>> result = new ArrayList<TreeSet<Integer>>();
		for (int i = 0; i < subGroupsIndexes.size(); i++) {
			if (i == 0) {
				done.addAll(subGroupsIndexes.get(i));
				result.add(subGroupsIndexes.get(i));
			} else {
				TreeSet<Integer> fresh = new TreeSet<Integer>();
				fresh.addAll(subGroupsIndexes.get(i));
				fresh.removeAll(done);
				if ((double) fresh.size() / subGroupsIndexes.get(i).size() >= cutoff) {
					result.add(subGroupsIndexes.get(i));
					done.addAll(subGroupsIndexes.get(i));
				}
			}
		}
		subGroupsIndexes = result;
	}
	/**
	 * Method reduces redundancy: remove identical subgroups and those with 0 descriptors
	 */
	private void removeIdentical(){
		List<TreeSet<Integer>> result = new ArrayList<TreeSet<Integer>>();
		for (int i = 0; i < subGroupsIndexes.size(); i++) {
			boolean flag = true;
			// check if empty
			if (subGroupsIndexes.get(i).isEmpty()){
				flag = false;
			}
			// check if there are the same
			for (int j = 0; j < i; j++){
				if (i == j){continue;}
				if (subGroupsIndexes.get(i).equals(subGroupsIndexes.get(j))){
					flag = false;
					break;
				}
			}
			if (flag) {
				result.add(subGroupsIndexes.get(i));
			}
		}
		subGroupsIndexes = result;
	}
	
	/**
	 * Method checks if the subgroup has enough descriptors representing
	 * different folds.
	 * 
	 * @param subGroup
	 * @param cutoff
	 * @return
	 */
	public  boolean hasEnoughFolds(TreeSet<Integer> subGroup, int cutoff) {
		/*
		TreeSet<String> folds = new TreeSet<String>();
		for (int index : subGroup) {
			String astrClass = group.getDescriptors().get(index)
					.getFoldAstral();
			folds.add(astrClass.substring(0, astrClass.indexOf('.', 2)));
		}
		if (folds.size() >= cutoff) {
			return true;
		} else {
			return false;
		}
		*/
		return (getNoFolds(subGroup) >= cutoff);
	}

	private int getNoFolds(TreeSet<Integer> subGroup){
		TreeSet<String> folds = new TreeSet<String>();
		for (int index : subGroup) {
			String astrClass = group.getDescriptors().get(index)
					.getFoldAstral();
			folds.add(astrClass.substring(0, astrClass.indexOf('.', 2)));
		}
		return folds.size();
	}
	
	public TreeSet<Integer> getBiggestSubGroup() {
		if (subGroupsIndexes.size() == 0) {
			return new TreeSet<Integer>();
		}
		return this.subGroupsIndexes.get(0);
	}

	public List<TreeSet<Integer>> getSubGroups() {
		return this.subGroupsIndexes;
	}

	public String getStatSummaryBiggestSubGroup() {
		String result = "";
		TreeSet<String> folds = new TreeSet<String>();
		TreeSet<String> superFamilies = new TreeSet<String>();
		TreeSet<String> families = new TreeSet<String>();
		Set<Integer> biggestSubGroup = this.getBiggestSubGroup();
		for (int index : biggestSubGroup) {
			String astralClass = group.getDescriptors().get(index)
					.getFoldAstral();
			String fold = astralClass.substring(0, astralClass.indexOf('.', 2));
			folds.add(fold);
			String superFamily = astralClass.substring(0,
					astralClass.indexOf('.', fold.length() + 1));
			superFamilies.add(superFamily);
			String family = astralClass;
			families.add(family);
		}

		result = folds.size() + "," + superFamilies.size() + ","
				+ families.size() + "," + biggestSubGroup.size() + ","
				+ String.format("%6.3f", 100.0 * folds.size() / group.getNumberFolds()) + ","
				+ String.format("%6.3f", 100.0 * superFamilies.size() / group.getNumberSuperFamilies())
				+ "," + String.format("%6.3f", 100.0 * families.size() / group.getNumberFamilies())
				+ ","
				+ String.format("%6.3f", 100.0 * biggestSubGroup.size() / group.getNumberMembers());

		return result;
	}

	public String getStatSummarySubGroup(Set<Integer> subGroup) {
		String result = "";
		TreeSet<String> folds = new TreeSet<String>();
		TreeSet<String> superFamilies = new TreeSet<String>();
		TreeSet<String> families = new TreeSet<String>();
		for (int index : subGroup) {
			String astralClass = group.getDescriptors().get(index)
					.getFoldAstral();
			String fold = astralClass.substring(0, astralClass.indexOf('.', 2));
			folds.add(fold);
			String superFamily = astralClass.substring(0,
					astralClass.indexOf('.', fold.length() + 1));
			superFamilies.add(superFamily);
			String family = astralClass;
			families.add(family);
		}
		result = folds.size() + "," + superFamilies.size() + ","
				+ families.size() + "," + subGroup.size() + ","
				+ String.format("%6.3f", 100.0 * folds.size() / group.getNumberFolds()) + ","
				+ String.format("%6.3f", 100.0 * superFamilies.size() / group.getNumberSuperFamilies())
				+ "," + String.format("%6.3f", 100.0 * families.size() / group.getNumberFamilies())
				+ ","
				+ String.format("%6.3f", 100.0 * subGroup.size() / group.getNumberMembers());
		return result;
	}

	public String toString(boolean header) {
		StringBuilder sb = new StringBuilder("");
		if (header) {
			sb.append("Group,segmNo,resNo,feature,cutoff,fold,superfam,fam,descr,pfold,psuperfam,pfam,pdescr\n");
		}
		sb.append(group.getName() + ","); // group name
		sb.append(segmNo + ","); // segmNo
		sb.append(resNo + ","); // resNo
		sb.append(featureType + ","); // featureID
		sb.append(cutoff + ","); // cutoff
		sb.append(getStatSummaryBiggestSubGroup());
		sb.append(" " + this.getBiggestSubGroup() + "\n");
		return sb.toString();
	}

	public String toString(boolean header, boolean allSubGroups) {
		StringBuilder sb = new StringBuilder("");
		if (header) {
			sb.append("Group,segmNo,resNo,feature,cutoff,fold,superfam,fam,descr,pfold,psuperfam,pfam,pdescr\n");
		}
		if (allSubGroups) {
			for (Set<Integer> subGroup : this.subGroupsIndexes) {
				sb.append(group.getName() + ","); // group name
				//sb.append(segmNo + ","); // segmNo
				//sb.append(resNo + ","); // resNo
				//sb.append(featureType + ","); // featureID
				sb.append(shortID + ",");
				sb.append(cutoff + ","); // cutoff
				sb.append(getStatSummarySubGroup(subGroup) + " ");
				//sb.append(subGroup.toString());
				sb.append("\n");
			}
		} else {
			sb.append(group.getName() + ","); // group name
			//sb.append(segmNo + ","); // segmNo
			//sb.append(resNo + ","); // resNo
			//sb.append(featureType + ","); // featureID
			sb.append(shortID + ",");
			sb.append(cutoff + ","); // cutoff
			sb.append(getStatSummaryBiggestSubGroup() + " ");
			//sb.append(this.getBiggestSubGroup().toString() );
			sb.append("\n");
			
		}
		return sb.toString();
	}

	public String toString(Set<Integer> subGroup) {
		StringBuilder sb = new StringBuilder("");
		sb.append(group.getName() + ","); // group name
		sb.append(segmNo + ","); // segmNo
		sb.append(resNo + ","); // resNo
		sb.append(featureType + ","); // featureID
		sb.append(cutoff + ","); // cutoff
		sb.append(getStatSummarySubGroup(subGroup));
		sb.append(" " + subGroup);
		return sb.toString();
	}

	public String toString(int k) {
		Set<Integer> subGroup = subGroupsIndexes.get(k);
		StringBuilder sb = new StringBuilder("");
		sb.append(group.getName() + ","); // group name
		sb.append(segmNo + ","); // segmNo
		sb.append(resNo + ","); // resNo
		sb.append(featureType + ","); // featureID
		sb.append(cutoff + ","); // cutoff
		sb.append(getStatSummarySubGroup(subGroup) + " " + k);
		return sb.toString();
	}

	public String toStringShort(int k) {
		Set<Integer> subGroup = subGroupsIndexes.get(k);
		StringBuilder sb = new StringBuilder("");
		sb.append(segmNo + ","); // segmNo
		sb.append(resNo + ","); // resNo
		sb.append(featureType + ","); // featureID
		sb.append(k + ",");
		sb.append(subGroup.size()); //
		return sb.toString();
	}

	public String getShortID() {
		return this.shortID;
	}

	public String getShortID(int k) {
		try {
			return shortID + "," + k + ","
					+ this.subGroupsIndexes.get(k).size();
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	/**
	 * The method check subgroups in consensus (similar to a given one) for a
	 * several cutoffs.<br>
	 * 
	 * @param k
	 *            - index of subgroup
	 * @param noSegmentsCutoff
	 *            - number of segments involved: default 3 for D3K and 2 for D2K
	 * @param noEntriesProximityCutoff
	 *            - number of proximity features (A,V,H), default = 7
	 * @return true if all filters are passed; false otherwise
	 */
	public boolean passedFilters(int k, int noSegmentsCutoff,
			int noResidueCutoff, int noEntriesProximityCutoff) {
		TreeSet<Integer> segmentsInvolved = new TreeSet<Integer>();
		TreeSet<Integer> residuesInvolved = new TreeSet<Integer>();
		int entriesCount = 0;
		// similar - shortID of subgroups which are in consensus with a given
		// one
		for (String similar : this.othersInConsensus.get(k)) {
			// 0,2,A,3,7: segmNo, resNo, featureID, index subgroup,
			// noDescriptors
			String[] tokens = similar.split(",");
			segmentsInvolved.add(Integer.valueOf(tokens[0]));
			residuesInvolved.add(25 * Integer.valueOf(tokens[0])
					+ Integer.valueOf(tokens[1]));
			// check proximity features: A, V, H
			if (tokens[2].equalsIgnoreCase("A")
					|| tokens[2].equalsIgnoreCase("H")
					|| tokens[2].equalsIgnoreCase("V")) {
				entriesCount++;
			}
		}
		if (segmentsInvolved.size() < noSegmentsCutoff) {
			//System.err.println(segmentsInvolved.size() + "<" + noSegmentsCutoff);
			return false;
		}
		if (residuesInvolved.size() < noResidueCutoff) {
			//System.err.println(residuesInvolved.size() + "<" + noResidueCutoff);
			return false;
		}
		if (entriesCount < noEntriesProximityCutoff) {
			//System.err.println(entriesCount + "<" + noEntriesProximityCutoff);
			return false;
		}
		return true;
	}

	/**
	 * The method calculates F-M entropy:<br>
	 * = - sum(noFolds_i * LOG2(fractionFolds_i), i - index over subgroups
	 * @return
	 */
	public Double calcFMentropy(){
		double result = 0.0;
		double sum = 0.0;
		for (TreeSet<Integer> subGroup : subGroupsIndexes) {
			sum += getNoFolds(subGroup);
		}
		if (sum == 0.0){
			return 1.0;
		}
		for (TreeSet<Integer> subGroup : subGroupsIndexes) {
			int k = getNoFolds(subGroup);
			if (k != 0){
				result += -k*Math.log(k/sum)/Math.log(2);
			}
		}
		if (result == 0.0){
			return 1.0;
		}
		return result; 
	}
	
}
