package graphe;

import java.util.ArrayList;

public class NoeudPosition extends Noeud {

	private boolean bit;
	private boolean traite = false;
	private boolean bitCourant;


	public NoeudPosition(int l) {
		super(l);
		voisins = new ArrayList<Arc>();
	}



	public void setBit(boolean bit) {
		this.bit = bit;
	}

	public boolean getBit() {
		return this.bit;
	}
	public boolean getBitCourant(){
		return bitCourant;
	}
	public void setBitCourant(boolean b){
		bitCourant =b;
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