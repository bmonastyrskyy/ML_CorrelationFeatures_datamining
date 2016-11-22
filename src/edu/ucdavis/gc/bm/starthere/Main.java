package edu.ucdavis.gc.bm.starthere;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import edu.ucdavis.gc.aa.utils.AAUtils;
import edu.ucdavis.gc.bm.clustering.HierarchicalClustering;
import edu.ucdavis.gc.bm.descriptorGroup.Descriptor;
import edu.ucdavis.gc.bm.descriptorGroup.Group;
import edu.ucdavis.gc.bm.descriptorGroup.ParseD2KGroups;
import edu.ucdavis.gc.bm.descriptorGroup.ParseD3KGroups;
import edu.ucdavis.gc.bm.descriptorGroup.ParseGroups;
import edu.ucdavis.gc.bm.descriptorGroup.Segment;
import edu.ucdavis.gc.bm.properties.Props;
import edu.ucdavis.gc.bm.survey.ClustersPerPosition;
import edu.ucdavis.gc.bm.survey.CompoundBestPerPositionFeature;
import edu.ucdavis.gc.bm.survey.CompoundFeature;
import edu.ucdavis.gc.bm.survey.ProfileFeature;
import edu.ucdavis.gc.bm.survey.SubgroupsPerPosition;
import edu.ucdavis.gc.bm.survey.SubgroupsPerPositionFilter;
import edu.ucdavis.gc.bm.survey.SubgroupsPerPositionPair;
import edu.ucdavis.gc.bm.visualisation.SummaryMatrix;
import edu.ucdavis.gc.bm.visualisation.Visualisator;

public class Main {

	/**
	 * HashMap of mapping residues addresses to fastaNo's for all astral domains
	 */
	public static HashMap<String, HashMap<String, Integer>> hashResMapStr2Int;
	/**
	 * HashMap of mapping fastaNo's to residues' addresses for all astral
	 * domains
	 */
	public static HashMap<String, HashMap<Integer, String>> hashResMapInt2Str;

	public static HashMap<String, String> hashFold;

	/**
	 * hash contains fasta sequences for all astral domains <br>
	 * hash with key - domain; value - fasta sequence
	 */
	public static HashMap<String, String> fastaSeqs;

	/**
	 * hash contains ss sequences for all astral domains <br>
	 * key - domain; value - ss sequence
	 */
	public static HashMap<String, String> SS_Seqs;

	public static TreeSet<String> problemDomains;

	private static String groupsSource;
	/**
	 * flag to switch between algorithms:<br>
	 * 1 - clustering algorithm<br>
	 * 2 - subgrouping algorithm<br>
	 */
	private static int method = 2;

	/**
	 * flag whether random sequences should be generated
	 */
	private static boolean randomSeq = false;
	/**
	 * flag indicating if use background probabilities from the group
	 */
	private static boolean randomSeqSelf = false;
	/**
	 * flag indicating whether to serialize compound features
	 */
	private static boolean fSerialize = false;

	public static void main(String[] args) throws FileNotFoundException {

		problemDomains = new TreeSet<String>();
		problemDomains.add("1gw5a_");
		problemDomains.add("1gw5b_");
		problemDomains.add("1uc8a2");
		problemDomains.add("1pya.1");
		problemDomains.add("2fpwa1");

		groupsSource = Props.get("groupsSource");

		method = Props.getInt("method");

		randomSeq = Props.getBool("randomSeq");

		fSerialize = Props.getBool("fSerialize");

		// read all ser-objects
		FileInputStream fis = null;
		ObjectInputStream in = null;
		String astralResMap_ser_filepath = Props
				.get("hashResMapStr2Int_ser_path");
		try {
			fis = new FileInputStream(astralResMap_ser_filepath);
			in = new ObjectInputStream(fis);
			hashResMapStr2Int = (HashMap<String, HashMap<String, Integer>>) in
					.readObject();
			in.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}

		astralResMap_ser_filepath = Props.get("hashResMapInt2Str_ser_path");
		try {
			fis = new FileInputStream(astralResMap_ser_filepath);
			in = new ObjectInputStream(fis);
			hashResMapInt2Str = (HashMap<String, HashMap<Integer, String>>) in
					.readObject();
			in.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}

		fis = null;
		in = null;
		String astralFastaSeq_ser_filepath = Props.get("fastaSeq_ser_path");
		try {
			fis = new FileInputStream(astralFastaSeq_ser_filepath);
			in = new ObjectInputStream(fis);
			fastaSeqs = (HashMap<String, String>) in.readObject();
			in.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}

		fis = null;
		in = null;
		String astralSSSeq_ser_filepath = Props.get("SS_Seq_ser_path");
		try {
			fis = new FileInputStream(astralSSSeq_ser_filepath);
			in = new ObjectInputStream(fis);
			SS_Seqs = (HashMap<String, String>) in.readObject();
			in.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}

		fis = null;
		in = null;
		String hashFold_filepath = Props.get("hashFold_path");
		try {
			fis = new FileInputStream(hashFold_filepath);
			in = new ObjectInputStream(fis);
			hashFold = (HashMap<String, String>) in.readObject();
			in.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}

		// end reading objects

		//new AAUtils();

		ParseGroups parser;
		boolean isD3K = Props.getBool("isD3K", true);

		if (isD3K) {
			parser = new ParseD3KGroups(groupsSource, hashResMapStr2Int,
					hashResMapInt2Str, fastaSeqs, SS_Seqs, problemDomains);
		} else {
			parser = new ParseD2KGroups(groupsSource, hashResMapStr2Int,
					hashResMapInt2Str, fastaSeqs, SS_Seqs, hashFold);
		}
		List<Group> groups = null;
		try {
			groups = parser.parse();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (randomSeq) {
			for (Group group : groups) {
				if (randomSeqSelf) {
					HashMap<Character, Double> probs = group
							.getBackGroundProbs();
					for (Descriptor desc : group.getDescriptors()) {
						for (Segment segm : desc.getSegments()) {
							String randSeq = AAUtils
									.getInstance()
									.genRandomSeq(segm.getSeq().length(), probs);
							segm.setSeq(randSeq);
						}
					}
				} else {
					for (Descriptor desc : group.getDescriptors()) {
						for (Segment segm : desc.getSegments()) {
							String randSeq = AAUtils.getInstance()
									.genRandomSeq(segm.getSeq().length());
							segm.setSeq(randSeq);
						}
					}
				}
			}
		}

		String[] features = { "aaprofile", "hydrophobicity", "volume"
		// ,"betaBranch", "electricCharge", "polarity"
		};

		File outDir = new File(Props.get("outDir"));
		if (!outDir.exists()) {
			outDir.mkdirs();
		} else {
			System.err.println("Directory " + Props.get("outDir") + " exists.");
			// System.exit(1);
		}

		if (method == 2) { // subgrouping
			boolean header = true;
			String outFile2 = Props.get("outDir") + "/" + "Stat."
					+ Props.get("set") + ".short.csv.txt";
			PrintWriter pw2 = null;

			String outFile3 = Props.get("outDir") + "/" + "Stat."
					+ Props.get("set") + ".perPosition.csv.txt";
			PrintWriter pw3 = null;

			try {
				pw2 = new PrintWriter(new File(outFile2));
				pw3 = new PrintWriter(new File(outFile3));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			}
			for (Group group : groups) { // loop over groups
				List<SubgroupsPerPosition> listSubGroups = new ArrayList<SubgroupsPerPosition>();
				pw2.println("\n\n" + group.getName() + " " + group.getNameD2K());
				for (String featureType : features) { // loop over features
					List<Double> cutoffs = new ArrayList<Double>();
					if (featureType.matches("(?i:aaprofile.*)")) {
						// cutoffs.add(2.0);
						// cutoffs.add(1.0);
						// cutoffs.add(0.0);
						// cutoffs.add(-1.0);
						cutoffs.add(Props.getDouble("aaprofile_cutoff"));
					} else if (featureType.matches("(?i:hydrophobic.*)")) {
						cutoffs.add(Props.getDouble("hydrophobic_cutoff"));
						// cutoffs.add(0.25);
						// cutoffs.add(0.5);
						// cutoffs.add(0.75);
						// cutoffs.add(1.0);
					} else if (featureType.matches("(?i:volume.*)")) {
						cutoffs.add(Props.getDouble("volume_cutoff"));
						// cutoffs.add(10.0);
						// cutoffs.add(20.0);
						// cutoffs.add(30.0);
						// cutoffs.add(40.0);
						// cutoffs.add(50.0);
					} else { // polarity, betabranch, electric charge
						cutoffs.add(0.5);
					}
					for (double cutoff : cutoffs) {
						for (int segmNo = 0; segmNo < group.getRootDescriptor()
								.getNumberSegments(); segmNo++) { // loop over
																	// segments
							for (int resNo = 0; resNo < group
									.getRootDescriptor().getSegments()
									.get(segmNo).getSeq().length(); resNo++) { // loop
																				// over
																				// residues
								SubgroupsPerPosition sgpp = new SubgroupsPerPosition(
										group, segmNo, resNo, featureType,
										cutoff);
								pw3.print(sgpp.toString(header, false));
								header = false;
								listSubGroups.add(sgpp);
							} // end loop over residues
						} // end loop over segments
					}
					boolean visualFlag = Props.getBool("visualFlag", false);
					if (visualFlag) {
						Visualisator vis = new Visualisator();
						String pngFile = Props.get("outDir") + "/"
								+ group.getName() + ".";
						if (featureType.equalsIgnoreCase("hydrophobicity")) {
							pngFile += "H";
						} else if (featureType.equalsIgnoreCase("aaprofile")) {
							pngFile += "A";
						} else if (featureType.equalsIgnoreCase("volume")) {
							pngFile += "V";
						} else if (featureType.equalsIgnoreCase("betaBranch")) {
							pngFile += "B";
						} else if (featureType
								.equalsIgnoreCase("electricCharge")) {
							pngFile += "E";
						} else if (featureType.equalsIgnoreCase("polarity")) {
							pngFile += "P";
						}
						pngFile += ".perPosition.png";
						vis.createAndShowGUI(group, listSubGroups, featureType,
								new File(pngFile));
					}
					
				} // end loop over features

				
				if (fSerialize){
					ProfileFeature pf1 = new ProfileFeature();
					pf1.calcWeights(group, listSubGroups, "A");
					String fileName1 = outDir + "/"
							+ group.getName() + "_" + pf1.getFeatureName() + ".ML_features" + ".java.ser";
					FileOutputStream fos1 = null;
					ObjectOutputStream out1 = null;
					try {
						fos1 = new FileOutputStream(fileName1);
						out1 = new ObjectOutputStream(fos1);
						out1.writeObject(pf1);
						out1.close();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
					CompoundBestPerPositionFeature cbppf = new CompoundBestPerPositionFeature();
					cbppf.calcWeights(group, listSubGroups, "A");
					String fileName2 = outDir + "/"
							+ group.getName() + "_" + cbppf.getFeatureName() + ".ML_features" + ".java.ser";
					FileOutputStream fos2 = null;
					ObjectOutputStream out2 = null;
					try {
						fos2 = new FileOutputStream(fileName2);
						out2 = new ObjectOutputStream(fos2);
						out2.writeObject(cbppf);
						out2.close();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
				// create hashmap of subgroups shortID's and corresponding descriptor indexes 
				HashMap<String, TreeSet<Integer>> hashSGPP2DescrInd = new HashMap<String, TreeSet<Integer>>();
				for(int i = 0; i < listSubGroups.size(); i++) {
					for(int k = 0; k < listSubGroups.get(i).getSubGroups().size(); k++){
						TreeSet<Integer> tmp = new TreeSet<Integer>();
						tmp.addAll(listSubGroups.get(i).getSubGroups().get(k));
						hashSGPP2DescrInd.put(listSubGroups.get(i).getShortID(k),tmp);
					}
				}
				//if (true) continue;
				// generating complex Features : add similar (in consensus) to
				// those over other positions
				for (int i = 0; i < listSubGroups.size(); i++) {
					if (listSubGroups.get(i).getBiggestSubGroup().isEmpty()) {
						continue;
					}
					for (int j = 0; j < listSubGroups.size(); j++) {
						if (i == j) {
							continue;
						}
						SubgroupsPerPositionPair pair = new SubgroupsPerPositionPair(
								listSubGroups.get(i), listSubGroups.get(j));
						pair.findOthersInConsensus();
						pair = null;
					}
				}
				// pw2.print(pair.toString(ifFirst));
				// pw2.print(pair.toStringShort(ifFirst));
				// pw2.flush();
				// remove redundant subgroup indexes
				for (int i = 0; i < listSubGroups.size() - 1; i++) {
					if (listSubGroups.get(i).getBiggestSubGroup().isEmpty()) {
						continue;
					}
					for (int j = i; j < listSubGroups.size(); j++) {
						if (i == j) {
							continue;
						}
						if (listSubGroups.get(j).getBiggestSubGroup().isEmpty()) {
							continue;
						}
						SubgroupsPerPositionPair pair = new SubgroupsPerPositionPair(
								listSubGroups.get(i), listSubGroups.get(j));
						pair.reduceRedundancyOthersInConsensus();
						pair = null;
					}
				}
				// print out
				// additional restrictions
				int noSegmentsCutoff = Props.getInt("noSegmentsCutoff", 2); // cutoff
																			// for
																			// number
																			// of
																			// segments
																			// involved
																			// in
																			// complex
																			// feature
				int noResiduesCutoff = Props.getInt("noResiduesCutoff", 5); // cutoff
																			// for
																			// number
																			// of
																			// residues
																			// involved
																			// in
																			// complex
																			// feature
				int noEntriesProximityCutoff = Props.getInt(
						"noEntriesProximityCutoff", 7); // cutoff for number
														// proximity features
				boolean visualFlag = Props.getBool("visualFlag", false);

				for (int i = 0; i < listSubGroups.size(); i++) {

					SubgroupsPerPosition sgpp = listSubGroups.get(i);
					if (sgpp.getBiggestSubGroup().isEmpty()) {
						continue;
					}

					for (int k = 0; k < listSubGroups.get(i)
							.getOthersInConsensus().size(); k++) {
						if (sgpp.getOthersInConsensus().get(k).isEmpty()
								|| sgpp.getOthersInConsensus().get(k).size() == 1) {
							continue;
						} else {
							if (!sgpp.passedFilters(k, noSegmentsCutoff,
									noResiduesCutoff, noEntriesProximityCutoff)) {
								continue;
							} else {
								boolean fPostFilter = Props.getBool("fPostFilter", false);
								if (fPostFilter){
									SubgroupsPerPositionFilter sgppf = new SubgroupsPerPositionFilter(sgpp, hashSGPP2DescrInd);
									sgppf.filter();
									if (!sgpp.hasEnoughFolds(sgpp.getSubGroups().get(k), Props.getInt("minNoFoldsPerSubGroup", 7))) {
										continue;
									}
									PrintWriter pw4 = new PrintWriter(Props.get("outDir") + "/"
											+ group.getName() + "_" + i + "_"
											+ k + ".clusters.txt");
									pw4.print(sgppf.getHashSGPP2Clusters().get(k));
									pw4.close();
								}
								if (visualFlag) {
									String pngFile = Props.get("outDir") + "/"
											+ group.getName() + "_" + i + "_"
											+ k + ".png";

									Visualisator vis = new Visualisator();
									vis.createAndShowGUI(group, sgpp, k,
											pngFile);
									System.out.println(pngFile);
								}
								StringBuilder sb = new StringBuilder("");
								sb.append("{" + sgpp.getShortID(k) + "}|");
								for (String str : sgpp.getOthersInConsensus()
										.get(k)) {
									sb.append("{" + str + "}");
								}
								pw2.println(sb);
								pw2.flush();
								CompoundFeature cf = new CompoundFeature();
								cf.setCFPPlist(group, sgpp.getShortID(k), sgpp
										.getOthersInConsensus().get(k), sgpp
										.getSubGroups().get(k));
								if (fSerialize) {
									String fileName1 = outDir + "/"
											+ group.getName() + "_" + i + "_"
											+ k + ".ML_features" + ".java.ser";
									FileOutputStream fos1 = null;
									ObjectOutputStream out1 = null;
									try {
										fos1 = new FileOutputStream(fileName1);
										out1 = new ObjectOutputStream(fos1);
										out1.writeObject(cf);
										out1.close();
									} catch (IOException ex) {
										ex.printStackTrace();
									}
								}
							}
						}
					}
				}
				SummaryMatrix sMatr = new SummaryMatrix(group, listSubGroups);
				// output matrix of compound features
				PrintWriter pw5 = new PrintWriter(Props.get("outDir") + "/"
						+ group.getName() +  ".matrix.txt");
				pw5.print(sMatr.listCFToString(group));
				pw5.close();
				if (visualFlag) {
					String pngFile = Props.get("outDir") + "/"
							+ group.getName() + ".Summary.png";

					Visualisator vis = new Visualisator();
					vis.createAndShowGUI(group, sMatr.getMatrix(), pngFile);
					vis = new Visualisator();
					pngFile = Props.get("outDir") + "/" + group.getName()
							+ ".CF.png";
					vis.createAndShowGUI(group, sMatr.getListCF(), pngFile,
							true);
				}

			} // end loop over groups
			pw2.close();
			pw3.close();

		} // end of subgrouping algorithm

		if (method == 1) { // clustering
			String statFile = Props.get("outDir") + "/StatGroups.txt";
			PrintWriter pw = null;
			try {
				pw = new PrintWriter(new File(statFile));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (Group group : groups) { // loop over groups
				pw.println(group.getName() + ": " + group.getSummaryStat());
				pw.flush();
				System.out.println("Group: " + group.getName());
				for (String featureID : features) { // loop over features
					List<ClustersPerPosition> listClusters = new ArrayList<ClustersPerPosition>();
					for (int segmNo = 0; segmNo < group.getNumberSegments(); segmNo++) { // loop
																							// over
																							// segments
						for (int resNo = 0; resNo < group.getRootDescriptor()
								.getSeqs().get(segmNo).length(); resNo++) { // loop
																			// over
																			// residue
																			// positions
							ClustersPerPosition cl = new ClustersPerPosition(
									group, segmNo, resNo, featureID);
							listClusters.add(cl);
							// System.out.println(cl.getBiggestCluster());
						}
					}

					// perform clustering over clusters per positions
					HierarchicalClustering hclust = new HierarchicalClustering(
							listClusters);
					hclust.setFDistBetweenClusters(2);
					hclust.process(0.5);
					// System.out.println(hclust.getClusters());
					// Scanner input = new Scanner(System.in);
					int count = 1;
					for (Set<Integer> cl_index : hclust.getClusters()) {
						// System.out.println("\nCluster" + count);
						List<ClustersPerPosition> listClustersForVis = new ArrayList<ClustersPerPosition>();
						for (int index : cl_index) {
							System.out.println(listClusters.get(index)
									.getFeatureID()
									+ " "
									+ listClusters.get(index).getSegmNo()
									+ " "
									+ listClusters.get(index).getResNo()
									+ " "
									+ listClusters.get(index)
											.getStatSummaryBiggestCluster());
							listClustersForVis.add(listClusters.get(index));
						}
						if (cl_index.size() >= 3) {
							String pngFile = Props.get("outDir") + "/"
									+ group.getName() + "_" + (count++) + "_"
									+ featureID + ".png";
							Visualisator vis = new Visualisator();
							vis.createAndShowGUI(group, listClustersForVis,
									new File(pngFile));
						}
						System.out.println("--------");
					}
				} // loop over features
				System.out.println("============");
			} // loop over groups
			pw.close();
		} // end of clustering algorithm
	}
}
