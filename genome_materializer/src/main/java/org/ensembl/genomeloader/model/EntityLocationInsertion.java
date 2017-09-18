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

/**
 * File: EntityLocationInsertion.java
 * Created by: dstaines
 * Created on: Dec 17, 2007
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.model;

import org.ensembl.genomeloader.util.templating.TemplateBuilder;

/**
 * Concrete implementation of {@link EntityLocationModifier} representing an
 * insertion into a genomic sequence
 *
 * @author dstaines
 *
 */
public class EntityLocationInsertion extends EntityLocationModifier {

	private static final long serialVersionUID = 2642393533363905392L;
	/**
	 * @param start
	 *            start of insertion
	 * @param stop
	 *            end of insertion
	 * @param proteinSeq
	 *            inserted protein sequence
	 */
	public EntityLocationInsertion(int start, int stop, String proteinSeq) {
		super(start, stop, proteinSeq);
	}

	private final static String INSERT_PATTERN = "/insertion=\"$start$^$stop$,seq:$seq$\"";
	public String toString() {
		return TemplateBuilder.template(INSERT_PATTERN, "start", getStart(),
				"stop", getStop(), "seq", getProteinSeq());
	}

}
