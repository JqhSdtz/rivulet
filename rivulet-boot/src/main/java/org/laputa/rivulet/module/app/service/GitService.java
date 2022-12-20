package org.laputa.rivulet.module.app.service;

import cn.hutool.core.io.FileUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.laputa.rivulet.module.app.property.GitProperty;
import org.laputa.rivulet.module.data_model.entity.RvPrototype;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

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
        } catch (GitAPIException e) {
            throw e;
        }
    }

    @SneakyThrows
    public void removeBuiltInRvPrototypes(List<RvPrototype> rvPrototypes) {
        if (rvPrototypes.size() == 0) return;
        RmCommand rmCommand = gitRepo.rm();
        rvPrototypes.forEach(rvPrototype -> {
            String filePath = "/prototypes/builtIn/" + rvPrototype.getName();
            rmCommand.addFilepattern(filePath);
        });
        rmCommand.call();
    }

    @SneakyThrows
    public void addBuiltInRvPrototypes(List<RvPrototype> rvPrototypes) {
        if (rvPrototypes.size() == 0) return;
        AddCommand addCommand = gitRepo.add();
        rvPrototypes.forEach(rvPrototype -> {
            String filePath = "/prototypes/builtIn/" + rvPrototype.getName() + ".js";
            File rvPrototypeFile = FileUtil.touch(FileUtil.normalize(gitProperty.getLocalDir() + File.separator + filePath));
            String content = rvPrototype.getTitle();
            FileUtil.writeString(content, rvPrototypeFile, StandardCharsets.UTF_8);
            addCommand.addFilepattern(filePath);
        });
        addCommand.call();
    }
}
