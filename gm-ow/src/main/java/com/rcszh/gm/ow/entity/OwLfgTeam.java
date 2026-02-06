package com.rcszh.gm.ow.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("ow_lfg_team")
public class OwLfgTeam {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String modeCode;

    private String platformCode;

    private Boolean allowCrossplay;

    private Integer capacity;

    private Integer memberCount;

    private Boolean autoApprove;

    private String regionCode;

    private String languageCode;

    private Integer voiceRequired;

    private String rankMin;

    private String rankMax;

    private String needRolesJson;

    private String preferredHeroCodesJson;

    private String tagsJson;

    private String note;

    private String contactJson;

    private String inviteCode;

    private String status;

    private LocalDateTime expiresAt;

    private Long createdByAccountId;

    private String createdByName;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getModeCode() {
        return modeCode;
    }

    public void setModeCode(String modeCode) {
        this.modeCode = modeCode;
    }

    public String getPlatformCode() {
        return platformCode;
    }

    public void setPlatformCode(String platformCode) {
        this.platformCode = platformCode;
    }

    public Boolean getAllowCrossplay() {
        return allowCrossplay;
    }

    public void setAllowCrossplay(Boolean allowCrossplay) {
        this.allowCrossplay = allowCrossplay;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }

    public Boolean getAutoApprove() {
        return autoApprove;
    }

    public void setAutoApprove(Boolean autoApprove) {
        this.autoApprove = autoApprove;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public Integer getVoiceRequired() {
        return voiceRequired;
    }

    public void setVoiceRequired(Integer voiceRequired) {
        this.voiceRequired = voiceRequired;
    }

    public String getRankMin() {
        return rankMin;
    }

    public void setRankMin(String rankMin) {
        this.rankMin = rankMin;
    }

    public String getRankMax() {
        return rankMax;
    }

    public void setRankMax(String rankMax) {
        this.rankMax = rankMax;
    }

    public String getNeedRolesJson() {
        return needRolesJson;
    }

    public void setNeedRolesJson(String needRolesJson) {
        this.needRolesJson = needRolesJson;
    }

    public String getPreferredHeroCodesJson() {
        return preferredHeroCodesJson;
    }

    public void setPreferredHeroCodesJson(String preferredHeroCodesJson) {
        this.preferredHeroCodesJson = preferredHeroCodesJson;
    }

    public String getTagsJson() {
        return tagsJson;
    }

    public void setTagsJson(String tagsJson) {
        this.tagsJson = tagsJson;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getContactJson() {
        return contactJson;
    }

    public void setContactJson(String contactJson) {
        this.contactJson = contactJson;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Long getCreatedByAccountId() {
        return createdByAccountId;
    }

    public void setCreatedByAccountId(Long createdByAccountId) {
        this.createdByAccountId = createdByAccountId;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

