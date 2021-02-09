package com.embl.api;


import com.embl.PersonApplication;
import com.embl.document.Person;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@SpringBootTest(classes = PersonApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PersonControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @Autowired
    private MongoTemplate mongoTemplate;

    private String getRootUrl() {
        return "http://localhost:" + port + "/embl-app";
    }

    @BeforeEach
    public void setUp(){
        Person person = new Person();
        person.setFirst_name("John");
        person.setLast_name("J");
        person.setAge("50");
        person.setFavourite_colour("Blue");
        mongoTemplate.save(person);

    }

    @AfterEach
    public void tearDown(){
        mongoTemplate.dropCollection("person");
    }

    @Test
    public void testAddPerson() {
        //setup
        Person person = new Person();
        person.setFirst_name("New John");
        person.setLast_name("J");
        person.setAge("50");
        person.setFavourite_colour("Blue");

        //test
        ResponseEntity<Person> postResponse = restTemplate.postForEntity( getRootUrl() +"/person/add", person, Person.class);

        //verify
        Assertions.assertEquals(HttpStatus.OK, postResponse.getStatusCode());
        Assertions.assertNotNull(postResponse.getBody());
    }


    @Test
    public void testGetAllPersons() {

        //test
        ResponseEntity<List> getResponse = restTemplate.getForEntity( getRootUrl() +"/persons",List.class);

        //verify
        Assertions.assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        Assertions.assertEquals(1, getResponse.getBody().size());
    }

    @Test
    public void testGetPerson() {
        //setup
        ResponseEntity<Person[]> allPersons = restTemplate.getForEntity( getRootUrl() +"/persons",Person[].class);
        String id = Arrays.stream(allPersons.getBody()).findFirst().get().getId();

        //test
        ResponseEntity<Person> getResponse = restTemplate.getForEntity( getRootUrl() +"/person/" + id ,Person.class);

        //verify
        Assertions.assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        Assertions.assertNotNull(getResponse.getBody());
    }


    @Test
    public void testDeletePerson() {
        //setup
        ResponseEntity<Person[]> allPersons = restTemplate.getForEntity( getRootUrl() +"/persons",Person[].class);
        String id = Arrays.stream(allPersons.getBody()).findFirst().get().getId();

        //test
        restTemplate.delete( getRootUrl() +"/person/delete/" + id );
        ResponseEntity<Person> getResponse = restTemplate.getForEntity( getRootUrl() +"/person/" + id ,Person.class);

        //verify
        Assertions.assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        Assertions.assertNull(getResponse.getBody());
    }

    @Test
    public void testBadRequestException(){
        ResponseEntity<Person[]> allPersons = restTemplate.getForEntity( getRootUrl() +"/persons",Person[].class);
        String id = Arrays.stream(allPersons.getBody()).findFirst().get().getId();
        ResponseEntity<String> response = restTemplate
                .exchange( getRootUrl() +"/person/patch/"+id,
                        HttpMethod.PATCH,
                        new HttpEntity<Map<String,String>>(Collections.singletonMap("INVALID_PROPERTY", "John")),
                        String.class);
        Assertions.assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testPersonNotFoundException(){
        ResponseEntity<String> response = restTemplate.exchange( getRootUrl() +"/person/delete/1234",HttpMethod.DELETE,null,String.class);
        Assertions.assertEquals(response.getStatusCode(), HttpStatus.NOT_FOUND);
    }

}
