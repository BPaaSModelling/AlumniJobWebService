package ch.fhnw.alumni;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import com.google.gson.Gson;

import ch.fhnw.alumni.ontology.TripleStoreManager;
import ch.fhnw.exception.NoResultsException;
import ch.fhnw.model.search.SearchResult;
import ch.fhnw.model.search.SearchResultsModel;

@Path("/search")
public class SearchEndpoint {

	private TripleStoreManager tripleStoreManager = TripleStoreManager.getInstance();
	private Gson gson = new Gson();

		@GET
		public Response search(@QueryParam("ns") String namespace, @QueryParam("search") String search) {
			System.out.println("\n####################<start>####################");
			System.out.println("/search received values: ns " + namespace + " :: " + search);
			System.out.println("####################<end>####################");

			// split keywords spaces spaces
			String[] searchItems = search.split("\\s+");

			SearchResultsModel searchResults = null;
			
			try {
				searchResults = query(namespace, searchItems);
			} catch (NoResultsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			String json = gson.toJson(searchResults);
			System.out.println("\n####################<start>####################");
			System.out.println("/search genereated json: " +json);
			System.out.println("####################<end>####################");
			return Response.status(Status.OK).entity(json).build();
		}

		private SearchResultsModel query(String namespace, String[] searchItems) throws NoResultsException {
			ParameterizedSparqlString queryStr = new ParameterizedSparqlString();

			queryStr.append("SELECT ?subclass ?label WHERE {");
			queryStr.append("?subclass rdfs:subClassOf* <" + namespace + "> .");
			queryStr.append("?subclass rdfs:label ?label .");
			queryStr.append("FILTER(?subclass!=<" + namespace +">)");

			for (String param : searchItems) {
				queryStr.append("FILTER regex(str(?label), \"" + param + "\", \"i\")");
			}
			queryStr.append("}");

			//ResultSet results = tripleStoreManager.performSelectQuery(queryStr);
			QueryExecution queryExecution = tripleStoreManager.performSelectQuery(queryStr);
			ResultSet results = queryExecution.execSelect();
			SearchResultsModel sr = new SearchResultsModel();
			if (results.hasNext()) {
				while (results.hasNext()) {
					QuerySolution soln = results.next();
					sr.add(new SearchResult(soln.get("?subclass").toString(), soln.get("?label").toString()));
				}
				
			} else {
				throw new NoResultsException("nore more results");
			}
			queryExecution.close();
			return sr;
		}
	}
