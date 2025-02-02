# USER MANAGEMENT APPLICATION

Java-based application for managing user entity in database.

## Controllers Functionality

![img.png](img.png)

## Set Up Instructions

Firstly, for successfully running application, ensure you have installed on your PC JDK 17, Maven and Postman.

Clone the repository:

`git clone git@github.com:oksana-rudenko/user-management.git`

Build the project:

`mvn clean package`

Run the project. Try end-points by using Postman (or Swagger).
Use Json template for creating, updating, deleting or searching entities:

{

    "email": "alice@gmail.com",
    "firstName": "Alice",
    "lastName": "Morgan",
    "birthDate": "1995-12-26",
    "address": "Kyiv",
    "phoneNumber": "+380676767677"
}

## Working with Postman

A few examples of trying project with Postman.
1) Creating a new user
![img_1.png](img_1.png)
2) Updating some user's fields
![img_2.png](img_2.png)
3) Updating all user's fields
![img_3.png](img_3.png)
4) Deleting user
![img_5.png](img_5.png)
5) Searching users by birthdate range
![img_4.png](img_4.png)

##  Technologies

1. [x] JDK 17
2. [x] Spring Boot v.3.1.4
3. [x] Spring Boot Web 3.1.4
4. [x] Maven 3.6.3
5. [x] Jackson
6. [x] Swagger
7. [x] Postman
8. [x] Junit
9. [x] Mockito
