<#-- @ftlvariable name="enumClass" type="java.lang.Class" -->
export const ${enumClass.getSimpleName()} = {
<#list enumPairs?keys as key>
    ${key}: <#if enumPairs[key]?is_string>'${enumPairs[key]}'<#else>${enumPairs[key]}</#if>${(key?index == enumPairs?size - 1)?string('' ,',')}
</#list>
};