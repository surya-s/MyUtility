package com.core.bean;

public class PLSQLObject {
	String moduleName;//MODULE_NAME
	String procedureName; //PROC
	String packageName; //PACKAGE_NAME
	String hookType; //TYPE
	String hookName;//HOOK_NAME
	
	long noOfLines;
	boolean commitExists;
	boolean rollbackExists;
	boolean savepointExists;
	boolean xnErrorCodeHandled;
	
	public String getModuleName() {
		return moduleName;
	}
	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}
	public String getProcedureName() {
		return procedureName;
	}
	public void setProcedureName(String procedureName) {
		this.procedureName = procedureName;
	}
	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	public String getHookType() {
		return hookType;
	}
	public void setHookType(String hookType) {
		this.hookType = hookType;
	}
	public String getHookName() {
		return hookName;
	}
	public void setHookName(String hookName) {
		this.hookName = hookName;
	}
	public long getNoOfLines() {
		return noOfLines;
	}
	public void setNoOfLines(long noOfLines) {
		this.noOfLines = noOfLines;
	}
	public boolean isCommitExists() {
		return commitExists;
	}
	public void setCommitExists(boolean commitExists) {
		this.commitExists = commitExists;
	}
	public boolean isRollbackExists() {
		return rollbackExists;
	}
	public void setRollbackExists(boolean rollbackExists) {
		this.rollbackExists = rollbackExists;
	}
	public boolean isSavepointExists() {
		return savepointExists;
	}
	public void setSavepointExists(boolean savepointExists) {
		this.savepointExists = savepointExists;
	}
	public boolean isXnErrorCodeHandled() {
		return xnErrorCodeHandled;
	}
	public void setXnErrorCodeHandled(boolean xnErrorCodeHandled) {
		this.xnErrorCodeHandled = xnErrorCodeHandled;
	}
	
	public String toString(){
		return this.hookName;
	}
}
