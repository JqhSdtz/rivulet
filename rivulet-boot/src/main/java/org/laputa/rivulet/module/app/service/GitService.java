package org.laputa.rivulet.module.app.service;

import cn.hutool.core.io.FileUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import jakarta.annotation.Resource;
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
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author JQH
 * @since 下午 4:15 22/09/04
 */

@Service
@Order(1)
@Slf4j
public class GitService implements ApplicationRunner {

    @Resource
    private GitProperty gitProperty;
    @Resource
    private FreeMarkerConfigurer freeMarkerConfigurer;

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
        } catch (IOException e) {
            throw new RuntimeException("git本地路径非法！");
        }
    }

    @SneakyThrows
    public void removeBuiltInRvPrototypes(List<RvTable> rvTables) {
        if (rvTables.isEmpty()) return;
        RmCommand rmCommand = gitRepo.rm();
        rvTables.forEach(rvPrototype -> {
            String filePath = "/prototypes/builtIn/" + rvPrototype.getCode();
            rmCommand.addFilepattern(filePath);
        });
        rmCommand.call();
    }

    @SneakyThrows
    public void addBuiltInRvPrototypes(List<Class<?>> tableClasses) {
        if (tableClasses.isEmpty()) return;
        AddCommand addCommand = gitRepo.add();
        for (Class<?> tableClass : tableClasses) {
            if (tableClass == null) continue;
            String filePath = "/src/prototypes/builtIn/" + tableClass.getSimpleName() + ".js";
            File rvPrototypeFile = FileUtil.touch(FileUtil.normalize(gitProperty.getLocalDir() + File.separator + filePath));
            String content = getContent(tableClass);
            FileUtil.writeString(content, rvPrototypeFile, StandardCharsets.UTF_8);
            addCommand.addFilepattern(filePath);
        }
        addCommand.call();
    }

    @SneakyThrows
    private String getContent(Class<?> tableClass) {
        Configuration configuration = freeMarkerConfigurer.getConfiguration();
        Template template = configuration.getTemplate("script/builtInPrototype.ftlh");
        Map<String, Object> dataModel = new HashMap<>();
        List<Method> sortedMethods = Arrays.stream(tableClass.getMethods()).sorted(Comparator.comparing(Method::getName)).toList();
        dataModel.put("sortedMethods", sortedMethods);
        dataModel.put("tableClass", tableClass);
        StringWriter stringWriter = new StringWriter();
        template.process(dataModel, stringWriter);
        return stringWriter.toString();
    }
}
