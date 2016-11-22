package edu.ucdavis.gc.bm.survey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import edu.ucdavis.gc.bm.clustering.HierarchicalClustering;
import edu.ucdavis.gc.bm.clustering.Metricable;
import edu.ucdavis.gc.bm.properties.Props;


public class SubgroupsPerPositionFilter {
	
	private SubgroupsPerPosition sgpp ;
	
	private HashMap<String, TreeSet<Integer>> hashSGPP2DescrInd;
	
	private HashMap<Integer, String> hashSGPP2Clusters = new HashMap<Integer, String>();
	
	public SubgroupsPerPositionFilter(SubgroupsPerPosition sgpp, HashMap<String, TreeSet<Integer>> hashSGPP2DescrInd){
		this.sgpp = sgpp;
		this.hashSGPP2DescrInd = hashSGPP2DescrInd;
	}
	
	public void filter(){
		Double hcFilterCutoff = Props.getDouble("hcFilterCutoff");
		if (null == hcFilterCutoff){
			hcFilterCutoff = 0.2;
		}
		for(int i = 0 ; i < sgpp.getSubGroups().size(); i++){// loop over subgroups
			if (sgpp.getOthersInConsensus().get(i).isEmpty() || sgpp.getOthersInConsensus().get(i).size() == 1){
				continue;
			}
			List<Integer> descIndexes = new ArrayList<Integer>(); 
			descIndexes.addAll(sgpp.getSubGroups().get(i)); // descriptors' indexes  
			int iSize = descIndexes.size();
			String [] arr = new String[iSize]; // array of strings 
			for(String ShortID : sgpp.getOthersInConsensus().get(i)){
				for(int j = 0 ; j < arr.length; j++){
					if(arr[j] == null){
						arr[j] = "";
					}
					if (hashSGPP2DescrInd.get(ShortID).contains(descIndexes.get(j))){
						arr[j] = arr[j] + "1";
					} else {
						arr[j] = arr[j] + "0";
					}
				}
			}
			List<MetricString> list = new ArrayList<MetricString>();
			for(int j = 0; j < iSize; j++){
				list.add(new MetricString(arr[j]));
			}
			HierarchicalClustering hclust = new HierarchicalClustering(list);
			hclust.setFDistBetweenClusters(2);
			hclust.process(hcFilterCutoff);
			TreeSet<Integer> newDescIndexes = new TreeSet<Integer>();
			for(int l : hclust.getBiggestCluster()){
				newDescIndexes.add(descIndexes.get(l));
			}
			sgpp.getSubGroups().set(i, newDescIndexes);
			
			hashSGPP2Clusters.put(i, printClusters(descIndexes, hclust.getClusters()));
			
		}// end loop over subgroups
	}
	
	public HashMap<Integer, String> getHashSGPP2Clusters(){
		return hashSGPP2Clusters;
	}
	
	private String printClusters(List<Integer> descIndexes, List<Set<Integer>> clusters){
		StringBuilder sb = new StringBuilder();
		int countCluster = 0;
		for(Set<Integer> cluster : clusters){
			sb.append("Cluster " + countCluster + "\n");
			countCluster++;
			for(int ind : cluster){
				sb.append(descIndexes.get(ind) + " ");
			}
			sb.append("\n");
		}
		return sb.toString();
	}
	
	private class MetricString implements Metricable{

		private String str;
		
		private String getStr(){
			return str;
		}
		
		MetricString(String str){
			this.str = str;
		}
		
		/**
		 * Method calculates similarity between strings 
		 * 
		 * @param o
		 * @return
		 */
		@Override
		public Double distanceTo(Metricable o) {
			return this.JacardDist(str, (MetricString) o);
		}

		private double JacardDist(String str1, MetricString str2) {
			double result = 0.0;
			if (str1.length() != str2.getStr().length()){
				return 999.99;
			}
			for (int i = 0 ; i < str1.length(); i++) {
				// if (str1.charAt(i) != str2.getStr().charAt(i)){
				if (str1.charAt(i) != '1' || str2.getStr().charAt(i) != '1'){
					result = result + 1.0;
				}
			}
			return result/str1.length();
		}
		
	}
}
