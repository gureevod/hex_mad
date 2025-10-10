package com.company.hex.api.annotations.param;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated parameter should be sent as a form parameter
 * in application/x-www-form-urlencoded or multipart/form-data requests.
 * The value specifies the form field name.
 * 
 * <p>Example usage:
 * <pre>
 * {@code
 * @POST("/users/{id}/avatar")
 * Response uploadAvatar(@Path("id") int userId, 
 *                       @FormParam("file") byte[] fileContent,
 *                       @FormParam("filename") String filename);
 * }
 * </pre>
 * 
 * @author Hex Framework
 * @version 1.0
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface FormParam {
    /**
     * The name of the form parameter.
     * 
     * @return the form parameter name
     */
    String value();
}