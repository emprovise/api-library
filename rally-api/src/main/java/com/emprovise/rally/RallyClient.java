package com.emprovise.rally;

import com.emprovise.rally.exception.RallyConcurrencyConflictException;
import com.emprovise.rally.param.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.*;
import com.rallydev.rest.response.*;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;
import com.rallydev.rest.util.Ref;
import org.apache.commons.lang.StringUtils;
import org.apache.http.ParseException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.emprovise.rally.param.Param.*;
import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.trim;

public class RallyClient {
	
	/**
	 * Rally Server Host URL.
	 */
	public static final String RALLY_HOST = "https://rally1.rallydev.com";

	/**
	 * Rally url to search the User story or Defect ID by specifying the User story or Defect ID in place of '%s'.
	 */
	public static final String RALLY_SEARCH_URL = "https://rally1.rallydev.com/slm/rally.sp#/search?keywords=%s";

    private static final String WSAPI_VERSION = "v2.0";

    /**
	 * Create and configure a new instance of RallyRestApi
	 */
	private RallyDefaultRestApi restApi = null;

	/**
	 * Initializes the Rally API by establishing a connection with the Rally Server.
	 * @throws Exception
	 */
    public RallyClient(String rallyUser, String rallyPassword, URI proxyUri, String proxyUser, String proxyPassword) throws Exception {
		this.restApi = new RallyDefaultRestApi(new URI(RALLY_HOST), rallyUser, rallyPassword, proxyUri, proxyUser, proxyPassword);
        initializeRallyApi();
    }

    public RallyClient(String apiKey, URI proxyUri, String proxyUser, String proxyPassword) throws Exception {
        this.restApi = new RallyDefaultRestApi(new URI(RALLY_HOST), apiKey, proxyUri, proxyUser, proxyPassword);
        initializeRallyApi();
    }

    public RallyClient(String rallyUser, String rallyPassword) throws Exception {
        this.restApi = new RallyDefaultRestApi(new URI(RALLY_HOST), rallyUser, rallyPassword);
        initializeRallyApi();
    }

    public RallyClient(String apiKey) throws Exception {
        this.restApi = new RallyDefaultRestApi(new URI(RALLY_HOST), apiKey);
        initializeRallyApi();
    }

	/**
	 * Initializes the Rally-API by establishing a connection with the Rally Server.
	 * @throws Exception
	 */
	private void initializeRallyApi() throws Exception {

        this.restApi.setApplicationName("RallyApi");
        this.restApi.setApplicationVendor("Emprovise");
        this.restApi.setWsapiVersion(WSAPI_VERSION);
	}

    /**
     * Close the Rally-API connection established during the initialization.
     * @throws IOException 
     */
    public void close() throws IOException {
		restApi.close();
    }

	/**
	 * @see #updateRally(JsonObject, String, String, String, String, String, String)
	 * 
	 * @param rallyIdentifier
	 * @param userName
	 * @param userEmail
	 * @param message
	 * @param scmUrl
	 * @param node
	 * @param tagName
	 * @return
	 * @throws Exception
	 */
	public Boolean updateRally(String rallyIdentifier, String userName, String userEmail, String message, String scmUrl, String node, String tagName) throws Exception {
		JsonObject rallyObject = getRallyObject(rallyIdentifier, true);
		return updateRally(rallyObject, userName, userEmail, message, scmUrl, node, tagName);
	}

	/**
     * Fetch the Rally artifact i.e. User Story, Defect or Task with its status and notes details, and call
     * update rally method to make rally updates.
     * 
     * return (null=object not found,true=found and updated,false=found and previously updated)
     * 
	 * @param rallyObject
     * @param userName
     * 		Name of the Author who made the SCM changes. 
     * @param userEmail
     * 		Email Address of the Author who made the SCM changes.
     * @param message
     * 		Comments or messages retrieved from the SCM changes.
     * @param scmUrl
     * 		SCM Repository url which is added to create a changeset link in rally updates.
     * @param node
     * 		Mercurial node in order to detect if the update already exists in rally.
     * @param tagName
     * 		Tag Name to be added to the rally user story or defect.
     * @throws Exception
	 */
	private Boolean updateRally(JsonObject rallyObject, String userName, String userEmail, String message, String scmUrl, String node, String tagName) throws Exception {
		
		if(rallyObject == null) {
			return null;
		}

        String rallyId = rallyObject.get("FormattedID").getAsString();
        message = message.trim().replaceFirst(rallyId, "");
        message = message.replaceAll("[(\\-+:=_)]", "");
        message = message.replace("\n", " ").replace("\r", " ");
        message = message.replaceAll("\\s+", " ");

		String ref = Ref.getRelativeRef(rallyObject.get("_ref").getAsString());
		StringBuilder notes = new StringBuilder(rallyObject.get("Notes").getAsString());
		boolean isFixed = false;
		boolean isComplete = false;
		
		if(rallyObject.get("_type").getAsString().equals("Defect")) { 
			
			if(message.toLowerCase().contains("fix") ) {
				isFixed = true;
			}
			
			String defectState = rallyObject.get("State").getAsString();

			if(isFixed && defectState!=null && !defectState.equalsIgnoreCase("submitted") && !defectState.equalsIgnoreCase("open")) {
				isFixed = false;
			}
		}
		else if(rallyObject.get("_type").getAsString().equals("HierarchicalRequirement")) {
			
			if(message.toLowerCase().contains("complete") ) {
				isComplete = true;
			}

			String scheduleState = rallyObject.get("ScheduleState").getAsString();
			
			if(isComplete && scheduleState!=null && (scheduleState.equalsIgnoreCase("completed") || scheduleState.equalsIgnoreCase("accepted"))) {
				isComplete = false;
			}
			
			if(isComplete) {
				JsonArray jsonArray = rallyObject.getAsJsonArray("Children");
				if(jsonArray!=null && jsonArray.size() > 0) {
					isComplete = false;
				}
			}
		}

		if(ref == null) {
			System.err.println("Rally Identifier '" + ref + "' not found.");
			return null;
		}
			
		DateFormat formatter = new SimpleDateFormat("MMM-dd-yyyy");

		if(node != null && !notes.toString().contains(node) && !notes.toString().contains(message)) {
			
			notes.append("<br /> <a href=\"mailto:");
			notes.append(userEmail).append("\">").append(userName).append("</a> - ");
			notes.append(formatter.format(new Date()));					
			
			if(scmUrl != null && !scmUrl.isEmpty()) {
				notes.append(" (<a href=\"").append(scmUrl).append("\" title=\"SCM Change\">");
				notes.append("changeset</a>) ");
			} else {
				
			}
			
			notes.append(" : ").append(message).append("<br />");
			
			JsonArray tags = rallyObject.get("Tags").getAsJsonObject().getAsJsonArray("_tagsNameArray");
			tags = addTags(tagName, tags);
			
			updateTask(restApi, ref, notes.toString(), isComplete, isFixed, tags);
			return true;
		}
		return false;
    }

	public String getNotes(String rallyId) throws Exception {
		JsonObject rallyObject = getRallyObject(rallyId);
		if(rallyObject == null) {
			return null;
		}
		String notes = rallyObject.get("Notes").getAsString();
		return notes;
    }

	public void setNotes(String rallyId, String notes) throws Exception {
		JsonObject rallyObject = getRallyObject(rallyId);
		JsonObject rallyTask = new JsonObject();
		rallyTask.addProperty("Notes", notes);

		String ref = Ref.getRelativeRef(rallyObject.get("_ref").getAsString());
		UpdateRequest updateRequest = new UpdateRequest(ref, rallyTask);

		UpdateResponse updateResponse = restApi.update(updateRequest);

		if (!updateResponse.wasSuccessful()) {
			throw resolveException(updateResponse);
		}
	}

	private RuntimeException resolveException(UpdateResponse updateResponse) {
		StringBuilder errors = new StringBuilder("Error in updating rally task: ");

		for (String err : updateResponse.getErrors()) {
			errors.append("\t").append(err);
		}

		String errorString = errors.toString();

		if (errorString.contains("ConcurrencyConflictException")) {
			return new RallyConcurrencyConflictException(errorString);
		} else {
			return new RuntimeException(errorString);
		}
	}

	/**
	 * @see #getRallyObject(String, boolean, String...)
	 * 
	 * @param rallyIdentifier
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 * @throws URISyntaxException
	 */
	public JsonObject getRallyObject(String rallyIdentifier) throws IOException, ParseException, URISyntaxException {
		boolean fetchAll = true;
		String[] attributesNone = null;
		return getRallyObject(rallyIdentifier, fetchAll, attributesNone);
	}

	
	/**
     * Fetches the Rally Gson object for the specified User Story or Defect which contains details such as name, status, notes etc.
     * 
	 * @param rallyIdentifier
     * 		Rally user story or defect number.
	 * @param fetchAll
	 * 		Fetches all the attributes for the Rally Gson Object when set true, else fetches only the attributes specified below.
	 * @param attributes
	 * 		The attributes to fetch from rally when the fetch all parameter is false. The FormattedID is fetched by default.
	 * @return
	 * 		Gson Object containing the specified or all the attributes for the specified User Story or Defect.
	 * @throws IOException
	 * @throws ParseException
	 * @throws URISyntaxException
	 */
	public JsonObject getRallyObject(String rallyIdentifier, boolean fetchAll, String... attributes) throws IOException, ParseException, URISyntaxException {

		String objectType = null;

        if(rallyIdentifier != null) {
        	rallyIdentifier = rallyIdentifier.trim();
			objectType = getObjectType(rallyIdentifier);
		}

		if (objectType != null) {
			QueryRequest request = new QueryRequest(objectType);
			request.setQueryFilter(new QueryFilter("FormattedID", "=", rallyIdentifier));
			
			if(!fetchAll) {
				if(attributes.length > 0) {
					String fetchAttributes = StringUtils.join(attributes, ',');
					request.setFetch(new Fetch("FormattedID", fetchAttributes));
				}
				else {
					request.setFetch(new Fetch("FormattedID"));
				}
			}
			
//			request.setQueryFilter(new QueryFilter("State", "<", "Fixed"));
			QueryResponse queryResponse = restApi.query(request);

			if (queryResponse.wasSuccessful()) {

				JsonObject rallyObject = null;
				
				for (JsonElement result : queryResponse.getResults()) {
					rallyObject = result.getAsJsonObject();

					if (rallyIdentifier.equals(rallyObject.get("FormattedID").getAsString())) {
						break;
					}
				}
				
				return rallyObject; 

			} else {
				String exception = null;
				for (String err : queryResponse.getErrors()) {
					exception = "\t" + err;
				}
				throw new RuntimeException("Rally Response Failed: <br /> \n " + exception);
			}
		}
		
		return null;
    }

	private String getObjectType(String rallyIdentifier) {

		rallyIdentifier = rallyIdentifier.toUpperCase();

		if(rallyIdentifier.startsWith("US")) {
            return "HierarchicalRequirement";
        }
		else if(rallyIdentifier.startsWith("DE")) {
			return "Defect";
		}
        else if(rallyIdentifier.startsWith("TA")) {
			return "Task";
        }
		else if(rallyIdentifier.startsWith("TC")) {
			return "TestCase";
		}

		return null;
	}

	public static String extractRallyIdPrefix(String s) {
        Pattern pattern = Pattern.compile("(DE|US|TA|TC)(\\d{3,})");
        Matcher matcher = pattern.matcher(s.toUpperCase());

        if (matcher.find()) {
            return trim(matcher.group(0));
        }

        return null;
    }
	
	/**
     * Fetches the name of the User Story or Defect for the specified Rally Identifier. 
     * @param rallyIdentifier
     * 		Identification number for User Story or a Defect in Rally.
     * @return
     * 		Name of the User Story or Defect as specified in the Rally.
     * @throws Exception
     */
	public String getRallyDescription(String rallyIdentifier) throws Exception {
		
		JsonObject rallyObject = getRallyObject(rallyIdentifier, false, "Name");
		if(rallyObject!=null) {
			return rallyObject.get("Name").getAsString();
		}
		else {
			return null;
		}
    }
	
	/**
	 * Updates the rally user story or defect using the reference id with the specified notes, tags and fixed or completed status.
	 * @param restApi
	 * 		{@link RallyRestApi} Object.
	 * @param ref
	 * 		Rally reference number to fetch the User Story, Defect or Task in Rally system.
	 * @param notes
	 * 		Text which is updated in Rally notes section. 
	 * @param isComplete
	 * 		true when the User Story or Defect is completed.
	 * @param isFixed
	 * 		true when the Defect is fixed. This parameter is only valid for Defects.
	 * @throws IOException
	 */
	private void updateTask(RallyRestApi restApi, String ref, String notes, boolean isComplete, boolean isFixed, JsonArray tags)
			throws IOException {
		
	  JsonObject rallyTask = new JsonObject();
	  rallyTask.addProperty("Notes", notes);
		  
	  if(isFixed) {
		 rallyTask.addProperty("State", "Fixed");  
	  }
	  else if(isComplete) {
		 rallyTask.addProperty("ScheduleState", "Completed");
	  }
	  
	  if(tags != null && tags.size() > 0) {
		  rallyTask.add("Tags", tags);
	  }
		  
	  UpdateRequest updateRequest = new UpdateRequest(ref, rallyTask);
	  UpdateResponse updateResponse = restApi.update(updateRequest);
		  
	  if (!updateResponse.wasSuccessful()) {
		  	
	  	  throw resolveException(updateResponse);
	  }
	}
	
	/**
	 * Adds the tag object with the specified name to the specified tag array. 
	 * When the tag array is empty or null, creates a new tag array to add the specified tag.
	 * When the tag with specified name is not present and cannot be created then returns a null tag array.
	 * When the tag name is already present in the existing tags array then returns a null tag array.
	 * When the tag name is not present in rally, creates a new tag and adds to the tag array.
	 * 
	 * @param tagName
	 * 		name of a new or existing tag to be added to the UserStory/Defect.
	 * @param tags
	 * 		{@link JsonArray} containing all the tags already added to the UserStory/Defect.
	 * @return
	 * 		{@link JsonArray} containing the tags to be added to the UserStory/Defect.
	 * @throws IOException
	 */
    private JsonArray addTags(String tagName, JsonArray tags) throws IOException {

		if(tags != null && tags.size() > 0) {
			
			if(!tags.toString().contains(tagName)) {
				JsonObject tagObject = getTag(tagName);
				tags.add(tagObject);
			}
			else {
				tags = null;
			}
		}
		else {
			JsonObject tagObject = getTag(tagName);
			tags = new JsonArray();
			tags.add(tagObject);
		}
		
		return tags;
	}
    
	/**
	 * Gets the tag object for the specified tag name. 
	 * When the tag is present returns the existing tag object else creates a new tag object.
	 *  
	 * @param tagName
	 * 		used to fetch or create the tag object.
	 * @return
	 * 		{@link JsonObject} containing the tag details.
	 * @throws IOException
	 */
    private JsonObject getTag(String tagName) throws IOException {
    	
    	if(tagName != null) {
			
    		JsonObject tagObject = getTagObject(tagName);
			if(tagObject==null) {
				tagObject = createTag(tagName);
			}
			
			if(tagObject==null) {
				return null;
			}
			else {
				JsonObject newTagJsonObject = new JsonObject();
				newTagJsonObject.add("Name", tagObject.get("Name"));
				newTagJsonObject.add("_ref", tagObject.get("_ref"));
				return newTagJsonObject;
			}
		}
    	else {
    		return null;
    	}
    }

    public String getObjectReference(JsonObject jsonObject) {
        return jsonObject.get("_ref").toString();
    }

    public JsonObject getWorkspaceObject(String workspace) throws IOException {
        QueryRequest workspaceRequest = new QueryRequest("Workspace");
        workspaceRequest.setFetch(new Fetch("Name", "Owner", "Projects"));
        workspaceRequest.setQueryFilter(new QueryFilter("Name", "=", workspace));
        QueryResponse workspaceQueryResponse = restApi.query(workspaceRequest);
        return workspaceQueryResponse.getResults().get(0).getAsJsonObject();
    }

    public JsonObject getProjectObject(String workspace, String project) throws IOException {
        QueryRequest projectRequest = new QueryRequest("Project");
        projectRequest.setFetch(new Fetch("Name", "Owner", "Projects"));
        projectRequest.setQueryFilter(new QueryFilter("Name", "=", project));
        QueryResponse projectQueryResponse = restApi.query(projectRequest);
        return projectQueryResponse.getResults().get(0).getAsJsonObject();
    }

    public JsonObject getUserObject(String rallyUserId) throws IOException {
        QueryRequest userRequest = new QueryRequest("User");
        userRequest.setFetch(new Fetch(
                        "UserName",
                        "FirstName",
                        "LastName",
                        "DisplayName",
                        "UserPermissions",
                        "Name",
                        "Role",
                        "Workspace",
                        "ObjectID",
                        "Project",
                        "ObjectID",
                        "TeamMemberships")
        );
        userRequest.setQueryFilter(new QueryFilter("UserName", "=", rallyUserId.toLowerCase()));
        QueryResponse userQueryResponse = restApi.query(userRequest);
        return userQueryResponse.getResults().get(0).getAsJsonObject();
    }

    public Map<String, String> getUserPermissions(String permissionsRef) throws IOException {

        Map<String, String> userPermissions = new HashMap<String, String>();
        GetRequest permissionsRequest = new GetRequest(permissionsRef);
        GetResponse permissionsResponse = restApi.get(permissionsRequest);
        LinkedTreeMap permissionsMap = new Gson().fromJson(permissionsResponse.getObject(), LinkedTreeMap.class);
        List permissions = (ArrayList) permissionsMap.get("Results");

        for(Object permission : permissions) {
            try {
                LinkedTreeMap map = (LinkedTreeMap) permission;
                if (map.get("Project") != null) {
                    userPermissions.put(((LinkedTreeMap) map.get("Project")).get("_refObjectName").toString(), map.get("Role").toString());
                }
            }
            catch (NullPointerException nex) { }
        }

        return userPermissions;
    }

    public String checkProjectPermissionByUser(String rallyUserId, String project, String role) throws IOException {
        JsonObject userObject = getUserObject(rallyUserId);
        String userRef = userObject.get("_ref").toString();
        LinkedTreeMap userMap = new Gson().fromJson(userObject.get("UserPermissions"), LinkedTreeMap.class);
        String permissionsRef = userMap.get("_ref").toString();
        Map<String, String> userPermissions = getUserPermissions(permissionsRef);

        if(userPermissions.get(project) != null && userPermissions.get(project).equals(role))
            return userRef;
        else {
            return null;
        }
    }

    /**
	 * Fetches the existing tag object using the specified tag name. 
	 * @param tagName
	 * 		used to fetch the existing tag object.
	 * @return
	 * 		{@link JsonObject} containing the tag details.
	 * @throws IOException
	 */
	public JsonObject getTagObject(String tagName) throws IOException {
		
        QueryRequest tagRequest = new QueryRequest("Tag");
        tagRequest.setFetch(new Fetch("Name"));
        tagRequest.setQueryFilter(new QueryFilter("Name", "=", tagName));
        
        QueryResponse tagQueryResponse = restApi.query(tagRequest);
    
        if(tagQueryResponse.wasSuccessful() && tagQueryResponse.getResults().size() > 0) {
        	
            JsonObject tagJsonObject = tagQueryResponse.getResults().get(0).getAsJsonObject();
            return tagJsonObject;
        }
        
		return null;
	}
	
	/**
	 * Creates a new tag in rally with the specified tag name.
	 * @param tagName
	 * 		used to create a new tag.
	 * @return
	 * 		{@link JsonObject} containing the new tag details.
	 * @throws IOException
	 */
	public JsonObject createTag(String tagName) throws IOException{
		JsonObject tag = new JsonObject();
	    tag.addProperty(NAME.value(), tagName);
		return createRallyObject("Tag", tag);
	}

	public JsonObject createUser(String username, String emailAddress) throws IOException{
		JsonObject user = new JsonObject();
		user.addProperty(USERNAME.value(), username);
		user.addProperty(EMAILADDRESS.value(), emailAddress);
		return createRallyObject("User", user);
	}

	public JsonObject createUserStory(String name, String description, ScheduleState state, String ownerRef) throws IOException{
		JsonObject userStory = new JsonObject();
		userStory.addProperty(NAME.value(), name);
		userStory.addProperty(DESCRIPTION.value(), description);
		userStory.addProperty(STATE.value(), state.value());
		userStory.addProperty(OWNER.value(), ownerRef);
		return createRallyObject("HierarchicalRequirement", userStory);
	}

	public JsonObject createDefect(String name, String description, DefectState defectState, String ownerRef,
								   Priority priority, Severity severity) throws IOException{
        JsonObject defect = new JsonObject();
        defect.addProperty(NAME.value(), name);
		defect.addProperty(DESCRIPTION.value(), description);
        defect.addProperty(STATE.value(), defectState.value());
        defect.addProperty(OWNER.value(), ownerRef);
		defect.addProperty(PRIORITY.value(), priority.value());
		defect.addProperty(SEVERITY.value(), severity.value());
		return createRallyObject("Defect", defect);
	}

	public JsonObject createTask(String name, String description, ScheduleState state, String ownerRef, String storyRef) throws IOException{
		JsonObject task = new JsonObject();
		task.addProperty(NAME.value(), name);
		task.addProperty(DESCRIPTION.value(), description);
		task.addProperty(STATE.value(), state.value());
		task.addProperty(OWNER.value(), ownerRef);
		task.addProperty(WORKPRODUCT.value(), storyRef);
		return createRallyObject("Task", task);
	}

	public JsonObject createTestCase(String name, String description, TestType type, TestMethod method, String projectRef) throws IOException{
		JsonObject testCase = new JsonObject();
		testCase.addProperty(NAME.value(), name);
		testCase.addProperty(DESCRIPTION.value(), description);
		testCase.addProperty(TYPE.value(), type.value());
		testCase.addProperty(METHOD.value(), method.value());
		testCase.addProperty(PROJECT.value(), projectRef);
		return createRallyObject("TestCase", testCase);
	}

	public JsonObject createTestStep(String input, String expectedResult, String testCaseRef) throws IOException {
		JsonObject testStep = new JsonObject();
		testStep.addProperty(INPUT.value(), input);
		testStep.addProperty(EXPECTEDRESULT.value(), expectedResult);
		testStep.addProperty(TESTCASE.value(), testCaseRef);
		return createRallyObject("TestCaseStep", testStep);
	}

	public JsonObject createTestResult(String verdict, String notes, String build, String userRef, String testCaseRef) throws IOException {
		JsonObject testCaseResult = new JsonObject();
		testCaseResult.addProperty(VERDICT.value(), verdict);

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		String currentDate = formatter.format(Calendar.getInstance().getTime());
		testCaseResult.addProperty(DATE.value(), currentDate);

		testCaseResult.addProperty(NOTES.value(), notes);
		testCaseResult.addProperty(BUILD.value(), build);
		testCaseResult.addProperty(TESTER.value(), userRef);
		testCaseResult.addProperty(TESTCASE.value(), testCaseRef);
		return createRallyObject("TestCaseResult", testCaseResult);
	}

	private JsonObject createRallyObject(String objectType, JsonObject requestObject) throws IOException {
		CreateRequest createRequest = new CreateRequest(objectType, requestObject);
		CreateResponse createResponse = restApi.create(createRequest);

		if(createResponse.wasSuccessful()) {
            JsonObject responseObject = createResponse.getObject().getAsJsonObject();
            return responseObject;
        }

        return null;
	}

	/**
     * Deletes the rally reference object with the specified reference id.
     * @param refId
     *		{@link String} representing the reference id of the rally object to be deleted.
     * @return
     * 		true when deletion is successful else false.
     * @throws IOException
     */
    public boolean deleteRallyObject(String refId) throws IOException {
        DeleteRequest deleteRequest = new DeleteRequest(refId);
        DeleteResponse deleteResponse = restApi.delete(deleteRequest);

        if(!deleteResponse.wasSuccessful()) {
            String errors = StringUtils.join(deleteResponse.getErrors(), ',');
            System.err.println("Errors: " + errors);
        }

        return deleteResponse.wasSuccessful();
    }

    public static String generateRallySearchUrl(String rallyIdentifier) {
		return format("%s/slm/rally.sp#/search?keywords=%s", RALLY_HOST, rallyIdentifier);
	}
}
