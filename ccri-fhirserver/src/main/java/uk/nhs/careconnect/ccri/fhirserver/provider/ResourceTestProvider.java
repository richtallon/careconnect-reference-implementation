package uk.nhs.careconnect.ccri.fhirserver.provider;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Validate;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.io.Reader;

@Component
public class ResourceTestProvider {

	@Value("${ccri.validate_flag}")
	private Boolean validate_flag;
	
	@Value("${ccri.tkw_server}")
	private String tkw_server;
	
    @Autowired
    FhirContext ctx;
    
	HttpResponse response;
	Reader reader;
    private static final Logger log = LoggerFactory.getLogger(ResourceTestProvider.class);

    
    private HttpClient getHttpClient(){
        final HttpClient httpClient = HttpClientBuilder.create().build();
        return httpClient;
    }
    public MethodOutcome testResource(@ResourceParam IBaseResource resourceToValidate,
                                  @Validate.Mode ValidationModeEnum theMode,
                                  @Validate.Profile String theProfile) {
        log.info("Checking testresource" + validate_flag);
        MethodOutcome retVal = new MethodOutcome();
    	if(!validate_flag)
    	{
    	//	MethodOutcome retVal = new MethodOutcome();
    		retVal.setOperationOutcome(null);
    		return retVal;
    	}
    	
    	
         
       
    	final HttpClient client1 = getHttpClient();
        final HttpPost request = new HttpPost(tkw_server);
        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/fhir+json");
        request.setHeader(HttpHeaders.ACCEPT, "application/fhir+json");
        try {
				request.setEntity(new StringEntity(ctx.newJsonParser().encodeResourceToString(resourceToValidate)));
				response = client1.execute(request);
				reader = new InputStreamReader(response.getEntity().getContent());
				
				 IBaseResource resource = ctx.newJsonParser().parseResource(reader);
				 if(resource instanceof OperationOutcome)
		         {
		            OperationOutcome operationOutcome = (OperationOutcome) resource;
		            log.info("Issue Count = " + operationOutcome.getIssue().size());
		            log.info(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(operationOutcome));
		            retVal.setOperationOutcome(operationOutcome);
		         }
		         else
		         {
		            throw new InternalErrorException("Server Error", (OperationOutcome) resource);
		         }

			
        	} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
      		
        return retVal;

}

}