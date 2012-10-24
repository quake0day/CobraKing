package codage;

public class MotMessage extends Mot {

	public MotMessage(int taille) {
		super(taille);
	}

	public MotMessage(boolean[] bits) {
		super(bits.length);
		super.bits = bits;
	}

	public MotMessage(MotDeCode mot) {
		super(mot.getTaille() / 2 + 1);
		int r = mot.getTaille() / 2;
		for (int i = 0; i < r + 1; i++)
			super.bits[i] = mot.getBit(i + r - 1);
	}

	public boolean equals(boolean[] bits) {
		for (int i = 0; i < super.getTaille(); i++)
			if (super.bits[i] ^ bits[i])
				return false;
		return true;
	}
	public int getDistance(MotMessage m) {
		int d = 0;
		for (int i = 0; i < super.getTaille(); i++)
			if (super.bits[i] ^ m.getBit(i))
				d++;
		return d;
	}
}