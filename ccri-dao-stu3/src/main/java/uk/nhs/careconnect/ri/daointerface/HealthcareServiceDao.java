package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.ri.daointerface.transforms.HealthcareServiceEntityToFHIRHealthcareServiceTransformer;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.healthcareService.*;
import uk.nhs.careconnect.ri.entity.location.LocationEntity;
import uk.nhs.careconnect.ri.entity.organization.OrganisationEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static uk.nhs.careconnect.ri.daointerface.daoutils.MAXROWS;

@Repository
@Transactional
public class HealthcareServiceDao implements HealthcareServiceRepository {

    @PersistenceContext
    EntityManager em;


    @Autowired
    HealthcareServiceEntityToFHIRHealthcareServiceTransformer serviceEntityToFHIRHealthcareServiceTransformer;

    @Autowired
    ConceptRepository conceptDao;

    @Autowired
    PatientRepository patientDao;

    @Autowired
    OrganisationRepository organisationDao;

    @Autowired
    LocationRepository locationDao;

    @Autowired
    private CodeSystemRepository codeSystemSvc;

    private static final Logger log = LoggerFactory.getLogger(HealthcareServiceDao.class);


    @Override
    public void save(FhirContext ctx, HealthcareServiceEntity serviceEntity) {
        em.persist(serviceEntity);
    }

    @Override
    public HealthcareService read(FhirContext ctx, IdType theId) {

        if (daoutils.isNumeric(theId.getIdPart())) {
            HealthcareServiceEntity serviceEntity = (HealthcareServiceEntity) em.find(HealthcareServiceEntity.class, Long.parseLong(theId.getIdPart()));
            return serviceEntity == null
                    ? null
                    : serviceEntityToFHIRHealthcareServiceTransformer.transform(serviceEntity);

        } else {
            return null;
        }
    }

    @Override
    public HealthcareServiceEntity readEntity(FhirContext ctx, IdType theId) {
        if (daoutils.isNumeric(theId.getIdPart())) {
            HealthcareServiceEntity serviceEntity = (HealthcareServiceEntity) em.find(HealthcareServiceEntity.class, Long.parseLong(theId.getIdPart()));

            return serviceEntity;

        } else {
            return null;
        }
    }

    @Override
    public HealthcareService create(FhirContext ctx, HealthcareService service, IdType theId, String theConditional) {
        log.debug("HealthcareService.save");

        HealthcareServiceEntity serviceEntity = null;

        if (service.hasId()) serviceEntity = readEntity(ctx, service.getIdElement());

        if (theConditional != null) {
            try {
                if (theConditional.contains("https://tools.ietf.org/html/rfc4122")) {
                    URI uri = new URI(theConditional);

                    String scheme = uri.getScheme();
                    log.info("** Scheme = "+scheme);
                    String host = uri.getHost();
                    log.info("** Host = "+host);
                    String query = uri.getRawQuery();
                    log.debug(query);
                    String[] spiltStr = query.split("%7C");
                    log.debug(spiltStr[1]);

                    List<HealthcareServiceEntity> results = searchHealthcareServiceEntity(ctx,  new TokenParam().setValue(spiltStr[1]).setSystem("https://tools.ietf.org/html/rfc4122"),null, null, null,null);
                    for (HealthcareServiceEntity con : results) {
                        serviceEntity = con;
                        break;
                    }
                } else {
                    log.info("NOT SUPPORTED: Conditional Url = "+theConditional);
                }

            } catch (Exception ex) {

            }
        }

        if (serviceEntity == null) {
            serviceEntity = new HealthcareServiceEntity();
        }

        if (service.hasProvidedBy()) {

            OrganisationEntity organisationEntity = organisationDao.readEntity(ctx, new IdType(service.getProvidedBy().getReference()));
            if (organisationEntity != null) {
                serviceEntity.setProvidedBy(organisationEntity);
            }
        }
        if (service.hasActive()) {
            serviceEntity.setActive(service.getActive());
        }
        if (service.hasName()) {
            serviceEntity.setName(service.getName());
        }

        if (service.hasCategory()) {
            ConceptEntity code = conceptDao.findCode(service.getCategory().getCoding().get(0));
            if (code != null) { serviceEntity.setCategory(code); }
            else {
                log.info("Category: Missing System/Code = "+ service.getCategory().getCoding().get(0).getSystem() +" code = "+service.getCategory().getCoding().get(0).getCode());

                throw new IllegalArgumentException("Missing System/Code = "+ service.getCategory().getCoding().get(0).getSystem()
                        +" code = "+service.getCategory().getCoding().get(0).getCode());
            }
        }

        em.persist(serviceEntity);

        for (Identifier identifier : service.getIdentifier()) {
            HealthcareServiceIdentifier serviceIdentifier = null;

            for (HealthcareServiceIdentifier orgSearch : serviceEntity.getIdentifiers()) {
                if (identifier.getSystem().equals(orgSearch.getSystemUri()) && identifier.getValue().equals(orgSearch.getValue())) {
                    serviceIdentifier = orgSearch;
                    break;
                }
            }
            if (serviceIdentifier == null)  serviceIdentifier = new HealthcareServiceIdentifier();

            serviceIdentifier.setValue(identifier.getValue());
            serviceIdentifier.setSystem(codeSystemSvc.findSystem(identifier.getSystem()));
            serviceIdentifier.setService(serviceEntity);
            em.persist(serviceIdentifier);
        }
        
        for (Reference reference : service.getLocation()) {
            LocationEntity locationEntity = locationDao.readEntity(ctx, new IdType(reference.getReference()));
            if (locationEntity != null) {
                HealthcareServiceLocation location = new HealthcareServiceLocation();
                location.setLocation(locationEntity);
                location.setHealthcareService(serviceEntity);
                em.persist(location);
            }
        }
        for (CodeableConcept concept :service.getSpecialty()) {

            if (concept.getCoding().size() > 0 && concept.getCoding().get(0).getCode() !=null) {
                ConceptEntity conceptEntity = conceptDao.findAddCode(concept.getCoding().get(0));
                if (conceptEntity != null) {
                    HealthcareServiceSpecialty specialtyEntity = null;
                    // Look for existing categories
                    for (HealthcareServiceSpecialty cat :serviceEntity.getSpecialties()) {
                        if (cat.getSpecialty().getCode().equals(concept.getCodingFirstRep().getCode())) specialtyEntity = cat;
                    }
                    if (specialtyEntity == null) specialtyEntity = new HealthcareServiceSpecialty();

                    specialtyEntity.setSpecialty(conceptEntity);
                    specialtyEntity.setHealthcareService(serviceEntity);
                    em.persist(specialtyEntity);
                    serviceEntity.getSpecialties().add(specialtyEntity);
                }
                else {
                    log.info("Missing ServiceRequested. System/Code = "+ concept.getCoding().get(0).getSystem() +" code = "+concept.getCoding().get(0).getCode());
                    throw new IllegalArgumentException("Missing System/Code = "+ concept.getCoding().get(0).getSystem() +" code = "+concept.getCoding().get(0).getCode());
                }
            }
        }

        for (CodeableConcept concept :service.getType()) {

            if (concept.getCoding().size() > 0 && concept.getCoding().get(0).getCode() !=null) {
                ConceptEntity conceptEntity = conceptDao.findAddCode(concept.getCoding().get(0));
                if (conceptEntity != null) {
                    HealthcareServiceType type = null;
                    // Look for existing categories
                    for (HealthcareServiceType cat :serviceEntity.getTypes()) {
                        if (cat.getType_().getCode().equals(concept.getCodingFirstRep().getCode())) type = cat;
                    }
                    if (service == null) type = new HealthcareServiceType();

                    type.setType_(conceptEntity);
                    type.setHealthcareService(serviceEntity);
                    em.persist(type);
                    serviceEntity.getTypes().add(type);
                }
                else {
                    log.info("Missing ServiceRequested. System/Code = "+ concept.getCoding().get(0).getSystem() +" code = "+concept.getCoding().get(0).getCode());
                    throw new IllegalArgumentException("Missing System/Code = "+ concept.getCoding().get(0).getSystem() +" code = "+concept.getCoding().get(0).getCode());
                }
            }
        }

        for (ContactPoint telecom : service.getTelecom()) {
            HealthcareServiceTelecom serviceTelecom = null;

            for (HealthcareServiceTelecom orgSearch : serviceEntity.getTelecoms()) {
                if (telecom.getValue().equals(orgSearch.getValue())) {
                    serviceTelecom = orgSearch;
                    break;
                }
            }
            if (serviceTelecom == null) {
                serviceTelecom = new HealthcareServiceTelecom();
                serviceTelecom.setHealthcareService(serviceEntity);
            }

            serviceTelecom.setValue(telecom.getValue());
            serviceTelecom.setSystem(telecom.getSystem());
            serviceTelecom.setTelecomUse(telecom.getUse());

            em.persist(serviceTelecom);
        }
        return serviceEntityToFHIRHealthcareServiceTransformer.transform(serviceEntity);

    }

    @Override
    public List<HealthcareService> searchHealthcareService(FhirContext ctx, TokenParam identifier, StringParam name, TokenOrListParam codes, TokenParam id, ReferenceParam organisation) {
        List<HealthcareServiceEntity> qryResults = searchHealthcareServiceEntity(ctx,identifier,name, codes,id,organisation);
        List<HealthcareService> results = new ArrayList<>();

        for (HealthcareServiceEntity healthcareServiceEntity : qryResults) {
            HealthcareService healthcareService = serviceEntityToFHIRHealthcareServiceTransformer.transform(healthcareServiceEntity);
            results.add(healthcareService);
        }

        return results;
    }

    @Override
    public List<HealthcareServiceEntity> searchHealthcareServiceEntity(FhirContext ctx, TokenParam identifier, StringParam name, TokenOrListParam codes, TokenParam id, ReferenceParam organisation) {
        List<HealthcareServiceEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<HealthcareServiceEntity> criteria = builder.createQuery(HealthcareServiceEntity.class);
        Root<HealthcareServiceEntity> root = criteria.from(HealthcareServiceEntity.class);


        List<Predicate> predList = new LinkedList<Predicate>();
        List<Organization> results = new ArrayList<Organization>();

        if (identifier !=null)
        {
            Join<HealthcareServiceEntity, HealthcareServiceIdentifier> join = root.join("identifiers", JoinType.LEFT);

            Predicate p = builder.equal(join.get("value"),identifier.getValue());
            predList.add(p);
            // TODO predList.add(builder.equal(join.get("system"),identifier.getSystem()));

        }
        if (id != null) {
            Predicate p = builder.equal(root.get("id"),id.getValue());
            predList.add(p);
        }
        if (name !=null)
        {

            Predicate p =
                    builder.like(
                            builder.upper(root.get("name").as(String.class)),
                            builder.upper(builder.literal(name.getValue()+"%"))
                    );

            predList.add(p);
        }
        


        Predicate[] predArray = new Predicate[predList.size()];
        predList.toArray(predArray);
        if (predList.size()>0)
        {
            criteria.select(root).where(predArray);
        }
        else
        {
            criteria.select(root);
        }

        qryResults = em.createQuery(criteria).setMaxResults(MAXROWS).getResultList();

        return qryResults;
    }

    @Override
    public Long count() {
        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(HealthcareServiceEntity.class)));
        return em.createQuery(cq).getSingleResult();
    }
}
