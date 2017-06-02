package ch.fhnw.alumni.model;

public class JobOfferModel {
	
	private String jobOfferName;
	private String jobType;
	private String jobOfferURI;

	public JobOfferModel(String jobOfferURI, String jobOfferName) {
		this.jobOfferURI = jobOfferURI;
		this.jobOfferName = jobOfferName;
	}

	public String getJobType() {
		return jobType;
	}

	public void setJobType(String jobType) {
		this.jobType = jobType;
	}

	public String getJobOfferName() {
		return jobOfferName;
	}

	public void setJobOfferName(String jobOfferName) {
		this.jobOfferName = jobOfferName;
	}

}
