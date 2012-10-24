package graphisme;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import codage.Codificateur;

public class Graphisme extends JFrame {

	private static final long serialVersionUID = 1L;

	private GridBagLayout layout;
	private GridBagConstraints constraints;

	private final int tailleGraphe = 800;

	private Codificateur cod;
	private int r;

	private JTextField rField;
	private MatriceParite matriceParite;
	private Tanner tanner;

	public Graphisme(Codificateur c) {
		super("Réalisation du codage");
		createLayout();

		this.cod = c;

		JPanel main = new JPanel(layout);

		rField = new JTextField(2);
		this.addComponent(main, rField, 0, 0, 1, 1);

		JButton setR = new JButton("OK");
		setR.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				r = Integer.parseInt(rField.getText());
				cod.setR(r);
				matriceParite.setH(cod.getH());
				tanner.setR(r);
			}
		});
		this.addComponent(main, setR, 1, 0, 1, 1);

		matriceParite = new MatriceParite();
		matriceParite.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Matrice de Parité"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		matriceParite.setPreferredSize(new Dimension(
				(int) (tailleGraphe * 0.6), (int) (tailleGraphe * 0.4)));
		this.addComponent(main, matriceParite, 0, 1, 2, 1);

		tanner = new Tanner();
		tanner.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createTitledBorder("Graphe de Tanner"), BorderFactory
				.createEmptyBorder(5, 5, 5, 5)));
		tanner.setPreferredSize(new Dimension(tailleGraphe, tailleGraphe));
		this.addComponent(main, tanner, 2, 1, 1, 2);

		this.setSize(new Dimension(1500, 1000));
		this.add(main);
		this.setResizable(false);
		this.setVisible(true);
	}

	private void createLayout() {
		layout = new GridBagLayout();

		constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.NORTHWEST;
	}

	private class Tanner extends JPanel {
		private static final long serialVersionUID = 1L;
		private int r;
		private double rayon, rayonExt = tailleGraphe * 0.4, rayonNoeud = 20.0,
				rayonInt = rayonExt / 2.0;
		private Font font;

		public void setR(int r) {
			this.r = r;
			rayon = rayonExt * Math.cos(Math.toRadians(180.0 / r));
			repaint();
		}

		public void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
					RenderingHints.VALUE_COLOR_RENDER_QUALITY);

			super.paintComponent(g2);
			this.setOpaque(false);
			font = g2.getFont();

			double x, y;
			x = y = tailleGraphe / 2;
			double ang = Math.toRadians(90.0);

			double noeudBits[][] = new double[2][2 * r];
			double noeudControle[][] = new double[2][r];

			double decalage = 35.0 / (r - 4.0);
			for (int i = 0; i < r; i++) {
				noeudControle[0][i] = x + rayonExt * Math.cos(ang);
				noeudControle[1][i] = y - rayonExt * Math.sin(ang);
				ang -= Math.toRadians(90.0 / r + decalage);
				noeudBits[0][2 * i] = x + rayonInt * Math.cos(ang);
				noeudBits[1][2 * i] = y - rayonInt * Math.sin(ang);
				ang -= Math.toRadians(90.0 / r - decalage);
				noeudBits[0][2 * i + 1] = x + rayon * Math.cos(ang);
				noeudBits[1][2 * i + 1] = y - rayon * Math.sin(ang);
				ang -= Math.toRadians(180.0 / r);
			}

			for (int i = 0; i < r; i++) {
				g2.drawLine((int) (noeudControle[0][i]),
						(int) (noeudControle[1][i]),
						(int) (noeudControle[0][i + 1 == r ? 0 : i + 1]),
						(int) noeudControle[1][i + 1 == r ? 0 : i + 1]);
				for (int j = 1; j < 5; j++)
					g2.drawLine((int) (noeudControle[0][i]),
							(int) (noeudControle[1][i]),
							(int) (noeudBits[0][2 * ((i + j + r - 5) % r)]),
							(int) noeudBits[1][2 * ((i + j + r - 5) % r)]);
			}

			for (int i = 0; i < r; i++) {
				// noeud de contrôle
				drawNoeud(g2, (int) (noeudControle[0][i]),
						(int) noeudControle[1][i], -1, i + 1);
				// noeud bit rajouté
				drawNoeud(g2, (int) (noeudBits[0][2 * i]),
						(int) noeudBits[1][2 * i], -2, i + r + 1);
				// noeud bit cycle externe
				drawNoeud(g2, (int) (noeudBits[0][2 * i + 1]),
						(int) noeudBits[1][2 * i + 1], -2, i + 1);

			}
		}

		private void drawNoeud(Graphics2D g2, int x, int y, int valeur,
				int numero) {
			g2.setColor(Color.GREEN);
			g2.fillOval((int) (x - rayonNoeud), (int) (y - rayonNoeud),
					(int) (2 * rayonNoeud), (int) (2 * rayonNoeud));
			g2.setColor(Color.BLACK);
			g2.drawOval((int) (x - rayonNoeud), (int) (y - rayonNoeud),
					(int) (2 * rayonNoeud), (int) (2 * rayonNoeud));
			g2.setFont(font);
			g2.drawString(String.format("%d", numero), (int) (x - rayonNoeud),
					(int) (y - rayonNoeud));
			if (valeur == -2) {
			} else if (valeur == -1) {
				g2.drawLine(x, (int) (y - rayonNoeud), x,
						(int) (y + rayonNoeud));
				g2.drawLine((int) (x - rayonNoeud), y, (int) (x + rayonNoeud),
						y);
			} else {
				g2.setFont(new Font(font.getFontName(), Font.BOLD, 20));
				g2.drawString(String.format("%d", valeur), x - 4, y + 8);
			}
		}
	}

	private class MatriceParite extends JPanel {
		private static final long serialVersionUID = 1L;
		private boolean[][] H;

		public void setH(boolean[][] H) {
			this.H = H;
			repaint();
		}

		public void paintComponent(Graphics g) {
			if (H != null) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
						RenderingHints.VALUE_COLOR_RENDER_QUALITY);

				super.paintComponent(g2);
				this.setOpaque(false);

				int r = H.length;

				Font f = new Font(g2.getFont().getFontName(), Font.BOLD, 20);
				g2.setFont(f);

				String s = "";
				for (int i = 0; i < r; i++) {
					s = "";
					for (int j = 0; j < 2 * r; j++) {
						s += H[i][j] ? "1 " : "0 ";
						if (j == r - 1)
							s += " ";
					}
					g2.drawString(s, 37, 28 + (i + 1) * 25);
				}
				int x0 = 21, y0 = 27;
				int stepX = 38, stepY = 25;
				int[][][] pos = {
						{
								{ x0 + 10, x0, x0, x0 + 10 },
								{ y0, y0, y0 + r * stepY + 8,
										y0 + r * stepY + 8 } },
						{
								{ stepX * r + 20, stepX * r + 30,
										stepX * r + 30, stepX * r + 20 },
								{ y0, y0, y0 + r * stepY + 8,
										y0 + r * stepY + 8 } } };
				g2.drawPolyline(pos[0][0], pos[0][1], 4);
				g2.drawPolyline(pos[1][0], pos[1][1], 4);
			}

		}
	}

	private void addComponent(JPanel panel, Component component, int column,
			int row, int width, int height) {
		constraints.gridx = column;
		constraints.gridy = row;
		constraints.gridwidth = width;
		constraints.gridheight = height;
		layout.setConstraints(component, constraints);
		panel.add(component);
	}
}