/*
 * Copyright (c) 2014-2017 Globo.com - ATeam
 * All rights reserved.
 *
 * This source is subject to the Apache License, Version 2.0.
 * Please see the LICENSE file for more information.
 *
 * Authors: See AUTHORS file
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.globo.ateam.taurina.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.globo.ateam.taurina.model.Result;
import com.globo.ateam.taurina.model.Scenario;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.JSR223Listener;
import org.apache.jorphan.collections.HashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.UUID;

@Service
public class JmeterService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final ObjectMapper mapper = new ObjectMapper();
    private final FilesService filesService;

    @Autowired
    public JmeterService(FilesService filesService) {
        this.filesService = filesService;
    }

    public Result start(long testId, Scenario scenario) throws IOException {

        String domain = scenario.getDomain();
        Integer port = scenario.getPort();
        String path = scenario.getPath();
        String method = scenario.getMethod();
        Integer loopCount = scenario.getLoopCount();
        Integer numThreads = scenario.getNumThreads();
        long duration = scenario.getDuration();
        Integer rampUp = scenario.getRampUp();
        String statsdPrefix = scenario.statsdPrefix();
        String statsdServer = scenario.statsdServer();
        Integer statsdPort = scenario.statsdPort();
        log.info("test #" + testId + ": " + mapper.writeValueAsString(scenario));

        System.setProperty("jmeter.logfile", filesService.pathIdDirectory(testId) + "/jmeter.log");

        String result = "";
        File jmeterHome = new File(filesService.jmeterHome());
        if (jmeterHome.exists()) {
            File jmeterProperties = new File(filesService.props());
            if (jmeterProperties.exists()) {
                StandardJMeterEngine jmeter = new StandardJMeterEngine();

                jmeterInitializer(jmeterHome, jmeterProperties);

                HashTree testPlanTree = new HashTree();

                JSR223Listener jsr223Listener = new JSR223Listener();
                jsr223Listener.setProperty("cacheKey", UUID.randomUUID().toString());
                jsr223Listener.setProperty("scriptLanguage", "beanshell");
                jsr223Listener.setProperty("script",
                        "import com.timgroup.statsd.StatsDClient;" +
                        "if (vars.getObject(\"statsd\") == null) " +
                        "vars.putObject(\"statsd\", new com.timgroup.statsd.NonBlockingStatsDClient(\"" + statsdPrefix + "\", \"" + statsdServer + "\", " + statsdPort + "));" +
                        "StatsDClient statsd = (StatsDClient) vars.getObject(\"statsd\"); " +
                        "long reqTime = sampler.sample().getTime(); " +
                        "if (reqTime == 0L) reqTime=1L; " +
                        "statsd.incrementCounter(\"http\" + sampler.sample().getResponseCode()); " +
                        "statsd.recordExecutionTime(\"requestTime\", reqTime); " +
                        "statsd.recordGaugeValue(\"bytesPerSec\", (sampler.sample().getBytes()/reqTime));"
                );

                HTTPSamplerProxy httpSamplerProxy = new HTTPSamplerProxy();
                httpSamplerProxy.setDomain(domain);
                httpSamplerProxy.setPort(port);
                httpSamplerProxy.setPath(path);
                httpSamplerProxy.setMethod(method);
                httpSamplerProxy.setName(HTTPSamplerProxy.class.getSimpleName());
                httpSamplerProxy.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
                httpSamplerProxy.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());

                LoopController loopController = new LoopController();
                loopController.setLoops(loopCount);
                loopController.setFirst(true);
                loopController.setProperty(TestElement.TEST_CLASS, LoopController.class.getName());
                loopController.setProperty(TestElement.GUI_CLASS, LoopControlPanel.class.getName());
                loopController.initialize();

                ThreadGroup threadGroup = new ThreadGroup();
                threadGroup.setName(ThreadGroup.class.getSimpleName());
                threadGroup.setNumThreads(numThreads);
                threadGroup.setDuration(duration);
                threadGroup.setRampUp(rampUp);
                threadGroup.setSamplerController(loopController);
                threadGroup.setProperty(TestElement.TEST_CLASS, ThreadGroup.class.getName());
                threadGroup.setProperty(TestElement.GUI_CLASS, ThreadGroupGui.class.getName());

                TestPlan testPlan = new TestPlan("Created by Taurina");
                testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
                testPlan.setProperty(TestElement.GUI_CLASS, TestPlanGui.class.getName());
                testPlan.setUserDefinedVariables((Arguments) new ArgumentsPanel().createTestElement());

                testPlanTree.add(testPlan);
                HashTree threadGroupHashTree = testPlanTree.add(testPlan, threadGroup);
                HashTree samplerHashTree = threadGroupHashTree.add(httpSamplerProxy);
                samplerHashTree.add(jsr223Listener);

                jmeter.configure(testPlanTree);
                jmeter.run();
            }
            return new Result(testId, result.getBytes(Charset.defaultCharset()));
        }

        log.error("jmeter.home property is not set or pointing to incorrect location");
        return new Result(-1L, new byte[0]);
    }

    private void jmeterInitializer(File jmeterHome, File jmeterProperties) {
        JMeterUtils.setJMeterHome(jmeterHome.getPath());
        JMeterUtils.loadJMeterProperties(jmeterProperties.getPath());
        JMeterUtils.initLogging();
        JMeterUtils.initLocale();
    }
}
