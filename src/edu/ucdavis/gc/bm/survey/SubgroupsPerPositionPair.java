package edu.ucdavis.gc.bm.survey;

import java.util.TreeSet;
import edu.ucdavis.gc.bm.properties.Props;

public class SubgroupsPerPositionPair {

	private SubgroupsPerPosition first;

	private SubgroupsPerPosition second;

	public SubgroupsPerPositionPair(SubgroupsPerPosition first,
			SubgroupsPerPosition second) {
		this.first = first;
		this.second = second;
	}

	public void findOthersInConsensus() {
		for (int k = 0; k < first.getSubGroups().size(); k++) { // loop over
																// subgroups of
																// first
			if (!first.hasEnoughFolds(first.getSubGroups().get(k), Props.getInt("minNoFoldsPerSubGroup", 7))){
				break; // the subgroups are sorted by number of folds 
			}
			for (int l = 0; l < second.getSubGroups().size(); l++) { // loop
																		// over
																		// subgroups
																		// of
																		// second
				if (!second.hasEnoughFolds(second.getSubGroups().get(l), Props.getInt("minNoFoldsPerSubGroup", 7))){
					break;
				}
				TreeSet<Integer> common = new TreeSet<Integer>();
				common.addAll(first.getSubGroups().get(k));
				common.retainAll(second.getSubGroups().get(l));
				if ((double) common.size() / first.getSubGroups().get(k).size() > Props
						.getDouble("commonDescr_cutoff", "0.9")) { // check if
																	// in
																	// consensus
					first.getOthersInConsensus().get(k)
							.add(second.getShortID(l));
				}
			}
		}
	}

	public void reduceRedundancyOthersInConsensus() {
		for (int k = 0; k < first.getOthersInConsensus().size(); k++) { // loop over subgroups of first
			TreeSet<String> consensusFirst = first.getOthersInConsensus()
					.get(k);
			//System.out.println(consensusFirst);
			if (consensusFirst.size() == 0) {
				continue;
			}
			for (int l = 0; l < second.getOthersInConsensus().size(); l++) { // loop over subgroups of second
				TreeSet<String> consensusSecond = second.getOthersInConsensus()
						.get(l);
				if (consensusSecond.size() == 0) {
					continue;
				}
				if (consensusFirst.containsAll(consensusSecond)) {
					second.getOthersInConsensus().get(l).clear();
				} else if (consensusSecond.containsAll(consensusFirst)) {
					first.getOthersInConsensus().get(k).clear();
					break;
				}
			}
		}
	}

	/*
	 * public String toString(boolean ifFirst) { StringBuilder sb = new
	 * StringBuilder(""); TreeSet<Integer> subGroupFirst =
	 * first.getSubGroups().get(k); if (ifFirst) { sb.append("==========\n");
	 * sb.append(first.toString(k) + "\n"); sb.append("--------\n" ); } int
	 * count = 0; for (TreeSet<Integer> subGroupSecond : second.getSubGroups())
	 * { TreeSet<Integer> common = new TreeSet<Integer>();
	 * common.addAll(subGroupFirst); common.retainAll(subGroupSecond); if
	 * ((double) common.size() / subGroupFirst.size() > Props
	 * .getDouble("commonDescr_cutoff", "0.9")) {
	 * sb.append(second.toString(count) + "\n"); } count++; } return
	 * sb.toString(); }
	 * 
	 * public String toStringShort(boolean ifFirst) { StringBuilder sb = new
	 * StringBuilder(""); TreeSet<Integer> subGroupFirst =
	 * first.getSubGroups().get(k); if (ifFirst) {
	 * //sb.append("\n==========\n"); //sb.append(first.toString(k) + "\n");
	 * //sb.append("--------\n" ); sb.append("\n{"+first.toStringShort(k)+"}|");
	 * } int count = 0; for (TreeSet<Integer> subGroupSecond :
	 * second.getSubGroups()) { TreeSet<Integer> common = new
	 * TreeSet<Integer>(); common.addAll(subGroupFirst);
	 * common.retainAll(subGroupSecond); if ((double) common.size() /
	 * subGroupFirst.size() > Props .getDouble("commonDescr_cutoff", "0.9")) {
	 * sb.append("{"+second.toStringShort(count)+"}"); } count++; }
	 * 
	 * return sb.toString(); }
	 */

}
