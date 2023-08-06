import java.util.ArrayList;

public class CandidateInstance extends Instance{
	private double similarity;
	
	public CandidateInstance(String URI, String label, String type, String wkt) {
		super(URI, label, type, wkt);
		
	}
	
	
	public void setSimilarity(Double similarity) {
		this.similarity = similarity;
	}
	
	public double getSimilarity() {
		return this.similarity;
	}

}
