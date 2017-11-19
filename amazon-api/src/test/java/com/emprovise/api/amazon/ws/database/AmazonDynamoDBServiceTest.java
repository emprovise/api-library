package com.emprovise.api.amazon.ws.database;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.emprovise.api.amazon.ws.database.model.Music;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class AmazonDynamoDBServiceTest {

    @Test
    public void findByParametersReturningMap() throws Exception {
        AWSCredentials credentials = new BasicAWSCredentials("YourAccessKeyID", "YourSecretAccessKey");
        AmazonDynamoDBService amazonDynamoDBService = new AmazonDynamoDBService(credentials, Regions.US_EAST_2);
        Map<String, String> map = new HashMap<>();
        map.put("Artist", "Michael Jackson");
        List<Map<String, AttributeValue>> attributeMapList = amazonDynamoDBService.findByParameters("Music", map);

        assertFalse(attributeMapList.isEmpty());
        assertEquals(3, attributeMapList.size());
        System.out.println(attributeMapList);
    }

    @Test
    public void findByParametersReturningObject() throws Exception {
        AWSCredentials credentials = new BasicAWSCredentials("YourAccessKeyID", "YourSecretAccessKey");
        AmazonDynamoDBService amazonDynamoDBService = new AmazonDynamoDBService(credentials, Regions.US_EAST_2);
        Map<String, String> map = new HashMap<>();
        map.put("Artist", "Michael Jackson");
        List<Music> musicList = amazonDynamoDBService.findByParameters("Music", map, Music.class);

        assertFalse(musicList.isEmpty());
        assertEquals(3, musicList.size());
        System.out.println(musicList);
    }

    @Test
    public void queryItems() throws Exception {
        AWSCredentials credentials = new BasicAWSCredentials("YourAccessKeyID", "YourSecretAccessKey");
        AmazonDynamoDBService amazonDynamoDBService = new AmazonDynamoDBService(credentials, Regions.US_EAST_2);

        HashMap<String, String> nameMap = new HashMap<String, String>();
        nameMap.put("#artist", "Artist");

        HashMap<String, Object> valueMap = new HashMap<String, Object>();
        valueMap.put(":artist", "Michael Jackson");

        QuerySpec querySpec = new QuerySpec().withKeyConditionExpression("#artist = :artist").withNameMap(nameMap)
                .withValueMap(valueMap);

        List<Item> itemList = amazonDynamoDBService.queryItems("Music", querySpec);
        assertFalse(itemList.isEmpty());
        assertEquals(3, itemList.size());
        System.out.println(itemList);
    }
}