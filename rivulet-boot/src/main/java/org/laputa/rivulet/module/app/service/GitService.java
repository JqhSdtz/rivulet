package org.laputa.rivulet.module.app.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ReflectUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import jakarta.annotation.Resource;
import liquibase.ext.hibernate.util.TableRemarkMetaInfo;
import liquibase.ext.hibernate.util.TableRemarkMetaInfoUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.laputa.rivulet.module.app.property.GitProperty;
import org.laputa.rivulet.module.dbms_model.entity.RvTable;
import org.laputa.rivulet.module.jpa_model.entity.RvPrototype;
import org.laputa.rivulet.module.jpa_model.service.JpaModelService;
import org.reflections.Reflections;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author JQH
 * @since 下午 4:15 22/09/04
 */

@Service
@Order(1001)
@Slf4j
public class GitService implements ApplicationRunner {

    @Resource
    private GitProperty gitProperty;
    @Resource
    private FreeMarkerConfigurer freeMarkerConfigurer;
    @Resource
    private JpaModelService jpaModelService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 业务代码git仓库
     */
    private Git gitRepo;

    @SneakyThrows
    @Override
    public void run(ApplicationArguments args) {
        if (gitProperty.getRepoUrl() == null) {
            throw new RuntimeException("请设置rivulet.git.repo-url属性！");
        } else if (gitProperty.getLocalDir() == null) {
            throw new RuntimeException("请设置rivulet.git.local-dir属性！");
        }
        FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder().findGitDir(new File(gitProperty.getLocalDir()));
        try {
            if (repositoryBuilder.getGitDir() != null) {
                gitRepo = Git.open(new File(gitProperty.getLocalDir()));
            } else {
                CloneCommand cloneCommand = Git.cloneRepository().setURI(gitProperty.getRepoUrl())
                        .setDirectory(new File(gitProperty.getLocalDir()));
                if (gitProperty.getUsername() != null && gitProperty.getPassword() != null) {
                    UsernamePasswordCredentialsProvider provider = new UsernamePasswordCredentialsProvider(gitProperty.getUsername(), gitProperty.getPassword());
                    cloneCommand.setCredentialsProvider(provider);
                }
                gitRepo = cloneCommand.call();
            }
            // 启动时即更新枚举类的js文件
            createJsEnums();
        } catch (IOException e) {
            throw new RuntimeException("git本地路径非法！");
        }
    }

    @SneakyThrows
    public void removeBuiltInRvTables(List<RvTable> rvTables) {
        if (rvTables.isEmpty()) return;
        RmCommand rmCommand = gitRepo.rm();
        rvTables.forEach(rvTable -> {
            String filePath = "/prototypes/builtIn/" + rvTable.getCode();
            rmCommand.addFilepattern(filePath);
        });
        rmCommand.call();
    }

    @SuppressWarnings("rawtypes")
    @SneakyThrows
    public void createJsEnums() {
        AddCommand addCommand = gitRepo.add();
        // 先给全部枚举类创建对应js文件
        Reflections reflections = new Reflections("org.laputa.rivulet.module");
        Set<Class<? extends Enum>> enumsClasses = reflections.getSubTypesOf(Enum.class);
        for (Class<? extends Enum> enumClass : enumsClasses) {
            Map<String, Object> enumPairs = getEnumPairs(enumClass);
            String classFilePath = getClassFilePath(enumClass);
            String jsFilePath = "/src/enums/" + classFilePath + "/" + enumClass.getSimpleName() + ".js";
            File enumFile = FileUtil.touch(FileUtil.normalize(gitProperty.getLocalDir() + File.separator + jsFilePath));
            String content = getJsEnumContent(enumClass, enumPairs);
            FileUtil.writeString(content, enumFile, StandardCharsets.UTF_8);
            addCommand.addFilepattern(jsFilePath);
        }
        addCommand.call();
    }

    @SuppressWarnings("rawtypes")
    @SneakyThrows
    public void createJsPrototypes(List<Class<?>> tableClasses) {
        if (tableClasses.isEmpty()) return;
        AddCommand addCommand = gitRepo.add();
        List<RvPrototype> rvPropertyList = jpaModelService.getRvPrototypeList();
        Map<String, RvPrototype> rvPrototypeMap = new HashMap<>();
        rvPropertyList.forEach(rvPrototype -> {
            TableRemarkMetaInfo metaInfo = TableRemarkMetaInfoUtil.getMetaInfo(rvPrototype.getRemark());
            rvPrototypeMap.put(metaInfo.getClassName(), rvPrototype);
        });
        objectMapper.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, false);
        for (Class<?> tableClass : tableClasses) {
            if (tableClass == null) continue;
            String classFilePath = getClassFilePath(tableClass);
            String jsFilePath = "/src/prototypes/" + classFilePath + "/" + tableClass.getSimpleName() + ".js";
            File rvPrototypeFile = FileUtil.touch(FileUtil.normalize(gitProperty.getLocalDir() + File.separator + jsFilePath));
            RvPrototype rvPrototype = rvPrototypeMap.get(tableClass.getName());
            String rvPrototypeJson = objectMapper.writeValueAsString(rvPrototype);
            String content = getJsPrototypeContent(tableClass, rvPrototypeJson);
            FileUtil.writeString(content, rvPrototypeFile, StandardCharsets.UTF_8);
            addCommand.addFilepattern(jsFilePath);
        }
        addCommand.call();
    }

    @SneakyThrows
    private String getJsPrototypeContent(Class<?> tableClass, String rvPrototypeJson) {
        Configuration configuration = freeMarkerConfigurer.getConfiguration();
        Template template = configuration.getTemplate("script/jsPrototype.ftlh");
        Map<String, Object> dataModel = new HashMap<>();
        List<Method> sortedMethods = Arrays.stream(tableClass.getMethods()).sorted(Comparator.comparing(Method::getName)).toList();
        dataModel.put("sortedMethods", sortedMethods);
        dataModel.put("tableClass", tableClass);
        dataModel.put("rvPrototypeJson", rvPrototypeJson);
        StringWriter stringWriter = new StringWriter();
        template.process(dataModel, stringWriter);
        return stringWriter.toString();
    }

    @SneakyThrows
    private String getJsEnumContent(Class<?> enumClass, Map<String, Object> enumPairs) {
        Configuration configuration = freeMarkerConfigurer.getConfiguration();
        Template template = configuration.getTemplate("script/jsEnum.ftlh");
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("enumClass", enumClass);
        dataModel.put("enumPairs", enumPairs);
        StringWriter stringWriter = new StringWriter();
        template.process(dataModel, stringWriter);
        return stringWriter.toString();
    }

    private String getClassFilePath(Class<?> tableClass) {
        Package pkg = tableClass.getPackage();
        String classFilePath;
        if (pkg == null) {
            classFilePath = "/";
        } else {
            String pkgPath = pkg.getName().replace(".", "/");
            String[] parts = pkgPath.split("/");
            if (parts.length <= 3) {
                classFilePath = "/";
            } else {
                StringBuilder result = new StringBuilder();
                for (int i = 3; i < parts.length; i++) {
                    if (i > 3) result.append('/');
                    result.append(parts[i]);
                }
                classFilePath = result.toString();
            }
        }
        return classFilePath;
    }

    @SuppressWarnings("rawtypes")
    private Map<String, Object> getEnumPairs(Class<? extends Enum> enumClass) {
        Map<String, Object> enumPairsMap = new HashMap<>();
        Enum<?>[] enumConstants = enumClass.getEnumConstants();
        for (Enum<?> enumConstant : enumConstants) {
            String name = enumConstant.name();
            Object value = getEnumValue(enumConstant);
            enumPairsMap.put(name, value);
        }
        return enumPairsMap;
    }

    @SneakyThrows
    private Object getEnumValue(Enum<?> enumConstant) {
        Class<?> enumClass = enumConstant.getClass();
        Method method;
        Object result;
        Field field;
        // 策略1：尝试调用getValue()方法
        method = ReflectUtil.getMethodByName(enumClass, "getValue");
        if (method != null) {
            result = method.invoke(enumConstant);
            if (result != null) return result;
        }
        // 策略2：尝试调用getCode()方法
        method = ReflectUtil.getMethodByName(enumClass, "getCode");
        if (method != null) {
            result = method.invoke(enumConstant);
            if (result != null) return result;
        }
        // 策略3：尝试读取value字段
        field = ReflectUtil.getField(enumClass, "value");
        if (field != null) {
            field.setAccessible(true);
            result = field.get(enumConstant);
            if (result != null) return result;
        }
        // 策略4：尝试读取code字段
        field = ReflectUtil.getField(enumClass, "code");
        if (field != null) {
            field.setAccessible(true);
            result = field.get(enumConstant);
            if (result != null) return result;
        }
        // 策略5：以上都没有，退回到ordinal()（枚举的声明顺序）
        Method ordinalMethod = Enum.class.getMethod("ordinal");
        result = ordinalMethod.invoke(enumConstant);
        return result;
    }

}
