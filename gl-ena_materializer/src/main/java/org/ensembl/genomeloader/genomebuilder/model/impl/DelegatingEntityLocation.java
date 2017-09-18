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
 * File: Locatable.java
 * Created by: dstaines
 * Created on: Oct 4, 2007
 * CVS:  $Id$
 */

package org.ensembl.genomeloader.genomebuilder.model.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.biojava.bio.Annotation;
import org.biojava.bio.symbol.Location;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.utils.ChangeListener;
import org.biojava.utils.ChangeType;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.CrossRef;
import org.biojavax.CrossReferenceResolver;
import org.biojavax.bio.seq.Position;
import org.biojavax.bio.seq.PositionResolver;
import org.biojavax.bio.seq.RichFeature;
import org.biojavax.bio.seq.RichLocation;
import org.biojavax.ontology.ComparableTerm;
import org.ensembl.genomeloader.genomebuilder.model.EntityLocation;
import org.ensembl.genomeloader.genomebuilder.model.EntityLocationException;
import org.ensembl.genomeloader.genomebuilder.model.EntityLocationInsertion;
import org.ensembl.genomeloader.util.biojava.LocationUtils;
import org.ensembl.genomeloader.util.collections.CollectionUtils;

/**
 * Delegating implementation which decorates a {@link RichLocation} with a
 * {@link MappingState}
 *
 * @author dstaines
 *
 */
public class DelegatingEntityLocation implements EntityLocation {

    private static final long serialVersionUID = 5326897202357684785L;

    public static Location getLocation(Location location) {
        if (location != null && DelegatingEntityLocation.class.isAssignableFrom(location.getClass())) {
            return ((DelegatingEntityLocation) location).location;
        } else {
            return location;
        }
    }

    private List<EntityLocationException> exceptions;
    private List<EntityLocationInsertion> insertions;
    protected transient RichLocation location;
    protected MappingState state = MappingState.ANNOTATED;

    public DelegatingEntityLocation(EntityLocation target, RichLocation loc) {
        this.location = loc;
        setState(target.getState());
        getExceptions().addAll(target.getExceptions());
        getInsertions().addAll(target.getInsertions());
    }

    public DelegatingEntityLocation(RichLocation location) {
        this.location = location;
    }

    public DelegatingEntityLocation(RichLocation location, MappingState state) {
        this.location = location;
        this.state = state;
    }

    public void addChangeListener(ChangeListener arg0) {
        location.addChangeListener(arg0);
    }

    public void addChangeListener(ChangeListener arg0, ChangeType arg1) {
        location.addChangeListener(arg0, arg1);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.ensembl.genomeloader.genomebuilder.model.EntityLocation#addException(uk.ac.
     * ebi.proteome.genomebuilder.model.EntityLocationException)
     */
    public void addException(EntityLocationException exception) {
        getExceptions().add(exception);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.ensembl.genomeloader.genomebuilder.model.EntityLocation#addInsertion(uk.ac.
     * ebi.proteome.genomebuilder.model.EntityLocationInsertion)
     */
    public void addInsertion(EntityLocationInsertion insertion) {
        getInsertions().add(insertion);
    }

    public Iterator blockIterator() {
        return location.blockIterator();
    }

    public int compareTo(Object o) {
        return location.compareTo(o);
    }

    public boolean contains(int arg0) {
        return location.contains(arg0);
    }

    public boolean contains(Location arg0) {
        return location.contains(getLocation(arg0));
    }

    public boolean equals(Object arg0) {
        return location.equals(arg0);
    }

    public Annotation getAnnotation() {
        return location.getAnnotation();
    }

    public int getCircularLength() {
        return location.getCircularLength();
    }

    public CrossRef getCrossRef() {
        return location.getCrossRef();
    }

    public Location getDecorator(Class arg0) {
        return location.getDecorator(arg0);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.ensembl.genomeloader.genomebuilder.model.EntityLocation#getExceptions()
     */
    public List<EntityLocationException> getExceptions() {
        if (exceptions == null) {
            exceptions = CollectionUtils.createArrayList();
        }
        return exceptions;
    }

    public RichFeature getFeature() {
        return location.getFeature();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.ensembl.genomeloader.genomebuilder.model.Integr8ModelComponent#getIdString(
     * )
     */
    public String getIdString() {
        return toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.ensembl.genomeloader.genomebuilder.model.EntityLocation#getInsertions()
     */
    public List<EntityLocationInsertion> getInsertions() {
        if (insertions == null) {
            insertions = CollectionUtils.createArrayList();
        }
        return insertions;
    }

    public int getMax() {
        return location.getMax();
    }

    public Position getMaxPosition() {
        return location.getMaxPosition();
    }

    public int getMin() {
        return location.getMin();
    }

    public Position getMinPosition() {
        return location.getMinPosition();
    }

    public Set getNoteSet() {
        return location.getNoteSet();
    }

    public int getRank() {
        return location.getRank();
    }

    public MappingState getState() {
        return state;
    }

    public Strand getStrand() {
        return location.getStrand();
    }

    public ComparableTerm getTerm() {
        return location.getTerm();
    }

    public Location intersection(Location arg0) {
        return location.intersection(arg0);
    }

    public boolean isContiguous() {
        return location.isContiguous();
    }

    public boolean isUnchanging(ChangeType arg0) {
        return location.isUnchanging(arg0);
    }

    public Location newInstance(Location arg0) {
        return location.newInstance(getLocation(arg0));
    }

    public boolean overlaps(Location arg0) {
        return location.overlaps(getLocation(arg0));
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        // in.defaultReadObject();
        int nLocs = (Integer) in.readObject();
        List<RichLocation> locs = CollectionUtils.createArrayList(nLocs);
        for (int i = 0; i < nLocs; i++) {
            String locStr = (String) in.readObject();
            RichLocation loc = LocationUtils.parseEmblLocation(locStr);
            int circular = (Integer) in.readObject();
            loc.setCircularLength(circular);
            Set notes = (Set) in.readObject();
            loc.setNoteSet(notes);
        }
        location = LocationUtils.construct(locs);
    }

    public void removeChangeListener(ChangeListener arg0) {
        location.removeChangeListener(arg0);
    }

    public void removeChangeListener(ChangeListener arg0, ChangeType arg1) {
        location.removeChangeListener(arg0, arg1);
    }

    public void setCircularLength(int arg0) throws ChangeVetoException {
        location.setCircularLength(arg0);
    }

    public void setCrossRefResolver(CrossReferenceResolver arg0) {
        location.setCrossRefResolver(arg0);
    }

    public void setFeature(RichFeature arg0) throws ChangeVetoException {
        location.setFeature(arg0);
    }

    public void setNoteSet(Set arg0) throws ChangeVetoException {
        location.setNoteSet(arg0);
    }

    public void setPositionResolver(PositionResolver arg0) {
        location.setPositionResolver(arg0);
    }

    public void setRank(int arg0) throws ChangeVetoException {
        location.setRank(arg0);
    }

    public void setState(MappingState state) {
        this.state = state;
    }

    public void setTerm(ComparableTerm arg0) throws ChangeVetoException {
        location.setTerm(arg0);
    }

    public void sort() {
        location.sort();
    }

    public SymbolList symbols(SymbolList arg0) {
        return location.symbols(arg0);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(LocationUtils.locationToEmblFormat(location));
        sb.append('(');
        sb.append(state);
        sb.append(')');
        sb.append(",insertions=[" + StringUtils.join(getInsertions().iterator(), ',') + ']');
        sb.append(",exceptions=[" + StringUtils.join(getExceptions().iterator(), ',') + ']');
        return sb.toString();
    }

    public Location translate(int arg0) {
        return location.translate(arg0);
    }

    public Location union(Location arg0) {
        return location.union(getLocation(arg0));
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        List<RichLocation> locs = CollectionUtils.createArrayList();
        for (Iterator<RichLocation> locI = location.blockIterator(); locI.hasNext();) {
            locs.add(locI.next());
        }
        out.writeObject(locs.size());
        for (RichLocation loc : locs) {
            out.writeObject(LocationUtils.locationToEmblFormat(loc));
            out.writeObject(loc.getCircularLength());
            out.writeObject(loc.getNoteSet());
        }
    }

}
