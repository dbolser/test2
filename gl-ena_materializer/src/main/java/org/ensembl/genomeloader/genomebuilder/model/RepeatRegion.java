/*
 * Copyright [2009-2014] EMBL-European Bioinformatics Institute
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ensembl.genomeloader.genomebuilder.model;

/**
 * Created by IntelliJ IDEA.
 * User: arnaud
 * Date: 10-Jun-2008
 * Time: 17:07:34
 */
public interface RepeatRegion extends Locatable, CrossReferenced, Identifiable, Integr8ModelComponent {

    public interface RepeatUnit {

        public String getRepeatConsensus();

        public String getRepeatName();

        public String getRepeatClass();

        public String getRepeatType();
        
    }

    public double getScore();

    public int getRepeatStart();

    public int getRepeatEnd();

    public RepeatUnit getRepeatUnit();

    public String getAnalysis();
    
}

