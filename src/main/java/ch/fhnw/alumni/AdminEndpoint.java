package ch.fhnw.alumni;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import com.google.gson.Gson;

import ch.fhnw.alumni.ontology.TripleStoreManager;
import ch.fhnw.alumni.persistence.GlobalVariables;
import ch.fhnw.exception.NoResultsException;
import ch.fhnw.model.alumni.Answer;
import ch.fhnw.model.alumni.JobOfferElement;


@Path("/admin")
public class AdminEndpoint {
	
	private String class_type = "sjp:JobElement";
	private Gson gson = new Gson();
	private TripleStoreManager tripleStoreManager = TripleStoreManager.getInstance();
	@GET
	@Path("getJobElements")
	public Response getJobElements(){
		System.out.println("\n####################<start>####################");
		System.out.println("/requested parameters to generate a new Job offer" );
		System.out.println("####################<end>####################");
		
		ArrayList<JobOfferElement> result = new ArrayList<JobOfferElement>();
		
		try {
			result = queryRawJobElements();
		} catch (NoResultsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String json = gson.toJson(result);
		System.out.println("\n####################<start>####################");
		System.out.println("/search genereated json: " +json);
		System.out.println("####################<end>####################");
		return Response.status(Status.OK).entity(json).build();
		
	}

	private ArrayList<JobOfferElement> queryRawJobElements() throws NoResultsException {
		ParameterizedSparqlString queryStr = new ParameterizedSparqlString();
		ArrayList<JobOfferElement> allJobOfferElement = new ArrayList<JobOfferElement>();
		
		queryStr.append("SELECT ?field ?label ?qType ?searchnamespace ?datatype WHERE {");
		queryStr.append("?field rdf:type* " + class_type + " .");
		queryStr.append("?field rdfs:label ?label .");
		queryStr.append("?field rdf:type ?qType .");
		queryStr.append("?qType rdfs:subClassOf* sjp:AnswerType .");
		queryStr.append("OPTIONAL{");
		queryStr.append("?field sjp:ValueInsertTypeHasDataType ?datatype .");
		queryStr.append("}");
		queryStr.append("OPTIONAL{");
		queryStr.append("?field sjp:ModelHasSearchNamespace ?searchnamespace .");
		queryStr.append("}");
		queryStr.append("}");

		ResultSet results = tripleStoreManager.performSelectQuery(queryStr).execSelect();
		
		//I query comparison operators only one time and I store them in a temp array
		ArrayList<Answer> comparisonOperators;
		comparisonOperators = new ArrayList<Answer>(getComparisonOperations());
		
		if (results.hasNext()) {
			while (results.hasNext()) {
				JobOfferElement tempJobElement = new JobOfferElement();
				
				QuerySolution soln = results.next();
				tempJobElement.setPropertyURI(soln.get("?field").toString());
				tempJobElement.setPropertyLabel(soln.get("?label").toString());
				tempJobElement.setTypeOfAnswer(soln.get("?qType").toString());
				if (soln.get("?qType").toString().equals(GlobalVariables.ANSWERTYPE_SINGLE_SELECTION) || 
						soln.get("?qType").toString().equals(GlobalVariables.ANSWERTYPE_MULTI_SELECTION)){
					//Call for the answers
					tempJobElement.setAnswerList(new ArrayList<Answer>(getAnswerList(soln.get("?field").toString())));
				}else if (soln.get("?qType").toString().equals(GlobalVariables.ANSWERTYPE_SEARCH_SELECTION)){
					//Call for the namespace
					tempJobElement.setSearchNamespace(soln.get("?searchnamespace").toString());
				}else if (soln.get("?qType").toString().equals(GlobalVariables.ANSWERTYPE_VALUEINSERT)){
					//Call for the Datatype
					tempJobElement.setAnswerDatatype(soln.get("?datatype").toString());
					tempJobElement.setComparisonOperationAnswer(comparisonOperators);
				}
				
				allJobOfferElement.add(tempJobElement);
			}
		} else {
			throw new NoResultsException("nore more results");
		}
		return allJobOfferElement;
	}
	
	private Set<Answer> getComparisonOperations() {
		ParameterizedSparqlString queryStr = new ParameterizedSparqlString();
		queryStr.append("SELECT ?operation ?label WHERE {");
		queryStr.append("?operation rdf:type ?type .");
		queryStr.append("?type rdfs:subClassOf* sjp:LogicalOperators .");
		queryStr.append("?operation rdfs:label ?label .");
		queryStr.append("}");
		
		ResultSet results = tripleStoreManager.performSelectQuery(queryStr).execSelect();
		
		Set<Answer> comparisonOps = new HashSet<Answer>();

		while (results.hasNext()) {
			QuerySolution soln = results.next();
			comparisonOps.add(new Answer(soln.get("?operation").toString(), soln.get("?label").toString()));
		}
		
		return comparisonOps;
	}
	
	private Set<Answer> getAnswerList(String element_URI) {
		ParameterizedSparqlString queryStr = new ParameterizedSparqlString();
		queryStr.append("SELECT ?answer ?label WHERE {");
		queryStr.append("<"+element_URI+">" + " sjp:ModelHasAnswers ?answer .");
		queryStr.append("?answer rdfs:label ?label .");
		queryStr.append("}");
		
		ResultSet results = tripleStoreManager.performSelectQuery(queryStr).execSelect();
		
		Set<Answer> answers = new HashSet<Answer>();

		while (results.hasNext()) {
			QuerySolution soln = results.next();
			answers.add(new Answer(soln.get("?answer").toString(), soln.get("?label").toString()));
		}
		
		return answers;
	}
	
//	@POST
//	@Path("/addJobOffer")
//	public Response getMsg(String json) {
//		
//		System.out.println("/addJobOffer received: " +json);
//		
//		Gson gson = new Gson();
//		JobOfferModel jobOfferModel = gson.fromJson(json, JobOfferModel.class);
//		
//		String id = UUID.randomUUID().toString();
//		
//		ParameterizedSparqlString querStr = new ParameterizedSparqlString();
//		
//		querStr.append("INSERT DATA {");
//		querStr.append("sjp_data:" +id  +" rdf:type sjp:Job ;");
//		querStr.append("rdfs:label \"" +jobOfferModel.getJobOfferName() +"\" ;");
//		querStr.append("}");
//		
//		tripleStoreManager.performUpdateQuery(querStr);
//		
//		
////		String newJson = gson.toJson(jobOfferModel);
//		
//		return Response.status(Status.OK).entity("{}").build();
//
//	}
//	
//	@GET
//	@Path("/getJobOffer")
//	public Response getJobOffer() {
//		
//		System.out.println("/getJobOffer was triggered: ");
//		
//		ParameterizedSparqlString querStr = new ParameterizedSparqlString();
//		
//		querStr.append("SELECT * WHERE{");
//		querStr.append("?jo rdf:type sjp:Job .");
//		querStr.append("?jo rdfs:label ?label .");
//		querStr.append("}");
//		
//		System.out.println("this is what I executed: " +querStr.toString());
//		
//		QueryExecution queryExecution = tripleStoreManager.performSelectQuery(querStr);
//		ResultSet results = queryExecution.execSelect();
//		
//		Set<JobOfferModel> tempSet = new HashSet<JobOfferModel>();
//		
//		while (results.hasNext()) {
//			QuerySolution soln = results.next();
//			tempSet.add(new JobOfferModel(soln.get("?jo").toString(), soln.get("?label").toString()));
//		}
//		queryExecution.close();
//		
//		Gson gson = new Gson();
//		String newJson = gson.toJson(tempSet);
//		
//		
//		System.out.println("this is what I sent: " +newJson);
//
//		return Response.status(Status.OK).entity(newJson).build();
//
//	}
	
	
	
	

}
