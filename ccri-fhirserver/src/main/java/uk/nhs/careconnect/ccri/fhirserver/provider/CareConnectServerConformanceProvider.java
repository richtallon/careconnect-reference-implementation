package uk.nhs.careconnect.ccri.fhirserver.provider;

import ca.uhn.fhir.rest.annotation.Metadata;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.RestulfulServerConfiguration;
import org.hl7.fhir.dstu3.hapi.rest.server.ServerCapabilityStatementProvider;
import org.hl7.fhir.dstu3.model.CapabilityStatement;
import org.hl7.fhir.dstu3.model.CapabilityStatement.CapabilityStatementRestComponent;
import org.hl7.fhir.dstu3.model.CapabilityStatement.ResourceInteractionComponent;
import org.hl7.fhir.dstu3.model.DecimalType;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.UriType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

//import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.ApplicationContext;

@Configuration
public class CareConnectServerConformanceProvider extends ServerCapabilityStatementProvider {
	
	
	
@Value("${ccri.CRUD_read}")
private String CRUD_read12;

@Autowired
private CareConnectServerConformanceProvider ccscp;
/*	
	
    @Value("${ccri.CRUD_update}")
    private String CRUD_update;
    
    @Value("${ccri.CRUD_delete}")
    private String CRUD_delete;
    
    @Value("${ccri.CRUD_create}")
    private String CRUD_create;
    
    @Value("${ccri.role}")
    private String ccri_role;
    
    @Autowired
    FhirContext ctx;
	*/
	
  
	//private ApplicationContext applicationContext;
	//CareConnectServerConformanceProvider(ApplicationContext context) {
  //      this.applicationContext = context;
 //   }
	
    
    private boolean myCache = true;
    private volatile CapabilityStatement myCapabilityStatement;

    private RestulfulServerConfiguration serverConfiguration;

    private RestfulServer restfulServer;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CareConnectServerConformanceProvider.class);

    public String validate_flag2 = null ;

    private String oauth2authorize;

    private String oauth2token;

    private String oauth2register;
    
    
    public CareConnectServerConformanceProvider() {
        super();
    //    validate_flag2 = this.env.getProperty("ccri.validate_flag");
    }

    @Override
    public void setRestfulServer(RestfulServer theRestfulServer) {

        serverConfiguration = theRestfulServer.createConfiguration();
        restfulServer = theRestfulServer;
        super.setRestfulServer(theRestfulServer);
    }
    
    @Override
    @Metadata
     public CapabilityStatement getServerConformance(HttpServletRequest theRequest) {
    	
    	WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(theRequest.getServletContext());
    	log.info("restful2 Server not null = " + ctx.getEnvironment().getProperty("ccri.validate_flag"));
    	
    	 	String CRUD_update =  ctx.getEnvironment().getProperty("ccri.CRUD_update");
    	 	String CRUD_delete = ctx.getEnvironment().getProperty("ccri.CRUD_delete");
    	 	String CRUD_create = ctx.getEnvironment().getProperty("ccri.CRUD_create");
    	 	String CRUD_read = ctx.getEnvironment().getProperty("ccri.CRUD_read");
    	 
    	 	
    	 	oauth2authorize = ctx.getEnvironment().getProperty("ccri.oauth2.authorize");
    	 	oauth2token = ctx.getEnvironment().getProperty("ccri.oauth2.token");
    	 	oauth2register = ctx.getEnvironment().getProperty("ccri.oauth2.register");
    	    
        if (myCapabilityStatement != null && myCache) {
            return myCapabilityStatement;
        }
        
        CapabilityStatement myCapabilityStatement = super.getServerConformance(theRequest);
   //     myCapabilityStatement.setImplementationGuide((List<UriType>) new UriType(System.getProperty("ccri.guide")));
        
        
        //CapabilityStatement retVal = new CapabilityStatement();
        //retVal.
        myCapabilityStatement.getImplementationGuide().add(new UriType(System.getProperty("ccri.guide")));
        
   //     myCapabilityStatement 
        
        


        
       
        
        log.info("autowired value = " + CRUD_read12);
        
     //   CapabilityStatement.CapabilityStatementRestComponent rest = myCapabilityStatement.addRest();
        log.info("CRUD_read = " + CRUD_read + ", CRUD_update = " + CRUD_update + "CRUD_create = " + CRUD_create + ", CRUD_delete = " + CRUD_delete);
        if (restfulServer != null) {
              log.info("restful Server not null");
            for (CapabilityStatement.CapabilityStatementRestComponent nextRest : myCapabilityStatement.getRest()) {
              	nextRest.setMode(CapabilityStatement.RestfulCapabilityMode.SERVER);
                if (oauth2token != null && oauth2register !=null && oauth2authorize != null) 
                {
                	nextRest.getSecurity()
                            .addService().addCoding()
                            .setSystem("http://hl7.org/fhir/restful-security-service")
                            .setDisplay("SMART-on-FHIR")
                            .setSystem("SMART-on-FHIR");
                    Extension securityExtension = nextRest.getSecurity().addExtension()
                            .setUrl("http://fhir-registry.smarthealthit.org/StructureDefinition/oauth-uris");

                    securityExtension.addExtension()
                            .setUrl("authorize")
                            .setValue(new UriType(oauth2authorize));

                    securityExtension.addExtension()
                            .setUrl("register")
                            .setValue(new UriType(oauth2register));

                    securityExtension.addExtension()
                            .setUrl("token")
                            .setValue(new UriType(oauth2token));
                }

                for (CapabilityStatement.CapabilityStatementRestResourceComponent restResourceComponent : nextRest.getResource()) {
                    log.info("restResourceComponent.getType - " + restResourceComponent.getType());
                    
                    List<ResourceInteractionComponent> l = restResourceComponent.getInteraction();
                    for(int i=0;i<l.size();i++)
                    	if(CRUD_read.equals("false"))
                    	if (restResourceComponent.getInteraction().get(i).getCode().toString()=="READ")
                    	{
                    		restResourceComponent.getInteraction().remove(i);
                    	}	
                    for(int i=0;i<l.size();i++)
                    	if(CRUD_update.equals("false"))
                    	if (restResourceComponent.getInteraction().get(i).getCode().toString()=="UPDATE")
                    	{
                    		restResourceComponent.getInteraction().remove(i);
                    	}	
                    for(int i=0;i<l.size();i++)
                    	if(CRUD_create.equals("false"))
                    	if (restResourceComponent.getInteraction().get(i).getCode().toString()=="CREATE")
                    	{
                    		restResourceComponent.getInteraction().remove(i);
                    	}	
                    for(int i=0;i<l.size();i++)
                    	if(CRUD_delete.equals("false"))
                    	if (restResourceComponent.getInteraction().get(i).getCode().toString()=="DELETE")
                    	{
                    		restResourceComponent.getInteraction().remove(i);
                    	}	
                    
                    
                   for (IResourceProvider provider : restfulServer.getResourceProviders()) {
                	   	
                        log.info("Provider Resource - " + provider.getResourceType().getSimpleName());
                        if (restResourceComponent.getType().equals(provider.getResourceType().getSimpleName())
                                || (restResourceComponent.getType().contains("List") && provider.getResourceType().getSimpleName().contains("List")))
                            if (provider instanceof ICCResourceProvider) {
                                log.info("ICCResourceProvider - " + provider.getClass());
                                ICCResourceProvider resourceProvider = (ICCResourceProvider) provider;
                               
                                Extension extension = restResourceComponent.getExtensionFirstRep();
                                if (extension == null) {
                                    extension = restResourceComponent.addExtension();
                                }
                                extension.setUrl("http://hl7api.sourceforge.net/hapi-fhir/res/extdefs.html#resourceCount")
                                        .setValue(new DecimalType(resourceProvider.count()));
                            }
                    }
                }
            }
        }

        //myCapabilityStatement.children().get(1).getc
     
        return myCapabilityStatement;
    }

}
