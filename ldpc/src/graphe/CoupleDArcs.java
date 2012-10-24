package graphe;

public class CoupleDArcs {
	private Arc arcCourant;
	private Arc arcAncetre;

	CoupleDArcs(Arc a1, Arc a2){
		arcCourant = a1;
		arcAncetre = a2;
	}

	public Arc getAncetre() { return arcAncetre; }
	public Arc getCourant() { return arcCourant; }

}