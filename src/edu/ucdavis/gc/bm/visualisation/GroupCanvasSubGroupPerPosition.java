package edu.ucdavis.gc.bm.visualisation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import edu.ucdavis.gc.aa.utils.AAUtils;
import edu.ucdavis.gc.bm.descriptorGroup.Group;
import edu.ucdavis.gc.bm.survey.SubgroupsPerPosition;

public class GroupCanvasSubGroupPerPosition extends JPanel {

	private Group group;

	// private TreeSet<Integer> cluster;

	private List<SubgroupsPerPosition> listSGPP; //List<ClustersPerPosition> listClusters;

	private Integer edgeXSize = 16;

	private Integer fontSize = edgeXSize;

	private int edgeYSize = edgeXSize;

	private int width = 0;

	private int widthLine = 0;

	private int height = 0;

	private double minColor = -2.0;

	private double maxColor = 2.0;

	private static String flag = "aaProfile"; 

	private int subGroupIndex = 0;
	
	/**
	 * object used to write the image to file
	 */
	private BufferedImage image = null;

	private int noHeaderLines = 2;

	/*
	public GroupCanvasSubGroups(Group group) {
		this.group = group;
		setDimensions();
	}
	*/
	
	public GroupCanvasSubGroupPerPosition (Group group, List<SubgroupsPerPosition> listSGPP, String featureID) {
		this.group = group;
		this.listSGPP = listSGPP;
		flag = featureID;
		setDimensions();
	}
	/*
	public GroupCanvasSubGroups(Group group, File imageFile) {
		this.group = group;
		setDimensions();
		image = new BufferedImage(widthLine, height,
				BufferedImage.TYPE_INT_ARGB);
		this.paintImage(imageFile);
	}
	*/
	public GroupCanvasSubGroupPerPosition (Group group, List<SubgroupsPerPosition> listSGPP , String featureID,
			File imageFile) {
		this.group = group;
		this.listSGPP = listSGPP;
		this.flag = featureID;
		setDimensions();
		image = new BufferedImage(widthLine, height,
				BufferedImage.TYPE_INT_ARGB);
		this.paintImage(imageFile);
	}

	private void setDimensions() {
		height = (noHeaderLines + group.getNumberMembers()) * edgeXSize;
		width = 2 * (group.getNumberSegments() - 1);
		width += group.getRootDescriptor().getNumberResidues();
		width *= edgeYSize;
		widthLine = width + 10 * edgeXSize;
		this.setPreferredSize(new Dimension(widthLine, height));
	}

	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		super.paintComponent(g2);
		this.drawHeader(g2);

		for (int descrIndex = 0; descrIndex < group.getNumberMembers(); descrIndex++) {
			drawDescrLine(descrIndex, g2);
		}
		this.drawGrid(g2);
		this.drawSelectedGrid(g2);
	}

	private void drawDescrLine(int descrIndex, Graphics2D g) {
		for (int segmIndex = 0; segmIndex < group.getDescriptors()
				.get(descrIndex).getSegments().size(); segmIndex++) {
			drawSegment(descrIndex, segmIndex, g);
		}
		int leftTopY = (this.noHeaderLines + descrIndex) * this.edgeYSize;
		Font f = new Font("Arrial", Font.BOLD, fontSize - 2);
		g.setFont(f);
		g.setColor(Color.BLACK);
		try {
			g.drawString(group.getDescriptors().get(descrIndex).getFoldAstral()
					.toString(), width + (int) (0.3 * this.edgeXSize), leftTopY
					+ (int) (0.9 * this.edgeYSize));
		} catch (NullPointerException e) {

		}
	}

	private void drawSegment(int descrIndex, int segmIndex, Graphics2D g) {
		for (int resIndex = 0; resIndex < group.getDescriptors()
				.get(descrIndex).getSeqs().get(segmIndex).length(); resIndex++) {
			Character res;
			if (flag.startsWith("ssTransition")) {
				try {
					res = group.getDescriptors().get(descrIndex).getSegments()
							.get(segmIndex).getSSSeq().toUpperCase()
							.charAt(resIndex);
				} catch (StringIndexOutOfBoundsException e) {
					res = '.';
				}
			} else {
				res = group.getDescriptors().get(descrIndex).getSeqs()
						.get(segmIndex).toUpperCase().charAt(resIndex);
			}

			this.drawRes(descrIndex, segmIndex, resIndex, res, g);
		}
	}

	private void drawRes(int descrIndex, int segmIndex, int resIndex,
			Character res, Graphics2D g) {

		int leftTopX = 0;
		for (int i = 0; i < segmIndex; i++) {
			leftTopX += group.getRootDescriptor().getSeqs().get(i).length() + 2;
		}
		leftTopX += resIndex;
		leftTopX *= this.edgeXSize;
		int leftTopY = (this.noHeaderLines + descrIndex) * this.edgeYSize;
		g.setColor(calcColor(res));
		g.fillRect(leftTopX, leftTopY, edgeXSize, edgeYSize);
		g.setColor(Color.WHITE);
		Font f = new Font("Arrial", Font.BOLD, fontSize - 2);
		g.setFont(f);
		g.drawString(res.toString(), leftTopX + (int) (0.3 * this.edgeXSize),
				leftTopY + (int) (0.9 * this.edgeYSize));
	}

	private void drawHeader(Graphics2D g) {
		g.setColor(Color.BLACK);
		Font f = new Font("Arrial", Font.BOLD, fontSize - 2);
		g.setFont(f);

		g.drawString("Group: " + group.getName(), (int) (0.3 * this.edgeXSize),
				(int) (0.9 * this.edgeYSize));
	}

	private void drawGrid(Graphics2D g) {
		int xStart = 0;
		int yStart = this.noHeaderLines * this.fontSize;
		int Height = this.height - yStart;
		int Width = this.width;
		int height = this.edgeYSize;
		int width = this.edgeXSize;
		g.setColor(Color.GRAY);
		while (width <= Width) {
			g.drawRect(xStart, yStart, width, Height);
			width += this.edgeXSize;
		}
		while (height <= Height) {
			g.drawRect(xStart, yStart, Width, height);
			height += this.edgeYSize;
		}
	}

	private void drawSelectedGrid(Graphics2D g) {
		//select first subgroup
		for (SubgroupsPerPosition sgpp : listSGPP){
			String shortID = sgpp.getShortID();
			String [] tokens = shortID.split(",");
			int segmNo = Integer.valueOf(tokens[0]);
			int resNo = Integer.valueOf(tokens[1]);
			String shortFeatureID = tokens[2];
			if ((shortFeatureID.equalsIgnoreCase("A") && flag.equalsIgnoreCase("aaProfile"))
					|| (shortFeatureID.equalsIgnoreCase("V") && flag.equalsIgnoreCase("volume"))
					|| (shortFeatureID.equalsIgnoreCase("H") && flag.equalsIgnoreCase("hydrophobicity"))
					|| (shortFeatureID.equalsIgnoreCase("P") && flag.equalsIgnoreCase("polarity"))
					|| (shortFeatureID.equalsIgnoreCase("E") && flag.equalsIgnoreCase("electriccharge"))
					|| (shortFeatureID.equalsIgnoreCase("B") && flag.equalsIgnoreCase("betabranch"))
					){
				
			} else {
				continue;
			}
			for (int descrindex : sgpp.getBiggestSubGroup()){
				drawSelelectedRes(g, segmNo, resNo, descrindex);
			}
		}
		
	}
/*	
	private void drawSelelectedResHeader(Graphics2D g, int segmIndex, int resIndex){
		int leftTopX = 0;
		for (int i = 0; i < segmIndex; i++) {
			leftTopX += group.getRootDescriptor().getSeqs().get(i).length() + 2;
		}
		leftTopX += resIndex;
		leftTopX *= this.edgeXSize;
		int leftTopY = (this.noHeaderLines - 6) * this.edgeYSize;
		Character res = 'A';
		if (flag.equalsIgnoreCase("aaProfile")){
			leftTopY = (this.noHeaderLines  - 6) * this.edgeYSize;
			res = 'A';
		} else if (flag.equalsIgnoreCase("hydrophobicity")){
			leftTopY = (this.noHeaderLines  - 5) * this.edgeYSize;
			res = 'H';
		} else if (flag.equalsIgnoreCase("volume")){
			leftTopY = (this.noHeaderLines  - 4) * this.edgeYSize;
			res = 'V';
		} else if (flag.equalsIgnoreCase("polarity")){
			leftTopY = (this.noHeaderLines  - 3) * this.edgeYSize;
			res = 'P';
		} else if (flag.equalsIgnoreCase("electriccharge")){
			leftTopY = (this.noHeaderLines  - 2) * this.edgeYSize;
			res = 'E';
		} else if (flag.equalsIgnoreCase("betabranch")){
			leftTopY = (this.noHeaderLines  - 1) * this.edgeYSize;
			res = 'B';
		}
		g.setColor(Color.WHITE);		
		g.fillRect(leftTopX, leftTopY, edgeXSize, edgeYSize);
		g.setColor(Color.GRAY);
		Font f = new Font("Arrial", Font.BOLD, fontSize - 2);
		g.setFont(f);
		g.drawString(res.toString(), leftTopX + (int) (0.3 * this.edgeXSize),
				leftTopY + (int) (0.9 * this.edgeYSize));
		
		String shortID = this.listSGPP.getShortID(this.subGroupIndex);
		String [] tokens = shortID.split(",");
		int segmNo = Integer.valueOf(tokens[0]);
		int resNo = Integer.valueOf(tokens[1]);
		String shortFeatureID = tokens[2];
		if (segmNo == segmIndex && resNo == resIndex && res == shortFeatureID.charAt(0)){
			g.drawRect(leftTopX, leftTopY, edgeXSize, edgeYSize);
		}
	}
*/
	private void drawSelelectedRes(Graphics2D g,   int segmNo, int resNo,
			int descIndex) {
		Character res = group.getDescriptors().get(descIndex).getSeqs()
				.get(segmNo).toUpperCase().charAt(resNo);
		this.drawRes(descIndex, segmNo, resNo, res, g);
		int xStart = 0;
		for (int i = 0; i < segmNo; i++) {
			xStart += (group.getRootDescriptor().getSegments().get(i).getSeq()
					.length() + 2)
					* this.edgeXSize;
		}
		xStart +=  resNo * this.edgeXSize;
		int yStart = (this.noHeaderLines + descIndex) * this.edgeYSize;
		double thickness = 2;
		Stroke oldStroke = g.getStroke();
		g.setStroke(new BasicStroke((float) thickness));
		g.setColor(Color.RED);
		g.drawRect(xStart, yStart, this.edgeXSize, this.edgeYSize);
		g.setStroke(oldStroke);
	}

	private Color calcColor(Character res) {
		Double score = null;
		Color result = null;
		if (flag.equalsIgnoreCase("hydrophobicity") || flag.equalsIgnoreCase("aaProfile")) {
			if (res.equals('.') || res.equals('X')) {
				return Color.white;
			} else {
				score = AAUtils.hydroPhobicScore.get(res);
			}
			int rgb = Color.HSBtoRGB(0.5f, 0.7f,
					(float) ((score - maxColor) / (minColor - maxColor)));
			result = new Color(rgb);
			return result;
		}
		if (flag.equalsIgnoreCase("volume")) {
			if (res.equals('.') || res.equals('X')) {
				return Color.white;
			} else {
				score = AAUtils.volume.get(res);
			}
			int rgb = Color.HSBtoRGB(0.5f, 0.7f,
					(float) ((score - 250.0) / (50.0 - 250.0)));
			result = new Color(rgb);
			return result;
		}
		if (flag.equalsIgnoreCase("electriccharge")
				|| flag.equalsIgnoreCase("electriccharge+")
				|| flag.equalsIgnoreCase("electriccharge-")) {
			if (res.equals('.') || res.equals('X')) {
				return Color.white;
			} else {
				score = AAUtils.electricCharge.get(res);
			}
			if (score == 0.0) {
				return Color.gray;
			}
			if (score == 1.0) {
				return Color.red;
			}
			if (score == -1.0) {
				return Color.blue;
			}
		}
		if (flag.equalsIgnoreCase("polarity")) {
			if (res.equals('.') || res.equals('X')) {
				return Color.white;
			} else {
				score = AAUtils.polarity.get(res);
			}
			if (score == 0.0) {
				return Color.gray;
			}
			if (score == 1.0) {
				return Color.magenta;
			}
		}
		if (flag.equalsIgnoreCase("betabranch")) {
			if (res.equals('.') || res.equals('X')) {
				return Color.white;
			} else {
				score = AAUtils.betaBranchness.get(res);
			}
			if (score == 0.0) {
				return Color.gray;
			}
			if (score == 1.0) {
				return Color.green;
			}
		}
		if (flag.equalsIgnoreCase("aaProfile")) {
			if (res.equals('.') || res.equals('X')) {
				return Color.white;
			} else {
				return Color.gray;
			}
		}
		if (flag.startsWith("ssTransition")) {
			if (res.equals('.') || res.equals('X')) {
				return Color.white;
			} else {
				return Color.gray;
			}
		}
		return result;
	}

	private void paintImage(File imageFile) {
		Graphics2D g2 = image.createGraphics();
		super.paintComponent(g2);
		g2.setColor(Color.WHITE);
		g2.fill(new Rectangle2D.Double((double) 0, (double) 0, widthLine,
				height));
		this.drawHeader(g2);

		for (int descrIndex = 0; descrIndex < group.getNumberMembers(); descrIndex++) {
			drawDescrLine(descrIndex, g2);
		}
		this.drawGrid(g2);
		this.drawSelectedGrid(g2);
		g2.drawImage(image, null, 0, 0);

		try {
			ImageIO.write(image, "png", imageFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
