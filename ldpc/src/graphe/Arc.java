package graphe;

public class Arc {
	private NoeudControle controle;
	private NoeudPosition position;
	private boolean traite = false;

	private double fromControleToPosition, fromPositionToControle;

	public Arc(NoeudControle controle, NoeudPosition position) {
		this.controle = controle;
		this.position = position;

		controle.ajouterVoisin(this);
		position.ajouterVoisin(this);
	}

	public NoeudControle getControle() {
		return controle;
	}

	public NoeudPosition getPosition() {
		return position;
	}

	public double getFromControleToPosition() {
		return fromControleToPosition;
	}

	public void setFromControleToPosition(double fromControleToPosition) {
		this.fromControleToPosition = fromControleToPosition;
	}

	public double getFromPositionToControle() {
		return fromPositionToControle;
	}

	public void setFromPositionToControle(double fromPositionToControle) {
		this.fromPositionToControle = fromPositionToControle;
	}

	public String toString() {
		return String.format("%d - %d", controle.l, position.l);
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