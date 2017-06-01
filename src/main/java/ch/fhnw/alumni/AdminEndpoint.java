package ch.fhnw.alumni;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.gson.Gson;

import ch.fhnw.alumni.model.JobOfferModel;

@Path("/admin")
public class AdminEndpoint {
	
	@POST
	@Path("/addJobOffer")
	public Response getMsg(String json) {
		
		System.out.println("/addJobOffer received: " +json);
		
		Gson gson = new Gson();
		JobOfferModel jobOfferModel = gson.fromJson(json, JobOfferModel.class);
		
		System.out.println("the name: " +jobOfferModel.getJobOfferName());
		System.out.println("the type: " +jobOfferModel.getJobType());
		
		
		jobOfferModel.setJobOfferName("Engineer");
		jobOfferModel.setJobType("whatever");
		
		String newJson = gson.toJson(jobOfferModel);
		
		return Response.status(Status.OK).entity(newJson).build();

	}

}
