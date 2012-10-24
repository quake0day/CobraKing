package codage;

public class Codificateur {

	private int r;
	private boolean H[][];

	public void setR(int r) {
		this.r = r;
		H = new boolean[r][2 * r];

		construirCm();
		construirM();
	}

	public boolean[] codage(boolean[] mot) {
		boolean[] code = null;
		printMot(mot);
		if (mot.length == r + 1) {
			code = new boolean[2 * r];
			for (int i = 0; i < r + 1; i++)
				code[i + r - 1] = mot[i];
		}

		for (int i = 0; i < r - 1; i++) {
			boolean somme = false;
			for (int j = 0; j < 4; j++)
				somme ^= code[r + (i + j + r - 4) % r];
			somme ^= code[(i + r - 1) % r];
			code[i] = somme;
		}

		printCode(code);

		return code;
	}

	private void construirCm() {
		H[0][r - 1] = true;
		H[r - 1][r - 1] = true;
		for (int i = 0; i < r - 1; i++) {
			H[i][i] = true;
			H[i + 1][i] = true;
		}
	}

	private void construirM() {
		for (int i = 0; i < r; i++)
			for (int j = r; j < 2 * r; j++)
				H[i][j] = true;

		enleverDiag(0);

		for (int i = 1; i < r - 4; i++) {
			enleverDiag(i);
		}
	}

	private void enleverDiag(int x) {
		if (x < r)
			for (int j = r; j < 2 * r; j++)
				H[j - r][(j + x < 2 * r ? j + x : j - (r - x))] = false;
	}

	public String toString() {
		String s = "";
		for (int i = 0; i < r; i++) {
			for (int j = 0; j < 2 * r; j++) {
				s += H[i][j] ? "1 " : "0 ";
				if (j == r - 1)
					s += "| ";
			}
			s += "\n";
		}
		return s;
	}

	public boolean[][] getH() {
		return H;
	}

	public void printH() {
		System.out.print(toString());
	}

	public void printMot(boolean[] mot) {
		for (boolean b : mot)
			System.out.print(b ? "1" : "0");
		System.out.println();
	}

	public void printCode(boolean[] code) {
		for (int i = 0; i < r - 1; i++)
			System.out.print(code[i] ? "1" : "0");
		System.out.print("|");
		for (int i = r - 1; i < 2 * r; i++)
			System.out.print(code[i] ? "1" : "0");
		System.out.println();
	}
}