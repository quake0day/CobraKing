package graphe;

import java.util.ArrayList;
import java.util.HashMap;

import codage.MatriceParite;
import codage.MotDeCode;
import codage.MotMessage;

public class Graphe {

	private int r;

	private ArrayList<NoeudControle> noeudsControle = new ArrayList<NoeudControle>();
	private ArrayList<NoeudPosition> noeudsPosition = new ArrayList<NoeudPosition>();
	private HashMap<Cle, Arc> arcs = new HashMap<Cle, Arc>();
	private MatriceParite matriceParite;

	private ArrayList<Integer> iterations = new ArrayList<Integer>();

	public Graphe(MatriceParite H) {
		this.r = H.getR();
		this.matriceParite = H;

		for (int i = 0; i < 2 * r; i++)
			noeudsPosition.add(new NoeudPosition(i));

		for (int i = 0; i < r; i++)
			noeudsControle.add(new NoeudControle(i));

		for (NoeudPosition p : noeudsPosition) {
			for (NoeudControle c : noeudsControle) {
				if (H.getBit(c.getIndice(), p.getIndice())) {
					arcs.put(new Cle(c.getIndice(), p.getIndice()), new Arc(c,
							p));
				}
			}
		}
	}

	public void metAJourMatrice() {
		for (int i = 0; i < r; i++) {
			for (int j = 0; j < 2 * r; j++) {
				matriceParite.setBit(false, i, j);
			}
		}
		for (Cle c : arcs.keySet()) {
			matriceParite.setBit(true, c.controle, c.position);
		}
	}

	/*
	 * ca ne marche pas apres les diffÃ©rents essais. public MotDeCode
	 * trouveMotDeCodePoidsFaible(int y){ MotDeCode m = new MotDeCode(2*r);
	 * NoeudPosition n = this.noeudsPosition.get(y); for(Arc a : n.getVoisins())
	 * for(Arc b : a.getControle().getVoisins()) { if(Math.random()>0.5)
	 * {m.modifierBit(b.getPosition().l); for(Arc c :
	 * b.getPosition().getVoisins()) for(Arc d : c.getControle().getVoisins())
	 * if(Math.random()>0.6) m.modifierBit(d.getPosition().l); } }
	 * System.out.print(m); return fromMotRecuToMotDeCode(m);
	 * 
	 * }
	 */
	public MotDeCode fromMotRecuToMotDeCode(MotDeCode m) {
		int q;
		boolean b;
		for (int i = 0; i < 2 * r; i++) {
			noeudsPosition.get(i).setBit(m.getBit(i));
		}

		for (Cle c : arcs.keySet()) {
			arcs.get(c)
					.setFromPositionToControle(m.getBit(c.position) ? -1 : 1);
		}
		for (NoeudPosition p : noeudsPosition) { // on initialise les bits
			// courants
			p.setBitCourant(m.getBit(p.l));
		}

		for (int i = 0; i < 50; i++) {

			for (Cle c : arcs.keySet()) { // on boucle sur tous les arcs
				double minimum = -1; // initialiser autrement... ERREUR !!!
				double signe = 1;
				for (Arc a : arcs.get(c).getControle().getVoisins()) {
					if (!a.equals(arcs.get(c))) {
						double delta = a.getFromPositionToControle(); // on
						// rï¿½cupï¿½re
						// le
						// delta
						// "courant"
						signe = delta * signe;
						if (Math.abs(delta) < minimum || minimum == -1) {
							minimum = Math.abs(delta);
						}

					}
				}
				signe = Math.signum(signe); // on met un moins ici ou pas ?
				arcs.get(c).setFromControleToPosition(signe * minimum);
			}
			for (Cle c : arcs.keySet()) { // on boucle sur tous les arcs
				double somme = arcs.get(c).getPosition().getBit() ? -1 : +1;
				for (Arc a : arcs.get(c).getPosition().getVoisins()) {
					if (!a.equals(arcs.get(c))) {
						somme = somme + a.getFromControleToPosition(); // on
						// ajoute
						// le
						// (-1)^...*min(|delta|)
					}
				}
				arcs.get(c).setFromPositionToControle(somme);
			}
			/* */q = 0;

			for (NoeudPosition p : noeudsPosition) {
				double somme = p.getBit() ? -1 : 1;
				for (Arc a : p.getVoisins()) {
					somme = somme + a.getFromControleToPosition(); // on ajoute
					// le
					// (-1)^...*min(|delta|)
				}
				b = somme < 0;

				if (b != p.getBitCourant())
					q = q + 1;
				p.setBitCourant(b);

			}

			if (q == 0) {
				iterations.add(i);
				break;
			}
		}

		MotDeCode mot = new MotDeCode(2 * r);

		for (NoeudPosition p : noeudsPosition) {
			double somme = p.getBit() ? -1 : 1;
			for (Arc a : p.getVoisins()) {
				somme = somme + a.getFromControleToPosition(); // on ajoute le
				// (-1)^...*min(|delta|)
			}
			mot.modifierBit(somme < 0, p.l);
			if (somme == 0) {
				System.out
						.println(String
								.format(
										"AIE lï¿½ on obtient un delta_i = %d. On choisit false par dï¿½faut",
										p.l));
			}
		}

		return mot;
	}

	public double getIterations(){
		double moyenne = 0.0;
		for(Integer i : iterations){
			moyenne += i;
		}
		moyenne /= iterations.size();
		iterations.clear();

		return moyenne;
	}

	public MotMessage fromMotDeCodeToMotMessage(MotDeCode m) {
		return new MotMessage(m);
	}

	public MotDeCode codage(MotMessage m) {
		return matriceParite.codage(m);
	}

	public MatriceParite getMatriceParite() {
		return matriceParite;
	}

	private class Cle {
		int controle, position;

		public Cle(int i, int j) {
			this.controle = i;
			this.position = j;
		}

		public boolean equals(Object o) {
			return (this.controle == ((Cle) o).controle)
					&& (this.position == ((Cle) o).position);
		}

		public int hashCode() {
			return 101 * controle + 257 * position;
		}
	}

	public ArrayList<NoeudControle> getNoeudsControle() {
		return noeudsControle;
	}

	public Arc detecterCycleDeLonguerFrom(NoeudPosition racine, int L) { // trouve
		// un
		// cycle
		// de
		// longueur
		// L
		// à
		// partir
		// de
		// la
		// position
		// "racine"
		boolean positionOuControle = false; // true signifie que les prochains
		// enfants seront les noeuds de
		// position

		HashMap<NoeudPosition, CoupleDArcs> noeudsPositionCourants = new HashMap<NoeudPosition, CoupleDArcs>(); // on
		// utilise
		// hashMap
		// pour
		// trouver
		// deux
		// trucs
		// identiques
		// très
		// rapidement
		HashMap<NoeudControle, CoupleDArcs> noeudsControleCourants = new HashMap<NoeudControle, CoupleDArcs>();

		for (Arc a : racine.getVoisins()) {
			noeudsControleCourants.put(a.getControle(), new CoupleDArcs(a, a));
		}

		CoupleDArcs temp;
		for (int l = (L / 2) - 1; l > 0; l--) {
			if (positionOuControle) { // ici les prochains enfants à traiter
				// sont les noeuds de positions
				noeudsControleCourants = new HashMap<NoeudControle, CoupleDArcs>();
				for (NoeudPosition n : noeudsPositionCourants.keySet()) {
					temp = noeudsPositionCourants.get(n);
					for (Arc a : n.getVoisins()) {
						if (!a.equals(temp.getCourant())) {
							if (noeudsControleCourants.containsKey(a
									.getControle())) { // ALERTE on a trouvé un
								// cycle au niveau L-l
								return (temp.getAncetre()); // on retourne le
								// premier arc
								// ancètre qui sera
								// dans la partie
								// gauche de la
								// matrice
							} else
								noeudsControleCourants.put(a.getControle(),
										new CoupleDArcs(a, temp.getAncetre()));

						}
					}

				}

			} else { // ici les prochains enfants à traiter sont des noeuds de
				// controle
				noeudsPositionCourants = new HashMap<NoeudPosition, CoupleDArcs>();
				for (NoeudControle c : noeudsControleCourants.keySet()) {
					temp = noeudsControleCourants.get(c);
					for (Arc a : c.getVoisins()) {
						if (!a.equals(temp.getCourant())) {
							if (noeudsPositionCourants.containsKey(a
									.getPosition())) { // alerte on a trouvé un
								// cycle au niveau L-l
								return (temp.getAncetre());
							} else
								noeudsPositionCourants.put(a.getPosition(),
										new CoupleDArcs(a, temp.getAncetre()));
						}
					}

				}

			}
			positionOuControle = !positionOuControle;
		}

		// System.out.println(String.format("pas trouvé de cycle de longuer %d",
		// L));

		return null;
	}

	public Arc getArcLointainFrom(NoeudPosition n) { // on prend une méthode au
		// hasard là, on espère
		// que ça marchera
		NoeudPosition e = getNoeudPosition(r + (int) (Math.random() * r));
		if (e.equals(n)) {
			return getArcLointainFrom(n);
		} else
			return e.getVoisins().get(
					(int) (Math.random() * e.getVoisins().size()));

	}

	public void croiserArcs(Arc a, Arc b) { // ici on croise un arc dit
		// "lointain" et un arc du cycle
		// courant
		Arc c = new Arc(a.getControle(), b.getPosition());
		Arc d = new Arc(b.getControle(), a.getPosition());

		a.getControle().supprimerVoisins(a);
		a.getPosition().supprimerVoisins(a);

		b.getControle().supprimerVoisins(b);
		b.getPosition().supprimerVoisins(b);

		arcs.remove(new Cle(a.getControle().l, a.getPosition().l));
		arcs.remove(new Cle(b.getControle().l, b.getPosition().l));
		arcs.put(new Cle(c.getControle().l, c.getPosition().l), c);
		arcs.put(new Cle(d.getControle().l, d.getPosition().l), d);

		// System.out.println("On a croisé " + a + " et " + b);
		// System.out.println("On a créé " + c + " et " + d);

	}

	public void casserCyclesLongueur(int L) {
		boolean b = true;
		while (b) {
			int k = 0;
			for (int i = r; i < 2 * r; i++) {
				NoeudPosition np = this.noeudsPosition.get(i);
				Arc a = this.detecterCycleDeLonguerFrom(np, L);

				while (a != null) {
					croiserArcs(a, getArcLointainFrom(a.getPosition()));
					a = this.detecterCycleDeLonguerFrom(np, L);
					k++;
				}
			}
			System.out.println(String.format("On a cassé %d cycles", k));
			k = 0;
			for (int i = r; i < 2 * r; i++) {
				Arc a = this.detecterCycleDeLonguerFrom(this.noeudsPosition
						.get(i), L);
				if (a != null)
					k = k + 1;
			}
			System.out.println(String.format("Encore %d cycles", k));
			if (k == 0)
				b = false;
		}
	}

	public NoeudPosition getNoeudPosition(int j) {
		return noeudsPosition.get(j);
	}

}