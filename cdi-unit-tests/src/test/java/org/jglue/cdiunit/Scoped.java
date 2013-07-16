package org.jglue.cdiunit;

import org.apache.deltaspike.core.api.exclude.annotation.Exclude;




@Exclude
public class Scoped {

	private Runnable disposeListener;
	public Scoped() {
		
	}
	public void setDisposedListener(Runnable disposeListener) {
		this.disposeListener = disposeListener;
		
	}
	

	public void dispose() {
		disposeListener.run();
	}
}