package dummyLib;

public class DummyLib {
	
	public DummyLib()
	{
		projectName = "defaultProjectName";
		projectID = 99;
	}
	
	public DummyLib(String _projectName, Integer _projectID)
	{
		projectName = _projectName;
		projectID = _projectID;
	}
	
	public String getProjectName()
	{
		return projectName;
	}
	
	public Integer getProjectID()
	{
		return projectID;
	}
	
	private String projectName;
	private Integer projectID;
}
