package codage;

public abstract class Mot {

	protected boolean[] bits;

	public Mot(int taille) {
		bits = new boolean[taille];
	}

	public int getTaille() {
		return bits.length;
	}

	public boolean getBit(int position) {
		return bits[position];
	}

	public void setBits(boolean[] bits) {
		this.bits = bits;
	}

	public void modifierBit(boolean bit, int position) {
		this.bits[position] = bit;
	}

	public void modifierBit(int position) {
		this.bits[position] = !this.bits[position];
	}

	public String toString() {
		String s = "";
		for (boolean b : bits)
			s += b ? "1" : "0";
		return s;
	}
}