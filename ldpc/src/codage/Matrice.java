package codage;

public class Matrice {

	private int[][] M;
	private int lignes;
	private int colonnes;

	public Matrice(boolean[][] m) {
		lignes = m.length;
		colonnes = m[0].length;

		M = new int[lignes][colonnes];

		for (int i = 0; i < lignes; i++) {
			for (int j = 0; j < colonnes; j++) {
				M[i][j] = m[i][j] ? 1 : 0;
			}
		}
	}

	public Matrice(int[][] m) {
		lignes = m.length;
		colonnes = m[0].length;

		this.M = m;
	}

	public Matrice produitMatrice(Matrice b) {

		int[][] out = new int[lignes][b.colonnes];

		if (colonnes == b.lignes) {
			for (int i = 0; i < lignes; i++) {
				for (int j = 0; j < b.colonnes; j++) {
					for (int k = 0; k < colonnes; k++) {
						out[i][j] += M[i][k] * b.getEntier(k, j);
					}
				}
			}
		}
		return new Matrice(out);
	}

	public Matrice transposee() {

		int[][] out = new int[M[0].length][M.length];

		for (int i = 0; i < M.length; i++) {
			for (int j = 0; j < M[0].length; j++) {
				out[j][i] = M[i][j];
			}
		}
		return new Matrice(out);
	}

	public String toString() {
		String out = "";
		for (int i = 0; i < M.length; i++) {
			for (int j = 0; j < M[0].length; j++) {
				out += String.format(M[i][j] == 0 ? "   "
						: (M[i][j] == 1 ? "  *" : "%3d"), M[i][j]);
			}
			out += "\n";
		}
		out += "\n";
		return out;
	}

	public int[][] getM() {
		return M;
	}

	public int getLignes() {
		return lignes;
	}

	public int getColonnes() {
		return colonnes;
	}

	public int getEntier(int i, int j) {
		return this.M[i][j];
	}

	public void setEntier(int i, int j, int v) {
		this.M[i][j] = v;
	}
}