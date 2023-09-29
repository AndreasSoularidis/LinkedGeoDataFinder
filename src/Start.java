import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.TopologyException;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.util.AssertionFailedException;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.WS4J;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;


public class Start {
	
	public static Double calculateJaccardSimilarity(CharSequence left, CharSequence right) {
        Set<String> intersectionSet = new HashSet<String>();
        Set<String> unionSet = new HashSet<String>();
        boolean unionFilled = false;
        int leftLength = left.length();
        int rightLength = right.length();
        if (leftLength == 0 || rightLength == 0) {
            return 0d;
        }

        for (int leftIndex = 0; leftIndex < leftLength; leftIndex++) {
            unionSet.add(String.valueOf(left.charAt(leftIndex)));
            for (int rightIndex = 0; rightIndex < rightLength; rightIndex++) {
                if (!unionFilled) {
                    unionSet.add(String.valueOf(right.charAt(rightIndex)));
                }
                if (left.charAt(leftIndex) == right.charAt(rightIndex)) {
                    intersectionSet.add(String.valueOf(left.charAt(leftIndex)));
                }
            }
            unionFilled = true;
        }
        return Double.valueOf(intersectionSet.size()) / Double.valueOf(unionSet.size());
    }
	
	
	public static Double LevenshteinSimilarity(String a, String b){
        a = a.toLowerCase();
        b = b.toLowerCase();
        
        int[] costs = new int[b.length() + 1];
        for (int j = 0; j < costs.length; j++)
            costs[j] = j;
        for (int i = 1; i <= a.length(); i++)
        {
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++)
            {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]),
                        a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        
        if(a.length() >= b.length())
        	return 1.0 - (Double.valueOf(costs[b.length()]) / Double.valueOf(a.length()));
        
        return 1.0 - (Double.valueOf(costs[b.length()]) / Double.valueOf(b.length()));
    }
	
	
//	public static void writeToFile(ArrayList<AegeanClass> aegeanClasses, String fname) {
//		File filename = new File(fname);
//		try {
//        	FileWriter writer = new FileWriter(filename);
//        	for(AegeanClass cl: aegeanClasses) {
//        		writer.write("Instances of Class " + cl.getURI());
//        		writer.write(System.lineSeparator());
//        		ArrayList<Instance> instances = cl.getInstances();
//        		if(instances.size() > 0) {
//        			for(Instance instance: instances) {
//        				ArrayList<String> candidates = instance.getCandidates();
//            			
//        				for(String candidate : candidates) {
//        					writer.write(candidate);
//                			writer.write(System.lineSeparator());
//        				}
//            		}
//        		}
//        		
//        	}
//        	writer.close();
//        }catch(IOException ex) {
//        	ex.printStackTrace();
//        }
//	}
	
	// ΕΔΩ ΘΑ ΜΠΕΙ Η ΣΥΝΑΡΤΗΣΗ ΠΟΥ ΘΑ ΒΡΙΣΚΕΙ ΤΟ SEMANTIC SIMILARITY
	// ΤΟ ΜΕΓΑΛΥΤΕΡΟ MATCH ΚΕΡΔΙΖΕΙ
	public static double wuPalSimilarity(String word1, String word2) {
		ILexicalDatabase db = new NictWordNet();

        // Set up WS4J configuration
        WS4JConfiguration.getInstance().setMFS(true);

        // Calculate the similarity
        double wuPalmerSimilarity = WS4J.runWUP(word1, word2);
//        System.out.println("Wu-Palmer Similarity between " + word1 + " and " + word2 + ": " + wuPalmerSimilarity);
        return wuPalmerSimilarity;
	}
	
	
	public static int isSubstring(String aegeanClassString, String otherClassString){
        // Split the camelCaseString (Class name) into separate words
        String[] words = aegeanClassString.split("(?=[A-Z])");

        // Check if at least one word of the Aegean class is a substring of the other class
        int index = 0;
        while(index < words.length) {
        	if(otherClassString.indexOf(words[index]) != -1)
        		return 1;
        	index += 1;
        }

        // None of the separate words in the Aegean class is a subset of the other class
        return -1;
    }
	
	public static String tranformToSentence(String camelCaseString) {
		// Split the camelCaseString into separate words
        String[] words = camelCaseString.split("(?=[A-Z])");

        // Join the words into a sentence
        String sentence = String.join(" ", words);
        return sentence;
	}
	
	
	public static void findEquivalentClasses(ArrayList<AegeanClass> aegeanClasses, ArrayList<CandidateClass> candidateClasses, Model model, double threshold) {
        int matches = 0;
        double similarity = -1;
        CandidateClass bestMatchClass = null;
        for(AegeanClass acl: aegeanClasses) {
        	
        	// Calculate the Levenshtein Similarity for each one of the candidate classes
        	double maxSimilarity = -1;
        	int maxIndex = -1;
        	for(int i=0; i<candidateClasses.size(); i++) {
        		if(isSubstring(acl.getName(), candidateClasses.get(i).getName()) != -1) {
        			String aegeanClassSentence = tranformToSentence(acl.getName());
        			String candidateClassSentence = tranformToSentence(candidateClasses.get(i).getName());
//	        		similarity = wuPalSimilarity(aegeanClassSentence, candidateClassSentence);
	        		similarity = LevenshteinSimilarity(acl.getName(), candidateClasses.get(i).getName());
//	        		System.out.println(aegeanClassSentence + " " + candidateClassSentence + " " + similarity);
	        		if(similarity > maxSimilarity){
	        			bestMatchClass = candidateClasses.get(i);
	        			maxSimilarity = similarity;
	        			maxIndex = i;
	        		}
        		}
        	}
        	
        	
        	// For each class we set as sameAs class, the candidate class that has similarity greater than 0.90
        	if(maxSimilarity >= threshold) {
        		acl.setSameAsClass(bestMatchClass.getURI());
//        		System.out.println(acl.getName() + " sameAs " + bestMatchClass.getName());
        		matches +=1;
//        		candidateClasses.remove(maxIndex);
        		// Create the resources
        		Resource newResource = model.createResource(acl.getURI());
            	Resource equivalentResource = model.createResource(acl.getSameAsClass());
            	newResource.addProperty(OWL.sameAs, equivalentResource);
//            	newResource.addProperty(SKOS.exactMatch, equivalentResource);
        	}
        }
        System.out.println("Total matches " + matches);
	}
	
	
	public static int findEquivalentInstances(ArrayList<AegeanInstance> aegeanInstances, ArrayList<CandidateInstance>candidateInstances,
		Model model, int distanceThreshold, double similarityThreshold) {
		int counter = 0;
		double distance;
		double intersection;
		double similarity = 0;
		try {
	        for(AegeanInstance aegeanInstance: aegeanInstances) {
	        	double maxSimilarity = -1;
	        	CandidateInstance bestMachingInstance = null;
	        	for(CandidateInstance candidateInstance : candidateInstances) {
	        		distance = 0;
		        	intersection = 0;
		        	if(aegeanInstance.getType().equals("POINT") && candidateInstance.getType().equals("POINT")) 
		        		distance = geometryDistance(aegeanInstance, candidateInstance);  		
		        	else { 
		        		distance = geometryDistance(aegeanInstance, candidateInstance); 
		        		intersection = geometyIntersection(aegeanInstance, candidateInstance);
		        	}
		        	if((distance <= distanceThreshold || intersection == 1 )) {
		        		if(aegeanInstance.getLabel() != null && candidateInstance.getLabel() != null)
		        			similarity = LevenshteinSimilarity(aegeanInstance.getLabel(), candidateInstance.getLabel());
		        		else if(aegeanInstance.getName() != null && candidateInstance.getName() != null)
		        			similarity = LevenshteinSimilarity(aegeanInstance.getName(), candidateInstance.getName());
		        		else if(aegeanInstance.getLabel() != null && candidateInstance.getLabel() == null)
		        			similarity =LevenshteinSimilarity(aegeanInstance.getLabel(), candidateInstance.getName());
		        		else if(aegeanInstance.getLabel() == null && candidateInstance.getLabel() != null)
		        			similarity = LevenshteinSimilarity(aegeanInstance.getName(), candidateInstance.getLabel());
		        	
		        		if(similarity >= maxSimilarity){
		        			bestMachingInstance = candidateInstance;
		        			maxSimilarity = similarity;
		        		}
		        	}
	        	}
	        	  	
	    		if(maxSimilarity < similarityThreshold) 
	    			continue;
		        
	    		// For each instance we set as sameAs instance, the candidate instance that has similarity greater than 0.90
		        aegeanInstance.setSameAs(bestMachingInstance.getURI());
	        	
	        	// Create the resources
	        	Resource newResource = model.createResource(aegeanInstance.getURI());
	            Resource equivalentResource = model.createResource(bestMachingInstance.getURI());
	            newResource.addProperty(OWL.sameAs, equivalentResource);
	            counter += 1;
	        }
       }catch(TopologyException ex) {
    	   System.out.println("An error occured (Topology Exception) " + ex.getMessage());
       }
		catch(NullPointerException ex) {
    	   System.out.println("An error occured (NullPointerException)" + ex.getMessage());
       }
		catch(AssertionFailedException ex) {
	    	   System.out.println("An error occured (AssertionFailedException)" + ex.getMessage());
	    }
		
		return counter;
	}
	
	public static void exportToTTL(Model model, String format, String filename) {
		// Export the .ttl file in N-Triples format "N-Triples"
//        model.write(System.out, format);
        try {
	    	FileWriter writer = new FileWriter(filename);
	    	model.write(writer, format);
	    	
	    	writer.close();
        }catch(IOException ex) {
        	ex.printStackTrace();
        }
	}
	
	
	public static ArrayList<AegeanClass> sparqlGetAllClasses() {
		ArrayList <AegeanClass> totalClasses = new ArrayList<AegeanClass>(); 
		
		String endpoint ="http://semantics.aegean.gr:3030/data";
		
		String sparqlClassesQuery = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
	       		+ "SELECT ?class ?label ?comment "
	       		+ "WHERE { "
	       		+ "    ?class a rdfs:Class ; "
	       		+ "    rdfs:label ?label ;"
	       		+ "    rdfs:comment ?comment . "
	       		+ "} ";

	    QueryExecution qexec = QueryExecution.service(endpoint).query(sparqlClassesQuery).build();

		ResultSet results = qexec.execSelect();
		while (results.hasNext()) {
	           QuerySolution qs = results.nextSolution();
	           AegeanClass newClass = new AegeanClass(qs.getResource("class").getURI().toString(), 
	        		   							      qs.getLiteral("comment").toString(), 
	        		   							      qs.getLiteral("label").toString());
	           totalClasses.add(newClass);

		}
		
		
		qexec.close();
		
		return totalClasses;
	}
	

	public static void sparqlGetClassInstances(AegeanClass aegeanCls) {
	
		String endpoint ="http://semantics.aegean.gr:3030/data";
		ArrayList <String> classInstances = new ArrayList<String>();
		String sparqlClassesQuery = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
					+ "PREFIX uoa: <http://semantics.aegean.gr/ontology/> "
					+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
					+ "PREFIX geosparql: <http://www.opengis.net/ont/geosparql#> "
		       		+ "SELECT distinct ?instance ?lbl ?wkt "
		       		+ "WHERE { "
		       		+ "    ?instance rdf:type uoa:"+ aegeanCls.getName() + " ."
		       		+ "    ?instance rdf:type geosparql:Feature ."
		       		+ "    ?instance geosparql:hasGeometry ?bn ."
		       		+ "    ?bn geosparql:asWKT ?wkt ."
		       		+ "    OPTIONAL{?instance rdfs:label ?lbl }"
		       		+ "Filter (contains(str(?wkt),'http://www.opengis.net/def/crs/EPSG/0/4326'))"
		       		+ "} ";
			

		QueryExecution qexec = QueryExecution.service(endpoint).query(sparqlClassesQuery).build();
		
		ResultSet results = qexec.execSelect();
		
		while (results.hasNext()) {
			QuerySolution qs = results.nextSolution();
			String lbl  = null;
			
			if(qs.getLiteral("lbl") != null) {
				lbl = qs.getLiteral("lbl").toString();
			}
			aegeanCls.addInstance(new AegeanInstance(qs.getResource("instance").getURI(),
					lbl, "Aegean", qs.getLiteral("wkt").toString()));
		}	
		qexec.close();
	}
	
	
	public static void sparqlInstanceProperties(AegeanClass aegeanCls) {
		
		String endpoint ="http://semantics.aegean.gr:3030/data";
		ArrayList <String> instanceProperties = new ArrayList<String>();
		String sparqlClassesQuery = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
					+ "PREFIX uoa: <http://semantics.aegean.gr/ontology/> "
					+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
					+ "PREFIX geosparql: <http://www.opengis.net/ont/geosparql#> "
		       		+ "SELECT distinct ?object ?property "
		       		+ "WHERE { "
		       		+ "    ?object ?property ?subject . "
		       		+ "    ?object rdf:type uoa:"+ aegeanCls.getName() + " ."
		       		+ "} LIMIT 10";
			

		QueryExecution qexec = QueryExecution.service(endpoint).query(sparqlClassesQuery).build();

		ResultSet results = qexec.execSelect();
		
		while (results.hasNext()) {
			QuerySolution qs = results.nextSolution();
			
			if(qs.getResource("property").getURI().toString().equals("http://www.w3.org/2000/01/rdf-schema#label")) {
				aegeanCls.setInstancesWithLabels(true);
			}
		}
		
		qexec.close();
	}
	
	
	public static void sparqlGetEquivalentClasses(AegeanClass acl) {
		String endpoint = "http://dbpedia.org/sparql";
		
		String[] nameParts = acl.getLabel().split(" ");
		
		for(String part : nameParts) {
		
			String sparqlGetClassQuery = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
					+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
					+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
		       		+ "SELECT distinct ?class "
		       		+ "WHERE { "
		       		+ "    ?class a owl:Class; "
		       		+ "         rdfs:label ?lbl . "
		       		+ " FILTER regex(?lbl, \"" + part + "\", \"i\") "
		       		+ "} ";		
	
		    QueryExecution qexec = QueryExecution.service(endpoint).query(sparqlGetClassQuery).build();

			ResultSet results = qexec.execSelect();
	
			while (results.hasNext()) {
		           QuerySolution qs = results.nextSolution();
		           acl.addCandidateClass(qs.getResource("class").getURI().toString());
			}
			qexec.close();
		}
	}
	
//	public static int sparqlCountDBpediaClasses() {
//		String endpoint = "http://dbpedia.org/sparql";
//		
//		String sparqlGetClassQuery = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
//			+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
//			+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
//		    + "SELECT COUNT(distinct ?class) WHERE {?s a ?class} ";		
//		//select  count distinct ?class where {?s a ?class} 
//		    QueryExecution qexec = QueryExecution.service(endpoint).query(sparqlGetClassQuery).build();
//
//			ResultSet results = qexec.execSelect();
//			int counter = -1;
//			if (results.hasNext()) {
//	            QuerySolution qs = results.nextSolution();
////	            System.out.println(qs.getLiteral("class").getString());
//	            counter = Integer.parseInt(qs.getLiteral("class").getString());
//	       
//	        }
//			qexec.close();
//			return counter;
//			
////			+ "SELECT distinct (COUNT(?subject) as ?counter) " 
////		    + "WHERE { "
////		    + "    ?subject a owl:Class; "
////		    + "         rdfs:label ?label . "
////		    + " FILTER(LANG(?label) = 'en' || LANG(?label) = 'el') "
////		    + "}";
//	}
	
	
	public static ArrayList<CandidateClass> sparqlGetDBpediaClasses(int limit, int offset) {
		String endpoint = "http://dbpedia.org/sparql";
		
		ArrayList<CandidateClass> candidates = new ArrayList<>();
//		System.out.println("Limit = " + limit + " OFFSET = " + offset);
		String sparqlGetClassQuery = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
			+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
			+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
			+ "PREFIX dbo: <http://dbpedia.org/ontology/> "
			+ "PREFIX dbr: <http://dbpedia.org/resource/> "
		    + "SELECT distinct ?class "
		    + "WHERE { "
		    + "    ?s a ?class . "
		    + "} LIMIT " + limit + " OFFSET " + offset;		
	
		    QueryExecution qexec = QueryExecution.service(endpoint).query(sparqlGetClassQuery).build();

			ResultSet results = qexec.execSelect();
	
			while (results.hasNext()) {
				QuerySolution qs = results.nextSolution();
			    String label  = null;
						
//				if(qs.getLiteral("label") != null) {
//					label = qs.getLiteral("label").toString();
//				}
			    candidates.add(new CandidateClass(qs.getResource("class").getURI().toString(), label));	           
			}
	
			qexec.close();
			return candidates;
	}
//	 + "WHERE { "
//	    + "    ?subject a owl:Class; "
//	    + "         rdfs:label ?label . "
//	    + " FILTER(LANG(?label) = 'en' || LANG(?label) = 'el') "
	
	public static ArrayList<CandidateInstance> sparqlGetDBpediaInstances(AegeanClass aClass, int limit, int offset) {
		String endpoint = "http://dbpedia.org/sparql";
		System.out.println("<" + aClass.getSameAsClass()+ ">");
		ArrayList<CandidateInstance> candidateInstances = new ArrayList<>();
		
		//try {

			String sparqlGetClassQuery = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
					+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
					+ "PREFIX dbp: <http://dbpedia.org/property/> "
					+ "PREFIX dbo: <http://dbpedia.org/ontology/> "
					+ "PREFIX dbr: <http://dbpedia.org/resource/> "
					+ "PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#> "
		       		+ "SELECT distinct ?uri ?label ?geometry "
		       		+ "WHERE { "
		       		+ "    ?uri a <" + aClass.getSameAsClass()+ "> ; "
		       		+ "         rdfs:label ?label ;"
		       		+ "         geo:lat ?lat ;"
		       		+ "         geo:long ?long ."
		       		+ " OPTIONAL { ?uri geo:geometry ?geometry}  "		       		
		       		+ " FILTER(LANG(?label) = 'en' || LANG(?label) = 'el') "
		       		+ " FILTER (?lat > 33 && ?lat < 42) "
		       		+ " FILTER (?long > 18 && ?long < 30) "
		       		+ " } LIMIT " + limit + " OFFSET " + offset;
			
//			+ "SELECT distinct ?uri ?label ?geometry "
//       		+ "WHERE { "
//       		+ "    ?uri a <" + aClass.getSameAsClass()+ "> ; "
//       		+ "         rdfs:label ?label . "
//       		+ "    OPTIONAL { ?uri dbo:country dbr:Greece } "
//       		+ "    OPTIONAL { ?uri geo:geometry ?geometry}  "		       		
//       		+ " FILTER(LANG(?label) = 'en' || LANG(?label) = 'el') "
//       		+ " } ";
			
//			+ "SELECT distinct ?uri ?label ?name ?geometry "
//       		+ "WHERE { "
//       		+ "    ?uri a <http://schema.org/Airport> ; "
//       		+ "         rdfs:label ?label ; "
//       		+ "         dbo:country dbr:Greece . "
//       		+ "         OPTIONAL { ?uri dbp:nameLocal ?name } . "
//       		+ "         OPTIONAL { ?uri  geo:geometry ?geometry}  "		       		
//       		+ " FILTER regex(?cl, \"" + aClass.getName() + "\", 'i') "
//       		+ " FILTER(LANG(?label) = 'en' || LANG(?label) = 'el') "
//       		+ " } ";
			
		    QueryExecution qexec = QueryExecution.service(endpoint).query(sparqlGetClassQuery).build();

			ResultSet results = qexec.execSelect();
			while (results.hasNext()) {
		           QuerySolution qs = results.nextSolution();
		           
		           String wkt  = null;
					
		           if(qs.getLiteral("geometry") != null) {
						wkt = qs.getLiteral("geometry").toString();
		           }
		           
		           String label  = null;
					
		           if(qs.getLiteral("label") != null) {
						label = qs.getLiteral("label").toString();
					}
		           candidateInstances.add(new CandidateInstance(qs.getResource("uri").getURI(), label, "DBpedia", wkt));
			}
			
		qexec.close();
		return candidateInstances;
	}
	
	
	public static double geometryDistance(AegeanInstance aegeanInstance, CandidateInstance candidateInstance) {
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
	    WKTReader reader = new WKTReader(geometryFactory);
	    Geometry geom1=null;
		Geometry geom2=null;
	    if(aegeanInstance.getType() != null && candidateInstance.getType() != null){
	    	try {
				geom1 =  reader.read(aegeanInstance.getWkt());
				geom2 =  reader.read(candidateInstance.getWkt());
				
				return (geom1.distance(geom2) * 111) / 0.001;
	
			} catch (ParseException e) {
				System.out.println("Unable to calculate the geometry distance for the instance " + candidateInstance.getLabel());
			}
	    }
	    
	    return -1.0;
	}
	
	public static int geometyIntersection(AegeanInstance aegeanInstance, CandidateInstance candidateInstance) {
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
	    WKTReader reader = new WKTReader(geometryFactory);
	    Geometry geom1=null;
		Geometry geom2=null;
	    if(aegeanInstance.getWkt() != null && candidateInstance.getWkt() != null){
	    	try {
				geom1 =  reader.read(aegeanInstance.getWkt());
				geom2 =  reader.read(candidateInstance.getWkt());
				
				if(geom1.intersects(geom2) == true) 
					return 1; // The two instances intersects 
				return 0;	// The two instances does not intersect 
			} catch (ParseException e) {
				System.out.println("Unable to calculate the geometry distance for the instance " + candidateInstance.getLabel());
				return -1;
			}
	    }
	    // We can not check if there is an intersection between instances
	    return -1;
	}
	
	
	public static void test(String aegeanInstance, String candidateInstance) {
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
	    WKTReader reader = new WKTReader(geometryFactory);
	    Geometry geom1=null;
		Geometry geom2=null;
	    	try {
				geom1 =  reader.read(aegeanInstance);
				geom2 =  reader.read(candidateInstance);
				
				if(geom1.intersects(geom2) == true) 
					System.out.println("true - 1"); // The two instances intersects 
				else {
					System.out.println((geom1.distance(geom2)* 111) / 0.001);
					System.out.println("false - 0");;	// The two instances does not intersect 
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
	}

	public static void main(String[] args) throws IOException {
		ArrayList <AegeanClass> aegeanClasses = new ArrayList<AegeanClass>();
		ArrayList <CandidateClass> candidateClasses = new ArrayList<>();
		ArrayList <AegeanInstance> aegeanInstances = new ArrayList<AegeanInstance>();
		ArrayList <CandidateInstance> candidateInstances = new ArrayList<>();
		
		int distanceThreshold = 30000; // threshold in meters
		double similarityThreshold = 0.4;
		double classSimilarityThreshold = 0.40;
		int counter = 0;
		// Create an empty Model
        Model model = ModelFactory.createDefaultModel();
        
        
        /*
         * For each class fetched by the Aegean SPARQL endpoint 
         * find candidate classes from dbpedia SPARQL endpoint that can be assigned as sameAs classes.
         * */
		int numberOfDBclasses;
		int limit = 10000;
		int offset = 0;
        
   	 	try {
	   	 	System.out.println("Please wait. Data from Aegean University endpoint are fetched..");
	   	 	aegeanClasses = sparqlGetAllClasses();
	   	 	System.out.println(aegeanClasses.size() + " classes were fetched from Aegean University.");
   	 		
        	System.out.println("Please wait. Data from DBpedia endpoint are fetched..");
        	while(true) {
        		ArrayList<CandidateClass> list = sparqlGetDBpediaClasses(limit, offset);
        		candidateClasses.addAll(list);
            	offset += limit;
            	if(list.size() < limit) break;
            	
        	}
        	System.out.println(candidateClasses.size() + " classes where fetched from DBpedia.");   
        	
        }catch (Exception ex) {
            System.err.println(ex);
        }
        System.out.println("Please wait. The matching procedure is in progress..");
        findEquivalentClasses(aegeanClasses, candidateClasses, model, classSimilarityThreshold);
        exportToTTL(model, "N-Triples", "classResults.ttl");
        
        /*
         * For each instance fetched by the Aegean SPARQL endpoint 
         * find candidate instances from dbpedia SPARQL endpoint that can be assigned as sameAs instance.
         * */
        
        try {
        	// For each class of the Aegean SPARQL endpoint the relevant instances are fetched
        	for(AegeanClass acl: aegeanClasses) {
        		if(acl.getSameAsClass() != null) {
        			Model instanceModel = ModelFactory.createDefaultModel();
//            		System.out.println("Instances of class " + acl.getURI() + " are fetched...");
            		sparqlGetClassInstances(acl);
            		aegeanInstances = acl.getInstances();
            		
            		limit = 10000;
            		offset = 0;
            		// καθάρισμα του ArrayList 
            		candidateInstances.clear();
            		ArrayList<CandidateInstance> instanceList;
            		while(true) {
                		instanceList = sparqlGetDBpediaInstances(acl, limit, offset);
                		candidateInstances.addAll(instanceList);
                    	offset += limit;
                    	if(instanceList.size() < limit) break;
                    	instanceList.clear();
                	}
//                	candidateInstances = sparqlGetDBpediaInstances(acl);
                	if(candidateInstances.size()>0) {
	                    counter += findEquivalentInstances(aegeanInstances, candidateInstances, instanceModel, distanceThreshold, similarityThreshold);
                	}
                	if(instanceModel.size() > 0)
                		exportToTTL(instanceModel, "N-Triples", acl.getName() + "MatchedInstances.ttl");
        		}
        	}
        	System.out.println(counter + " instances were matched");
        }
        catch (Exception ex) {
            System.err.println(ex);
        }        
    }	       
}
