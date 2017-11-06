package com.emprovise.api.db.sql;

import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

public class SQLDatabase {

    private Sql2o sql2o;

    public SQLDatabase(String url) {
        this(url, null, null);
    }

    public SQLDatabase(String url, String username, String password) {

        this.sql2o = new Sql2o(url, username, password);
    }

    public <T> List<T> find(String query, Class<T> clazz) {

        List<T> results;

        try (Connection connection = sql2o.open()) {
            results = connection.createQuery(query).executeAndFetch(clazz);
        }

        return results;
    }

    public <T> void insert(final T object) throws IllegalAccessException {

        Class<?> className = object.getClass();
        List<Field> fields = getAllFields(new LinkedList<>(), className);

        try (Connection conn = sql2o.open()) {

            List<String> fieldNames = fields.stream()
                    .map(field -> field.getName())
                    .collect(Collectors.toList());

            String columns = String.join(",", fieldNames);

            String parameters = fieldNames.stream()
                    .map(fieldName -> ":" + fieldName)
                    .collect(joining(","));

            Query query = conn.createQuery(String.format("INSERT INTO %s(%s) VALUES (%s)", className.getSimpleName(), columns, parameters));

            for (Field field : fields) {
                field.setAccessible(true);
                query.addParameter(field.getName(), field.get(object));
            }

            query.executeUpdate();
        }
    }

    public void executeQuery(String queryString, Map<String, Object> parameters) {

        try (Connection con = sql2o.open()) {

            Query query = con.createQuery(queryString);

            if(parameters != null) {
                for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                    query.addParameter(entry.getKey(), entry.getValue());
                }
            }

            query.executeUpdate();
        }
    }

    private List<Field> getAllFields(List<Field> fields, Class<?> type) {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));

        if (type.getSuperclass() != null && !type.getSuperclass().equals(Object.class)) {
            fields = getAllFields(fields, type.getSuperclass());
        }
        return fields;
    }
}
