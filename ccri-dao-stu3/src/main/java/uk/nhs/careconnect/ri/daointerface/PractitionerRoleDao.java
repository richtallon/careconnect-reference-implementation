package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.ri.daointerface.transforms.PractitionerRoleToFHIRPractitionerRoleTransformer;
import uk.nhs.careconnect.ri.entity.Terminology.ConceptEntity;
import uk.nhs.careconnect.ri.entity.organization.OrganisationEntity;
import uk.nhs.careconnect.ri.entity.practitioner.PractitionerEntity;
import uk.nhs.careconnect.ri.entity.practitioner.PractitionerRole;
import uk.nhs.careconnect.ri.entity.practitioner.PractitionerRoleIdentifier;
import uk.nhs.careconnect.ri.entity.practitioner.PractitionerSpecialty;
import uk.org.hl7.fhir.core.Stu3.CareConnectSystem;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Repository
@Transactional
public class PractitionerRoleDao implements PractitionerRoleRepository {

    @PersistenceContext
    EntityManager em;

    @Autowired
    private PractitionerRoleToFHIRPractitionerRoleTransformer
            practitionerRoleToFHIRPractitionerRoleTransformer;

    @Autowired
    private OrganisationRepository organisationDao;

    @Autowired
    private PractitionerRepository practitionerDao;

    @Autowired
    private CodeSystemRepository codeSystemDao;

    @Autowired
    private ConceptRepository codeDao;

    @Override
    public void save(PractitionerRole practitioner) {

    }

    @Override
    public org.hl7.fhir.dstu3.model.PractitionerRole read(IdType theId) {

        PractitionerRole roleEntity = (PractitionerRole) em.find(PractitionerRole.class,Long.parseLong(theId.getIdPart()));
        return roleEntity == null
                ? null
                : practitionerRoleToFHIRPractitionerRoleTransformer.transform(roleEntity);
    }

    @Override
    public PractitionerRole readEntity(IdType theId) {
       return (PractitionerRole) em.find(PractitionerRole.class,Long.parseLong(theId.getIdPart()));
    }

    private static final Logger log = LoggerFactory.getLogger(PractitionerRoleDao.class);


    @Override
    public org.hl7.fhir.dstu3.model.PractitionerRole create(org.hl7.fhir.dstu3.model.PractitionerRole practitionerRole, IdType theId, String theConditional) {

        PractitionerRole roleEntity = null;
        if (practitionerRole.hasId()) {
            roleEntity = (PractitionerRole) em.find(PractitionerRole.class, Long.parseLong(practitionerRole.getId()));
        }
        log.trace("theConditionalUrl = "+theConditional);
        if (theConditional != null) {
            try {
                log.trace("Contains is not null");
                //CareConnectSystem.ODSPractitionerCode
                if (theConditional.contains("fhir.nhs.uk/Id/sds-user-id")) {
                    log.trace("Contains "+theConditional);
                    URI uri = new URI(theConditional);

                    String scheme = uri.getScheme();
                    String host = uri.getHost();
                    String query = uri.getRawQuery();
                    log.trace(query);
                    String[] spiltStr = query.split("%7C");
                    log.trace(spiltStr[1]);

                    List<PractitionerRole> results = searchEntity(new TokenParam().setValue(spiltStr[1]).setSystem(CareConnectSystem.SDSUserId), null, null);
                    for (PractitionerRole org : results) {
                        roleEntity = org;
                        break;
                    }
                } else {
                    log.trace("NOT SUPPORTED: Conditional Url = " + theConditional);
                }

            } catch (Exception ex) {
                log.error("Exception "+ex.getMessage());
            }
        }
        if (roleEntity == null) {
            roleEntity = new PractitionerRole();
        }
        if (practitionerRole.getPractitioner() != null) {
            roleEntity.setPractitioner(practitionerDao.readEntity(new IdType(practitionerRole.getPractitioner().getReference())));
        }

        if (practitionerRole.getOrganization() != null) {
            roleEntity.setOrganisation(organisationDao.readEntity(new IdType(practitionerRole.getOrganization().getReference())));
        }

        if (practitionerRole.getCode().size() > 0) {
            if (practitionerRole.getCode().get(0).getCoding().get(0).getSystem().equals(CareConnectSystem.SDSJobRoleName)) {
                roleEntity.setRole(codeDao.findCode(practitionerRole.getCode().get(0).getCoding().get(0).getSystem(), practitionerRole.getCode().get(0).getCoding().get(0).getCode()));
            }
        }
        em.persist(roleEntity);

        for (CodeableConcept specialty : practitionerRole.getSpecialty()) {
            Boolean found = false;
            ConceptEntity specialtyConcept = codeDao.findCode(specialty.getCoding().get(0).getSystem()
                    , specialty.getCoding().get(0).getCode());
            for (PractitionerSpecialty searchSpecialty : roleEntity.getSpecialties()) {
                log.trace("Already has specialty = " + searchSpecialty.getSpecialty().getCode() + " code "+searchSpecialty.getSpecialty().getSystem());
                if (searchSpecialty.getSpecialty().getCode().equals(specialtyConcept.getCode())
                        && searchSpecialty.getSpecialty().getSystem().equals(specialtyConcept.getSystem())) found = true;
            }
            try {
                if (!found){
                    log.trace("not found! specialty = " + specialty.getCoding().get(0).getCode() + " code "+specialty.getCoding().get(0).getSystem());
                    PractitionerSpecialty practitionerSpecialty = new PractitionerSpecialty();
                    practitionerSpecialty.setPractitionerRole(roleEntity);
                    practitionerSpecialty.setSpecialty(specialtyConcept);
                    em.persist(practitionerSpecialty);
                    roleEntity.getSpecialties().add(practitionerSpecialty);
                }
            } catch (Exception ex) {

            }
        }

        Boolean found = false;
        for (Identifier identifier : practitionerRole.getIdentifier()) {
            log.trace("Recieved identifier = " + identifier.getSystem() + " code "+identifier.getValue());

            for (PractitionerRoleIdentifier identifierEntity : roleEntity.getIdentifiers()) {
                log.trace("Existing identifier = " + identifierEntity.getSystemUri() + " code "+identifierEntity.getValue());

                if (identifier.getSystem().equals(identifierEntity.getSystemUri()) && identifier.getValue().equals(identifierEntity.getValue())) {
                    found = true;
                }
            }
            if (!found) {
                log.trace("Not found Identifier!");
                PractitionerRoleIdentifier ident = new PractitionerRoleIdentifier();
                ident.setValue(identifier.getValue());
                ident.setPractitionerRole(roleEntity);
                ident.setSystem(codeSystemDao.findSystem(identifier.getSystem()));
                em.persist(ident);
                roleEntity.getIdentifiers().add(ident);
            }
        }
        return practitionerRole; //roleEntity;
    }

    @Override
    public List<PractitionerRole> searchEntity(
            TokenParam identifier
            , ReferenceParam practitioner
            , ReferenceParam organisation) {

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<PractitionerRole> criteria = builder.createQuery(PractitionerRole.class);
        Root<PractitionerRole> root = criteria.from(PractitionerRole.class);

        List<Predicate> predList = new LinkedList<Predicate>();

        if (identifier !=null)
        {
            log.trace("Search on value = "+identifier.getValue());

            Join<PractitionerRole, PractitionerRoleIdentifier> join = root.join("identifiers", JoinType.LEFT);

            Predicate p = builder.equal(join.get("value"),identifier.getValue());
            predList.add(p);
            // TODO predList.add(builder.equal(join.get("system"),identifier.getSystem()));

        }
        if (practitioner != null) {
            Join<PractitionerRole,PractitionerEntity> joinPractitioner = root.join("practitionerEntity",JoinType.LEFT);
            Predicate p = builder.equal(joinPractitioner.get("id"),practitioner.getIdPart());
            predList.add(p);
        }
        if (organisation != null) {
            Join<PractitionerRole,OrganisationEntity> joinOrganisation = root.join("managingOrganisation",JoinType.LEFT);
            Predicate p = builder.equal(joinOrganisation.get("id"),organisation.getIdPart());
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

        return em.createQuery(criteria).getResultList();

    }


    @Override
    public List<org.hl7.fhir.dstu3.model.PractitionerRole> search(
            TokenParam identifier
            , ReferenceParam practitioner
            , ReferenceParam organisation) {
        List<org.hl7.fhir.dstu3.model.PractitionerRole> results = new ArrayList<>();
        List<PractitionerRole> roles = searchEntity(identifier,practitioner,organisation);
        for (PractitionerRole role : roles) {
             results.add(practitionerRoleToFHIRPractitionerRoleTransformer.transform(role));
        }
        return results;
    }

}