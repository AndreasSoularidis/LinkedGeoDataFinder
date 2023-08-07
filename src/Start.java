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
	

	public static void findEquivalentClasses(ArrayList<AegeanClass> aegeanClasses, Model model) {
        int matches = 0;
        for(AegeanClass acl: aegeanClasses) {
        	ArrayList<String> candidates = acl.getCandidates();
        	// Calculate the Levenshtein Similarity for each one of the candidate classes
        	for(String candidate : candidates) {
        		String candidateName = acl.extractName(candidate);
        		acl.addSimilarity(LevenshteinSimilarity(acl.getName(), candidateName));
        	}
        	
        	// Find the class from all the candidate classes with the greatest score of similarity
        	ArrayList<Double> similarities = acl.getSimilarities();
        	int maxIndex = acl.findMaxSimilarity(); 
        	if(maxIndex == -1) // A similar class was not found
        		continue;
        	
        	// For each class we set as sameAs class, the candidate class that has similarity greater than 0.90
        	if(similarities.get(maxIndex) >= 0.90) {
        		acl.setSameAsClass(candidates.get(maxIndex));
        		// System.out.println(acl.getName() + " sameAs " + candidates.get(maxIndex) + " with similarity equals to " + similarities.get(maxIndex));
        		matches +=1;
        		
        		// Create the resources
        		Resource newResource = model.createResource(acl.getURI());
            	Resource equivalentResource = model.createResource(acl.getSameAsClass());
            	newResource.addProperty(OWL.sameAs, equivalentResource);
//            	newResource.addProperty(SKOS.exactMatch, equivalentResource);
        	}
        }
        System.out.println("Total matches " + matches);
	}
	
	
	public static void findEquivalentInstances(ArrayList<AegeanInstance> aegeanInstances, ArrayList<CandidateInstance>candidateInstances,
		Model model, int distanceThreshold) {
		try {
	        for(AegeanInstance aegeanInstance: aegeanInstances) {
	        	// Calculate the Levenshtein Similarity for each one of the candidate instances
	        	for(CandidateInstance candidateInstance : candidateInstances) {
//	        		System.out.println(aegeanInstance.getLabel() + " " + candidateInstance.getLabel());
	        		if(aegeanInstance.getLabel() != null && candidateInstance.getLabel() != null)
	        			candidateInstance.setSimilarity(LevenshteinSimilarity(aegeanInstance.getLabel(), candidateInstance.getLabel()));
	        		else
	        			candidateInstance.setSimilarity(LevenshteinSimilarity(aegeanInstance.getName(), candidateInstance.getName()));
	        		
	        	}
	        	
	        	// Find the instance from all the candidate instances with the greatest score of similarity
	    		CandidateInstance bestMachingInstance = candidateInstances.get(0);
	    		double maxSimilarity = candidateInstances.get(0).getSimilarity();
	    		for(CandidateInstance candidate : candidateInstances) {
	    			if(candidate.getSimilarity() > maxSimilarity) {
	    				maxSimilarity = candidate.getSimilarity();
	    				bestMachingInstance = candidate;
	    			}
	    		}

	        	// ΕΛΕΓΧΟΣ ΜΕ ΣΥΝΤΕΤΑΓΜΕΝΕΣ
	    		if(bestMachingInstance.getWkt() != null) {
	    			double distance = 0;
		        	double intersection = 0;
		        	if(aegeanInstance.getType().equals("POINT") && bestMachingInstance.getType().equals("POINT")) 
		        		distance = geometyDistance(aegeanInstance, bestMachingInstance);  		
		        	else { 
		        		distance = geometyDistance(aegeanInstance, bestMachingInstance); 
		        		intersection = geometyIntersection(aegeanInstance, bestMachingInstance);
		        	}
		        	if(distance > distanceThreshold && intersection == 0)
		        		continue;
	    		}
	    		if(bestMachingInstance.getWkt() == null && bestMachingInstance.getSimilarity() < 0.9)
	    			continue;
	        	
	        	// For each instance we set as sameAs instance, the candidate instance that has similarity greater than 0.90
	        	aegeanInstance.setSameAs(bestMachingInstance.getURI());
	        	
	        	// Create the resources
	        	Resource newResource = model.createResource(aegeanInstance.getURI());
	            Resource equivalentResource = model.createResource(bestMachingInstance.getURI());
	            newResource.addProperty(OWL.sameAs, equivalentResource);
	        }
       }catch(TopologyException ex) {
    	   System.out.println("An error occured " + ex.getMessage());
       }
		catch(NullPointerException ex) {
    	   System.out.println("An error occured " + ex.getMessage());
       }
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
		
		System.out.println(totalClasses.size() + " classes were fetched in total");
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
		       		+ "    ?class a owl:Class ; "
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
	
	
	public static ArrayList<CandidateInstance> sparqlGetDBpediaInstances(AegeanClass aClass) {
		String endpoint = "http://dbpedia.org/sparql";
		
		ArrayList<CandidateInstance> candidateInstances = new ArrayList<>();
		
		//try {

			String sparqlGetClassQuery = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
					+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
					+ "PREFIX dbp: <http://dbpedia.org/property/> "
					+ "PREFIX dbo: <http://dbpedia.org/ontology/> "
					+ "PREFIX dbr: <http://dbpedia.org/resource/> "
					+ "PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#> "
		       		+ "SELECT distinct ?uri ?label ?name ?geometry "
		       		+ "WHERE { "
		       		+ "    ?uri a ?cl ; "
		       		+ "         rdfs:label ?label ; "
		       		+ "         dbo:country dbr:Greece . "
		       		+ "         OPTIONAL { ?uri dbp:nameLocal ?name } . "
		       		+ "         OPTIONAL { ?uri  geo:geometry ?geometry}  "		       		
		       		+ " FILTER regex(?cl, \"" + aClass.getName() + "\", 'i') "
		       		+ " FILTER(LANG(?label) = 'en' || LANG(?label) = 'el') "
		       		+ " } ";
			
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
			
//		}catch(NullPointerException ex) {
//			System.out.println("1Unable to fetch data from DBpedia for instance " + instance.getURI() + "because of an exception!");
//		}catch(PatternSyntaxException ex) {
//			System.out.println("2Unable to fetch data from DBpedia for instance " + instance.getURI() + "because of an exception!");
//		}
//		catch(ExprEvalException ex) {
//			System.out.println("3Unable to fetch data from DBpedia for instance " + instance.getURI() + "because of an exception!");
//		}catch(Exception ex) {
//			System.out.println("4Unable to fetch data from DBpedia for instance " + instance.getURI() + "because of an exception!");
//		}
		qexec.close();
		return candidateInstances;
	}
	
	
	public static double geometyDistance(AegeanInstance aegeanInstance, CandidateInstance candidateInstance) {
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
				e.printStackTrace();
			}
	    }
	    
	    return -1.0;
	}
	
	public static int geometyIntersection(AegeanInstance aegeanInstance, CandidateInstance candidateInstance) {
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
	    WKTReader reader = new WKTReader(geometryFactory);
	    Geometry geom1=null;
		Geometry geom2=null;
	    if(aegeanInstance.getType() != null && candidateInstance.getType() != null){
	    	try {
				geom1 =  reader.read(aegeanInstance.getWkt());
				geom2 =  reader.read(candidateInstance.getWkt());
				
				if(geom1.intersects(geom2) == true) 
					return 1; // The two instances intersects 
				return 0;	// The two instances does not intersect 
			} catch (ParseException e) {
				e.printStackTrace();
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
//        String s1 = "POLYGON ((19.521269769706763 39.778610674978324, 19.533293574203686 39.766566888942734, 19.53641063216305 39.75916776962544, 19.52311995606503 39.758952067491656, 19.507983093368797 39.783562212133596, 19.521269769706763 39.778610674978324))";
//		String s2 = "POINT(19.516666412354 39.766666412354)";
//		test(s1, s2);
		ArrayList <AegeanClass> aegeanClasses = new ArrayList<AegeanClass>();
		ArrayList <AegeanInstance> aegeanInstances = new ArrayList<AegeanInstance>();
		ArrayList <CandidateInstance> candidateInstances = new ArrayList<CandidateInstance>();
		
		int distanceThreshold = 5000; // threshols in meters
		
		// Create an empty Model
        Model model = ModelFactory.createDefaultModel();
        Model instanceModel = ModelFactory.createDefaultModel();
        
        /*
         * For each class fetched by the Aegean SPARQL endpoint 
         * find candidate classes from dbpedia SPARQL endpoint that can be assigned as sameAs classes.
         * */
		
        try {
        	aegeanClasses = sparqlGetAllClasses();
        	
        	for(AegeanClass acl : aegeanClasses) {
        		sparqlGetEquivalentClasses(acl);
        	}      	
        }
        catch (Exception ex) {
            System.err.println(ex);
        }
        
        findEquivalentClasses(aegeanClasses, model);
        
        /*
         * For each instance fetched by the Aegean SPARQL endpoint 
         * find candidate instances from dbpedia SPARQL endpoint that can be assigned as sameAs instance.
         * */
        
        try {
        	// For each class of the Aegean SPARQL endpoint the relevant instances are fetched
        	for(AegeanClass acl: aegeanClasses) {
//        		if(acl.getSameAsClass() != null) {
            		sparqlInstanceProperties(acl);
            		if(acl.getInstancesWithLabels() == true) {
            			System.out.println("Instances of class " + acl.getURI() + " are fetching...");
            			sparqlGetClassInstances(acl);
                		aegeanInstances = acl.getInstances();
                		System.out.println("For class " + acl.getURI() + " " + aegeanInstances.size() + " are fetched from Aegean Endpoint!");
                		candidateInstances = sparqlGetDBpediaInstances(acl);
                		System.out.println("For class " + acl.getURI() + " " + candidateInstances.size() + " are fetched from DBpedia Endpoint!");
                		if(candidateInstances.size()>0) {
	                		System.out.println("Equivalent instances of class " + acl.getName() + " are searched..");
	                    	findEquivalentInstances(aegeanInstances, candidateInstances, instanceModel, distanceThreshold);
                		}else {
                			System.out.println("No instances are fetched from DBpedia for class " + acl.getURI());
                		}
                	}
//        		}

        		Thread.sleep(10000);
        	}
        }
        catch (Exception ex) {
            System.err.println(ex);
        }
        
        
        exportToTTL(model, "N-Triples", "classResults.ttl");
        exportToTTL(instanceModel, "N-Triples", "instanceResults.ttl");
    }	       

}
