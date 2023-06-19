import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;




public class Start {
	
	public static ArrayList<String> sparqlGetAllClasses() {
		ArrayList <String> totalClasses = new ArrayList<String>(); 
		String endpoint ="http://semantics.aegean.gr:3030/data";
		String sparqlClassesQuery = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
	       		+ "SELECT distinct ?class "
	       		+ "WHERE { "
	       		+ "    ?class a rdfs:Class "
	       		+ "} LIMIT 1";

	    QueryExecution qexec = QueryExecution.service(endpoint).query(sparqlClassesQuery).build();
		ResultSet results = qexec.execSelect();
		while (results.hasNext()) {
	           QuerySolution qs = results.nextSolution();
	           totalClasses.add(qs.getResource("class").getURI());
		}
		
		System.out.println(totalClasses.size() + " classes were fetched in total");
		
		return totalClasses;
	}
	

	public static void sparqlGetInstancesOfClasses() {
		/**
		 * Get an ArrayList of class (that were fetched from semantics.aegean endpoint) 
		 * and for each of them gets all the instaces from the same endpoint
		 * 
		 */
		Dictionary<String, ArrayList<String>> instances = new Hashtable<>();
		
		
		
		String[] classNames = {"Island", "Volcano"};
		
		String endpoint ="http://semantics.aegean.gr:3030/data";
		for(String className : classNames) {
			
			ArrayList <String> classInstances = new ArrayList<String>(); 
			String sparqlClassesQuery = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
					+ "PREFIX uoa: <http://semantics.aegean.gr/ontology/> "
					+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
		       		+ "SELECT ?instance "
		       		+ "WHERE { "
		       		+ "    ?instance rdf:type uoa:" + className
		       		+ "} LIMIT 5";
			

		    QueryExecution qexec = QueryExecution.service(endpoint).query(sparqlClassesQuery).build();
			ResultSet results = qexec.execSelect();
			while (results.hasNext()) {
		           QuerySolution qs = results.nextSolution();
		           classInstances.add(qs.getResource("instance").getURI());
			}
			
			instances.put(className, classInstances);

		
		}
		
		System.out.println("Results");
		Enumeration<String> k = instances.keys();
        while (k.hasMoreElements()) {
            String key = k.nextElement();
            System.out.println(key);
            ArrayList<String> test = instances.get(key);
            for(String name: test) {
            	System.out.println(name);
            }
        }
		
		
	}
	
	public static void sparqlGetClass(String eageanClasses) {
		String endpoint = "http://dbpedia.org/sparql";
		
		String[] strParts = eageanClasses.split("/");
		String className = strParts[strParts.length-1];
		className = className.toLowerCase();
		
		String sparqlGetClassQuery = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
	       		+ "SELECT distinct ?obj "
	       		+ "WHERE { "
	       		+ "    ?obj a owl:Class; "
	       		+ "    ?obj     rdfs:label \""+className+"\"@en "
	       		+ "         ?rdfs:label ?o "
	       		+ "} ";

	    QueryExecution qexec = QueryExecution.service(endpoint).query(sparqlGetClassQuery).build();
		ResultSet results = qexec.execSelect();
		while (results.hasNext()) {
	           QuerySolution qs = results.nextSolution();
	           System.out.println(qs.getResource("obj").getURI());
		}
	}
	
	

	public static void main(String[] args) throws IOException {
        
		ArrayList <String> aegeanClasses = new ArrayList<String>(); 
        try {
        	//aegeanClasses = sparqlGetAllClasses();
        	sparqlGetInstancesOfClasses();
        }
        catch (Exception ex) {
            System.err.println(ex);
        }
        
//        sparqlGetClass("http://semantics.aegean.gr/ontology/Island");
        
    }	       

}
