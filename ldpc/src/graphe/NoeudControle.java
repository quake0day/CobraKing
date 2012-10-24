package graphe;

import java.util.ArrayList;

public class NoeudControle extends Noeud {

	public boolean traite = false;

	public NoeudControle(int l) {
		super(l);
		voisins = new ArrayList<Arc>();
	}



	public void marque(){
		if(this.traite) System.out.print("Noeud déja marqué");
		else this.traite = true;
	}
	public void deMarque(){
		if(! this.traite) System.out.print("Noeud déja pas marqué");
		else this.traite = false;
	}

}