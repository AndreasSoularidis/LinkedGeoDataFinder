import java.util.ArrayList;

public class CandidateClass {
	private String URI;
	private String name;
	private String label;
	
	public CandidateClass(String URI, String label) {
		this.URI = URI;
		this.name = this.extractName(URI);
		this.label = this.extractLabel(label);
	}
	
	public String extractLabel(String label) {
		if(label == null)
			return null;
		String[] strParts = label.split("@");
		return strParts[0];
	}
	
	public String extractName(String URI) {
		String[] strParts = URI.split("/");
		String className = strParts[strParts.length-1];
		return className;
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
	

	public String getLabel() {
		return label;
	}


	public void setLabel(String label) {
		this.label = label;
	}

	
	public String toString() {
		String s = "";
		s += this.URI + " ";
		s += this.label + "\n";		
		return s;
	}
}
