package com.emprovise.api.amazon.ws.database;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.*;
import com.amazonaws.services.dynamodbv2.model.*;
import org.apache.commons.beanutils.BeanUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @see <a href="https://aws.amazon.com/getting-started/tutorials/create-nosql-table/">Create Table</a>
 * <a href="https://github.com/gkatzioura/egkatzioura.wordpress.com">Dynamo Example</a>
 * <a href="http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/GettingStarted.Java.03.html">Dynamo Example</a>
 * <a href="http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/SettingUp.DynamoWebService.html">Setting up DynamoWebService</a>
 * <a href="https://dzone.com/articles/query-dynamodb-items-withjava">Query Dynamodb Items</a>
 */
public class AmazonDynamoDBService {

    private AmazonDynamoDB amazonDynamoDB;
    private DynamoDB dynamoDB;
    private static final String AND = " and ";

    public AmazonDynamoDBService(AWSCredentials credentials, Regions region) {
        this(new AWSStaticCredentialsProvider(credentials), region);
    }

    public AmazonDynamoDBService(AWSCredentials credentials, Regions region, ClientConfiguration config) {
        this(new AWSStaticCredentialsProvider(credentials), region, config);
    }

    public AmazonDynamoDBService(AWSCredentialsProvider awsCredentialsProvider, Regions region) {
        this(awsCredentialsProvider, region, new ClientConfiguration());
    }

    public AmazonDynamoDBService(AWSCredentialsProvider awsCredentialsProvider, Regions region, ClientConfiguration config) {
        this.amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
                                    .withRegion(region)
                                    .withCredentials(awsCredentialsProvider)
                                    .withClientConfiguration(config)
                                    .build();
        this.dynamoDB = new DynamoDB(amazonDynamoDB);
    }

    public DeleteTableResult deleteTable(String tableName) {
        DeleteTableRequest deleteTableRequest = new DeleteTableRequest();
        deleteTableRequest.setTableName(tableName);
        return amazonDynamoDB.deleteTable(deleteTableRequest);
    }

    public PutItemResult insertRow(String tableName, Map<String, AttributeValue> attributeValues) {
        PutItemRequest putItemRequest = new PutItemRequest()
                .withTableName(tableName)
                .withItem(attributeValues);
        return amazonDynamoDB.putItem(putItemRequest);
    }

    public BatchWriteItemResult insertRows(String tableName, List<Map<String, String>> keyValuesMapList) {

        List<WriteRequest> batchList = new ArrayList<>();
        List<Map<String, AttributeValue>> attributeValuesList = new ArrayList<>();

        for (Map<String, String> keyValuesMap : keyValuesMapList) {
            Map<String, AttributeValue> attributeValuesMap = keyValuesMap.entrySet()
                                                            .stream()
                                                            .collect(Collectors.toMap(Map.Entry::getKey, e -> new AttributeValue(e.getValue())));
            attributeValuesList.add(attributeValuesMap);
        }

        for (Map<String, AttributeValue> stringAttributeValueMap : attributeValuesList) {
            PutRequest putRequest = new PutRequest();
            putRequest.setItem(stringAttributeValueMap);

            WriteRequest writeRequest = new WriteRequest();
            writeRequest.setPutRequest(putRequest);
            batchList.add(writeRequest);
        }

        Map<String, List<WriteRequest>> batchTableRequests = new HashMap<>();
        batchTableRequests.put(tableName, batchList);

        BatchWriteItemRequest batchWriteItemRequest = new BatchWriteItemRequest();
        batchWriteItemRequest.setRequestItems(batchTableRequests);

        return amazonDynamoDB.batchWriteItem(batchWriteItemRequest);
    }

    /**
     * @param tableName is the {@link String } name of the dynamodb table to fetch records.
     * @param parameters query parameters for dynamodb table. Simple query were only those records whose values equals to the specified parameters map will be fetched.
     *                   Complex conditions such as not equals, greater than, less than or in between values is not supported by this method.
     * @return {@link List } of {@link Map } containing key value pairs of the result rows fetched from the specified table.
     * @throws Exception
     */
    public List<Map<String,AttributeValue>> findByParameters(String tableName, Map<String, String> parameters) {

        StringBuilder queryBuilder = new StringBuilder();
        Map<String, String> expressionAttributesNames = new HashMap<>();
        Map<String,AttributeValue> expressionAttributeValues = new HashMap<>();

        QueryRequest queryRequest = new QueryRequest().withTableName(tableName);

        if(parameters != null) {
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                String key = "#" + entry.getKey();
                expressionAttributesNames.put(key, entry.getKey());
                String value = ":" + entry.getKey();
                expressionAttributeValues.put(value, new AttributeValue().withS(entry.getValue()));

                if(queryBuilder.length() > 0) {
                    queryBuilder.append(AND);
                }

                queryBuilder.append(key + " = " + value);
            }

            queryRequest.withKeyConditionExpression(queryBuilder.toString())
                        .withExpressionAttributeNames(expressionAttributesNames)
                        .withExpressionAttributeValues(expressionAttributeValues);
        }

        QueryResult queryResult = amazonDynamoDB.query(queryRequest);
        return queryResult.getItems();
    }

    /**
     * @param tableName is the {@link String } name of the dynamodb table to fetch records.
     * @param parameters query parameters for dynamodb table. Simple query were only those records whose values equals to the specified parameters map will be fetched.
     *                   Complex conditions such as not equals, greater than, less than or in between values is not supported by this method.
     * @param clazz result class object {@link Class } to which the result record attributes should be mapped. The field names of the specified class should match with the database attribute names
     *              except the first letter of the name should be in lowercase as per java naming conventions.
     * @return {@link List } of specified class objects as result.
     * @throws Exception
     */
    public <T> List<T> findByParameters(String tableName, Map<String, String> parameters, Class<T> clazz) throws Exception {

        List<T> result = new ArrayList<>();
        List<Map<String, AttributeValue>> resultParameterList = findByParameters(tableName, parameters);

        for (Map<String, AttributeValue> attributeValueMap : resultParameterList) {

            Map<String, String> stringAttributeMap = attributeValueMap.entrySet()
                    .stream()
                    .collect(Collectors.toMap(e -> firstCharToLowerCase(e.getKey()), e -> e.getValue().getS() ));
            T responseObject = clazz.newInstance();
            BeanUtils.populate(responseObject, stringAttributeMap);
            result.add(responseObject);
        }
        return result;
    }

    public Table createTable(CreateTableRequest req) {
        return dynamoDB.createTable(req);
    }

    public Item getItem(String tableName, GetItemSpec itemSpec) {
        return dynamoDB.getTable(tableName).getItem(itemSpec);
    }

    public List<Item> queryItems(String tableName, QuerySpec querySpec) {
        ItemCollection<QueryOutcome> items = dynamoDB.getTable(tableName).query(querySpec);
        Iterator<Item> iterator = items.iterator();
        return toList(iterator);
    }

    public List<Item> scanItems(String tableName, ScanSpec scanSpec) {
        ItemCollection<ScanOutcome> items = dynamoDB.getTable(tableName).scan(scanSpec);
        Iterator<Item> iterator = items.iterator();
        return toList(iterator);
    }

    public PutItemOutcome putItem(String tableName, Item item) {
        return dynamoDB.getTable(tableName).putItem(item);
    }

    public UpdateItemOutcome updateItem(String tableName, UpdateItemSpec updateItemSpec) {
        return dynamoDB.getTable(tableName).updateItem(updateItemSpec);
    }

    public DeleteItemOutcome deleteItem(String tableName, DeleteItemSpec deleteItemSpec) {
        return dynamoDB.getTable(tableName).deleteItem(deleteItemSpec);
    }

    public List<String> listAllTables() {
        TableCollection<ListTablesResult> tableList = dynamoDB.listTables();
        Iterator<Table> iterator = tableList.iterator();
        List<Table> tables = toList(iterator);
        return tables.stream()
                .map(table -> table.getTableName())
                .collect(Collectors.toList());
    }

    private  <T> List<T> toList(Iterator<T> iterator) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator,
                Spliterator.ORDERED), false).collect(
                Collectors.<T> toList());
    }

    private String firstCharToLowerCase(String string) {
        if (string == null || string.length() == 0) {
            return string;
        }
        char c[] = string.toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        return new String(c);
    }
}
