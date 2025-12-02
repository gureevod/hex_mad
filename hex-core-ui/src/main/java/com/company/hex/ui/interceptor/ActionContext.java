package com.company.hex.ui.interceptor;

import com.company.hex.ui.core.BaseElement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Неизменяемый контекст выполнения действия над элементом.
 */
public final class ActionContext {

    private final BaseElement element;
    private final String actionName;
    private final Object[] args;
    private final Map<String, Object> attributes;
    private final long startTime;

    private ActionContext(Builder builder) {
        this.element = Objects.requireNonNull(builder.element, "element");
        this.actionName = Objects.requireNonNull(builder.actionName, "actionName");
        this.args = builder.args != null ? builder.args.clone() : new Object[0];
        this.attributes = Collections.unmodifiableMap(new HashMap<>(builder.attributes));
        this.startTime = System.currentTimeMillis();
    }

    public BaseElement getElement() {
        return element;
    }

    public String getActionName() {
        return actionName;
    }

    public Object[] getArgs() {
        return args.clone();
    }

    public long getStartTime() {
        return startTime;
    }

    public long getElapsedMillis() {
        return System.currentTimeMillis() - startTime;
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return (T) attributes.get(key);
    }

    public boolean hasAttribute(String key) {
        return attributes.containsKey(key);
    }

    /**
     * Создает новый контекст с дополнительным атрибутом.
     */
    public ActionContext withAttribute(String key, Object value) {
        return toBuilder().attribute(key, value).build();
    }

    public Builder toBuilder() {
        return new Builder()
                .element(element)
                .actionName(actionName)
                .args(args)
                .attributes(new HashMap<>(attributes));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private BaseElement element;
        private String actionName;
        private Object[] args;
        private Map<String, Object> attributes = new HashMap<>();

        public Builder element(BaseElement element) {
            this.element = element;
            return this;
        }

        public Builder actionName(String actionName) {
            this.actionName = actionName;
            return this;
        }

        public Builder args(Object[] args) {
            this.args = args;
            return this;
        }

        public Builder attribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }

        public Builder attributes(Map<String, Object> attributes) {
            this.attributes = attributes;
            return this;
        }

        public ActionContext build() {
            return new ActionContext(this);
        }
    }

    @Override
    public String toString() {
        return String.format("ActionContext{element=%s, action=%s, args=%d}",
                element.getName(), actionName, args.length);
    }
}