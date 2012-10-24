import graphe.Graphe;
import codage.MatriceParite;
import codage.MotDeCode;
import codage.MotMessage;

public class Test {

	public static void main(String[] args) {
		int r = 4096;
		int N = 100000;
		double p = 0.05;

		MatriceParite op = new MatriceParite(r);
		System.out.println("matrix created");
		Graphe g = new Graphe(op);
		System.out.println("graphe created");

		System.out.println("Cycle 4");
		g.casserCyclesLongueur(4);

		if (r > 513) {
			System.out.println("\nCycle 6");
			g.casserCyclesLongueur(6);
		}

		int erreurs = 0;
		for (int i = 0; i < N; i++) {
			MotMessage m;
			boolean[] mot = new boolean[r + 1];
			for (int j = 0; j < r + 1; j++)
				mot[j] = (Math.random() > 0.5);

			m = new MotMessage(mot);
			erreurs += codeDecode(m, g, p, false) > 0 ? 1 : 0;
		}
		System.out.println("p " + p + " -> " + (double) erreurs / (double) N
				+ "Iterations: " + g.getIterations());

	}

	public static int codeDecode(MotMessage m, Graphe g, double bruit,
			boolean affichage) {
		if (affichage)
			System.out.println("Message:          " + m);

		MotDeCode mo = g.codage(m);

		int bitChanges = mo.ajouterBruit(bruit);
		if (affichage) {
			System.out.println("Code:             " + mo);
			System.out.println("Bits changed: " + bitChanges);
		}

		if (affichage)
			System.out.println("Code recovered:     " + mo);

		MotDeCode moo = g.fromMotRecuToMotDeCode(mo);
		if (affichage)
			System.out.println("Decoded:         " + moo);

		MotMessage mooo = g.fromMotDeCodeToMotMessage(moo);
		if (affichage) {
			System.out.println("Message recover: " + mooo.toString());
			System.out.println("Distance: " + m.getDistance(mooo) + "\n");
		}

		return m.getDistance(mooo);
	}
}