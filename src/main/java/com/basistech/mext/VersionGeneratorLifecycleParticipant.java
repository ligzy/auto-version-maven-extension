/*
* Copyright 2015 Basis Technology Corp.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.basistech.mext;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 */
@Component(role = AbstractMavenLifecycleParticipant.class, hint = "com.basistech.version")
public class VersionGeneratorLifecycleParticipant extends AbstractMavenLifecycleParticipant {
    private static final Logger LOG = LoggerFactory.getLogger(VersionGeneratorLifecycleParticipant.class);

    @Override
    public void afterSessionStart(MavenSession session) throws MavenExecutionException {
        File projectBase = new File(session.getRequest().getBaseDirectory());
        File policyFile = new File(projectBase, "version-policy.txt");
        if (policyFile.exists()) {
            String template;
            try {
                template = fileContents(policyFile);
            } catch (IOException e) {
                throw new MavenExecutionException("Failed to read " + policyFile.getAbsolutePath(), e);
            }

            String[] bits = template.split("=");
            String prop = bits[0];
            String valueTemplate = bits[1];

            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
            String timestamp = format.format(new Date());
            String rev = valueTemplate.replace("${timestamp}", timestamp);
            LOG.info("Setting {} to {}", prop, rev);
            session.getUserProperties().put(prop, rev);
        }
    }

    private String fileContents(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("utf-8")))) {
            return reader.readLine(); // we expect One Line.
        }
    }
}
