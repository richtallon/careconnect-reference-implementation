package uk.nhs.careconnect.ri.database.entity.conceptMap;

import org.hl7.fhir.dstu3.model.Enumerations;
import uk.nhs.careconnect.ri.database.entity.BaseResource;
import uk.nhs.careconnect.ri.database.entity.codeSystem.ConceptEntity;
import uk.nhs.careconnect.ri.database.entity.valueSet.ValueSetEntity;
import uk.nhs.careconnect.ri.database.entity.valueSet.ValueSetInclude;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "ConceptMap")
public class ConceptMapEntity extends BaseResource {

	/*

Does not currently include target dependsOn TODO not required at present

ditto for target product

	 */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="CONCEPT_MAP_ID")
	private Long id;

	@Column(name = "URL")
	private String url;

	@OneToMany(mappedBy="conceptMap", targetEntity= ConceptMapIdentifier.class)
	private List<ConceptMapIdentifier> identifiers;

	@Column(name = "VERSION")
	private String version;

	@Column(name = "NAME")
	private String name;

	@Column(name = "TITLE")
	private String title;

	@Enumerated(EnumType.ORDINAL)
	@Column(name="status", nullable = false)
	Enumerations.PublicationStatus status;

	@Column(name="EXPERIMENTAL")
	private Boolean experimental;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CHANGE_DATE")
	private Date changedDate;

	@Column(name = "PUBLISHER")
	private String publisher;

	@OneToMany(mappedBy="conceptMap", targetEntity= ConceptMapTelecom.class)
	private List<ConceptMapTelecom> contacts;

	@Column(name = "DESCRIPTION")
	private String description;

	// useContext .. implement if required

	// jurisdiction ... hard code to UK

	@Column(name = "PURPOSE")
	private String purpose;

	@Column(name = "COPYRIGHT")
	private String copyright;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="SOURCE_VALUESET_ID",foreignKey= @ForeignKey(name="FK_SOURCE_VALUESET"))
	private ValueSetEntity xxsourceValueset;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="TARGET_VALUESET_ID",foreignKey= @ForeignKey(name="FK_TARGET_VALUESET"))
	private ValueSetEntity xxtargetValueset;


	@JoinColumn(name="SOURCE_VALUESET")
	private String sourceValueset;


	@JoinColumn(name="TARGET_VALUESET")
	private String targetValueset;

	@OneToMany(mappedBy="conceptMap", targetEntity= ConceptMapGroup.class)
	private List<ConceptMapGroup> groups;




	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	
	
	// ConceptMap IDENTIFIERS

	public void setIdentifiers(List<ConceptMapIdentifier> identifiers) {
		this.identifiers = identifiers;
	}
	public List<ConceptMapIdentifier> getIdentifiers( ) {
		if (identifiers == null) {
			identifiers = new ArrayList<ConceptMapIdentifier>();
		}
		return this.identifiers;
	}
	public List<ConceptMapIdentifier> addIdentifier(ConceptMapIdentifier pi) {
		identifiers.add(pi);
		return identifiers; }

	public List<ConceptMapIdentifier> removeIdentifier(ConceptMapIdentifier identifier){
		identifiers.remove(identifiers); return identifiers; }

	// ConceptMap Address


	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Enumerations.PublicationStatus getStatus() {
		return status;
	}

	public void setStatus(Enumerations.PublicationStatus status) {
		this.status = status;
	}

	public Boolean getExperimental() {
		return experimental;
	}

	public void setExperimental(Boolean experimental) {
		this.experimental = experimental;
	}

	public Date getChangedDate() {
		return changedDate;
	}

	public void setChangedDate(Date changedDate) {
		this.changedDate = changedDate;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public List<ConceptMapTelecom> getContacts() {
		return contacts;
	}

	public void setContacts(List<ConceptMapTelecom> contacts) {
		this.contacts = contacts;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	public String getCopyright() {
		return copyright;
	}

	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}

	public List<ConceptMapGroup> getGroups() {
		if (groups == null) {
			this.groups = new ArrayList<>();
		}
		return groups;
	}

	public void setGroups(List<ConceptMapGroup> groups) {
		this.groups = groups;
	}

	public ValueSetEntity getXxsourceValueset() {
		return xxsourceValueset;
	}

	public void setXxsourceValueset(ValueSetEntity xxsourceValueset) {
		this.xxsourceValueset = xxsourceValueset;
	}

	public ValueSetEntity getXxtargetValueset() {
		return xxtargetValueset;
	}

	public void setXxtargetValueset(ValueSetEntity xxtargetValueset) {
		this.xxtargetValueset = xxtargetValueset;
	}

	public String getSourceValueset() {
		return sourceValueset;
	}

	public void setSourceValueset(String sourceValueset) {
		this.sourceValueset = sourceValueset;
	}

	public String getTargetValueset() {
		return targetValueset;
	}

	public void setTargetValueset(String targetValueset) {
		this.targetValueset = targetValueset;
	}
}
