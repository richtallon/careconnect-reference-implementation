package uk.nhs.careconnect.ri.provider;


import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.instance.model.IdType;
import org.hl7.fhir.instance.model.Location;
import org.hl7.fhir.instance.model.OperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.careconnect.ri.OperationOutcomeFactory;
import uk.nhs.careconnect.ri.SystemCode;
import uk.nhs.careconnect.ri.dao.Location.LocationRepository;

import java.util.List;

@Component
public class LocationResourceProvider implements IResourceProvider {


    @Autowired
    private LocationRepository locationDao;

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Location.class;
    }


    @Search
    public List<Location> getLo(@RequiredParam(name = Location.SP_IDENTIFIER) TokenParam identifierCode) {
        return locationDao.searchLocation(identifierCode);
    }

    @Read()
    public Location getLocation(@IdParam IdType locationId) {

        Location location = locationDao.read(locationId);

        if ( location == null) {
            throw OperationOutcomeFactory.buildOperationOutcomeException(
                    new ResourceNotFoundException("No patient details found for patient ID: " + locationId.getIdPart()),
                    SystemCode.PRACTITIONER_NOT_FOUND, OperationOutcome.IssueType.NOTFOUND);
        }

        return location;
    }


}
