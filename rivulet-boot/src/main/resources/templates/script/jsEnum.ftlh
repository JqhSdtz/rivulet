<#-- @ftlvariable name="enumClass" type="java.lang.Class" -->
export const ${enumClass.getSimpleName()} = {
<#list enumPairs?keys as key>
    ${key}: ${enumPairs[key]}${(key?index == enumPairs?size - 1)?string('' ,',')}
</#list>
};