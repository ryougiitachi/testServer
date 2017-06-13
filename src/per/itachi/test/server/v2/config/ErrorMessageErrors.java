package per.itachi.test.server.v2.config;

import java.util.ArrayList;
import java.util.List;

public class ErrorMessageErrors {
	
	private List<ErrorMessageError> errors;
	
	public ErrorMessageErrors() {
		errors = new ArrayList<>(32);
	}
	
	public ErrorMessageError getError(int index) {
		return errors.get(index);
	}

	public void addError(ErrorMessageError error) {
		errors.add(error);
	}
	
	public int countError() {
		return errors.size();
	}
}
