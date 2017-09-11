Feature: Patient


Scenario: Patient Read
        Given I add a Patient with an Id of 1
        Then the result should be a FHIR Patient
        And the results should be a CareConnect Patient

  Scenario: Patient first name Search Found
        Given I search for a Patient with a family name of Kanfeld
        Then the result should be a list with 1 entry
        And they shall all be FHIR Patient resources
        And the results should be a list of CareConnect Patients

   Scenario: Patient first name Search NOT Found
         Given I search for a Patient with a family name of Smith
         Then the result should be a list with 0 entry

   Scenario: Patient first name Search Found
         Given I search for a Patient with a given name of Bernie
         Then the result should be a list with 1 entry
         And they shall all be FHIR Patient resources
         And the results should be a list of CareConnect Patients

   Scenario: Patient first name Search NOT Found
         Given I search for a Patient with a given name of Eric
         Then the result should be a list with 0 entry


    Scenario: Patient birthdate name Search Found
         Given I search for a Patient with a birthdate of '1998-03-06'
         Then the result should be a list with 1 entry
         And they shall all be FHIR Patient resources
         And the results should be a list of CareConnect Patients


     Scenario: Patient birthdate Search NOT Found
             Given I search for a Patient with a birthdate of '1918-02-17'
             Then the result should be a list with 0 entry


   Scenario: Patient gender Search Found
             Given I search for a Patient with a gender of FEMALE
             Then the result should be a list with 1 entry
             And they shall all be FHIR Patient resources
             And the results should be a list of CareConnect Patients

  Scenario: Patient gender Search NOT Found
             Given I search for a Patient with a gender of MALE
             Then the result should be a list with 0 entry


 Scenario: Patient identifier Search Found
             Given I search for a Patient with a NHSNumber of 9876543210
             Then the result should be a list with 1 entry
             And they shall all be FHIR Patient resources
             And the results should be a list of CareConnect Patients

 Scenario: Patient identifier Search Found
              Given I search for a Patient with a NHSNumber of 1234567890
              Then the result should be a list with 0 entry


  Scenario: Patient name (given supplied) Search Found
               Given I search for a Patient with a name of "Bernie"
               Then the result should be a list with 1 entry
               And they shall all be FHIR Patient resources
               And the results should be a list of CareConnect Patients

    Scenario: Patient name (family supplied) Search Found
                 Given I search for a Patient with a name of "Kanfeld"
                 Then the result should be a list with 1 entry
                 And they shall all be FHIR Patient resources
                 And the results should be a list of CareConnect Patients

   Scenario: Patient name Search NOT Found
                 Given I search for a Patient with a name of "Eric"
                 Then the result should be a list with 0 entry




