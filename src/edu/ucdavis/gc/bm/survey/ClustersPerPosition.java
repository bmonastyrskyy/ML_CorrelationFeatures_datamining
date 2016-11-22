package edu.ucdavis.gc.bm.survey;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import edu.ucdavis.gc.aa.utils.AAUtils;
import edu.ucdavis.gc.bm.clustering.HierarchicalClustering;
import edu.ucdavis.gc.bm.clustering.Metricable;
import edu.ucdavis.gc.bm.descriptorGroup.Descriptor;
import edu.ucdavis.gc.bm.descriptorGroup.Group;

/**
 * The class deals with clusters generated over the set of descriptors in a
 * group.<br>
 * The clusters are built based on the features per residue position.
 * 
 * @author bohdan
 *
 */
public class ClustersPerPosition implements Comparable, Metricable {
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
	private String featureID;
	/**
	 * clusters - list of sets of descriptors' indexes identifying the clusters
	 */
	private List<? extends Set<Integer>> clusters;

	/**
	 * Constructor of the class Clusters.
	 * 
	 * @param group
	 * @param segmNo
	 * @param resNo
	 * @param featureID
	 */
	public ClustersPerPosition(Group group, int segmNo, int resNo,
			String featureID) {
		this.group = group;
		this.segmNo = segmNo;
		this.resNo = resNo;
		this.featureID = featureID;
		calcClusters();
	}

	/**
	 * Getter of clusters.
	 * 
	 * @return
	 */
	public List<? extends Set<Integer>> getClusters() {
		return clusters;
	}

	/**
	 * The method returns the biggest cluster.
	 * 
	 * @return
	 */
	public Set<Integer> getBiggestCluster() {
		Set<Integer> result = new TreeSet<Integer>();
		for (Set<Integer> cl : clusters) {
			if (cl.size() > result.size()) {
				result.clear();
				result.addAll(cl);
			}
		}
		return result;
	}
	/**
	 * The method returns summary of biggest cluster:<br>
	 * numbers of folds, superfamilies , families and descriptors.
	 * @return
	 */
	public String getStatSummaryBiggestCluster() {
		String result = "";
		TreeSet<String> folds = new TreeSet<String>();
		TreeSet<String> superFamilies = new TreeSet<String>();
		TreeSet<String> families = new TreeSet<String>();
		Set<Integer> biggestCluster = this.getBiggestCluster();
		for (int index : biggestCluster) {
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
		result = "Fld: " + folds.size() + ", Sf: " + superFamilies.size()
				+ ",  Fml: " + families.size() + ", Dscr:"
				+ biggestCluster.size();
		return result;
	}

	/**
	 * Getter of segmNo.
	 * 
	 * @return
	 */
	public Integer getSegmNo() {
		return this.segmNo;
	}

	/**
	 * Getter of resNo.
	 * 
	 * @return
	 */
	public Integer getResNo() {
		return this.resNo;
	}

	/**
	 * Getter of featureID.
	 * 
	 * @return
	 */
	public String getFeatureID() {
		return this.featureID;
	}

	/**
	 * Private method, performs calculation of clusters.
	 */
	private void calcClusters() {
		List<MetricFeature> elements = new ArrayList<MetricFeature>();
		for (Descriptor desc : group.getDescriptors()) {
			Character c = desc.getSegments().get(segmNo).getSeq().toUpperCase()
					.toCharArray()[resNo];
			if (c == '.') {
				c = 'X';
			}
			elements.add(new MetricFeature(c));
		}
		HierarchicalClustering hclust = new HierarchicalClustering(elements);
		hclust.setFDistBetweenClusters(2); // set flag - distance between
											// clusters: maximum distance
											// between elements
		if (featureID.matches("(?i:hydrophob.*)")) {
			hclust.process(0.6);
		} else if (featureID.matches("(?i:volume.*)")) {
			hclust.process(50.0);
		} else if (featureID.matches("(?i:polarity.*)")) {
			hclust.process(0.5);
		} else if (featureID.matches("(?i:electric.*)")) {
			hclust.process(0.5);
		} else if (featureID.matches("(?i:betabranch.*)")) {
			hclust.process(0.5);
		} else if (featureID.matches("(?i:aaprofile.*)")) {
			hclust.process(2.0);
		}
		clusters = hclust.getClusters();
	}

	/**
	 * Private inner class which implements Metricable interface for a feature
	 * per residue position.<br>
	 * It is used in clustering algorithm.
	 * 
	 * @author bohdan
	 *
	 */
	private class MetricFeature implements Metricable {

		private Character c;

		MetricFeature(Character c) {
			super();
			this.c = c;
		}

		private Character getC() {
			return c;
		}

		@Override
		public Double distanceTo(Metricable o) {
			// down casting Metricable variable o to class MetricFeature
			Character otherC = ((MetricFeature) o).getC();
			if (featureID.matches("(?i:hydrophob.*)")) {
				try {
					return Math.abs(AAUtils.getInstance().hydroPhobicScore
							.get(c)
							- AAUtils.getInstance().hydroPhobicScore
									.get(otherC));
				} catch (NullPointerException e) {
					System.out.println(c);
					throw e;
				}
			} else if (featureID.matches("(?i:volume.*)")) {
				return Math.abs(AAUtils.getInstance().volume.get(c)
						- AAUtils.getInstance().volume.get(otherC));
			} else if (featureID.matches("(?i:polarity.*)")) {
				return Math.abs(AAUtils.getInstance().polarity.get(c)
						- AAUtils.getInstance().polarity.get(otherC));
			} else if (featureID.matches("(?i:electric.*)")) {
				return Math.abs(AAUtils.getInstance().electricCharge.get(c)
						- AAUtils.getInstance().electricCharge.get(otherC));
			} else if (featureID.matches("(?i:betabranch.*)")) {
				return Math.abs(AAUtils.getInstance().betaBranchness.get(c)
						- AAUtils.getInstance().betaBranchness.get(otherC));
			} else if (featureID.matches("(?i:aaprofile.*)")) {
				return (double) -AAUtils.getInstance().Blosum62[AAUtils
						.getInstance().indexAA.get(c)][AAUtils.getInstance().indexAA
						.get(otherC)];
			}
			return null;
		}

	}

	/**
	 * Method compareTo() from interface Comparable.<br>
	 * The comparison is structured according to the hierarchy: <li>comparison
	 * of segments <li>comparison of residue positions <li>comparison of
	 * featureIDs <br>
	 */
	@Override
	public int compareTo(Object otherObject) {

		if (this.segmNo == ((ClustersPerPosition) otherObject).getSegmNo()) {
			if (this.resNo == ((ClustersPerPosition) otherObject).getResNo()) {
				return this.featureID
						.compareTo(((ClustersPerPosition) otherObject)
								.getFeatureID());
			} else {
				return this.resNo.compareTo(((ClustersPerPosition) otherObject)
						.getResNo());
			}
		} else {
			return this.segmNo.compareTo(((ClustersPerPosition) otherObject)
					.getSegmNo());
		}
	}

	/**
	 * Method hashCode() overridden from class Object.<br>
	 * 
	 * @return 10*segmNo + resNo
	 */
	@Override
	public int hashCode() {
		return (10 * this.segmNo + this.resNo);
	}

	/**
	 * Method calculates Jaccard distance between the biggest clusters
	 * 
	 * @param o
	 * @return
	 */
	@Override
	public Double distanceTo(Metricable o) {
		return 1 - this.JacardDist(this.getBiggestCluster(),
				((ClustersPerPosition) o).getBiggestCluster());
	}

	private double JacardDist(Set<Integer> set1, Set<Integer> set2) {
		Set<Integer> union = new TreeSet<Integer>();
		Set<Integer> diff = new TreeSet<Integer>();
		union.addAll(set1);
		union.addAll(set2);
		diff.addAll(set1);
		diff.retainAll(set2);
		return (double) diff.size() / union.size();
	}
}
