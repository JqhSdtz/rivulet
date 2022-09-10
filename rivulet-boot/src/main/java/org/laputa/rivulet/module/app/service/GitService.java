package org.laputa.rivulet.module.app.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.laputa.rivulet.module.app.property.GitProperty;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;

/**
 * @author JQH
 * @since 下午 4:15 22/09/04
 */

@Service
@Order(2)
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
}
