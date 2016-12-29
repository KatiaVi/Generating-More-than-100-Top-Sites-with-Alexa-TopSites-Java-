/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import sun.misc.BASE64Encoder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.SignatureException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 *This demo shows how to successfully make a series of queries to Alexa TopSites and how to generate a text document with the list of top sites that are returned from the query.
 *Note: this demo returns the top 1000 sites from the specified region
 *Note: you must sign up for Alexa Top Sites
 * at http://aws.amazon.com/alexatopsites before running this demo.
 * 
 */
public class TopSites {
    protected static final String ACTION_NAME = "TopSites";
    protected static final String RESPONSE_GROUP_NAME = "Country";
    protected static final String SERVICE_HOST = "ats.amazonaws.com";
    protected static final String AWS_BASE_URL = "http://" + SERVICE_HOST + "/?";

    protected static final int NUMBER_TO_RETURN = 100; //the number of sites to be returned per query, value can range from 1 to 100
    protected static final String DATEFORMAT_AWS = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final String HASH_ALGORITHM = "HmacSHA256";

    private String accessKeyId;
    private String secretAccessKey;
    private String countryCode;
    private int startNumber;

    public TopSites(String accessKeyId, String secretAccessKey, String countryCode,int startNumber) {
        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;
        this.countryCode = countryCode;
        this.startNumber = startNumber;
    }

    /**
     * Computes RFC 2104-compliant HMAC signature.
     *
     * @param data (data to be signed)
     * @return base64-encoded RFC 2104-compliant HMAC signature.
     * @throws java.security.SignatureException
     *          when signature generation fails
     */
    protected String generateSignature(String data)
        throws java.security.SignatureException {
        String result;
        try {
            // get an hmac key from the raw key bytes
            SecretKeySpec signingKey =
                new SecretKeySpec(secretAccessKey.getBytes(),
                                  HASH_ALGORITHM);

            // get a mac instance and initialize with the signing key
            Mac mac = Mac.getInstance(HASH_ALGORITHM);
            mac.init(signingKey);

            // compute the hmac on input data bytes
            byte[] rawHmac = mac.doFinal(data.getBytes());

            // base64-encode the hmac
            result = new BASE64Encoder().encode(rawHmac);

        } catch (Exception e) {
            throw new SignatureException("Failed to generate HMAC : "
                                         + e.getMessage());
        }
        return result;
    }

    /**
     * Generates a timestamp for use with AWS request signing
     *
     * @param date (current date)
     * @return timestamp
     */
    public static String getTimestampFromLocalTime(Date date) {
        SimpleDateFormat format = new SimpleDateFormat(DATEFORMAT_AWS);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return format.format(date);
    }

    /**
     * Builds the query string
     */
    protected String buildQuery() throws UnsupportedEncodingException {
        String timestamp = getTimestampFromLocalTime(Calendar.getInstance().getTime());

        // TreeMap puts keys in alphabetical order
        Map<String, String> queryParams = new TreeMap<String, String>();
        queryParams.put("Action", ACTION_NAME);
        queryParams.put("ResponseGroup", RESPONSE_GROUP_NAME);
        queryParams.put("AWSAccessKeyId", accessKeyId);
        queryParams.put("Timestamp", URLEncoder.encode(timestamp, "UTF-8"));
        queryParams.put("CountryCode", countryCode);
        queryParams.put("Count", "" + NUMBER_TO_RETURN);
        queryParams.put("Start", "" + startNumber);
        queryParams.put("SignatureVersion", "2");
        queryParams.put("SignatureMethod", HASH_ALGORITHM);

        String query = "";
        boolean first = true;
        for (String name : queryParams.keySet()) {
            if (first)
                first = false;
            else
                query += "&";

            query += name + "=" + queryParams.get(name);
        }

        return query;
    }

    /**
     * Parses response and prints results
     *
     * @param in stream containing response xml
     */
    private static void parseResponse(InputStream in, BufferedWriter bw) throws Exception {
        // Parse the response
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document responseDoc = dbf.newDocumentBuilder().parse(in);

        NodeList responses = responseDoc.getElementsByTagNameNS("*", "Site");

        for (int i = 0; i < responses.getLength(); i++) {
            Element response = (Element) responses.item(i);
            Element node = (Element)
                response.getElementsByTagNameNS("*", "DataUrl").item(0);
            String siteUrl = node.getFirstChild().getNodeValue();
            System.out.println(siteUrl);

            //writes the siteURL to the TopSites.txt file
            bw.write(siteUrl);
            bw.newLine();
        }
    }

    /**
     * Makes a request to the Alexa Top Sites web service
     * @param args
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: java AlexaTopSites ACCESS_KEY_ID " +
                               "SECRET_ACCESS_KEY [COUNTRY_CODE]");
            System.exit(-1);
        }
        String accessKey = args[0];
        String secretKey = args[1];
        String countryCode =  (args.length > 2) ? args[2] : "";

        int startNumber;

        /*******CHANGE THIS VARIABLE TO (THE NUMBER OF TOP SITES YOU WANT)/(NUMBER_TO_RETURN)*********/

        int numRequests = 10;   //i.e. 1000 sites/100 = 10

        /*********************************************************************************************/
        
        //creates a a TopSites.txt file to write the top sites to
        File fout = new File("TopSites.txt");
        FileOutputStream fos = new FileOutputStream(fout);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
        
        
        for(int i = 1; i < numRequests * NUMBER_TO_RETURN + 1; i = i + NUMBER_TO_RETURN){
            startNumber = i;
            TopSites topSites = new TopSites(accessKey, secretKey, countryCode,startNumber);
            String query = topSites.buildQuery();

            String toSign = "GET\n" + SERVICE_HOST + "\n/\n" + query;

            System.out.println("String to sign:\n" + toSign + "\n");

            String signature = topSites.generateSignature(toSign);

            String uri = AWS_BASE_URL + query +
                "&Signature=" + URLEncoder.encode(signature, "UTF-8");

            // Make request
            System.out.println("\nMaking request to: " + uri + "\n");

            URL url = new URL(uri);
            URLConnection conn = url.openConnection();
            InputStream in = conn.getInputStream();

            parseResponse(in,bw);
        }
        bw.close();
    }

}

