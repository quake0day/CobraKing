package codage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class MatriceParite {

	private int r;
	private boolean H[][];

	public MatriceParite(boolean H[][]) {
		this.H = H;
		this.r = H.length;
	}

	public MatriceParite(int r) {
		this.r = r;
		H = new boolean[r][2 * r];

		construirCm();

		MatriceM M = new MatriceM(r);
		for (int i = 0; i < r; i++) {
			for (int j = 0; j < r; j++) {
				H[i][j + r] = M.getBit(i, j);
			}
		}
		// m = matriceDuParcours(1);
		// chasserTousLesCyclesLongueur4();
	}

	/**
	 * ON NE S'EN SERT PLUS!!!
	 */
	public Matrice matriceDuParcours(int puissance) {
		Matrice Hint = new Matrice(H);
		Matrice prochainEtat4 = Hint.transposee().produitMatrice(Hint);

		for (int i = 0; i < prochainEtat4.getLignes(); i++)
			prochainEtat4.setEntier(i, i, 0);

		for (int i = 1; i < puissance; i++)
			prochainEtat4 = prochainEtat4.produitMatrice(prochainEtat4);

		return prochainEtat4;
	}

	/**
	 * ON NE S'EN SERT PLUS!!!
	 */
	public void chasserTousLesCyclesLongueur4(Matrice m) {
		for (int i = r; i < 2 * r; i++) {
			for (int j = 0; j < 2 * r; j++) {
				if (i != j)
					if (m.getEntier(i, j) > 1) {
						chasserUnCycleLongueur4(i, j, m);
						i = r;
						j = 0;
					}
			}
		}
		System.out.println("Cycle of length 4 is broken");
	}

	public int getIndiceNoeudPositionLointain(int k, Matrice m) {
		int i;
		while (true) {
			i = r + (int) (Math.random() * r);
			if (m.getEntier(i, k) == 0)
				return i;
		}
	}

	/**
	 * ON NE S'EN SERT PLUS!!!
	 */
	public void chasserUnCycleLongueur4(int k, int l, Matrice m) { // voir le
		// dessin
		// explicatif,
		if (!(k > l)) {
			chasserUnCycleLongueur4(l, k, m); // c'est la colonne k qu'on
			// modifie
			// ici donc on doit avoir k>=r

		} else
			for (int i = 0; i < r; i++) {
				if (H[i][k] && H[i][l]) { // ici on a trouvŽ un voisin commun ˆ
					// k et l

					int p = getIndiceNoeudPositionLointain(k, m);
					H[i][k] = false;
					for (int j = 0; j < r; j++) { // on veut juste un voisin de
						// p
						if (H[j][p]) {
							H[j][k] = true;
							H[j][p] = false;
							break;
						}
					}
					H[i][p] = true;

					// c'est bourrin car on a sžrement
					// pas besoin de faire a ˆ la
					// matrice mais bon.

					break;
				}
			}

	}

	public MotDeCode codage(MotMessage mot) {
		boolean[] code = null;
		if (mot.getTaille() == r + 1) {
			code = new boolean[2 * r];
			for (int i = r - 1; i < 2 * r; i++)
				code[i] = mot.getBit(i - r + 1);
		}
		for (int i = 0; i < r - 1; i++) {
			boolean somme = false;
			for (int j = r; j < 2 * r; j++)
				somme ^= (code[j] & H[i][j]);
			somme ^= code[(i + r - 1) % r];
			code[i] = somme;
		}

		return new MotDeCode(code);
	}

	public int estDansNoyau(MotDeCode mot) {
		for (int i = 0; i < r; i++) {
			boolean somme = false;
			for (int j = 0; j < 2 * r; j++)
				somme ^= (H[i][j] & mot.getBit(j));
			if (somme) {
				return i;
			}
		}

		return -1;
	}

	private void construirCm() {
		H[0][r - 1] = true;
		H[r - 1][r - 1] = true;
		for (int i = 0; i < r - 1; i++) {
			H[i][i] = true;
			H[i + 1][i] = true;
		}
	}

	private class MatriceM { // ici on construit M de la forme (1)
		// alŽatoirement.

		private int r;
		private boolean M[][];

		public boolean getBit(int i, int j) {
			return M[i][j];
		}

		public MatriceM(int r) {
			this.r = r;
			M = new boolean[r][r];
			construirM1();
		}

		private void construirM1() {

			int q = r / 4;
			for (int i = 0; i < 4; i++) {
				for (int j = 0; j < r / 4; j++) {
					M[j + q * i][j * 4 + 0] = true;
					M[j + q * i][j * 4 + 1] = true;
					M[j + q * i][j * 4 + 2] = true;
					M[j + q * i][j * 4 + 3] = true;
				}
			}

			for (int i = 0; i < 10 * M.length; i++) {
				if (Math.random() > 0.5) {
					permuter(true, (int) (Math.random() * r), (int) (Math
							.random() * r), (int) (Math.random() * 4));
				} else {
					permuter(false, (int) (Math.random() * r / 4), (int) (Math
							.random()
							* r / 4), (int) (Math.random() * 4));
				}
			}
		}

		private void permuter(boolean cl, int a, int b, int bloque) {
			if (cl) {// colonnes
				for (int i = 0; i < r / 4; i++) {
					boolean temp = M[i + r * bloque / 4][a];
					M[i + r * bloque / 4][a] = M[i + r * bloque / 4][b];
					M[i + r * bloque / 4][b] = temp;
				}
			} else { // lignes
				for (int i = 0; i < r; i++) {
					boolean temp = M[a + r * bloque / 4][i];
					M[a + r * bloque / 4][i] = M[b + r * bloque / 4][i];
					M[b + r * bloque / 4][i] = temp;
				}
			}
		}
	}

	public int getR() {
		return r;
	}

	public boolean[][] getH() {
		return H;
	}

	public boolean getBit(int i, int j) {
		return H[i][j];
	}

	public String toString() {
		String s = "";
		for (int i = 0; i < 2 * r; i++)
			s += String.format(" %2d", i - 1);
		for (int i = 0; i < r; i++) {
			s += String.format(" %2d", i - 1);
			for (int j = 0; j < 2 * r; j++) {
				s += H[i][j] ? "  1" : "   ";
				if (j == r - 1)
					s += "|";
			}
			s += "\n";
		}
		return s;
	}

	public static void saveMatrice(boolean[][] matrice, String filename) {
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(filename + ".txt");
			ObjectOutputStream oos = new ObjectOutputStream(fos);

			oos.writeObject(matrice);

			oos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static boolean[][] loadMatrice(String filename) {
		FileInputStream fis;
		boolean[][] k = null;
		try {
			fis = new FileInputStream(new File(filename + ".txt"));
			ObjectInputStream ois = new ObjectInputStream(fis);

			k = (boolean[][]) ois.readObject();

			ois.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return k;
	}

	public void setBit(boolean b, int i, int j) {
		this.H[i][j] = b;
	}
}