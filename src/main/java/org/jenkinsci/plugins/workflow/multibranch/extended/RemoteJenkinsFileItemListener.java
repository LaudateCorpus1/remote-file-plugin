package org.jenkinsci.plugins.workflow.multibranch.extended;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.*;
import hudson.plugins.git.GitSCM;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.jenkinsci.plugins.workflow.flow.FlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.multibranch.extended.scm.ExtendedSCMBinder;

import java.io.IOException;
import java.util.StringJoiner;

@Extension
public class RemoteJenkinsFileItemListener extends EnvironmentContributor {

    public static final String RJPP_SCM_ENV_NAME = "RJPP_SCM_URL";
    public static final String RJPP_JFILE_ENV_NAME = "RJPP_JENKINSFILE";
    public static final String RJPP_BRANCH_ENV_NAME = "RJPP_BRANCH";

    @Override
    public void buildEnvironmentFor(@NonNull Run r, @NonNull EnvVars envs, @NonNull TaskListener listener) throws IOException, InterruptedException {
        if (r instanceof WorkflowRun) {
            WorkflowRun workflowRun = (WorkflowRun) r;
            WorkflowJob workflowJob = workflowRun.getParent();
            FlowDefinition flowDefinition = workflowJob.getDefinition();
            if (flowDefinition instanceof ExtendedSCMBinder) {
                ExtendedSCMBinder extendedSCMBinder = (ExtendedSCMBinder) flowDefinition;
                if (extendedSCMBinder.getRemoteJenkinsFileSCM() instanceof GitSCM) {
                    GitSCM gitSCM = (GitSCM) extendedSCMBinder.getRemoteJenkinsFileSCM();
                    StringJoiner scmUrls = new StringJoiner(",");
                    for (RemoteConfig remoteConfig : gitSCM.getRepositories()) {
                        for (URIish urIish : remoteConfig.getURIs()) {
                            scmUrls.add(urIish.toString());
                        }
                    }
                    String jenkinsFile = extendedSCMBinder.getRemoteJenkinsFile();
                    envs.put(RemoteJenkinsFileItemListener.RJPP_SCM_ENV_NAME, scmUrls.toString());
                    envs.put(RemoteJenkinsFileItemListener.RJPP_JFILE_ENV_NAME, jenkinsFile);
                    envs.put(RemoteJenkinsFileItemListener.RJPP_BRANCH_ENV_NAME, extendedSCMBinder.getRemoteJenkinsFileBranch());
                }
            }
        }
        super.buildEnvironmentFor(r, envs, listener);
    }
}
