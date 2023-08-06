import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Instance {
	private String URI;
	private String label;
	private String name;
	private String type;
	private String wkt;
	private String wktType;
	private String latitude;
	private String longitude;
	private String description;
	
	public Instance(String URI, String label, String type, String wkt) {
		this.URI = URI;
		this.label = this.clearLabel(label);
		this.name = this.extractName();
		this.type = type;
		this.wkt = wkt;	
		this.wktType = null;
		this.findGeometryType();
	}
	
	public int findGeometryType() {
		int start, end;
//		System.out.println(wkt);
		String polygonRepresentation;
		Pattern multipoligonPattern = Pattern.compile("MULTIPOLYGON ", Pattern.CASE_INSENSITIVE);
		Pattern polygonPattern = Pattern.compile("POLYGON ", Pattern.CASE_INSENSITIVE);
		
//		String[] strParts = wkt.split("^^");
//		String geoObject = strParts[strParts.length-1];
		
		if(wkt == null)
			return 0;
		
	    Matcher multipoligonMatcher = multipoligonPattern.matcher(wkt);
	    if(multipoligonMatcher.find() == true) {
	        start = wkt.indexOf("(");
	        end = wkt.indexOf(")))") + 3;
	        wkt = "MULTIPOLYGON " + wkt.substring(start, end);
	    	wktType = "MULTIPOLYGON";
	    	return 0;
	    }
	    
	    Matcher poligonMatcher = polygonPattern.matcher(wkt);
	    if(poligonMatcher.find() == true) {
	    	start = wkt.indexOf("(");
	        end = wkt.indexOf(")") + 2;
	        wkt = "POLYGON " + wkt.substring(start, end);
	        wktType = "POLYGON";
	        return 0;
	    }
	    	
	    start = wkt.indexOf("(");
        end = wkt.indexOf(")") + 1;
        wkt = "POINT " + wkt.substring(start, end);
	    wktType = "POINT";
	    return 0;
	}
	
	
	public String extractName() {
		String[] strParts = URI.split("/");
		String className = strParts[strParts.length-1];
		return className;
	}
	
	public String clearLabel(String label) {
		if(label == null)
			return null;
		String[] strParts = label.split("@");
		String lbl = strParts[0];
		return lbl;
	}
	
	
	public String getURI() {
		return URI;
	}


	public void setURI(String uRI) {
		URI = uRI;
	}


	public String getLabel() {
		return label;
	}


	public void setLabel(String label) {
		this.label = label;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	public String getWkt() {
		return wkt;
	}


	public void setWkt(String wkt) {
		this.wkt = wkt;
	}
	

	public String toString() {
		return "URI: " + this.URI + "\n" ;

	}
}
