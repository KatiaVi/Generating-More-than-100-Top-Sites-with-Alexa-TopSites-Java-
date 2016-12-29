# Generating-More-than-100-Top-Sites-with-Alexa-TopSites-Java-

This demo is a modified version of the java sample code provided by Alexa Top Sites here: https://aws.amazon.com/code/Alexa-Top-Sites/405

The additional features of this demo are:
  1. Allows user to request more than 100 Top Sites within a certain region
  2. Creates a TopSites.txt file with a list of the top sites returned by the query
  
 Note: This demo currently returns the top 1000 sites within a specified region
  
#Directions for Using this Demo
Here are the directions for how to use this demo (part of these directions are from the orginal java code sample README available
at the website mentioned above):

1. Sign up for an Amazon Web Services account at http://aws.amazon.com (Note that you must have a valid credit card)
2. Get your Access Key ID and Secret Access Key
3. Sign up for Alexa Top Sites at http://aws.amazon.com/alexatopsites
4. Modify the value of NUMBER_TO_RETURN to any value from 1 - 100 corresponding to the number of sites to be returned per query (default value of 100)
5. Modify the value of numRequests to the (number of top sites you want)/NUMBER_TO_RETURN, (default value of 10)
ex: if the number top sites you want is 1000 then numRequests = 10 (where NUMBER_TO_RETURN =100)
6. To Compile TopSites.java: javac TopSites.java
7. To Run: java TopSites ACCESS_KEY_ID SECRET_ACCESS_KEY [COUNTRY_CODE] Country code is optional.


#Possible Errors and Reasons for Them
If you are getting "Not Authorized" messages, you probably have one of the following problems:

1. Your access key or secret key were not entered properly.  Please re-check that they are correct.
2. You did not sign up for Alexa Top Sites at http://aws.amazon.com/alexatopsites (This step is separate from signing 
up for Amazon Web Services.)
3. Your credit card was not valid.

If you are getting "Request Expired" messages, please check that the date 
and time are properly set on your computer.



  
 
