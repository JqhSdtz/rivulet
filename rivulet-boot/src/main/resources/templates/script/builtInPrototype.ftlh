<#-- @ftlvariable name="tableClass" type="java.lang.Class" -->
export class ${tableClass.getSimpleName()} {
    static JavaType = Java.type('${tableClass.getName()}');
    constructor(javaObject) {
        if (typeof javaObject !== 'undefined') {
            this.javaObject = javaObject;
        } else {
            this.javaObject = new JavaType();
        }
    }

    getJavaObject() {return this.javaObject;}

<#list sortedMethods as method>
    ${method.getName()}(<#list method.getParameters() as param>${(param?index != 0)?string(',' ,'')}${param.getName()}</#list>) {return this.javaObject.${method.getName()}(<#list method.getParameters() as param>${(param?index != 0)?string(',' ,'')}${param.getName()}</#list>);}
</#list>
}