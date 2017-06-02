package ch.fhnw.alumni;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import com.google.gson.Gson;
import ch.fhnw.alumni.model.JobOfferModel;
import ch.fhnw.alumni.ontology.TripleStoreManager;

@Path("/admin")
public class AdminEndpoint {
	
	
	private TripleStoreManager tripleStoreManager = new TripleStoreManager();
	
	
	@POST
	@Path("/addJobOffer")
	public Response getMsg(String json) {
		
		System.out.println("/addJobOffer received: " +json);
		
		Gson gson = new Gson();
		JobOfferModel jobOfferModel = gson.fromJson(json, JobOfferModel.class);
		
		String id = UUID.randomUUID().toString();
		
		ParameterizedSparqlString querStr = new ParameterizedSparqlString();
		
		querStr.append("INSERT DATA {");
		querStr.append("sjp_data:" +id  +" rdf:type sjp:Job ;");
		querStr.append("rdfs:label \"" +jobOfferModel.getJobOfferName() +"\" ;");
		querStr.append("}");
		
		tripleStoreManager.performUpdateQuery(querStr);
		
		
//		String newJson = gson.toJson(jobOfferModel);
		
		return Response.status(Status.OK).entity("{}").build();

	}
	
	@GET
	@Path("/getJobOffer")
	public Response getJobOffer() {
		
		System.out.println("/getJobOffer was triggered: ");
		
		ParameterizedSparqlString querStr = new ParameterizedSparqlString();
		
		querStr.append("SELECT * WHERE{");
		querStr.append("?jo rdf:type sjp:Job .");
		querStr.append("?jo rdfs:label ?label .");
		querStr.append("}");
		
		System.out.println("this is what I executed: " +querStr.toString());
		
		QueryExecution queryExecution = tripleStoreManager.performSelectQuery(querStr);
		ResultSet results = queryExecution.execSelect();
		
		Set<JobOfferModel> tempSet = new HashSet<JobOfferModel>();
		
		while (results.hasNext()) {
			QuerySolution soln = results.next();
			tempSet.add(new JobOfferModel(soln.get("?jo").toString(), soln.get("?label").toString()));
		}
		queryExecution.close();
		
		Gson gson = new Gson();
		String newJson = gson.toJson(tempSet);
		
		
		System.out.println("this is what I sent: " +newJson);

		return Response.status(Status.OK).entity(newJson).build();

	}
	
	
	
	

}
