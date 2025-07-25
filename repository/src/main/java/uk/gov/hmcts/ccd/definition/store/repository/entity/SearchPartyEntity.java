package uk.gov.hmcts.ccd.definition.store.repository.entity;

import java.io.Serializable;
import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import static jakarta.persistence.FetchType.LAZY;

@Table(name = "search_party")
@Entity
public class SearchPartyEntity implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "live_from")
    private Date liveFrom;

    @Column(name = "live_to")
    private Date liveTo;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "case_type_id", nullable = false)
    private CaseTypeEntity caseType;

    @Column(name = "search_party_name")
    private String searchPartyName;

    @Column(name = "search_party_email_address")
    private String searchPartyEmailAddress;

    @Column(name = "search_party_address_line_1")
    private String searchPartyAddressLine1;

    @Column(name = "search_party_post_code")
    private String searchPartyPostCode;

    @Column(name = "search_party_dob")
    private String searchPartyDob;

    @Column(name = "search_party_dod")
    private String searchPartyDod;

    @Column(name = "search_party_collection_field_name")
    private String searchPartyCollectionFieldName;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getLiveFrom() {
        return liveFrom;
    }

    public void setLiveFrom(Date liveFrom) {
        this.liveFrom = liveFrom;
    }

    public Date getLiveTo() {
        return liveTo;
    }

    public void setLiveTo(Date liveTo) {
        this.liveTo = liveTo;
    }

    public CaseTypeEntity getCaseType() {
        return caseType;
    }

    public void setCaseType(CaseTypeEntity caseType) {
        this.caseType = caseType;
    }

    public String getSearchPartyName() {
        return searchPartyName;
    }

    public void setSearchPartyName(String searchPartyName) {
        this.searchPartyName = searchPartyName;
    }

    public String getSearchPartyEmailAddress() {
        return searchPartyEmailAddress;
    }

    public void setSearchPartyEmailAddress(String searchPartyEmailAddress) {
        this.searchPartyEmailAddress = searchPartyEmailAddress;
    }

    public String getSearchPartyAddressLine1() {
        return searchPartyAddressLine1;
    }

    public void setSearchPartyAddressLine1(String searchPartyAddressLine1) {
        this.searchPartyAddressLine1 = searchPartyAddressLine1;
    }

    public String getSearchPartyPostCode() {
        return searchPartyPostCode;
    }

    public void setSearchPartyPostCode(String searchPartyPostCode) {
        this.searchPartyPostCode = searchPartyPostCode;
    }

    public String getSearchPartyDob() {
        return searchPartyDob;
    }

    public void setSearchPartyDob(String searchPartyDob) {
        this.searchPartyDob = searchPartyDob;
    }

    public String getSearchPartyDod() {
        return searchPartyDod;
    }

    public void setSearchPartyDod(String searchPartyDod) {
        this.searchPartyDod = searchPartyDod;
    }

    public String getSearchPartyCollectionFieldName() {
        return searchPartyCollectionFieldName;
    }

    public void setSearchPartyCollectionFieldName(String searchPartyCollectionFieldName) {
        this.searchPartyCollectionFieldName = searchPartyCollectionFieldName;
    }
}
