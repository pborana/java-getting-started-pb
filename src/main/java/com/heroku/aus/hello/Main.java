package com.heroku.aus.hello;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import spark.ModelAndView;
import spark.template.freemarker.FreeMarkerEngine;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

public class Main {

  public static void main(String[] args) {

	int port = System.getenv("PORT") != null ? Integer.valueOf(System.getenv("PORT")) : 8080;
	  
    port(port);
    staticFileLocation("/public");

    get("/hello", (req, res) -> "Hello World");
    
   get("/.well-known/acme-challenge/wwizlMm0sN1MzqpYvpCSc7zVw7Jid-RSV6XfQbK9GnU", (req, res) -> "wwizlMm0sN1MzqpYvpCSc7zVw7Jid-RSV6XfQbK9GnU.edcuzuKjUypi2zdt6-5_CLqv5X5y7iSKUx8pC4RnpLE");

    get("/", (request, response) -> {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("message", "Hello World!");

        return new ModelAndView(attributes, "index.ftl");
    }, new FreeMarkerEngine());

    HikariConfig config = new  HikariConfig();
    config.setJdbcUrl(System.getenv("JDBC_DATABASE_URL"));
    final HikariDataSource dataSource = (config.getJdbcUrl() != null) ?
      new HikariDataSource(config) : new HikariDataSource();

    get("/db", (req, res) -> {
      Map<String, Object> attributes = new HashMap<>();
      try(Connection connection = dataSource.getConnection()) {
        Statement stmt = connection.createStatement();
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (tick timestamp)");
        stmt.executeUpdate("INSERT INTO ticks VALUES (now())");
        ResultSet rs = stmt.executeQuery("SELECT tick FROM ticks");

        ArrayList<String> output = new ArrayList<String>();
        while (rs.next()) {
          output.add( "Read from DB: " + rs.getTimestamp("tick"));
        }

        attributes.put("results", output);
        return new ModelAndView(attributes, "db.ftl");
      } catch (Exception e) {
        attributes.put("message", "There was an error: " + e);
        return new ModelAndView(attributes, "error.ftl");
      }
    }, new FreeMarkerEngine());

  }

}
