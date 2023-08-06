import java.util.ArrayList;

public class AegeanClass {
	private String URI;
	private String name;
	private String comments;
	private String label;
	private Boolean instancesWithLabels;
	private ArrayList<AegeanInstance> instances;
	private ArrayList<String> candidates;
	private ArrayList<Double> similarities;
	private String sameAsClass;
	
	public AegeanClass(String URI, String comments, String label) {
		this.URI = URI;
		this.name = this.extractName(URI);
		this.comments = comments;
//		this.label = label.replace(" ", "");
		this.label = label;
		this.instancesWithLabels = false;
		this.instances = new ArrayList<>();
		this.candidates = new ArrayList<>();
		this.similarities = new ArrayList<>();
		this.sameAsClass = null;
	}
	
	public String extractName(String URI) {
		String[] strParts = URI.split("/");
		String className = strParts[strParts.length-1];
		
//		return className.toLowerCase();
		return className;
	}
	
	
	public int printInstances() {
		if(instances.size() == 0) {
			System.out.println("Δεν υπάρχουν instances για την κλάση " + this.label);
			return 0;
		}
		for(Instance instance : instances) {
			System.out.println(instance);
			System.out.println("----------------");
		}
		return 1;
	}
	
	
	public int findMaxSimilarity() {
		if(this.similarities.size() == 0)
			return -1;
		
		int maxIndex = 0;
		double maxValue = this.similarities.get(0);
		
		for(int i = 1; i< this.similarities.size(); i++) {
			if(this.similarities.get(i) > maxValue) {
				maxValue = this.similarities.get(i);
				maxIndex = i;
			}
		}
		
		return maxIndex;
	}
	
	public int findMinimumDistance() {
		if(this.similarities.size() == 0)
			return -1;
		
		int minIndex = 0;
		double minValue = this.similarities.get(0);
		
		for(int i = 1; i< this.similarities.size(); i++) {
			if(this.similarities.get(i) < minValue) {
				minValue = this.similarities.get(i);
				minIndex = i;
			}
		}
		
		return minIndex;
	}
	
	public void addCandidateClass(String candidate) {
		this.candidates.add(candidate);
	}
	
	public void addSimilarity(Double similarity) {
		this.similarities.add(similarity);
	}
	
	
	public void addInstance(AegeanInstance instance) {
		this.instances.add(instance);
	}
	
	public ArrayList<AegeanInstance> getInstances(){
		return this.instances;
	}
	
	
	
	public String getURI() {
		return URI;
	}


	public void setURI(String uRI) {
		URI = uRI;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public Boolean getInstancesWithLabels() {
		return instancesWithLabels;
	}
	
	public void setInstancesWithLabels(Boolean value) {
		this.instancesWithLabels = value;
	}

	public String getLabel() {
		return label;
	}


	public void setLabel(String label) {
		this.label = label;
	}
	
	
	public ArrayList<String> getCandidates() {
		return candidates;
	}

	
	public void setCandidates(ArrayList<String> candidates) {
		this.candidates = candidates;
	}


	public ArrayList<Double> getSimilarities() {
		return similarities;
	}
	

	public void setSimilarities(ArrayList<Double> similarities) {
		this.similarities = similarities;
	}
	
	public void setSameAsClass(String classURI) {
		this.sameAsClass = classURI;
	}
	
	public String getSameAsClass() {
		return this.sameAsClass;
	}
	

	public String toString() {
		String s = "";
		s += this.URI + "\n";
		s += this.name + "\n";
		s += this.label + "\n";
		s += this.comments + "\n";
		
		return s;
	}
	
	
}
