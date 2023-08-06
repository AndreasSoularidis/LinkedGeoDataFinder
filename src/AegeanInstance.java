import java.util.ArrayList;

public class AegeanInstance extends Instance{
	
	private String sameAs;
	private ArrayList<CandidateInstance> candidates;
	
	public AegeanInstance(String URI, String label, String type, String wkt) {
		super(URI, label, type, wkt);
		this.sameAs = null;
		candidates = new ArrayList<CandidateInstance>();
	}
	
	public String getSameAs() {
		return sameAs;
	}

	public void setSameAs(String uri) {
		this.sameAs = uri;
	}
	
	public void addCandidateInstance(CandidateInstance instance) {
		this.candidates.add(instance);
	}

	public ArrayList<CandidateInstance> getCandidates() {
		return candidates;
	}

	public void setCandidates(ArrayList<CandidateInstance> candidates) {
		this.candidates = candidates;
	}
	
	public CandidateInstance findBestInstance() {
		if(candidates.size() == 0)
			return null;
		
		CandidateInstance bestInstance = candidates.get(0);
		double maxSimilarity = candidates.get(0).getSimilarity();
		for(CandidateInstance candidate : candidates) {
			if(candidate.getSimilarity() > maxSimilarity) {
				maxSimilarity = candidate.getSimilarity();
				bestInstance = candidate;
			}
		}
		return bestInstance;
	}
	
//	public int findMaxSimilarity() {
//		if(this.similarities.size() == 0)
//			return -1;
//		
//		int maxIndex = 0;
//		double maxValue = this.similarities.get(0);
//		
//		for(int i = 1; i< this.similarities.size(); i++) {
//			if(this.similarities.get(i) > maxValue) {
//				maxValue = this.similarities.get(i);
//				maxIndex = i;
//			}
//		}
//		
//		return maxIndex;
//	}

}
