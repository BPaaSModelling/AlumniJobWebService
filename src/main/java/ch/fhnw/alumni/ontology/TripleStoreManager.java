package ch.fhnw.alumni.ontology;

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

public class TripleStoreManager {
	private static TripleStoreManager INSTANCE;
	private static String TRIPLESTOREENDPOINT 	= "http://localhost:3030/test1";
	private static String UPDATEENDPOINT 		= TRIPLESTOREENDPOINT + "/update";
	private static String QUERYENDPOINT			= TRIPLESTOREENDPOINT + "/query";
	
	public static synchronized TripleStoreManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new TripleStoreManager();
		}
		return INSTANCE;
	}
	
	
	public TripleStoreManager(){
		
	}

	public void performUpdateQuery(ParameterizedSparqlString queryStr) {
		addNamespaces(queryStr);
		
		System.out.println("this is what I executed: " +queryStr.toString());
		
		UpdateRequest update = UpdateFactory.create(queryStr.toString());
		UpdateProcessor up = UpdateExecutionFactory.createRemote(update, UPDATEENDPOINT);
		up.execute();
	}

	private void addNamespaces(ParameterizedSparqlString queryStr) {
		queryStr.setNsPrefix("sjp_data", "http://ikm-group.ch/archimeo/sjp_data#");
		queryStr.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		queryStr.setNsPrefix("sjp", "http://ikm-group.ch/archimeo/sjp#");
		queryStr.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
	}

	public QueryExecution performSelectQuery(ParameterizedSparqlString queryStr) {
		addNamespaces(queryStr);
		Query query = QueryFactory.create(queryStr.toString());
		return QueryExecutionFactory.sparqlService(QUERYENDPOINT, query);
	}

}
