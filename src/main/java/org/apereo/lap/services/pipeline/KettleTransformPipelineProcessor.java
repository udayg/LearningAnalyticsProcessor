/**
 * Copyright 2013 Unicon (R) Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.apereo.lap.services.pipeline;

import org.apache.commons.lang.StringUtils;
import org.apereo.lap.model.PipelineConfig;
import org.apereo.lap.model.Processor;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.scoring.WekaScoringMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.jsoninput.JsonInputMeta;
import org.pentaho.di.trans.steps.jsonoutput.JsonOutputMeta;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * Handles the pipeline processing for Kettle processors
 *
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 * @author Robert Long (rlong @ unicon.net)
 */
@Component
public class KettleTransformPipelineProcessor extends KettleBasePipelineProcessor {

    @PostConstruct
    public void init() {
        // Do any init here you need to (but note this is for the service and not each run)
        setKettlePluginsDirectory();
    }

    @Override
    public Processor.ProcessorType getProcessorType() {
        return Processor.ProcessorType.KETTLE_TRANSFORM;
    }

    @Override
    public ProcessorResult process(PipelineConfig pipelineConfig, Processor processorConfig) {
        ProcessorResult result = new ProcessorResult(Processor.ProcessorType.KETTLE_TRANSFORM);
        File kettleXMLFile = getFile(processorConfig.filename);

        try {
            KettleEnvironment.init(false);
            EnvUtil.environmentInit();
            TransMeta transMeta = new TransMeta(kettleXMLFile.getAbsolutePath());

            List<StepMeta> stepMetaList = transMeta.getSteps();
            for (StepMeta stepMeta : stepMetaList) {
                logger.info("Processing step: "+stepMeta.getName()+" in file: "+kettleXMLFile.getAbsolutePath());
                stepMeta.setChangedDate(new Date());
                File newFile = null;

                // set the file path to the one necessary, based on step type
                if (StringUtils.equalsIgnoreCase(stepMeta.getTypeId(), "JsonInput")){
                    newFile = getFile("/kettle/sample1_input.json");
                    JsonInputMeta jsonInputMeta = (JsonInputMeta) stepMeta.getStepMetaInterface();
                    jsonInputMeta.setFileName(new String[]{newFile.getAbsolutePath()});
                } else if (StringUtils.equalsIgnoreCase(stepMeta.getTypeId(), "JsonOutput")) {
                    newFile = createOutputFile("sample1_output");
                    JsonOutputMeta jsonOutputMeta = (JsonOutputMeta) stepMeta.getStepMetaInterface();
                    jsonOutputMeta.setFileName(newFile.getAbsolutePath());
                } else if (StringUtils.equalsIgnoreCase(stepMeta.getTypeId(), "WekaScoring")) {
                    newFile = getFile("/kettle/Marist_OAAI_ACADEMIC_RISK.xml");
                    WekaScoringMeta wekaScoringMeta = (WekaScoringMeta) stepMeta.getStepMetaInterface();
                    wekaScoringMeta.setSerializedModelFileName(newFile.getAbsolutePath());
                }
            }

            Trans trans = new Trans(transMeta);
            trans.calculateBatchIdAndDateRange();
            trans.beginProcessing();
            trans.execute(new String[]{});
            trans.waitUntilFinished();

            Result transResult = trans.getResult();
            result.done((int) transResult.getNrErrors(), null);
        } catch (Exception e) {
            // swallow exceptions for now...
        }

        return result;
    }

}