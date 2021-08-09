package uk.gov.hmcts.ccd.definition.store.repository.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

import static javax.persistence.FetchType.LAZY;

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
}
