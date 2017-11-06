package com.emprovise.api.google.chrome.dao;

public class Cookie {

    private String creation_utc;
    private String host_key;
    private String name;
    private String value;
    private String path;
    private String expires_utc;
    private String secure;
    private String httponly;
    private String last_access_utc;
    private String has_expires;
    private String persistent;
    private String priority;
    private String encrypted_value;
    private String firstpartyonly;

    public String getCreationUTC() {
        return creation_utc;
    }

    public void setCreationUTC(String creation_utc) {
        this.creation_utc = creation_utc;
    }

    public String getHostKey() {
        return host_key;
    }

    public void setHostKey(String host_key) {
        this.host_key = host_key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getExpiresUTC() {
        return expires_utc;
    }

    public void setExpiresUTC(String expires_utc) {
        this.expires_utc = expires_utc;
    }

    public String getSecure() {
        return secure;
    }

    public void setSecure(String secure) {
        this.secure = secure;
    }

    public String getHttponly() {
        return httponly;
    }

    public void setHttponly(String httponly) {
        this.httponly = httponly;
    }

    public String getLastAccessUTC() {
        return last_access_utc;
    }

    public void setLastAccessUTC(String last_access_utc) {
        this.last_access_utc = last_access_utc;
    }

    public String getHasExpires() {
        return has_expires;
    }

    public void setHasExpires(String has_expires) {
        this.has_expires = has_expires;
    }

    public String getPersistent() {
        return persistent;
    }

    public void setPersistent(String persistent) {
        this.persistent = persistent;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getEncryptedValue() {
        return encrypted_value;
    }

    public void setEncryptedValue(String encrypted_value) {
        this.encrypted_value = encrypted_value;
    }

    public String getFirstPartyOnly() {
        return firstpartyonly;
    }

    public void setFirstPartyOnly(String firstpartyonly) {
        this.firstpartyonly = firstpartyonly;
    }
}
