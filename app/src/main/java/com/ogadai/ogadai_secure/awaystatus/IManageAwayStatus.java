package com.ogadai.ogadai_secure.awaystatus;

/**
 * Created by alee on 22/02/2016.
 */
public interface IManageAwayStatus {
    void setAwayStatus(String action);
    void retryPending();
}
