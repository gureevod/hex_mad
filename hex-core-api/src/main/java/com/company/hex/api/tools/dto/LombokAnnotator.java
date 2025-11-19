package com.company.hex.api.tools.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.*;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.Jackson2Annotator;

/**
 * Custom annotator that adds Lombok annotations to generated DTOs.
 * <p>
 * Extends Jackson2Annotator to preserve Jackson annotations while adding Lombok support.
 * </p>
 */
class LombokAnnotator extends Jackson2Annotator {
    
    private final boolean useLombok;
    private final boolean includeHashcodeAndEquals;
    private final boolean includeToString;
    
    public LombokAnnotator(GenerationConfig config, boolean useLombok, boolean includeHashcodeAndEquals, boolean includeToString) {
        super(config);
        this.useLombok = useLombok;
        this.includeHashcodeAndEquals = includeHashcodeAndEquals;
        this.includeToString = includeToString;
    }
    
    @Override
    public void propertyOrder(JDefinedClass clazz, JsonNode propertiesNode) {
        // Skip @JsonPropertyOrder to reduce verbosity
        // Properties will still be serialized correctly with @JsonProperty
    }
    
    @Override
    public void propertyInclusion(JDefinedClass clazz, JsonNode schema) {
        // Add @JsonIgnoreProperties(ignoreUnknown = true) for flexibility
        try {
            JClass jsonIgnorePropertiesClass = clazz.owner().ref("com.fasterxml.jackson.annotation.JsonIgnoreProperties");
            JAnnotationUse annotation = clazz.annotate(jsonIgnorePropertiesClass);
            annotation.param("ignoreUnknown", true);
        } catch (Exception e) {
            // Ignore if annotation fails
        }
        
        // Don't add @JsonInclude to reduce verbosity - use defaults
    }
    
    @Override
    public void propertyField(JFieldVar field, JDefinedClass clazz, String propertyName, JsonNode propertyNode) {
        super.propertyField(field, clazz, propertyName, propertyNode);
        
        // Make fields private when using Lombok
        if (useLombok) {
            field.mods().setPrivate();
        }
    }
    
    /**
     * Called when a class is generated - add Lombok annotations here.
     */
    public void addLombokAnnotations(JDefinedClass clazz) {
        if (!useLombok) {
            return;
        }
        
        try {
            // Use @Data for compact output (includes @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor)
            // Note: @Data respects includeHashcodeAndEquals and includeToString settings
            if (includeHashcodeAndEquals && includeToString) {
                clazz.annotate(clazz.owner().ref("lombok.Data"));
            } else {
                // If not using default settings, add annotations individually
                clazz.annotate(clazz.owner().ref("lombok.Getter"));
                clazz.annotate(clazz.owner().ref("lombok.Setter"));
                
                if (includeHashcodeAndEquals) {
                    clazz.annotate(clazz.owner().ref("lombok.EqualsAndHashCode"));
                }
                
                if (includeToString) {
                    clazz.annotate(clazz.owner().ref("lombok.ToString"));
                }
            }
            
            clazz.annotate(clazz.owner().ref("lombok.Builder"));
            clazz.annotate(clazz.owner().ref("lombok.NoArgsConstructor"));
            clazz.annotate(clazz.owner().ref("lombok.AllArgsConstructor"));
            
        } catch (Exception e) {
            // Lombok annotations may fail if not on classpath - that's OK
        }
    }
}