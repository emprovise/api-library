package com.emprovise.api.rally.exception;

/**
 *  RallyConcurrencyConflictException is thrown when a failure occurs while updating the user-story or the defect
 *  by Rally-Api because it was been modified simultaneously by another user.
 */
public class RallyConcurrencyConflictException extends RuntimeException {

    private static final long serialVersionUID = 819521807034654198L;

    public RallyConcurrencyConflictException(final String aMessage)  {
        super(aMessage);
    }
}
