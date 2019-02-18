package uk.nhs.careconnect.ri.database.daointerface;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.UriParam;
import org.hl7.fhir.dstu3.model.CodeSystem;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.ValueSet;
import uk.nhs.careconnect.fhir.OperationOutcomeException;
import uk.nhs.careconnect.ri.database.entity.codeSystem.CodeSystemEntity;
import uk.nhs.careconnect.ri.database.entity.codeSystem.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.codeSystem.SystemEntity;

import java.util.List;

public interface CodeSystemRepository {

    CodeSystemEntity findBySystem(String system);

    SystemEntity findSystem(String system) throws OperationOutcomeException;

    ConceptEntity findAddCode(CodeSystemEntity codeSystemEntity, ValueSet.ConceptReferenceComponent concept);

    CodeSystem read(FhirContext ctx, IdType theId) ;

    CodeSystem create(FhirContext ctx,CodeSystem codeSystem);

    void save(FhirContext ctx, CodeSystemEntity codeSystemEntity);

    List<CodeSystem> search (FhirContext ctx,
                             @OptionalParam(name = CodeSystem.SP_NAME) StringParam name,
                             @OptionalParam(name = CodeSystem.SP_PUBLISHER) StringParam publisher,
                             @OptionalParam(name = CodeSystem.SP_URL) UriParam url
    );
    public void setProcessDeferred(boolean theProcessDeferred);

 //   void saveDeferred();

}
