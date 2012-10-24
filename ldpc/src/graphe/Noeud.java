package graphe;

import java.util.ArrayList;

public abstract class Noeud {
	protected int l;

	protected ArrayList<Arc> voisins;

	public int getIndice() {
		return l;
	}

	public Noeud(int l) {
		this.l = l;
	}


	public void supprimerVoisins(Arc a){
		voisins.remove(a);
	}
	public ArrayList<Arc> getVoisins() {
		return voisins;
	}

	public void ajouterVoisin(Arc p) {
		voisins.add(p);
	}

}