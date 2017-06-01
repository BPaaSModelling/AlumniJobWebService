package ch.fhnw.inspire.models;

public class Question {
	
	private String questionLabel;
	private String questionType;
	private String testVariable;
	
	public Question(){
		
	}

	public String getQuestionLabel() {
		return questionLabel;
	}

	public void setQuestionLabel(String questionLabel) {
		this.questionLabel = questionLabel;
	}
	
	public String getQuestionType() {
		return questionType;
	}

	public void setQuestionType(String questionType) {
		this.questionType = questionType;
	}

	public void setTestVariable(String testVariable) {
		this.testVariable = testVariable;
	}
	
	public String getTestVariable() {
		return this.testVariable;
	}

}
