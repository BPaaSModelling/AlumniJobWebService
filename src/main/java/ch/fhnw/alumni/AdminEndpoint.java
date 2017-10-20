package ch.fhnw.alumni;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import com.google.gson.Gson;

import ch.fhnw.alumni.ontology.TripleStoreManager;
import ch.fhnw.alumni.persistence.GlobalVariables;
import ch.fhnw.exception.NoResultsException;
import ch.fhnw.model.alumni.Answer;
import ch.fhnw.model.alumni.JobOfferElement;
import ch.fhnw.model.alumni.JobOfferModel;


@Path("/admin")
public class AdminEndpoint {
	private Gson gson = new Gson();
	private TripleStoreManager tripleStoreManager = new TripleStoreManager();
	private String class_type = "sjp:JobElement";
	
	
	@GET
	@Path("/getJobElements")
	public Response getJobElements(){
		System.out.println("\n####################<start>####################");
		System.out.println("/requested parameters to generate a new Job offer" );
		System.out.println("#################### <end> ####################");
		
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
		
		queryStr.append("SELECT ?field ?label ?qType ?searchnamespace ?datatype ?searchType WHERE {");
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
		queryStr.append("OPTIONAL{");
		queryStr.append("?field sjp:searchOnInstancesInsteadOfClasses ?searchType .");
		queryStr.append("}");
		queryStr.append("}");
		
		QueryExecution queryExecution = tripleStoreManager.performSelectQuery(queryStr);
		ResultSet results = queryExecution.execSelect();
		
		
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
					//Call for the searchType (Classes of Instances)
					if (soln.get("?searchType").toString() == null ||
						soln.get("?searchType").toString().equals("") ||
						soln.get("?searchType").toString().equals(GlobalVariables.BOOLEAN_FALSE_URI)){
						tempJobElement.setSearchOnInstancesInsteadOfClasses(false);
					} else {
						tempJobElement.setSearchOnInstancesInsteadOfClasses(true);
					}
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
		queryExecution.close();
		return allJobOfferElement;
	}
	
	private Set<Answer> getComparisonOperations() {
		ParameterizedSparqlString queryStr = new ParameterizedSparqlString();
		queryStr.append("SELECT ?operation ?label WHERE {");
		queryStr.append("?operation rdf:type ?type .");
		queryStr.append("?type rdfs:subClassOf* sjp:LogicalOperators .");
		queryStr.append("?operation rdfs:label ?label .");
		queryStr.append("}");
		
		QueryExecution queryExecution = tripleStoreManager.performSelectQuery(queryStr);
		ResultSet results = queryExecution.execSelect();
		
		Set<Answer> comparisonOps = new HashSet<Answer>();

		while (results.hasNext()) {
			QuerySolution soln = results.next();
			comparisonOps.add(new Answer(soln.get("?operation").toString(), soln.get("?label").toString()));
		}
		queryExecution.close();
		return comparisonOps;
	}
	
	private Set<Answer> getAnswerList(String element_URI) {
		ParameterizedSparqlString queryStr = new ParameterizedSparqlString();
		queryStr.append("SELECT ?answer ?label WHERE {");
		queryStr.append("<"+element_URI+">" + " sjp:ModelHasAnswers ?answer .");
		queryStr.append("?answer rdfs:label ?label .");
		queryStr.append("}");
		
		//ResultSet results = tripleStoreManager.performSelectQuery(queryStr);
		QueryExecution queryExecution = tripleStoreManager.performSelectQuery(queryStr);
		ResultSet results = queryExecution.execSelect();
		Set<Answer> answers = new HashSet<Answer>();

		while (results.hasNext()) {
			QuerySolution soln = results.next();
			answers.add(new Answer(soln.get("?answer").toString(), soln.get("?label").toString()));
		}
		queryExecution.close();
		return answers;
	}
	
	@POST
@Path("/addjo")
public Response getMsg(String json) {
	
	System.out.println("/joModel received: " +json);
	
	Gson gson = new Gson();
	JobOfferModel jom = gson.fromJson(json, JobOfferModel.class);
	
	String id = UUID.randomUUID().toString();
	//Here i remove all the elements (attributes) that does not have a valid value
	jom.setProperties(removeEmptyElements(jom.getProperties()));
	
	//Here i set all the AnnotationRelation properties
	jom.setProperties(addAnnotationRelations(jom.getProperties()));
	
	ParameterizedSparqlString querStr = new ParameterizedSparqlString();
	
	querStr.append("INSERT DATA{");
	querStr.append("sjp:JobOffer" + "JobOffer"+id  +" rdf:type sjp:JobOffer ;");
	System.out.println("    JobOffer ID: "+ "JobOffer"+id);
	querStr.append("rdfs:label \"" + jom.getLabel() +"\" ;");
	System.out.println("    JobOffer Label: "+ jom.getLabel());
	for (int i = 0; i < jom.getProperties().size(); i++){
		if (jom.getProperties().get(i).getTypeOfAnswer().equals(GlobalVariables.ANSWERTYPE_VALUEINSERT)){
			querStr.append("<" + jom.getProperties().get(i).getAnnotationRelation() +"> \"" + jom.getProperties().get(i).getComparisonAnswer().trim() + "\"^^<" + jom.getProperties().get(i).getAnswerDatatype() + ">" +" ;");
			System.out.println("    "+jom.getProperties().get(i).getPropertyLabel() + ": " + jom.getProperties().get(i).getComparisonAnswer().trim());
		} else {
			if (jom.getProperties().get(i).getGivenAnswerList() != null){
			for (int j = 0; j < jom.getProperties().get(i).getGivenAnswerList().size(); j++){
				querStr.append("<" + jom.getProperties().get(i).getAnnotationRelation() +"> " + "<" + jom.getProperties().get(i).getGivenAnswerList().get(j).getAnswerID().trim() +"> ;");
				System.out.println("    "+jom.getProperties().get(i).getPropertyLabel() + ": " + jom.getProperties().get(i).getGivenAnswerList().get(j).getAnswerLabel().trim());
			}
			}
		}
	}

	querStr.append("}");
	//Model modelTpl = ModelFactory.createDefaultModel();
	tripleStoreManager.insertQuery(querStr);

	return Response.status(Status.OK).entity("{}").build();
}

	private ArrayList<JobOfferElement>addAnnotationRelations(ArrayList<JobOfferElement> elements){
		ParameterizedSparqlString queryStr = new ParameterizedSparqlString();
		queryStr.append("SELECT ?relation ?element WHERE {");
		
		queryStr.append("?element rdf:type " + class_type + ".");
		queryStr.append("?element questionnaire:questionHasAnnotationRelation ?relation .");
		queryStr.append("}");
		
		QueryExecution qexec = tripleStoreManager.performSelectQuery(queryStr);
		ResultSet results = qexec.execSelect();

		while (results.hasNext()) {
			QuerySolution soln = results.next();
			for (int i = 0; i < elements.size(); i++){
				if (elements.get(i).getPropertyURI().equals(soln.get("?element").toString())){
					elements.get(i).setAnnotationRelation(soln.get("?relation").toString());
				}
			}
		}
		qexec.close();
		return elements;

	}

	private ArrayList<JobOfferElement> removeEmptyElements(ArrayList<JobOfferElement> elements){
	ArrayList<JobOfferElement> temp = new ArrayList<JobOfferElement>();
	
		for (int i = 0; i < elements.size(); i++){
			if (elements.get(i).getTypeOfAnswer().equals(GlobalVariables.ANSWERTYPE_VALUEINSERT) &&
					elements.get(i).getComparisonAnswer() != null &&
					!elements.get(i).getComparisonAnswer().trim().equals("")){
				temp.add(elements.get(i));
			}
			else if (elements.get(i).getGivenAnswerList().size() != 0){
				if (elements.get(i).getGivenAnswerList().size() == 1 &&
					!elements.get(i).getGivenAnswerList().get(0).getAnswerID().equals("")){
					temp.add(elements.get(i));
					//System.out.println("Validated - "+elements.get(i).getPropertyLabel());
				}
			}
		}
		
		return temp;
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
