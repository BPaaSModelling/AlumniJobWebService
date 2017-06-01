package ch.fhnw.inspire;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.gson.Gson;

import ch.fhnw.inspire.models.Question;

@Path("/admin")
public class AdminEndpoint {
	
	private Gson gson = new Gson();
	
	@POST
	@Path("/question/add")
	public Response getQuestionToAdd(String json) {
		
		System.out.println("I received: " +json +"\n\n");
		
		try{
			Question question = gson.fromJson(json, Question.class);
			question.setTestVariable("cooooooooool");
			
			String toBeSent = gson.toJson(question);
			
			
			return Response.status(Status.OK).entity(toBeSent).build();
			
		}catch (com.google.gson.JsonSyntaxException e){
			System.err.println("could not convert");
			
			return Response.status(Status.OK).entity("sorry, no chance").build();
		}
		
		
	}
	
	

}
