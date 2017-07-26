package ch.fhnw.model.alumni;

import java.util.ArrayList;


import ch.fhnw.model.alumni.JobOfferElement;

public class JobOfferModel {
	
private String name;
private String id;
private String label;
private ArrayList<JobOfferElement> properties;

public String getLabel() {


	return label;
}

public ArrayList<JobOfferElement> getProperties() {
	
	return properties;
}

public void setProperties(ArrayList<JobOfferElement> properties) {
	// TODO Auto-generated method stub
	this.properties = properties;
}


}
