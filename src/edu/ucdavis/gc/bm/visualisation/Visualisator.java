package edu.ucdavis.gc.bm.visualisation;



import java.io.File;
import java.util.List;
import java.util.TreeSet;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import edu.ucdavis.gc.bm.descriptorGroup.Group;
import edu.ucdavis.gc.bm.survey.ClustersPerPosition;
import edu.ucdavis.gc.bm.survey.SubgroupsPerPosition;



public class Visualisator {
	
	public void createAndShowGUI(Group group)
	{		
			JFrame frame = new JFrame("Group: " + group.getName());				
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			GroupCanvas groupCanvas = new GroupCanvas(group);
			JScrollPane scrollPane = new JScrollPane(groupCanvas);
			scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			frame.setContentPane(scrollPane);
			frame.setPreferredSize(groupCanvas.getPreferredSize());
			frame.pack();
			frame.setVisible(true);		
	}
	
	public void createAndShowGUI(Group group,  File pngFile)
	{
			new GroupCanvas(group, pngFile);
	}
	
	public void createAndShowGUI(Group group,  List<ClustersPerPosition> listClusters){
		//new GroupCanvas(group, listClusters);
		JFrame frame = new JFrame("Group: " + group.getName());				
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GroupCanvas groupCanvas = new GroupCanvas(group, listClusters);
		JScrollPane scrollPane = new JScrollPane(groupCanvas);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		frame.setContentPane(scrollPane);
		frame.setPreferredSize(groupCanvas.getPreferredSize());
		frame.pack();
		frame.setVisible(true);
	}
	
	public void createAndShowGUI(Group group,List<ClustersPerPosition> listClusters , File pngFile)
	{
			new GroupCanvas(group, listClusters, pngFile);
	}
	
	
	public void createAndShowGUI(Group group, SubgroupsPerPosition sgpp, int subGroupIndex){
		//new GroupCanvas(group, listClusters);
		JFrame frame = new JFrame("Group: " + group.getName());				
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GroupCanvasSubGroups groupCanvas = new GroupCanvasSubGroups(group, sgpp, subGroupIndex);
		JScrollPane scrollPane = new JScrollPane(groupCanvas);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		frame.setContentPane(scrollPane);
		frame.setPreferredSize(groupCanvas.getPreferredSize());
		frame.pack();
		frame.setVisible(true);
	}
	
	public void createAndShowGUI(Group group, SubgroupsPerPosition sgpp, int subGroupIndex, String pngFileName)
	{
			File pngFile = new File(pngFileName);
			new GroupCanvasSubGroups(group, sgpp, subGroupIndex, pngFile);
	}
	
	public void createAndShowGUI(Group group, double [] [] [] colorMatrix){
		//new GroupCanvas(group, listClusters);
		JFrame frame = new JFrame("Group: " + group.getName());				
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GroupCanvasSummary groupCanvas = new GroupCanvasSummary(group,colorMatrix);
		JScrollPane scrollPane = new JScrollPane(groupCanvas);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		frame.setContentPane(scrollPane);
		frame.setPreferredSize(groupCanvas.getPreferredSize());
		frame.pack();
		frame.setVisible(true);
	}
	
	public void createAndShowGUI(Group group, double [] [] [] colorMatrix, String pngFileName)
	{
			File pngFile = new File(pngFileName);
			new GroupCanvasSummary(group, colorMatrix, pngFile);
	}
	
	public void createAndShowGUI(Group group, List<TreeSet<Integer>> listCF, boolean dummVar){
		//new GroupCanvas(group, listClusters);
		JFrame frame = new JFrame("Group: " + group.getName());				
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GroupCanvasListCF groupCanvas = new GroupCanvasListCF(group,listCF);
		JScrollPane scrollPane = new JScrollPane(groupCanvas);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		frame.setContentPane(scrollPane);
		frame.setPreferredSize(groupCanvas.getPreferredSize());
		frame.pack();
		frame.setVisible(true);
	}
	
	public void createAndShowGUI(Group group, List<TreeSet<Integer>> listCF,  String pngFileName, boolean dummVar)
	{
			File pngFile = new File(pngFileName);
			new GroupCanvasListCF(group, listCF, pngFile);
	}
	
	
	public void createAndShowGUI(Group group, List<SubgroupsPerPosition> listSGPP, String featureID){
		
		JFrame frame = new JFrame("Group: " + group.getName());				
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GroupCanvasSubGroupPerPosition groupCanvas = new GroupCanvasSubGroupPerPosition(group, listSGPP, featureID);
		JScrollPane scrollPane = new JScrollPane(groupCanvas);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		frame.setContentPane(scrollPane);
		frame.setPreferredSize(groupCanvas.getPreferredSize());
		frame.pack();
		frame.setVisible(true);
	}
	
	public void createAndShowGUI(Group group, List<SubgroupsPerPosition> listSGPP, String featureID, File pngFile)
	{
			new GroupCanvasSubGroupPerPosition(group, listSGPP, featureID, pngFile);
	}
}
