package codage;

public class MotDeCode extends Mot {

	public MotDeCode(int taille) {
		super(taille);
	}

	public MotDeCode(boolean[] b) {
		super(b.length);
		super.bits = b;
	}

	public int getPoids() {
		int i = 0;
		for (boolean b : super.bits)
			if (b)
				i++;
		return i;
	}

	public int getDistance(MotDeCode m) {
		int d = 0;
		for (int i = 0; i < super.getTaille(); i++)
			if (super.bits[i] ^ m.getBit(i))
				d++;
		return d;
	}

	public String toString() {
		String s = "";
		for (int i = 0; i < super.getTaille(); i++) {
			s += super.bits[i] ? "1" : "0";
			if (i == (super.getTaille() - 2) / 2)
				s += "|";
		}
		return s;
	}

	public int ajouterBruit(double p) {
		int e = 0;
		for (int i = 0; i < super.getTaille(); i++)
			if (Math.random() < p){
				super.bits[i] = !super.bits[i];
				e++;
			}
		return e;
	}



}