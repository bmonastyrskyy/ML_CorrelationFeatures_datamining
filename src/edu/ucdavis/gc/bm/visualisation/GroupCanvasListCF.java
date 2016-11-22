package edu.ucdavis.gc.bm.visualisation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import edu.ucdavis.gc.bm.descriptorGroup.Group;

public class GroupCanvasListCF extends JPanel {

	private Group group;

	// private TreeSet<Integer> cluster;

	//private SubgroupsPerPosition sgpp; //List<ClustersPerPosition> listClusters;

	private Integer edgeXSize = 16;

	private Integer fontSize = edgeXSize;

	private int edgeYSize = edgeXSize;

	private int width = 0;

	private int widthLine = 0;

	private int height = 0;

	private List<TreeSet<Integer>> listCF;

	
	/**
	 * object used to write the image to file
	 */
	private BufferedImage image = null;

	private int noHeaderLines = 2;

	public GroupCanvasListCF (Group group, List<TreeSet<Integer>> listCF) {
		this.group = group;
		this.listCF = listCF;
		setDimensions();
	}

	public GroupCanvasListCF(Group group, List<TreeSet<Integer>> listCF,
			File imageFile) {
		this.group = group;
		this.listCF = listCF;
		setDimensions();
		image = new BufferedImage(widthLine, height,
				BufferedImage.TYPE_INT_ARGB);
		this.paintImage(imageFile);
	}

	private void setDimensions() {
		height = (noHeaderLines + group.getNumberMembers()) * edgeXSize;
		width = listCF.size(); 
		width *= edgeYSize;
		widthLine = width + 10 * edgeXSize;
		this.setPreferredSize(new Dimension(widthLine, height));
	}

	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		super.paintComponent(g2);
		this.drawHeader(g2);
		this.drawCFs(g2);
		for (int descrIndex = 0; descrIndex < group.getNumberMembers(); descrIndex++) {
			drawDescrLine(descrIndex, g2);
		}
		this.drawGrid(g2);
	}

	private void drawDescrLine(int descrIndex, Graphics2D g) {
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

	private void drawCFs(Graphics2D g){
		for (int indexCF = 0; indexCF < listCF.size(); indexCF++){
			int leftTopX = (indexCF)*edgeXSize;
			for (int indexDescr : listCF.get(indexCF)) {
				int leftTopY = (indexDescr + noHeaderLines) * edgeYSize;
				g.setColor(Color.GRAY);
				g.fillRect(leftTopX, leftTopY, edgeXSize, edgeYSize);
			}
		}
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


	private void paintImage(File imageFile) {
		Graphics2D g2 = image.createGraphics();
		super.paintComponent(g2);
		g2.setColor(Color.WHITE);
		g2.fill(new Rectangle2D.Double((double) 0, (double) 0, widthLine,
				height));
		this.drawHeader(g2);
		this.drawCFs(g2);
		for (int descrIndex = 0; descrIndex < group.getNumberMembers(); descrIndex++) {
			drawDescrLine(descrIndex, g2);
		}
		this.drawGrid(g2);
		g2.drawImage(image, null, 0, 0);

		try {
			ImageIO.write(image, "png", imageFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
