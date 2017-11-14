package uk.nhs.careconnect.ri.daointerface;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.IdType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.nhs.careconnect.ri.daointerface.transforms.ConditionEntityToFHIRConditionTransformer;
import uk.nhs.careconnect.ri.entity.condition.ConditionEntity;
import uk.nhs.careconnect.ri.entity.patient.PatientEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Repository
@Transactional
public class ConditionDao implements ConditionRepository {

    @PersistenceContext
    EntityManager em;

    @Autowired
    ConditionEntityToFHIRConditionTransformer conditionEntityToFHIRConditionTransformer;

    @Override
    public void save(ConditionEntity condition) {

    }

    @Override
    public Condition read(IdType theId) {
        ConditionEntity condition = (ConditionEntity) em.find(ConditionEntity.class,Long.parseLong(theId.getIdPart()));

        return condition == null
                ? null
                : conditionEntityToFHIRConditionTransformer.transform(condition);
    }

    @Override
    public Condition create(Condition condition, IdType theId, String theConditional) {
        return null;
    }


    @Override
    public List<Condition> search(ReferenceParam patient, TokenParam category, TokenParam clinicalstatus, DateRangeParam asserted) {
        List<ConditionEntity> qryResults = searchEntity(patient, category, clinicalstatus, asserted);
        List<Condition> results = new ArrayList<>();

        for (ConditionEntity conditionEntity : qryResults)
        {
            // log.trace("HAPI Custom = "+doc.getId());
            Condition condition = conditionEntityToFHIRConditionTransformer.transform(conditionEntity);
            results.add(condition);
        }

        return results;
    }

    @Override
    public List<ConditionEntity> searchEntity(ReferenceParam patient, TokenParam category, TokenParam clinicalstatus, DateRangeParam asserted) {
        List<ConditionEntity> qryResults = null;

        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<ConditionEntity> criteria = builder.createQuery(ConditionEntity.class);
        Root<ConditionEntity> root = criteria.from(ConditionEntity.class);

        List<Predicate> predList = new LinkedList<Predicate>();
        List<Condition> results = new ArrayList<Condition>();

        if (patient != null) {
            Join<ConditionEntity, PatientEntity> join = root.join("patient", JoinType.LEFT);

            Predicate p = builder.equal(join.get("id"),patient.getIdPart());
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

        qryResults = em.createQuery(criteria).getResultList();
        return qryResults;
    }
}