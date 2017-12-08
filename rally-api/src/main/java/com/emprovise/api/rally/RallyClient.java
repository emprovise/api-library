package com.emprovise.api.rally;

import com.emprovise.api.rally.exception.RallyConcurrencyConflictException;
import com.emprovise.api.rally.type.*;
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

import static com.emprovise.api.rally.type.Param.*;
import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.trim;

/**
 * Rally Client uses the Rally Rest Services to create, fetch and update Rally items.
 * Please refer to @see <a href="https://rally1.rallydev.com/slm/doc/webservice/">Rally Webservice Documentation</a>
 */
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
	 * Initializes the Rally API by establishing a connection with the Rally Server using rally credentials
	 * with the specified URI for the proxy server.
	 * @throws Exception
	 * @deprecated  user credentials are replaced by rally api keys
	 */
	@Deprecated
    public RallyClient(String rallyUser, String rallyPassword, URI proxyUri, String proxyUser, String proxyPassword) throws Exception {
		this.restApi = new RallyDefaultRestApi(new URI(RALLY_HOST), rallyUser, rallyPassword, proxyUri, proxyUser, proxyPassword);
        initializeRallyApi();
    }

	/**
	 * Initializes the Rally API by establishing a connection with the Rally Server using rally API key
	 * with the specified URI for the proxy server.
	 * Please refer to @see <a href="https://help.rallydev.com/rally-application-manager">Rally Api Key Documentation</a>
	 *
	 * @param apiKey
	 * @param proxyUri
	 * @param proxyUser
	 * @param proxyPassword
	 * @throws Exception
	 */
    public RallyClient(String apiKey, URI proxyUri, String proxyUser, String proxyPassword) throws Exception {
        this.restApi = new RallyDefaultRestApi(new URI(RALLY_HOST), apiKey, proxyUri, proxyUser, proxyPassword);
        initializeRallyApi();
    }

	/**
	 * Initializes the Rally API by establishing a connection with the Rally Server using rally credentials.
	 *
	 * @param rallyUser
	 * @param rallyPassword
	 * @throws Exception
	 * @deprecated  user credentials are replaced by rally api keys
	 */
	@Deprecated
    public RallyClient(String rallyUser, String rallyPassword) throws Exception {
        this.restApi = new RallyDefaultRestApi(new URI(RALLY_HOST), rallyUser, rallyPassword);
        initializeRallyApi();
    }

	/**
	 * Initializes the Rally API by establishing a connection with the Rally Server using rally API key.
	 * Please refer to @see <a href="https://help.rallydev.com/rally-application-manager">Rally Api Key Documentation</a>
	 * @param apiKey
	 * @throws Exception
	 */
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
	 * Customize the application and vendor name which uses the rally API.
	 *
	 * @param applicationName
	 * 							Application name using rally api.
	 * @param applicationVendor
	 * 							Application vendor for the application.
	 */
	public void setApplication(String applicationName, String applicationVendor) {
		this.restApi.setApplicationName(applicationName);
		this.restApi.setApplicationVendor(applicationVendor);
	}

	/**
	 * Calls the default URL on the rally server to determine that the authentication is
	 * successful, since initializing the RallyClient does not verify the connection.
	 *
	 * @return
	 * 			true if connection is successful else false.
	 */
	public boolean isConnected() {
		try {
			this.restApi.get(new GetRequest("/"));
			return true;
		}
		catch(Exception ex) {
			return false;
		}
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
	 *					Valid rally identifier identifying User Story, Defect, Task or a Test Case.
	 * @param userName
	 * 					User name who wants to update rally notes.
	 * @param userEmail
	 * 					User contact email address.
	 * @param message
	 * 					User's comments or messages to be updated to the notes section of rally item.
	 * @param scmUrl
	 * 					Scm url link were the Scm server is hosted.
	 * @param changeset
	 * 					Scm changeset which refers to a particular commit.
	 * @param tagName
	 * 					Tag to be added to the rally item.
	 * @return
	 * @throws Exception
	 */
	public Boolean updateRally(String rallyIdentifier, String userName, String userEmail, String message, String scmUrl, String changeset, String tagName) throws Exception {
		JsonObject rallyObject = getRallyObject(rallyIdentifier, true);
		return updateRally(rallyObject, userName, userEmail, message, scmUrl, changeset, tagName);
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
	 * @param changeset
	 * 		Mercurial node in order to detect if the update already exists in rally.
	 * @param tagName
	 * 		Tag Name to be added to the rally user story or defect.
	 * @throws Exception
	 */
	private Boolean updateRally(JsonObject rallyObject, String userName, String userEmail, String message, String scmUrl, String changeset, String tagName) throws Exception {
		
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
			
			if(message.toLowerCase().startsWith("fix") ) {
				isFixed = true;
			}
			
			String defectState = rallyObject.get("State").getAsString();

			if(isFixed && defectState!=null && !defectState.equalsIgnoreCase("submitted") && !defectState.equalsIgnoreCase("open")) {
				isFixed = false;
			}
		}
		else if(rallyObject.get("_type").getAsString().equals("HierarchicalRequirement")) {
			
			if(message.toLowerCase().startsWith("complete") ) {
				isComplete = true;
			}

			String scheduleState = rallyObject.get("ScheduleState").getAsString();
			
			if(isComplete && scheduleState!=null && (scheduleState.equalsIgnoreCase("completed") || scheduleState.equalsIgnoreCase("accepted"))) {
				isComplete = false;
			}
			
			if(isComplete) {
				JsonElement children = rallyObject.get("Children");
				if(children!=null && children.isJsonArray()) {
					JsonArray jsonArray = rallyObject.getAsJsonArray("Children");
					if (jsonArray != null && jsonArray.size() > 0) {
						isComplete = false;
					}
				}
			}
		}

		if(ref == null) {
			System.err.println("Rally Identifier '" + ref + "' not found.");
			return null;
		}
			
		DateFormat formatter = new SimpleDateFormat("MMM-dd-yyyy");

		if(changeset != null && !notes.toString().contains(changeset) && !notes.toString().contains(message)) {
			
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

	/**
	 * Get Notes using the rally Identifier
	 * @param rallyIdentifier
	 * * 		Valid rally identifier identifying User Story, Defect, Task or a Test Case.
	 * @return
	 * 			Notes for the specified rally object.
	 * @throws Exception
	 */
	public String getNotes(String rallyIdentifier) throws Exception {
		JsonObject rallyObject = getRallyObject(rallyIdentifier);
		if(rallyObject == null) {
			return null;
		}
		String notes = rallyObject.get("Notes").getAsString();
		return notes;
    }

	/**
	 * Update the notes for the rally object using rally identifier.
	 * @param rallyIdentifier
	 * 			Valid rally identifier identifying User Story, Defect, Task or a Test Case.
	 * @param notes
	 * 			Notes or Comments which the needs to be added to the rally object.
	 * @throws Exception
	 */
	public void setNotes(String rallyIdentifier, String notes) throws Exception {
		JsonObject rallyObject = getRallyObject(rallyIdentifier);
		JsonObject rallyTask = new JsonObject();
		rallyTask.addProperty("Notes", notes);

		String ref = Ref.getRelativeRef(rallyObject.get("_ref").getAsString());
		UpdateRequest updateRequest = new UpdateRequest(ref, rallyTask);

		UpdateResponse updateResponse = restApi.update(updateRequest);

		if (!updateResponse.wasSuccessful()) {
			throw resolveException(updateResponse);
		}
	}

	/**
	 * Resolves the exception based on the rally response object.
	 * Throws RallyConcurrencyException for any conflicts occurred while updating
	 * a particular items, which could be retried later, else throws RuntimeException
	 * for the rest of exceptions.
	 *
	 * @param updateResponse
	 * @return
	 */
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
	 * 					Valid rally identifier identifying User Story, Defect, Task or a Test Case.
	 * @return
	 * 					Rally json object containing the details of the rally item specified by rally identifier.
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
	 * Fetches the Rally Gson object for the specified Rally UserStory, Defect, Task, TestCase which contains details such as
	 * name, status, notes etc.
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
		Identifier identifier = Identifier.getIdentifierByPrefix(rallyIdentifier.substring(0, 2));

		if(identifier != null) {
			return identifier.objectType();
		} else if(rallyIdentifier.startsWith("F")) {
			return Identifier.FEATURE.objectType();
		}

		return null;
	}

	/**
	 * Extracts rally identifier from the text, currently supporting User Story (US), Defect (DE), Task (TA) or
	 * a Test Case (TC).
	 *
	 * @param text
	 * 			Text to be parsed to retrieve the rally identifier as per format standards.
	 * @return
	 * 			Rally Identifier.
	 */
	public static String extractRallyIdPrefix(String text) {
        Pattern pattern = Pattern.compile("(DE|US|TA|TC)(\\d{3,})");
        Matcher matcher = pattern.matcher(text.toUpperCase());

        if (matcher.find()) {
            return trim(matcher.group(0));
        }

        return null;
    }

	/**
	 * Fetches the name of the Rally item for the specified Rally Identifier.
	 * @param rallyIdentifier
	 * 		Identification number for Rally Item (User Story, Defect, Task, Test Case).
	 * @return
	 * 		Name of the rally item for the specified Rally identifier.
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

	public JsonObject getSubscriptionObject() throws IOException {
		QueryRequest subscriptionRequest = new QueryRequest("Subscriptions");
		subscriptionRequest.setFetch(new Fetch("Name", "SubscriptionID", "Workspaces"));
		QueryResponse subscriptionQueryResponse = restApi.query(subscriptionRequest);
		return subscriptionQueryResponse.getResults().get(0).getAsJsonObject();
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

	public JsonArray getWorkspaces() throws IOException {
		QueryRequest workspaceRequest = new QueryRequest("Workspace");
		workspaceRequest.setFetch(new Fetch("CreationDate",
				"ObjectID",
				"Children",
				"Description",
				"Name",
				"Notes",
				"Owner",
				"Projects",
				"State",
				"Style"));

		QueryResponse workspaceQueryResponse = restApi.query(workspaceRequest);
		return cleanupJsonArray(workspaceQueryResponse.getResults());
	}

	public String getWorkspaceRef(String workspace) throws IOException {
		QueryRequest workspaceRequest = new QueryRequest("Workspace");
		workspaceRequest.setFetch(new Fetch("ObjectID"));
		workspaceRequest.setQueryFilter(new QueryFilter("Name", "=", workspace));
		QueryResponse workspaceQueryResponse = restApi.query(workspaceRequest);

		if(workspaceQueryResponse.getResults().size() < 1) {
			throw new RuntimeException(String.format("No workspace named '%s' found", workspace));
		}

		JsonObject jsonObject = workspaceQueryResponse.getResults().get(0).getAsJsonObject();
		return "/workspace/" + jsonObject.get("ObjectID").toString();
	}

	public JsonArray getMilestones(String workspace) throws IOException {
		QueryRequest  milestoneRequest = new QueryRequest("Milestone");
		milestoneRequest.setWorkspace(getWorkspaceRef(workspace));
		QueryResponse milestoneResponse = restApi.query(milestoneRequest);
		return cleanupJsonArray(milestoneResponse.getResults());
	}

	public JsonArray getProjects(String workspace) throws IOException {
		QueryRequest projectRequest = new QueryRequest("Project");
		projectRequest.setFetch(new Fetch("CreationDate",
				"ObjectID",
				"BuildDefinitions",
				"Children",
				"Description",
				"Editors",
				"Iterations",
				"Name",
				"Notes",
				"Owner",
				"Parent",
				"Releases",
				"State",
				"TeamMembers",
				"Viewers"));

		projectRequest.setWorkspace(getWorkspaceRef(workspace));
		QueryResponse projectQueryResponse = restApi.query(projectRequest);
		return cleanupJsonArray(projectQueryResponse.getResults());
	}

	public String getProjectRef(String project) throws IOException {
		QueryRequest projectRequest = new QueryRequest("Project");
		projectRequest.setFetch(new Fetch("ObjectID"));
		projectRequest.setQueryFilter(new QueryFilter("Name", "=", project));
		QueryResponse projectQueryResponse = restApi.query(projectRequest);

		if(projectQueryResponse.getResults().size() < 1) {
			throw new RuntimeException(String.format("No project named '%s' found", project));
		}

		JsonObject jsonObject = projectQueryResponse.getResults().get(0).getAsJsonObject();
		return "/project/" + jsonObject.get("ObjectID").toString();
	}

	public JsonArray getReleases(String project) throws IOException {

		QueryRequest  releaseRequest = new QueryRequest("Release");
		releaseRequest.setFetch(new Fetch("CreationDate",
				"ObjectID",
				"Accepted",
				"GrossEstimateConversionRatio",
				"Name",
				"PlanEstimate",
				"PlannedVelocity",
				"ReleaseDate",
				"ReleaseStartDate",
				"State",
				"TaskActualTotal",
				"TaskEstimateTotal",
				"TaskRemainingTotal"));

		releaseRequest.setScopedDown(false);
		releaseRequest.setScopedUp(false);
		releaseRequest.setProject(getProjectRef(project));
		QueryResponse releaseQueryResponse = restApi.query(releaseRequest);
		return cleanupJsonArray(releaseQueryResponse.getResults());
	}

	public JsonArray getIterations(String project, String startDate, String endDate) throws IOException {

		QueryRequest  iterationRequest = new QueryRequest("Iteration");
		iterationRequest.setFetch(new Fetch("CreationDate",
				"ObjectID",
				"EndDate",
				"Name",
				"PlanEstimate",
				"PlannedVelocity",
				"StartDate",
				"State",
				"TaskActualTotal",
				"TaskEstimateTotal",
				"TaskRemainingTotal",
				"UserIterationCapacities"));

		iterationRequest.setScopedDown(false);
		iterationRequest.setScopedUp(false);
		iterationRequest.setProject(getProjectRef(project));

		if(StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)) {
			QueryFilter queryFilter = new QueryFilter("StartDate", ">=", startDate).and(new QueryFilter("EndDate", "<=", endDate));
			iterationRequest.setQueryFilter(queryFilter);
		}

		QueryResponse iterationResponse = restApi.query(iterationRequest);
		return cleanupJsonArray(iterationResponse.getResults());
	}

	public JsonArray getRallyObjects(Identifier identifier, String project, String iteration, String startDate, String endDate) throws IOException {
		QueryRequest rallyRequest = new QueryRequest(identifier.objectType());
		rallyRequest.setFetch(new Fetch("CreationDate",
				"ObjectID",
				"Workspace",
				"Changesets",
				"FormattedID",
				"LastUpdateDate",
				"Name",
				"Owner",
				"Project",
				"Ready",
				"Tags",
				"FlowState",
				"ScheduleState",
				"TestCaseCount",
				"AcceptedDate",
				"Blocked",
				"BlockedReason",
				"Blocker",
				"Children",
				"DefectStatus",
				"Defects",
				"DirectChildrenCount",
				"HasParent",
				"InProgressDate",
				"Iteration",
				"Parent",
				"PlanEstimate",
				"Recycled",
				"Release",
				"TaskActualTotal",
				"TaskEstimateTotal",
				"TaskRemainingTotal",
				"TaskStatus",
				"Tasks"));

		rallyRequest.setLimit(25000);
		rallyRequest.setScopedDown(true);
		rallyRequest.setScopedUp(false);

		QueryFilter queryFilter = new QueryFilter("Project.Name", "=", project);

		if(StringUtils.isNotBlank(startDate)) {
			queryFilter.and(new QueryFilter("Iteration.Name", "=", iteration));
		}

		if(StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)) {
			queryFilter = queryFilter.and(new QueryFilter("Iteration.StartDate", ">=", startDate))
					.and(new QueryFilter("Iteration.EndDate", "<=", endDate));
		}

		rallyRequest.setQueryFilter(queryFilter);

		QueryResponse projectQueryResponse = restApi.query(rallyRequest);
		JsonArray results = projectQueryResponse.getResults();

		for (JsonElement element : results) {
			JsonObject jsonObject = element.getAsJsonObject();
			Map<String, String> addProperties = new LinkedHashMap<>();
			List<String> removeProperties = new ArrayList<>();

			for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
				String property = entry.getKey();
				JsonElement jsonElement = entry.getValue();

				if(jsonElement != null && jsonElement.isJsonObject()) {
					JsonObject json = jsonElement.getAsJsonObject();

					JsonElement objectName = json.get("_refObjectName");
					if(objectName != null) {
						addProperties.put(property + ".Name", objectName.getAsString());
					}

					JsonElement count = json.get("Count");
					if(count != null) {
						addProperties.put(property + ".Count", count.getAsString());
					}

					removeProperties.add(property);
				}
			}

			removeProperties.forEach(removeProperty -> jsonObject.remove(removeProperty));
			addProperties.forEach((k,v)->{
				jsonObject.addProperty(k,v);
			});

			removeRallyReferenceFields(jsonObject);
		}

		return results;
	}

	public JsonArray attachResultsByReference(JsonArray jsonArray, String property) throws IOException {

		for (JsonElement element : jsonArray) {
			JsonObject jsonObject = element.getAsJsonObject();
			JsonObject jsonObjectReference = null;

			for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
				JsonElement jsonElement = entry.getValue();

				if(jsonElement != null && jsonElement.isJsonObject() && entry.getKey().equalsIgnoreCase(property)) {
					jsonObjectReference = entry.getValue().getAsJsonObject();
					break;
				}
			}

			if(jsonObjectReference != null) {
				jsonObject.remove(property);
				jsonObject.add(property, getResults(jsonObjectReference));
			}
		}


		return jsonArray;
	}

	private JsonArray getResults(JsonObject jsonObject) throws IOException {
		QueryRequest queryRequest = new QueryRequest(jsonObject);
		JsonArray results = restApi.query(queryRequest).getResults();
		return cleanupJsonArray(results);
	}

	private JsonArray cleanupJsonArray(JsonArray results) {
		for (JsonElement element : results) {
			removeRallyReferenceFields(element.getAsJsonObject());
		}

		return results;
	}

	private void removeRallyReferenceFields(JsonObject jsonObject) {
		jsonObject.remove("_rallyAPIMajor");
		jsonObject.remove("_rallyAPIMinor");
		jsonObject.remove("_ref");
		jsonObject.remove("_refObjectUUID");
		jsonObject.remove("_objectVersion");
		jsonObject.remove("_refObjectName");
		jsonObject.remove("_CreatedAt");
	}
}
