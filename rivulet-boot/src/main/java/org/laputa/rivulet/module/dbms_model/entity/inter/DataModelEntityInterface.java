package org.laputa.rivulet.module.dbms_model.entity.inter;

public interface DataModelEntityInterface {
    Boolean getBuiltIn();
    void setBuiltIn(Boolean builtIn);

    String getTitle();
    void setTitle(String title);

    String getCode();
    void setCode(String code);
}
