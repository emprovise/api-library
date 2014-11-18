package com.emprovise.api.rally.exception;


/**
 *  RallyPasswordExpiredException is thrown when the rally account password expires after a 3 month period,
 *  and connection to rally fails.
 */
public class RallyPasswordExpiredException extends RuntimeException {

    private static final long serialVersionUID = 2433826402543036552L;

    public RallyPasswordExpiredException()  {
        super("Rally Password has been expired. Please reset the rally password for the rally user.<br/>");
    }
}
